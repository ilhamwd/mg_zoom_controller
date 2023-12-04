import 'package:mg_zoom_controller/mg_meeting_status.dart';

import 'mg_zoom_controller_platform_interface.dart';

export 'mg_meeting_status.dart';

abstract class MgZoomController {
  static Future<bool> joinMeeting({
    required String link,
    required String displayName,
  }) =>
      MgZoomControllerPlatform.instance
          .joinMeeting(link: link, displayName: displayName);

  static Future<bool> leaveMeeting() =>
      MgZoomControllerPlatform.instance.leaveMeeting();

  static Stream<MgZoomMeetingStatus> get meetingStatus =>
      MgZoomControllerPlatform.instance.meetingStatus;

  static Future<MgZoomMeetingStatus> get currentMeetingStatus =>
      MgZoomControllerPlatform.instance.currentMeetingStatus;

  static Future<bool> launchMeetingActivity() => MgZoomControllerPlatform.instance.launchMeetingActivity();
}
