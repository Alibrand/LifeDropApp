package com.ksacp2022.lifedrop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AskChatbotActivity extends AppCompatActivity {

    EditText question_text;

    RecyclerView messages_list, filtered_list;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    ProgressDialog progressDialog;
    List<Question> questionList=new ArrayList<>();
    List<Question> filteredList;
    ImageView back;

    List<Message> chatMessageList=new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ask_chatbot);
        question_text = findViewById(R.id.question_text);
        filtered_list = findViewById(R.id.filtered_list);
        messages_list = findViewById(R.id.messages_list);
        back = findViewById(R.id.back);


        firestore=FirebaseFirestore.getInstance();
        firebaseAuth=FirebaseAuth.getInstance();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        progressDialog=new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Preparing Bot");
        progressDialog.show();

        firestore.collection("questions")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        questionList=new ArrayList<>();
                        for (DocumentSnapshot doc:queryDocumentSnapshots.getDocuments()
                        ) {
                            Map<String,Object> data=doc.getData();
                            Question question=new Question();
                            question.setQuestion(data.get("question").toString());
                            question.setAnswer(data.get("answer").toString());
                            question.setKeywords(data.get("keywords").toString());

                            questionList.add(question);
                        }
                        progressDialog.dismiss();

                        Toast.makeText(AskChatbotActivity.this,"Chat bot Ready " , Toast.LENGTH_LONG).show();



                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(AskChatbotActivity.this,"Unable to load info " , Toast.LENGTH_LONG).show();
                        finish();
                    }
                });


        question_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String query= question_text.getText().toString();
                if(query.isEmpty())
                {
                    filtered_list.setVisibility(View.GONE);
                    return;
                }
                filteredList =new ArrayList<>();
                for (Question quest:questionList
                ) {
                    if(quest.getKeywords().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))
                            || quest.getQuestion().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)))
                        filteredList.add(quest);

                }
                if(filteredList.size()>0)
                {
                    filtered_list.setVisibility(View.VISIBLE);
                    QuestionsListAdapter adapter=new QuestionsListAdapter(filteredList,AskChatbotActivity.this);
                    filtered_list.setAdapter(adapter);
                }
                else{
                    filtered_list.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });



    }
    public void add_message(Question question) {
        //add message for question
        String uid=firebaseAuth.getUid();
        Message question_message=new Message();
        question_message.setFrom(uid);
        question_message.setText(question.getQuestion());
        //add message for bot answer
        Message answer_message=new Message();
        answer_message.setFrom("bot");
        answer_message.setText(question.getAnswer());

        chatMessageList.add(question_message);
        chatMessageList.add(answer_message);
        MessagesListAdapter adapter=new MessagesListAdapter(chatMessageList,AskChatbotActivity.this);
        messages_list.setAdapter(adapter);
        filtered_list.setVisibility(View.GONE);
        question_text.setText("");
        messages_list.scrollToPosition(chatMessageList.size()-1);
    }
}