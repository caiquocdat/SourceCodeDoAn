package com.example.mangxahoiute;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mangxahoiute.adapters.AdapterPost;
import com.example.mangxahoiute.models.ModelLock;
import com.example.mangxahoiute.models.ModelNotification;
import com.example.mangxahoiute.models.ModelPost;
import com.example.mangxahoiute.models.ModelUsers;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ThereProfileActivity extends AppCompatActivity {
    RecyclerView postsRecyclerView;
    List<ModelPost> postList;
    AdapterPost adapterPost;
    String uid;
    FirebaseAuth firebaseAuth;
    ImageView avatarIv,coverIv;
    TextView nameTv,emailTv,phoneTv,typeTv;
    LinearLayout linearLayout;
    String myUid;
    ImageButton lockIbtn;
    private static String timestampLock;
//    private static String timestampLocked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_there_profile);
        postsRecyclerView=findViewById(R.id.recyclerview_posts);
        firebaseAuth=FirebaseAuth.getInstance();
        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Profile");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        avatarIv=findViewById(R.id.avatarIv);
        coverIv=findViewById(R.id.coverIv);
        nameTv=findViewById(R.id.nameTv);
        emailTv=findViewById(R.id.emailTv);
        phoneTv=findViewById(R.id.phoneTv);
        typeTv=findViewById(R.id.typeTv);
        linearLayout=findViewById(R.id.linearLayout);
        lockIbtn=findViewById(R.id.lockIbtn);

        firebaseAuth=FirebaseAuth.getInstance();
        myUid=firebaseAuth.getUid();

        timestampLock = "" + System.currentTimeMillis();





//        Toast.makeText(this, ""+timestampLocked, Toast.LENGTH_SHORT).show();

        //get uid of user
        Intent intent=getIntent();
        uid=intent.getStringExtra("uid");

        showLock();

        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imBlockedORNot(uid);
            }
        });

        Query query=FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(uid);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check until required data get
                for (DataSnapshot ds:snapshot.getChildren()){
                    //get data
                    String name=""+ds.child("name").getValue();
                    String type=""+ds.child("type").getValue();
                    String email=""+ds.child("email").getValue();
                    String phone=""+ds.child("phone").getValue();
                    String image=""+ds.child("image").getValue();
                    String cover=""+ds.child("cover").getValue();
                    //set data
                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);
                    typeTv.setText(type);
                    try {
                        // if image is received then set
                        Picasso.get().load(image).into(avatarIv);
                    }
                    catch (Exception e){
                        // if there is any exception while getting image then set default
                        Picasso.get().load(R.drawable.ic_default_img_white).into(avatarIv);
                    }
                    try {
                        // if image is received then set
                        Picasso.get().load(cover).into(coverIv);
                    }
                    catch (Exception e){
                        // if there is any exception while getting image then set default

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        postList= new ArrayList<>();


        checkUserStatus();
        loadHisPosts();
        checkLock();
    }

    private void checkLock() {
//        Query query=FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(uid);
//        query.orderByChild("Lock");
//        query.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for (DataSnapshot ds:snapshot.getChildren()){
//                    String reason=""+ds.child("reasonLock").getValue();
//                    Toast.makeText(ThereProfileActivity.this, ""+reason, Toast.LENGTH_SHORT).show();
//                }
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid).child("Lock").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds:snapshot.getChildren()){
                    ModelLock modelLock= ds.getValue(ModelLock.class);
                    String status=modelLock.getStatus();
                    if (status==null){
                        lockIbtn.setImageDrawable(getDrawable(R.drawable.ic_lock));
                    }
                    else if (status.equals("false")){
                        lockIbtn.setImageDrawable(getDrawable(R.drawable.ic_un_lock));
                    }else{
                        lockIbtn.setImageDrawable(getDrawable(R.drawable.ic_lock));
                    }
//                    ModelNotification modelNotification=ds.getValue(ModelNotification.class);
//                    Toast.makeText(ThereProfileActivity.this, ""+modelNotification.getNotification(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void showLock() {
        FirebaseUser fUser= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref1= FirebaseDatabase.getInstance().getReference("Users");
        String reasonLock="Spam";
        ref1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds:snapshot.getChildren()){

                    ModelUsers modelUsers=ds.getValue(ModelUsers.class);
                    if (modelUsers.getType().equals("Admin")){
                        if (myUid.equals(modelUsers.getUid())){
                            lockIbtn.setVisibility(View.VISIBLE);
                            lockIbtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    lockUser(uid,reasonLock);
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void lockUser(String uid, String reasonLock) {

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");

        ref.child(uid).child("Lock").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //kiểm tra cây Lock rỗng
                if (snapshot.getChildrenCount()==0){

                    openReason1Dialog(Gravity.CENTER);
                }else {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        ModelLock modelLock = ds.getValue(ModelLock.class);
                        String status = modelLock.getStatus();
                        String time=modelLock.getTimestamp();
                        SharedPreferences sharedPreferencess = getSharedPreferences("timestampLock", MODE_PRIVATE);
                        SharedPreferences.Editor editor1=sharedPreferencess.edit();
                        editor1.putString("timestamp",time);
                        editor1.apply();
                        if (status.equals("false")) {
                            lockIbtn.setImageDrawable(getDrawable(R.drawable.ic_lock));
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("status", "true");
                            DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Users");
                            ref1.child(uid).child("Lock").child(time).updateChildren(hashMap).
                                    addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(ThereProfileActivity.this, "Unlocked...", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });
                            return;

                        }
//                        else {
//                            openReason2Dialog(Gravity.CENTER,time);
//
//                        }
//                    ModelNotification modelNotification=ds.getValue(ModelNotification.class);
//                    Toast.makeText(ThereProfileActivity.this, ""+modelNotification.getNotification(), Toast.LENGTH_SHORT).show();
                    }
                    SharedPreferences sharedPreferences = getSharedPreferences("timestampLock", MODE_PRIVATE);
                    String timestampLocked=sharedPreferences.getString("timestamp","");
                    openReason2Dialog(Gravity.CENTER,timestampLocked);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void openReason1Dialog(int center) {
        final Dialog dialog= new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_dialog_reason_disapproval);
        Window window=dialog.getWindow();
        if (window==null){
            return;
        }
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowAttributes= window.getAttributes();
        windowAttributes.gravity=center;
        window.setAttributes(windowAttributes);
        if (Gravity.BOTTOM ==  center){
            dialog.setCancelable(true);
        }
        else{
            dialog.setCancelable(false);
        }
        EditText edtReason=dialog.findViewById(R.id.edt_reason);
        Button btnCancel=dialog.findViewById(R.id.btn_cancel);
        Button btnOK=dialog.findViewById(R.id.btn_ok);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences sharedPreferencess = getSharedPreferences("timestampLock", MODE_PRIVATE);
                SharedPreferences.Editor editor1=sharedPreferencess.edit();
                editor1.putString("timestamp",timestampLock);
                editor1.apply();

                HashMap<Object, String> hashMap = new HashMap<>();
                hashMap.put("timestamp", timestampLock);
                hashMap.put("reasonLock", edtReason.getText().toString());
                hashMap.put("status", "false");
                DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Users");
                ref1.child(uid).child("Lock").child(timestampLock).setValue(hashMap).
                        addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(ThereProfileActivity.this, "Locked...", Toast.LENGTH_SHORT).show();
                                lockIbtn.setImageDrawable(getDrawable(R.drawable.ic_un_lock));
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void openReason2Dialog(int center,String time) {
        final Dialog dialog= new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_dialog_reason_disapproval);
        Window window=dialog.getWindow();
        if (window==null){
            return;
        }
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowAttributes= window.getAttributes();
        windowAttributes.gravity=center;
        window.setAttributes(windowAttributes);
        if (Gravity.BOTTOM ==  center){
            dialog.setCancelable(true);
        }
        else{
            dialog.setCancelable(false);
        }
        EditText edtReason=dialog.findViewById(R.id.edt_reason);
        Button btnCancel=dialog.findViewById(R.id.btn_cancel);
        Button btnOK=dialog.findViewById(R.id.btn_ok);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("reasonLock", edtReason.getText().toString());
                hashMap.put("status", "false");
                DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Users");
                ref1.child(uid).child("Lock").child(time).updateChildren(hashMap).
                        addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(ThereProfileActivity.this, "Locked...", Toast.LENGTH_SHORT).show();
                                lockIbtn.setImageDrawable(getDrawable(R.drawable.ic_un_lock));
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void imBlockedORNot(String hisUID){
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUID).child("BlockedUsers").orderByChild("uid").equalTo(myUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds:snapshot.getChildren()){
                            if (ds.exists()){
                                Toast.makeText(ThereProfileActivity.this, "You're blocked by that user, can't send message", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        Intent intent = new Intent(ThereProfileActivity.this, ChatActivity.class);
                        intent.putExtra("hisUid",hisUID);
                        startActivity(intent);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void loadHisPosts() {
        LinearLayoutManager layoutManager= new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        postsRecyclerView.setLayoutManager(layoutManager);

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");
        Query query=ref.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelPost myPosts=ds.getValue(ModelPost.class);
                    postList.add(myPosts);
                    adapterPost= new AdapterPost(ThereProfileActivity.this,postList);
                    postsRecyclerView.setAdapter(adapterPost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void searchHisPosts(String searchQuery){
        LinearLayoutManager layoutManager= new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        postsRecyclerView.setLayoutManager(layoutManager);

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
        Query query=ref.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelPost myPosts=ds.getValue(ModelPost.class);
                    if (myPosts.getpTitle().toLowerCase().contains(searchQuery.toLowerCase())||
                            myPosts.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())){
                        postList.add(myPosts);
                    }
                    adapterPost= new AdapterPost(ThereProfileActivity.this,postList);
                    postsRecyclerView.setAdapter(adapterPost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void checkUserStatus(){
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if (user!=null){


        }else {
            startActivity(new Intent(this, MainActivity.class));
            finish();

        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        menu.findItem(R.id.acciton_add_post).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.acciton_scan_qrcode).setVisible(false);
        menu.findItem(R.id.acciton_logout).setVisible(false);
        menu.findItem(R.id.acciton_myQrCode).setVisible(false);
        menu.findItem(R.id.action_add_participant_group).setVisible(false);
        menu.findItem(R.id.action_groupinfo).setVisible(false);
        menu.findItem(R.id.action_videocall).setVisible(false);
        MenuItem item= menu.findItem(R.id.acciton_search);
        SearchView searchView=(SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query)){
                    searchHisPosts(query);
                }else{
                    loadHisPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (!TextUtils.isEmpty(query)){
                    searchHisPosts(query);
                }else{
                    loadHisPosts();
                }
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if (id==R.id.acciton_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}