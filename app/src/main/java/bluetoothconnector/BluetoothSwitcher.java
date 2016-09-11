package bluetoothconnector;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import com.hackathon.disrupt.pulsehack.CallBack;
import com.hackathon.disrupt.pulsehack.MainActivity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


public class BluetoothSwitcher implements BluetoothBroadcastReceiver.Callback, BluetoothA2DPRequester.Callback {
  private static final String TAG = "BluetoothSwitcher";

  /**
   * This is the name of the device to connect to. You can replace this with the name of
   * your device.
   */
  private static final String DEVICE_NAME_HEADPHONES = "JBL Everest Elite 700";
  private static final String DEVICE_NAME_PULSE = "NotiflyPulse";

  /**
   * Local reference to the device's BluetoothAdapter
   */
  private BluetoothAdapter mAdapter;
  private MainActivity context;
  private CallBack callBack;

  public BluetoothSwitcher(MainActivity context, CallBack callBack) {
    this.context = context;
    this.callBack = callBack;

    mAdapter = BluetoothAdapter.getDefaultAdapter();

    if (mAdapter.isEnabled()) {
      onBluetoothConnected();
      return;
    }

    //Check if we're allowed to enable Bluetooth. If so, listen for a
    //successful enabling
    if (mAdapter.enable()) {
      BluetoothBroadcastReceiver.register(this, context);
    } else {
      Log.e(TAG, "Unable to enable Bluetooth. Is Airplane Mode enabled?");
    }
  }

  @Override
  public void onBluetoothError() {
    Log.e(TAG, "There was an error enabling the Bluetooth Adapter.");
  }

  @Override
  public void onBluetoothConnected() {
    new BluetoothA2DPRequester(this).request(context, mAdapter);
  }

  @Override
  public void onA2DPProxyReceived(final BluetoothA2dp proxy) {
    final Method connectMethod = getConnectMethod();
    final Method disconnectMethod = getDisconnectMethod();
    final BluetoothDevice pulse = findBondedDeviceByName(mAdapter, DEVICE_NAME_PULSE);
    final BluetoothDevice headphones = findBondedDeviceByName(mAdapter, DEVICE_NAME_HEADPHONES);

    //If either is null, just return. The errors have already been logged
    if (connectMethod == null || headphones == null || pulse == null) {
      return;
    }

    try {
      //disconnect pulse first
      disconnectMethod.invoke(proxy, pulse);
      connectMethod.invoke(proxy, headphones);
      context.everestStatus.setText("Connected");
      context.pulseStatus.setText("Disconnected");
      new Timer().schedule(new TimerTask() {
        @Override
        public void run() {
          callBack.perform(proxy, headphones, pulse, disconnectMethod, connectMethod);
        }
      }, 5500);
    } catch (InvocationTargetException ex) {
      Log.e(TAG, "Unable to invoke connectMethod(BluetoothDevice) method on proxy. " + ex.toString());
    } catch (IllegalAccessException ex) {
      Log.e(TAG, "Illegal Access! " + ex.toString());
    }
  }

  private Method getDisconnectMethod() {
    Method disconnect = null;
    try {
      disconnect = BluetoothA2dp.class.getDeclaredMethod("disconnect", BluetoothDevice.class);
      disconnect.setAccessible(true);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
    return disconnect;
  }

  /**
   * Wrapper around some reflection code to get the hidden 'connect()' method
   *
   * @return the connect(BluetoothDevice) method, or null if it could not be found
   */
  private Method getConnectMethod() {
    try {
      Method method = BluetoothA2dp.class.getDeclaredMethod("connect", BluetoothDevice.class);
      method.setAccessible(true);
      return method;
    } catch (NoSuchMethodException ex) {
      Log.e(TAG, "Unable to find connect(BluetoothDevice) method in BluetoothA2dp proxy.");
      return null;
    }
  }

  /**
   * Search the set of bonded devices in the BluetoothAdapter for one that matches
   * the given name
   *
   * @param adapter the BluetoothAdapter whose bonded devices should be queried
   * @param name    the name of the device to search for
   * @return the BluetoothDevice by the given name (if found); null if it was not found
   */
  private static BluetoothDevice findBondedDeviceByName(BluetoothAdapter adapter, String name) {
    for (BluetoothDevice device : getBondedDevices(adapter)) {
      if (name.matches(device.getName())) {
        Log.v(TAG, String.format("Found device with name %s and address %s.", device.getName(), device.getAddress()));
        return device;
      }
    }
    Log.w(TAG, String.format("Unable to find device with name %s.", name));
    return null;
  }

  /**
   * Safety wrapper around BluetoothAdapter#getBondedDevices() that is guaranteed
   * to return a non-null result
   *
   * @param adapter the BluetoothAdapter whose bonded devices should be obtained
   * @return the set of all bonded devices to the adapter; an empty set if there was an error
   */
  private static Set<BluetoothDevice> getBondedDevices(BluetoothAdapter adapter) {
    Set<BluetoothDevice> results = adapter.getBondedDevices();
    if (results == null) {
      results = new HashSet<BluetoothDevice>();
    }
    return results;
  }
}
