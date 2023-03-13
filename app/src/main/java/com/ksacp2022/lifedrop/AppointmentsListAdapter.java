package com.ksacp2022.lifedrop;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.GeoPoint;

import java.text.SimpleDateFormat;
import java.util.List;

public class AppointmentsListAdapter extends RecyclerView.Adapter<AppointmentCard>{
    List<DonationAppointment> donationAppointmentList;
    Context context;

    public AppointmentsListAdapter(List<DonationAppointment> notificationList, Context context) {
        this.donationAppointmentList = notificationList;
        this.context = context;
    }

    @NonNull
    @Override
    public AppointmentCard onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.donation_appointment_card,parent,false);

        return new AppointmentCard(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentCard holder, int position) {

        DonationAppointment donationAppointment= donationAppointmentList.get(position);

        holder.donor_name.setText(donationAppointment.getDonor_name());
        holder.blood_group.setText(donationAppointment.getBlood_group());
        SimpleDateFormat smp=new SimpleDateFormat("dd-MM-YYYY");
        holder.estimated_date.setText(smp.format(donationAppointment.getEstimated_date()));

        holder.status.setText(donationAppointment.getStatus());

        holder.review.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context,AppointmentReviewActivity. class);
                intent.putExtra("request_id",donationAppointment.getId());
                context.startActivity(intent);

            }
        });



    }

    @Override
    public int getItemCount() {
        return donationAppointmentList.size();
    }
}

class AppointmentCard extends RecyclerView.ViewHolder{

   TextView donor_name, blood_group, estimated_date,status;
   AppCompatButton review;


   public AppointmentCard(@NonNull View itemView) {
       super(itemView);
       estimated_date =itemView.findViewById(R.id.estimated_date);
       donor_name =itemView.findViewById(R.id.donor_name);
       blood_group =itemView.findViewById(R.id.blood_group);
       review =itemView.findViewById(R.id.review);
       status =itemView.findViewById(R.id.status);
   }
}
