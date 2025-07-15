package com.example.szakdoga;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class MainActivity extends AppCompatActivity {


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
    }

    public void loggingIn(View view) {
        Intent intent = new Intent(MainActivity.this, LoggedIn.class);
        startActivity(intent);

    }
    public void pdfOpen(View view) {
        Intent intent = new Intent(MainActivity.this, PDFOpen.class);
        startActivity(intent);

    }
}