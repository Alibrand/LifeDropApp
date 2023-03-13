package com.ksacp2022.lifedrop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminCentersRequestsActivity extends AppCompatActivity {
    RecyclerView requests_list;
    TextView title;
    ImageView back;

    FirebaseFirestore firestore;
    ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_centers_requests);
        requests_list = findViewById(R.id.requests_list);
        back = findViewById(R.id.back);
        title = findViewById(R.id.title);




        firestore=FirebaseFirestore.getInstance();

        String request_status=getIntent().getStringExtra("request_status");
        title.setText(request_status+" Requests");

        dialog=new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage("Loading Requests");
        dialog.show();

        firestore.collection("accounts")
                .whereEqualTo("status",request_status)
                .whereEqualTo("account_type","Donation Center")
                .orderBy("created_at", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        dialog.dismiss();
                        List<Account> accountList=new ArrayList<>();
                        for (DocumentSnapshot doc:queryDocumentSnapshots.getDocuments()
                        ) {
                            Map<String,Object> data=doc.getData();
                            Account account=new Account();
                            account.setId(doc.getId());
                            account.setFull_name(data.get("full_name").toString());
                            account.setLocation((GeoPoint) data.get("location"));
                            account.setStatus(data.get("status").toString());
                            account.setPhone(data.get("phone").toString());
                            Timestamp created_at= (Timestamp) data.get("created_at");
                            account.setCreated_at(created_at.toDate());
                            accountList.add(account);




                        }


                        CenterRequestListAdapter adapter=new CenterRequestListAdapter(accountList,AdminCentersRequestsActivity.this);
                        requests_list.setAdapter(adapter);


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        Toast.makeText(AdminCentersRequestsActivity.this,"Failed to load requests" , Toast.LENGTH_LONG).show();

                    }
                });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });




    }
}