package com.hackathon.disrupt.pulsehack;

import com.harman.pulsesdk.PulseColor;

/**
 * Created by tchan on 11/09/16.
 */
public class SkinToneDetector {

  enum SkinTone {
    BEIGE(252, -1, 3);
    private final int r;
    private final int g;
    private final int b;

    SkinTone(int r, int g, int b) {
      this.r = r;
      this.g = g;
      this.b = b;
    }
  }

  public static boolean doesSkinToneMatch(PulseColor color) {
    return (SkinTone.BEIGE.r == -1 ? true : SkinTone.BEIGE.r == color.red) &&
        (SkinTone.BEIGE.g == -1 ? true : SkinTone.BEIGE.g == color.green) &&
        (SkinTone.BEIGE.r == -1 ? true : SkinTone.BEIGE.b == color.blue);
  }
}
