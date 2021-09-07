package com.example.appshop.activity.seller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.appshop.R;
import com.example.appshop.model.Product;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class ProductDetailsSeller extends AppCompatActivity {

    private ImageView imgBack, imgEdit, imgDelete, iconProduct;
    private TextView txtDiscountNote, txtTitle, txtDescription,
            txtCategory, txtQuantity, txtDiscountedPrice, txtPrice;

    private Product product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details_seller);
        mapping();
        uploadData();
    }

    private void uploadData() {
        //get data
        String id = product.getProductId();
        String uid = product.getUid();
        String discountAvailable = product.getDiscountAvailable();
        String discountNote = product.getDiscountNote();
        String discountPrice = product.getDiscountedPrice();
        String price = product.getPrice();
        String productCategory = product.getProductCategory();
        String productDescription = product.getProductDescription();
        String productIcon = product.getProductIcon();
        String productQuantity = product.getProductQuantity();
        String productTitle = product.getProductTitle();
        String productTimestamp = product.getTimestamp();

        //set data
        txtTitle.setText(productTitle);
        txtDiscountNote.setText(discountNote);
        txtDescription.setText(productDescription);
        txtCategory.setText(productCategory);
        txtQuantity.setText(productQuantity);
        txtPrice.setText("$" + price);
        txtDiscountedPrice.setText("$" + discountPrice);

        if(discountAvailable.equals("true")){
            //product is on discount
            txtDiscountedPrice.setVisibility(View.VISIBLE);
            txtDiscountNote.setVisibility(View.VISIBLE);
            txtPrice.setPaintFlags(txtPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); //add strike through on original price
        }
        else {
            txtDiscountedPrice.setVisibility(View.GONE);
            txtDiscountNote.setVisibility(View.GONE);
        }

        try {
            Picasso.get().load(productIcon).placeholder(R.drawable.ic_add_shopping_primary).into(iconProduct);
        } catch (Exception e) {
            iconProduct.setImageResource(R.drawable.ic_add_shopping_primary);
        }

        //Chuc nang
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show delete comfirm dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(ProductDetailsSeller.this);
                builder.setTitle("Delete")
                        .setMessage("Are you sure you want to delete product " + productTitle + "?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //delete
                                deleteProduct(id);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //cancel
                                dialog.dismiss();
                            }
                        })
                        .show();

            }
        });

        imgEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open edit product activity
                Intent intent = new Intent(ProductDetailsSeller.this, EditProductSeller.class);
                intent.putExtra("productId", id);
                startActivity(intent);
            }
        });
    }

    private void deleteProduct(String id) {
        //delete product using its id

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products").child(id).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(ProductDetailsSeller.this, "Delete success!", Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProductDetailsSeller.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void mapping(){
        imgBack = findViewById(R.id.imgBack);
        imgEdit = findViewById(R.id.imgEdit);
        imgDelete = findViewById(R.id.imgDelete);
        iconProduct = findViewById(R.id.iconProduct);
        txtDiscountNote = findViewById(R.id.txtDiscountNote);
        txtTitle = findViewById(R.id.txtTitle);
        txtDescription = findViewById(R.id.txtDescription);
        txtCategory = findViewById(R.id.txtCategory);
        txtQuantity = findViewById(R.id.txtQuantity);
        txtDiscountedPrice = findViewById(R.id.txtDiscountedPrice);
        txtPrice = findViewById(R.id.txtPrice);
        product = (Product) getIntent().getSerializableExtra("product");
    }
}