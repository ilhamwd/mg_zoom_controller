package com.wmi.mg_zoom_controller

import us.zoom.sdk.ChatMessageDeleteType
import us.zoom.sdk.FreeMeetingNeedUpgradeType
import us.zoom.sdk.IRequestLocalRecordingPrivilegeHandler
import us.zoom.sdk.InMeetingAudioController
import us.zoom.sdk.InMeetingChatController
import us.zoom.sdk.InMeetingChatMessage
import us.zoom.sdk.InMeetingEventHandler
import us.zoom.sdk.InMeetingServiceListener
import us.zoom.sdk.LocalRecordingRequestPrivilegeStatus
import us.zoom.sdk.VideoQuality

abstract class SimplifiedInMeetingServiceListener: InMeetingServiceListener {
    override fun onMeetingNeedPasswordOrDisplayName(
        p0: Boolean,
        p1: Boolean,
        p2: InMeetingEventHandler?
    ) {
        
    }

    override fun onWebinarNeedRegister(p0: String?) {
        
    }

    override fun onJoinWebinarNeedUserNameAndEmail(p0: InMeetingEventHandler?) {
        
    }

    override fun onMeetingNeedCloseOtherMeeting(p0: InMeetingEventHandler?) {
        
    }

    override fun onMeetingFail(p0: Int, p1: Int) {
        
    }

    override fun onMeetingLeaveComplete(p0: Long) {
        
    }

    override fun onMeetingUserJoin(p0: MutableList<Long>?) {
        
    }

    override fun onMeetingUserLeave(p0: MutableList<Long>?) {
        
    }

    override fun onMeetingUserUpdated(p0: Long) {
        
    }

    override fun onInMeetingUserAvatarPathUpdated(p0: Long) {
        
    }

    override fun onMeetingHostChanged(p0: Long) {
        
    }

    override fun onMeetingCoHostChanged(p0: Long) {
        
    }

    override fun onMeetingCoHostChange(p0: Long, p1: Boolean) {
        
    }

    override fun onActiveVideoUserChanged(p0: Long) {
        
    }

    override fun onActiveSpeakerVideoUserChanged(p0: Long) {
        
    }

    override fun onHostVideoOrderUpdated(p0: MutableList<Long>?) {
        
    }

    override fun onFollowHostVideoOrderChanged(p0: Boolean) {
        
    }

    override fun onSpotlightVideoChanged(p0: Boolean) {
        
    }

    override fun onSpotlightVideoChanged(p0: MutableList<Long>?) {
        
    }

    override fun onUserVideoStatusChanged(p0: Long, p1: InMeetingServiceListener.VideoStatus?) {
        
    }

    override fun onUserNetworkQualityChanged(p0: Long) {
        
    }

    override fun onSinkMeetingVideoQualityChanged(p0: VideoQuality?, p1: Long) {
        
    }

    override fun onMicrophoneStatusError(p0: InMeetingAudioController.MobileRTCMicrophoneError?) {
        
    }

    override fun onUserAudioStatusChanged(p0: Long, p1: InMeetingServiceListener.AudioStatus?) {
        
    }

    override fun onHostAskUnMute(p0: Long) {
        
    }

    override fun onHostAskStartVideo(p0: Long) {
        
    }

    override fun onUserAudioTypeChanged(p0: Long) {
        
    }

    override fun onMyAudioSourceTypeChanged(p0: Int) {
        
    }

    override fun onLowOrRaiseHandStatusChanged(p0: Long, p1: Boolean) {
        
    }

    override fun onChatMessageReceived(p0: InMeetingChatMessage?) {
        
    }

    override fun onChatMsgDeleteNotification(p0: String?, p1: ChatMessageDeleteType?) {
        
    }

    override fun onShareMeetingChatStatusChanged(p0: Boolean) {
        
    }

    override fun onSilentModeChanged(p0: Boolean) {
        
    }

    override fun onFreeMeetingReminder(p0: Boolean, p1: Boolean, p2: Boolean) {
        
    }

    override fun onMeetingActiveVideo(p0: Long) {
        
    }

    override fun onSinkAttendeeChatPriviledgeChanged(p0: Int) {
        
    }

    override fun onSinkAllowAttendeeChatNotification(p0: Int) {
        
    }

    override fun onSinkPanelistChatPrivilegeChanged(p0: InMeetingChatController.MobileRTCWebinarPanelistChatPrivilege?) {
        
    }

    override fun onUserNameChanged(p0: Long, p1: String?) {
        
    }

    override fun onUserNamesChanged(p0: MutableList<Long>?) {
        
    }

    override fun onFreeMeetingNeedToUpgrade(p0: FreeMeetingNeedUpgradeType?, p1: String?) {
        
    }

    override fun onFreeMeetingUpgradeToGiftFreeTrialStart() {
        
    }

    override fun onFreeMeetingUpgradeToGiftFreeTrialStop() {
        
    }

    override fun onFreeMeetingUpgradeToProMeeting() {
        
    }

    override fun onClosedCaptionReceived(p0: String?, p1: Long) {
        
    }

    override fun onRecordingStatus(p0: InMeetingServiceListener.RecordingStatus?) {
        
    }

    override fun onLocalRecordingStatus(p0: Long, p1: InMeetingServiceListener.RecordingStatus?) {
        
    }

    override fun onInvalidReclaimHostkey() {
        
    }

    override fun onPermissionRequested(p0: Array<out String>?) {
        
    }

    override fun onAllHandsLowered() {
        
    }

    override fun onLocalVideoOrderUpdated(p0: MutableList<Long>?) {
        
    }

    override fun onLocalRecordingPrivilegeRequested(p0: IRequestLocalRecordingPrivilegeHandler?) {
        
    }

    override fun onSuspendParticipantsActivities() {
        
    }

    override fun onAllowParticipantsStartVideoNotification(p0: Boolean) {
        
    }

    override fun onAllowParticipantsRenameNotification(p0: Boolean) {
        
    }

    override fun onAllowParticipantsUnmuteSelfNotification(p0: Boolean) {
        
    }

    override fun onAllowParticipantsShareWhiteBoardNotification(p0: Boolean) {
        
    }

    override fun onMeetingLockStatus(p0: Boolean) {
        
    }

    override fun onRequestLocalRecordingPrivilegeChanged(p0: LocalRecordingRequestPrivilegeStatus?) {
        
    }

    override fun onAICompanionActiveChangeNotice(p0: Boolean) {
        
    }

    override fun onParticipantProfilePictureStatusChange(p0: Boolean) {
        
    }
}