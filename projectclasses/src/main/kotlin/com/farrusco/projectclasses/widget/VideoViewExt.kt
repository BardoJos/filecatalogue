/*
 * Copyright (c) 2022. farrusco (jos dot farrusco at gmail dot com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.farrusco.projectclasses.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.MediaPlayer.OnBufferingUpdateListener
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnPreparedListener
import android.media.MediaPlayer.OnVideoSizeChangedListener
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.Surface
import android.view.SurfaceHolder
import android.view.TextureView
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.MediaController
import android.widget.MediaController.MediaPlayerControl
import android.widget.Toast
import com.farrusco.projectclasses.utils.Logging
import com.farrusco.projectclasses.messages.ToastExt
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import kotlin.math.roundToInt

/**
 *
 * Displays a video file.
 *
 */
class VideoViewExt : TextureView, MediaPlayerControl   {
    private var mUri: Uri? = null
    private var mHeaders: Map<String, String>? = null

    private val mKeepScreenOnMsg = 1
    private val mStateERROR = -1
    private val mStateIDLE = 0
    private val mStatePREPARING = 1
    private val mStatePREPARED = 2
    private val mStatePLAYING = 3
    private val mStatePAUSED = 4
    private val mStatePlaybackCompleted = 5

    private val mMetadataAll = false
    private val mBypassMetadataFilter = false
    // Playback capabilities.
    private val mPauseAvailable = 1 // Boolean
    private val mSeekBackwardAvailable = 2 // Boolean
    private val mSeekForwardAvailable = 3 // Boolean
    //const val mSeekAvailable = 4 // Boolean
    private var mediaData: Any? = null

    private var mCurrentState = mStateIDLE
    private var mTargetState = mStateIDLE

    private var mSurface: Surface? = null
    private var mMediaPlayer: MediaPlayer? = null
    private var mAudioSession = 0
    private var mVideoWidth = 0
    private var mVideoHeight = 0
    private var mSurfaceWidth = 0
    private var mSurfaceHeight = 0
    private var mMediaController: MediaController? = null
    private var mOnCompletionListener: OnCompletionListener? = null
    private var mOnPreparedListener: OnPreparedListener? = null
    private var mOnErrorListener: MediaPlayer.OnErrorListener? = null
    //private var mOnInfoListener: MediaPlayer.OnInfoListener? = null
    private var mOnSurfaceTextureListener: SurfaceTextureListener? = null
    private var mCurrentBufferPercentage = 0
    private var mSeekWhenPrepared = 0
    private var mCanPause = false
    private var mCanSeekBack = false
    private var mCanSeekForward = false
    private val mSurfaceFrame = Rect()
    private val mOnBufferingUpdateListener: OnBufferingUpdateListener? = null

    constructor(context: Context?) : super(context!!) {
        initVideoView()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
        initVideoView()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!, attrs, defStyle
    ) {
        initVideoView()
    }
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = getDefaultSize(mVideoWidth, widthMeasureSpec)
        var height = getDefaultSize(mVideoHeight, heightMeasureSpec)
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
            val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
            val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
            val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
            if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY) {
                // the size is fixed
                width = widthSpecSize
                height = heightSpecSize

                // for compatibility, we adjust size based on aspect ratio
                if (mVideoWidth * height < width * mVideoHeight) {
                    //Log.i("@@@", "image too wide, correcting");
                    width = height * mVideoWidth / mVideoHeight
                } else if (mVideoWidth * height > width * mVideoHeight) {
                    //Log.i("@@@", "image too tall, correcting");
                    height = width * mVideoHeight / mVideoWidth
                }
            } else if (widthSpecMode == MeasureSpec.EXACTLY) {
                // only the width is fixed, adjust the height to match aspect ratio if possible
                width = widthSpecSize
                height = width * mVideoHeight / mVideoWidth
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    height = heightSpecSize
                }
            } else if (heightSpecMode == MeasureSpec.EXACTLY) {
                // only the height is fixed, adjust the width to match aspect ratio if possible
                height = heightSpecSize
                width = height * mVideoWidth / mVideoHeight
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    width = widthSpecSize
                }
            } else {
                // neither the width nor the height are fixed, try to use actual video size
                width = mVideoWidth
                height = mVideoHeight
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // too tall, decrease both width and height
                    height = heightSpecSize
                    width = height * mVideoWidth / mVideoHeight
                }
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // too wide, decrease both width and height
                    width = widthSpecSize
                    height = width * mVideoHeight / mVideoWidth
                }
            }
        } else {
            // no size yet, just adopt the given spec sizes
        }
        setMeasuredDimension(width, height)
    }

    override fun onInitializeAccessibilityEvent(event: AccessibilityEvent) {
        super.onInitializeAccessibilityEvent(event)
        event.className = VideoViewExt::class.java.name
    }

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        info.className = VideoViewExt::class.java.name
    }

    override fun setSurfaceTextureListener(listener: SurfaceTextureListener?) {
        mOnSurfaceTextureListener = listener
    }

