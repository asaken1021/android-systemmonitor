package net.asaken1021.systemmonitor;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

public class SystemMonitorService extends Service {

    Handler mHandler;
    Timer mTimer;
    int count = 0;
    boolean isServiceStarted = false;
    Intent activity;

    ActivityManager am;
    ActivityManager.MemoryInfo mi;
    long availMem = 0;
    long totalMem = 0;
    long usedMem = 0;
    Notification notify;
    NotificationCompat.Builder builder;
    NotificationManagerCompat manager;

    String[] cmdArgs = {"/system/bin/cat", "/proc/stat"};
    String cpuLine = "";
    StringBuffer cpuBuffer = new StringBuffer();
    ProcessBuilder cmd = new ProcessBuilder(cmdArgs);
    int totalCpuTime = 0;
    int tickCpuTime = 0;
    int cpuLineBuffer = 0;
    boolean isFirstTick = true;
    long CpuUsage = 0;
    int debug_cpu_time = 0; //For debug

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("SystemMonitorService", "onCreate");
        mHandler = new Handler();

        activity = new Intent(getApplicationContext(), MainActivity.class);

        am = ((ActivityManager) getSystemService(MainActivity.context.ACTIVITY_SERVICE));
        mi = new ActivityManager.MemoryInfo();


        builder = new NotificationCompat.Builder(getApplicationContext());
        manager = NotificationManagerCompat.from(getApplicationContext());

        builder.setSmallIcon(R.drawable.cpu_icon);
        builder.setContentTitle("SystemMonitor");
        builder.setContentText("NotificationTest");
//        builder.setSubText("SubText");
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
                        Log.d("DEBUG_LINE", "-------------------------------------------------------------------------");
                        count++;
//                        builder.setSubText("Count: " + count);
                        am.getMemoryInfo(mi);
                        availMem = mi.availMem / 1024000;
                        totalMem = mi.totalMem / 1024000;
                        usedMem = totalMem - availMem;
                        checkCpuUsage();
                        Log.d("RAM_VALUE", "" + availMem);
                        builder.setContentTitle("CPU: " + CpuUsage + "%" + " 空きRAM: " + availMem + "MB");
                        builder.setContentText("使用中/全体RAM: " + usedMem + "/" + totalMem + "MB");
                        builder.setShowWhen(false);
                        notify = builder.build();
                        notify.flags = Notification.FLAG_NO_CLEAR;
                        manager.notify(1, notify);
                    }
                });
            }
        }, 0, 1000);
    }

    private void checkCpuUsage() {
        try {
            Process process = cmd.start(); //cat /proc/stat

            InputStream in = process.getInputStream();

            // 統計情報より1024バイト分を読み込む
            // cpu user/nice/system/idle/iowait/irq/softirq/steal/の情報を取得する

            byte[] lineBytes = new byte[1024];

            while (in.read(lineBytes) != -1) {
                cpuBuffer.append(new String(lineBytes));
            }

            in.close();

        } catch (java.io.IOException exception) {

        }

        cpuLine = cpuBuffer.toString();

        Log.d("cpuLine", cpuLine);

        // 1024バイトより「cpu～cpu0」までの文字列を抽出
        int start = cpuLine.indexOf("cpu") + 5;
        int end = cpuLine.indexOf("cpu0");

        cpuLine = cpuLine.substring(start, end);

        Log.d("CPU_VALUES", cpuLine);

        //1つ目の値の切り取り
        end = cpuLine.indexOf(" ");
        cpuLineBuffer = Integer.parseInt(cpuLine.substring(0, end));
        debug_cpu_time = cpuLineBuffer; //For debug
        Log.d("CPU_VALUE_1", "" + cpuLineBuffer);
        //2つ目の値の切り取り
        start = end + 1;
        cpuLine = cpuLine.substring(start);
        end = cpuLine.indexOf(" ");
        cpuLineBuffer += Integer.parseInt(cpuLine.substring(0, end));
        Log.d("CPU_VALUE_2", "" + (cpuLineBuffer - debug_cpu_time));
        debug_cpu_time = cpuLineBuffer;
        //3つ目の値の切り取り
        start = end + 1;
        cpuLine = cpuLine.substring(start);
        end = cpuLine.indexOf(" ");
        cpuLineBuffer += Integer.parseInt(cpuLine.substring(0, end));
        Log.d("CPU_VALUE_3", "" + (cpuLineBuffer - debug_cpu_time));
        debug_cpu_time = cpuLineBuffer;

        if (isFirstTick == true) {
            totalCpuTime = cpuLineBuffer;
            isFirstTick = false;
        } else {
            //これは正しい?
            tickCpuTime = totalCpuTime / cpuLineBuffer;
            CpuUsage = tickCpuTime / 1;
            totalCpuTime = cpuLineBuffer;
        }

        Log.d("CPU_TICK_VALUE", "" + tickCpuTime);

        Log.d("CPU_USAGE_VALUE", "" + CpuUsage);
    }
}