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

import java.text.SimpleDateFormat;
import java.util.List;

public class MyRequestsListAdapter extends RecyclerView.Adapter<MyRequestCard>{
    List<DonationAppointment> donationAppointmentList;
    Context context;

    public MyRequestsListAdapter(List<DonationAppointment> notificationList, Context context) {
        this.donationAppointmentList = notificationList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyRequestCard onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.my_request_card,parent,false);

        return new MyRequestCard(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyRequestCard holder, int position) {

        DonationAppointment donationAppointment= donationAppointmentList.get(position);

        holder.center_name.setText(donationAppointment.getCenter_name());
        holder.blood_group.setText(donationAppointment.getBlood_group());
        SimpleDateFormat smp=new SimpleDateFormat("dd-MM-YYYY");
        holder.estimated_date.setText(smp.format(donationAppointment.getEstimated_date()));

        holder.status.setText(donationAppointment.getStatus());

        holder.qr_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context,MyRequestQRActivity. class);
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

class MyRequestCard extends RecyclerView.ViewHolder{

   TextView center_name, blood_group, estimated_date,status;
   AppCompatButton qr_code;


   public MyRequestCard(@NonNull View itemView) {
       super(itemView);
       estimated_date =itemView.findViewById(R.id.estimated_date);
       center_name =itemView.findViewById(R.id.center_name);
       blood_group =itemView.findViewById(R.id.blood_group);
       qr_code =itemView.findViewById(R.id.qr_code);
       status =itemView.findViewById(R.id.status);
   }
}
