package com.example.dizda.realtime_2;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class MediaStreamClient {
    static final int frequency = 44100;
    static final int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    boolean isPlaying;
    int playBufSize;
    Socket connfd;
    ServerSocket sockfd;
    AudioTrack audioTrack;
    private static final String TAG = "MyActivity";
    public static final int SERVERPORT = 8083;


    public MediaStreamClient(final Context ctx) {
        playBufSize=AudioTrack.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
        audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, frequency, channelConfiguration, audioEncoding, playBufSize, AudioTrack.MODE_STREAM);
        audioTrack.setStereoVolume(1f, 1f);

        new Thread() {
            byte[] buffer = new byte[playBufSize];
            public void run() {
                try { sockfd = new ServerSocket(SERVERPORT); }
                catch (Exception e) {
                    e.printStackTrace();
                    Intent intent = new Intent()
                            .setAction("tw.rascov.MediaStreamer.ERROR")
                            .putExtra("msg", e.toString());
                    ctx.sendBroadcast(intent);
                    return;
                }
                while(true) {
                    Log.v(TAG, "pocelo");
                    try {
                        connfd = sockfd.accept();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Intent intent = new Intent()
                                .setAction("tw.rascov.MediaStreamer.ERROR")
                                .putExtra("msg", e.toString());
                        ctx.sendBroadcast(intent);
                    }
                    Log.v(TAG, "Connected");
                    audioTrack.play();
                    isPlaying = true;
                    while (isPlaying) {
                        int readSize = 0;
                        try {
                            readSize = connfd.getInputStream().read(buffer);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Intent intent = new Intent()
                                    .setAction("tw.rascov.MediaStreamer.ERROR")
                                    .putExtra("msg", e.toString());
                            ctx.sendBroadcast(intent);
                            break;
                        }
                        audioTrack.write(buffer, 0, readSize);
                    }
                    audioTrack.stop();
                    audioTrack.release();
                    try {
                        connfd.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }.start();
    }

    public void stop() {
        isPlaying = false;
    }

    public void setVolume(float lvol, float rvol) {
        audioTrack.setStereoVolume(lvol, rvol);
    }
}