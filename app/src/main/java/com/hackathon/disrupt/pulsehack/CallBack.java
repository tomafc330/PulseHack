package com.hackathon.disrupt.pulsehack;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothDevice;

import java.lang.reflect.Method;

/**
 * Created by tchan on 11/09/16.
 */
public interface CallBack {
  void perform(BluetoothA2dp proxy, BluetoothDevice device, Method disconnectMethod);
}
