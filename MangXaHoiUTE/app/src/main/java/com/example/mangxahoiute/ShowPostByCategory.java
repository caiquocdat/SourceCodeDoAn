package com.example.mangxahoiute;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mangxahoiute.adapters.AdapterPost;
import com.example.mangxahoiute.adapters.AdapterTypePost;
import com.example.mangxahoiute.adapters.AdapterTypePostGridView;
import com.example.mangxahoiute.models.ModelPost;
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

public class ShowPostByCategory extends AppCompatActivity {
    String typePosts;
    FirebaseAuth firebaseAuth;
    RecyclerView recyclerView,recyclerViewTypePostGridView;
    List<ModelPost> postList;
    //
    List<ModelTypePost> typePostList;
    AdapterPost adapterPost;
    DatabaseReference typePDbRef,typeRef;
    String typePt;
    public static String ddPts,tPts;

    AdapterTypePost adapterTypePost;
    //
    AdapterTypePostGridView adapterTypePostGridView;

    List<ModelTypePost> usersList;
    public static boolean tonTaiTrongList;
    String email,uid;
    Context context;
    private SharedPreferences shf;
    public static String idPts,typePost;
    ModelPost modelPost;
    FrameLayout fr;
    TextView hienThiId;
    ActionBar actionBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_post_by_category);
        Intent intent=getIntent();
        typePosts=intent.getStringExtra("typepost");
//        Toast.makeText(this, typePosts, Toast.LENGTH_SHORT).show();
        firebaseAuth= FirebaseAuth.getInstance();
        //setup title actionBar
        actionBar= getSupportActionBar();
        actionBar.setTitle(typePosts);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        //setup recyclerView của post bài đăng
        recyclerView=findViewById(R.id.postRecyclerview);
        hienThiId=findViewById(R.id.hienThiIdPost);
        LinearLayoutManager layoutManager= new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);
        postList= new ArrayList<>();

    }
    private void loadPostApproved() {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelPost modelPost=ds.getValue(ModelPost.class);
                    if (modelPost.getpStatus().contains("true")&&
                    modelPost.getpTypePost().contains(typePosts)) {
                        postList.add(modelPost);
                    }
                    adapterPost= new AdapterPost(ShowPostByCategory.this,postList);
                    recyclerView.setAdapter(adapterPost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(getActivity(),""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
    private void searchPosts(String searchQuery){
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelPost modelPost=ds.getValue(ModelPost.class);
                    if ((modelPost.getpTitle().toLowerCase().contains(searchQuery.toLowerCase())
                            ||modelPost.getuName().toLowerCase().contains(searchQuery.toLowerCase())
                           || modelPost.getpDescr().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            modelPost.getuType().toLowerCase().contains(searchQuery.toLowerCase()))&&
                            modelPost.getpTypePost().contains(typePosts)
                    ){
                        postList.add(modelPost);
                    }
                    adapterPost= new AdapterPost(ShowPostByCategory.this,postList);
                    recyclerView.setAdapter(adapterPost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(getActivity(),""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onStart() {
        loadPostApproved();
        super.onStart();
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
        menu.findItem(R.id.acciton_myQrCode).setVisible(false);
        menu.findItem(R.id.acciton_scan_qrcode).setVisible(false);
        menu.findItem(R.id.acciton_logout).setVisible(false);
        menu.findItem(R.id.action_add_participant_group).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_groupinfo).setVisible(false);
        menu.findItem(R.id.action_videocall).setVisible(false);
        menu.findItem(R.id.acciton_search).setVisible(true);

        MenuItem item= menu.findItem(R.id.acciton_search);
        SearchView searchView= (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query)){
                    searchPosts(query);
                }else{
//                    loadPost();
                    loadPostApproved();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (!TextUtils.isEmpty(query)){
                    searchPosts(query);
                }else{
//                    loadPost();
                    loadPostApproved();
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