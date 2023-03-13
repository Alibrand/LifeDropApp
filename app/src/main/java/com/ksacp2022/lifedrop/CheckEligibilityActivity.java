package com.ksacp2022.lifedrop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;



import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class CheckEligibilityActivity extends AppCompatActivity {

    EditText current_age,current_weight;
    AppCompatButton check;
    DatePicker last_donation;
    String center_name,center_id;
    ImageView back;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_eligibility);
        current_age=findViewById(R.id.current_age);
        current_weight = findViewById(R.id.current_weight);
        check = findViewById(R.id.check);
        last_donation = findViewById(R.id.last_donation);
        back = findViewById(R.id.back);

        firebaseAuth=FirebaseAuth.getInstance();

        firestore=FirebaseFirestore.getInstance();
        progressDialog=new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);



        progressDialog.setMessage("Checking Info");
        progressDialog.show();


        firestore.collection("accounts")
                .document(firebaseAuth.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                       progressDialog.dismiss();
                        Map<String,Object> data=documentSnapshot.getData();
                        if(data.get("last_donation_date")!=null)
                        {
                            Timestamp timestamp= (Timestamp) data.get("last_donation_date");
                            Calendar calendar=Calendar.getInstance();
                            calendar.setTime(timestamp.toDate());
                            last_donation.updateDate(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
                            last_donation.setEnabled(false);
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

        center_name=getIntent().getStringExtra("center_name");
        center_id=getIntent().getStringExtra("center_id");

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String age_str=current_age.getText().toString();
                String weight_str=current_weight.getText().toString();

                if(age_str.isEmpty())
                {
                    current_age.setError("Required Field");
                    return;
                }

                if(weight_str.isEmpty())
                {
                    current_weight.setError("Required Field");
                    return;
                }

                List<String> errors=new ArrayList<>();

                int age=Integer.parseInt(age_str);
                if(age<18)
                {
                    errors.add("-You should be at least 18 years old.");
                }

                int weight=Integer.parseInt(weight_str);
                if(weight<50)
                {
                    errors.add("-You should weigh at least 50KG.");
                }


                int day=last_donation.getDayOfMonth();
                int month=last_donation.getMonth();
                int year=last_donation.getYear();
                Calendar calendar=Calendar.getInstance();
                Date now=calendar.getTime();
                calendar.set(year,month,day);
                Date last_date=calendar.getTime();

                long difference=now.getTime()-last_date.getTime();
                int diff_days = (int) (difference / (1000 * 60 * 60 * 24));
                if(diff_days<56){
                    errors.add("-You must wait at least eight weeks (56 days) between donations");
                }


                if(errors.size()>0)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(CheckEligibilityActivity.this);


                    builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
                    builder.setTitle("You Are Not Eligible for donation");
                    StringBuilder str=new StringBuilder();
                    str.append("Reasons:\n");
                    for (String error:errors) {
                        str.append(error+"\n");
                    }
                    builder.setMessage(str.toString());

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(CheckEligibilityActivity.this);

                    builder.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(CheckEligibilityActivity.this,BookAppointmentActivity. class);
                            intent.putExtra("center_name",center_name);
                            intent.putExtra("center_id",center_id);
                            startActivity(intent);
                            finish();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
                    builder.setTitle("You Are Eligible for donation");

                    builder.setMessage("You can proceed to book an appointment");



                    AlertDialog dialog = builder.create();
                    dialog.show();


                }



            }
        });





    }
}