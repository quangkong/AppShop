package com.example.appshop.activity.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.example.appshop.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class WriteReview extends AppCompatActivity {

    private String shopUid;
    private ImageView imgBack;
    private CircularImageView imgAvt;
    private TextView txtShopName, txtLabel;
    private RatingBar ratingBar;
    private EditText edtReview;
    private FloatingActionButton fabSubmit;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);

        shopUid = getIntent().getStringExtra("shopUid");
        firebaseAuth = FirebaseAuth.getInstance();

        //load my review
        loadMyReview();

        //load info shop
        loadShopInfo();
        mapping();
        onClick();
    }

    //chua nang
    private void onClick(){

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        fabSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputData();
            }
        });
    }

    private void loadShopInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //get data
                String shopName = "" + snapshot.child("shopName").getValue();
                String shopAvt = "" + snapshot.child("profileImage").getValue();

                //set data
                txtShopName.setText(shopName);

                try {
                    Picasso.get().load(shopAvt).placeholder(R.drawable.ic_store_gray).into(imgAvt);
                } catch (Exception e){
                    imgAvt.setImageResource(R.drawable.ic_store_gray);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void inputData() {
        String ratings = "" + ratingBar.getRating();
        String review = edtReview.getText().toString().trim();

        //for time of review
        String timestamp = "" + System.currentTimeMillis();

        //setup data
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", "" + firebaseAuth.getUid());
        hashMap.put("ratings", "" + ratings);
        hashMap.put("review", "" + review);
        hashMap.put("timestamp", "" + timestamp);

        //put to db: DB > Users > ShopUid > Ratings

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Ratings").child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(WriteReview.this, "Review successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(WriteReview.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadMyReview() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Ratings").child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            String uid = "" + snapshot.child("uid").getValue();
                            String ratings = "" + snapshot.child("ratings").getValue();
                            String review = "" + snapshot.child("review").getValue();
                            String timestamp = "" + snapshot.child("timestamp").getValue();

                            //set review
                            float myRating = Float.parseFloat(ratings);
                            ratingBar.setRating(myRating);
                            edtReview.setText(review);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void mapping(){
        imgBack = findViewById(R.id.imgBack);
        imgAvt = findViewById(R.id.imgAvt);
        txtShopName = findViewById(R.id.txtShopName);
        txtLabel = findViewById(R.id.txtLabel);
        ratingBar = findViewById(R.id.ratingBar);
        edtReview = findViewById(R.id.edtReview);
        fabSubmit = findViewById(R.id.fabSubmit);
    }
}