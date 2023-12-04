package com.wmi.mg_zoom_controller

import android.content.Context
import us.zoom.sdk.IRichTextStyleItem
import us.zoom.sdk.InMeetingChatMessage
import us.zoom.sdk.ZoomSDKChatMessageType
import java.io.File

class ZoomChatCacheManager(private val context: Context) {
    private val file: File = File(context.cacheDir.absolutePath + "/zoom-chat.temp")

    fun write(data: InMeetingChatMessage) {
        val cur = read()

        file.writeText(
            "$cur\n" + "${data.msgId.toByteArray().joinToString(" ")}\t${data.senderUserId}\t${
                data.senderDisplayName.toByteArray().joinToString(" ")
            }\t${data.time}\t${
                data.content.toByteArray().joinToString(" ")
            }"
        )
    }

    fun clean(): Boolean {
        return if (file.exists()) file.delete() else false
    }

    fun read(): String {
        return if (file.exists()) file.readText() else ""
    }

    fun parseByteArrayFromRawData(rawData: String): ByteArray {
        var contentByteArray = byteArrayOf()
        val contentRaw = rawData.split(" ").map { it.toByte() }

        for (byte in contentRaw) {
            contentByteArray += byte
        }

        return contentByteArray
    }

    fun parse(): List<InMeetingChatMessage> {
        val data = mutableListOf<InMeetingChatMessage>()
        val storedData = read()
        val lineBreak = storedData.split("\n")

        for (rawChat in lineBreak) {
            val tabLine = rawChat.split("\t")

            if (tabLine.size < 5) continue

            data.add(object : InMeetingChatMessage {
                override fun getChatMessageType(): ZoomSDKChatMessageType {
                    return ZoomSDKChatMessageType.ZoomSDKChatMessageType_To_All
                }

                override fun getMsgId(): String {
                    return String(parseByteArrayFromRawData(tabLine[0]))
                }

                override fun getSenderUserId(): Long {
                    return tabLine[1].toLong()
                }

                override fun getSenderDisplayName(): String {
                    return String(parseByteArrayFromRawData(tabLine[2]))
                }

                override fun getReceiverUserId(): Long {
                    return 0
                }

                override fun getReceiverDisplayName(): String {
                    return ""
                }

                override fun getContent(): String {
                    return String(parseByteArrayFromRawData(tabLine[4]))
                }

                override fun getTime(): Long {
                    return tabLine[3].toLong()
                }

                override fun isChatToAll(): Boolean {
                    TODO("Not yet implemented")
                }

                override fun isChatToAllPanelist(): Boolean {
                    TODO("Not yet implemented")
                }

                override fun isChatToWaitingroom(): Boolean {
                    TODO("Not yet implemented")
                }

                override fun isComment(): Boolean {
                    TODO("Not yet implemented")
                }

                override fun isThread(): Boolean {
                    TODO("Not yet implemented")
                }

                override fun getTextStyleItemList(): MutableList<IRichTextStyleItem> {
                    TODO("Not yet implemented")
                }

                override fun getThreadId(): String {
                    TODO("Not yet implemented")
                }

            })
        }

        return data
    }
}