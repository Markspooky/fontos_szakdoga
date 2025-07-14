package com.example.szakdoga;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.ImageView;
import android.animation.ObjectAnimator;
import android.animation.AnimatorSet;


import com.google.android.material.switchmaterial.SwitchMaterial;

public class MainActivity extends AppCompatActivity {

//    private SharedPreferences sharedPreferences;
//    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        View title = findViewById(R.id.title);
        View belepes = findViewById(R.id.belepes);
        View pdfek = findViewById(R.id.pdfek);

        Animation anim1 = AnimationUtils.loadAnimation(this, R.anim.fade_in_slide_up);
        Animation anim2 = AnimationUtils.loadAnimation(this, R.anim.fade_in_slide_up);
        Animation anim3 = AnimationUtils.loadAnimation(this, R.anim.fade_in_slide_up);

        anim2.setStartOffset(150);
        anim3.setStartOffset(300);

        title.startAnimation(anim1);
        belepes.startAnimation(anim2);
        pdfek.startAnimation(anim3);

//        ImageView themeToggle = findViewById(R.id.themeToggle);
//
//// SharedPreferences
//        sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
//        editor = sharedPreferences.edit();
//
//        boolean isDarkModeOn = sharedPreferences.getBoolean("isDarkModeOn", false);
//
//// Apply theme
//        if (isDarkModeOn) {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//            themeToggle.setImageResource(R.drawable.ic_sun);
//        } else {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//            themeToggle.setImageResource(R.drawable.ic_moon);
//        }
//
//        themeToggle.setOnClickListener(v -> {
//            boolean isDark = sharedPreferences.getBoolean("isDarkModeOn", false);
//            boolean newMode = !isDark;
//
//            // Animate icon rotation
//            ObjectAnimator rotateOut = ObjectAnimator.ofFloat(themeToggle, "rotation", 0f, 90f);
//            ObjectAnimator rotateIn = ObjectAnimator.ofFloat(themeToggle, "rotation", -90f, 0f);
//
//            rotateOut.setDuration(150);
//            rotateIn.setDuration(150);
//
//            rotateOut.addListener(new android.animation.AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(android.animation.Animator animation) {
//                    if (newMode) {
//                        themeToggle.setImageResource(R.drawable.ic_sun);
//                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//                    } else {
//                        themeToggle.setImageResource(R.drawable.ic_moon);
//                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//                    }
//                }
//            });
//
//            AnimatorSet set = new AnimatorSet();
//            set.playSequentially(rotateOut, rotateIn);
//            set.start();
//
//            // Save preference
//            editor.putBoolean("isDarkModeOn", newMode);
//            editor.apply();
//        });

    }

    public void loggingIn(View view) {
        Intent intent = new Intent(MainActivity.this, LoggedIn.class);
        startActivity(intent);

    }
    public void pdfOpen(View view) {
        Intent intent = new Intent(MainActivity.this, PDFOpen.class);
        startActivity(intent);

    }



    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("start");
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("stop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("destroy");
    }

    @Override
    protected void onPause() {
      super.onPause();
        System.out.println("pause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("resume");
    }
}