package com.example.appshop.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appshop.R;
import com.example.appshop.model.OrderedItemUser;

import java.util.ArrayList;

public class AdapterOrderedItem extends RecyclerView.Adapter<AdapterOrderedItem.HolderOrderedItem>{

    private Context context;
    private ArrayList<OrderedItemUser> list;

    public AdapterOrderedItem(Context context, ArrayList<OrderedItemUser> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public HolderOrderedItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_ordered_item, parent, false);
        return new HolderOrderedItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterOrderedItem.HolderOrderedItem holder, int position) {
        OrderedItemUser orderedItemUser = list.get(position);
        String cost = orderedItemUser.getCost();
        String name = orderedItemUser.getName();
        String price = orderedItemUser.getPrice();
        String quantity = orderedItemUser.getQuantity();

        //set data
        holder.txtItemPriceEach.setText("$" + price);
        holder.txtPriceItem.setText("$" + cost);
        holder.txtTitleItem.setText(name);
        holder.txtItemQuantity.setText("[" + quantity + "]");
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class HolderOrderedItem extends RecyclerView.ViewHolder{

        private TextView txtTitleItem, txtPriceItem, txtItemPriceEach, txtItemQuantity;

        public HolderOrderedItem(@NonNull View itemView) {
            super(itemView);

            //init views
            txtItemPriceEach = itemView.findViewById(R.id.txtItemPriceEach);
            txtTitleItem = itemView.findViewById(R.id.txtTitleItem);
            txtPriceItem = itemView.findViewById(R.id.txtPriceItem);
            txtItemQuantity = itemView.findViewById(R.id.txtItemQuantity);
        }
    }
}
