package com.wmi.mg_zoom_controller

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.view.KeyEvent
import android.view.View
import android.view.WindowInsets
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.SnackbarLayout
import us.zoom.sdk.ChatMessageDeleteType
import us.zoom.sdk.InMeetingChatMessage
import us.zoom.sdk.InMeetingShareController.InMeetingShareListener
import us.zoom.sdk.MeetingParameter
import us.zoom.sdk.MeetingServiceListener
import us.zoom.sdk.MeetingStatus
import us.zoom.sdk.MobileRTCRenderInfo
import us.zoom.sdk.MobileRTCVideoUnitAspectMode
import us.zoom.sdk.MobileRTCVideoUnitRenderInfo
import us.zoom.sdk.MobileRTCVideoView
import us.zoom.sdk.ShareSettingType
import us.zoom.sdk.SharingStatus
import us.zoom.sdk.ZoomSDK


class ZoomActivity : AppCompatActivity() {
    private val zoomSDK = ZoomSDK.getInstance()
    private lateinit var chatCacheManager: ZoomChatCacheManager

    // RTCs
    private lateinit var zoomPiPModeVideoRtc: MobileRTCVideoView
    private lateinit var zoomPiPModeShareRtc: MobileRTCVideoView
    private lateinit var zoomFullScreenModeVideoRtc: MobileRTCVideoView
    private lateinit var zoomFullScreenModeShareRtc: MobileRTCVideoView
    private lateinit var myVideoRtc: MobileRTCVideoView

    private lateinit var zoomFullScreenMode: ConstraintLayout
    private lateinit var zoomPiPModeLayout: RelativeLayout
    private lateinit var participantsGridContainer: LinearLayout
    private lateinit var myVideoContainer: RelativeLayout

    // Meeting controls
    private lateinit var btnToggleMic: ImageView
    private lateinit var btnToggleVideo: ImageView
    private lateinit var btnToggleShare: ImageView
    private lateinit var btnChats: ImageView

    // Listeners
    private val inMeetingListener = InMeetingListener()
    private val shareListener = ShareScreenListener()
    private val meetingListener = ActivityMeetingListener()

    private var speakerId: Long? = null

