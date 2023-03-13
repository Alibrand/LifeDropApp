package com.ksacp2022.lifedrop;

import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.makeText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    AppCompatButton login,create_user,create_center;
    EditText email,password;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    ProgressDialog progressDialog;
    TextView forgot_password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        create_user = findViewById(R.id.create_user);
        create_center = findViewById(R.id.create_center);
        forgot_password = findViewById(R.id.forgot_password);


        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Loging");
        progressDialog.setMessage("Please Wait");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();

        //if a user is already logged
        if(firebaseAuth.getCurrentUser()!=null)
        {
            redirect_user();
        }



        create_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,CreateAccountActivity. class);
                intent.putExtra("account_type","User");
                startActivity(intent);
            }
        });

        create_center.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,CreateAccountActivity. class);
                intent.putExtra("account_type","Donation Center");
                startActivity(intent);
            }
        });

        forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,ForgetPasswordActivity. class);
                startActivity(intent);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str_email= email.getText().toString();
                String str_password=password.getText().toString();

                if(str_email.isEmpty())
                {
                    email.setError("Required field");
                    return;
                }
                if(str_password.isEmpty())
                {
                    password.setError("Required field");
                    return;
                }





                progressDialog.show();

                firebaseAuth.signInWithEmailAndPassword(str_email,str_password)
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                //check if the user is admin
                                //redirect to admin home page
                                if(firebaseAuth.getCurrentUser().getEmail().equals("admin@lifedrop.com"))
                                {
                                    //save user type in shared preferences
                                    SharedPreferences sharedPref = LoginActivity.this.getPreferences(Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putString("full_name","ِAdmin");
                                    editor.apply();
                                    Intent intent = new Intent(LoginActivity.this,AdminHomeActivity. class);
                                    startActivity(intent);
                                    finish();
                                    progressDialog.dismiss();
                                    return;
                                }
                                String uid=firebaseAuth.getUid();

                                firebaseFirestore.collection("accounts")
                                                .document(uid)
                                                        .get()
                                                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                    @Override
                                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                        Map<String,Object> data=documentSnapshot.getData();
                                                                        String account_type=data.get("account_type").toString();
                                                                        String full_name=data.get("full_name").toString();
                                                                        //save user type in shared preferences
                                                                        SharedPreferences sharedPref = LoginActivity.this.getSharedPreferences("MyShared" ,Context.MODE_PRIVATE);
                                                                        SharedPreferences.Editor editor = sharedPref.edit();
                                                                        editor.putString("account_type",account_type);
                                                                        editor.putString("full_name",full_name);
                                                                        editor.apply();


                                                                        redirect_user();
                                                                        progressDialog.dismiss();
                                                                    }
                                                                })
                                                                        .addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@NonNull Exception e) {
                                                                                progressDialog.dismiss();
                                                                                makeText(LoginActivity.this,"Failed to log in due to "+e.getMessage().toString() , LENGTH_LONG).show();

                                                                            }
                                                                        });



                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                makeText(LoginActivity.this,"Failed to log in due to "+e.getMessage().toString() , LENGTH_LONG).show();
                            }
                        });
            }
        });





    }

    private void redirect_user() {
        //check if the user is admin
        if(firebaseAuth.getCurrentUser().getEmail().equals("admin@lifedrop.com"))
        {
            Intent intent = new Intent(LoginActivity.this,AdminHomeActivity. class);
            startActivity(intent);
            finish();
            return;
        }
        //read stored user data from shared preferences
        SharedPreferences sharedPref = LoginActivity.this.getSharedPreferences("MyShared",Context.MODE_PRIVATE);
        String account_type = sharedPref.getString("account_type", "");
        String full_name = sharedPref.getString("full_name", "");
        //redirect the user based on his account type : User or Donation Center
        if(account_type.equals("User"))
        {
            Intent intent = new Intent(LoginActivity.this,UserHomeActivity.
                    class);
            //send user's name to the next activity
            intent.putExtra("full_name",full_name);
            startActivity(intent);
            finish();
        }
        //if the account is for a donation center
        else
        {

            //check the status
            progressDialog.setMessage("Checking Status");
            progressDialog.show();
            String uid= firebaseAuth.getUid();
            firebaseFirestore.collection("accounts")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            Map<String,Object> data=documentSnapshot.getData();

                            String status=data.get("status").toString();

                            if(status.equals("Pending"))
                            {
                                Intent intent = new Intent(LoginActivity.this,MessageActivity.
                                        class);
                                String message="Your account will be reviewed by Admin Soon";
                                intent.putExtra("message",message);
                                startActivity(intent);
                            }
                            else if (status.equals("Rejected"))
                            {
                                Intent intent = new Intent(LoginActivity.this,MessageActivity.
                                        class);
                                String message="Unfortunately your account is rejected";
                                intent.putExtra("message",message);
                                startActivity(intent);
                            }
                            else {
                                Intent intent = new Intent(LoginActivity.this, CenterHomeActivity.
                                        class);
                                //send user's name to the next activity
                                intent.putExtra("full_name", full_name);
                                startActivity(intent);
                            }


                            progressDialog.dismiss();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            makeText(LoginActivity.this,"Failed to log in due to "+e.getMessage().toString() , LENGTH_LONG).show();
                        }
                    });



        }

    }
}