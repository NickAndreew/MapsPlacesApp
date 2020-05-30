package edu.harvard.cs50.mapsplacesapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import edu.harvard.cs50.mapsplacesapp.R;
import edu.harvard.cs50.mapsplacesapp.adapters.IntroViewPagerAdapter;
import edu.harvard.cs50.mapsplacesapp.model.ScreenItem;

public class IntroActivity extends AppCompatActivity {

    private IntroViewPagerAdapter introViewPagerAdapter;

    private TabLayout tabIndicator;

    private Animation btnAnimation;

    private ViewPager screenPager;

    private Button btnGetStarted;
    private Button btnNext;

    private int position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // make the activitry on full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // when this activity is about to be launched we need to check if it's opened before
        if (restorePrefData()) {

            Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(mainActivity);
            finish();
        }

        setContentView(R.layout.activity_intro);

        getSupportActionBar().hide();

        // init views
        btnNext = findViewById(R.id.btn_next);
        btnGetStarted = findViewById(R.id.btn_get_started);
        tabIndicator = findViewById(R.id.tab_indicator);
        btnAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.button_animation);

        // fill list screen
        List<ScreenItem> mList = new ArrayList<>();

        mList.add(
                new ScreenItem("Travel",
                "The app is supposed to inspire and help tourists and travelers, " +
                        "help with the routines of exploring the areas around the globe.",
                R.drawable.travel)
        );

        mList.add(
                new ScreenItem("Explore",
                "Discover the places remotely and search for " +
                        "locations and information about them.",
                R.drawable.wonders)
        );

        mList.add(
                new ScreenItem("See Places",
                "Make the App your pocket guide on the go and at home, " +
                        "let the app show you what is out there in the world.",
                R.drawable.places)
        );

        //setup viewpager
        screenPager = findViewById(R.id.screen_viewpager);
        introViewPagerAdapter = new IntroViewPagerAdapter(this, mList);
        screenPager.setAdapter(introViewPagerAdapter);

        // setup
        tabIndicator.setupWithViewPager(screenPager);

        // next button click Listener
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                position = screenPager.getCurrentItem();
                if (position < mList.size()) {

                    position++;
                    screenPager.setCurrentItem(position);

                }

                if (position == mList.size() - 1) { // when we read to the last screen
                    // TODO : show the GETSTARTED Button and hide the indicator and the next button
                    loadLastScreen();


                }
            }
        });

        // tab layout add change listener

        tabIndicator.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == mList.size() - 1) {
                    loadLastScreen();
                } else {
                    loadBasicScreen();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        // Get Started button click Listener

        btnGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // open main activity
                Intent mainActivity = new Intent(getApplicationContext(), GuideActivity.class);
                startActivity(mainActivity);
                // also we need to save a boolean value to storage so next time when the user run the app
                // we could know that he is already checked the intro activity
                // i'm going to use shared preferences to that process

                savePrefsData();
                finish();

            }
        });
    }

    private boolean restorePrefData() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("myPrefs", MODE_PRIVATE);
        Boolean isIntroActivityOpenedBefore = pref.getBoolean("isIntroOpened", false);
        return isIntroActivityOpenedBefore;
    }

    private void savePrefsData() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("myPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isIntroOpened", true);
        editor.apply();
    }

    // show the GETSTARTED Button and hide the indicator and the next button
    private void loadLastScreen() {
        btnNext.setVisibility(View.INVISIBLE);
        tabIndicator.setVisibility(View.INVISIBLE);
        btnGetStarted.setVisibility(View.VISIBLE);

        // TODO : ADD and animation the getstarted button
        // setup animation
        btnGetStarted.setAnimation(btnAnimation);
    }

    private void loadBasicScreen() {
        btnNext.setVisibility(View.VISIBLE);
        tabIndicator.setVisibility(View.VISIBLE);
        btnGetStarted.setVisibility(View.INVISIBLE);
    }
    // https://youtu.be/pwcG6npiXyo?t=434
}
