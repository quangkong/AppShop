package com.example.appshop.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appshop.R;
import com.example.appshop.activity.seller.FilterOrderSeller;
import com.example.appshop.activity.seller.FilterProductSeller;
import com.example.appshop.activity.seller.OrderDetailsSeller;
import com.example.appshop.activity.user.OrderDatailsUser;
import com.example.appshop.model.OrderSeller;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterOrderSeller extends RecyclerView.Adapter<AdapterOrderSeller.HolderOrderSeller> implements Filterable {

    private Context context;
    public ArrayList<OrderSeller> listOrder, listFilterOrder;
    private FilterOrderSeller filter;

    public AdapterOrderSeller(Context context, ArrayList<OrderSeller> listOrder) {
        this.context = context;
        this.listOrder = listOrder;
        this.listFilterOrder = listOrder;
    }

    @NonNull
    @Override
    public HolderOrderSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_order_seller, parent, false);
        return new HolderOrderSeller(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterOrderSeller.HolderOrderSeller holder, int position) {

        //get data
        OrderSeller orderSeller = listOrder.get(position);
        String orderId = orderSeller.getOrderId();
        String orderAddress = orderSeller.getOrderAddress();
        String orderBy = orderSeller.getOrderBy();
        String orderCost = orderSeller.getOrderCost();
        String orderStatus = orderSeller.getOrderStatus();
        String orderTime = orderSeller.getOrderTime();
        String orderTo = orderSeller.getOrderTo();
        String orderSellerDeliveryFee = orderSeller.getDeliveryFee();

        //load user
        loadUserInfo(orderSeller, holder);
        //set data
        holder.txtAmount.setText("Amount: $" + orderCost);
        holder.txtStatus.setText(orderStatus);
        holder.txtOrder.setText("OrderID: "+orderId);
        if(orderStatus.equals("In Progress")){
            holder.txtStatus.setTextColor(context.getResources().getColor(R.color.colorPrimary));
        }
        else if(orderStatus.equals("Completed")) {
            holder.txtStatus.setTextColor(context.getResources().getColor(R.color.colorGreen));
        }
        else if(orderStatus.equals("Cancelled")) {
            holder.txtStatus.setTextColor(context.getResources().getColor(R.color.colorRed));
        }

        //convert timetamp to proper format
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(orderTime));
        String formatedData = DateFormat.format("dd/MM/yyyy", calendar).toString();

        holder.txtDate.setText(formatedData);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open order details
                Intent intent = new Intent(context, OrderDetailsSeller.class);
                intent.putExtra("orderBy", orderBy);  //to load order info
                intent.putExtra("orderId", orderId);  //to load info of the user who placed order
                context.startActivity(intent);
            }
        });
    }

    private void loadUserInfo(OrderSeller orderSeller, HolderOrderSeller holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(orderSeller.getOrderTo())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String shopName = "" + snapshot.child("email").getValue();
                        holder.txtEmail.setText(shopName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public Filter getFilter() {
        if(filter == null){
            filter = new FilterOrderSeller(this, listFilterOrder);
        }
        return filter;
    }

    @Override
    public int getItemCount() {
        return listOrder.size();
    }

    class HolderOrderSeller extends RecyclerView.ViewHolder{
        private TextView txtOrder, txtDate, txtEmail, txtAmount, txtStatus;
        public HolderOrderSeller(@NonNull View itemView) {
            super(itemView);

            txtOrder = itemView.findViewById(R.id.txtOrder);
            txtAmount = itemView.findViewById(R.id.txtAmount);
            txtEmail = itemView.findViewById(R.id.txtEmail);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtStatus = itemView.findViewById(R.id.txtStatus);
        }
    }
}
