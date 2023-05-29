package com.example.videoexp.player

import android.content.Context
import com.example.videoexp.model.VideoData

interface Player {
   suspend fun getAllVideo(context: Context): ArrayList<VideoData>
}