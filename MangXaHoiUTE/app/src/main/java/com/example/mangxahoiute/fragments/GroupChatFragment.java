package com.example.mangxahoiute.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.mangxahoiute.GroupCreateActivity;
import com.example.mangxahoiute.MainActivity;
import com.example.mangxahoiute.R;
import com.example.mangxahoiute.ScanQRCodeActivity;
import com.example.mangxahoiute.adapters.AdapterGroupChatList;
import com.example.mangxahoiute.adapters.AdapterUser;
import com.example.mangxahoiute.models.ModelGroupChatList;
import com.example.mangxahoiute.models.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class GroupChatFragment extends Fragment {


    private RecyclerView groupsRv;
    private FirebaseAuth firebaseAuth;

    private SearchView searchView;

    private ArrayList<ModelGroupChatList> groupChatLists;
    private AdapterGroupChatList adapterGroupChatList;
    public GroupChatFragment() {

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_group_chat, container, false);
        groupsRv=view.findViewById(R.id.groupRv);
        firebaseAuth=FirebaseAuth.getInstance();
        loadGroupChatList();
        return view;
    }

    private void loadGroupChatList() {
        groupChatLists=new ArrayList<>();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Groups");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupChatLists.clear();
                for (DataSnapshot ds:snapshot.getChildren()){
                    if (ds.child("Participants").child(firebaseAuth.getUid()).exists()){
                        ModelGroupChatList model=ds.getValue(ModelGroupChatList.class);
                        groupChatLists.add(model);
                    }
                }
                adapterGroupChatList=new AdapterGroupChatList(getActivity(),groupChatLists);
                groupsRv.setAdapter(adapterGroupChatList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void searchGroupChatList(String query) {
        groupChatLists=new ArrayList<>();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Groups");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupChatLists.clear();
                for (DataSnapshot ds:snapshot.getChildren()){
                    if (ds.child("Participants").child(firebaseAuth.getUid()).exists()){
                        if (ds.child("groupTitle").toString().toLowerCase().contains(query.toLowerCase())){
                            ModelGroupChatList model=ds.getValue(ModelGroupChatList.class);
                            groupChatLists.add(model);
                        }

                    }
                }
                adapterGroupChatList=new AdapterGroupChatList(getActivity(),groupChatLists);
                groupsRv.setAdapter(adapterGroupChatList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true); // show menu option in fragment
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main,menu);
        // hide addpost icon from this fragment
        menu.findItem(R.id.acciton_add_post).setVisible(false);
        menu.findItem(R.id.acciton_logout).setVisible(false);
        menu.findItem(R.id.acciton_myQrCode).setVisible(false);
        menu.findItem(R.id.acciton_scan_qrcode).setVisible(false);
        menu.findItem(R.id.action_add_participant_group).setVisible(false);
        menu.findItem(R.id.action_groupinfo).setVisible(false);
        menu.findItem(R.id.action_videocall).setVisible(false);
        MenuItem item= menu.findItem(R.id.acciton_search);
        searchView= (SearchView) MenuItemCompat.getActionView(item);

        //search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (!TextUtils.isEmpty(s.trim())){
                    searchGroupChatList(s);
                }else {
                    loadGroupChatList();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (!TextUtils.isEmpty(s.trim())){
                    searchGroupChatList(s);
                }else {
                    loadGroupChatList();
                }
                return false;
            }
        });
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if (id==R.id.acciton_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        if (id==R.id.acciton_scan_qrcode){
            startActivity(new Intent(getActivity(), ScanQRCodeActivity.class));
        }

        if (id==R.id.action_create_group){
            startActivity(new Intent(getActivity(), GroupCreateActivity.class));
        }
//        if (id==R.id.acciton_myQrCode){
//            Toast.makeText(getActivity(), "1", Toast.LENGTH_SHORT).show();
//            startActivity(new Intent(getActivity(), MyQrCodeActivity.class));
//            getActivity().finish();
//        }
        return super.onOptionsItemSelected(item);
    }

    private void checkUserStatus() {
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if (user==null){
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }
}