package com.example.appshop.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.example.appshop.R;
import com.example.appshop.activity.user.ShopDetailsUser;
import com.example.appshop.model.Shop;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterShopUser extends RecyclerView.Adapter<AdapterShopUser.HolderShop>{

    private Context context;
    public ArrayList<Shop> list;

    public AdapterShopUser(Context context, ArrayList<Shop> list) {
        this.context = context;
        this.list = list;
    }



    @NonNull
    @Override
    public HolderShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_shop, parent, false);
        return new HolderShop(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterShopUser.HolderShop holder, int position) {
        Shop shop = list.get(position);

        String accType = shop.getUserType();
        String address = shop.getAddress();
        String deliveryFee = shop.getDeliveryFee();
        String email = shop.getEmail();
        String name = shop.getName();
        String online = shop.getOnline();
        String password = shop.getPassword();
        String phone = shop.getPhone();
        String profileImage = shop.getProfileImage();
        String shopName = shop.getShopName();
        String shopOpen = shop.getShopOpen();
        String timestamp = shop.getTimestamp();
        String uid = shop.getUid();

        loadReviews(uid, holder);

        //set data
        holder.txtShopName.setText(shopName);
        holder.txtPhone.setText(phone);
        holder.txtAddress.setText(address);

        if(online.equals("true")){
            holder.imgOnline.setVisibility(View.VISIBLE);
        }
        else {
            holder.imgOnline.setVisibility(View.GONE);
        }

        if(shopOpen.equals("true")){
            holder.txtShopClose.setVisibility(View.GONE);
        }
        else {
            holder.txtShopClose.setVisibility(View.VISIBLE);
        }

        try {
            Picasso.get().load(profileImage).placeholder(R.drawable.ic_store_gray).into(holder.iconAvt);
        } catch (Exception e) {
            holder.iconAvt.setImageResource(R.drawable.ic_store_gray);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ShopDetailsUser.class);
                intent.putExtra("shopUid", uid);
                context.startActivity(intent);
            }
        });
    }

    private float ratingSum = 0;
    private void loadReviews(String shopUid, HolderShop holder) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ratingSum = 0;

                        for (DataSnapshot ds : snapshot.getChildren()){
                            float rating = Float.parseFloat("" + ds.child("ratings").getValue());
                            ratingSum = ratingSum + rating;
                        }
                        long numberOfReviews = snapshot.getChildrenCount();
                        float avgRating = ratingSum/numberOfReviews;

                        holder.ratingBar.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class HolderShop extends RecyclerView.ViewHolder{

        private ImageView imgOnline;
        private CircularImageView iconAvt;
        private TextView txtShopClose, txtShopName, txtPhone, txtAddress;
        private RatingBar ratingBar;


        public HolderShop(@NonNull View itemView) {
            super(itemView);

            //init uid views
            imgOnline = itemView.findViewById(R.id.imgOnline);
            iconAvt = itemView.findViewById(R.id.iconAvt);
            txtShopClose = itemView.findViewById(R.id.txtShopClose);
            txtShopName = itemView.findViewById(R.id.txtShopName);
            txtPhone = itemView.findViewById(R.id.txtPhone);
            txtAddress = itemView.findViewById(R.id.txtAddress);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }
}
