package edu.harvard.cs50.mapsplacesapp.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Button;
import android.view.View;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import edu.harvard.cs50.mapsplacesapp.R;

public class GuideActivity extends AppCompatActivity {

    private static final String TAG = "GuideActivity";

    private final static String map_introduction_text =
            "Here is a simple map that you can scroll around";
    private final static String info_button_text =
            "Find info about the place";
    private final static String search_field_text =
            "Search for a place anywhere in the world";
    private final static String places_button_text = "See what's there around you";
    private final static String zoom_buttons_text = "Adjust the size of the map";
    private final static String location_button_text = "Find your location";
    private final static String start_using_app = "Start using the App";

    private int touch_counter = 0;

    private RelativeLayout mRelLayout;
    private ImageView mMagnifyingGlass;
    private AutoCompleteTextView mAutoCompleteTextView;

    private View root;

    private ImageView mGpsGuide;
    private ImageView mPlacePicker;
    private ImageView mPlaceInfo;

    private TextView mGuideText;
    private Button mNextButton;

    private Animation icon_fade_anim;
    private Animation text_fade_anim;
    private Animation button_fade_anim;
    private Animation button_animation;

    private Drawable btn_gradient_style;

//    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.

//        getWindow().getDecorView().setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                        | View.SYSTEM_UI_FLAG_FULLSCREEN
//                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
//
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getSupportActionBar().hide();
        setContentView(R.layout.activity_guide);

        mGuideText = findViewById(R.id.general_guide_text);
        mNextButton = findViewById(R.id.button_next_guidance);

        root = findViewById(R.id.root_relLayout);

        // init guidance views
        mRelLayout = findViewById(R.id.relLayout1_guide);
        mMagnifyingGlass = findViewById(R.id.ic_magnify_guide);
        mAutoCompleteTextView = findViewById(R.id.input_search_guide);

        mPlacePicker = findViewById(R.id.place_picker_guide);
        mPlaceInfo = findViewById(R.id.place_info_guide);
        mGpsGuide = findViewById(R.id.ic_gps_guide);

        // set visibility
        mRelLayout.setVisibility(View.INVISIBLE);
        mMagnifyingGlass.setVisibility(View.INVISIBLE);
        mAutoCompleteTextView.setVisibility(View.INVISIBLE);

        mPlacePicker.setVisibility(View.INVISIBLE);
        mPlaceInfo.setVisibility(View.INVISIBLE);
        mGpsGuide.setVisibility(View.INVISIBLE);

        btn_gradient_style = getDrawable(R.drawable.btn_gradient_style);

        mAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(arg1.getApplicationWindowToken(), 0);
            }
        });

        mAutoCompleteTextView.setInputType(0);

        icon_fade_anim = AnimationUtils.loadAnimation(this, R.anim.fade_icon);

        text_fade_anim = AnimationUtils.loadAnimation(this, R.anim.fade_text);
        text_fade_anim.setStartOffset(2000L);

        button_fade_anim = AnimationUtils.loadAnimation(this, R.anim.fade_button);
        button_fade_anim.setStartOffset(4000L);

        button_animation = AnimationUtils.loadAnimation(this, R.anim.button_animation);

        mGuideText.startAnimation(text_fade_anim);
        mNextButton.startAnimation(button_fade_anim);

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeItAnimate(v);
            }
        });
    }

    private void makeItAnimate(View view) {
        touch_counter++;

        if (touch_counter == 1) {
            mRelLayout.setVisibility(View.VISIBLE);
            mMagnifyingGlass.setVisibility(View.VISIBLE);
            mAutoCompleteTextView.setVisibility(View.VISIBLE);

            mRelLayout.startAnimation(icon_fade_anim);
            mMagnifyingGlass.startAnimation(icon_fade_anim);
            mAutoCompleteTextView.startAnimation(icon_fade_anim);

            mGuideText.setText(search_field_text);

            root.setBackground(getDrawable(R.drawable.screenshot_middle_east));

            switchViewAnimation();
        } else if (touch_counter == 2) {
            mPlacePicker.setVisibility(View.VISIBLE);
            mPlacePicker.startAnimation(icon_fade_anim);

            mGuideText.setText(places_button_text);

            root.setBackground(getDrawable(R.drawable.screenshot_americas));

            switchViewAnimation();
        } else if (touch_counter == 3) {
            mPlaceInfo.setVisibility(View.VISIBLE);
            mPlaceInfo.startAnimation(icon_fade_anim);

            mGuideText.setText(info_button_text);

            root.setBackground(getDrawable(R.drawable.screenshot_asia));

            switchViewAnimation();
        } else if (touch_counter == 4) {
            mGpsGuide.setVisibility(View.VISIBLE);
            mGpsGuide.startAnimation(icon_fade_anim);

            mGuideText.setText(location_button_text);

            root.setBackground(getDrawable(R.drawable.screenshot_africa_europe));

            switchViewAnimation();
        } else if (touch_counter == 5) {

            mGuideText.setText(start_using_app);

            root.setBackground(getDrawable(R.drawable.screenshot_harvard_1));

            mNextButton.setBackground(btn_gradient_style);
            mNextButton.startAnimation(button_animation);
        } else {
            updateSharedPreferences();
            Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(mainActivity);
            finish();
        }
    }

    private void switchViewAnimation() {
        mGuideText.startAnimation(text_fade_anim);
        mNextButton.startAnimation(button_fade_anim);
    }

    private void updateSharedPreferences() {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(MainActivity.USER_PASSED_GUIDANCE, true);
        editor.apply();
    }
}