    private lateinit var resultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zoom)

        chatCacheManager = ZoomChatCacheManager(this)

        val btnSwitchCamera = findViewById<ImageView>(R.id.btnSwitchUserCamera)
        val btnMinimize = findViewById<ImageView>(R.id.btnMinimize)
        val btnLeave = findViewById<ImageView>(R.id.btnLeave)

        // RTCs assignment
        zoomPiPModeVideoRtc = findViewById(R.id.zoomPiPModeVideoRtc)
        zoomPiPModeShareRtc = findViewById(R.id.zoomPiPModeShareRtc)
        zoomFullScreenModeVideoRtc = findViewById(R.id.zoomFullScreenModeVideoRtc)
        zoomFullScreenModeShareRtc = findViewById(R.id.zoomFullScreenModeShareRtc)
        myVideoRtc = findViewById(R.id.myVideoRtc)

        zoomFullScreenMode = findViewById(R.id.zoomMeetingFullScreenMode)
        zoomPiPModeLayout = findViewById(R.id.zoomPiPModeLayout)
        myVideoContainer = findViewById(R.id.myVideoContainer)
        participantsGridContainer = findViewById(R.id.participantsGridContainer)
        btnToggleMic = findViewById(R.id.btnToggleMic)
        btnToggleVideo = findViewById(R.id.btnToggleVideo)
        btnToggleShare = findViewById(R.id.btnToggleShare)
        btnChats = findViewById(R.id.btnChats)

        myVideoRtc.videoViewManager.addPreviewVideoUnit(
            MobileRTCVideoUnitRenderInfo(
                0,
                0,
                100,
                100
            ).apply {
                corner_radius = 20
                aspect_mode = MobileRTCVideoUnitAspectMode.VIDEO_ASPECT_PAN_AND_SCAN
            })

        if (zoomSDK.inMeetingService.inMeetingVideoController.isMyVideoMuted) {
            myVideoContainer.visibility = View.GONE
        }

        btnChats.setOnClickListener {
            val intent = Intent(this, ChatsActivity::class.java)

            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            startActivity(intent)
        }

        btnMinimize.setOnClickListener {
            enterPiPMode()
        }

        btnLeave.setOnClickListener {
            zoomSDK.meetingService.leaveCurrentMeeting(true)
        }

        btnSwitchCamera.setOnClickListener {
            myVideoRtc.videoViewManager.removeAllVideoUnits()
            zoomSDK.inMeetingService.inMeetingVideoController.switchToNextCamera()
            myVideoRtc.videoViewManager.addPreviewVideoUnit(
                MobileRTCVideoUnitRenderInfo(
                    0,
                    0,
                    100,
                    100
                ).apply {
                    corner_radius = 20
                    aspect_mode = MobileRTCVideoUnitAspectMode.VIDEO_ASPECT_PAN_AND_SCAN
                })
        }

        with(zoomSDK) {
            inMeetingService.addListener(inMeetingListener)
            inMeetingService.inMeetingShareController.addListener(shareListener)
            meetingService.addListener(meetingListener)

            with(inMeetingService.inMeetingAudioController) {
                if (!isAudioConnected) {
                    connectAudioWithVoIP()
                }
            }
        }

        updateWindowMode()
        updateMeetingControlsStatus()
        initiateSpeakerVideo()
        initiateCameraPreview()
        updateZoomGrid()
        updateMainStageView()

        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data

                    with(zoomSDK.inMeetingService.inMeetingShareController) {
                        startShareScreenSession(data)
                        startShareScreenContent()
                    }
                }
            }
    }

    private fun updateWindowMode() {
        if (isInPictureInPictureMode) {
            zoomPiPModeLayout.visibility = View.VISIBLE
            zoomFullScreenMode.visibility = View.GONE
        } else {
            zoomPiPModeLayout.visibility = View.GONE
            zoomFullScreenMode.visibility = View.VISIBLE

            updateZoomGrid()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            updateWindowMode()
        }
    }

    private fun getMeetingParticipants(): List<Long> {
        return zoomSDK.inMeetingService.inMeetingUserList.filter { it != zoomSDK.inMeetingService.myUserID }
    }

    private fun enterPiPMode() {
        val params = PictureInPictureParams.Builder().setAspectRatio(Rational(9, 16)).build()

        enterPictureInPictureMode(params)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK) {
            enterPiPMode()

            true
        } else super.onKeyDown(keyCode, event)
    }

    // Grid of 3 participants
    fun updateZoomGrid(participants: List<Long>? = null) {
        val shareController = zoomSDK.inMeetingService.inMeetingShareController
        val isSharingScreen =
            shareController.isOtherSharing || shareController.isSharingScreen || shareController.isSharingOut
        var updatedParticipants = participants ?: getMeetingParticipants()

        if (updatedParticipants.size > 3) {
            updatedParticipants = updatedParticipants.subList(0, 3)
        }

        participantsGridContainer.removeAllViews()

        if (updatedParticipants.size < 2) {
            participantsGridContainer.visibility = View.GONE

            if (updatedParticipants.isNotEmpty()) updateSpeakerVideo(updatedParticipants[0])
            else updateSpeakerVideo(zoomSDK.inMeetingService.myUserID)

            if (!isSharingScreen) return
        }

        participantsGridContainer.visibility = View.VISIBLE

        for (participant in updatedParticipants) {
            if (participant == speakerId && !isSharingScreen) continue

            val rtcContainer = layoutInflater.inflate(
                R.layout.participant_zoom_tile, participantsGridContainer, false
            )
            val rtc = rtcContainer.findViewById<MobileRTCVideoView>(R.id.zoomTileRtc)
            val userNameView = rtcContainer.findViewById<TextView>(R.id.zoomTileUserName)

            userNameView.text = zoomSDK.inMeetingService.getUserInfoById(participant).userName

            rtc.videoViewManager.addAttendeeVideoUnit(participant,
                MobileRTCVideoUnitRenderInfo(0, 0, 100, 100).apply { corner_radius = 20 })

            participantsGridContainer.addView(rtcContainer)
        }
    }

    // TODO Develop feature that allows user to disable/enable camera before joining the meeting
    private fun initiateCameraPreview() {
    }

    private fun updateMeetingControlsStatus() {
        with(ZoomSDK.getInstance().inMeetingService.inMeetingAudioController) {
            btnToggleMic.setImageResource(if (isMyAudioMuted) R.drawable.zm_btn_unmute_audio_normal else R.drawable.zm_btn_mute_audio_normal)
            btnToggleMic.setOnClickListener {
                if (isMyAudioMuted) {
                    muteMyAudio(false)
                    btnToggleMic.setImageResource(R.drawable.zm_btn_mute_audio_normal)
                } else {
                    muteMyAudio(true)
                    btnToggleMic.setImageResource(R.drawable.zm_btn_unmute_audio_normal)
                }
            }
        }

        with(ZoomSDK.getInstance().inMeetingService.inMeetingVideoController) {
            btnToggleVideo.setImageResource(if (isMyVideoMuted) R.drawable.zm_btn_unmute_video_normal else R.drawable.zm_btn_mute_video_normal)
            btnToggleVideo.setOnClickListener {
                if (isMyVideoMuted) {
                    muteMyVideo(false)

                    myVideoContainer.visibility = View.VISIBLE

                    btnToggleVideo.setImageResource(R.drawable.zm_btn_mute_video_normal)
                } else {
                    muteMyVideo(true)

                    myVideoContainer.visibility = View.GONE

                    btnToggleVideo.setImageResource(R.drawable.zm_btn_unmute_video_normal)
                }
            }
        }

        with(ZoomSDK.getInstance().inMeetingService.inMeetingShareController) {
            btnToggleShare.setImageResource(if (isSharingScreen) R.drawable.zm_meeting_stop_share_normal else R.drawable.zm_share_btn_normal)

            if (isShareLocked) {
                btnToggleShare.alpha = .5F
                btnToggleShare.setOnClickListener {
                    Toast.makeText(
                        this@ZoomActivity, "Host disabled the share screen", Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                btnToggleShare.alpha = 1F

                btnToggleShare.setOnClickListener {
                    if (!isSharingScreen) {
                        if (isOtherSharing) {
                            return@setOnClickListener Toast.makeText(
                                this@ZoomActivity,
                                "Cannot share while the other is sharing their screen",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        val manager =
                            getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                        val intent = manager.createScreenCaptureIntent()

                        resultLauncher.launch(intent)
                    } else {
                        stopShareScreen()
                    }
                }
            }

        }
    }

    private fun initiateSpeakerVideo() {
        val participants = getMeetingParticipants()

        speakerId = if (participants.isNotEmpty()) {
            participants[0]
        } else {
            zoomSDK.inMeetingService.myUserID
        }

        updateSpeakerVideo(speakerId!!)
    }

    fun updateSpeakerVideo(userId: Long) {
        speakerId = userId

        val videoUnit = MobileRTCVideoUnitRenderInfo(0, 0, 100, 100).apply {
            corner_radius = 20
        }

        zoomFullScreenModeVideoRtc.videoViewManager.addAttendeeVideoUnit(
            userId, videoUnit
        )
        zoomPiPModeVideoRtc.videoViewManager.addAttendeeVideoUnit(
            userId, videoUnit
        )
    }

    fun updateMainStageView() {
        zoomFullScreenModeShareRtc.videoViewManager.removeAllVideoUnits()
        zoomPiPModeShareRtc.videoViewManager.removeAllVideoUnits()

        with(zoomSDK.inMeetingService.inMeetingShareController) {

            if (isOtherSharing || isSharingScreen || isSharingOut) {
                val userId = zoomSDK.inMeetingService.activeShareUserID()

                zoomPiPModeVideoRtc.visibility = View.GONE
                zoomFullScreenModeVideoRtc.visibility = View.GONE
                zoomPiPModeShareRtc.visibility = View.VISIBLE
                zoomFullScreenModeShareRtc.visibility = View.VISIBLE

                val videoUnit = MobileRTCRenderInfo(0, 0, 100, 100)

                zoomFullScreenModeShareRtc.videoViewManager.addShareVideoUnit(userId, videoUnit)
                zoomPiPModeShareRtc.videoViewManager.addShareVideoUnit(userId, videoUnit)
            } else {
                zoomPiPModeVideoRtc.visibility = View.VISIBLE
                zoomFullScreenModeVideoRtc.visibility = View.VISIBLE
                zoomPiPModeShareRtc.visibility = View.GONE
                zoomFullScreenModeShareRtc.visibility = View.GONE
            }

        }

        updateZoomGrid()
    }

    inner class ActivityMeetingListener : MeetingServiceListener {
        override fun onMeetingStatusChanged(p0: MeetingStatus?, p1: Int, p2: Int) {
            if (p0 == MeetingStatus.MEETING_STATUS_DISCONNECTING) {
                finish()
                chatCacheManager.clean()
            }
        }

        override fun onMeetingParameterNotification(p0: MeetingParameter?) {
        }

    }

    inner class InMeetingListener() :
        SimplifiedInMeetingServiceListener() {
        override fun onMeetingUserJoin(p0: MutableList<Long>?) {
            updateZoomGrid()
        }

        override fun onMeetingUserLeave(p0: MutableList<Long>?) {
            updateZoomGrid()
        }

        override fun onMeetingHostChanged(p0: Long) {

        }

        override fun onActiveSpeakerVideoUserChanged(p0: Long) {
            if (p0 == zoomSDK.inMeetingService.myUserID) return

            updateSpeakerVideo(p0)
            updateZoomGrid()
        }

        override fun onChatMessageReceived(p0: InMeetingChatMessage?) {
            if (p0 == null) return

            chatCacheManager.write(p0)

            val layout = layoutInflater.inflate(R.layout.chat_item, null)
            val snackBar = Snackbar.make(
                findViewById<View>(android.R.id.content).rootView,
                "",
                Snackbar.LENGTH_LONG
            )
            val snackBarLayout = snackBar.view as SnackbarLayout
            val viewHolder = ZoomChatListAdapter.ZoomChatListViewHolder(layout)

            layout.setOnApplyWindowInsetsListener { view, insets ->
                val bottomInset = when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                        view.rootWindowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars()).bottom
                    }

                    else -> {
                        0
                    }
                }

                viewHolder.container.setPadding(25, 35, 25, bottomInset + 35)

                insets
            }

            viewHolder.renderChatView(p0)

            snackBarLayout.addView(layout)
            snackBar.show()
        }

        override fun onChatMsgDeleteNotification(p0: String?, p1: ChatMessageDeleteType?) {}
    }

    inner class ShareScreenListener : InMeetingShareListener {
        private val shareController = zoomSDK.inMeetingService.inMeetingShareController

        override fun onShareActiveUser(p0: Long) {
            btnToggleShare.setImageResource(if (shareController.isSharingScreen) R.drawable.zm_meeting_stop_share_normal else R.drawable.zm_share_btn_normal)

            updateMainStageView()
        }

        override fun onSharingStatus(p0: SharingStatus?, p1: Long) {
            updateMeetingControlsStatus()
        }

        override fun onShareUserReceivingStatus(p0: Long) {}

        override fun onShareSettingTypeChanged(p0: ShareSettingType?) {
            updateMeetingControlsStatus()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        with(zoomSDK) {
            inMeetingService.removeListener(inMeetingListener)
            meetingService.removeListener(meetingListener)
            inMeetingService.inMeetingShareController.removeListener(shareListener)
        }
    }
}