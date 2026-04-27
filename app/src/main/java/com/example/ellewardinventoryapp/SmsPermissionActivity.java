package com.example.ellewardinventoryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * SmsPermissionActivity handles requesting SMS permission from the user.
 * The user can grant permission for low stock alerts or skip it entirely.
 * The app continues to function normally regardless of the user's choice.
 */
public class SmsPermissionActivity extends AppCompatActivity {

    // UI elements
    private Button buttonAllowSms;
    private Button buttonDenySms;
    private Button buttonContinueToApp;
    private TextView textSmsPermissionStatus;
    
    // Permission request code
    private static final int SMS_PERMISSION_REQUEST_CODE = 100;
    
    // SharedPreferences key for tracking permission state
    private static final String PREFS_NAME = "TagTalkPrefs";
    private static final String KEY_SMS_PERMISSION_ASKED = "sms_permission_asked";
    
    // User information
    private int userId;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_permission);
        
        // Get user information from intent
        userId = getIntent().getIntExtra("USER_ID", -1);
        username = getIntent().getStringExtra("USERNAME");
        
        // Initialize UI elements
        buttonAllowSms = findViewById(R.id.buttonAllowSms);
        buttonDenySms = findViewById(R.id.buttonDenySms);
        buttonContinueToApp = findViewById(R.id.buttonContinueToApp);
        textSmsPermissionStatus = findViewById(R.id.textSmsPermissionStatus);
        
        // Check if permission was already asked
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean permissionAsked = prefs.getBoolean(KEY_SMS_PERMISSION_ASKED, false);
        
        // If already asked, skip this screen
        if (permissionAsked) {
            navigateBackToInventory();
            return;
        }
        
        // Set up allow button to request SMS permission
        buttonAllowSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestSmsPermission();
            }
        });
        
        // Set up deny button to skip permission request
        buttonDenySms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePermissionDenied();
            }
        });
        
        // Set up continue button to go back to inventory
        buttonContinueToApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateBackToInventory();
            }
        });
    }

    /**
     * Requests SMS permission from the user
     */
    private void requestSmsPermission() {
        // Check if permission is already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            handlePermissionGranted();
        } else {
            // Request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    SMS_PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * Handles the result of the permission request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                handlePermissionGranted();
            } else {
                // Permission denied
                handlePermissionDenied();
            }
        }
    }

    /**
     * Called when SMS permission is granted
     */
    private void handlePermissionGranted() {
        // Save that permission was asked
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_SMS_PERMISSION_ASKED, true);
        editor.apply();
        
        // Update UI to show success
        textSmsPermissionStatus.setText("SMS alerts enabled! You'll get notified when items run low.");
        textSmsPermissionStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        
        // Hide allow/deny buttons, show continue button
        buttonAllowSms.setVisibility(View.GONE);
        buttonDenySms.setVisibility(View.GONE);
        buttonContinueToApp.setVisibility(View.VISIBLE);
    }

    /**
     * Called when user skips or denies SMS permission
     */
    private void handlePermissionDenied() {
        // Save that permission was asked
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_SMS_PERMISSION_ASKED, true);
        editor.apply();
        
        // Update UI to show app will work without SMS
        textSmsPermissionStatus.setText("No problem! The app works fine without SMS alerts.");
        textSmsPermissionStatus.setTextColor(getResources().getColor(android.R.color.darker_gray));
        
        // Hide allow/deny buttons, show continue button
        buttonAllowSms.setVisibility(View.GONE);
        buttonDenySms.setVisibility(View.GONE);
        buttonContinueToApp.setVisibility(View.VISIBLE);
    }

    /**
     * Navigates back to the inventory screen
     */
    private void navigateBackToInventory() {
        // Close this activity and return to inventory
        finish();
    }
}
