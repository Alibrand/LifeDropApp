package com.ksacp2022.lifedrop;

import static android.widget.Toast.*;
import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.makeText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Map;

public class BookAppointmentActivity extends AppCompatActivity {

    AppCompatButton send;
    TextView center_name,full_name,blood_group;
    DatePicker estimated_date;
    ImageView back;

    GeoPoint donor_location;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_appointment);
        send = findViewById(R.id.send);
        center_name = findViewById(R.id.center_name);
        full_name = findViewById(R.id.full_name);
        blood_group = findViewById(R.id.blood_group);
        estimated_date = findViewById(R.id.estimated_date);
        back = findViewById(R.id.back);


        //get center name and id from previous activity
        String centername=getIntent().getStringExtra("center_name");
        center_name.setText(centername);
        String center_id=getIntent().getStringExtra("center_id");

        firebaseAuth=FirebaseAuth.getInstance();
        String uid= firebaseAuth.getUid();
        firestore=FirebaseFirestore.getInstance();


        progressDialog=new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Loading Info");
        progressDialog.show();

        firestore.collection("accounts")
                .document(uid)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        progressDialog.dismiss();
                        Map<String,Object> data=documentSnapshot.getData();
                        full_name.setText(data.get("full_name").toString());
                        blood_group.setText(data.get("blood_group").toString());
                        donor_location= (GeoPoint) data.get("location");


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        makeText(BookAppointmentActivity.this,"Failed to get Info" , LENGTH_LONG).show();
                        finish();
                    }
                });


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int year=estimated_date.getYear();
                int month=estimated_date.getMonth();
                int day=estimated_date.getDayOfMonth();

                String donor_blood_group=blood_group.getText().toString();
                String donor_name=full_name.getText().toString();

                Calendar calendar=Calendar.getInstance();
                calendar.set(year,month,day);




                DonationAppointment donationAppointment=new DonationAppointment();
                donationAppointment.setBlood_group(donor_blood_group);
                donationAppointment.setDonor_name(donor_name);
                donationAppointment.setCreated_at(null);
                donationAppointment.setEstimated_date(calendar.getTime());
                donationAppointment.setDonor_id(uid);
                donationAppointment.setCenter_id(center_id);
                donationAppointment.setCenter_name(centername);
                donationAppointment.setLocation(donor_location);


                progressDialog.setMessage("Checking Request");
                progressDialog.show();

                firestore.collection("donation_appointments")
                        .whereEqualTo("donor_id",uid)
                        .whereEqualTo("center_id",center_id)
                        .whereNotIn("status", Arrays.asList("Done","Rejected"))
                        .get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        if(queryDocumentSnapshots.getDocuments().size()>0)
                                        {
                                            progressDialog.dismiss();
                                            makeText(BookAppointmentActivity.this,"You have already a pending request in this center" , LENGTH_LONG).show();
                                        }else{
                                            progressDialog.setMessage("Sending");
                                            progressDialog.show();

                                            firestore.collection("donation_appointments")
                                                    .add(donationAppointment)
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                        @Override
                                                        public void onSuccess(DocumentReference documentReference) {
                                                            makeText(BookAppointmentActivity.this,"Appointment was booked successfully..wait for confirmation" , LENGTH_LONG).show();
                                                            finish();

                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            makeText(BookAppointmentActivity.this,"Failed to save appointment" , LENGTH_LONG).show();
                                                            progressDialog.dismiss();
                                                        }
                                                    });
                                        }

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                makeText(BookAppointmentActivity.this,"Failed to save appointment" , LENGTH_LONG).show();
                                progressDialog.dismiss();
                            }
                        });





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