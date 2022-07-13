package com.example.mangxahoiute;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.mangxahoiute.adapters.AdapterTypePost;
import com.example.mangxahoiute.models.ModelTypePost;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TypePostActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    SearchView searchView;
    AdapterTypePost adapterTypePost;
    List<ModelTypePost> usersList;
    FirebaseAuth firebaseAuth;
    ActionBar actionBar;
    String name,email,uid,dp,typePt;
    Button btnOk;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type_post);
        actionBar= getSupportActionBar();
        actionBar.setTitle("Select or enter Type Post");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        firebaseAuth=FirebaseAuth.getInstance();
        checkUserStatus();
        actionBar.setSubtitle(email);

        anhXa();
        Intent intent= getIntent();
        String isUpdateKey=""+intent.getStringExtra("keyTypePost");

        //Lưu editext của addpost khi nhấn chọn typepost
        SharedPreferences sharedPreferences = getSharedPreferences("saveEditText", MODE_PRIVATE);
        String emailkt=sharedPreferences.getString("email","");
        String title=sharedPreferences.getString("title","");
        String image=sharedPreferences.getString("image","");
        String description=sharedPreferences.getString("description","");

        //Lưu editext của editpost của editpost khi nhấn chọn typepost
        SharedPreferences sharedPreferencesEditPost = getSharedPreferences("saveEditTextOfEditPost", MODE_PRIVATE);
        String emailktEdtPost=sharedPreferencesEditPost.getString("email","");
        String idEdtPost=sharedPreferencesEditPost.getString("idpost","");
        String titleEdtPost=sharedPreferencesEditPost.getString("title","");
        String imageEdtPost=sharedPreferencesEditPost.getString("image","");
        String descriptionEdtPost=sharedPreferencesEditPost.getString("description","");



//        Toast.makeText(this, image, Toast.LENGTH_SHORT).show();
//        Toast.makeText(TypePostActivity.this, email+" "+title+" "+image+" "+description, Toast.LENGTH_SHORT).show();

//        Toast.makeText(this, isUpdateKey, Toast.LENGTH_SHORT).show();
//        Intent intent=getIntent();
//        typePt=intent.getStringExtra("typePost");



//        try {
//            SharedPreferences shf = getSharedPreferences("typepost1", MODE_PRIVATE);
//            typePt=shf.getString("tpost","");
//            searchView.setQuery(typePt,false);
//            //fill searchview
//
//        } catch (Exception e){
//            Toast.makeText(this, "Null", Toast.LENGTH_SHORT).show();
//        }

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tPost=searchView.getQuery().toString();
                SharedPreferences shf = getSharedPreferences("typepost1", MODE_PRIVATE);
                SharedPreferences.Editor editor=shf.edit();
                editor.putString("tpost",tPost);
                editor.apply();
                Intent intent= new Intent(TypePostActivity.this, AddPostActivity.class);
                if (isUpdateKey.equals("editPost")){
                    intent.putExtra("keyTypePts","editPost");
                }
                else{
                    intent.putExtra("keyTypePts","addPost");
                }
                startActivity(intent);

                //Truyền dữ liệu của các edittext về cho addpostactivity
                SharedPreferences sharedPreferencess = getSharedPreferences("saveEditText", MODE_PRIVATE);
                SharedPreferences.Editor editor1=sharedPreferencess.edit();
                editor1.putString("key","check");
                editor1.putString("email",emailkt);
                editor1.putString("title",title);
                editor1.putString("image",image);
                editor1.putString("description",description);
                editor1.apply();

                //Truyền dữ liệu của các edittext về cho edit của addpostactivity
                SharedPreferences sharedPreferencessEditPost = getSharedPreferences("saveEditTextOfEditPost", MODE_PRIVATE);
                SharedPreferences.Editor editorEditPost=sharedPreferencessEditPost.edit();
                editorEditPost.putString("key","check");
                editorEditPost.putString("email",emailktEdtPost);
                editorEditPost.putString("idpost",idEdtPost);
                editorEditPost.putString("title",titleEdtPost);
                editorEditPost.putString("image",imageEdtPost);
                editorEditPost.putString("description",descriptionEdtPost);
                editorEditPost.apply();

            }
        });
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersList= new ArrayList<>();
        adapterTypePost= new AdapterTypePost(this,usersList);
        recyclerView.setAdapter(adapterTypePost);
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("TypePosts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelTypePost modelTypePost= ds.getValue(ModelTypePost.class);
                    usersList.add(modelTypePost);
                }
                adapterTypePost.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        getAllUsers();

//        okBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(TypePostActivity.this,AddPostActivity.class);
//                startActivity(intent);
//            }
//        });

                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (!TextUtils.isEmpty(s.trim())){
                    searchUsers(s);
                }else {
                    getAllUsers();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (!TextUtils.isEmpty(s.trim())){
                    searchUsers(s);
                }else {
                    getAllUsers();
                }
                return false;
            }
        });


    }
    private void searchUsers(String query) {
        FirebaseUser fUser= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("TypePosts");
        adapterTypePost = new AdapterTypePost(this,usersList);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelTypePost modelTypePost=ds.getValue(ModelTypePost.class);
                    if (modelTypePost.gettPost().toLowerCase().contains(query.toLowerCase())){
                        usersList.add(modelTypePost);

                    }
                    adapterTypePost.notifyDataSetChanged();
                    recyclerView.setAdapter(adapterTypePost);
                }
                //refresh  adapter

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getAllUsers() {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("TypePosts");
        adapterTypePost = new AdapterTypePost(this,usersList);
        recyclerView.setAdapter(adapterTypePost);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelTypePost modelTypePost=ds.getValue(ModelTypePost.class);
                    usersList.add(modelTypePost);


                }
                adapterTypePost.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void anhXa() {
        searchView=findViewById(R.id.pTypePostEt);
        recyclerView=findViewById(R.id.typePost_recyclerView);
        btnOk=findViewById(R.id.pOkBtn);
    }
    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
    }

    private void checkUserStatus(){
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if (user!=null){
            email=user.getEmail();
            uid=user.getUid();

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
        menu.findItem(R.id.acciton_search).setVisible(false);
        menu.findItem(R.id.acciton_scan_qrcode).setVisible(false);
        menu.findItem(R.id.acciton_logout).setVisible(false);
        menu.findItem(R.id.acciton_myQrCode).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_add_participant_group).setVisible(false);
        menu.findItem(R.id.action_groupinfo).setVisible(false);
        menu.findItem(R.id.action_videocall).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }
}