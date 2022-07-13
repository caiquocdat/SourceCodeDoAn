package com.example.mangxahoiute.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.mangxahoiute.AddPostActivity;
import com.example.mangxahoiute.LockedActivity;
import com.example.mangxahoiute.R;
import com.example.mangxahoiute.adapters.AdapterApprovePost;
import com.example.mangxahoiute.adapters.AdapterPost;
import com.example.mangxahoiute.models.ModelPost;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ApproveFragment extends Fragment {

    RecyclerView recyclerView;
    List<ModelPost> postList;
    AdapterApprovePost adapterPost;
    SearchView searchView;
    TextView listLockTv;


    public ApproveFragment() {
        // Required empty public constructor


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_approve, container, false);
        listLockTv=view.findViewById(R.id.listLockTv);
        recyclerView=view.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager= new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);
        postList= new ArrayList<>();
        listLockTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), LockedActivity.class));
            }
        });
        return view;
    }
    private void loadPostApproved() {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelPost modelPost=ds.getValue(ModelPost.class);
                    if (modelPost.getpStatus().contains("false")) {
                        postList.add(modelPost);
                    }
                    adapterPost= new AdapterApprovePost(getActivity(),postList);
                    recyclerView.setAdapter(adapterPost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(getActivity(),""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void searchPostApproved(String query) {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelPost modelPost=ds.getValue(ModelPost.class);
                    if (modelPost.getpStatus().contains("false")) {
                        if(modelPost.getpTitle().toLowerCase().contains(query.toLowerCase()) ||
                                modelPost.getpDescr().toLowerCase().contains(query.toLowerCase()) ||
                                modelPost.getuType().toLowerCase().contains(query.toLowerCase())
                                ||modelPost.getuName().toLowerCase().contains(query.toLowerCase())){
                        postList.add(modelPost);
                        }
                    }
                    adapterPost= new AdapterApprovePost(getActivity(),postList);
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
        menu.findItem(R.id.action_create_group).setVisible(false);

        MenuItem item= menu.findItem(R.id.acciton_search);
        searchView= (SearchView) MenuItemCompat.getActionView(item);

        //search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (!TextUtils.isEmpty(s.trim())){
                    searchPostApproved(s);
                }else {
                    loadPostApproved();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (!TextUtils.isEmpty(s.trim())){
                    searchPostApproved(s);
                }else {
                    loadPostApproved();
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public void onStart() {
        super.onStart();
        loadPostApproved();
        listLockTv.setText(Html.fromHtml("<p><u>Locked list of users</u></p>"));
    }
}