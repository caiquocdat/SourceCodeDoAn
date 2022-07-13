package com.example.mangxahoiute;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.mangxahoiute.fragments.ProfileFragment;
import com.example.mangxahoiute.models.ModelQrCode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class MyQrCodeActivity extends AppCompatActivity {
    String myEmail="caiquocdat123@gmail.com";
    ImageView QRImage;
    public static boolean tonTaiTrongList;
    Bitmap qrBits;
    FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_qr_code);
        firebaseAuth=FirebaseAuth.getInstance();
        FirebaseUser user=firebaseAuth.getCurrentUser();
        String email=user.getEmail();


        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("My QR Code");

        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        QRImage=findViewById(R.id.QRImage);
        QRGEncoder qrgEncoder=new QRGEncoder(email,null, QRGContents.Type.TEXT,500);
        try {

            qrBits=qrgEncoder.getBitmap();
            QRImage.setImageBitmap(qrBits);
        }catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private void checkTypePost(String query) {
        FirebaseUser fUser= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("TypePosts");


//        String timeStamp= String.valueOf(System.currentTimeMillis());
//        HashMap<Object, String> hashMapTypePost = new HashMap<>();
//        hashMapTypePost.put("tPost", query);
//        DatabaseReference reftPost = FirebaseDatabase.getInstance().getReference("TypePosts");
//        reftPost.child(timeStamp).setValue(hashMapTypePost);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                usersList.clear();

                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelQrCode modelQrCode=ds.getValue(ModelQrCode.class);
                    if ((modelQrCode.getEmail().toLowerCase().equals(query.toLowerCase()))){
//                        Toast.makeText(AddPostActivity.this, "Đã tồn tại", Toast.LENGTH_SHORT).show();
//                        Toast.makeText(context, modelTypePost.gettPost()+"-"+query, Toast.LENGTH_SHORT).show();
                        tonTaiTrongList=true;
                        break;
                    }else{
                        tonTaiTrongList=false;
                    }
                }
//                Toast.makeText(context, tonTaiTrongList+"", Toast.LENGTH_SHORT).show();
                if (tonTaiTrongList==false){
//                    Toast.makeText(context, query, Toast.LENGTH_SHORT).show();
                    String timeStamp= String.valueOf(System.currentTimeMillis());
                    HashMap<Object, String> hashMapTypePost = new HashMap<>();
                    hashMapTypePost.put("email", query);
                    //Đang dừng: add giá trị QRCode vào firebase
                    hashMapTypePost.put("QRCode",qrBits+"");
                    DatabaseReference reftPost = FirebaseDatabase.getInstance().getReference("QRCodes");
                    reftPost.child(timeStamp).setValue(hashMapTypePost);
                }



                //refresh  adapter

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
//        Intent intent2= new Intent(MyQrCodeActivity.this,DashboardActivity.class);
//        startActivity(intent2);
//        finish();
        Intent intent2= new Intent(MyQrCodeActivity.this, ProfileFragment.class);
        intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        finish();
        return super.onSupportNavigateUp();
    }
}