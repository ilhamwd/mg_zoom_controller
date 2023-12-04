package com.wmi.mg_zoom_controller

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.EventChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import us.zoom.sdk.JoinMeetingOptions
import us.zoom.sdk.JoinMeetingParams
import us.zoom.sdk.MeetingParameter
import us.zoom.sdk.MeetingServiceListener
import us.zoom.sdk.MeetingStatus
import us.zoom.sdk.ZoomSDK
import us.zoom.sdk.ZoomSDKInitParams
import us.zoom.sdk.ZoomSDKInitializeListener
import java.lang.Thread.sleep
import java.util.Date
import java.util.TimerTask
import java.util.logging.StreamHandler

class ZoomHelpers(context: Context, binaryMessenger: BinaryMessenger) {
    private val context = context
    private val meetingStatusEventChannel = EventChannel(binaryMessenger, "mg/zoom_meeting_status")
    private var meetingStatusSink: EventChannel.EventSink? = null
    private var isZoomReady: Boolean = false

    init {
        val sdk: ZoomSDK = ZoomSDK.getInstance()

        // Initialize event channel
        meetingStatusEventChannel.setStreamHandler(object : EventChannel.StreamHandler {
            override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                meetingStatusSink = events;
            }

            override fun onCancel(arguments: Any?) {
                meetingStatusSink = null
            }

        })

        val initParams = ZoomSDKInitParams()
        initParams.jwtToken = generateToken()

        sdk.initialize(context, InitializeListener(), initParams)
    }

    fun getMeetingAttendees(): List<Long> {
        val sdk: ZoomSDK = ZoomSDK.getInstance()

        return sdk.inMeetingService.inMeetingUserList.filter { it != sdk.inMeetingService.myUserID }
    }

    fun joinMeeting(link: String, displayName: String) {
        if (!isZoomReady) {
            return Toast.makeText(
                context,
                "Zoom SDK is not ready. Please wait for another minute.",
                Toast.LENGTH_LONG
            ).show()
        }

        val sdk: ZoomSDK = ZoomSDK.getInstance()
        val uri = Uri.parse(link)
        val meetingId = uri.lastPathSegment ?: ""
        val password = uri.getQueryParameter("pwd") ?: ""

        val options = JoinMeetingOptions()
        val params = JoinMeetingParams()

        with(params) {
            this.displayName = displayName
            this.password = password
            meetingNo = meetingId
        }

        sdk.meetingService.joinMeetingWithParams(context, params, options)
    }

    private fun generateToken(): String {
        val currentTime = Date().time / 1000
        val expiry = currentTime + (170000)
        return JWT.create().withExpiresAt(Date(expiry))
            .withClaim("iat", currentTime)
            .withClaim("appKey", "ErcYJ02Eo2KBJM8Z9myEnRVJTP9qMTn1iPPD")
            .withClaim("sdkKey", "ErcYJ02Eo2KBJM8Z9myEnRVJTP9qMTn1iPPD")
            .withClaim("role", "participant")
            .withClaim("exp", expiry)
            .withClaim("tokenExp", expiry)
            .sign(Algorithm.HMAC256("p5CINIuWDAGe2lAxOADEQyvQSazs43yUPLR4"))
    }

    inner class MeetingListener : MeetingServiceListener {
        override fun onMeetingStatusChanged(p0: MeetingStatus?, p1: Int, p2: Int) {
            meetingStatusSink?.success(p0?.name)
        }

        override fun onMeetingParameterNotification(p0: MeetingParameter?) {
        }
    }

    inner class InitializeListener : ZoomSDKInitializeListener {

        override fun onZoomSDKInitializeResult(p0: Int, p1: Int) {
            val sdk: ZoomSDK = ZoomSDK.getInstance()
            isZoomReady = true
            sdk.meetingSettingsHelper.isCustomizedMeetingUIEnabled = true

            sdk.meetingService.addListener(MeetingListener())
        }

        override fun onZoomAuthIdentityExpired() {
            Toast.makeText(
                context,
                "Your Zoom credential has expired. Please rejoin this meeting.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}