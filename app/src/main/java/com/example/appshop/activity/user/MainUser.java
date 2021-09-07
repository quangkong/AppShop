package com.example.appshop.activity.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.example.appshop.R;
import com.example.appshop.activity.account.Login;
import com.example.appshop.activity.seller.MainSeller;
import com.example.appshop.activity.seller.ProfileEditSallerActivity;
import com.example.appshop.adapter.AdapterOrderUser;
import com.example.appshop.adapter.AdapterShopUser;
import com.example.appshop.model.OrderUser;
import com.example.appshop.model.Shop;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class MainUser extends AppCompatActivity {

    private TextView txtName, txtEmail, txtPhone, txtTabShops, txtTabOrders;
    private ImageView imgPow, imgEdit;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private RelativeLayout toolBarShops, toolBarOrders;
    private EditText edtSearch;
    private RecyclerView rvShops, rvOrder;
    private CircularImageView iconAvt;

    private ArrayList<Shop> shopsList;
    private AdapterShopUser adapterShopUser;

    private ArrayList<OrderUser> listOrderUsers;
    private AdapterOrderUser adapterOrderUser;

    private boolean check = false;

    @Override
    protected void onResume() {
        super.onResume();
        if (check){
            checkUser();
        }
        check = true;
    }

    @Override
     protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main_user);
        mapping();
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Logging out...");
        progressDialog.setCanceledOnTouchOutside(false);
        checkUser();
        showShopsUI();
        onClick();
        //at start show shops ui

    }

    //Chuc nang
    private void onClick(){
        imgPow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //offline
                //goto login
                makeMeOffline();
            }
        });

        imgEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainUser.this, ProfileEditUserActivity.class));
            }
        });

        txtTabShops.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show shops
                showShopsUI();
            }
        });

        txtTabOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show shops
                showOrdersUI();
            }
        });
    }

    private void showShopsUI() {
        //show shops ui, hide orders ui
        toolBarShops.setVisibility(View.VISIBLE);
        toolBarOrders.setVisibility(View.GONE);

        txtTabShops.setTextColor(getResources().getColor(R.color.black));
        txtTabShops.setBackgroundResource(R.drawable.shape_rect04);

        txtTabOrders.setTextColor(getResources().getColor(R.color.white));
        txtTabOrders.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void showOrdersUI() {
        //show orders ui, hide shops ui
        toolBarShops.setVisibility(View.GONE);
        toolBarOrders.setVisibility(View.VISIBLE);

        txtTabOrders.setTextColor(getResources().getColor(R.color.black));
        txtTabOrders.setBackgroundResource(R.drawable.shape_rect04);

        txtTabShops.setTextColor(getResources().getColor(R.color.white));
        txtTabShops.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void makeMeOffline() {
        //after logging in, make user online
        progressDialog.setMessage("Logging out...");

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online", "false");

        //update value to db
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //update successfully
                        firebaseAuth.signOut();
                        checkUser();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(MainUser.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user == null){
            startActivity(new Intent(MainUser.this, Login.class));
            finish();
        }
        else {
            loadMyInfo();
        }
    }

    private void loadMyInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            String name = "" + dataSnapshot.child("name").getValue();
                            String email = "" + dataSnapshot.child("email").getValue();
                            String phone = "" + dataSnapshot.child("phone").getValue();
                            String profileImage = "" + dataSnapshot.child("profileImage").getValue();
                            txtName.setText(name);
                            txtEmail.setText(email);
                            txtPhone.setText(phone);
                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.ic_person_gray).into(iconAvt);
                            } catch (Exception e) {
                                iconAvt.setImageResource(R.drawable.ic_person_gray);
                            }
                            loadShops();
                            loadOrder();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrder() {
        listOrderUsers = new ArrayList<>();
        //get orders
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listOrderUsers.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    String uid = "" + ds.getRef().getKey();
                    DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Orders");
                    reference1.orderByChild("orderBy").equalTo(firebaseAuth.getUid())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot1) {
                                    if (snapshot.exists()){
                                        for (DataSnapshot ds1 : snapshot1.getChildren()){
                                            OrderUser orderUser = ds1.getValue(OrderUser.class);
                                            //add to list
                                            listOrderUsers.add(orderUser);
                                        }
                                        adapterOrderUser = new AdapterOrderUser(MainUser.this, listOrderUsers);
                                        rvOrder.setAdapter(adapterOrderUser);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadShops() {
        //init list
        shopsList = new ArrayList<>();

        DatabaseReference reference =  FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("userType").equalTo("Seller")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        shopsList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()){
                            Shop shop = ds.getValue(Shop.class);
                            shopsList.add(shop);
                        }

                        adapterShopUser = new AdapterShopUser(MainUser.this, shopsList);
                        rvShops.setAdapter(adapterShopUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void mapping() {
        imgPow = findViewById(R.id.imgPow);
        txtName = findViewById(R.id.txtName);
        txtEmail = findViewById(R.id.txtEmail);
        txtPhone = findViewById(R.id.txtPhone);
        txtTabShops = findViewById(R.id.txtTabShops);
        txtTabOrders = findViewById(R.id.txtTabOrders);
        imgEdit = findViewById(R.id.imgEdit);
        toolBarShops = findViewById(R.id.toolBarShops);
        toolBarOrders = findViewById(R.id.toolBarOrders);
        edtSearch = findViewById(R.id.edtSearch);
        rvShops = findViewById(R.id.rvShops);
        iconAvt = findViewById(R.id.iconAvt);
        rvOrder = findViewById(R.id.rvOrder);
    }
}