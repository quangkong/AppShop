package com.example.appshop.activity.account;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.appshop.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {


    private ImageView iconLock, imgBack;
    private EditText edtEmail;
    private Button btnRecover;
    private String email;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_forgot_password);
        mapping();
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
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

        btnRecover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recoverPassword();
            }
        });
    }

    private void recoverPassword() {
        email = edtEmail.getText().toString().trim();
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Invalid email...", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Sending instructions to reset password");
        progressDialog.show();

        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //instructions sent
                        progressDialog.dismiss();
                        Toast.makeText(ForgotPassword.this, "Password reset instructions sent to your email...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(ForgotPassword.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //Anh xa
    private void mapping(){
        iconLock = findViewById(R.id.iconLock);
        edtEmail = findViewById(R.id.edtEmail);
        btnRecover = findViewById(R.id.btnRecover);
        imgBack = findViewById(R.id.imgBack);
    }
}