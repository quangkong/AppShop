package com.example.appshop.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appshop.R;
import com.example.appshop.activity.seller.EditProductSeller;
import com.example.appshop.activity.seller.FilterProductSeller;
import com.example.appshop.activity.seller.ProductDetailsSeller;
import com.example.appshop.model.Product;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterProductSeller extends RecyclerView.Adapter<AdapterProductSeller.HolderProductSeller> implements Filterable {

    private Context context;
    public ArrayList<Product> list, filterList;
    private FilterProductSeller filter;

    public AdapterProductSeller(Context context, ArrayList<Product> list) {
        this.context = context;
        this.list = list;
        this.filterList = list;
    }

    @NonNull
    @Override
    public HolderProductSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout

        View view = LayoutInflater.from(context).inflate(R.layout.row_product_seller, parent, false);
        return new HolderProductSeller(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterProductSeller.HolderProductSeller holder, int position) {
        Product product = list.get(position);
        String id = product.getProductId();
        String uid = product.getUid();
        String discountAvailable = product.getDiscountAvailable();
        String discountNote = product.getDiscountNote();
        String discountedPrice = product.getDiscountedPrice();
        String price = product.getPrice();
        String productCategory = product.getProductCategory();
        String productDescription = product.getProductDescription();
        String productIcon = product.getProductIcon();
        String productQuantity = product.getProductQuantity();
        String productTitle = product.getProductTitle();
        String productTimestamp = product.getTimestamp();

        //set data
        holder.txtTitle.setText(productTitle);
        holder.txtDiscountNote.setText(discountNote);
        holder.txtDiscountedPrice.setText("$" + discountedPrice);
        holder.txtPrice.setText("$" + price);
        holder.txtQuantity.setText(productQuantity);
        if(discountAvailable.equals("true")){
            //product is on discount
            holder.txtDiscountedPrice.setVisibility(View.VISIBLE);
            holder.txtDiscountNote.setVisibility(View.VISIBLE);
            holder.txtPrice.setPaintFlags(holder.txtPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); //add strike through on original price
        }
        else {
            holder.txtDiscountedPrice.setVisibility(View.GONE);
            holder.txtDiscountNote.setVisibility(View.GONE);
            holder.txtPrice.setPaintFlags(0);
        }

        try {
            Picasso.get().load(productIcon).placeholder(R.drawable.ic_add_shopping_primary).into(holder.imgProduct);
        } catch (Exception e) {
            holder.imgProduct.setImageResource(R.drawable.ic_add_shopping_primary);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //handle item clicks, show item details (in bottom sheet)
                //detailsBottomSheet(product);
                Intent intent = new Intent(context, ProductDetailsSeller.class);
                intent.putExtra("product", product);
                context.startActivity(intent);
            }
        });
    }

//
//    private void detailsBottomSheet(Product product) {
//        //bottom sheet
//        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
//        View view = LayoutInflater.from(context).inflate(R.layout.bs_product_details_seller, null);
//
//        //set view to bottomsheet
//        bottomSheetDialog.setContentView(view);
//
//        //init views of bottomsheet
//        ImageView imgBack = view.findViewById(R.id.imgBack);
//        ImageView imgEdit = view.findViewById(R.id.imgEdit);
//        ImageView imgDelete = view.findViewById(R.id.imgDelete);
//        ImageView iconProduct = view.findViewById(R.id.iconProduct);
//        TextView txtDiscountNote = view.findViewById(R.id.txtDiscountNote);
//        TextView txtTitle = view.findViewById(R.id.txtTitle);
//        TextView txtDescription = view.findViewById(R.id.txtDescription);
//        TextView txtCategory = view.findViewById(R.id.txtCategory);
//        TextView txtQuantity = view.findViewById(R.id.txtQuantity);
//        TextView txtDiscountedPrice = view.findViewById(R.id.txtDiscountedPrice);
//        TextView txtPrice = view.findViewById(R.id.txtPrice);
//
//
//        //get data
//        String id = product.getProductId();
//        String uid = product.getUid();
//        String discountAvailable = product.getDiscountAvailable();
//        String discountNote = product.getDiscountNote();
//        String discountPrice = product.getDiscountedPrice();
//        String price = product.getPrice();
//        String productCategory = product.getProductCategory();
//        String productDescription = product.getProductDescription();
//        String productIcon = product.getProductIcon();
//        String productQuantity = product.getProductQuantity();
//        String productTitle = product.getProductTitle();
//        String productTimestamp = product.getTimestamp();
//
//        //set data
//        txtTitle.setText(productTitle);
//        txtDiscountNote.setText(discountNote);
//        txtDescription.setText(productDescription);
//        txtCategory.setText(productCategory);
//        txtQuantity.setText(productQuantity);
//        txtPrice.setText("$" + price);
//        txtDiscountedPrice.setText("$" + discountPrice);
//
//        if(discountAvailable.equals("true")){
//            //product is on discount
//            txtDiscountedPrice.setVisibility(View.VISIBLE);
//            txtDiscountNote.setVisibility(View.VISIBLE);
//            txtPrice.setPaintFlags(txtPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); //add strike through on original price
//        }
//        else {
//            txtDiscountedPrice.setVisibility(View.GONE);
//            txtDiscountNote.setVisibility(View.GONE);
//        }
//
//        try {
//            Picasso.get().load(productIcon).placeholder(R.drawable.ic_add_shopping_primary).into(iconProduct);
//        } catch (Exception e) {
//            iconProduct.setImageResource(R.drawable.ic_add_shopping_primary);
//        }
//
//        bottomSheetDialog.show();
//
//        //Chuc nang
//        imgBack.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                bottomSheetDialog.dismiss();
//            }
//        });
//
//        imgDelete.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                bottomSheetDialog.dismiss();
//
//                //show delete comfirm dialog
//                AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                builder.setTitle("Delete")
//                        .setMessage("Are you sure you want to delete product " + productTitle + "?")
//                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                //delete
//                                deleteProduct(id);
//                            }
//                        })
//                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                //cancel
//                                dialog.dismiss();
//                            }
//                        })
//                        .show();
//
//            }
//        });
//
//        imgEdit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                bottomSheetDialog.dismiss();
//                //open edit product activity
//                Intent intent = new Intent(context, EditProductSeller.class);
//                intent.putExtra("productId", id);
//                context.startActivity(intent);
//            }
//        });
//
//    }
//
//    private void deleteProduct(String id) {
//        //delete product using its id
//
//        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
//        reference.child(firebaseAuth.getUid()).child("Products").child(id).removeValue()
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void unused) {
//                        Toast.makeText(context, "Delete success!", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public Filter getFilter() {
        if(filter == null){
            filter = new FilterProductSeller(this, filterList);
        }
        return filter;
    }

    class HolderProductSeller extends RecyclerView.ViewHolder{
        /*hold view of recyclerview*/

        private ImageView imgProduct, imgNext;
        private TextView txtDiscountNote, txtTitle, txtQuantity, txtDiscountedPrice, txtPrice;

        public HolderProductSeller(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            imgNext = itemView.findViewById(R.id.imgNext);
            txtDiscountNote = itemView.findViewById(R.id.txtDiscountNote);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtQuantity = itemView.findViewById(R.id.txtQuantity);
            txtDiscountedPrice = itemView.findViewById(R.id.txtDiscountedPrice);
            txtPrice = itemView.findViewById(R.id.txtPrice);
        }
    }
}
