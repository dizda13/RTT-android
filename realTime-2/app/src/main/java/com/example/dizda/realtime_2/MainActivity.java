package com.example.dizda.realtime_2;

import android.content.Intent;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {

    private TextView serverStatus;
    private EditText serverIp;
    private Button nazovi;
    private Button serverMod;
    private Button connect;
    //private String line;
    // DEFAULT IP
    public static String SERVERIP = "";
    private MediaStreamServer mss;
    private MediaStreamClient msc;
    // DESIGNATE A PORT
    public static final int SERVERPORT = 8080;
    boolean isRecording;
    ServerSocket sockfd;
    Socket connfd;
    private static final String TAG = "MyActivity";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nazovi = (Button) findViewById(R.id.nazovi);
        serverIp = (EditText) findViewById(R.id.ipAdress);
        serverMod = (Button) findViewById(R.id.serverMod);
        connect = (Button) findViewById(R.id.connect);
        serverStatus=(TextView) findViewById(R.id.labela);
        nazovi.setOnTouchListener(nazoviL);
        serverMod.setOnClickListener(listenPort);
        connect.setOnClickListener(sendRequest);
        isRecording=false;
    }

    private View.OnClickListener sendRequest=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //Intent intent=new Intent(this, ServerActivity);
            SERVERIP = getLocalIpAddress();
            String ip=serverIp.getText().toString();
            msc=new MediaStreamClient(MainActivity.this,ip,SERVERPORT);
            /*msc=new MediaStreamClient(MainActivity.this,ip,SERVERPORT);
            msc.play(MainActivity.this);*/
        }
    };

    private View.OnClickListener listenPort=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //Intent intent=new Intent(this, ServerActivity);
            try { sockfd = new ServerSocket(SERVERPORT); }
            catch (Exception e) {
                e.printStackTrace();
                Intent intent = new Intent()
                        .setAction("tw.rascov.MediaStreamer.ERROR")
                        .putExtra("msg", e.toString());
                MainActivity.this.sendBroadcast(intent);
                return;
            }
            new Thread() {
               public void run() {
                    Log.v(TAG, "pocelo");
                    try {
                        connfd = sockfd.accept();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Intent intent = new Intent()
                                .setAction("tw.rascov.MediaStreamer.ERROR")
                                .putExtra("msg", e.toString());
                        MainActivity.this.sendBroadcast(intent);
                        return;
                    }
                    Log.v(TAG, "Connected");
                }
            }.start();

        }
    };



    private View.OnTouchListener nazoviL=new View.OnTouchListener() {

        public boolean onTouch(View v, MotionEvent event) {
            // TODO Auto-generated method stub
            //String mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
            //mFileName += "/audiorecordtest.3gp";
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mss=new MediaStreamServer(MainActivity.this,connfd);
                    break;
                case MotionEvent.ACTION_UP:
                    mss.stop(sockfd);
                    break;
            }
            return false;
        }
    };

    private String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) { return inetAddress.getHostAddress().toString(); }
                }
            }
        } catch (SocketException ex) {
            Log.e("ServerActivity", ex.toString());
        }
        return null;
    }

}
