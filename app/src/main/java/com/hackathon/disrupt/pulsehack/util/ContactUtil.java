package com.hackathon.disrupt.pulsehack.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

/**
 * Created by tchan on 11/07/16.
 */
public class ContactUtil {

  public static String findPhoneContactByNumber(Context context, String phoneNumber) {
    Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));

    Cursor cursor = context.getContentResolver().query(uri, new String[] {ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

    if (cursor.moveToNext()) {
      return cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
    }

    cursor.close();

    return null;
  }
}
