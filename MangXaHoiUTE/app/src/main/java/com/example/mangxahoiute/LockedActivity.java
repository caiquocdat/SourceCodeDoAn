package com.example.mangxahoiute;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.mangxahoiute.adapters.AdapterParticipantAdd;
import com.example.mangxahoiute.adapters.AdapterUser;
import com.example.mangxahoiute.models.ModelLock;
import com.example.mangxahoiute.models.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LockedActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    AdapterUser adapterUser;
    List<ModelUsers> usersList;
    FirebaseAuth firebaseAuth;
    RecyclerView.LayoutManager layoutManager;
    SearchView searchView;
    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locked);

        firebaseAuth=FirebaseAuth.getInstance();
        recyclerView= findViewById(R.id.users_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Dùng để tạo gridView
//        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(),2));
        usersList= new ArrayList<>();

        actionBar=getSupportActionBar();
        actionBar.setTitle("The list of users has been locked");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);


        getAllUsers();
    }

    private void getAllUsers() {
        FirebaseUser fUser= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelUsers modelUsers=ds.getValue(ModelUsers.class);
                    String uid=modelUsers.getUid();
                    DatabaseReference ref1= FirebaseDatabase.getInstance().getReference("Users");
                    ref1.child(uid).child("Lock").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds:snapshot.getChildren()){
                                ModelLock modelLock=ds.getValue(ModelLock.class);
                                if (modelLock.getStatus().equals("false")){
                                    usersList.add(modelUsers);
                                    adapterUser = new AdapterUser(LockedActivity.this,usersList);
                                    recyclerView.setAdapter(adapterUser);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.acciton_add_post).setVisible(false);
        menu.findItem(R.id.acciton_logout).setVisible(false);
        menu.findItem(R.id.acciton_myQrCode).setVisible(false);
        menu.findItem(R.id.action_add_participant_group).setVisible(false);
        menu.findItem(R.id.action_groupinfo).setVisible(false);
        menu.findItem(R.id.acciton_scan_qrcode).setVisible(false);
        menu.findItem(R.id.action_videocall).setVisible(false);

        MenuItem item= menu.findItem(R.id.acciton_search);
        searchView= (SearchView) MenuItemCompat.getActionView(item);

        //search listener
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

        return super.onCreateOptionsMenu(menu);
    }

    private void searchUsers(String query) {
        FirebaseUser fUser= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelUsers modelUsers=ds.getValue(ModelUsers.class);
                    String uid=modelUsers.getUid();
                    DatabaseReference ref1= FirebaseDatabase.getInstance().getReference("Users");
                    ref1.child(uid).child("Lock").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds:snapshot.getChildren()){
                                ModelLock modelLock=ds.getValue(ModelLock.class);
                                if (modelLock.getStatus().equals("false")&&
                                modelUsers.getEmail().toLowerCase().contains(query.toLowerCase())){
                                    usersList.add(modelUsers);
                                }
                            }
                            adapterUser = new AdapterUser(LockedActivity.this,usersList);
                            recyclerView.setAdapter(adapterUser);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}