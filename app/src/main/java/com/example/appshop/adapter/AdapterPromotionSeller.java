package com.example.appshop.adapter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appshop.R;
import com.example.appshop.activity.seller.AddPromotionCode;
import com.example.appshop.model.Promotion;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AdapterPromotionSeller extends RecyclerView.Adapter<AdapterPromotionSeller.HolderPromotionSeller>{

    private Context context;
    private ArrayList<Promotion> list;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    public AdapterPromotionSeller(Context context, ArrayList<Promotion> list) {
        this.context = context;
        this.list = list;
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderPromotionSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_promotion_seller, parent, false);
        return new HolderPromotionSeller(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterPromotionSeller.HolderPromotionSeller holder, int position) {
        //get data
        Promotion promotion = list.get(position);
        String id = promotion.getId();
        String description = promotion.getDescription();
        String expireDate = promotion.getExpireDate();
        String minimumOrderPrice = promotion.getMinimumOrderPrice();
        String promoCode = promotion.getPromoCode();
        String promoPrice = promotion.getPromoPrice();
        String timestamp = promotion.getTimestamp();

        //set data
        holder.txtDescription.setText(description);
        holder.txtPromoCode.setText("Code: " + promoCode);
        holder.txtPromoPrice.setText(promoPrice);
        holder.txtMinimumOrderPrice.setText(minimumOrderPrice);
        holder.txtDate.setText("Expire Date: " + expireDate);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editDeleteDialog(promotion, holder);
            }
        });
    }

    private void editDeleteDialog(Promotion promotion, HolderPromotionSeller holder) {
        String[] options = {"Edit", "Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose Option")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0){
                            editPromotionCode(promotion);
                        }
                        else if (which == 1){
                            deletePromotionCode(promotion);
                        }
                    }
                }).show();
    }

    private void deletePromotionCode(Promotion promotion) {
        progressDialog.setMessage("Deleting Promotion Code...");
        progressDialog.show();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Promotions").child(promotion.getId())
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(context, "Deleted successfullty", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void editPromotionCode(Promotion promotion) {
        Intent intent = new Intent(context, AddPromotionCode.class);
        intent.putExtra("promoId", promotion.getId());
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class HolderPromotionSeller extends RecyclerView.ViewHolder{
        private ImageView imgAvt;
        private TextView txtPromoCode, txtPromoPrice, txtMinimumOrderPrice,
                txtDate, txtDescription;

        public HolderPromotionSeller(@NonNull View itemView) {
            super(itemView);

            imgAvt = itemView.findViewById(R.id.imgAvt);
            txtPromoCode = itemView.findViewById(R.id.txtPromoCode);
            txtPromoPrice = itemView.findViewById(R.id.txtPromoPrice);
            txtMinimumOrderPrice = itemView.findViewById(R.id.txtMinimumOrderPrice);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtDescription = itemView.findViewById(R.id.txtDescription);
        }
    }
}
