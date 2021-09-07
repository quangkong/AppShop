package com.example.appshop.activity.user;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.example.appshop.R;
import com.example.appshop.activity.account.Login;
import com.example.appshop.activity.seller.RegisterSeller;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class RegisterUser extends AppCompatActivity{

    private ImageView imgBack, iconKey;
    private Button btnRegiter;
    private CircularImageView iconAvt;
    private EditText edtEmail, edtFullName, edtPhone,
            edtAddress, edtPassword, edtComfirmPassword;
    private TextView txtRegisterSeller;
    private Uri uri = null;
    private String email, fullName, phone, address, password, comfirmPassword;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    //permission constants
    private static final int STORAGE_REQUEST_CODE = 100;

    //image pick constants
    private static final int IMAGE_REQUEST_CODE = 200;

    //camera
    private static final int CAMERA_REQUEST_CODE = 300;

    //image pick arrays
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int CAMERA_PICK_GALLERY_CODE = 500;

    //permissions array
    private String[] storagePermissions;
    private String[] cameraPermissions;
    //image picked uri

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_register_user);
        mapping();
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        onClick();
    }

    //chuc nang
    private void onClick(){

        //Back activity
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //Button Regiter
        btnRegiter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputData();
            }
        });

        iconAvt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showIMagePickDialog();
            }
        });

        txtRegisterSeller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterUser.this, RegisterSeller.class));
            }
        });
    }

    //lua chon camera hay album
    private void showIMagePickDialog() {
        //options to display in dialog
        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            //camera
                            if(checkCameraPermission()) pickFromCamera(); //bo suu tap
                            else requestCameraPermission();
                        }
                        else {
                            //gallery
                            if(checkStoragePermission()) showImagePickFromGallety(); //bo suu tap
                            else requestStoragePermission();
                        }
                    }
                })
                .show();
    }

    //Gallery
    private boolean checkStoragePermission(){
        boolean res = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return res;
    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    //camera
    private void pickFromCamera(){
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_Image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Image Description");

        uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, CAMERA_PICK_GALLERY_CODE);
    }

    private boolean checkCameraPermission(){
        boolean res = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean res1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return res && res1;
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this, storagePermissions, CAMERA_REQUEST_CODE);
    }

    //lay du lieu tu man hinh
    private void inputData() {
        //private String email, fullName, phone, address, password, comfirmPassword;
        fullName = edtFullName.getText().toString().trim();
        phone = edtPhone.getText().toString().trim();
        address = edtAddress.getText().toString().trim();
        email = edtEmail.getText().toString().trim();
        password = edtPassword.getText().toString().trim();
        comfirmPassword = edtComfirmPassword.getText().toString().trim();
        if(fullName.equals("")){
            Toast.makeText(this, "Enter name...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(phone.equals("")){
            Toast.makeText(this, "Enter phone...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(address.equals("")){
            Toast.makeText(this, "Enter address...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Enter email...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(password.length() < 6){
            Toast.makeText(this, "Password must be atleast 6...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!password.equals(comfirmPassword)){
            Toast.makeText(this, "Password doesn't match...", Toast.LENGTH_SHORT).show();
            return;
        }
        createAccount();
    }

    //doc ảnh
    private void createAccount(){
        progressDialog.setMessage("Creating Account");
        progressDialog.show();

        //create account
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        saveAccountFireBase();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        //failed creating account
                        progressDialog.dismiss();
                        Toast.makeText(RegisterUser.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveAccountFireBase() {
        progressDialog.setMessage("Saving account info...");

        String timestamp = "" + System.currentTimeMillis();

        if(uri == null){
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("uid", ""+firebaseAuth.getUid());
            hashMap.put("email", ""+email);
            hashMap.put("name", ""+fullName);
            hashMap.put("phone", ""+phone);
            hashMap.put("address", ""+address);
            hashMap.put("password", ""+password);
            hashMap.put("timestamp", ""+timestamp);
            hashMap.put("online", "true");
            hashMap.put("userType", "User");
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            progressDialog.dismiss();
                            startActivity(new Intent(RegisterUser.this, Login.class));
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            progressDialog.dismiss();
                            startActivity(new Intent(RegisterUser.this, Login.class));
                            finish();
                        }
                    });
        }
        else {
            //save info with image

            //name and path of image
            String filePathAndName = "profile_images/" + firebaseAuth.getUid();
            //upload image
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        Uri dowloadImageUri = uriTask.getResult();
                        if(uriTask.isSuccessful()){
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("uid", ""+firebaseAuth.getUid());
                            hashMap.put("email", ""+email);
                            hashMap.put("name", ""+fullName);
                            hashMap.put("phone", ""+phone);
                            hashMap.put("address", ""+address);
                            hashMap.put("password", ""+password);
                            hashMap.put("timestamp", ""+timestamp);
                            hashMap.put("online", "true");
                            hashMap.put("userType", "User");
                            hashMap.put("profileImage", "" + dowloadImageUri);  //url of uploaded image
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                            reference.child(firebaseAuth.getUid()).setValue(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            progressDialog.dismiss();
                                            startActivity(new Intent(RegisterUser.this, Login.class));
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(Exception e) {
                                            progressDialog.dismiss();
                                            Toast.makeText(RegisterUser.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterUser.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK && requestCode == IMAGE_PICK_GALLERY_CODE){
            uri = data.getData();
            iconAvt.setImageURI(uri);
        }
        else if(requestCode == CAMERA_PICK_GALLERY_CODE && resultCode == RESULT_OK){
            iconAvt.setImageURI(uri);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        showImagePickFromGallety();
                    } else {
                        Toast.makeText(this, "Storage permission is necessary", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            case CAMERA_REQUEST_CODE:{
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        pickFromCamera();
                    } else {
                        Toast.makeText(this, "Camera permission is necessary", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //show album
    private void showImagePickFromGallety() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        //intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    //anh xa
    private void mapping(){
        imgBack = findViewById(R.id.imgBack);
        iconKey = findViewById(R.id.iconKey);
        btnRegiter = findViewById(R.id.btnRegiter);
        iconAvt = findViewById(R.id.iconAvt);
        edtEmail = findViewById(R.id.edtEmail);
        edtFullName = findViewById(R.id.edtFullName);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);
        edtPassword = findViewById(R.id.edtPassword);
        edtComfirmPassword = findViewById(R.id.edtComfirmPassword);
        txtRegisterSeller = findViewById(R.id.txtRegisterSeller);
    }
}