package net.asaken1021.systemmonitor;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.CpuUsageInfo;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class SystemMonitorService extends Service {


    Handler mHandler;
    Timer mTimer;
    int count = 0;
    boolean isServiceStarted = false;
    CpuUsageInfo cpu;
    ActivityManager am = ((ActivityManager) getSystemService(Activity.ACTIVITY_SERVICE));
    ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
    int availMem = 0;
    Notification notify;
    NotificationCompat.Builder builder;
    NotificationManagerCompat manager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("SystemMonitorService", "onCreate");
        mHandler = new Handler();

        builder = new NotificationCompat.Builder(getApplicationContext());
        manager = NotificationManagerCompat.from(getApplicationContext());

        builder.setSmallIcon(R.drawable.cpu_icon);
        builder.setContentTitle("SystemMonitor");
        builder.setContentText("NotificationTest");
        builder.setSubText("SubText");
        builder.setAutoCancel(false);
        if (isServiceStarted == false) {
            start();
            isServiceStarted = true;
        } else {
            Log.d("SystemMonitorService", "Service was already started.");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("SystemMonitorService", "onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i("SystemMonitorService", "onDestroy");
        manager.cancel(1);
        isServiceStarted = false;
        mTimer.cancel();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        Log.i("SystemMonitorService", "onBind");
        return null;
    }

    private void start() {
        mTimer = new Timer(false);
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        count++;
//                        builder.setSubText("Count: " + count);
                        am.getMemoryInfo(mi);
                        availMem = (int) mi.availMem / 1024;
                        builder.setContentText("CPU: " + cpu.getActive() + "ms" + " RAM: " + availMem + "MB");
                        notify = builder.build();
                        notify.flags = Notification.FLAG_NO_CLEAR;
                        manager.notify(1, notify);
                    }
                });
            }
        }, 0, 1000);
    }
}
