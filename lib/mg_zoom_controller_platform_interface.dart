import 'package:mg_zoom_controller/mg_meeting_status.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'mg_zoom_controller_method_channel.dart';

abstract class MgZoomControllerPlatform extends PlatformInterface {
  /// Constructs a MgZoomControllerPlatform.
  MgZoomControllerPlatform() : super(token: _token);

  static final Object _token = Object();

  Stream<MgZoomMeetingStatus>? meetingStatusStream;

  static MgZoomControllerPlatform _instance = MethodChannelMgZoomController();

  /// The default instance of [MgZoomControllerPlatform] to use.
  ///
  /// Defaults to [MethodChannelMgZoomController].
  static MgZoomControllerPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [MgZoomControllerPlatform] when
  /// they register themselves.
  static set instance(MgZoomControllerPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<bool> joinMeeting(
      {required String displayName, required String link}) {
    throw UnimplementedError('joinMeeting() has not been implemented.');
  }

  Future<bool> leaveMeeting() {
    throw UnimplementedError('leaveMeeting() has not been implemented.');
  }

  Future<MgZoomMeetingStatus> get currentMeetingStatus {
    throw UnimplementedError('Getter currentMeetingStatus has not been implemented.');
  }
  
  Future<bool> launchMeetingActivity() {
   throw UnimplementedError("launchMeetingActivity() has not been implemented.");
  }

  Stream<MgZoomMeetingStatus> get meetingStatus {
    throw UnimplementedError('Getter meetingStatus has not been implemented.');
  }
}
