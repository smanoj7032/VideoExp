package com.example.videoexp

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.example.videoexp.model.VideoData
import com.example.videoexp.player.Player
import com.example.videoexp.utils.getVideoThumbnail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PlayerImpl @Inject constructor() : Player {
    override suspend fun getAllVideo(
        context: Context
    ): ArrayList<VideoData> {
        val videoList = ArrayList<VideoData>()
        val uri: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Thumbnails.DATA
        )
        val orderBy = MediaStore.Video.Media.DATE_TAKEN

        withContext(Dispatchers.IO) {
            val cursor = context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                "$orderBy DESC"
            )

            cursor?.use { cursor1 ->
                val columnIndexDisplayName =
                    cursor1.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val columnIndexData = cursor1.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                val columnIndexThumbnail =
                    cursor1.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.DATA)

                while (cursor1.moveToNext()) {
                    val videoName = cursor1.getString(columnIndexDisplayName)
                    val absolutePathOfVideo = cursor1.getString(columnIndexData)
                    val thumbnailPath = cursor1.getString(columnIndexThumbnail)
                    val thumbnailUri = getVideoThumbnail(Uri.parse(thumbnailPath))

                    Log.e("Tag--VideoUri", absolutePathOfVideo)
                    Log.e("Tag--videoName", videoName)
                    Log.e("Tag--thumbnailPath", thumbnailPath)
                    if (thumbnailUri != null) {
                        val videoData =
                            VideoData(videoName, Uri.parse(absolutePathOfVideo), thumbnailUri)
                        videoList.add(videoData)
                    }
                }
            }
        }

        return videoList
    }


}