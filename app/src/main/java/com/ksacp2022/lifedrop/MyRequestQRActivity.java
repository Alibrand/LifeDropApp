package com.ksacp2022.lifedrop;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.ksacp2022.lifedrop.qr.QRGContents;
import com.ksacp2022.lifedrop.qr.QRGEncoder;

public class MyRequestQRActivity extends AppCompatActivity {
    ImageView qr_image,back;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_request_qractivity);
        qr_image = findViewById(R.id.qr_image);
        back = findViewById(R.id.back);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        String request_id=getIntent().getStringExtra("request_id");

        QRGEncoder qrgEncoder=new QRGEncoder(request_id,null, QRGContents.Type.TEXT,500);
        qrgEncoder.setColorBlack(Color.RED);
        try{
            Bitmap qr_bitmap=qrgEncoder.getBitmap();
            qr_image.setImageBitmap(qr_bitmap);

        }catch (Exception ex)
        {

        }



    }
}