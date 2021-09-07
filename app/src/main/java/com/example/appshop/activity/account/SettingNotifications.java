package com.example.appshop.activity.account;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.appshop.R;
import com.example.appshop.model.Constants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

public class SettingNotifications extends AppCompatActivity {

    private SwitchCompat scSettings;
    private TextView txtNotificationStatus;
    private ImageView imgBack;

    private static final String enabledMessage = "Notifications are enabled";
    private static final String disabledMessage = "Notifications are disabled";

    private FirebaseAuth firebaseAuth;

    private boolean check = false;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_notifications);
        mapping();
        firebaseAuth = FirebaseAuth.getInstance();

        //init shared perferences
        sharedPreferences = getSharedPreferences("SETTINGS_SP", MODE_PRIVATE);

        //check last selected option
        check = sharedPreferences.getBoolean("FCM_ENABLED", false);
        scSettings.setChecked(check);
        if (check){
            txtNotificationStatus.setText(enabledMessage);
        }
        else {
            txtNotificationStatus.setText(disabledMessage);
        }
        onClick();
    }

    //Chuc nang
    private void onClick(){
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        scSettings.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    subscribeToTopic();
                }
                else {
                    unSubscribeToTopic();
                }
            }
        });
    }

    private void mapping(){
        imgBack = findViewById(R.id.imgBack);
        scSettings = findViewById(R.id.scSettings);
        txtNotificationStatus = findViewById(R.id.txtNotificationStatus);
    }

    private void subscribeToTopic(){
        FirebaseMessaging.getInstance().subscribeToTopic(Constants.FCM_TOPIC)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //save setting
                        editor = sharedPreferences.edit();
                        editor.putBoolean("FCM_ENABLED", true);
                        editor.apply();

                        Toast.makeText(SettingNotifications.this, ""+enabledMessage, Toast.LENGTH_SHORT).show();
                        txtNotificationStatus.setText(enabledMessage);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SettingNotifications.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void unSubscribeToTopic(){
        FirebaseMessaging.getInstance().unsubscribeFromTopic(Constants.FCM_TOPIC)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //save setting
                        editor = sharedPreferences.edit();
                        editor.putBoolean("FCM_ENABLED", false);
                        editor.apply();
                        Toast.makeText(SettingNotifications.this, ""+disabledMessage, Toast.LENGTH_SHORT).show();
                        txtNotificationStatus.setText(disabledMessage);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SettingNotifications.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}