package com.hackathon.disrupt.pulsehack;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.hackathon.disrupt.pulsehack.model.BooleanWrapper;
import com.harman.pulsesdk.DeviceModel;
import com.harman.pulsesdk.ImplementPulseHandler;
import com.harman.pulsesdk.PulseColor;
import com.harman.pulsesdk.PulseNotifiedListener;
import com.harman.pulsesdk.PulseThemePattern;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements PulseNotifiedListener {

  static String Tag = "PulseDemo";
  boolean isActive;
  Timer mTimer = null;
  boolean isConnectBT;

  int unreadMessages = 0;

  @Bind(R.id.is_on)
  public ToggleButton isOnButton;

  public ImplementPulseHandler pulseHandler = new ImplementPulseHandler();

  PulseColor[] currentColors = getEmptyPulseColors();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.activity_main);
    ButterKnife.bind(this);

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

  @OnClick(R.id.simulate_anon_contact)
  public void anonClick() {
    updateColors(false);
    pulseHandler.SetColorImage(currentColors);
    blink5Times();
    unreadMessages++;
  }

  @OnClick(R.id.simulate_verified_contact)
  public void contactClick() {
    updateColors(true);
    pulseHandler.SetColorImage(currentColors);
    blink5Times();
    unreadMessages++;
  }

  private void blink5Times() {
    final Timer blinkTimer = new Timer();
    final BooleanWrapper boolWrapper = new BooleanWrapper();
    TimerTask task = new TimerTask() {
      @Override
      public void run() {
        MainActivity.this.runOnUiThread(new Runnable() {
          @Override
          public synchronized void run() {
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
            blinkTimer.cancel();
            pulseHandler.SetColorImage(currentColors);
          }
        });
      }
    }, 5000);


  }

  private void resetUnread() {
    unreadMessages = 0;
    currentColors = getEmptyPulseColors();
  }

  private void updateColors(boolean isContact) {
    int end = unreadMessages > 0 ? 99 - ((unreadMessages) * 22) : 99;
    for (int i = 77 - (unreadMessages * 22); i < end; i++) {
      PulseColor pulseColor = new PulseColor();
      pulseColor.red = isContact ? (byte) -1 : (byte) 0;
      pulseColor.green = isContact ? (byte) 0 : (byte) -1;
      pulseColor.blue = 0;
      currentColors[i] = pulseColor;
    }
  }

  public PulseColor[] getColorsForLastMessage() {
    PulseColor[] results = currentColors.clone();
    for (int i = 77 - (unreadMessages * 22); i < 99 - ((unreadMessages - 1) * 22); i++) {
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


  public void pick_color(View v) {
    pulseHandler.CaptureColorFromColorPicker();
  }

  public void record_sound(View v) {
    pulseHandler.GetMicrophoneSoundLevel();
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
    //Toast.makeText(this, "onLEDPatternChanged:" + pattern.name(), Toast.LENGTH_SHORT);
    Log.i(Tag, "onLEDPatternChanged");

//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        });
  }

  @Override
  public void onSoundEvent(final int soundLevel) {
    //Toast.makeText(this, "onSoundEvent: level=" + soundLevel, Toast.LENGTH_SHORT);
    Log.i(Tag, "soundLevel:" + soundLevel);
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(MainActivity.this, "soundLevel:" + soundLevel, Toast.LENGTH_SHORT);
      }
    });
  }

  @Override
  public void onRetCaptureColor(final PulseColor capturedColor) {
    Toast.makeText(this,
        "onRetCaptureColor: red=" + capturedColor.red + " green=" + capturedColor.green + " blue=" + capturedColor.blue,
        Toast.LENGTH_SHORT);
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        pulseHandler.SetBackgroundColor(capturedColor, false);
        Log.i(Tag, "red:" + (((int) capturedColor.red) & 0xff) + " green:" + (((int) capturedColor.green) & 0xff) + " blue:" + (((int) capturedColor.blue) & 0xff));
      }
    });
  }

  @Override
  public void onRetCaptureColor(byte red, byte green, byte blue) {
    Toast.makeText(this, "onRetCaptureColor1: red=" + red + " green=" + green + " blue=" + blue, Toast.LENGTH_SHORT);
  }

  @Override
  public void onRetSetDeviceInfo(boolean ret) {
    Toast.makeText(this, "onRetSetDeviceInfo:" + ret, Toast.LENGTH_SHORT);
  }

  @Override
  public void onRetGetLEDPattern(PulseThemePattern pattern) {
    Toast.makeText(this, "onRetGetLEDPattern:" + (pattern == null ? "null" : pattern.name()), Toast.LENGTH_SHORT);
  }

  @Override
  public void onRetRequestDeviceInfo(DeviceModel[] deviceModel) {
    Toast.makeText(this, "onRetRequestDeviceInfo:" + deviceModel.toString(), Toast.LENGTH_SHORT);
  }

  @Override
  public void onRetSetLEDPattern(boolean b) {
    Log.i(Tag, "onRetSetLEDPattern:" + b);
    pulseHandler.PropagateCurrentLedPattern();
  }

  @Override
  public void onRetBrightness(int i) {

  }

}

