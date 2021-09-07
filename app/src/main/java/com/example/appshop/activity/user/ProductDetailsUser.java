package com.example.appshop.activity.user;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.appshop.R;
import com.example.appshop.model.Product;
import com.squareup.picasso.Picasso;
import com.example.appshop.adapter.AdapterCartItem;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

import static com.example.appshop.adapter.AdapterProductUser.itemId;

public class ProductDetailsUser extends AppCompatActivity {

    ImageView imgRemove, imgAdd, imgProduct, imgBack;
    TextView txtTitle, txtQuantity, txtDescription, txtDiscountNote,
            txtPrice, txtDiscountedPrice, txtFinal, txtSum;
    Button btnAddToCart;

    private Product product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details_user);
        mapping();
        uploadData();
    }

    private int cost = 0;
    private int finalCost = 0;
    private int quantity = 0;
    private void uploadData() {
        //get data from model
        String productId = product.getProductId();
        String discountNote = product.getDiscountNote();
        final String price;
        String productDescription = product.getProductDescription();
        String productIcon = product.getProductIcon();
        String productQuantity = product.getProductQuantity();
        String productTitle = product.getProductTitle();
        if(product.getDiscountAvailable().equals("true")){
            //product have discount
            price = product.getDiscountedPrice();
            txtDiscountNote.setVisibility(View.VISIBLE);
            txtPrice.setPaintFlags(txtPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        else {
            //product don't have discount
            txtDiscountNote.setVisibility(View.GONE);
            txtDiscountedPrice.setVisibility(View.GONE);
            price = product.getPrice();
        }

        cost = Integer.parseInt(price.replaceAll("$", ""));
        finalCost = Integer.parseInt(price.replaceAll("$", ""));
        quantity = 1;


        //set data
        try {
            Picasso.get().load(productIcon).placeholder(R.drawable.ic_cart_gray).into(imgProduct);
        } catch (Exception e){
            imgProduct.setImageResource(R.drawable.ic_cart_gray);
        }
        txtTitle.setText(""+productTitle);
        txtDescription.setText(""+productDescription);
        txtDiscountNote.setText(""+discountNote);
        txtSum.setText(""+quantity);
        txtQuantity.setText("" + productQuantity);
        txtPrice.setText("$" + product.getPrice());
        txtDiscountedPrice.setText("$" + product.getDiscountedPrice());
        txtFinal.setText("$" + finalCost);

        //increase quantity of the product
        imgAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalCost = finalCost + cost;
                quantity++;
                txtFinal.setText("$" + finalCost);
                txtSum.setText("" + quantity);
            }
        });

        //decrement quantity of product, only if quantity is > 1
        imgRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(quantity > 1){
                    finalCost = finalCost - cost;
                    quantity--;
                    txtFinal.setText("$" + finalCost);
                    txtSum.setText("" + quantity);
                }
            }
        });

        btnAddToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = txtTitle.getText().toString().trim();
                String priceEach = price;
                String totalPrice = txtFinal.getText().toString().trim().replace("$", "");
                String quantity = txtSum.getText().toString().trim();

                //add to db(SQLite)
                addtoCart(productId, title, priceEach, totalPrice, quantity);
                onBackPressed();
            }
        });

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void addtoCart(String productId, String title, String priceEach, String price, String quantity) {
        itemId++;
        EasyDB easyDB = EasyDB.init(ProductDetailsUser.this, "ITEM_DB")
                .setTableName("ITEM_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                .doneTableColumn();

        //get data db
//        Cursor cursor = easyDB.getAllData();
//        while (cursor.moveToNext()){
//            String pId = cursor.getString(2);
//            String quantity1 = cursor.getString(6);
//            if (pId.equals(productId)){
//                int sum = Integer.parseInt(quantity) + Integer.parseInt(quantity1);
//                quantity = String.valueOf(sum);
//                boolean delete = easyDB.deleteRow("Item_Id", pId);
//                break;
//            }
//        }

        Boolean b = easyDB
                .addData("Item_Id", itemId)
                .addData("Item_PID", productId)
                .addData("Item_Name", title)
                .addData("Item_Price_Each", priceEach)
                .addData("Item_Price", price)
                .addData("Item_Quantity", quantity)
                .doneDataAdding();
        ShopDetailsUser.cartCount();

        Toast.makeText(ProductDetailsUser.this, "Added to cart", Toast.LENGTH_SHORT).show();
        //update cart count
        onBackPressed();
    }


    //Chuc nang
    private void onClick(){
        //Chuc nang
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }


    private void mapping(){
        imgBack = findViewById(R.id.imgBack);
        imgProduct = findViewById(R.id.imgProduct);
        txtDiscountNote = findViewById(R.id.txtDiscountNote);
        txtTitle = findViewById(R.id.txtTitle);
        txtDescription = findViewById(R.id.txtDescription);
        imgAdd = findViewById(R.id.imgAdd);
        txtQuantity = findViewById(R.id.txtQuantity);
        txtDiscountedPrice = findViewById(R.id.txtDiscountedPrice);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        imgRemove = findViewById(R.id.imgRemove);
        txtFinal = findViewById(R.id.txtFinal);
        txtSum = findViewById(R.id.txtSum);
        txtPrice = findViewById(R.id.txtPrice);
        product = (Product) getIntent().getSerializableExtra("product");
    }
}