/*    fun resolveAdjustedSize(desiredSize: Int, measureSpec: Int): Int {
        return getDefaultSize(desiredSize, measureSpec)
    }*/

    private fun initVideoView() {
        mVideoWidth = 0
        mVideoHeight = 0
        // setSurfaceTextureListener has been overwritten, and it is not call the super.
        super.setSurfaceTextureListener(mSurfaceTextureListener)
        isFocusable = true
        isFocusableInTouchMode = true
        requestFocus()
        mCurrentState = mStateIDLE
        mTargetState = mStateIDLE

    }

/*    fun setVideoPath(path: String?) {
        setVideoURI(Uri.parse(path))
    }*/

    fun setVideoURI(uri: Uri?) {
        setVideoURI(uri, null)
    }

    private fun setVideoURI(uri: Uri?, headers: Map<String, String>?) {
        mUri = uri
        mHeaders = headers
        mSeekWhenPrepared = 0
        openVideo()
        requestLayout()
        invalidate()
    }

    fun stopPlayback() {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.stop()
            mMediaPlayer!!.release()
            mMediaPlayer = null
            mCurrentState = mStateIDLE
            mTargetState = mStateIDLE
        }
    }

    private fun openVideo() {
        if (mUri == null || mSurface == null) {
            // not ready for playback just yet, will try again later
            return
        }
        val context = context
        // Tell the music playback service to pause
        val i = Intent("com.android.music.musicservicecommand")
        i.putExtra("command", "pause")
        context.sendBroadcast(i)

        // we shouldn't clear the target state, because somebody might have
        // called start() previously
        release(false)
        try {
            mMediaPlayer = MediaPlayer()
            if (mAudioSession != 0) {
                mMediaPlayer!!.audioSessionId = mAudioSession
            } else {
                mAudioSession = mMediaPlayer!!.audioSessionId
            }
            mMediaPlayer!!.setOnPreparedListener(mPreparedListener)
            mMediaPlayer!!.setOnVideoSizeChangedListener(mSizeChangedListener)
            mMediaPlayer!!.setOnCompletionListener(mCompletionListener)
            mMediaPlayer!!.setOnErrorListener(mErrorListener)
            mMediaPlayer!!.setOnBufferingUpdateListener(mBufferingUpdateListener)
            mCurrentBufferPercentage = 0
            //mMediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
            mMediaPlayer!!.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            mMediaPlayer!!.setDataSource(context, mUri!!, mHeaders)
            mMediaPlayer!!.setDisplay(mSurfaceHolder)
            mMediaPlayer!!.setScreenOnWhilePlaying(true)
            mMediaPlayer!!.prepareAsync()

            // we don't set the target state here either, but preserve the
            // target state that was there before.
            mCurrentState = mStatePREPARING
            attachMediaController()
        } catch (ex: IOException) {
            Logging.w(String.format("Unable to open content: %s", mUri), ex)
            mCurrentState = mStateERROR
            mTargetState = mStateERROR
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0)
            return
        } catch (ex: IllegalArgumentException) {
            Logging.w(String.format("Unable to open content: %s", mUri), ex)
            mCurrentState = mStateERROR
            mTargetState = mStateERROR
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0)
            return
        }
    }

    fun setMediaController(controller: MediaController?) {
        if (mMediaController != null) {
            mMediaController!!.hide()
        }
        mMediaController = controller
        attachMediaController()
    }

    private fun attachMediaController() {
        if (mMediaPlayer != null && mMediaController != null) {
            mMediaController!!.setMediaPlayer(this)
            val anchorView = if (this.parent is View) this.parent as View else this
            mMediaController!!.setAnchorView(anchorView)
            mMediaController!!.isEnabled = isInPlaybackState
        }
    }

    private fun setFixedSize(width: Int, height: Int) {
        mSurfaceHolder.setFixedSize(width, height)
    }

    private fun hasValidSize(): Boolean {
        val surfaceRatio = (mSurfaceWidth / mSurfaceHeight.toFloat() * 10.0f).roundToInt() / 10.0f
        val videoRatio = (mVideoWidth / mVideoHeight.toFloat() * 10.0f).roundToInt() / 10.0f
        return surfaceRatio == videoRatio
    }

    private var mSizeChangedListener = OnVideoSizeChangedListener { mp, _, _ ->
        mVideoWidth = mp.videoWidth
        mVideoHeight = mp.videoHeight
        if (mVideoWidth != 0 && mVideoHeight != 0) {
            setFixedSize(mVideoWidth, mVideoHeight)
            requestLayout()
        }
    }
    private var mPreparedListener = OnPreparedListener { mp ->
        // briefly show the media controller
        mCurrentState = mStatePREPARED

        // Get the capabilities of the player for this stream
        init(mp)
        if (isInitialized) {
            mCanPause = (!has(mPauseAvailable)
                    || getBoolean(mPauseAvailable))
            mCanSeekBack = (!has(mSeekBackwardAvailable)
                    || getBoolean(mSeekBackwardAvailable))
            mCanSeekForward = (!has(mSeekForwardAvailable)
                    || getBoolean(mSeekForwardAvailable))
        } else {
            mCanSeekForward = true
            mCanSeekBack = true
            mCanPause = true
        }
        if (mOnPreparedListener != null) {
            mOnPreparedListener!!.onPrepared(mMediaPlayer)
        }
        if (mMediaController != null) {
            mMediaController!!.isEnabled = true
        }
        val seekToPosition =
            mSeekWhenPrepared // mSeekWhenPrepared may be changed after seekTo() call
        if (seekToPosition != 0) {
            seekTo(seekToPosition)
        }
        if (mVideoWidth != 0 && mVideoHeight != 0) {
            //Logging.i("@@@@", "video size: " + mVideoWidth +"/"+ mVideoHeight);
            setFixedSize(mVideoWidth, mVideoHeight)
            if (hasValidSize()) {
                // We didn't actually change the size (it was already at the size
                // we need), so we won't get a "surface changed" callback, so
                // start the video here instead of in the callback.
                if (mTargetState == mStatePLAYING) {
                    start()
                    if (mMediaController != null) {
                        mMediaController!!.show()
                    }
                } else if (!isPlaying &&
                    (seekToPosition != 0 || currentPosition > 0)
                ) {
                    if (mMediaController != null) {
                        // Show the media controls when we're paused into a video and make 'em stick.
                        mMediaController!!.show(0)
                    }
                }
            }
        } else {
            // We don't know the video size yet, but should start anyway.
            // The video size might be reported to us later.
            if (mTargetState == mStatePLAYING) {
                start()
            }
        }
    }

    private val mCompletionListener = OnCompletionListener {
        mCurrentState = mStatePlaybackCompleted
        mTargetState = mStatePlaybackCompleted
        if (mMediaController != null) {
            mMediaController!!.hide()
        }
        if (mOnCompletionListener != null) {
            mOnCompletionListener!!.onCompletion(mMediaPlayer)
        }
    }

