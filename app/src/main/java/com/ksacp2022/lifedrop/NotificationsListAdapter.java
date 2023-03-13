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

public class NotificationsListAdapter extends RecyclerView.Adapter<NotificationCard>{
    List<Notification> notificationList;
    Context context;

    public NotificationsListAdapter(List<Notification> notificationList, Context context) {
        this.notificationList = notificationList;
        this.context = context;
    }

    @NonNull
    @Override
    public NotificationCard onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_card,parent,false);

        return new NotificationCard(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationCard holder, int position) {

        Notification notification= notificationList.get(position);

        holder.title.setText(notification.getTitle());
        holder.content.setText(notification.getContent());
        holder.type.setText(notification.getType());
        SimpleDateFormat sdf=new SimpleDateFormat("dd-MM-YYY HH:mm");
        holder.created_at.setText(sdf.format(notification.getCreated_at()));

        holder.profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(notification.getType().equals("Appeal"))
                {
                Intent intent = new Intent(context,UserProfileActivity. class);
                intent.putExtra("user_id",notification.getFrom());
                context.startActivity(intent);
                }
                else{
                    Intent intent = new Intent(context,CenterProfileActivity. class);
                    intent.putExtra("center_id",notification.getFrom());
                    context.startActivity(intent);
                }
            }
        });

        holder.location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                GeoPoint location= notification.getLocation();
                if(notification.getType().equals("Appeal"))
                {
                Intent intent = new Intent(context,AppealsMapActivity. class);
                intent.putExtra("lat",location.getLatitude());
                intent.putExtra("lng",location.getLongitude());
                context.startActivity(intent);
                }
                else if( notification.getType().startsWith("Blood Need"))
                {
                    Intent intent = new Intent(context,NearbyCentersMapActivity. class);
                    intent.putExtra("lat",location.getLatitude());
                    intent.putExtra("lng",location.getLongitude());
                    context.startActivity(intent);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }
}

 class NotificationCard extends RecyclerView.ViewHolder{

    TextView title,content,type,created_at;
    AppCompatButton profile,location;


    public NotificationCard(@NonNull View itemView) {
        super(itemView);
        type=itemView.findViewById(R.id.type);
        title=itemView.findViewById(R.id.title);
        content=itemView.findViewById(R.id.content);
        profile=itemView.findViewById(R.id.profile);
        location=itemView.findViewById(R.id.location);
        created_at=itemView.findViewById(R.id.created_at);
    }
}
