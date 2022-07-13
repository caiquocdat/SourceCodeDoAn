package com.example.mangxahoiute;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.zolad.zoominimageview.ZoomInImageView;
import com.zolad.zoominimageview.ZoomInImageViewAttacher;

public class DetailImagePostActivity extends AppCompatActivity {
    ImageView imageView;
    String image;
    ZoomInImageView zoomImg;
    ActionBar actionBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_image_post);
        actionBar= getSupportActionBar();
        actionBar.setTitle("Image details");

        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        zoomImg=findViewById(R.id.zoomImg);
        ZoomInImageViewAttacher mIvAttacter = new ZoomInImageViewAttacher();

        Intent intent=getIntent();
        image=intent.getStringExtra("image");
        Picasso.get().load(image).into(zoomImg);
//        mIvAttacter.attachImageView(image);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}