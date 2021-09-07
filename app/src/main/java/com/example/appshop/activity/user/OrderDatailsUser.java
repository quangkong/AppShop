package com.example.appshop.activity.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.appshop.R;
import com.example.appshop.adapter.AdapterOrderedItem;
import com.example.appshop.model.OrderedItemUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class OrderDatailsUser extends AppCompatActivity {

    private String orderTo, orderId;

    private ImageView imgBack, imgReview;
    private TextView txtOrderId, txtDate, txtOrderStatus
            , txtShopName, txtTotalItems, txtAmount, txtAddress;
    private RecyclerView rvOrderItem;

    private FirebaseAuth firebaseAuth;

    private ArrayList<OrderedItemUser> list;
    private AdapterOrderedItem adapterOrderedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_datails_user);
        Intent intent = getIntent();
        orderId = intent.getStringExtra("orderId");
        orderTo = intent.getStringExtra("orderTo");
        mapping();
        firebaseAuth = FirebaseAuth.getInstance();
        loadShopInfo();
        loadOrderDetails();
        loadOrderedItems();
        onClick();
    }

    private void loadShopInfo() {
        //get shop info
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(orderTo)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String shopName = "" + snapshot.child("shopName").getValue();
                        txtShopName.setText(shopName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrderDetails() {
        //load order derails
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(orderTo).child("Orders").child(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get data
                        String orderBy = "" + snapshot.child("orderBy").getValue();
                        String orderId = "" + snapshot.child("orderId").getValue();
                        String orderTime = "" + snapshot.child("orderTime").getValue();
                        String orderStatus = "" + snapshot.child("orderStatus").getValue();
                        String orderCost = "" + snapshot.child("orderCost").getValue();
                        String orderTo = "" + snapshot.child("orderTo").getValue();
                        String orderAddress = "" + snapshot.child("orderAddress").getValue();
                        String deliveryFee = "" + snapshot.child("deliveryFee").getValue();
                        String discount = "" + snapshot.child("discount").getValue();

                        if (discount.equals("null") || discount.equals("0")){
                            discount = "& Discount $0";
                        }
                        else {
                            discount = "& Discount $" + discount;
                        }

                        //Conver timestamp to proper format
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(Long.parseLong(orderTime));
                        String formatedDate = DateFormat.format("dd/MM/yyyy hh:mm a", calendar).toString();

                        if(orderStatus.equals("In Progress")){
                            txtOrderStatus.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (orderStatus.equals("Completed")) {
                            txtOrderStatus.setTextColor(getResources().getColor(R.color.colorGreen));
                        }
                        else if (orderStatus.equals("Cancelled")) {
                            txtOrderStatus.setTextColor(getResources().getColor(R.color.colorRed));
                        }


                        //set data
                        txtOrderId.setText(orderId);
                        txtOrderStatus.setText(orderStatus);
                        txtAmount.setText("$" + orderCost + "[Including delivery fee $" + deliveryFee + " " + discount + "]");
                        txtDate.setText(formatedDate);
                        txtAddress.setText(orderAddress);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrderedItems() {
        list = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(orderTo).child("Orders").child(orderId).child("Items")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        for (DataSnapshot ds : snapshot.getChildren()){
                            OrderedItemUser orderedItemUser = ds.getValue(OrderedItemUser.class);

                            list.add(orderedItemUser);
                        }

                        adapterOrderedItem = new AdapterOrderedItem(OrderDatailsUser.this, list);
                        rvOrderItem.setAdapter(adapterOrderedItem);

                        txtTotalItems.setText("" + snapshot.getChildrenCount());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    //chuc nang
    private void onClick(){
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        imgReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrderDatailsUser.this, WriteReview.class);
                intent.putExtra("shopUid", orderTo);
                startActivity(intent);
            }
        });
    }

    private void mapping(){
        imgBack = findViewById(R.id.imgBack);
        txtOrderId = findViewById(R.id.txtOrderId);
        txtDate = findViewById(R.id.txtDate);
        txtOrderStatus = findViewById(R.id.txtOrderStatus);
        txtShopName = findViewById(R.id.txtShopName);
        txtTotalItems = findViewById(R.id.txtTotalItems);
        txtAmount = findViewById(R.id.txtAmount);
        txtAddress = findViewById(R.id.txtAddress);
        rvOrderItem = findViewById(R.id.rvOrderItem);
        imgReview = findViewById(R.id.imgReview);
    }
}