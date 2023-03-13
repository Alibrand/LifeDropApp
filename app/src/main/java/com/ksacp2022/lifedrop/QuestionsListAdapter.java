package com.ksacp2022.lifedrop;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class QuestionsListAdapter extends RecyclerView.Adapter<ChatBotQuestionCard> {
    List<Question> questionList;
    Context context;
    FirebaseFirestore firestore;

    public QuestionsListAdapter(List<Question> questionList, Context context) {
        this.questionList = questionList;
        this.context = context;
        firestore=FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ChatBotQuestionCard onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chatbot_question_card,parent,false);
        return new ChatBotQuestionCard(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatBotQuestionCard holder, int position) {
        Question question= questionList.get(position);

        holder.question_text.setText(question.getQuestion());


        holder.question_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((AskChatbotActivity)context).add_message(question);
            }
        });



    }

    @Override
    public int getItemCount() {
        return questionList.size();
    }
}

class ChatBotQuestionCard extends RecyclerView.ViewHolder{
    TextView question_text;


    public ChatBotQuestionCard(@NonNull View itemView) {
        super(itemView);

        question_text=itemView.findViewById(R.id.question_text);

    }
}
