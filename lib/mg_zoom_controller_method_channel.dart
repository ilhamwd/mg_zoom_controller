import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:mg_zoom_controller/mg_meeting_launch_status.dart';
import 'package:mg_zoom_controller/mg_meeting_status.dart';

import 'mg_zoom_controller_platform_interface.dart';

/// An implementation of [MgZoomControllerPlatform] that uses method channels.
class MethodChannelMgZoomController extends MgZoomControllerPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('mg_zoom_controller');

  @override
  Future<MgMeetingLaunchStatus> joinMeeting(
      {required String displayName, required String link}) async {
    try {
      late StreamSubscription<MgZoomMeetingStatus> meetingStatusSub;
      meetingStatusSub = meetingStatus.listen((event) async {
        if (event == MgZoomMeetingStatus.disconnecting) {
          await meetingStatusSub.cancel();
        }

        if (event == MgZoomMeetingStatus.inMeeting) {
          await meetingStatusSub.cancel();
          methodChannel.invokeMethod('launchMeetingActivity');
        }
      });

      final result = await methodChannel.invokeMethod(
          'joinMeeting', {"link": link, "display_name": displayName});

      if (result != "success") {
        meetingStatusSub.cancel();
      }

      switch (result) {
        case "success":
          return MgMeetingLaunchStatus.success;
        case "uninitialized":
          return MgMeetingLaunchStatus.uninitialized;
        case "failed":
        default:
          throw 1;
      }
    } catch (e) {
      return MgMeetingLaunchStatus.failed;
    }
  }

  @override
  Future<bool> launchMeetingActivity() async {
    try {
      await methodChannel.invokeMethod('launchMeetingActivity');
      return true;
    } catch (e) {
      return false;
    }
  }

  @override
  Future<bool> leaveMeeting() async {
    try {
      await methodChannel.invokeMethod('leaveMeeting');

      return true;
    } catch (e) {
      return false;
    }
  }

  @override
  Stream<MgZoomMeetingStatus> get meetingStatus {
    if (meetingStatusStream == null) {
      final streamController = StreamController<MgZoomMeetingStatus>();
      const eventChannel = EventChannel('mg/zoom_meeting_status');

      eventChannel.receiveBroadcastStream().listen((event) {
        streamController.sink.add(_parseMeetingStatus(event));
      });

      meetingStatusStream = streamController.stream.asBroadcastStream();
    }

    return meetingStatusStream!;
  }

  @override
  Future<MgZoomMeetingStatus> get currentMeetingStatus async {
    final rawStatus =
        await methodChannel.invokeMethod("getCurrentMeetingStatus");

    return _parseMeetingStatus(rawStatus);
  }

  MgZoomMeetingStatus _parseMeetingStatus(event) {
    var status = MgZoomMeetingStatus.idle;

    switch (event as String) {
      case "MEETING_STATUS_IDLE":
        status = MgZoomMeetingStatus.idle;
        break;
      case "MEETING_STATUS_CONNECTING":
        status = MgZoomMeetingStatus.connecting;
        break;
      case "MEETING_STATUS_WAITINGFORHOST":
        status = MgZoomMeetingStatus.waitingForHost;
        break;
      case "MEETING_STATUS_IN_WAITING_ROOM":
        status = MgZoomMeetingStatus.inWaitingRoom;
        break;
      case "MEETING_STATUS_INMEETING":
        status = MgZoomMeetingStatus.inMeeting;
        break;
      case "MEETING_STATUS_RECONNECTING":
        status = MgZoomMeetingStatus.reconnecting;
        break;
      case "MEETING_STATUS_DISCONNECTING":
        status = MgZoomMeetingStatus.disconnecting;
        break;
      case "MEETING_STATUS_ENDED":
        status = MgZoomMeetingStatus.ended;
        break;
      case "MEETING_STATUS_FAILED":
        status = MgZoomMeetingStatus.failed;
        break;
    }
    return status;
  }
}
