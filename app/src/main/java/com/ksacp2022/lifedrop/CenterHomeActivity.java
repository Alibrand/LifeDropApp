package com.ksacp2022.lifedrop;

import static android.widget.Toast.makeText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class CenterHomeActivity extends AppCompatActivity {

    TextView user_name,notifications_count;
    AppCompatButton inbox,logout,post_need,browse_map,donation_appointments,nearby_donors,chatbot;
    ImageView notifications;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_center_home);
        user_name = findViewById(R.id.user_name);
        logout = findViewById(R.id.logout);
        post_need = findViewById(R.id.post_need);
        browse_map = findViewById(R.id.browse_map);
        notifications_count = findViewById(R.id.notifications_count);
        notifications = findViewById(R.id.notifications);
        donation_appointments = findViewById(R.id.donation_appointments);
        nearby_donors = findViewById(R.id.nearby_donors);
        inbox = findViewById(R.id.inbox);
        chatbot = findViewById(R.id.chatbot);








        firebaseAuth=FirebaseAuth.getInstance();
        firestore=FirebaseFirestore.getInstance();


        Intent intent=getIntent();
        String full_name=intent.getStringExtra("full_name");

        user_name.setText(full_name);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();
                Intent intent = new Intent(CenterHomeActivity.this,LoginActivity. class);
                startActivity(intent);
                finish();
            }
        });
        post_need.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CenterHomeActivity.this,AddBloodNeedActivity. class);
                startActivity(intent);
            }
        });

        browse_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CenterHomeActivity.this,AppealsMapActivity. class);
                startActivity(intent);
            }
        });

        donation_appointments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CenterHomeActivity.this,DonationAppointmentsActivity. class);
                startActivity(intent);
            }
        });

        notifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CenterHomeActivity.this,NotificationsActivity. class);
                startActivity(intent);
            }
        });

        nearby_donors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CenterHomeActivity.this,NearByDonorsActivity. class);
                startActivity(intent);
            }
        });
        inbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CenterHomeActivity.this,InboxActivity. class);
                startActivity(intent);
            }
        });
        chatbot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CenterHomeActivity.this,AskChatbotActivity. class);
                startActivity(intent);
            }
        });







        check_notifications();

    }

    private void check_notifications() {

        firestore.collection("notifications")
                .whereEqualTo("to."+firebaseAuth.getUid(),false)
                //.orderBy("to."+firebaseAuth.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        int count=queryDocumentSnapshots.getDocuments().size();
                        if(count==0)
                            notifications_count.setVisibility(View.INVISIBLE);
                        else
                            notifications_count.setVisibility(View.VISIBLE);
                        notifications_count.setText(String.valueOf(count));

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
                                                   Toast.makeText(CenterHomeActivity.this, "You have new messages..Check your Inbox", Toast.LENGTH_LONG).show();
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
        check_notifications();
        check_new_messages();
    }
}