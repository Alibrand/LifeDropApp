package com.ksacp2022.lifedrop;

import static android.widget.Toast.*;
import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.makeText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.LinearLayoutCompat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateAccountActivity extends AppCompatActivity {

    //declare objects
    ImageView back;
    EditText full_name,email,password,confirm_password,weight,
            phone,address,age;
    Spinner blood_group;
    LinearLayoutCompat user_info;
    String[] blood_groups={"A+","B+","AB+","O+","A-","B-","AB-","O-"};
    TextView login;
    AppCompatButton create,set_location;
    //we need fire authentication object to manage users accounts
    FirebaseAuth firebaseAuth;
    //we need firestore object to store user extra info e.g name,phone,address,blood group ...etc
    FirebaseFirestore firebaseFirestore;
    ProgressDialog progressDialog;
    GeoPoint location;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //linking with layout
        setContentView(R.layout.activity_create_account);
        create = findViewById(R.id.create);
        full_name = findViewById(R.id.full_name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirm_password = findViewById(R.id.confirm_password);
        phone = findViewById(R.id.phone);
        address = findViewById(R.id.address);
        age = findViewById(R.id.age);
        blood_group = findViewById(R.id.blood_group);
        weight = findViewById(R.id.weight);
        user_info = findViewById(R.id.user_info);
        back = findViewById(R.id.back);
        login = findViewById(R.id.login);
        set_location = findViewById(R.id.set_location);


        //initiate objects
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();
        progressDialog=new ProgressDialog(this);




        //adapter to show list of blood groups
        ArrayAdapter adapter=new ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,blood_groups);
        blood_group.setAdapter(adapter);

        //get account type from login activity
        //account types : User - Donation Center
        Intent intent=getIntent();
        String account_type=intent.getStringExtra("account_type");

        //if the user type is Donation Center
        //hide age,weight,blood group
        if(account_type.equals("Donation Center"))
        {
            user_info.setVisibility(View.GONE);
        }

        set_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(CreateAccountActivity.this,ChooseLocationActivity.class);
                startActivityForResult(intent,110);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str_full_name =full_name.getText().toString();
                String str_email =email.getText().toString();
                String str_password =password.getText().toString();
                String str_confirm_password =confirm_password.getText().toString();
                String str_phone =phone.getText().toString();
                String str_address =address.getText().toString();
                String str_age =age.getText().toString();
                String str_blood_group =blood_group.getSelectedItem().toString();
                String str_weight =weight.getText().toString();


                if(str_full_name.isEmpty())
                {
                    full_name.setError("Required field");
                    full_name.requestFocus();
                    return;
                }
                if(str_email.isEmpty())
                {
                    email.setError("Required field");
                    email.requestFocus();
                    return;
                }
                if(!isValidEmail(str_email))
                {
                    email.setError("Bad email format.It should look like example@mail.com");
                    email.requestFocus();
                    return;
                }
                if(str_password.isEmpty())
                {
                    password.setError("Required field");
                    password.requestFocus();
                    return;
                }

                if(!isValidPassword(str_password))
                {
                    password.setError("Password should be at least 8 characters and contains a mix of capital and small letters with numbers ");
                    password.requestFocus();
                    return;
                }

                if(str_confirm_password.isEmpty())
                {
                    confirm_password.setError("Required field");
                    confirm_password.requestFocus();
                    return;
                }
                if(!str_password.equals(str_confirm_password))
                {
                    confirm_password.setError("Passwords don't match");
                    confirm_password.requestFocus();
                    return;
                }
                if(str_phone.isEmpty())
                {
                    phone.setError("Required field");
                    phone.requestFocus();
                    return;
                }

                if(str_address.isEmpty())
                {
                    address.setError("Required field");
                    address.requestFocus();
                    return;
                }

                if(account_type.equals("User"))
                {
                if(str_age.isEmpty())
                {
                    age.setError("Required field");
                    age.requestFocus();
                    return;
                }
                if(str_weight.isEmpty() )
                {
                    weight.setError("Required field");
                    weight.requestFocus();
                    return;
                }

                }
                if(location==null)
                {
                    makeText(CreateAccountActivity.this,"You should set your location" , LENGTH_LONG).show();
                 return;
                }


                progressDialog.setTitle("Creating Account");
                progressDialog.setMessage("Please Wait");
                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                firebaseAuth.createUserWithEmailAndPassword(str_email,str_password)
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                String uid=firebaseAuth.getUid();

                                Account account=new Account();
                                account.setFull_name(str_full_name);
                                account.setAccount_type(account_type);
                                account.setAddress(str_address);
                                account.setAge(str_age);
                                account.setPhone(str_phone);
                                account.setBlood_group(str_blood_group);
                                account.setWeight(str_weight);
                                account.setLocation(location);


                                firebaseFirestore.collection("accounts")
                                        .document(uid)
                                        .set(account)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //save user type in shared preferences
                                                SharedPreferences sharedPref = CreateAccountActivity.this.getPreferences(Context.MODE_PRIVATE);
                                                SharedPreferences.Editor editor = sharedPref.edit();
                                                editor.putString("account_type",account_type);
                                                editor.putString("full_name",str_full_name);
                                                editor.apply();
                                                if(account_type.equals("User"))
                                                {
                                                    Intent intent = new Intent(CreateAccountActivity.this,UserHomeActivity.
                                                            class);
                                                    //send user's name to the next activity
                                                    intent.putExtra("full_name",str_full_name);
                                                    startActivity(intent);
                                                }
                                                else
                                                {
//                                                    Intent intent = new Intent(CreateAccountActivity.this,CenterHomeActivity.
//                                                            class);
//                                                    //send user's name to the next activity
//                                                    intent.putExtra("full_name",str_full_name);
//                                                    startActivity(intent);
                                                    Intent intent = new Intent(CreateAccountActivity.this,MessageActivity.
                                                    class);
                                                    String message="Your account will be reviewed by Admin Soon";
                                                    intent.putExtra("message",message);
                                                    startActivity(intent);
                                                }
                                                progressDialog.dismiss();
                                                finish();
                                                makeText(CreateAccountActivity.this,"Account Created Successfully" , LENGTH_LONG).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                makeText(CreateAccountActivity.this,"Failed to create account due to  "+e.getMessage().toString() , LENGTH_LONG).show();
                                            }
                                        });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                makeText(CreateAccountActivity.this,"Failed to create account due to  "+e.getMessage().toString() , LENGTH_LONG).show();
                                progressDialog.dismiss();
                            }
                        });




            }
        });






    }
    public boolean isValidPassword(final String password) {

        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[a-z]).{8,}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();

    }

    public boolean isValidEmail(final String email) {

        Pattern pattern;
        Matcher matcher;
        final String EMAIL_PATTERN = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+.[a-zA-Z]{2,6}$";
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);

        return matcher.matches();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==110)
            if(resultCode==RESULT_OK)
            {
                double latitude= (double) data.getSerializableExtra("lat");
                double longitude= (double) data.getSerializableExtra("long");
                makeText(CreateAccountActivity.this,"Location set successfully", LENGTH_LONG).show();
                location=new GeoPoint(latitude,longitude);
            }
    }
}