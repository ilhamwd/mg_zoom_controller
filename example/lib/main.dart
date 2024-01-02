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
              child: Row(
                children: [
                  ElevatedButton(
                      onPressed: () async {
                        final currentMeetingStatus =
                            await MgZoomController.currentMeetingStatus;
                        if (currentMeetingStatus == MgZoomMeetingStatus.idle) {
                          MgZoomController.joinMeeting(
                              link: link, displayName: "Qwertyuiop");
                        } else {
                          MgZoomController.leaveMeeting();
                        }
                      },
                      child: const Text("Launch")),
                  const SizedBox(width: 15),
                  ElevatedButton(
                      onPressed: () {
                        MgZoomController.launchMeetingActivity();
                      },
                      child: const Text("Open activity")),
                ],
              ),
            ),
            Row(
              children: [
                ElevatedButton(
                    onPressed: () {
                      MgZoomController.volumeDown();
                    },
                    child: const Text("-")),
                const SizedBox(width: 15),
                ElevatedButton(
                    onPressed: () {
                      MgZoomController.volumeUp();
                    },
                    child: const Text("+")),
                const SizedBox(width: 15),
                ElevatedButton(
                    onPressed: () {
                      MgZoomController.toggleSpeakerMute();
                    },
                    child: const Text("Toggle Mute")),
              ],
            ),
            const SizedBox(height: 10),
            StreamBuilder(
                stream: MgZoomController.meetingStatus,
                builder: (context, snapshot) {
                  return Text(snapshot.data.toString());
                })
          ],
        ),
      ),
    );
  }
}
