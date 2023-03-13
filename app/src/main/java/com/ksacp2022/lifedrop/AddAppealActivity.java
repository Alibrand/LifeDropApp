package com.ksacp2022.lifedrop;

import static android.widget.Toast.*;
import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.makeText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.ProgressDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class AddAppealActivity extends AppCompatActivity {



    GeoPoint cur_location;
    EditText full_name,content,blood_group;
    AppCompatButton set_location,post;

    FirebaseFirestore firestore;
    FirebaseAuth firebaseAuth;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_appeal);
        full_name = findViewById(R.id.full_name);
        content = findViewById(R.id.content);
        blood_group = findViewById(R.id.blood_group);

        set_location = findViewById(R.id.set_location);
        post = findViewById(R.id.post);

        firestore=FirebaseFirestore.getInstance();
        firebaseAuth=FirebaseAuth.getInstance();

        progressDialog=new ProgressDialog(AddAppealActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);


        progressDialog.setMessage("Loading info");
        progressDialog.show();

        firestore.collection("accounts")
                        .document(firebaseAuth.getUid())
                                .get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                progressDialog.dismiss();
                                                Map<String,Object> data=documentSnapshot.getData();
                                                blood_group.setText(data.get("blood_group").toString());
                                                full_name.setText(data.get("full_name").toString());
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        makeText(AddAppealActivity.this,"Failed to get user info" , LENGTH_LONG).show();
                        finish();
                    }
                });








        set_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(AddAppealActivity.this,ChooseLocationActivity.class);
                startActivityForResult(intent,110);
            }
        });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name=full_name.getText().toString();
                String post_content=content.getText().toString();
                String blood_g=blood_group.getText().toString();



                if(post_content.isEmpty())
                {
                    content.setError("Empty Field");
                    return;
                }
                if(cur_location==null)
                {
                    makeText(AddAppealActivity.this,"You should select your location" , LENGTH_LONG).show();
                    return;
                }

                TimeZone tz = TimeZone.getTimeZone("GMT+03:00");
                Calendar calendar=Calendar.getInstance();
                calendar.setTimeZone(tz);
                Date created_at=calendar.getTime();

                Appeal appeal=new Appeal();
                appeal.setCreated_at(created_at);
                appeal.setContent(post_content);
                appeal.creator_id=firebaseAuth.getUid();
                appeal.setLocation(cur_location);
                appeal.setCreator_name(name);
                appeal.setBlood_group(blood_g);




                progressDialog.setMessage("Saving");
                progressDialog.show();


                firestore.collection("appeals")
                        .add(appeal)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                 //create notification for appeal
                                Notification notification=new Notification();
                                notification.setFrom(firebaseAuth.getUid());
                                notification.setTitle("Appeal from "+full_name.getText().toString());
                                notification.setContent(content.getText().toString());
                                notification.setType("Appeal");
                                GeoPoint location=new GeoPoint(cur_location.getLatitude(),cur_location.getLongitude());
                                notification.setLocation(location);

                                Map<String,Boolean> to=new HashMap<>();

                                //get all users in firestore
                                firestore.collection("accounts")
                                        .whereNotEqualTo("status","Rejected")
                                                .get()
                                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                                for (DocumentSnapshot doc:queryDocumentSnapshots.getDocuments()
                                                                     ) {
                                                                    //exclude appeal's owner
                                                                    if(doc.getId().equals(firebaseAuth.getUid()))
                                                                        continue;

                                                                    to.put(doc.getId(), false);
                                                                }

                                                                notification.setTo(to);

                                                                firestore.collection("notifications")
                                                                        .add(notification).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                            @Override
                                                                            public void onSuccess(DocumentReference documentReference) {
                                                                                makeText(AddAppealActivity.this,"Your appeal has been published successfully" , LENGTH_LONG).show();
                                                                                progressDialog.dismiss();

                                                                                finish();
                                                                            }
                                                                        });

                                                            }
                                                        });








                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                makeText(AddAppealActivity.this,"Failed to save appeal" , LENGTH_LONG).show();
                                progressDialog.dismiss();
                            }
                        });



            }
        });






    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==110)
            if(resultCode==RESULT_OK)
            {
                double latitude= (double) data.getSerializableExtra("lat");
                double longitude= (double) data.getSerializableExtra("long");
                makeText(AddAppealActivity.this,"Location set successfully", LENGTH_LONG).show();
                cur_location=new GeoPoint(latitude,longitude);
            }
    }
}