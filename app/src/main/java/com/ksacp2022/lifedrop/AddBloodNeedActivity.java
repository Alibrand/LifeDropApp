package com.ksacp2022.lifedrop;

import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.makeText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.ProgressDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

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

public class AddBloodNeedActivity extends AppCompatActivity {
    GeoPoint cur_location;
    EditText center_name,content;
    Spinner blood_group;
    AppCompatButton post;
    String[] blood_groups={"A+","B+","AB+","O+","A-","B-","AB-","O-"};
    FirebaseFirestore firestore;
    FirebaseAuth firebaseAuth;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_blood_need);
        center_name = findViewById(R.id.full_name);
        content = findViewById(R.id.content);
        blood_group = findViewById(R.id.blood_group);

        ArrayAdapter adapter=new ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,blood_groups);
        blood_group.setAdapter(adapter);
        post = findViewById(R.id.post);

        firestore=FirebaseFirestore.getInstance();
        firebaseAuth=FirebaseAuth.getInstance();

        progressDialog=new ProgressDialog(this);
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
                        center_name.setText(data.get("full_name").toString());
                        cur_location= (GeoPoint) data.get("location");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        makeText(AddBloodNeedActivity.this,"Failed to get center info" , LENGTH_LONG).show();
                        finish();
                    }
                });





        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name= center_name.getText().toString();
                String post_content=content.getText().toString();
                String blood_g=blood_group.getSelectedItem().toString();



                if(post_content.isEmpty())
                {
                    content.setError("Empty Field");
                    return;
                }





                progressDialog.setMessage("Posting");
                progressDialog.show();



                                //create notification for blood need
                                Notification notification=new Notification();
                                notification.setFrom(firebaseAuth.getUid());
                                notification.setTitle( name);
                                notification.setContent(content.getText().toString());
                                notification.setType("Blood Need "+blood_g);
                                notification.setLocation(cur_location);
                                Map<String,Boolean> to=new HashMap<>();

                                //get all users in firestore
                                firestore.collection("accounts")
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
                                                                makeText(AddBloodNeedActivity.this,"A notification has been sent to all users" , LENGTH_LONG).show();
                                                                progressDialog.dismiss();

                                                                finish();
                                                            }
                                                        });

                                            }
                                        });













            }
        });
    }
}