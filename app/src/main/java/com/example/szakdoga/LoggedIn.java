package com.example.szakdoga;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class LoggedIn extends AppCompatActivity {

    EditText searchEditText;
    LinearLayout resultContaier;

    static class Asd {
        String name, ip, type, color;
        int mekkoraafasza;
        Asd(String name, String ip, String type, String color, int mekkoraafasza) {
            this.name = name;
            this.ip = ip;
            this.type = type;
            this.color = color;
            this.mekkoraafasza = mekkoraafasza;
        }
    }

    List<Asd> allRouters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_logged_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        searchEditText = findViewById(R.id.searchEditText);
        resultContaier = findViewById(R.id.resultContainer);



        allRouters = Arrays.asList(
                new Asd("TP-Link AX3000", "192.168.0.1", "WiFi 6", "zold", 14),
                new Asd("Asus RT-AC68U", "192.168.1.1", "AC1900", "fekete", 20),
                new Asd("Netgear Nighthawk", "192.168.0.254", "Gaming", "kek", 2),
                new Asd("Tenda AC10U", "192.168.8.1", "Budget", "rozsaszin", 30),
                new Asd("keress ram more", "most azonnal", "Budget", "fekete", 1)
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
                Button btn = new Button(this);
                btn.setText("Nev: " + asd.name + "\nIP:" + asd.ip + "\nTipus: " + asd.type);
                btn.setPadding(30,30,30,30);
                LinearLayout.LayoutParams vau = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                btn.setTextSize(25);
                btn.setBackgroundResource(R.drawable.rounded_edittext);
                btn.setTextAlignment(ViewGroup.TEXT_ALIGNMENT_VIEW_START);
                btn.setTextColor(android.graphics.Color.BLACK);
                btn.setElevation(1);
                vau.setMargins(0,0,0,30);
                btn.setLayoutParams(vau);
                resultContaier.addView(btn);

                btn.setOnClickListener(v -> showDialog(asd));
            }
        }
    }
    @SuppressLint("SetTextI18n")
    private void showDialog(Asd asd) {

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.detailed_info);

        TextView textView = dialog.findViewById(R.id.textView);

        textView.setTextSize(25);
        textView.setText("Nev: " + asd.name + "\nIP:" + asd.ip + "\nTipus: " + asd.type  + "\nSzin: " + asd.color +  "\nMeret: " + asd.mekkoraafasza);


        ImageView dialog_close = dialog.findViewById(R.id.btn_close);

        dialog_close.setOnClickListener(v -> dialog.dismiss());

        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.bg_window);


        dialog.show();
    }


}

