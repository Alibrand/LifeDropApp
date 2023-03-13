package com.ksacp2022.lifedrop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class NotificationsActivity extends AppCompatActivity {

    RecyclerView notifications_list;
    TextView clear_all;
    ImageView back;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        notifications_list = findViewById(R.id.notifications_list);
        clear_all = findViewById(R.id.clear_all);
        back = findViewById(R.id.back);


        firebaseAuth=FirebaseAuth.getInstance();
        firestore=FirebaseFirestore.getInstance();


        String uid=firebaseAuth.getUid();

        dialog=new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage("Loading");
        dialog.show();

        firestore.collection("notifications")
                .orderBy("to."+uid)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        dialog.dismiss();
                        List<Notification> notificationList=new ArrayList<>();
                        for (DocumentSnapshot doc:queryDocumentSnapshots.getDocuments()
                             ) {
                            Notification notification=doc.toObject(Notification.class);
                            Map<String,Object> data=doc.getData();
//                            Notification notification=new Notification();
//                            notification.setFrom(data.get("from").toString());
//                            notification.setTitle(data.get("title").toString());
//                            notification.setContent(data.get("content").toString());
//                            notification.setLocation((GeoPoint) data.get("location"));
//                            notification.setType(data.get("type").toString());
//                            Timestamp created_at= (Timestamp) data.get("created_at");
//                            notification.setCreated_at(created_at.toDate());
                            notificationList.add(notification);

                            firestore.collection("notifications")
                                    .document(doc.getId())
                                    .update("to."+uid,true);


                        }


                       notificationList.sort((n1,n2)-> n2.getCreated_at().compareTo(n1.getCreated_at()));


                        NotificationsListAdapter adapter=new NotificationsListAdapter(notificationList,NotificationsActivity.this);
                        notifications_list.setAdapter(adapter);


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        Toast.makeText(NotificationsActivity.this,"Failted to load notificatios" , Toast.LENGTH_LONG).show();

                    }
                });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        clear_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firestore.collection("notifications")
                        .orderBy("to."+uid)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                dialog.dismiss();
                                List<Notification> notificationList=new ArrayList<>();
                                for (DocumentSnapshot doc:queryDocumentSnapshots.getDocuments()
                                ) {
                                    //delete uid from to list
                                    firestore.collection("notifications")
                                            .document(doc.getId())
                                            .update("to."+uid, FieldValue.delete());


                                }
                                //clear list
                               notifications_list.setAdapter(null);





                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
            }
        });

    }


}