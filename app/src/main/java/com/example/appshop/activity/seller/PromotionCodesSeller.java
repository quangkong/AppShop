package com.example.appshop.activity.seller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.appshop.R;
import com.example.appshop.adapter.AdapterPromotionSeller;
import com.example.appshop.model.Promotion;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class PromotionCodesSeller extends AppCompatActivity {

    private ImageView imgBack, imgAdd, imgFilter;
    private TextView txtFilter;
    private RecyclerView rvPromotion;

    private ArrayList<Promotion> listPromotions;
    private AdapterPromotionSeller adapterPromotionSeller;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotion_codes_seller);
        mapping();
        firebaseAuth = FirebaseAuth.getInstance();
        loadAllPromotionCodes();
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

        imgAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PromotionCodesSeller.this, AddPromotionCode.class));
            }
        });
        imgFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterDialog();
            }
        });
    }

    private void filterDialog() {
        String[] options = {"All", "Expired", "Not Expired"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filter Promotion Codes")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0){
                            txtFilter.setText("All Promotion Codes");
                            loadAllPromotionCodes();
                        }
                        else if (which == 1){
                            txtFilter.setText("Expired Promotion Codes");
                            loadExpiredPromotionCodes();
                        }
                        else if (which == 2){
                            txtFilter.setText("Not Expired Promotion Codes");
                            loadNotExpiredPromotionCodes();
                        }
                    }
                }).show();
    }

    private void loadAllPromotionCodes(){
        listPromotions = new ArrayList<>();

        //db reference Users > current user > Promotions > code data

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Promotions")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        listPromotions.clear();
                        for (DataSnapshot ds : snapshot.getChildren()){
                            Promotion promotion = ds.getValue(Promotion.class);
                            listPromotions.add(promotion);
                        }

                        adapterPromotionSeller = new AdapterPromotionSeller(PromotionCodesSeller.this, listPromotions);

                        rvPromotion.setAdapter(adapterPromotionSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadExpiredPromotionCodes(){
        //get current date
        DecimalFormat format = new DecimalFormat("00");
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String todayDate = day + "/" + month + "/" + year;

        listPromotions = new ArrayList<>();

        //db reference Users > current user > Promotions > code data

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Promotions")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        listPromotions.clear();
                        for (DataSnapshot ds : snapshot.getChildren()){
                            Promotion promotion = ds.getValue(Promotion.class);

                            String expDate = promotion.getExpireDate();

                            //check date
                            try {
                                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                                Date cur = format.parse(todayDate);
                                Date expireDate = format.parse(expDate);
                                if (expireDate.compareTo(cur) > 0){

                                }
                                else if (expireDate.compareTo(cur) < 0){
                                    listPromotions.add(promotion);
                                }
                                else if (expireDate.compareTo(cur) == 0){

                                }
                            } catch (Exception e){

                            }
                        }

                        adapterPromotionSeller = new AdapterPromotionSeller(PromotionCodesSeller.this, listPromotions);

                        rvPromotion.setAdapter(adapterPromotionSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadNotExpiredPromotionCodes(){
//get current date
        DecimalFormat format = new DecimalFormat("00");
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String todayDate = day + "/" + month + "/" + year;

        listPromotions = new ArrayList<>();

        //db reference Users > current user > Promotions > code data

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Promotions")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        listPromotions.clear();
                        for (DataSnapshot ds : snapshot.getChildren()){
                            Promotion promotion = ds.getValue(Promotion.class);

                            String expDate = promotion.getExpireDate();

                            //check date
                            try {
                                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                                Date cur = format.parse(todayDate);
                                Date expireDate = format.parse(expDate);
                                if (expireDate.compareTo(cur) > 0){
                                    listPromotions.add(promotion);
                                }
                                else if (expireDate.compareTo(cur) < 0){

                                }
                                else if (expireDate.compareTo(cur) == 0){
                                    listPromotions.add(promotion);
                                }
                            } catch (Exception e){

                            }
                        }

                        adapterPromotionSeller = new AdapterPromotionSeller(PromotionCodesSeller.this, listPromotions);

                        rvPromotion.setAdapter(adapterPromotionSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void mapping(){
        imgBack = findViewById(R.id.imgBack);
        imgAdd = findViewById(R.id.imgAdd);
        imgFilter = findViewById(R.id.imgFilter);
        txtFilter = findViewById(R.id.txtFilter);
        rvPromotion = findViewById(R.id.rvPromotion);
    }
}