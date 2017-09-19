package com.coolweather.app.coolweather.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.app.coolweather.R;
import com.coolweather.app.coolweather.service.AutoUpdateService;

/**
 * Created by root on 2017/9/19.
 */

public class WeatherSettings extends Activity {

    private Switch refreshSwitch;
    private TextView refreshState;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);
        refreshSwitch=(Switch) findViewById(R.id.refresh_switch);
        refreshState=(TextView) findViewById(R.id.refresh_state);

        refreshSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    Intent i=new Intent(WeatherSettings.this, AutoUpdateService.class);
                    startService(i);
                    Toast.makeText(WeatherSettings.this,"自动更新启动",Toast.LENGTH_SHORT).show();
                    refreshState.setText("on");


                }
                else{
                    Intent i=new Intent(WeatherSettings.this, AutoUpdateService.class);
                    stopService(i);
                    Toast.makeText(WeatherSettings.this,"自动更新关闭",Toast.LENGTH_SHORT).show();
                    refreshState.setText("off");

                }
            }
        });


    }
}
