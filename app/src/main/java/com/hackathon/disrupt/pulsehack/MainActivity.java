package com.hackathon.disrupt.pulsehack;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.hackathon.disrupt.pulsehack.model.BooleanWrapper;
import com.hackathon.disrupt.pulsehack.util.ContactUtil;
import com.harman.pulsesdk.DeviceModel;
import com.harman.pulsesdk.ImplementPulseHandler;
import com.harman.pulsesdk.PulseColor;
import com.harman.pulsesdk.PulseNotifiedListener;
import com.harman.pulsesdk.PulseThemePattern;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import bluetoothconnector.BluetoothConnector;
import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements PulseNotifiedListener {

  static String Tag = "PulseDemo";
  boolean isActive;
  Timer mTimer = null;
  boolean isConnectBT;

  List<String> unreadMessages = new ArrayList<>();

  @Bind(R.id.is_on)
  public ToggleButton isOnButton;

  public ImplementPulseHandler pulseHandler = new ImplementPulseHandler();
  private SpeechWrapper speechWrapper;

  PulseColor[] currentColors = getEmptyPulseColors();
  private boolean isCapturing;
  private int soundLevel = -1;
  private static MainActivity instance;

  public static MainActivity getInstance() {
    return instance;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.activity_main);
    ButterKnife.bind(this);

    instance = this;

    speechWrapper = new SpeechWrapper(getApplicationContext());
    pulseHandler.ConnectMasterDevice(this);
    pulseHandler.registerPulseNotifiedListener(this);

    setTimer();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    isActive = false;
    System.exit(0);
  }

  public void anonMessageReceived(String message) {
    updateColors(false);
    pulseHandler.SetColorImage(currentColors);
    blink5Times();
    unreadMessages.add(message);
  }

  public void contactMessageReceived(String message) {
    updateColors(true);
    pulseHandler.SetColorImage(currentColors);
    blink5Times();
    unreadMessages.add(message);
  }

  private void blink5Times() {
    isCapturing = true;
    pulseHandler.GetMicrophoneSoundLevel();

    final Timer blinkTimer = new Timer();
    final BooleanWrapper boolWrapper = new BooleanWrapper();
    TimerTask task = new TimerTask() {
      @Override
      public void run() {
        MainActivity.this.runOnUiThread(new Runnable() {
          @Override
          public synchronized void run() {
            if (isCapturing) {
              pulseHandler.GetMicrophoneSoundLevel();
            }
            if (boolWrapper.bool) {
              boolWrapper.bool = false;
              pulseHandler.SetColorImage(currentColors);
            } else {
              boolWrapper.bool = true;
              pulseHandler.SetColorImage(getColorsForLastMessage());
            }
          }
        });
      }
    };
    blinkTimer.scheduleAtFixedRate(task, 2000, 1000);


    Timer cancelBlinkTimer = new Timer();
    cancelBlinkTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        MainActivity.this.runOnUiThread(new Runnable() {
          @Override
          public synchronized void run() {
            isCapturing = false;
            soundLevel = -1;
            blinkTimer.cancel();
            pulseHandler.SetColorImage(currentColors);
          }
        });
      }
    }, 10000);


  }

  private void resetUnread() {
    unreadMessages.clear();
    currentColors = getEmptyPulseColors();
  }

  private void updateColors(boolean isContact) {
    int size = unreadMessages.size();
    int end = size > 0 ? 99 - (size * 22) : 99;
    for (int i = 77 - (size * 22); i < end; i++) {
      PulseColor pulseColor = new PulseColor();
      pulseColor.red = isContact ? (byte) -1 : (byte) 0;
      pulseColor.green = isContact ? (byte) 0 : (byte) -1;
      pulseColor.blue = 0;
      currentColors[i] = pulseColor;
    }
  }

  public PulseColor[] getColorsForLastMessage() {
    PulseColor[] results = currentColors.clone();
    int size = unreadMessages.size();
    int start = 77 - (size * 22) < 0 ? 0 : 77 - (size * 22);
    int end = 99 - ((size - 1) * 22) > 99 ? 99 : 99 - ((size - 1) * 22);
    for (int i = start; i < end; i++) {
      PulseColor pulseColor = new PulseColor();
      pulseColor.red = 0;
      pulseColor.green = 0;
      pulseColor.blue = 0;
      results[i] = pulseColor;
    }

    return results;
  }

  @NonNull
  private PulseColor[] getEmptyPulseColors() {
    PulseColor[] results = new PulseColor[99];
    init(results);
    return results;
  }

  private void init(PulseColor[] results) {
    for (int i = 0; i < results.length; i++) {
      results[i] = new PulseColor();
    }
  }

  public void setTimer() {
    if (mTimer != null)
      return;

    mTimer = new Timer();
    TimerTask task = new TimerTask() {
      @Override
      public void run() {
        MainActivity.this.runOnUiThread(new Runnable() {
          @Override
          public synchronized void run() {
            if (isConnectBT) {
              pulseHandler.ConnectMasterDevice(MainActivity.this);
            }
          }
        });
      }
    };
    mTimer.schedule(task, 1000, 1500);
  }

  private void cancelTimer() {
    if (mTimer != null) {
      mTimer.cancel();
      mTimer = null;
    }
  }

  @Override
  public void onConnectMasterDevice() {
    Log.i(Tag, "onConnectMasterDevice");
    isOnButton.setChecked(true);
    isConnectBT = true;
    cancelTimer();
    Toast.makeText(this, "onConnectMasterDevice", Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onDisconnectMasterDevice() {
    Log.i(Tag, "onDisconnectMasterDevice");
    isOnButton.setChecked(false);
    isConnectBT = false;
    setTimer();
    Toast.makeText(this, "onDisconnectMasterDevice", Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onLEDPatternChanged(PulseThemePattern pattern) {
  }

  @Override
  public void onSoundEvent(final int level) {
    Log.i(Tag, "soundLevel:" + level + " previousLevel: " + this.soundLevel);
    if (this.soundLevel == -1) {
      this.soundLevel = level;
    } else if (this.soundLevel + 5 < level) {
      speak(unreadMessages.get(unreadMessages.size() - 1));
      this.soundLevel = -1;
      isCapturing = false;
      resetUnread();
    }
  }

  @Override
  public void onRetCaptureColor(final PulseColor capturedColor) {
    if (isAllSameColor(capturedColor)) {
      sendToHeadphones(capturedColor);
    }
  }

  @Override
  public void onRetCaptureColor(byte red, byte green, byte blue) {
  }

  private void sendToHeadphones(PulseColor capturedColor) {
    unreadMessages.add("This is a test message, Please come back for dinner later");
    if (!unreadMessages.isEmpty() && isAllSameColor(capturedColor)) {
      new BluetoothConnector(this, new CallBack() {
        public void perform(final BluetoothA2dp proxy, final BluetoothDevice device, final Method disconnectMethod) {
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              speak(unreadMessages.get(unreadMessages.size() - 1));

              Timer unPairTimer = new Timer();
              unPairTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                  MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public synchronized void run() {
                      disconnectMethod.setAccessible(true);
                      try {
                        disconnectMethod.invoke(proxy, device);
                      } catch (IllegalAccessException e) {
                        e.printStackTrace();
                      } catch (InvocationTargetException e) {
                        e.printStackTrace();
                      }
                    }
                  });
                }
              }, 5000);

            }
          });
        }

        ;
      });
    }
  }

  private boolean isAllSameColor(PulseColor capturedColor) {
    Log.e("MainActivity", "Color: R=" + capturedColor.red + " G=" + capturedColor.green + " B=" + capturedColor.blue);
    return false;
  }

  private void unpairDevice(BluetoothDevice device) {
    try {
      Method m = device.getClass()
          .getMethod("removeBond", (Class[]) null);
      m.invoke(device, (Object[]) null);
    } catch (Exception e) {
      Log.e("MainActivity", e.getMessage());
    }
  }

  @Override
  public void onRetSetDeviceInfo(boolean ret) {
//    Toast.makeText(this, "onRetSetDeviceInfo:" + ret, Toast.LENGTH_SHORT);
  }

  @Override
  public void onRetGetLEDPattern(PulseThemePattern pattern) {
//    Toast.makeText(this, "onRetGetLEDPattern:" + (pattern == null ? "null" : pattern.name()), Toast.LENGTH_SHORT);
  }

  @Override
  public void onRetRequestDeviceInfo(DeviceModel[] deviceModel) {
//    Toast.makeText(this, "onRetRequestDeviceInfo:" + deviceModel.toString(), Toast.LENGTH_SHORT);
  }

  @Override
  public void onRetSetLEDPattern(boolean b) {
    Log.i(Tag, "onRetSetLEDPattern:" + b);
  }

  @Override
  public void onRetBrightness(int i) {
  }


  public void speak(String string) {
    speechWrapper.speak(string);
  }

  public void onSmsReceived(String msgFrom, String msgBody) {
    Log.i("MainActivity", msgFrom + msgBody);
    // check to see if it's a contact
    String contact = ContactUtil.findPhoneContactByNumber(this, msgFrom);
    if (contact == null) {
      anonMessageReceived(msgBody + " from unknown caller.");
    } else {
      contactMessageReceived(msgBody + " from " + contact);
    }

  }
}

