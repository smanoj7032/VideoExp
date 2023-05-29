package com.example.videoexp

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.videoexp.model.VideoData
import com.example.videoexp.player.Player
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewModel @Inject constructor(private val player: Player) : ViewModel() {
    val videoData = MutableLiveData<ArrayList<VideoData>>()

    fun getAllVideo(context: Context) {
        viewModelScope.launch {
            videoData.value = player.getAllVideo(context)
            Log.e("TAG-->V", "getAllVideo: ${videoData.value}")
        }
    }
}