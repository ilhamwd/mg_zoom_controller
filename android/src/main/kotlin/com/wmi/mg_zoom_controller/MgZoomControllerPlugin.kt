package com.wmi.mg_zoom_controller

import android.app.Activity
import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.content.Intent
import android.media.AudioManager
import android.util.Log
import androidx.annotation.NonNull
import androidx.core.os.BuildCompat
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import us.zoom.sdk.MeetingStatus
import us.zoom.sdk.ZoomSDK
import java.util.Date

@BuildCompat.PrereleaseSdkCheck
/** MgZoomControllerPlugin */
class MgZoomControllerPlugin : FlutterPlugin, MethodCallHandler {
    private lateinit var channel: MethodChannel
    private lateinit var context: Context
    private lateinit var zoomHelpers: ZoomHelpers

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "mg_zoom_controller")
        context = flutterPluginBinding.applicationContext

        channel.setMethodCallHandler(this)

        // Zoom initialization
        zoomHelpers = ZoomHelpers(context, flutterPluginBinding.binaryMessenger)
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        Log.e("MethodName", call.method)

        when (call.method) {
            "joinMeeting" ->
                result.success(
                        zoomHelpers.joinMeeting(
                                call.argument<String>("link")!!, call.argument<String>("display_name")!!
                        )
                )

            "volumeUp" -> {
                val audioManager = context.getSystemService(AUDIO_SERVICE) as AudioManager

                audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_VIBRATE or AudioManager.FLAG_SHOW_UI)
            }

            "volumeDown" -> {
                val audioManager = context.getSystemService(AUDIO_SERVICE) as AudioManager

                audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_VIBRATE or AudioManager.FLAG_SHOW_UI)
            }

            "toggleSpeakerMute" -> {
                val audioManager = context.getSystemService(AUDIO_SERVICE) as AudioManager

                audioManager.adjustVolume(AudioManager.ADJUST_TOGGLE_MUTE, AudioManager.FLAG_VIBRATE or AudioManager.FLAG_SHOW_UI)
            }

            "getCurrentMeetingStatus" -> {
                val sdk = ZoomSDK.getInstance()

                try {
                    result.success(sdk.meetingService.meetingStatus.name)
                } catch (e: Exception) {
                    result.success(MeetingStatus.MEETING_STATUS_IDLE.name)
                }
            }

            "leaveMeeting" -> ZoomSDK.getInstance().meetingService.leaveCurrentMeeting(true)
            "launchMeetingActivity" -> context.startActivity(Intent(
                    context, ZoomActivity::class.java
            ).apply { this.flags = Intent.FLAG_ACTIVITY_NEW_TASK })
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)

        ZoomChatCacheManager(binding.applicationContext).clean()
        ZoomSDK.getInstance().uninitialize()
    }
}
