import 'package:flutter/material.dart';
import 'package:mg_zoom_controller/mg_meeting_status.dart';
import 'package:mg_zoom_controller/mg_zoom_controller.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final _mgZoomControllerPlugin = MgZoomController();

  @override
  Widget build(BuildContext context) {
    const link =
        "https://us04web.zoom.us/j/8078400745?pwd=eEhnR1lZbWxzWkk5bzJ0bVZMOEtJdz09";

    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Column(
          children: [
            Center(
              child: ElevatedButton(
                  onPressed: () async {
                    final currentMeetingStatus =
                        await _mgZoomControllerPlugin.currentMeetingStatus;
                    if (currentMeetingStatus == MgZoomMeetingStatus.idle) {
                      _mgZoomControllerPlugin.joinMeeting(
                          link: link, displayName: "Qwertyuiop");
                    } else {
                      _mgZoomControllerPlugin.leaveMeeting();
                    }
                  },
                  child: const Text("Launch")),
            ),
            const SizedBox(height: 10),
            StreamBuilder(
                stream: _mgZoomControllerPlugin.meetingStatus,
                builder: (context, snapshot) {
                  return Text(snapshot.data.toString());
                })
          ],
        ),
      ),
    );
  }
}
