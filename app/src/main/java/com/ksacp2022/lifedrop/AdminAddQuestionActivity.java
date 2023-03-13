package com.ksacp2022.lifedrop;

import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.makeText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class AdminAddQuestionActivity extends AppCompatActivity {
    ImageView back;
    EditText question_text,answer_text,key_words;
    AppCompatButton save;


    FirebaseFirestore firestore;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_question);
        back = findViewById(R.id.back);
        question_text = findViewById(R.id.question_text);
        answer_text = findViewById(R.id.answer_text);
        key_words = findViewById(R.id.key_words);
        save = findViewById(R.id.save);


        firestore=FirebaseFirestore.getInstance();

        progressDialog=new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String question= question_text.getText().toString();
                String answer=answer_text.getText().toString();
                String keywords=key_words.getText().toString();



                if(question.isEmpty())
                {
                    question_text.setError("Empty Field");
                    return;
                }
                if(answer.isEmpty())
                {
                    answer_text.setError("Empty Field");
                    return;
                }
                if(keywords.isEmpty())
                {
                    key_words.setError("Empty Field");
                    return;
                }





                progressDialog.setMessage("Saving");
                progressDialog.show();


                Question new_question=new Question();
                new_question.setQuestion(question);
                new_question.setAnswer(answer);
                new_question.setKeywords(keywords);



                firestore.collection("questions")
                        .add(new_question)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                progressDialog.dismiss();
                                Toast.makeText(AdminAddQuestionActivity.this,"The question saved successfully " , Toast.LENGTH_LONG).show();
                            finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(AdminAddQuestionActivity.this,"Failed to save question " , Toast.LENGTH_LONG).show();

                            }
                        });



            }
        });





    }
}