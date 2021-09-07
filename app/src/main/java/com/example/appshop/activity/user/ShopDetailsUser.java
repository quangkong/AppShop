package com.example.appshop.activity.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.appshop.R;
import com.example.appshop.model.Constants;
import com.example.appshop.adapter.AdapterCartItem;
import com.example.appshop.adapter.AdapterProductUser;
import com.example.appshop.model.CartItem;
import com.example.appshop.model.Product;
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

import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class ShopDetailsUser extends AppCompatActivity{

    private String address, phone;

    private ImageView imgShop, imgFilter, imgBack, imgCart, imgStar;
    private TextView txtShopName;
    private TextView txtPhone;
    private TextView txtEmail;
    private TextView txtAddress;
    private TextView txtOpen;
    private TextView txtDeliveryFee;
    private TextView txtShowAll;
    private static TextView txtCartCount;
    private EditText edtSearch;
    private RecyclerView rvProducts;
    private RatingBar ratingBar;

    private String shopUid, shopName, shopEmail, shopPhone, shopAddress;
    private FirebaseAuth firebaseAuth;

    private ArrayList<Product> products;
    private AdapterProductUser adapterProductUser;

    private ArrayList<CartItem> list;
    private AdapterCartItem adapterCartItem;

    public String deliveryFee;

    private ProgressDialog progressDialog;

    private static EasyDB easyDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_details_user);
        mapping();

        easyDB = EasyDB.init(this, "ITEM_DB")
                .setTableName("ITEM_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                .doneTableColumn();

        deleteCartData();

        loadMyInfo();
        loadShopDetails();
        loadShopProDucts();
        loadReviews();
        onClick();

        //Tranh lap lai trong gio hang

        cartCount();
    }

    private float ratingSum = 0;
    private void loadReviews() {

        list = new ArrayList<>();

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

                        ratingBar.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void deleteCartData() {
        easyDB.deleteAllDataFromTable();
    }

    public static void cartCount(){
        //get cart count

        int count = easyDB.getAllData().getCount();
        if(count<=0){
            txtCartCount.setVisibility(View.GONE);
        }
        else {
            txtCartCount.setVisibility(View.VISIBLE);
            txtCartCount.setText("" + count);
        }
    }

    //Chuc nang
    private void onClick(){
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                //startActivity(new Intent(ShopDetailsUser.this, MainUser.class));
            }
        });

        imgCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show cart dialog
                showCartDialog();
            }
        });

        txtPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                diaCall();
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
                    if (adapterProductUser != null && !(s.toString().equals(""))){
                        adapterProductUser.getFilter().filter(s.toString());
                    }
                } catch (Exception e) {
                    Toast.makeText(ShopDetailsUser.this, "1", Toast.LENGTH_LONG).show();
                    Toast.makeText(ShopDetailsUser.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        imgFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                diglogFilter();
            }
        });


        //show reviews activity
        imgStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pass shop uid to show its reviews
                Intent intent = new Intent(ShopDetailsUser.this, ShopReviews.class);
                intent.putExtra("shopUid", shopUid);
                startActivity(intent);
            }
        });
    }

    public int allTotalPrice = 0;
    //need to access these views in adapter so making public
    public TextView txtSTotal, txtFee, txtTotal, txtPromoDescription, txtDiscount;
    public EditText edtPromotion;
    public FloatingActionButton fabSubmit;
    public Button btnApply;
    private void showCartDialog() {

        //init list
        list = new ArrayList<>();
        list.clear();

        //inflate cart layout
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_cart, null);

        //init views
        TextView txtShopName = view.findViewById(R.id.txtShopName);
        txtFee = view.findViewById(R.id.txtFee);
        txtSTotal = view.findViewById(R.id.txtSTotal);
        txtTotal = view.findViewById(R.id.txtTotal);
        txtPromoDescription = view.findViewById(R.id.txtPromoDescription);
        txtDiscount = view.findViewById(R.id.txtDiscount);
        edtPromotion = view.findViewById(R.id.edtPromotion);
        fabSubmit = view.findViewById(R.id.fabSubmit);
        btnApply = view.findViewById(R.id.btnApply);
        RecyclerView rvCartItem = view.findViewById(R.id.rvCartItem);
        Button btnCheck = view.findViewById(R.id.btnCheck);

        //whenever cart dialog shows, check if promo code is applied or not
        if (isPromoCodeApplied){
            //applied
            txtPromoDescription.setVisibility(View.VISIBLE);
            btnApply.setVisibility(View.VISIBLE);
            btnApply.setText("APPLIED");
            edtPromotion.setText(promoCode);
            txtPromoDescription.setText(promoDescription);
        }
        else {
            txtPromoDescription.setVisibility(View.GONE);
            btnApply.setVisibility(View.GONE);
            btnApply.setText("APPLY");
        }

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //set view
        builder.setView(view);

        txtShopName.setText(shopName);

        //get all records
        Cursor cursor = easyDB.getAllData();
        while (cursor.moveToNext()){
            String id = cursor.getString(1);
            String pId = cursor.getString(2);
            String name = cursor.getString(3);
            String price = cursor.getString(4);
            String cost = cursor.getString(5);
            String quantity = cursor.getString(6);

            allTotalPrice = allTotalPrice + Integer.parseInt(cost);

            CartItem cartItem = new CartItem(
                    ""+id,
                    ""+pId,
                    ""+name,
                    ""+price,
                    ""+cost,
                    ""+quantity
            );
            list.add(cartItem);
        }

        //setup adapter
        adapterCartItem = new AdapterCartItem(this, list);

        //set to recyclerview
        rvCartItem.setAdapter(adapterCartItem);

        if (isPromoCodeApplied){
            priceWithDiscount();
        }
        else {
            priceWithoutDiscount();
        }

        //show dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        //reset total price on dialog dismiss
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                allTotalPrice = 0;
            }
        });

        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //first validate delivery address
                if (address.equals("") || address.equals("null")){
                    Toast.makeText(ShopDetailsUser.this, "Please enter your address in you profile before placing order", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(phone.equals("") || phone.equals("null")){
                    Toast.makeText(ShopDetailsUser.this, "Please enter your phone number in you profile before placing order", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(list.size() == 0){
                    //cart list is empty
                    Toast.makeText(ShopDetailsUser.this, "No Item!!", Toast.LENGTH_SHORT).show();
                    return;
                }
                submitOrder();
                dialog.dismiss();
            }
        });

        fabSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                1, get code
                    if not empty: promotion may be applied, otherwise no promotion
                2, check if code is valid. Available id seller's promotion db
                    if available: promotion may be applied, otherwise no promotion
                3, check if expired or not
                    if not expired: promotion may be applied, otherwise no promotion
                4, check if minimum order price
                    if minimumOrderPrice is >= subtotal prixe: promotion available, otherwise no promotion
                * */

                String promotionCode = edtPromotion.getText().toString().trim();
                if (TextUtils.isEmpty(promotionCode)){
                    Toast.makeText(ShopDetailsUser.this, "Please enter promo code...", Toast.LENGTH_SHORT).show();
                }
                else {
                    checkCodeAvailability(promotionCode);
                }
            }
        });

        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPromoCodeApplied = true;
                btnApply.setText("APPLIED");

                priceWithDiscount();
            }
        });
    }

    public boolean isPromoCodeApplied = false;
    public String promoId, promoTimestamp, promoDescription, promoCode, promoExpDate, promoMinimumOrderPrice = "0", promoPrice = "0";
    private void checkCodeAvailability(String promotionCode){ //promotionCode is promo code entered by user
        //progress bar
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setMessage("Checking Promo Code...");
        progressDialog.setCanceledOnTouchOutside(false);

        isPromoCodeApplied = false;
        btnApply.setText("Apply");

        priceWithoutDiscount();

        //check promo code
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Promotions").orderByChild("promoCode").equalTo(promotionCode)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            progressDialog.dismiss();
                            for (DataSnapshot ds : snapshot.getChildren()){
                                promoId = "" + ds.child("id").getValue();
                                promoTimestamp = "" + ds.child("timestamp").getValue();
                                promoDescription = "" + ds.child("description").getValue();
                                promoExpDate = "" + ds.child("expireDate").getValue();
                                promoMinimumOrderPrice = "" + ds.child("minimumOrderPrice").getValue();
                                promoPrice = "" + ds.child("promoPrice").getValue();
                                promoCode = "" + ds.child("promoCode").getValue();
                                checkCodeExpireDate();
                            }
                        }
                        else {
                            progressDialog.dismiss();
                            Toast.makeText(ShopDetailsUser.this, "Invalid promo code", Toast.LENGTH_SHORT).show();
                            btnApply.setVisibility(View.GONE);
                            txtPromoDescription.setVisibility(View.GONE);
                            txtPromoDescription.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkCodeExpireDate() {
        //get current date to set on calendar
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        String today = day + "/" + month + "/" + year;

        try {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            Date cur = format.parse(today);
            Date expireDate = format.parse(promoExpDate);

            if (expireDate.compareTo(cur) >= 0){
                //date 1 occurs after 2
                checkMinimumOrderPrice();
            }
            else if (expireDate.compareTo(cur) < 0){
                Toast.makeText(this, "The promotion code is expired on", Toast.LENGTH_SHORT).show();
                btnApply.setVisibility(View.GONE);
                txtPromoDescription.setVisibility(View.GONE);
                txtPromoDescription.setText("");
            }
        }
        catch (Exception e){
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            btnApply.setVisibility(View.GONE);
            txtPromoDescription.setVisibility(View.GONE);
            txtPromoDescription.setText("");
        }
    }

    private void checkMinimumOrderPrice() {
        //each promo code have minimum order price requirement, if order price is less then required then don't allow to apply code
        if (Integer.parseInt(String.valueOf(allTotalPrice)) < Integer.parseInt(promoMinimumOrderPrice)){
            //current order price is less then minimum order required by promo code, so don't allow to apply
            Toast.makeText(this, "This code is valid for order with minimum amount: $" + promoMinimumOrderPrice, Toast.LENGTH_SHORT).show();
            btnApply.setVisibility(View.GONE);
            txtPromoDescription.setVisibility(View.GONE);
            txtPromoDescription.setText("");
        }
        else {
            btnApply.setVisibility(View.VISIBLE);
            txtPromoDescription.setVisibility(View.VISIBLE);
            txtPromoDescription.setText(promoDescription);
        }
    }

    private void priceWithDiscount(){
        txtDiscount.setText("$" + promoPrice);
        txtFee.setText("$" + deliveryFee);
        txtSTotal.setText("$" + allTotalPrice);
        txtTotal.setText("$" + (allTotalPrice + Integer.parseInt(deliveryFee.replace("$", "")) - Integer.parseInt(promoPrice)));
    }

    private void priceWithoutDiscount() {
        txtDiscount.setText("$0");
        txtFee.setText("$" + deliveryFee);
        txtSTotal.setText("$" + allTotalPrice);
        txtTotal.setText("$" + (allTotalPrice + Integer.parseInt(deliveryFee.replace("$", ""))));
    }

    private void submitOrder() {
        //show progress dialog
        progressDialog.setMessage("Placing order...");
        progressDialog.show();

        //for order id and order time
        String timestamp = "" + System.currentTimeMillis();

        String cost = txtTotal.getText().toString().trim().replace("$", "");

        //setup order data
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("orderId", "" + timestamp);
        hashMap.put("orderTime", "" + timestamp);
        hashMap.put("orderStatus", "In Progress"); //In Progress/Completed/Cancelled
        hashMap.put("orderCost", "" + cost);
        hashMap.put("deliveryFee", "" + deliveryFee);
        hashMap.put("orderAddress", "" + address);
        hashMap.put("orderBy", "" + firebaseAuth.getUid());
        hashMap.put("orderTo", "" + shopUid);

        if (isPromoCodeApplied){
            hashMap.put("discount", "" + promoPrice);
        }
        else {
            hashMap.put("discount", "0");
        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(shopUid).child("Orders");
        reference.child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //order info added now add order items
                        for(int i = 0; i<list.size(); i++){
                            String pId = list.get(i).getpId();
                            String id = list.get(i).getId();
                            String cost = list.get(i).getCost();
                            String name = list.get(i).getName();
                            String price = list.get(i).getPrice();
                            String quantity = list.get(i).getQuantity();

                            HashMap<String, String> hashMap1 = new HashMap<>();
                            hashMap1.put("pId", pId);
                            hashMap1.put("name", name);
                            hashMap1.put("cost", cost);
                            hashMap1.put("price", price);
                            hashMap1.put("quantity", quantity);

                            reference.child(timestamp).child("Items").child(pId).setValue(hashMap1);
                        }
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsUser.this, "Order Successfully", Toast.LENGTH_SHORT).show();

                        //notification
                        prepareNotificationMessage(timestamp);

                        //after placing order open order details page
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsUser.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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
                            phone = "" + dataSnapshot.child("phone").getValue();
                            String profileImage = "" + dataSnapshot.child("profileImage").getValue();
                            address = "" + dataSnapshot.child("address").getValue();

                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void diglogFilter(){
        AlertDialog.Builder builder = new AlertDialog.Builder(ShopDetailsUser.this);
        builder.setTitle("Choose Category: ")
                .setItems(Constants.productAll, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //get selected item
                        String selected = Constants.productAll[which];
                        txtShowAll.setText(selected);
                        if(selected.equals("All")){
                            loadShopProDucts();
                        }
                        else {
                            //load filtered
                            try {
                                if (adapterProductUser != null){
                                    adapterProductUser.getFilter().filter(selected);
                                }
                            } catch (Exception e) {
                                Toast.makeText(ShopDetailsUser.this, "2", Toast.LENGTH_LONG).show();
                                Toast.makeText(ShopDetailsUser.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }).show();
    }

    private void loadShopDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = "" + snapshot.child("name").getValue();
                shopName = "" + snapshot.child("shopName").getValue();
                shopEmail = "" + snapshot.child("email").getValue();
                shopPhone = "" + snapshot.child("phone").getValue();
                shopAddress = "" + snapshot.child("address").getValue();
                deliveryFee = "" + snapshot.child("deliveryFee").getValue();
                String profileImage = "" + snapshot.child("profileImage").getValue();
                String shopOpen = "" + snapshot.child("shopOpen").getValue();

                //set data
                txtShopName.setText(shopName);
                txtPhone.setText(shopPhone);
                txtEmail.setText(shopEmail);
                txtDeliveryFee.setText("Delivery: $" + deliveryFee);
                txtAddress.setText(shopAddress);
                if (shopOpen.equals("true")){
                    txtOpen.setText("Open");
                }
                else {
                    txtOpen.setText("Closed");
                }

                try {
                    Picasso.get().load(profileImage).into(imgShop);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadShopProDucts() {
        //init list
        products = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        products.clear();
                        for (DataSnapshot ds : snapshot.getChildren()){
                            Product product = ds.getValue(Product.class);
                            products.add(product);
                        }

                        adapterProductUser = new AdapterProductUser(ShopDetailsUser.this, products);
                        rvProducts.setAdapter(adapterProductUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void diaCall() {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+Uri.encode(shopPhone))));
        Toast.makeText(this, "" + shopPhone, Toast.LENGTH_SHORT).show();
    }

    private void mapping(){
        imgShop = findViewById(R.id.imgShop);
        imgFilter = findViewById(R.id.imgFilter);
        imgBack = findViewById(R.id.imgBack);
        imgCart = findViewById(R.id.imgCart);
        txtShopName = findViewById(R.id.txtShopName);
        txtPhone = findViewById(R.id.txtPhone);
        txtEmail = findViewById(R.id.txtEmail);
        txtAddress = findViewById(R.id.txtAddress);
        txtOpen = findViewById(R.id.txtOpen);
        txtDeliveryFee = findViewById(R.id.txtDeliveryFee);
        edtSearch = findViewById(R.id.edtSearch);
        rvProducts = findViewById(R.id.rvProducts);
        txtCartCount = findViewById(R.id.txtCartCount);
        imgStar = findViewById(R.id.imgStar);
        txtShowAll = findViewById(R.id.txtShowAll);
        ratingBar = findViewById(R.id.ratingBar);
        shopUid = getIntent().getStringExtra("shopUid");
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    private void prepareNotificationMessage(String orderId){
        //when user places order, send notification to seller

        //prepare data for notification
        String NOTIFICATION_TOPIC = "/topics/" + Constants.FCM_TOPIC;  //must be same as subscribed by user
        String NOTIFICATION_TITLE = "New Order " + orderId;
        String NOTIFICATION_MESSAGE = "Congratulations...! You have new order";
        String NOTIFICATION_TYPE = "NewOrder";

        //prepare json
        JSONObject notificationJS = new JSONObject();
        JSONObject notificationBodyJS = new JSONObject();
        try {
            //what to send
            notificationBodyJS.put("notificationType", NOTIFICATION_TYPE);
            notificationBodyJS.put("buyerUid", firebaseAuth.getUid()); //since we are logger in as buyer to place order so current user is buyer uid
            notificationBodyJS.put("sellerUid", shopUid);
            notificationBodyJS.put("orderId", orderId);
            notificationBodyJS.put("notificationTitle", NOTIFICATION_TITLE);
            notificationBodyJS.put("notificationMessage", NOTIFICATION_MESSAGE);

            //where to send
            notificationJS.put("to", NOTIFICATION_TOPIC); //to all who subscribed to this topic
            notificationJS.put("data", notificationBodyJS);

        }catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        sendFcmNotification(notificationJS, orderId);
    }

    private void sendFcmNotification(JSONObject notificationJS, String orderId) {
        //send volley request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJS, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //after sending fcm start order details activity
                Intent intent = new Intent(ShopDetailsUser.this, OrderDatailsUser.class);
                intent.putExtra("orderTo", shopUid);
                intent.putExtra("orderId", orderId);
                startActivity(intent);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ShopDetailsUser.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
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