/*    private val mInfoListener = MediaPlayer.OnInfoListener { mp, arg1, arg2 ->
        if (mOnInfoListener != null) {
            mOnInfoListener!!.onInfo(mp, arg1, arg2)
        }
        true
    }*/

    private val mErrorListener = MediaPlayer.OnErrorListener { mp, frameworkErr, implErr ->
        mCurrentState = mStateERROR
        mTargetState = mStateERROR
        if (mMediaController != null) {
            mMediaController!!.hide()
        }
        if (mOnErrorListener != null) {
            if (mOnErrorListener!!.onError(mp, frameworkErr, implErr)) {
                return@OnErrorListener true
            }
        }
        /* Otherwise, pop up an error dialog so the user knows that
             * something bad has happened. Only try and pop up the dialog
             * if we're attached to a window. When we're going away and no
             * longer have a window, don't bother showing the user an error.
             */
        if (windowToken != null) {
        //val r = context.resources
        val messageId = if (frameworkErr == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
                android.R.string.VideoView_error_text_invalid_progressive_playback
            } else {
            android.R.string.VideoView_error_text_unknown
            }

        ToastExt().makeText( context, messageId, Toast.LENGTH_LONG ).show()

        if (mOnCompletionListener != null) {
            mOnCompletionListener!!.onCompletion(mMediaPlayer)
        }
    }
        true
    }
    private val mBufferingUpdateListener = OnBufferingUpdateListener { mp, percent ->
        mCurrentBufferPercentage = percent
        mOnBufferingUpdateListener?.onBufferingUpdate(mp, percent)
    }

    /**
     * Register a callback to be invoked when the media file
     * is loaded and ready to go.
     *
    fun setOnPreparedListener(l: OnPreparedListener?) {
    mOnPreparedListener = l
    }
     */


