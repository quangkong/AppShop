package com.example.appshop.activity.seller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.appshop.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;

public class AddPromotionCode extends AppCompatActivity {

    private ImageView imgBack;
    private EditText edtPromoCode, edtPromoDescription, edtPromoPrice
            , edtMinimunOrderPrice;
    private Button btnAdd;
    private TextView txtToolbar, txtExpireDate;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private String promoId;

    private boolean isUpdating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_promotion_code);
        mapping();
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        Intent intent = getIntent();
        if (intent.getStringExtra("promoId") != null){
            promoId = intent.getStringExtra("promoId");
            txtToolbar.setText("Update Promotion Code");
            btnAdd.setText("UPDATE");
            isUpdating = true;

            loadPromotionInfo();
        }
        else {
            txtToolbar.setText("Add Promotion Code");
            btnAdd.setText("ADD");
            isUpdating = false;
        }
        onClick();
    }

    private void loadPromotionInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Promotions").child(promoId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String id = "" + snapshot.child("id").getValue();
                        String timestamp = "" + snapshot.child("timestamp").getValue();
                        String description = "" + snapshot.child("description").getValue();
                        String promoCode = "" + snapshot.child("promoCode").getValue();
                        String promoPrice = "" + snapshot.child("promoPrice").getValue();
                        String minimumOrderPrice = "" + snapshot.child("minimumOrderPrice").getValue();
                        String expireDate = "" + snapshot.child("expireDate").getValue();

                        //set data
                        txtExpireDate.setText(expireDate);
                        edtMinimunOrderPrice.setText(minimumOrderPrice);
                        edtPromoCode.setText(promoCode);
                        edtPromoDescription.setText(description);
                        edtPromoPrice.setText(promoPrice);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    //Chuc nang
    private void onClick(){
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        txtExpireDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataPickDialog();
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCode();
            }
        });
    }
    
    private String description, promoCode, promoPrice, minimumOrderPrice, expireDate;
    private void addCode() {
        //input data
        promoCode = edtPromoCode.getText().toString().trim();
        description = edtPromoDescription.getText().toString().trim();
        promoPrice = edtPromoPrice.getText().toString().trim();
        minimumOrderPrice = edtMinimunOrderPrice.getText().toString().trim();
        expireDate = txtExpireDate.getText().toString().trim();
        
        //validate form data
        if (promoCode.equals("")){
            Toast.makeText(this, "Enter discount code....", Toast.LENGTH_SHORT).show();
            return;
        }
        if (description.equals("")){
            Toast.makeText(this, "Enter description....", Toast.LENGTH_SHORT).show();
            return;
        }
        if (promoPrice.equals("")){
            Toast.makeText(this, "Enter promotion price....", Toast.LENGTH_SHORT).show();
            return;
        }
        if (minimumOrderPrice.equals("")){
            Toast.makeText(this, "Enter minimum order price....", Toast.LENGTH_SHORT).show();
            return;
        }
        if (expireDate.equals("")){
            Toast.makeText(this, "Choose expired date....", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isUpdating){
            updateData();
        }
        else {
            //add
            addData();
        }
    }

    private void updateData() {
        progressDialog.setMessage("Editting promotion code...");
        progressDialog.show();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("description", "" + description);
        hashMap.put("promoCode", "" + promoCode);
        hashMap.put("promoPrice", "" + promoPrice);
        hashMap.put("minimumOrderPrice", "" + minimumOrderPrice);
        hashMap.put("expireDate", "" + expireDate);

        //init db reference Users > Current User > Promotions > PromoID > Promo Data
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Promotions").child(promoId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(AddPromotionCode.this, "Updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(AddPromotionCode.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addData() {
        progressDialog.setMessage("Adding promotion code...");
        progressDialog.show();

        String timestamp = "" + System.currentTimeMillis();

        //setup data add db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", "" + timestamp);
        hashMap.put("timestamp", "" + timestamp);
        hashMap.put("description", "" + description);
        hashMap.put("promoCode", "" + promoCode);
        hashMap.put("promoPrice", "" + promoPrice);
        hashMap.put("minimumOrderPrice", "" + minimumOrderPrice);
        hashMap.put("expireDate", "" + expireDate);

        //init db reference Users > Current User > Promotions > PromoID > Promo Data
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Promotions").child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        clearInput();
                        Toast.makeText(AddPromotionCode.this, "Promotion code added...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddPromotionCode.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void clearInput() {
        edtMinimunOrderPrice.setText("");
        edtPromoPrice.setText("");
        edtPromoDescription.setText("");
        txtExpireDate.setText("");
        edtPromoCode.setText("");
    }

    private void dataPickDialog() {
        //get current date to set on calendar
        Calendar calendar = Calendar.getInstance();
        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH);
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);

        //date pick dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                DecimalFormat format = new DecimalFormat("00");
                String pDay = format.format(dayOfMonth);
                String pMonth = format.format(month);
                String pYear = format.format(year);
                String pDate = pDay + "/" + pMonth + "/" + pYear;
                txtExpireDate.setText(pDate);
            }
        }, mYear, mMonth, mDay);

        //show dialog
        datePickerDialog.show();

        //disble past dates selection on calendar

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
    }

    private void mapping(){
        imgBack = findViewById(R.id.imgBack);
        edtPromoCode = findViewById(R.id.edtPromoCode);
        edtPromoDescription = findViewById(R.id.edtPromoDescription);
        edtPromoPrice = findViewById(R.id.edtPromoPrice);
        edtMinimunOrderPrice = findViewById(R.id.edtMinimunOrderPrice);
        txtExpireDate = findViewById(R.id.txtExpireDate);
        btnAdd = findViewById(R.id.btnAdd);
        txtToolbar = findViewById(R.id.txtToolbar);
    }
}