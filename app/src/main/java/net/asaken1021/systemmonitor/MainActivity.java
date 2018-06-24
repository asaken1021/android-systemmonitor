package net.asaken1021.systemmonitor;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.CpuUsageInfo;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    Button startBtn;
    Button stopBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startBtn = (Button) findViewById(R.id.startButton);
        stopBtn = (Button) findViewById(R.id.stopButton);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
//        stopService(new Intent(MainActivity.this, SystemMonitorService.class));
    }

    public void startBtn(View v) {
        startService(new Intent(MainActivity.this, SystemMonitorService.class));
    }

    public void stopBtn(View v) {
        stopService(new Intent(MainActivity.this, SystemMonitorService.class));
    }
}
