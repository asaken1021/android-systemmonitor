package net.asaken1021.systemmonitor;

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class SystemMonitorIntentService extends IntentService {

    boolean isServiceStarted = false;

    public SystemMonitorIntentService() {
        super("SystemMonitorIService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("SystemMonitorIService", "onCreate");
        if (isServiceStarted == false) {
            startService(new Intent(this, SystemMonitorService.class));
            isServiceStarted = true;
        } else {
            Log.d("SystemMonitorIService", "already started");
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i("SystemMonitorIService", "onStartCommand");
        return START_STICKY;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("SystemMonitorIService", "onDestroy");
        stopService(new Intent(this, SystemMonitorService.class));
        isServiceStarted = false;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        super.onBind(arg0);
        Log.i("SystemMonitorIService", "onBind");
        return null;
    }
}