/**
 * Register a callback to be invoked when the end of a media file
 * has been reached during playback.
 *
 */
    fun setOnCompletionListener(l: OnCompletionListener?) {
        mOnCompletionListener = l
    }
    /**
     * Register a callback to be invoked when an error occurs
     * during playback or setup.  If no listener is specified,
     * or if the listener returned false, VideoView will inform
     * the user of any errors.
     *
    fun setOnErrorListener(l: MediaPlayer.OnErrorListener?) {
    mOnErrorListener = l
    }
     */

    /**
     * Register a callback to be invoked when an informational event
     * occurs during playback or setup.
     *
    fun setOnInfoListener(l: MediaPlayer.OnInfoListener?) {
    mOnInfoListener = l
    }
     */

    private val mSurfaceTextureListener: SurfaceTextureListener = object : SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            if (mOnSurfaceTextureListener != null) {
                mOnSurfaceTextureListener!!.onSurfaceTextureAvailable(surface, width, height)
            }
            mSurfaceWidth = width
            mSurfaceHeight = height
            mSurface = Surface(surface)
            openVideo()
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
            if (mOnSurfaceTextureListener != null) {
                mOnSurfaceTextureListener!!.onSurfaceTextureSizeChanged(surface, width, height)
            }
            mSurfaceWidth = width
            mSurfaceHeight = height
            val isValidState = mTargetState == mStatePLAYING
            if (mMediaPlayer != null && isValidState && hasValidSize()) {
                if (mSeekWhenPrepared != 0) {
                    seekTo(mSeekWhenPrepared)
                }
                start()
            }
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            if (mMediaController != null) {
                mMediaController!!.hide()
            }
            release(true)
            if (mOnSurfaceTextureListener != null) {
                if (mOnSurfaceTextureListener!!.onSurfaceTextureDestroyed(surface)) return true
            }
            if (mSurface != null) {
                mSurface!!.release()
                mSurface = null
            }
            return true
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
            if (mOnSurfaceTextureListener != null) {
                mOnSurfaceTextureListener!!.onSurfaceTextureUpdated(surface)
            }
        }
    }

    /*
    * release the media player in any state
    */
    private fun release(clearTargetState: Boolean) {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.reset()
            mMediaPlayer!!.release()
            mMediaPlayer = null
            mCurrentState = mStateIDLE
            if (clearTargetState) {
                mTargetState = mStateIDLE
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isInPlaybackState && mMediaController != null) {
            toggleMediaControlsVisibility()
        }
        return false
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        val isKeyCodeSupported =
            keyCode != KeyEvent.KEYCODE_BACK && keyCode != KeyEvent.KEYCODE_VOLUME_UP && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN && keyCode != KeyEvent.KEYCODE_VOLUME_MUTE && keyCode != KeyEvent.KEYCODE_MENU && keyCode != KeyEvent.KEYCODE_CALL && keyCode != KeyEvent.KEYCODE_ENDCALL
        if (isInPlaybackState && isKeyCodeSupported && mMediaController != null) {
            when (keyCode){
                KeyEvent.KEYCODE_HEADSETHOOK, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                        if (mMediaPlayer!!.isPlaying) {
                            pause()
                            mMediaController!!.show()
                        } else {
                            start()
                            mMediaController!!.hide()
                        }
                        return true
                    }
                KeyEvent.KEYCODE_MEDIA_PLAY -> {
                    if (!mMediaPlayer!!.isPlaying) {
                        start()
                        mMediaController!!.hide()
                    }
                    return true
                }
                KeyEvent.KEYCODE_MEDIA_STOP, KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                    if (mMediaPlayer!!.isPlaying) {
                        pause()
                        mMediaController!!.show()
                    }
                    return true
                }
                else ->{
                    toggleMediaControlsVisibility()
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun toggleMediaControlsVisibility() {
        if (mMediaController!!.isShowing) {
            mMediaController!!.hide()
        } else {
            mMediaController!!.show()
        }
    }

    override fun start() {
        if (isInPlaybackState) {
            mMediaPlayer!!.start()
            mCurrentState = mStatePLAYING
        }
        mTargetState = mStatePLAYING
    }

    override fun pause() {
        if (isInPlaybackState) {
            if (mMediaPlayer!!.isPlaying) {
                mMediaPlayer!!.pause()
                mCurrentState = mStatePAUSED
            }
        }
        mTargetState = mStatePAUSED
    }
/*
    fun suspend() {
        release(false)
    }

    fun resume() {
        openVideo()
    }*/

    override fun getDuration(): Int {
        return if (isInPlaybackState) {
            mMediaPlayer!!.duration
        } else -1
    }

    override fun getCurrentPosition(): Int {
        return if (isInPlaybackState) {
            mMediaPlayer!!.currentPosition
        } else 0
    }

    override fun seekTo(msec: Int) {
        mSeekWhenPrepared = if (isInPlaybackState) {
            mMediaPlayer!!.seekTo(msec)
            0
        } else {
            msec
        }
    }

    override fun isPlaying(): Boolean {
        return isInPlaybackState && mMediaPlayer!!.isPlaying
    }

    override fun getBufferPercentage(): Int {
        return if (mMediaPlayer != null) {
            mCurrentBufferPercentage
        } else 0
    }

    private val isInPlaybackState: Boolean
        get() = mMediaPlayer != null && mCurrentState != mStateERROR && mCurrentState != mStateIDLE && mCurrentState != mStatePREPARING

    override fun canPause(): Boolean {
        return mCanPause
    }

    override fun canSeekBackward(): Boolean {
        return mCanSeekBack
    }

    override fun canSeekForward(): Boolean {
        return mCanSeekForward
    }

    override fun getAudioSessionId(): Int {
        if (mAudioSession == 0) {
            val foo = MediaPlayer()
            mAudioSession = foo.audioSessionId
            foo.release()
        }
        return mAudioSession
    }

    val mHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                mKeepScreenOnMsg -> {
                    keepScreenOn = msg.arg1 != 0
                }
            }
        }
    }

    private val mSurfaceHolder: SurfaceHolder = object : SurfaceHolder {
        override fun addCallback(callback: SurfaceHolder.Callback) {}
        override fun removeCallback(callback: SurfaceHolder.Callback) {}
        override fun isCreating(): Boolean {
            return mSurface != null
        }

        @Deprecated("Deprecated in Java")
        override fun setType(type: Int) {}

        override fun setFixedSize(width: Int, height: Int) {
            if (getWidth() != width || getHeight() != height) {
                requestLayout()
            }
        }

        override fun setSizeFromLayout() {}
        override fun setFormat(format: Int) {}
        override fun setKeepScreenOn(screenOn: Boolean) {
            val msg = mHandler.obtainMessage(mKeepScreenOnMsg)
            msg.arg1 = if (screenOn) 1 else 0
            mHandler.sendMessage(msg)
        }

        override fun lockCanvas(): Canvas? {
            return null
        }

        override fun lockCanvas(dirty: Rect): Canvas? {
            return null
        }

        override fun unlockCanvasAndPost(canvas: Canvas) {}
        override fun getSurfaceFrame(): Rect {
            mSurfaceFrame[0, 0, mVideoWidth] = mVideoHeight
            return mSurfaceFrame
        }

        override fun getSurface(): Surface {
            return mSurface!!
        }
    }

    fun init(mp: MediaPlayer?) {
        val method = mediadataMethod
        method!!.isAccessible = true
        try {
            mediaData = method.invoke(mp, mMetadataAll, mBypassMetadataFilter)
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
            mediaData = null
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
            mediaData = null
        }
    }

    val isInitialized: Boolean
        get() = mediaData != null

    private fun has(key: Int): Boolean {
        val method = getHasMethod(
            mediaData!!.javaClass
        )
        try {
            return method!!.invoke(mediaData, key) as Boolean
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
        return false
    }

    private fun getBoolean(key: Int): Boolean {
        val method = getBooleanMethod(
            mediaData!!.javaClass
        )
        try {
            return method!!.invoke(mediaData, key) as Boolean
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
        return false
    }

    private fun getBooleanMethod(clazz: Class<*>): Method? {
        val methods = clazz.declaredMethods
        for (method in methods) {
            if (TextUtils.equals(method.name, "getBoolean")) {
                return method
            }
        }
        return null
    }

    private fun getHasMethod(clazz: Class<*>): Method? {
        val methods = clazz.declaredMethods
        for (method in methods) {
            if (TextUtils.equals(method.name, "has")) {
                return method
            }
        }
        return null
    }

    private val mediadataMethod: Method?
        get() {
            val methods = MediaPlayer::class.java.declaredMethods
            for (method in methods) {
                if (TextUtils.equals(method.name, "getMetadata")) {
                    return method
                }
            }
            return null
        }
}