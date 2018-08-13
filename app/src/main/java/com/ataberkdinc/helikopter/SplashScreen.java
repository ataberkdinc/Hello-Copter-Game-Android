package com.ataberkdinc.helikopter;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import gr.net.maroulis.library.EasySplashScreen;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        EasySplashScreen config = new EasySplashScreen(SplashScreen.this)
                .withFullScreen()
                .withTargetActivity(Oyun.class)
                .withSplashTimeOut(3000)
                .withBackgroundColor(Color.parseColor("#363636"))
                .withLogo(R.drawable.atagames);

        View view = config.create();

        setContentView(view);
    }
}
