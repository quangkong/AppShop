package com.example.appshop.activity.user;

import androidx.annotation.NonNull;
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
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.example.appshop.R;
import com.example.appshop.activity.account.Login;
import com.example.appshop.activity.account.SettingNotifications;
import com.example.appshop.activity.seller.ProfileEditSallerActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class ProfileEditUserActivity extends AppCompatActivity {

    private ImageView imgBack, imgSettings;
    private EditText edtFullName, edtPhone, edtAddress;
    private Button btnEdit;
    private CircularImageView iconAvt;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private Uri uri = null;

    private String fullName, phone, address;

    //permission constants
    private static final int STORAGE_REQUEST_CODE = 100;

    //image pick constants
    private static final int IMAGE_REQUEST_CODE = 200;

    //camera
    private static final int CAMERA_REQUEST_CODE = 300;

    //image pick arrays
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int CAMERA_PICK_GALLERY_CODE = 500;

    private String[] storagePermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_profile_edit_user);
        mapping();
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Logging out...");
        progressDialog.setCanceledOnTouchOutside(false);
        onClick();
        loadMyInfo();
    }



    //Chuc nang
    private void onClick(){
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btnEdit.setOnClickListener(new View.OnClickListener() {
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

        imgSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileEditUserActivity.this, SettingNotifications.class));
            }
        });
    }

    //check user
    private void loadMyInfo(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds : snapshot.getChildren()){
                            String address = "" + ds.child("address").getValue();
                            String name = "" + ds.child("name").getValue();
                            String phone = "" + ds.child("phone").getValue();
                            String profileImage = "" + ds.child("profileImage").getValue();
                            edtFullName.setText(name);
                            edtAddress.setText(address);
                            edtPhone.setText(phone);
                            try {
                                Picasso.get().load(profileImage)
                                        .placeholder(R.drawable.ic_store_gray).into(iconAvt);
                            }
                            catch (Exception e){
                                iconAvt.setImageResource(R.drawable.ic_person_gray);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    //lay du lieu tu man hinh
    private void inputData() {
        //private String email, fullName, phone, address, password, comfirmPassword;
        fullName = edtFullName.getText().toString().trim();
        phone = edtPhone.getText().toString().trim();
        address = edtAddress.getText().toString().trim();
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
        editAccount();
    }

    private void editAccount(){
        progressDialog.setMessage("Creating Account");
        progressDialog.show();

        //update account
        progressDialog.setMessage("Saving account info...");


        if(uri == null){
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("uid", ""+firebaseAuth.getUid());
            hashMap.put("name", ""+fullName);
            hashMap.put("phone", ""+phone);
            hashMap.put("address", ""+address);
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            progressDialog.dismiss();
                            startActivity(new Intent(ProfileEditUserActivity.this, Login.class));
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            progressDialog.dismiss();
                            startActivity(new Intent(ProfileEditUserActivity.this, Login.class));
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
                                hashMap.put("name", ""+fullName);
                                hashMap.put("phone", ""+phone);
                                hashMap.put("address", ""+address);
                                hashMap.put("profileImage", "" + dowloadImageUri);  //url of uploaded image
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                                reference.child(firebaseAuth.getUid()).updateChildren(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                progressDialog.dismiss();
                                                Toast.makeText(ProfileEditUserActivity.this, "Successful...", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(Exception e) {
                                                progressDialog.dismiss();
                                                Toast.makeText(ProfileEditUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(ProfileEditUserActivity.this, "Email đã được sử dụng", Toast.LENGTH_SHORT).show();
                        }
                    });

        }
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
    private void showImagePickFromGallety() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        //intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

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

    private void mapping(){
        imgBack = findViewById(R.id.imgBack);
        btnEdit = findViewById(R.id.btnEdit);
        iconAvt = findViewById(R.id.iconAvt);
        edtFullName = findViewById(R.id.edtFullName);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);
        imgSettings = findViewById(R.id.imgSettings);
    }
}