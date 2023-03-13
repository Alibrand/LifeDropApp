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

public class UserProfileActivity extends AppCompatActivity {
    TextView user_name;
    AppCompatButton call,chat;

    FirebaseFirestore firebaseFirestore;
    String user_id;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        user_name = findViewById(R.id.center_name);
        call = findViewById(R.id.call);
        chat = findViewById(R.id.chat);

        user_id=getIntent().getStringExtra("user_id");

        firebaseFirestore=FirebaseFirestore.getInstance();
        dialog=new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage("Loading Info");
        dialog.show();

        firebaseFirestore.collection("accounts")
                .document(user_id)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        dialog.dismiss();
                        Map<String,Object> data=documentSnapshot.getData();
                        user_name.setText(data.get("full_name").toString());

                        call.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent=new Intent(Intent.ACTION_DIAL);
                                intent.setData(Uri.parse("tel:"+data.get("phone").toString()));
                                startActivity(intent);
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UserProfileActivity.this,"Failed to get info" , Toast.LENGTH_LONG).show();
                        finish();
                    }
                });

        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserProfileActivity.this,ChatActivity. class);
                intent.putExtra("receiver_name",user_name.getText().toString());
                intent.putExtra("receiver_id",user_id);
                startActivity(intent);
            }
        });



    }
}