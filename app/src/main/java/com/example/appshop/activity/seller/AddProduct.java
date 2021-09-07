package com.example.appshop.activity.seller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.example.appshop.R;
import com.example.appshop.model.Constants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class AddProduct extends AppCompatActivity {

    private ImageView imgBack;
    private EditText edtTitle, edtDescription, edtCategory, edtQuantity
            ,edtDiscountedPrice, edtDiscountNote, edtPrice;
    private CircularImageView iconProduct;
    private SwitchCompat swichDiscount;
    private Button btnAddProduct;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    //permission constants
    private static final int STORAGE_REQUEST_CODE = 100;

    //camera
    private static final int CAMERA_REQUEST_CODE = 300;

    //image pick arrays
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int CAMERA_PICK_GALLERY_CODE = 500;

    //permissions array
    private String[] storagePermissions;

    //image picked uri
    private Uri uri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_add_product);
        mapping();
        onClick();
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //hide edtDiscountPrice, edtDiscountNote
        edtDiscountedPrice.setVisibility(View.GONE);
        edtDiscountNote.setVisibility(View.GONE);
        //showProductsUI();
        checkSwitchDiscount();
    }

    //Chuc nang
    private void onClick(){
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        iconProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showIMagePickDialog();
            }
        });

        btnAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Flow:
                //1) Input data
                //2) Validate data
                //3) Add data to db
                inputData();
            }
        });

        edtCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                diglogCategory();
            }
        });

    }

    private String title, descriotion, catedory, quantity, discountPrice, discountNote, price;
    private boolean discountAvailable = false;
    //input Data
    private void inputData() {

        //1) Input data
        title = edtTitle.getText().toString().trim();
        descriotion = edtDescription.getText().toString().trim();
        catedory = edtCategory.getText().toString().trim();
        quantity = edtQuantity.getText().toString().trim();
        price = edtPrice.getText().toString().trim();
        discountAvailable = swichDiscount.isChecked();

        //2) Validate data
        if(title.equals("")){
            Toast.makeText(this, "Title is requied...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(catedory.equals("")){
            Toast.makeText(this, "Category is requied...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(price.equals("")){
            Toast.makeText(this, "Price is requied...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(discountAvailable){
            discountPrice = edtDiscountedPrice.getText().toString().trim();
            discountNote = edtDiscountNote.getText().toString().trim();
            if(discountPrice.equals("")){
                Toast.makeText(this, "Discount Price is requied...", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        else {
            discountPrice = "0";
            discountNote = "";
        }

        addProduct();
    }

    private void addProduct() {
        //3) Add data to db
        progressDialog.setMessage("Adding Product....");
        progressDialog.show();

        String timestamp = "" + System.currentTimeMillis();

        if(uri == null){
            //upliad without image
            //setup data
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("productId", "" + timestamp);
            hashMap.put("productTitle", "" + title);
            hashMap.put("productDescription", "" + descriotion);
            hashMap.put("productCategory", "" + catedory);
            hashMap.put("productQuantity", "" + quantity);
            hashMap.put("productIcon", "");
            hashMap.put("price", ""+price);
            hashMap.put("discountAvailable", "" + discountAvailable);
            hashMap.put("discountedPrice", ""+discountPrice);
            hashMap.put("discountNote", "" + discountNote);
            hashMap.put("timestamp", "" + timestamp);
            hashMap.put("uid", "" + firebaseAuth.getUid());

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).child("Products").child(timestamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            progressDialog.dismiss();
                            Toast.makeText(AddProduct.this, "Successful", Toast.LENGTH_SHORT).show();
                            clearData();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(AddProduct.this, "" +e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        else {
            //upload with image

            //first upload image to storage
            String filePathAndName = "profile_images/" + "" + timestamp;
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
                                hashMap.put("productId", "" + timestamp);
                                hashMap.put("productTitle", "" + title);
                                hashMap.put("productDescription", "" + descriotion);
                                hashMap.put("productCategory", "" + catedory);
                                hashMap.put("productQuantity", "" + quantity);
                                hashMap.put("productIcon","" + dowloadImageUri);
                                hashMap.put("price", ""+price);
                                hashMap.put("discountAvailable", "" + discountAvailable);
                                hashMap.put("discountedPrice", ""+discountPrice);
                                hashMap.put("discountNote", "" + discountNote);
                                hashMap.put("timestamp", "" + timestamp);
                                hashMap.put("uid", "" + firebaseAuth.getUid());

                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                                reference.child(firebaseAuth.getUid()).child("Products").child(timestamp).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                progressDialog.dismiss();
                                                Toast.makeText(AddProduct.this, "Successful", Toast.LENGTH_SHORT).show();
                                                clearData();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                Toast.makeText(AddProduct.this, "" +e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(AddProduct.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
    }

    private void clearData() {
        //clear data after uploading priduct
        edtPrice.setText("");
        edtDiscountNote.setText("");
        edtDiscountedPrice.setText("");
        edtQuantity.setText("");
        edtCategory.setText("");
        edtDescription.setText("");
        edtDescription.setText("");
        edtTitle.setText("");
        uri = null;
        iconProduct.setImageResource(R.drawable.ic_add_shopping_primary);
    }

    //Check Switch Discount
    private void checkSwitchDiscount(){
        swichDiscount.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    //show edtDiscountPrice, edtDiscountNote
                    edtDiscountedPrice.setVisibility(View.VISIBLE);
                    edtDiscountNote.setVisibility(View.VISIBLE);
                }
                else {
                    //hide edtDiscountPrice, edtDiscountNote
                    edtDiscountedPrice.setVisibility(View.GONE);
                    edtDiscountNote.setVisibility(View.GONE);
                }
            }
        });
    }

    //select category
    private void diglogCategory() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Product Category")
                .setItems(Constants.productCategories, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //get picked category
                        String category = Constants.productCategories[which];

                        //set pick category
                        edtCategory.setText(category);
                    }
                }).show();
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
                            if(checkCameraPermission()) pickFromCamera(); //bo suu tap - permisson granted
                            else requestCameraPermission(); //permission not granted, request
                        }
                        else {
                            //gallery
                            if(checkStoragePermission()) showImagePickFromGallety(); //bo suu tap - permisson granted
                            else requestStoragePermission(); //permission not granted, request
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
            iconProduct.setImageURI(uri);
        }
        else if(requestCode == CAMERA_PICK_GALLERY_CODE && resultCode == RESULT_OK){
            iconProduct.setImageURI(uri);
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
        edtTitle = findViewById(R.id.edtTitle);
        edtDescription = findViewById(R.id.edtDescription);
        edtCategory = findViewById(R.id.edtCategory);
        edtQuantity = findViewById(R.id.edtQuantity);
        edtPrice = findViewById(R.id.edtPrice);
        edtDiscountedPrice = findViewById(R.id.edtDiscountedPrice);
        edtDiscountNote = findViewById(R.id.edtDiscountNote);
        iconProduct = findViewById(R.id.iconProduct);
        swichDiscount = findViewById(R.id.swichDiscount);
        btnAddProduct = findViewById(R.id.btnAddProduct);
    }
}