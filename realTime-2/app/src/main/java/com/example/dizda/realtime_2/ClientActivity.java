package com.example.dizda.realtime_2;

import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by dizda on 8/28/16.
 */
public class ClientActivity implements Runnable {

    private static final int SERVERPORT = 8083;
    private static final String IP = "192.168.0.106";
    private ServerSocket sockfd;
    private Socket connfd;
    private static final String TAG = "MyActivity";

    static final int frequency = 44100;
    static final int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    boolean isPlaying;
    int playBufSize;
    AudioTrack audioTrack;

    @Override
    public void run() {
        playBufSize= AudioTrack.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
        audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, frequency, channelConfiguration, audioEncoding, playBufSize, AudioTrack.MODE_STREAM);
        audioTrack.setStereoVolume(1f, 1f);


        byte[] buffer = new byte[playBufSize];

        try { connfd = new Socket(IP, SERVERPORT); }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }
        audioTrack.play();
        isPlaying = true;
        while (isPlaying) {
            int readSize = 0;
            try {
                readSize = connfd.getInputStream().read(buffer);
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
            audioTrack.write(buffer, 0, readSize);
        }
        audioTrack.stop();
        try {
            connfd.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
