package com.example.szakdoga;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

public class LoggedIn extends AppCompatActivity {

    EditText searchEditText;
    LinearLayout resultContaier;

    class Asd {
        String name, ip, type;
        Asd(String name, String ip, String type) {
            this.name = name;
            this.ip = ip;
            this.type = type;
        }
    }

    List<Asd> allRouters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_logged_in);

        searchEditText = findViewById(R.id.searchEditText);
        resultContaier = findViewById(R.id.resultContainer);

        allRouters = Arrays.asList(
                new Asd("TP-Link AX3000", "192.168.0.1", "WiFi 6"),
                new Asd("Asus RT-AC68U", "192.168.1.1", "AC1900"),
                new Asd("Netgear Nighthawk", "192.168.0.254", "Gaming"),
                new Asd("Tenda AC10U", "192.168.8.1", "Budget"),
                new Asd("keress ram more", "most azonnal", "Budget")
        );

        updateResults("");


        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateResults(s.toString());
            }

        });

    }

    //dinamikus kereso
    //egyelore megadott adatokbol mukszik
    @SuppressLint("SetTextI18n")
    private void updateResults(String query) {
        resultContaier.removeAllViews();

        for(Asd asd : allRouters) {
            if (asd.name.toLowerCase().contains(query.toLowerCase())) {
                TextView tv = new TextView(this);
                tv.setText("Nev: " + asd.name + "\nIP:" + asd.ip + "\nTipus: " + asd.type);
                tv.setPadding(30,30,30,30);
                LinearLayout.LayoutParams vau = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                tv.setTextSize(25);

                tv.setBackgroundResource(R.drawable.rounded_edittext);
                tv.setTextColor(android.graphics.Color.BLACK);
                tv.setElevation(1);
                vau.setMargins(0,0,0,30);
                tv.setLayoutParams(vau);
                resultContaier.addView(tv);

            }
        }
    }



}

