package com.hackathon.disrupt.pulsehack;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

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

import bluetoothconnector.BluetoothSwitcher;
import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements PulseNotifiedListener {

  static String Tag = "PulseDemo";
  boolean isActive;
  Timer mTimer = null;
  boolean isConnectBT;

  List<String> unreadMessages = new ArrayList<>();

  public ImplementPulseHandler pulseHandler = new ImplementPulseHandler();
  private SpeechWrapper speechWrapper;

  PulseColor[] currentColors = getEmptyPulseColors();
  private boolean isCapturing;
  private int soundLevel = -1;
  private static MainActivity instance;

  @Bind(R.id.everest_status)
  public Button everestStatus;

  @Bind(R.id.pulse_status)
  public Button pulseStatus;

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
    blinkTimer.scheduleAtFixedRate(task, 2000, 750);


    Timer cancelBlinkTimer = new Timer();
    cancelBlinkTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        MainActivity.this.runOnUiThread(new Runnable() {
          @Override
          public synchronized void run() {
            blinkTimer.cancel();
            isCapturing = false;
            soundLevel = -1;
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
    isConnectBT = true;
    cancelTimer();
    Toast.makeText(this, "onConnectMasterDevice", Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onDisconnectMasterDevice() {
    Log.i(Tag, "onDisconnectMasterDevice");
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
      speakAndReset(unreadMessages.get(unreadMessages.size() - 1));
    }
  }

  @Override
  public void onRetCaptureColor(final PulseColor capturedColor) {
    Log.e("MainActivity", "Color: R=" + capturedColor.red + " G=" + capturedColor.green + " B=" + capturedColor.blue);
    if (SkinToneDetector.doesSkinToneMatch(capturedColor)) {
      sendToHeadphones();
    }
  }

  @Override
  public void onRetCaptureColor(byte red, byte green, byte blue) {
  }

  private void sendToHeadphones() {
    if (!unreadMessages.isEmpty()) {
      new BluetoothSwitcher(this, new CallBack() {
        public void perform(final BluetoothA2dp proxy, final BluetoothDevice headphones, final BluetoothDevice pulse, final Method disconnectMethod, final Method connectMethod) {
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              speakAndReset(unreadMessages.get(unreadMessages.size() - 1));

              Timer unPairTimer = new Timer();
              unPairTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                  MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public synchronized void run() {
                      try {
                        everestStatus.setText("Disconnected");
                        pulseStatus.setText("Connected");
                        disconnectMethod.invoke(proxy, headphones);
                        connectMethod.invoke(proxy, pulse);
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


  public void speakAndReset(String string) {
    speechWrapper.speak(string);
    this.soundLevel = -1;
    isCapturing = false;
    resetUnread();
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

