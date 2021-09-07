package com.example.appshop.activity.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.example.appshop.R;
import com.example.appshop.adapter.AdapterReview;
import com.example.appshop.model.Review;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ShopReviews extends AppCompatActivity {


    private String shopUid;
    private ImageView imgBack;
    private CircularImageView imgAvt;
    private TextView txtShopName, txtRatings;
    private RatingBar ratingBar;
    private RecyclerView rvReviews;

    private FirebaseAuth firebaseAuth;

    private ArrayList<Review> list;
    private AdapterReview adapterReview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_reviews);
        shopUid = getIntent().getStringExtra("shopUid");
        mapping();
        loadShopDetails();
        loadReviews();
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private float ratingSum = 0;
    private void loadReviews() {

        list = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        ratingSum = 0;

                        for (DataSnapshot ds : snapshot.getChildren()){
                            float rating = Float.parseFloat("" + ds.child("ratings").getValue());
                            ratingSum = ratingSum + rating;

                            Review review = ds.getValue(Review.class);
                            list.add(review);
                        }

                        adapterReview = new AdapterReview(ShopReviews.this, list);
                        rvReviews.setAdapter(adapterReview);

                        long numberOfReviews = snapshot.getChildrenCount();
                        float avgRating = ratingSum/numberOfReviews;

                        txtRatings.setText(String.format("%.2f", avgRating) + " [" + numberOfReviews + "]");
                        ratingBar.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadShopDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String shopName = "" + snapshot.child("shopName").getValue();
                        String image = "" + snapshot.child("profileImage").getValue();

                        txtShopName.setText(shopName);
                        try {
                            Picasso.get().load(image).placeholder(R.drawable.ic_store_gray).into(imgAvt);
                        } catch (Exception e) {
                            imgAvt.setImageResource(R.drawable.ic_store_gray);
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
        txtRatings = findViewById(R.id.txtRatings);
        txtShopName = findViewById(R.id.txtShopName);
        ratingBar = findViewById(R.id.ratingBar);
        rvReviews = findViewById(R.id.rvReviews);
        firebaseAuth = FirebaseAuth.getInstance();
    }
}