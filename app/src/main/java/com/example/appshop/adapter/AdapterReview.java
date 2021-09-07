package com.example.appshop.adapter;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.example.appshop.R;
import com.example.appshop.model.Review;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterReview extends RecyclerView.Adapter<AdapterReview.HolderReview>{

    private Context context;
    private ArrayList<Review> list;

    public AdapterReview(Context context, ArrayList<Review> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public HolderReview onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_review, parent, false);
        return new HolderReview(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterReview.HolderReview holder, int position) {

        Review review = list.get(position);
        String ratings = review.getRatings();
        String review1 = review.getReview();
        String timestamp = review.getTimestamp();
        String uid = review.getUid();

        //set image and name

        loadUserDetail(uid, holder);

        //convert timestamp to proper format
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String formatData = DateFormat.format("dd/MM/yyyy", calendar).toString();

        //set data

        holder.txtDate.setText(formatData);
        holder.txtReview.setText(review1);
        holder.ratingBar.setRating(Float.parseFloat(ratings));
    }

    private void loadUserDetail(String uid, HolderReview holder) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get data in firebase
                        String name = "" + snapshot.child("name").getValue();
                        String image = "" + snapshot.child("profileImage").getValue();


                        //get data
                        holder.txtName.setText(name);

                        try {
                            Picasso.get().load(image).placeholder(R.drawable.ic_person_gray).into(holder.imgAvt);
                        } catch (Exception e) {
                            holder.imgAvt.setImageResource(R.drawable.ic_person_gray);
                        }
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

    class HolderReview extends RecyclerView.ViewHolder{
        private CircularImageView imgAvt;
        private TextView txtName, txtDate, txtReview;
        private RatingBar ratingBar;

        public HolderReview(@NonNull View itemView) {
            super(itemView);

            imgAvt = itemView.findViewById(R.id.imgAvt);
            txtName = itemView.findViewById(R.id.txtName);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtReview = itemView.findViewById(R.id.txtReview);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }
}
