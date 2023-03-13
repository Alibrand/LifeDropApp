package com.ksacp2022.lifedrop;

import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.makeText;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.ksacp2022.lifedrop.databinding.ActivityAppealsMapBinding;

import java.util.Map;

public class AppealsMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityAppealsMapBinding binding;
    AppCompatButton all,a_p,b_p,ab_p,o_p,a_n,b_n,ab_n,o_n;
    TextView filter;
    String current_filter="all";
    ProgressDialog dialog;

    FirebaseFirestore firestore;
    FirebaseAuth firebaseAuth;
    FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAppealsMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        all = findViewById(R.id.all);
        a_p = findViewById(R.id.a_p);
        b_p = findViewById(R.id.b_p);
        ab_p = findViewById(R.id.ab_p);
        o_p = findViewById(R.id.o_p);
        a_n = findViewById(R.id.a_n);
        b_n = findViewById(R.id.b_n);
        ab_n = findViewById(R.id.ab_n);
        o_n = findViewById(R.id.o_n);
        filter = findViewById(R.id.filter);

        firestore=FirebaseFirestore.getInstance();
        firebaseAuth=FirebaseAuth.getInstance();

        dialog=new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        load_appeals();


        all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                current_filter="all";
                load_appeals();
            }
        });
        a_p.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                current_filter="A+";
                load_appeals();
            }
        });
        b_p.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                current_filter="B+";
                load_appeals();
            }
        });
        ab_p.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                current_filter="AB+";
                load_appeals();
            }
        });
        o_p.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                current_filter="O+";
                load_appeals();
            }
        });
        a_n.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                current_filter="A-";
                load_appeals();
            }
        });
        b_n.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                current_filter="B-";
                load_appeals();
            }
        });
        ab_n.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                current_filter="AB-";
                load_appeals();
            }
        });
        o_n.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                current_filter="O-";
                load_appeals();
            }
        });











    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Requesting the permission
            ActivityCompat.requestPermissions(AppealsMapActivity.this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION  }, 110);

            return;
        }
        mMap.setMyLocationEnabled(true);


        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                CameraUpdate update;
                //check if a location has been sent from notification

                if(getIntent().getDoubleExtra("lat",0.0)==0.0)
                    update = CameraUpdateFactory.newLatLngZoom(
                        new LatLng(location.getLatitude(),location.getLongitude()), 15);
                else
                {
                    double lat=getIntent().getDoubleExtra("lat",location.getLatitude());
                    double lng=getIntent().getDoubleExtra("lng",location.getLongitude());
                    update = CameraUpdateFactory.newLatLngZoom(
                            new LatLng(lat,lng), 15);

                }
                mMap.animateCamera( update);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(@NonNull Marker marker) {
                String user_id=marker.getTag().toString();
                Intent intent=new Intent(AppealsMapActivity.this,UserProfileActivity.class);
                intent.putExtra("user_id",user_id);
                startActivity(intent);

            }
        });

        //load style
        googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.map_style));

    }

    void load_appeals(){
        dialog.setMessage("Loading Appeals");
        dialog.show();
        filter.setText("Filter Appeals: "+current_filter);

        QuerySnapshot querySnapshot;
        if(current_filter.equals("all"))
            firestore.collection("appeals")
                    .whereNotEqualTo("creator_id",firebaseAuth.getUid())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            dialog.dismiss();
                            mMap.clear();
                            for (DocumentSnapshot doc:queryDocumentSnapshots.getDocuments()
                            ) {
                                Map<String,Object> data=doc.getData();
                                if(data.get("location")==null)
                                    continue;
                                GeoPoint location= (GeoPoint) data.get("location");

                                LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());
                                Marker marker=mMap.addMarker(new MarkerOptions().position(latLng) );
                                marker.setTag(data.get("creator_id").toString());
                                marker.setTitle(data.get("creator_name").toString()+"-"+data.get("blood_group").toString());





                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });

        else{
            firestore.collection("appeals")
                    .whereEqualTo("blood_group",current_filter)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            mMap.clear();
                            dialog.dismiss();
                            for (DocumentSnapshot doc:queryDocumentSnapshots.getDocuments()
                            ) {
                                Map<String,Object> data=doc.getData();
                                if(data.get("location")==null)
                                    continue;
                                GeoPoint location= (GeoPoint) data.get("location");

                                LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());
                                Marker marker=mMap.addMarker(new MarkerOptions().position(latLng) );
                                marker.setTitle(data.get("creator_name").toString()+"-"+data.get("blood_group").toString());
                                marker.setTag(data.get("creator_id").toString());





                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
        }



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==110)
        {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                makeText(AppealsMapActivity.this,"We could not determine your location" , LENGTH_LONG).show();
            }

        }
    }
}