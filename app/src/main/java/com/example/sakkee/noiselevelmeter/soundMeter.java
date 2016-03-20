package com.example.sakkee.noiselevelmeter;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class soundMeter {

    private AudioRecord ar = null;
    private int minSize;

    //Start recording
    public void start() {
        minSize= AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        ar = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000,AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,minSize);
        ar.startRecording();
    }
    //Stop recording
    public void stop() {
        if (ar != null) {
            ar.stop();
        }
    }
    //Get decibel
    public double getDecibel() {
        short[] buffer = new short[minSize];
        ar.read(buffer, 0, minSize);
        int max = 0;
        for (short s : buffer)
        {
            if (Math.abs(s) > max)
            {
                max = Math.abs(s);
            }
        }
        //Returns 20*log10(amplitude) = decibel
        return 20*Math.log10(max);
    }

}