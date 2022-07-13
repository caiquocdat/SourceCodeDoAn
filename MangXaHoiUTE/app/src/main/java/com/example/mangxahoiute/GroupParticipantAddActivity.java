package com.example.mangxahoiute;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.mangxahoiute.adapters.AdapterParticipantAdd;
import com.example.mangxahoiute.adapters.AdapterUser;
import com.example.mangxahoiute.models.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GroupParticipantAddActivity extends AppCompatActivity {

    private RecyclerView usersRv;
    private ActionBar actionBar;
    private FirebaseAuth firebaseAuth;
    private String groupId;
    private String myGroupRole;
    private ArrayList<ModelUsers> userList;
    private AdapterParticipantAdd adapterParticipantAdd;
    private SearchView searchView;
    private static String resultQRCode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_participant_add);
        actionBar=getSupportActionBar();
        actionBar.setTitle("Add Participants");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        firebaseAuth=FirebaseAuth.getInstance();

        usersRv=findViewById(R.id.usersRv);
        groupId=getIntent().getStringExtra("groupId");
        loadGroupInfo();
        getAllUsers();
    }

    private void getAllUsers() {
        userList=new ArrayList<>();
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds:snapshot.getChildren()){
                    ModelUsers modelUsers=ds.getValue(ModelUsers.class);
                    if (!firebaseAuth.getUid().equals(modelUsers.getUid())){
                        userList.add(modelUsers);
                    }
                }
//                Toast.makeText(GroupParticipantAddActivity.this, ""+myGroupRole, Toast.LENGTH_SHORT).show();
                adapterParticipantAdd= new AdapterParticipantAdd(GroupParticipantAddActivity.this,userList,""+groupId,""+myGroupRole);
                usersRv.setAdapter(adapterParticipantAdd);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void loadGroupInfo() {
        DatabaseReference ref1= FirebaseDatabase.getInstance().getReference("Groups");
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds:snapshot.getChildren()){
                    String groupId=""+ds.child("groupId").getValue();
                    String groupTitle=""+ds.child("groupTitle").getValue();
                    String groupDescription=""+ds.child("groupDescription").getValue();
                    String groupIcon=""+ds.child("groupIcon").getValue();
                    String timestamp=""+ds.child("timestamp").getValue();
                    actionBar.setTitle("Add Participants");
                    ref1.child(groupId).child("Participants").child(firebaseAuth.getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        myGroupRole = "" + snapshot.child("role").getValue();
                                        actionBar.setTitle(groupTitle + " {" + myGroupRole + "}");
//                                        Toast.makeText(GroupParticipantAddActivity.this, ""+myGroupRole, Toast.LENGTH_SHORT).show();
                                        getAllUsers();

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
        userList=new ArrayList<>();
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds:snapshot.getChildren()){
                    ModelUsers modelUsers=ds.getValue(ModelUsers.class);
                    if (!firebaseAuth.getUid().equals(modelUsers.getUid())){
                        if (modelUsers.getName().toLowerCase().contains(query.toLowerCase())
                                || modelUsers.getEmail().toLowerCase().contains(query.toLowerCase())
                        )
                        userList.add(modelUsers);
                    }
                }
                adapterParticipantAdd= new AdapterParticipantAdd(GroupParticipantAddActivity.this,userList,""+groupId,""+myGroupRole);
                usersRv.setAdapter(adapterParticipantAdd);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if (id==R.id.acciton_scan_qrcode){
            startActivity(new Intent(GroupParticipantAddActivity.this, ScanQRCodeActivity.class));
        }
//        if (id==R.id.acciton_myQrCode){
//            Toast.makeText(getActivity(), "1", Toast.LENGTH_SHORT).show();
//            startActivity(new Intent(getActivity(), MyQrCodeActivity.class));
//            getActivity().finish();
//        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onStart() {
        SharedPreferences shf = getSharedPreferences("ResultQRCode", MODE_PRIVATE);
//                String idPost = shf.getString("Id", "");
        resultQRCode = shf.getString("Result", "");
//        Toast.makeText(getActivity(), ""+resultQRCode, Toast.LENGTH_SHORT).show();

        if (resultQRCode==null){
            resultQRCode="";
        }
        searchUsers(resultQRCode);
        super.onStart();
    }

    @Override
    protected void onPause() {
        SharedPreferences sharedPreferences1 =getSharedPreferences("ResultQRCode", MODE_PRIVATE);
        SharedPreferences.Editor editor1a=sharedPreferences1.edit();
        editor1a.remove("Result");
        editor1a.apply();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        SharedPreferences sharedPreferences1 =getSharedPreferences("ResultQRCode", MODE_PRIVATE);
        SharedPreferences.Editor editor1a=sharedPreferences1.edit();
        editor1a.remove("Result");
        editor1a.apply();
        super.onDestroy();
    }
}