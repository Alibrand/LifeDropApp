package com.ksacp2022.lifedrop;

import static android.widget.Toast.*;
import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.makeText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class AppointmentReviewActivity extends AppCompatActivity {
    TextView donor_name,blood_group,estimated_date,appointment_status;
    AppCompatButton confirm_button,done_button,reject_button,profile,location;
    EditText notes;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    ProgressDialog progressDialog;
    String status;
    String donor_id;
    Timestamp timestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_review);
        donor_name = findViewById(R.id.donor_name);
        blood_group = findViewById(R.id.blood_group);
        estimated_date = findViewById(R.id.estimated_date);
        confirm_button = findViewById(R.id.confirm_button);
        reject_button = findViewById(R.id.reject_button);
        profile = findViewById(R.id.profile);
        location = findViewById(R.id.location);
        notes = findViewById(R.id.notes);
        appointment_status = findViewById(R.id.status);
        done_button = findViewById(R.id.done_button);




        String request_id=getIntent().getStringExtra("request_id");


        firebaseAuth=FirebaseAuth.getInstance();
        firestore=FirebaseFirestore.getInstance();
        progressDialog=new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Loading");
        progressDialog.show();


        firestore.collection("donation_appointments")
                .document(request_id)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        progressDialog.dismiss();
                        Map<String,Object> data=documentSnapshot.getData();
                        donor_name.setText(data.get("donor_name").toString());
                        blood_group.setText(data.get("blood_group").toString());
                         timestamp= (Timestamp) data.get("estimated_date");
                        donor_id= data.get("donor_id").toString();
                        SimpleDateFormat sdf=new SimpleDateFormat("dd-MM-YYYY");
                        estimated_date.setText(sdf.format(timestamp.toDate()));
                        status=data.get("status").toString();
                        notes.setText(data.get("notes").toString());
                        appointment_status.setText(status);

                        if(status.equals("Done"))
                        {
                            makeText(AppointmentReviewActivity.this,"This donation Request was Done" , LENGTH_LONG).show();
                        finish();
                        }



                        location.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                GeoPoint location= (GeoPoint) data.get("location");
                                Intent intent = new Intent(AppointmentReviewActivity.this,NearByDonorsActivity. class);
                                intent.putExtra("lat",location.getLatitude());
                                intent.putExtra("lng",location.getLongitude());
                                 startActivity(intent);
                            }
                        });

                        profile.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                    Intent intent = new Intent(AppointmentReviewActivity.this,UserProfileActivity. class);
                                    intent.putExtra("user_id",data.get("donor_id").toString());
                                    startActivity(intent);


                            }
                        });

                        confirm_button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if(status.equals("Confirmed"))
                                {
                                    makeText(AppointmentReviewActivity.this,"Request is already Confirmed" , LENGTH_LONG).show();
                                return;
                                }
                                progressDialog.setMessage("Changing Status");
                                progressDialog.show();
                                String request_notes=notes.getText().toString();
                                firestore.collection("donation_appointments")
                                        .document(request_id)
                                        .update("status","Confirmed","notes",request_notes)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Notification notification=new Notification();
                                                notification.setFrom(firebaseAuth.getUid());
                                                Map<String,Boolean> to=new HashMap<>();
                                                to.put(data.get("donor_id").toString(),false);
                                                notification.setTo(to);
                                                notification.setTitle("From "+data.get("center_name"));
                                                notification.setContent("Your Appointment has been confirmed..check your requests\n"+request_notes);
                                                notification.setType("Appointment Confirmation");
                                                notification.setLocation((GeoPoint) data.get("location"));

                                                publish_notification(notification);

                                                status="Confirmed";


                                            }


                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                makeText(AppointmentReviewActivity.this,"Failed to confirm request" , LENGTH_LONG).show();
                                            }
                                        });
                            }
                        });

                        reject_button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if(status.equals("Rejected"))
                                {
                                    makeText(AppointmentReviewActivity.this,"Request is already Rejected" , LENGTH_LONG).show();
                                    return;
                                }
                                progressDialog.setMessage("Changing Status");
                                progressDialog.show();

                                                String request_notes=notes.getText().toString();
                                                firestore.collection("donation_appointments")
                                                        .document(request_id)
                                                        .update("status","Rejected","notes",request_notes)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                Notification notification=new Notification();
                                                                notification.setFrom(firebaseAuth.getUid());
                                                                Map<String,Boolean> to=new HashMap<>();
                                                                to.put(data.get("donor_id").toString(),false);
                                                                notification.setTo(to);
                                                                notification.setTitle("From "+data.get("center_name"));
                                                                notification.setContent("Your Appointment has been rejected..\n"+request_notes);
                                                                notification.setType("Appointment Rejected");
                                                                notification.setLocation((GeoPoint) data.get("location"));

                                                                publish_notification(notification);
                                                                status="Rejected";


                                                            }


                                                        }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                progressDialog.dismiss();
                                                                makeText(AppointmentReviewActivity.this,"Failed to confirm request" , LENGTH_LONG).show();
                                                            }
                                                        });


                            }
                        });

                        done_button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if(status.equals("Done"))
                                {
                                    makeText(AppointmentReviewActivity.this,"Request is already Done" , LENGTH_LONG).show();
                                    return;
                                }
                                progressDialog.setMessage("Changing Status");
                                progressDialog.show();

                                                String request_notes=notes.getText().toString();
                                                firestore.collection("donation_appointments")
                                                        .document(request_id)
                                                        .update("status","Done","notes",request_notes)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                Notification notification=new Notification();
                                                                notification.setFrom(firebaseAuth.getUid());
                                                                Map<String,Boolean> to=new HashMap<>();
                                                                to.put(data.get("donor_id").toString(),false);
                                                                notification.setTo(to);
                                                                notification.setTitle("From "+data.get("center_name"));
                                                                notification.setContent("Thanks for your donation");
                                                                notification.setType("Donation Done");
                                                                notification.setLocation((GeoPoint) data.get("location"));

                                                                publish_notification(notification);
                                                                status="Done";

                                                                update_last_donation();


                                                            }


                                                        }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                progressDialog.dismiss();
                                                                makeText(AppointmentReviewActivity.this,"Failed to confirm request" , LENGTH_LONG).show();
                                                            }
                                                        });


                            }
                        });


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        makeText(AppointmentReviewActivity.this,"Failed to load request" , LENGTH_LONG).show();
                        finish();
                    }
                });







    }

    private void update_last_donation(){
        firestore.collection("accounts")
                .document(donor_id)
                .update("last_donation_date",timestamp)
                ;

    }

    private void publish_notification(Notification notification) {
        firestore.collection("notifications")
                .add(notification)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        progressDialog.dismiss();
                        makeText(AppointmentReviewActivity.this,"A notification has been sent to Donor" , LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        makeText(AppointmentReviewActivity.this,"Failed to publish a notification" , LENGTH_LONG).show();

                    }
                });
    }
}