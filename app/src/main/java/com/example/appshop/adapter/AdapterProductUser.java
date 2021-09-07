package com.example.appshop.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.example.appshop.R;
import com.example.appshop.activity.user.FilterProductUser;
import com.example.appshop.activity.user.ProductDetailsUser;
import com.example.appshop.activity.user.ShopDetailsUser;
import com.example.appshop.model.Product;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterProductUser extends RecyclerView.Adapter<AdapterProductUser.HolderProductUser> implements Filterable {

    public Context context;
    public ArrayList<Product> products, filterList;
    private FilterProductUser filterProductUser;

    public AdapterProductUser(Context context, ArrayList<Product> products) {
        this.context = context;
        this.products = products;
        this.filterList = products;
    }

    @NonNull
    @Override
    public HolderProductUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_product_user, parent, false);
        return new HolderProductUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterProductUser.HolderProductUser holder, int position) {

        //get data
        final Product product = products.get(position);
        String discountAvailable = product.getDiscountAvailable();
        String discountNote = product.getDiscountNote();
        String discountedPrice = product.getDiscountedPrice();
        String price = product.getPrice();
        String productCategory = product.getProductCategory();
        String productDescription = product.getProductDescription();
        String productIcon = product.getProductIcon();
        String productId = product.getProductId();
        String productQuantity = product.getProductQuantity();
        String productTitle = product.getProductTitle();
        String timestamp = product.getTimestamp();

        //set data
        holder.txtTitle.setText(productTitle);
        holder.txtDiscountNote.setText(discountNote);
        holder.txtDescription.setText(productDescription);
        holder.txtDiscountedPrice.setText("$" + discountedPrice);
        holder.txtPrice.setText("$" + price);
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

        holder.txtAddToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add product to cart
                showQuantityDialog(product);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ProductDetailsUser.class);
                intent.putExtra("product", product);
                context.startActivity(intent);
            }
        });
    }


    private int cost = 0;
    private int finalCost = 0;
    private int quantity = 0;
    private void showQuantityDialog(Product product) {
        //inflate layout for dialog
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_quantity, null);
        //init layout views
        CircularImageView imgProduct = view.findViewById(R.id.imgProduct);
        ImageView imgRemove = view.findViewById(R.id.imgRemove);
        ImageView imgAdd = view.findViewById(R.id.imgAdd);
        TextView txtTitle = view.findViewById(R.id.txtTitle);
        TextView txtQuantity = view.findViewById(R.id.txtQuantity);
        TextView txtDescription = view.findViewById(R.id.txtDescription);
        TextView txtDiscountNote = view.findViewById(R.id.txtDiscountNote);
        TextView txtPrice = view.findViewById(R.id.txtPrice);
        TextView txtDiscountedPrice = view.findViewById(R.id.txtDiscountedPrice);
        TextView txtFinal = view.findViewById(R.id.txtFinal);
        TextView txtSum = view.findViewById(R.id.txtSum);
        Button btnAddToCart = view.findViewById(R.id.btnAddToCart);

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

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);


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


        final AlertDialog dialog = builder.create();
        dialog.show();

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

                dialog.dismiss();
            }
        });
    }

    public static int itemId = 1;
    private void addtoCart(String productId, String title, String priceEach, String price, String quantity) {
        itemId++;

        EasyDB easyDB = EasyDB.init(context, "ITEM_DB")
                .setTableName("ITEM_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                .doneTableColumn();
        
        Boolean b = easyDB
                .addData("Item_Id", itemId)
                .addData("Item_PID", productId)
                .addData("Item_Name", title)
                .addData("Item_Price_Each", priceEach)
                .addData("Item_Price", price)
                .addData("Item_Quantity", quantity)
                .doneDataAdding();

        Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show();

        //update cart count
        ShopDetailsUser.cartCount();
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    @Override
    public Filter getFilter() {
        if (filterProductUser == null){
            filterProductUser = new FilterProductUser(this, filterList);
        }
        return filterProductUser;
    }

    class HolderProductUser extends RecyclerView.ViewHolder{
        private ImageView imgProduct, imgNext;
        private TextView txtDiscountNote, txtTitle, txtDescription
                , txtAddToCart, txtDiscountedPrice, txtPrice;

        public HolderProductUser(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            imgNext = itemView.findViewById(R.id.imgNext);
            txtDiscountNote = itemView.findViewById(R.id.txtDiscountNote);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtDescription = itemView.findViewById(R.id.txtDescription);
            txtAddToCart = itemView.findViewById(R.id.txtAddToCart);
            txtDiscountedPrice = itemView.findViewById(R.id.txtDiscountedPrice);
            txtPrice = itemView.findViewById(R.id.txtPrice);
        }
    }
}
