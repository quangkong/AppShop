package com.example.appshop.activity.seller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.example.appshop.R;
import com.example.appshop.activity.account.Login;
import com.example.appshop.activity.account.SettingNotifications;
import com.example.appshop.activity.user.ShopReviews;
import com.example.appshop.adapter.AdapterOrderSeller;
import com.example.appshop.adapter.AdapterProductSeller;
import com.example.appshop.model.Constants;
import com.example.appshop.model.OrderSeller;
import com.example.appshop.model.Product;
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

public class MainSeller extends AppCompatActivity {

    private TextView txtName, txtShopName, txtEmail, txtShowAll
            , txtTabProducts, txtTabOrders, txtFilterOrders;
    private ImageView imgMore, imgFilterProducts
            , imgFilterOrders;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private EditText edtSearch;
    private RecyclerView rvProducts, rvOrders;
    CircularImageView iconAvt;
    private RelativeLayout toolBarProducts, toolBarOrders;

    private ArrayList<Product> listProducts;
    private AdapterProductSeller adapterProductSeller;

    private ArrayList<OrderSeller> listOrders;
    private AdapterOrderSeller adapterOrderSeller;

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
        setContentView(R.layout.activity_main_seller);
        mapping();
        onClick();
        showProductsUI();
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wite");
        progressDialog.setCanceledOnTouchOutside(false);
        checkUser();
        loadAllProducts();
        loadAllOrders();
    }

    //Chuc nang
    private void onClick(){

        txtTabProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //load products
                showProductsUI();
            }
        });

        txtTabOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //load order
                showOrdersUI();
            }
        });

        imgFilterProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                diglogFilter();
            }
        });

        //search
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterProductSeller.getFilter().filter(s);
                } catch (Exception e) {
                    Toast.makeText(MainSeller.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        imgFilterOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                diglogFilterOrders();
            }
        });

        PopupMenu popupMenu = new PopupMenu(MainSeller.this, imgMore);
        popupMenu.getMenu().add("Add Product");
        popupMenu.getMenu().add("Promotion Codes");
        popupMenu.getMenu().add("Edit Profile");
        popupMenu.getMenu().add("Reviews");
        popupMenu.getMenu().add("Settings");
        popupMenu.getMenu().add("Logout");
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getTitle() == "Add Product"){
                    startActivity(new Intent(MainSeller.this, AddProduct.class));
                }
                else  if (item.getTitle() == "Promotion Codes"){
                    //open reviews activity
                    Intent intent = new Intent(MainSeller.this, PromotionCodesSeller.class);
                    intent.putExtra("shopUid", firebaseAuth.getUid());
                    startActivity(intent);
                }
                else  if (item.getTitle() == "Edit Profile"){
                    startActivity(new Intent(MainSeller.this, ProfileEditSallerActivity.class));
                }
                else  if (item.getTitle() == "Reviews"){
                    //open reviews activity
                    Intent intent = new Intent(MainSeller.this, ShopReviews.class);
                    intent.putExtra("shopUid", firebaseAuth.getUid());
                    startActivity(intent);
                }
                else  if (item.getTitle() == "Settings"){
                    startActivity(new Intent(MainSeller.this, SettingNotifications.class));
                }
                else  if (item.getTitle() == "Logout"){
                    makeMeOffline();
                }
                return true;
            }
        });

        imgMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.show();
            }
        });
    }

    private void diglogFilterOrders() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainSeller.this);
        builder.setTitle("Choose Category: ")
                .setItems(Constants.statusSeller, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //get selected item
                        String selected = Constants.statusSeller[which];
                        if(selected.equals("All")){
                            txtFilterOrders.setText("Showing All Orders");
                            if(adapterOrderSeller != null){
                                adapterOrderSeller.getFilter().filter("");
                            }
                        }
                        else {
                            txtFilterOrders.setText("Showing " + selected + " Orders");
                            if(adapterOrderSeller != null){
                                adapterOrderSeller.getFilter().filter(selected);
                            }
                        }
                    }
                }).show();
    }

    private void loadAllOrders() {
        listOrders = new ArrayList<>();

        //get all products
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Orders")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //before getting reset list
                        listOrders.clear();
                        for(DataSnapshot ds : snapshot.getChildren()){
                            OrderSeller orderSeller = ds.getValue(OrderSeller.class);
                            listOrders.add(orderSeller);
                        }
                        //setup adapter
                        adapterOrderSeller = new AdapterOrderSeller(MainSeller.this, listOrders);
                        //set adapter
                        rvOrders.setAdapter(adapterOrderSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void showProductsUI() {
        //show products ui and hide orders ui
        toolBarOrders.setVisibility(View.GONE);
        toolBarProducts.setVisibility(View.VISIBLE);

        txtTabProducts.setTextColor(getResources().getColor(R.color.black));
        txtTabProducts.setBackgroundResource(R.drawable.shape_rect04);

        txtTabOrders.setTextColor(getResources().getColor(R.color.white));
        txtTabOrders.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void showOrdersUI() {
        //show orders ui and hide products ui
        toolBarOrders.setVisibility(View.VISIBLE);
        toolBarProducts.setVisibility(View.GONE);

        txtTabOrders.setTextColor(getResources().getColor(R.color.black));
        txtTabOrders.setBackgroundResource(R.drawable.shape_rect04);

        txtTabProducts.setTextColor(getResources().getColor(R.color.white));
        txtTabProducts.setBackgroundColor(getResources().getColor(android.R.color.transparent));

    }

    private void diglogFilter(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainSeller.this);
        builder.setTitle("Choose Category: ")
                .setItems(Constants.productAll, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //get selected item
                        String selected = Constants.productAll[which];
                        txtShowAll.setText(selected);
                        if(selected.equals("All")){
                            loadAllProducts();
                        }
                        else {
                            loadFilterProduct(selected);
                        }
                    }
                }).show();
    }

    private void loadFilterProduct(String selected) {
        listProducts = new ArrayList<>();

        //get all products
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //before getting reset list
                        listProducts.clear();
                        for(DataSnapshot ds : snapshot.getChildren()){

                            String productCategory = ""+ds.child("productCategory").getValue();

                            //if selected category matches product category then add in list
                            if (selected.equals(productCategory)){
                                Product product = ds.getValue(Product.class);
                                listProducts.add(product);
                            }
                        }

                        //setup adapter
                        adapterProductSeller = new AdapterProductSeller(MainSeller.this, listProducts);
                        //set adapter
                        rvProducts.setAdapter(adapterProductSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadAllProducts() {
        listProducts = new ArrayList<>();

        //get all products
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //before getting reset list
                        listProducts.clear();
                        for(DataSnapshot ds : snapshot.getChildren()){
                            Product product = ds.getValue(Product.class);
                            listProducts.add(product);
                        }

                        //setup adapter
                        adapterProductSeller = new AdapterProductSeller(MainSeller.this, listProducts);
                        //set adapter
                        rvProducts.setAdapter(adapterProductSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    //off line
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
                        Toast.makeText(MainSeller.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user == null){
            startActivity(new Intent(MainSeller.this, Login.class));
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
                            //get data
                            String name = "" + dataSnapshot.child("name").getValue();
                            String shopName = "" + dataSnapshot.child("shopName").getValue();
                            String email = "" + dataSnapshot.child("email").getValue();
                            String img = "" + dataSnapshot.child("profileImage").getValue();

                            txtName.setText(name);
                            txtShopName.setText(shopName);
                            txtEmail.setText(email);
                            try{
                                Picasso.get().load(img).placeholder(R.drawable.ic_store_gray).into(iconAvt);
                            }
                            catch (Exception e){
                                iconAvt.setImageResource(R.drawable.ic_store_gray);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MainSeller.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void mapping() {
        imgMore = findViewById(R.id.imgMore);
        txtName = findViewById(R.id.txtName);
        txtShopName = findViewById(R.id.txtShopName);
        txtEmail = findViewById(R.id.txtEmail);
        iconAvt = findViewById(R.id.iconAvt);
        edtSearch = findViewById(R.id.edtSearch);
        imgFilterProducts = findViewById(R.id.imgFilterProducts);
        txtShowAll = findViewById(R.id.txtShowAll);
        rvProducts = findViewById(R.id.rvProducts);
        txtTabProducts = findViewById(R.id.txtTabProducts);
        txtTabOrders = findViewById(R.id.txtTabOrders);
        toolBarOrders = findViewById(R.id.toolBarOrders);
        toolBarProducts = findViewById(R.id.toolBarProducts);
        imgFilterOrders = findViewById(R.id.imgFilterOrders);
        txtFilterOrders = findViewById(R.id.txtFilterOrders);
        rvOrders = findViewById(R.id.rvOrders);
    }
}