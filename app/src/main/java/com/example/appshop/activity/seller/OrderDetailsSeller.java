package com.example.appshop.activity.seller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.appshop.R;
import com.example.appshop.activity.user.OrderDatailsUser;
import com.example.appshop.activity.user.ShopDetailsUser;
import com.example.appshop.adapter.AdapterOrderedItem;
import com.example.appshop.model.Constants;
import com.example.appshop.model.OrderedItemUser;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class OrderDetailsSeller extends AppCompatActivity {

    private String orderBy, orderId;

    private ImageView imgBack, imgEdit;
    private TextView txtOrderId, txtDate, txtOrderStatus
            , txtEmail, txtPhone, txtAmount, txtAddress, txtItem;
    private RecyclerView rvOrderItem;

    private FirebaseAuth firebaseAuth;

    private ArrayList<OrderedItemUser> list;
    private AdapterOrderedItem adapterOrderedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details_seller);
        Intent intent = getIntent();
        orderBy = intent.getStringExtra("orderBy");
        orderId = intent.getStringExtra("orderId");
        mapping();
        firebaseAuth = FirebaseAuth.getInstance();
        loadMyInfo();
        loadBuyerInfo();
        loadOrderDetails();
        loadOrderedItems();
        onClick();
    }

    private void loadOrderDetails() {
        //load detailed info of this order, based on order id
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Orders").child(orderId)
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

    private void loadMyInfo() {
        //get my info
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadBuyerInfo() {
        //get buyer info
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(orderBy)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String email = "" + snapshot.child("email").getValue();
                        String phone = "" + snapshot.child("phone").getValue();

                        //set info
                        txtEmail.setText(email);
                        txtPhone.setText(phone);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrderedItems(){
        //load the products/items of order
        list = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Orders").child(orderId).child("Items")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        for (DataSnapshot ds : snapshot.getChildren()){
                            OrderedItemUser orderedItemUser = ds.getValue(OrderedItemUser.class);

                            list.add(orderedItemUser);
                        }

                        adapterOrderedItem = new AdapterOrderedItem(OrderDetailsSeller.this, list);
                        rvOrderItem.setAdapter(adapterOrderedItem);

                        txtItem.setText("" + snapshot.getChildrenCount());
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

        imgEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //edit order status: In Progress, Completed, Cancelled
                editOrderStatusDialog();
            }
        });
    }

    private void editOrderStatusDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(OrderDetailsSeller.this);
        builder.setTitle("Edit order status")
                .setItems(Constants.statusOrderSeller, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String select = Constants.statusOrderSeller[which];
                        editOrderStatus(select);
                    }
                }).show();
    }

    private void editOrderStatus(String select) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("orderStatus", "" + select);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Orders").child(orderId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        String message = "Order is name" + select;
                        Toast.makeText(OrderDetailsSeller.this, message, Toast.LENGTH_SHORT).show();

                        prepareNotificationMessage(orderId, message);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(OrderDetailsSeller.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void mapping(){
        imgBack = findViewById(R.id.imgBack);
        imgEdit = findViewById(R.id.imgEdit);
        txtOrderId = findViewById(R.id.txtOrderId);
        txtOrderStatus = findViewById(R.id.txtOrderStatus);
        txtOrderStatus = findViewById(R.id.txtOrderStatus);
        txtDate = findViewById(R.id.txtDate);
        txtEmail = findViewById(R.id.txtEmail);
        txtPhone = findViewById(R.id.txtPhone);
        txtAmount = findViewById(R.id.txtAmount);
        txtAddress = findViewById(R.id.txtAddress);
        txtItem = findViewById(R.id.txtItem);
        rvOrderItem = findViewById(R.id.rvOrderItem);
    }

    private void prepareNotificationMessage(String orderId, String message){
        //when user seller changes order status , send notification to buyer

        //prepare data for notification
        String NOTIFICATION_TOPIC = "/topics/" + Constants.FCM_TOPIC;  //must be same as subscribed by user
        String NOTIFICATION_TITLE = "Your Order " + orderId;
        String NOTIFICATION_MESSAGE = "" + message;
        String NOTIFICATION_TYPE = "OrderStatusChanged";

        //prepare json
        JSONObject notificationJS = new JSONObject();
        JSONObject notificationBodyJS = new JSONObject();
        try {
            //what to send
            notificationBodyJS.put("notificationType", NOTIFICATION_TYPE);
            notificationBodyJS.put("buyerUid", orderBy);
            notificationBodyJS.put("sellerUid", firebaseAuth.getUid()); //since we are logged in as seller to change order status so current user uid is seller uid
            notificationBodyJS.put("orderId", orderId);
            notificationBodyJS.put("notificationTitle", NOTIFICATION_TITLE);
            notificationBodyJS.put("notificationMessage", NOTIFICATION_MESSAGE);

            //where to send
            notificationJS.put("to", NOTIFICATION_TOPIC); //to all who subscribed to this topic
            notificationJS.put("data", notificationBodyJS);

        }catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        sendFcmNotification(notificationJS);
    }

    private void sendFcmNotification(JSONObject notificationJS) {
        //send volley request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJS, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //notification sent

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //notification failed
                Toast.makeText(OrderDetailsSeller.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                //put requied headers
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "key=" + Constants.FCM_KEY);
                return headers;
            }
        };

        //enque the volley request
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
}