package com.ksacp2022.lifedrop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
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
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyDonationRequestsActivity extends AppCompatActivity {

    RecyclerView requests_list;
    ImageView back;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_donation_requests);
        requests_list = findViewById(R.id.requests_list);
        back = findViewById(R.id.back);


        firebaseAuth=FirebaseAuth.getInstance();
        firestore=FirebaseFirestore.getInstance();


        String uid=firebaseAuth.getUid();

        dialog=new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage("Loading");
        dialog.show();

        firestore.collection("donation_appointments")
                .whereEqualTo("donor_id",uid)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        dialog.dismiss();
                        List<DonationAppointment> donationAppointments=new ArrayList<>();
                        for (DocumentSnapshot doc:queryDocumentSnapshots.getDocuments()
                        ) {
                            Map<String,Object> data=doc.getData();
                            DonationAppointment donationAppointment=new DonationAppointment();
                            donationAppointment.setCenter_name(data.get("center_name").toString());
                            donationAppointment.setBlood_group(data.get("blood_group").toString());
                            Timestamp timestamp = (Timestamp) data.get("estimated_date");
                            donationAppointment.setEstimated_date(timestamp.toDate());
                            donationAppointment.setId(doc.getId());
                            donationAppointment.setStatus(data.get("status").toString());
                            donationAppointments.add(donationAppointment);


                        }




                        MyRequestsListAdapter adapter=new MyRequestsListAdapter(donationAppointments,MyDonationRequestsActivity.this);
                        requests_list.setAdapter(adapter);


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        Toast.makeText(MyDonationRequestsActivity.this,"Failted to load requests" , Toast.LENGTH_LONG).show();

                    }
                });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });




    }
}