package com.example.mangxahoiute;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class SplashActivity extends AppCompatActivity {
    ImageView ivTop,ivHeart,ivBeat,ivBottom;
    TextView textView;
    CharSequence charSequence;
    int index;
    long deplay=200;
    Handler handler= new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_splash);
        //
        anhXa();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Animation animation= AnimationUtils.loadAnimation(this,R.anim.top_ware);
        ivTop.setAnimation(animation);
        ObjectAnimator objectAnimator =ObjectAnimator.ofPropertyValuesHolder(
                ivHeart,
                PropertyValuesHolder.ofFloat("scaleX",1.2f),
                PropertyValuesHolder.ofFloat("scaleY",1.2f)
        );
        objectAnimator.setDuration(500);
        objectAnimator.setRepeatCount(ValueAnimator.INFINITE);
        objectAnimator.setRepeatMode(ValueAnimator.REVERSE);
        objectAnimator.start();
        animatText("UTE SOCIAL");
        Glide.with(this).load("https://firebasestorage.googleapis.com/v0/b/demoapp-ae96a.appspot.com/o/heart_beat.gif?alt=media&token=b21dddd8-782c-457c-babd-f2e922ba172b")
                .load("")
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(ivBeat);
        //Initalize bottom animation
        Animation animation2= AnimationUtils.loadAnimation(this,R.anim.bottom_ware);
        //Start bottom animation
        ivBottom.setAnimation(animation2);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this,MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                finish();
            }
        },4000);
    }
    Runnable runnable= new Runnable() {
        @Override
        public void run() {
            textView.setText(charSequence.subSequence(0,index++));
            if(index<=charSequence.length()){
                handler.postDelayed(runnable,deplay);
            }
        }
    };
    //Create animated text method
    public void animatText(CharSequence cs){
        charSequence =cs;
        index =0;
        textView.setText("");
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable,deplay);
    }

    private void anhXa() {
        ivTop= findViewById(R.id.iv_top);
        ivHeart= findViewById(R.id.iv_heart);
        ivBeat= findViewById(R.id.iv_beat);
        ivBottom= findViewById(R.id.iv_bottom);
        textView= findViewById(R.id.text_view);
    }
}