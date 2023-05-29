package com.example.videoexp.utils

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.example.videoexp.model.VideoData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


 fun getVideoThumbnail(videoUri: Uri): Bitmap? {
    val thumbnailSize = ThumbnailUtils.OPTIONS_RECYCLE_INPUT
    val bitmap = ThumbnailUtils.createVideoThumbnail(videoUri.path!!, thumbnailSize)
    return if (bitmap != null) {
        Bitmap.createScaledBitmap(bitmap, 120, 120, true)
    } else {
        null
    }
}

suspend fun getAllVideo(context: Context, isLoading: (Boolean) -> Unit): ArrayList<VideoData> {
    return withContext(Dispatchers.IO) {
        isLoading(true)
        val videoList = ArrayList<VideoData>()
        val uri: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.MediaColumns.DATA,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Thumbnails.DATA
        )
        val orderBy = MediaStore.Images.Media.DATE_TAKEN

        val cursor: Cursor? = context.contentResolver.query(
            uri, projection, null, null,
            "$orderBy DESC"
        )

        val column_index_data = cursor?.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
        val column_index_folder_name =
            cursor?.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
        val column_index_display_name =
            cursor?.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
        val thum = cursor?.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.DATA)

        while (cursor?.moveToNext() == true) {
            val absolutePathOfVideo = cursor.getString(column_index_data!!)
            val folderName = cursor.getString(column_index_folder_name!!)
            val videoName = cursor.getString(column_index_display_name!!)
            val thumbnailPath = cursor.getString(thum!!)
            val thumbnailUri = getVideoThumbnail(Uri.parse(thumbnailPath))

            Log.e("Tag--VideoUri", absolutePathOfVideo)
            Log.e("Tag--folderName", folderName)
            Log.e("Tag--videoName", videoName)
            Log.e("Tag--thumbnailPath", thumbnailPath)
            if (thumbnailUri != null) {
                val videoData = VideoData(videoName, Uri.parse(absolutePathOfVideo), thumbnailUri)
                videoList.add(videoData)
            }
        }

        cursor?.close()
        isLoading(false)
        videoList
    }
}




