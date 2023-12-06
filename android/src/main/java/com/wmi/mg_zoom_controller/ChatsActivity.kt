package com.wmi.mg_zoom_controller

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import us.zoom.sdk.InMeetingChatController
import us.zoom.sdk.InMeetingChatMessage
import us.zoom.sdk.InMeetingServiceListener
import us.zoom.sdk.MeetingParameter
import us.zoom.sdk.MeetingServiceListener
import us.zoom.sdk.MeetingStatus
import us.zoom.sdk.ZoomSDK

class ChatsActivity : AppCompatActivity() {
    private lateinit var inMeetingListener: InMeetingServiceListener
    private lateinit var meetingListener: MeetingServiceListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chats)

        val sdk = ZoomSDK.getInstance()
        val chatCacheManager = ZoomChatCacheManager(this)
        val btnSend = findViewById<ImageView>(R.id.btnSend)
        val messageTextBox = findViewById<EditText>(R.id.messageTextBox)
        val chatList = findViewById<RecyclerView>(R.id.chatsListView)
        var chats = mutableListOf<InMeetingChatMessage>().apply { addAll(chatCacheManager.parse()) }
        val chatAdapter = ZoomChatListAdapter(chats)
        inMeetingListener = object : SimplifiedInMeetingServiceListener() {
            override fun onChatMessageReceived(p0: InMeetingChatMessage?) {
                super.onChatMessageReceived(p0)

                if (p0 == null) return

                chats.add(p0)
                chatList.adapter!!.notifyItemInserted(chats.size - 1)
            }
        }
        meetingListener = object : MeetingServiceListener {
            override fun onMeetingStatusChanged(p0: MeetingStatus?, p1: Int, p2: Int) {
                if (p0 == MeetingStatus.MEETING_STATUS_DISCONNECTING) {
                    finish()
                }
            }

            override fun onMeetingParameterNotification(p0: MeetingParameter?) {}
        }

        btnSend.setOnClickListener {
            if (messageTextBox.text.isBlank()) return@setOnClickListener

            sdk.inMeetingService.inMeetingChatController.sendChatToGroup(
                InMeetingChatController.MobileRTCChatGroup.MobileRTCChatGroup_All,
                messageTextBox.text.toString()
            )
            messageTextBox.text.clear()
        }

        chatList.layoutManager = LinearLayoutManager(this)
        chatList.adapter = chatAdapter

        sdk.inMeetingService.addListener(inMeetingListener)
        sdk.meetingService.addListener(meetingListener)
    }

    override fun onDestroy() {
        with(ZoomSDK.getInstance()) {
            inMeetingService.removeListener(inMeetingListener)
            meetingService.removeListener(meetingListener)
        }
        super.onDestroy()
    }
}