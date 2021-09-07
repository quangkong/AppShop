package com.example.appshop.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appshop.R;
import com.example.appshop.activity.user.OrderDatailsUser;
import com.example.appshop.model.OrderUser;
import com.example.appshop.ultil.Database;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterOrderUser extends RecyclerView.Adapter<AdapterOrderUser.HolderOrderUser>{

    private Context context;
    private ArrayList<OrderUser> list;

    public AdapterOrderUser(Context context, ArrayList<OrderUser> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public HolderOrderUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_order_user, parent, false);
        return new HolderOrderUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterOrderUser.HolderOrderUser holder, int position) {
        //get data
        OrderUser orderUser = list.get(position);
        
        //get shop info
        loadShopInfo(orderUser, holder);

        String orderBy = orderUser.getOrderBy();
        String orderCost = orderUser.getOrderCost();
        String orderId = orderUser.getOrderId();
        String orderStatus = orderUser.getOrderStatus();
        String orderTime = orderUser.getOrderTime();
        String orderTo = orderUser.getOrderTo();

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
                Intent intent = new Intent(context, OrderDatailsUser.class);
                intent.putExtra("orderTo", orderTo);
                intent.putExtra("orderId", orderId);
                context.startActivity(intent);
            }
        });
    }

    private void loadShopInfo(OrderUser orderUser, HolderOrderUser holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(orderUser.getOrderTo())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String shopName = "" + snapshot.child("shopName").getValue();
                        holder.txtShopName.setText(shopName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class HolderOrderUser extends RecyclerView.ViewHolder{
        private TextView txtOrder, txtDate, txtShopName, txtAmount, txtStatus;

        public HolderOrderUser(@NonNull View itemView) {
            super(itemView);

            //init views
            txtAmount = itemView.findViewById(R.id.txtStatus);
            txtOrder = itemView.findViewById(R.id.txtOrder);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtShopName = itemView.findViewById(R.id.txtShopName);
            txtStatus = itemView.findViewById(R.id.txtStatus);
        }
    }
}
