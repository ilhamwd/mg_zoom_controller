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
import us.zoom.sdk.ZoomError
import us.zoom.sdk.ZoomSDK
import us.zoom.sdk.ZoomSDKInitParams
import us.zoom.sdk.ZoomSDKInitializeListener
import java.lang.Thread.sleep
import java.util.Date
import java.util.TimerTask
import java.util.logging.StreamHandler

class ZoomHelpers(private val context: Context, binaryMessenger: BinaryMessenger) {
    private var isInitialized = false
    private val meetingStatusEventChannel = EventChannel(binaryMessenger, "mg/zoom_meeting_status")
    private var meetingStatusSink: EventChannel.EventSink? = null

    init {
        // Initialize event channel
        meetingStatusEventChannel.setStreamHandler(object : EventChannel.StreamHandler {
            override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                meetingStatusSink = events;
            }

            override fun onCancel(arguments: Any?) {
                meetingStatusSink = null
            }
        })

        initialize()
    }

    fun getMeetingAttendees(): List<Long> {
        val sdk: ZoomSDK = ZoomSDK.getInstance()

        return sdk.inMeetingService.inMeetingUserList.filter { it != sdk.inMeetingService.myUserID }
    }

    fun joinMeeting(link: String, displayName: String): String {
        val sdk: ZoomSDK = ZoomSDK.getInstance()

        if (!sdk.isInitialized) {
            initialize()

            return "uninitialized"
        }

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

        return try {
            sdk.meetingService.joinMeetingWithParams(context, params, options)

            "success"
        } catch (e: Exception) {
            "failed"
        }
    }

    private fun generateToken(): String {
        val currentTime = Date().time / 1000
        val expiry = currentTime + (170000)
        return JWT.create().withExpiresAt(Date(expiry)).withClaim("iat", currentTime)
            .withClaim("appKey", "ErcYJ02Eo2KBJM8Z9myEnRVJTP9qMTn1iPPD")
            .withClaim("sdkKey", "ErcYJ02Eo2KBJM8Z9myEnRVJTP9qMTn1iPPD")
            .withClaim("role", "participant").withClaim("exp", expiry).withClaim("tokenExp", expiry)
            .sign(Algorithm.HMAC256("p5CINIuWDAGe2lAxOADEQyvQSazs43yUPLR4"))
    }

    fun initialize() {
        val sdk: ZoomSDK = ZoomSDK.getInstance()
        val initParams = ZoomSDKInitParams()
        initParams.jwtToken = generateToken()

        sdk.initialize(context, InitializeListener(), initParams)
    }

    inner class MeetingListener : MeetingServiceListener {
        override fun onMeetingStatusChanged(p0: MeetingStatus?, p1: Int, p2: Int) {
            try {
                if (p0 == MeetingStatus.MEETING_STATUS_DISCONNECTING) {
                    ZoomChatCacheManager(context).clean()
                }

                meetingStatusSink?.success(p0?.name)
            } catch (e: Exception) {
                Log.e(
                    "MeetingStatusEventChannel",
                    "Failed to post meeting status to EventChannel. Perhaps it was detached or the app was killed."
                )
            }
        }

        override fun onMeetingParameterNotification(p0: MeetingParameter?) {
        }
    }

    inner class InitializeListener : ZoomSDKInitializeListener {

        override fun onZoomSDKInitializeResult(errorCode: Int, p1: Int) {
            if (errorCode != ZoomError.ZOOM_ERROR_SUCCESS) {
                val message = when (errorCode) {
                    ZoomError.ZOOM_ERROR_NETWORK_UNAVAILABLE -> "Network unavailable"
                    else -> "Unable to initialize Zoom SDK"
                }

                Toast.makeText(context, "$message ($p1)", Toast.LENGTH_LONG).show()

                return
            }

            if (isInitialized) return

            isInitialized = true
            val sdk: ZoomSDK = ZoomSDK.getInstance()
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