package com.example.mangxahoiute.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.mangxahoiute.GroupCreateActivity;
import com.example.mangxahoiute.MainActivity;
import com.example.mangxahoiute.R;
import com.example.mangxahoiute.ScanQRCodeActivity;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;


public class UsersFragment extends Fragment {

    RecyclerView recyclerView;
    AdapterUser adapterUser;
    List<ModelUsers> usersList;
    FirebaseAuth firebaseAuth;
    RecyclerView.LayoutManager layoutManager;
    SearchView searchView;
    private static String resultQRCode;

    public UsersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_users, container, false);
        firebaseAuth=FirebaseAuth.getInstance();
        recyclerView= view.findViewById(R.id.users_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        // Dùng để tạo gridView
//        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(),2));
        usersList= new ArrayList<>();


        getAllUsers();



        return  view;
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
                     if (!modelUsers.getUid().equals(fUser.getUid())){
                         usersList.add(modelUsers);
                     }
                     Collections.sort(usersList, new Comparator<ModelUsers>() {
                         @Override
                         public int compare(ModelUsers o1, ModelUsers o2) {
                             return o1.getEmail().compareToIgnoreCase(o2.getEmail());
                         }
                     });
                     adapterUser = new AdapterUser(getActivity(),usersList);
                     recyclerView.setAdapter(adapterUser);
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
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();

        }
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
        menu.findItem(R.id.action_create_group).setVisible(false);
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
       super.onCreateOptionsMenu(menu,inflater);
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
                    if (!modelUsers.getUid().equals(fUser.getUid())){
                        if (modelUsers.getName().toLowerCase().contains(query.toLowerCase())
                         || modelUsers.getEmail().toLowerCase().contains(query.toLowerCase())
                        || modelUsers.getType().toLowerCase().contains(query.toLowerCase())){
                            usersList.add(modelUsers);
                        }

                    }
                    adapterUser = new AdapterUser(getActivity(),usersList);
                    //refresh  adapter
                    adapterUser.notifyDataSetChanged();
                    recyclerView.setAdapter(adapterUser);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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

    @Override
    public void onStart() {
        SharedPreferences shf = getActivity().getSharedPreferences("ResultQRCode", MODE_PRIVATE);
//                String idPost = shf.getString("Id", "");
        resultQRCode = shf.getString("Result", "");
//        Toast.makeText(getActivity(), ""+resultQRCode, Toast.LENGTH_SHORT).show();

        if (resultQRCode==null){
            resultQRCode="";
        }
        searchUsers(resultQRCode);
        super.onStart();
    }

    //Đang gặp lỗi chổ lấy resultQRCode khi scanner sang searchview


    @Override
    public void onPause() {
        SharedPreferences sharedPreferences1 =getContext().getSharedPreferences("ResultQRCode", MODE_PRIVATE);
        SharedPreferences.Editor editor1a=sharedPreferences1.edit();
        editor1a.remove("Result");
        editor1a.apply();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        SharedPreferences sharedPreferences1 =getContext().getSharedPreferences("ResultQRCode", MODE_PRIVATE);
        SharedPreferences.Editor editor1a=sharedPreferences1.edit();
        editor1a.remove("Result");
        editor1a.apply();
        super.onDestroy();
    }
}