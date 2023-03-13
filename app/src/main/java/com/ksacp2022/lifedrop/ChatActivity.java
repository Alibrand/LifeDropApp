package com.ksacp2022.lifedrop;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    RecyclerView messages_list;
    ImageView back;
    ImageButton send;
    EditText message;
    TextView name;

    FirebaseFirestore firestore;
    FirebaseAuth firebaseAuth;
    ProgressBar progress;

    ListenerRegistration messagesListener;

    List<Message> messagesList;
    String chat_id, sender_id, receiver_id, receiver_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messages_list = findViewById(R.id.messages_list);
        back = findViewById(R.id.back);
        name = findViewById(R.id.name);
        send = findViewById(R.id.send);
        message = findViewById(R.id.message);
        progress = findViewById(R.id.progress);


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        sender_id = firebaseAuth.getUid();
        receiver_name = getIntent().getStringExtra("receiver_name");
        receiver_id = getIntent().getStringExtra("receiver_id");

        //if this is a new chat with this user
        //create a unique id for this chat
        int compare=sender_id.compareTo(receiver_id);
        if (getIntent().getStringExtra("chat_id") == null)
            if(compare<0)
            chat_id = sender_id + "_" + receiver_id;
            else
            chat_id = receiver_id + "_" + sender_id;
        else
            chat_id = getIntent().getStringExtra("chat_id");

        //set the other user name
        name.setText(receiver_name);


        //listening to every change in  chat's messages
        //adding a snapshot listener
        messagesListener = firestore.collection("inbox")
                .document(chat_id)
                .collection("messages")
                .orderBy("created_at", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        messagesList = new ArrayList<>();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Map<String, Object> data = doc.getData();
                            Message message = new Message();
                            message.setFrom(data.get("from").toString());
                            message.setTo(data.get("to").toString());
                            message.setText(data.get("text").toString());

                            messagesList.add(message);
                        }

                        update_messages_status();

                        MessagesListAdapter adapter = new MessagesListAdapter(messagesList, ChatActivity.this);
                        messages_list.setAdapter(adapter);
                        messages_list.scrollToPosition(messagesList.size() - 1);


                    }
                });


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = message.getText().toString();
                if (text.isEmpty())
                    return;
                message.setText("");

                progress.setIndeterminate(true);
                //if this is the first message ever
                //create a chat document
                if (messagesList.size() == 0) {
                    //get current user name (sender name)
                    //read stored user data from shared preferences
                    SharedPreferences sharedPref = ChatActivity.this.getSharedPreferences("MyShared",Context.MODE_PRIVATE);
                    String sender_name = sharedPref.getString("full_name", "");
                    Log.d("sender_name",sender_name);
                    Chat chat = new Chat();
                    chat.setLast_update(null);
                    List<String> users_ids = Arrays.asList(sender_id, receiver_id);
                    List<String> users_names = Arrays.asList(sender_name, receiver_name);
                    chat.setUsers_ids(users_ids);
                    chat.setUsers_names(users_names);

                    firestore.collection("inbox")
                            .document(chat_id)
                            .set(chat)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        send_message(text);
                                    } else {
                                        Toast.makeText(ChatActivity.this, "Failed to create chat", Toast.LENGTH_LONG).show();
                                        progress.setIndeterminate(false);

                                    }
                                }
                            });


                } else
                    send_message(text);
            }
        });


    }

    private void send_message(String text) {

        Message message = new Message();
        message.setText(text);
        message.setTo(receiver_id);
        message.setFrom(sender_id);
        message.setCreated_at(null);


        firestore.collection("inbox")
                .document(chat_id)
                .collection("messages")
                .add(message)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {

                        if (task.isSuccessful()) {

                            progress.setIndeterminate(false);
                            //update chat last_update
                            firestore.collection("inbox")
                                    .document(chat_id)
                                    .update("last_update", FieldValue.serverTimestamp());

                        }

                    }
                });

    }

    private void update_messages_status() {
        firestore.collection("inbox")
                .document(chat_id)
                .collection("messages")
                .whereEqualTo("to", sender_id)
                .whereEqualTo("status", "unseen")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                //update messages status
                                firestore.collection("inbox")
                                        .document(chat_id)
                                        .collection("messages")
                                        .document(doc.getId())
                                        .update("status", "seen");
                            }
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        messagesListener.remove();
        super.onDestroy();

    }
}