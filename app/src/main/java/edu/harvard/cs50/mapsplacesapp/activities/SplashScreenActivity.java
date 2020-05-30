package edu.harvard.cs50.mapsplacesapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import edu.harvard.cs50.mapsplacesapp.R;
import gr.net.maroulis.library.EasySplashScreen;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_splash_screen);

        EasySplashScreen config = new EasySplashScreen(SplashScreenActivity.this)
                .withFullScreen()
                .withTargetActivity(IntroActivity.class)
                .withSplashTimeOut(5000)
                .withBackgroundColor(Color.parseColor("#ffffff"))
                .withLogo(R.mipmap.ic_launcher)
                .withAfterLogoText("Loading ...")
                .withFooterText("Created for CS50 by Mykyta Andreiev");

        config.getLogo().setScaleX(2.5f);
        config.getLogo().setScaleY(2.5f);
        config.getLogo().setPadding( 0, 0, 0, 50);

        config.getAfterLogoTextView().setPadding(0, 160, 0, 0);
        config.getAfterLogoTextView().setTextColor(Color.BLACK);
        config.getAfterLogoTextView().setTextSize(25f);

        config.getFooterTextView().setTextColor(Color.BLACK);
        config.getFooterTextView().setTextSize(12f);

        View easySplashScreen = config.create();
        setContentView(easySplashScreen);
    }
}
