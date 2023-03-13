package com.ksacp2022.lifedrop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class CenterProfileActivity extends AppCompatActivity {

    TextView center_name;
    AppCompatButton call,chat,book_donation;

    FirebaseFirestore firebaseFirestore;
    String center_id;
    String centername;
    ProgressDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_center_profile);
        center_name = findViewById(R.id.center_name);
        call = findViewById(R.id.call);
        chat = findViewById(R.id.chat);
        book_donation = findViewById(R.id.book_donation);


        center_id=getIntent().getStringExtra("center_id");

        firebaseFirestore=FirebaseFirestore.getInstance();
        dialog=new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage("Loading Info");
        dialog.show();

        firebaseFirestore.collection("accounts")
                .document(center_id)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        dialog.dismiss();
                        Map<String,Object> data=documentSnapshot.getData();
                        center_name.setText(data.get("full_name").toString());
                        centername=data.get("full_name").toString();
                        call.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent=new Intent(Intent.ACTION_DIAL);
                                intent.setData(Uri.parse("tel:"+data.get("phone").toString()));
                                startActivity(intent);
                            }
                        });

                        book_donation.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(CenterProfileActivity.this,CheckEligibilityActivity. class);
                                intent.putExtra("center_name",centername);
                                intent.putExtra("center_id",center_id);
                                startActivity(intent);
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CenterProfileActivity.this,"Failed to get info" , Toast.LENGTH_LONG).show();
                    finish();
                    }
                });

        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CenterProfileActivity.this,ChatActivity. class);
                intent.putExtra("receiver_name",centername);
                intent.putExtra("receiver_id",center_id);
                startActivity(intent);
            }
        });





    }
}