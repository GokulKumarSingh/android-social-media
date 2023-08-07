package com.example.firechat.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.example.firechat.R;
import com.example.firechat.database.DatabaseHelper;
import com.example.firechat.databinding.ActivitySplashScreenBinding;
import com.example.firechat.model.Message;
import com.example.firechat.model.User;
import com.example.firechat.util.Utils;

public class SplashScreen extends AppCompatActivity {

    ActivitySplashScreenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences preferences = getSharedPreferences("application", MODE_PRIVATE);
        String selfUuid = preferences.getString("self_uuid", null);

        new Handler().postDelayed(() -> {
            if (selfUuid != null || true) {
                Log.d("log9999", "onCreate: " + selfUuid);
                startActivity(new Intent(SplashScreen.this, MainActivity.class));
                finish();
            } else {
                startActivity(new Intent(SplashScreen.this, CreateProfileActivity.class));
                finish();
            }
        }, 2500);
    }
}