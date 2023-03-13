package com.ksacp2022.lifedrop;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.text.SimpleDateFormat;
import java.util.List;

public class CenterRequestListAdapter extends RecyclerView.Adapter<CenterRequestCard>{
    List<Account> accountList;
    Context context;
    FirebaseFirestore firestore;
    ProgressDialog progressDialog;

    public CenterRequestListAdapter(List<Account> accountList, Context context) {
        this.accountList = accountList;
        this.context = context;
        firestore=FirebaseFirestore.getInstance();
        progressDialog=new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

    }

    @NonNull
    @Override
    public CenterRequestCard onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.center_request_card,parent,false);

        return new CenterRequestCard(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CenterRequestCard holder, int position) {

        Account account= accountList.get(position);


        holder.name.setText(account.getFull_name());
        holder.status.setText(account.getStatus());
        SimpleDateFormat sdf=new SimpleDateFormat("dd-MM-YYY HH:mm");
        holder.created_at.setText(sdf.format(account.getCreated_at()));
        //if the account is not active hie chat button
        if(!account.getStatus().equals("Active"))
            holder.chat.setVisibility(View.GONE);

        holder.call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:"+account.getPhone()));
                    context.startActivity(intent);

            }
        });

        holder.location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                GeoPoint location= account.getLocation();

                    Intent intent = new Intent(context,NearbyCentersMapActivity. class);
                    intent.putExtra("lat",location.getLatitude());
                    intent.putExtra("lng",location.getLongitude());
                    context.startActivity(intent);

            }
        });

        holder.approve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(account.getStatus().equals("Active"))
                    return;
                progressDialog.setTitle("Approving");
                progressDialog.setMessage("Changing Status");
                progressDialog.show();
                firestore.collection("accounts")
                        .document(account.getId())
                        .update("status","Active")
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                progressDialog.dismiss();
                                accountList.remove(account);
                                CenterRequestListAdapter.this.notifyDataSetChanged();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(context,"An error occured" , Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
        holder.reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(account.getStatus().equals("Rejected"))
                    return;
                progressDialog.setTitle("Rejecting");
                progressDialog.setMessage("Changing Status");
                progressDialog.show();
                firestore.collection("accounts")
                        .document(account.getId())
                        .update("status","Rejected")
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                progressDialog.dismiss();
                                accountList.remove(account);
                                CenterRequestListAdapter.this.notifyDataSetChanged();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(context,"An error occured" , Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });

        holder.chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,ChatActivity. class);
                intent.putExtra("receiver_name",account.getFull_name());
                intent.putExtra("receiver_id",account.getId());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return accountList.size();
    }
}

class CenterRequestCard extends RecyclerView.ViewHolder{

   TextView name,created_at,status;
   AppCompatButton call,location,approve,reject,chat;


   public CenterRequestCard(@NonNull View itemView) {
       super(itemView);
       name=itemView.findViewById(R.id.center_name);
       status=itemView.findViewById(R.id.status);
       call=itemView.findViewById(R.id.call);
       chat=itemView.findViewById(R.id.chat);
       location=itemView.findViewById(R.id.location);
       created_at=itemView.findViewById(R.id.created_at);
       reject = itemView.findViewById(R.id.reject);
       approve = itemView.findViewById(R.id.approve);

   }
}
