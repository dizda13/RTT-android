package com.example.dizda.realtime_2;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class MediaStreamServer {
    static final int frequency = 44100;
    static final int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    boolean isRecording;
    int recBufSize;
    public static final int SERVERPORT = 8083;
    //ServerSocket sockfd;
    Socket connfd;
    AudioRecord audioRecord;

    public MediaStreamServer(final Context ctx, final String ip) {
        recBufSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, audioEncoding, recBufSize);

        new Thread() {
            byte[] buffer = new byte[recBufSize];
            public void run() {
                try { connfd = new Socket(ip, SERVERPORT); }
                catch (Exception e) {
                    e.printStackTrace();
                    Intent intent = new Intent()
                            .setAction("tw.rascov.MediaStreamer.ERROR")
                            .putExtra("msg", e.toString());
                    ctx.sendBroadcast(intent);
                    return;
                }
                audioRecord.startRecording();
                isRecording = true;
                while (isRecording) {
                    int readSize = audioRecord.read(buffer, 0, recBufSize);
                    try { connfd.getOutputStream().write(buffer, 0, readSize); }
                    catch (Exception e) {
                        e.printStackTrace();
                        Intent intent = new Intent()
                                .setAction("tw.rascov.MediaStreamer.ERROR")
                                .putExtra("msg", e.toString());
                        ctx.sendBroadcast(intent);
                        break;
                    }
                }
                audioRecord.stop();
                audioRecord.release();
                try { connfd.close(); }
                catch (Exception e) { e.printStackTrace();}
            }

        }.start();
    }

    public void stop(ServerSocket sockfd) {
        isRecording = false;
        /*try { sockfd.close(); }
        catch (Exception e) { e.printStackTrace(); }*/
    }
}