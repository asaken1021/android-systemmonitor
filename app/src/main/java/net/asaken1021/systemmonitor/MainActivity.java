package net.asaken1021.systemmonitor;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button startBtn;
    Button stopBtn;

    public static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startBtn = (Button) findViewById(R.id.startButton);
        stopBtn = (Button) findViewById(R.id.stopButton);

        context = this;
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
