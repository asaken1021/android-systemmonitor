package net.asaken1021.systemmonitor;

import android.app.ActivityManager;
import android.app.IntentService;
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

    //メモリ関連
    ActivityManager am;
    ActivityManager.MemoryInfo mi;
    long availMem = 0;  //空きメモリ量
    long totalMem = 0;  //全体メモリ量
    long usedMem = 0;   //使用メモリ量
    long MemUsage = 0;   //メモリ使用率
    Notification notify;
    NotificationCompat.Builder builder;
    NotificationManagerCompat manager;
    String availMem_String = "";
    String totalMem_String = "";
    String usedMem_String = "";
    String MemUsage_String = "";

    //CPU関連
    String[] cmdArgs = {"/system/bin/cat", "/proc/stat"};
    String cpuLine = "";
    StringBuffer cpuBuffer = new StringBuffer();
    ProcessBuilder cmd = new ProcessBuilder(cmdArgs);
    int cpuUsedTime = 0;        //毎tick時のCPU使用時間
    int cpuPrevUsedTime = 0;    //前tick時のCPU使用時間
    int cpuIdleTime = 0;        //毎tick時のCPUのidle時間
    int cpuPrevIdleTime = 0;    //前tick時のCPUのidle時間
    int cpuTotalTime = 0;       //過去のCPU使用時間の総和
    boolean isFirstTick = true;
    boolean isSecondTick = true;
    int CpuUsage = 0;          //CPU使用率
    int debug_cpu_time = 0;     //デバッグ用
    String CpuUsage_String = "";

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
        builder.setContentTitle("システムモニター");
        builder.setContentText("Text");
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
        am.getMemoryInfo(mi);
        totalMem = mi.totalMem / 1024000;
        mTimer = new Timer(false);
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("DEBUG_LINE", "-------------------------------------------------------------------------");
                        count++;
                        am.getMemoryInfo(mi);
                        availMem = mi.availMem / 1024000;
                        usedMem = totalMem - availMem;
                        checkCpuUsage();
                        MemUsage = usedMem / (totalMem / 100);
                        Log.d("RAM_USAGE", "" + MemUsage);
                        CpuUsage_String = String.format("%3d", CpuUsage);
                        MemUsage_String = String.format("%3d", MemUsage);
                        availMem_String = String.format("%4d", availMem);
                        usedMem_String = String.format("%4d", usedMem);
                        totalMem_String = String.format("%4d", totalMem);
                        builder.setContentTitle("CPU: " + CpuUsage_String + "%" + " メモリ: " + MemUsage_String + "%");
                        builder.setContentText("空き/使用中/全体RAM: " + availMem_String + "/" + usedMem_String + "/" + totalMem_String + "MB");
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

            cpuBuffer = new StringBuffer();

            byte[] lineBytes = new byte[1024];

            while (in.read(lineBytes) != -1) {
                cpuBuffer.append(new String(lineBytes));
            }

            in.close();

        } catch (java.io.IOException exception) {

        }

        cpuLine = cpuBuffer.toString();

        int start = cpuLine.indexOf("cpu") + 5;
        int end = cpuLine.indexOf("cpu0");

        cpuLine = cpuLine.substring(start, end);

        Log.d("CPU_VALUES", cpuLine);

        //1つ目の値(user)の切り取り
        end = cpuLine.indexOf(" ");
        cpuUsedTime = Integer.parseInt(cpuLine.substring(0, end));
        debug_cpu_time = cpuUsedTime;
        Log.d("CPU_VALUE_USER", "" + cpuUsedTime);
        //2つ目の値(nice)の切り取り
        start = end + 1;
        cpuLine = cpuLine.substring(start);
        end = cpuLine.indexOf(" ");
        cpuUsedTime += Integer.parseInt(cpuLine.substring(0, end));
        Log.d("CPU_VALUE_NICE", "" + (cpuUsedTime - debug_cpu_time));
        debug_cpu_time = cpuUsedTime;
        //3つ目の値(system)の切り取り
        start = end + 1;
        cpuLine = cpuLine.substring(start);
        end = cpuLine.indexOf(" ");
        cpuUsedTime += Integer.parseInt(cpuLine.substring(0, end));
        Log.d("CPU_VALUE_SYST", "" + (cpuUsedTime - debug_cpu_time));
        debug_cpu_time = cpuUsedTime;
        //4つ目の値(idle)の切り取り
        start = end + 1;
        cpuLine = cpuLine.substring(start);
        end = cpuLine.indexOf(" ");
        cpuIdleTime = Integer.parseInt(cpuLine.substring(0, end));
        Log.d("CPU_VALUE_IDLE", "" + (cpuIdleTime));

        if (isFirstTick == true) {
            cpuTotalTime = (cpuUsedTime + cpuIdleTime);

            isFirstTick = false;
        } else {
            cpuTotalTime = (cpuUsedTime + cpuIdleTime) - (cpuPrevUsedTime + cpuPrevIdleTime);

            CpuUsage = (cpuUsedTime - cpuPrevUsedTime) * 100 / (cpuTotalTime);

            cpuPrevUsedTime = cpuUsedTime;
            cpuPrevIdleTime = cpuIdleTime;

            if (isSecondTick == true) {
                CpuUsage = 0;
                isSecondTick = false;
            }
        }

        Log.d("CPU_USAGE", "" + CpuUsage);
    }
}