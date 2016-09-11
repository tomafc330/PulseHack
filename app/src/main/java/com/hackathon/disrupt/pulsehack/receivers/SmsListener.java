package com.hackathon.disrupt.pulsehack.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.hackathon.disrupt.pulsehack.MainActivity;

public class SmsListener extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
      Bundle bundle = intent.getExtras();
      SmsMessage[] msgs = null;
      String msgFrom;
      if (bundle != null) {
        try {
          Object[] pdus = (Object[]) bundle.get("pdus");
          msgs = new SmsMessage[pdus.length];
          for (int i = 0; i < msgs.length; i++) {
            msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
            msgFrom = msgs[i].getOriginatingAddress();
            String msgBody = msgs[i].getMessageBody();

            MainActivity instance = MainActivity.getInstance();
            if (instance != null) {
              instance.onSmsReceived(msgFrom, msgBody);
            }
          }
        } catch (Exception e) {
          Log.e("SMSListener", "Exception caught: " + e.getMessage());
        }
      }
    }
  }

}
