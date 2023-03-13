package com.ksacp2022.lifedrop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class AdminHomeActivity extends AppCompatActivity {

    AppCompatButton pending_requests,active_requests,rejected_requests,logout,inbox,manage_chatbot;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);
        pending_requests = findViewById(R.id.pending_requests);
        active_requests = findViewById(R.id.active_requests);
        rejected_requests = findViewById(R.id.rejected_requests);
        logout = findViewById(R.id.logout);
        inbox = findViewById(R.id.inbox);
        manage_chatbot = findViewById(R.id.manage_chatbot);




        firebaseAuth=FirebaseAuth.getInstance();
        firestore=FirebaseFirestore.getInstance();


        pending_requests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminHomeActivity.this,AdminCentersRequestsActivity. class);
                intent.putExtra("request_status","Pending");
                startActivity(intent);
            }
        });

        active_requests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminHomeActivity.this,AdminCentersRequestsActivity. class);
                intent.putExtra("request_status","Active");
                startActivity(intent);
            }
        });
        rejected_requests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminHomeActivity.this,AdminCentersRequestsActivity. class);
                intent.putExtra("request_status","Rejected");
                startActivity(intent);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();
                Intent intent = new Intent(AdminHomeActivity.this,LoginActivity. class);
                startActivity(intent);
                finish();
            }
        });

        inbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminHomeActivity.this,InboxActivity. class);
                startActivity(intent);
            }
        });

        manage_chatbot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminHomeActivity.this,AdminManageChatbotActivity. class);
                startActivity(intent);
            }
        });





    }

    private  void  check_new_messages(){
        inbox.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
        firestore.collection("inbox")
                .whereArrayContains("users_ids",firebaseAuth.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot doc:queryDocumentSnapshots.getDocuments()) {
                            doc.getReference()
                                    .collection("messages")
                                    .whereEqualTo("to",firebaseAuth.getUid())
                                    .whereEqualTo("status","unseen")
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if(task.isSuccessful())
                                            {
                                                int new_messages_count= task.getResult().getDocuments().size();
                                                if(new_messages_count>0)
                                                {
                                                    inbox.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_mark_unread_chat_alt_24,0,0,0);
                                                    Toast.makeText(AdminHomeActivity.this, "You have new messages..Check your Inbox", Toast.LENGTH_LONG).show();
                                                }

                                            }

                                        }
                                    });
                        }
                    }
                });

    }

    @Override
    protected void onResume() {
        super.onResume();
        check_new_messages();
    }
}