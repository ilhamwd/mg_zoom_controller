package com.wmi.mg_zoom_controller

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import us.zoom.sdk.InMeetingChatMessage
import java.text.SimpleDateFormat
import java.util.Date

class ZoomChatListAdapter(private val chats: List<InMeetingChatMessage>) :
    RecyclerView.Adapter<ZoomChatListAdapter.ZoomChatListViewHolder>() {

    private val userIdToColorMap = mutableMapOf<Long, Int>()

    class ZoomChatListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val dateFormatter = SimpleDateFormat("hh:mm")

        val container: ConstraintLayout
        val avatarContainer: RelativeLayout
        val avatarInitial: TextView
        val chatSenderName: TextView
        val chatTimestamp: TextView
        val chatContent: TextView
        val context: Context

        init {
            context = view.context
            container = view.findViewById(R.id.chatItemLayout)
            avatarContainer = view.findViewById(R.id.avatar)
            avatarInitial = view.findViewById(R.id.avatarInitial)
            chatSenderName = view.findViewById(R.id.sender)
            chatTimestamp = view.findViewById(R.id.timestamp)
            chatContent = view.findViewById(R.id.content)
        }

        fun renderChatView(data: InMeetingChatMessage) {
            chatContent.text = data.content
            chatTimestamp.text = dateFormatter.format(Date(data.time * 1000))
            avatarInitial.text = data.senderDisplayName[0].toString().uppercase()
            chatSenderName.text = data.senderDisplayName
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ZoomChatListViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.chat_item, parent, false)

        return ZoomChatListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return chats.size
    }

    override fun onBindViewHolder(holder: ZoomChatListViewHolder, position: Int) {
        val data = chats[position]

        holder.container.setPadding(0, 50, 0, 0)

        if (chats.size > 1 && position > 0 && chats[position - 1].senderUserId == data.senderUserId) {
            holder.chatSenderName.visibility = View.GONE
            holder.chatTimestamp.visibility = View.GONE
            holder.avatarContainer.visibility = View.INVISIBLE
            holder.avatarContainer.layoutParams.height = 0
            holder.container.setPadding(0, 5, 0, 0)
        }

        holder.renderChatView(data)
    }
}