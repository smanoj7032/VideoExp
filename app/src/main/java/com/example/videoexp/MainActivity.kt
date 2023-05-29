package com.example.videoexp

import android.Manifest
import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.view.View
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.videoexp.databinding.ActivityMainBinding
import com.example.videoexp.databinding.ItemVideoListBinding
import com.example.videoexp.model.VideoData
import com.example.videoexp.utils.PermissionUtils
import com.example.videoexp.utils.RVAdapter
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val permission = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
    )
    private val viewModel: ViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private lateinit var videoAdapter: RVAdapter<VideoData, ItemVideoListBinding>
    private var exoPlayer: ExoPlayer? = null
    private var playbackPosition = 0L
    private var index = 0
    private var playWhenReady = true
    private var isPipMode: Boolean = false
    private val onBackPressedCallback = this.onBackPressedDispatcher.addCallback(this) {
        if (!isPipMode) {
            startPIPMode()
            isPipMode = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setObserver()
        setAdapter()
    }

    override fun onStart() {
        super.onStart()
        setObserver()
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onStop() {
        if (isPipMode) {
            playWhenReady = exoPlayer?.playWhenReady ?: true
            playbackPosition = exoPlayer?.currentPosition ?: 0
        }
        releasePlayer()
        super.onStop()
    }

    override fun onPause() {
        releasePlayer()
        super.onPause()
    }

    private fun releasePlayer() {
        exoPlayer?.let { player ->
            playbackPosition = player.currentPosition
            playWhenReady = player.playWhenReady
            player.release()
            exoPlayer = null
        }
    }

    fun initializeExoPlayer(uri: Uri) {
        releasePlayer()
        exoPlayer = ExoPlayer.Builder(applicationContext).build()
        binding.playerView.player = exoPlayer
        binding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
        val mediaItem: MediaItem = MediaItem.fromUri(uri)
        exoPlayer!!.setMediaItem(mediaItem)
        exoPlayer!!.seekTo(playbackPosition)
        exoPlayer!!.playWhenReady = playWhenReady
        exoPlayer!!.prepare()

        exoPlayer!!.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_BUFFERING) {
                    binding.pbProgress.visibility = View.VISIBLE
                } else if (state == Player.STATE_READY) {
                    binding.pbProgress.visibility = View.INVISIBLE
                }
                if (state == Player.STATE_ENDED)
                    index = +1
            }
        })
    }


    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean, newConfig: Configuration
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            isPipMode != isInPictureInPictureMode
            super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        }
    }

    override fun onUserLeaveHint() {
        startPIPMode()
        super.onUserLeaveHint()
    }


    private fun startPIPMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val rect = Rect()
            binding.playerView.getGlobalVisibleRect(rect)
            val aspectRatio = Rational(
                rect.width(), rect.height()
            ) // Use the width and height of the playerView's visible area
            val pipBuilder = PictureInPictureParams.Builder()
            pipBuilder.setAspectRatio(aspectRatio)
            enterPictureInPictureMode(pipBuilder.build())
        }
    }


    private fun setAdapter() {
        videoAdapter =
            object : RVAdapter<VideoData, ItemVideoListBinding>(R.layout.item_video_list, 1) {
                override fun onBind(binding: ItemVideoListBinding, bean: VideoData, position: Int) {
                    super.onBind(binding, bean, position)
                    binding.videoName.text = bean.name
                    binding.ivThumbnail.setImageBitmap(bean.thumbNail)
                    binding.clLay.setOnClickListener {
                        initializeExoPlayer(bean.videoUri)
                        index = position
                        Log.e("Pos--", "onBind: $position")
                    }
                }
            }
        binding.rvVideo.layoutManager = GridLayoutManager(this, 4)
        binding.rvVideo.adapter = videoAdapter
        viewModel.videoData.observe(this) { list ->
            videoAdapter.list = list
            Log.e("TAG-->", "setObserver: $list")
            binding.pbVideo.visibility = View.GONE
            initializeExoPlayer(list[index].videoUri)

        }
    }

    private fun setObserver() {
        if (PermissionUtils.isPermissionGranted(this, permission)) {
            lifecycleScope.launch {
                viewModel.getAllVideo(this@MainActivity)
            }
        } else PermissionUtils.requestPermissions(1, this, permission)

    }
}
