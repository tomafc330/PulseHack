package com.hackathon.disrupt.pulsehack;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

public class SpeechWrapper {
    private final TextToSpeech tts;

    public SpeechWrapper(final Context context) {
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(Locale.UK);
                }
            }
        });
    }

    public void speak(final String string) {
        tts.speak(string, TextToSpeech.QUEUE_FLUSH, null);
    }
}
