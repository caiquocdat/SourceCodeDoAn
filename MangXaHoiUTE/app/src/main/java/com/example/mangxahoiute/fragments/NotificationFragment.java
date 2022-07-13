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
import com.example.mangxahoiute.R;
import com.example.mangxahoiute.adapters.AdapterNotification;
import com.example.mangxahoiute.models.ModelNotification;
import com.example.mangxahoiute.models.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class NotificationFragment extends Fragment {

    RecyclerView notificationsRv;
    private FirebaseAuth firebaseAuth;
    private ArrayList<ModelNotification> notificationsList;
    private AdapterNotification adapterNotification;
    private SearchView searchView;

    public NotificationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view= inflater.inflate(R.layout.fragment_notification, container, false);
        notificationsRv=view.findViewById(R.id.notificationsRv);
        firebaseAuth=FirebaseAuth.getInstance();
        getAllNotifications();
        return view;
    }

    private void getAllNotifications() {
        notificationsList= new ArrayList<>();


        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Notifications")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        notificationsList.clear();
                        for (DataSnapshot ds:snapshot.getChildren()){
                            ModelNotification model=ds.getValue(ModelNotification.class);
                            if (!model.getpUid().equals(model.getsUid())){
                                notificationsList.add(model);
                            }
//                            Collections.sort(notificationsList, new Comparator<ModelUsers>() {
//                                @Override
//                                public int compare(ModelUsers o1, ModelUsers o2) {
//                                    return o1.get.compareToIgnoreCase(o2.getEmail());
//                                }
//                            });
                            Collections.sort(notificationsList, new Comparator<ModelNotification>() {
                                @Override
                                public int compare(ModelNotification o1, ModelNotification o2) {
                                    return o2.getTimestamp().compareToIgnoreCase(o1.getTimestamp());
                                }
                            });
                        }
                        adapterNotification= new AdapterNotification(getActivity(),notificationsList);
                        notificationsRv.setAdapter(adapterNotification);



                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true); //set menu in fragment
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
        menu.findItem(R.id.acciton_search).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);

        super.onCreateOptionsMenu(menu,inflater);
    }

}