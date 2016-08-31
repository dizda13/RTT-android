package com.example.dizda.realtime_2;

import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by dizda on 8/28/16.
 */
public class ServerActivity implements Runnable {

    private static final int SERVERPORT = 8083;
    private ServerSocket sockfd;
    private Socket connfd;
    private static final String TAG = "MyActivity";

    static final int frequency = 44100;
    static final int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    boolean isRecording=false;
    int recBufSize;
    AudioRecord audioRecord;


    @Override
    public void run() {

        try { sockfd = new ServerSocket(SERVERPORT); }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }

        Log.v(TAG, "pocelo");
        try {
            connfd = sockfd.accept();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        Log.v(TAG, "Connected");

        recBufSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, audioEncoding, recBufSize);

        while(true){
            byte[] buffer = new byte[recBufSize];
            if(isRecording){
                audioRecord.startRecording();
            }
        //isRecording = true;
        while (isRecording) {
            int readSize = audioRecord.read(buffer, 0, recBufSize);
            try { connfd.getOutputStream().write(buffer, 0, readSize); }
            catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
            audioRecord.stop();
        }

    }

    public void changeState(){
        if(isRecording)
            isRecording=false;
        else
            isRecording=true;
    }
}

