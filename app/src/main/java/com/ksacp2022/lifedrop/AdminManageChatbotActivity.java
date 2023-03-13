package com.ksacp2022.lifedrop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminManageChatbotActivity extends AppCompatActivity {
    RecyclerView questions_list;

    ImageView back;
    AppCompatButton add_question;


    FirebaseFirestore firestore;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_chatbot);
        questions_list = findViewById(R.id.questions_list);
        add_question = findViewById(R.id.add_question);
        back = findViewById(R.id.back);



        firestore=FirebaseFirestore.getInstance();




        dialog=new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);


        load_questions();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        add_question.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminManageChatbotActivity.this,AdminAddQuestionActivity. class);
                startActivity(intent);
            }
        });




    }

    private void load_questions() {
        dialog.setMessage("Loading");
        dialog.show();

        firestore.collection("questions")
                .orderBy("created_at", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        dialog.dismiss();
                        List<Question> questionList=new ArrayList<>();
                        for (DocumentSnapshot doc:queryDocumentSnapshots.getDocuments()
                        ) {
                            Map<String,Object> data=doc.getData();
                            Question question=new Question();
                            question.setQuestion(data.get("question").toString());
                            question.setAnswer(data.get("answer").toString());
                            question.setKeywords(data.get("keywords").toString());
                            question.setId(doc.getId());
                            questionList.add(question);



                        }



                        AdminQuestionsListAdapter adapter=new AdminQuestionsListAdapter(questionList,AdminManageChatbotActivity.this);
                        questions_list.setAdapter(adapter);


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        Toast.makeText(AdminManageChatbotActivity.this,"Failed to load questions" , Toast.LENGTH_LONG).show();

                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        load_questions();
    }
}