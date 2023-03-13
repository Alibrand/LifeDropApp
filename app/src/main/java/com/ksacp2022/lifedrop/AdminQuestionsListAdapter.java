package com.ksacp2022.lifedrop;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;

public class AdminQuestionsListAdapter extends RecyclerView.Adapter<AdminChatQuestionCard> {
    List<Question> questionList;
    Context context;
    FirebaseFirestore firestore;

    public AdminQuestionsListAdapter(List<Question> questionList, Context context) {
        this.questionList = questionList;
        this.context = context;
        firestore=FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public AdminChatQuestionCard onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_chat_question_card,parent,false);
        return new AdminChatQuestionCard(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminChatQuestionCard holder, int position) {
        Question question= questionList.get(position);

        holder.question_text.setText(question.getQuestion());



        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
           firestore.collection("questions")
                   .document(question.getId())
                   .delete()
                   .addOnSuccessListener(new OnSuccessListener<Void>() {
                       @Override
                       public void onSuccess(Void unused) {
                           questionList.remove(question);
                           AdminQuestionsListAdapter.this.notifyDataSetChanged();
                       }
                   }).addOnFailureListener(new OnFailureListener() {
                       @Override
                       public void onFailure(@NonNull Exception e) {
                           Toast.makeText(context,"Couldn't delete item" , Toast.LENGTH_LONG).show();
                       }
                   });
            }
        });


    }

    @Override
    public int getItemCount() {
        return questionList.size();
    }
}

class AdminChatQuestionCard extends RecyclerView.ViewHolder{
    TextView question_text;
    ImageButton delete;

    public AdminChatQuestionCard(@NonNull View itemView) {
        super(itemView);
        delete=itemView.findViewById(R.id.delete);
        question_text=itemView.findViewById(R.id.question_text);

    }
}
