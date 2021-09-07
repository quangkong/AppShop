package com.example.appshop.adapter;

import android.app.PendingIntent;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appshop.R;
import com.example.appshop.activity.user.ShopDetailsUser;
import com.example.appshop.model.CartItem;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterCartItem extends RecyclerView.Adapter<AdapterCartItem.HolderCartItem>{

    private Context context;
    private ArrayList<CartItem> list;

    public AdapterCartItem(Context context, ArrayList<CartItem> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public HolderCartItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_cartitem, parent, false);
        return new HolderCartItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterCartItem.HolderCartItem holder, int position) {
        CartItem cartItem = list.get(position);

        String id = cartItem.getId();
        String getpId = cartItem.getpId();
        String cost = cartItem.getCost();
        String name = cartItem.getName();
        String price = cartItem.getPrice();
        String quantity = cartItem.getQuantity();

        //set data
        holder.txtItemTitle.setText("" + name);
        holder.txtItemPrice.setText("" + cost);
        holder.txtItemPriceEach.setText("" + price);
        holder.txtItemQuantity.setText("[" + quantity+ "]");

        //handle remove click listener, delete item from cart
        holder.txtRemoveItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                //will create table if not exits, but in that case will must exits
                EasyDB easyDB = EasyDB.init(context, "ITEM_DB")
                        .setTableName("ITEM_TABLE")
                        .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                        .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                        .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                        .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                        .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                        .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                        .doneTableColumn();

                easyDB.deleteRow(1, id);
                Toast.makeText(context, "Remove Succsess...", Toast.LENGTH_SHORT).show();
                list.remove(position);
                notifyItemChanged(position);
                notifyDataSetChanged();

                //adjust the subtotal after product remove
                int subTotalWithoutDiscount = ((ShopDetailsUser)context).allTotalPrice;
                int subTotalAfterProductRemove = subTotalWithoutDiscount - Integer.parseInt(cost.replace("$", ""));
                ((ShopDetailsUser)context).allTotalPrice = subTotalAfterProductRemove;
                ((ShopDetailsUser)context).txtSTotal.setText("$" + ((ShopDetailsUser)context).allTotalPrice);

                //once subtotal is updated...check minimum order price of promo code
                int promoPrice = Integer.parseInt(((ShopDetailsUser)context).promoPrice);
                int delivery = Integer.parseInt(((ShopDetailsUser)context).deliveryFee.replace("$", ""));

                //check if promo code applied
                if (((ShopDetailsUser)context).isPromoCodeApplied){
                    //applied
                    if (subTotalAfterProductRemove < Integer.parseInt(((ShopDetailsUser)context).promoMinimumOrderPrice)){
                        Toast.makeText(context, "This code is valid for order with minimum amount: $" + ((ShopDetailsUser)context).promoMinimumOrderPrice, Toast.LENGTH_SHORT).show();

                        ((ShopDetailsUser)context).btnApply.setVisibility(View.GONE);
                        ((ShopDetailsUser)context).txtPromoDescription.setVisibility(View.GONE);
                        ((ShopDetailsUser)context).txtPromoDescription.setText("");
                        ((ShopDetailsUser)context).txtDiscount.setText("$0");
                        ((ShopDetailsUser)context).isPromoCodeApplied = false;

                        //show new net total after delivery fee
                        ((ShopDetailsUser)context).txtTotal.setText("$" + (Integer.parseInt(String.valueOf(subTotalAfterProductRemove)) + Integer.parseInt(String.valueOf(delivery))));
                    }
                    else {
                        ((ShopDetailsUser)context).btnApply.setVisibility(View.VISIBLE);
                        ((ShopDetailsUser)context).txtPromoDescription.setVisibility(View.VISIBLE);
                        ((ShopDetailsUser)context).txtPromoDescription.setText(((ShopDetailsUser)context).promoDescription);

                        //show new total price after adding delivery fee and subtracting promo fee
                        ((ShopDetailsUser)context).isPromoCodeApplied = true;
                        ((ShopDetailsUser)context).txtTotal.setText("$" + (subTotalAfterProductRemove + delivery - promoPrice));
                    }
                }
                else {
                    //not applied
                    ((ShopDetailsUser)context).txtTotal.setText("$" + (subTotalAfterProductRemove + delivery));
                }

                //after removing item from cart, update cart count
                ((ShopDetailsUser)context).cartCount();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class HolderCartItem extends RecyclerView.ViewHolder{

        private TextView txtItemTitle, txtItemPrice, txtItemPriceEach
                , txtItemQuantity, txtRemoveItem;

        public HolderCartItem(@NonNull View itemView) {
            super(itemView);

            //init views
            txtItemTitle = itemView.findViewById(R.id.txtItemTitle);
            txtItemPrice = itemView.findViewById(R.id.txtItemPrice);
            txtItemPriceEach = itemView.findViewById(R.id.txtItemPriceEach);
            txtItemQuantity = itemView.findViewById(R.id.txtItemQuantity);
            txtRemoveItem = itemView.findViewById(R.id.txtRemoveItem);
        }



    }
}
