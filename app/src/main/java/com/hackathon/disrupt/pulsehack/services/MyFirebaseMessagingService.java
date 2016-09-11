/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hackathon.disrupt.pulsehack.services;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

  private static final String TAG = "MyFirebaseMsgService";

  /**
   * Called when message is received.
   *
   * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
   */
  // [START receive_message]
  @Override
  public void onMessageReceived(RemoteMessage remoteMessage) {
    Log.d(TAG, "From: " + remoteMessage.getFrom());
    if (remoteMessage.getData().size() > 0) {
      Log.d(TAG, "Message data payload: " + remoteMessage.getData());
    }
    if (remoteMessage.getNotification() != null) {
      Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
    }

    String type = remoteMessage.getData().get("type"); // could be message or call
    String message = remoteMessage.getData().get("message");

    sendNotification(type, message);
  }

  /**
   * Create and show a simple notification containing the received FCM message.
   *
   */
  private void sendNotification(String gate, String locationIds) {
//    Intent intent = new Intent(this, LaunchRideActivity.class);
//    intent.putExtra("locationIds", locationIds);
//    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
//        PendingIntent.FLAG_ONE_SHOT);
//
//
//    Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
//        .setSmallIcon(R.drawable.ic_airplanemode_active_black_18dp)
//        .setContentTitle("Your flight has arrived!")
//        .setContentText("Please meet the other party at baggage carousel " + gate)
//        .setAutoCancel(true)
//        .setSound(defaultSoundUri)
//        .setContentIntent(pendingIntent);
//
//    NotificationManager notificationManager =
//        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//    notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
  }
}