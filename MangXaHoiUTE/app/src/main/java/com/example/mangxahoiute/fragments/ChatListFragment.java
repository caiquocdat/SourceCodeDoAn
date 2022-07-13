package com.example.mangxahoiute.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.Display;
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
import com.example.mangxahoiute.adapters.AdapterChatList;
import com.example.mangxahoiute.models.ModelChat;
import com.example.mangxahoiute.models.ModelChatList;
import com.example.mangxahoiute.models.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatListFragment extends Fragment {


    FirebaseAuth firebaseAuth;
    RecyclerView recyclerView;
    List<ModelChatList> chatlistList;
    List<ModelUsers> usersList;
    DatabaseReference reference;
    FirebaseUser currentUser;
    AdapterChatList adapterChatList;
    SearchView searchView;


    public ChatListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser=FirebaseAuth.getInstance().getCurrentUser();

        recyclerView=view.findViewById(R.id.recyclerView);
        chatlistList= new ArrayList<>();
        reference= FirebaseDatabase.getInstance().getReference("ChatList").child(currentUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatlistList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelChatList chatList=ds.getValue(ModelChatList.class);
                    chatlistList.add(chatList);
                }
                loadChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }

    private void searchChat(String query) {
        usersList = new ArrayList<>();
        reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelUsers user=ds.getValue(ModelUsers.class);
                    for (ModelChatList chatList:chatlistList){
                        if (user.getUid()!=null&&user.getUid().equals(chatList.getId())){
                            if (user.getName().toLowerCase().contains(query.toLowerCase())
                                    ||user.getType().toLowerCase().contains(query.toLowerCase())
                            ) {
                                usersList.add(user);
                                break;
                            }
                        }
                    }
                    adapterChatList= new AdapterChatList(getContext(),usersList);
                    recyclerView.setAdapter(adapterChatList);
                    for (int i=0;i<usersList.size();i++){
                        lastMessage(usersList.get(i).getUid());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadChats() {
        usersList = new ArrayList<>();
        reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelUsers user=ds.getValue(ModelUsers.class);
                    for (ModelChatList chatList:chatlistList){
                        if (user.getUid()!=null&&user.getUid().equals(chatList.getId())){
                            usersList.add(user);
                            break;
                        }
                    }
                    adapterChatList= new AdapterChatList(getContext(),usersList);
                    recyclerView.setAdapter(adapterChatList);
                    for (int i=0;i<usersList.size();i++){
                        lastMessage(usersList.get(i).getUid());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void lastMessage(String userId) {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String theLastMessage="default";
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelChat chat=ds.getValue(ModelChat.class);
                    if (chat==null){
                        continue;
                    }
                    String sender=chat.getSender();
                    String receiver=chat.getReceiver();
                    if (sender==null ||receiver==null){
                        continue;
                    }
                    if (chat.getReceiver().equals(currentUser.getUid())&& chat.getSender().equals(userId)
                    ||chat.getReceiver().equals(userId)&& chat.getSender().equals(currentUser.getUid())){

                        if (chat.getType().equals("image")){
                            theLastMessage="Sent a photo";

                        }else{
                            theLastMessage=chat.getMessage();
                        }


                    }
                }
                adapterChatList.setLastMessageMap(userId,theLastMessage);
                adapterChatList.notifyDataSetChanged();
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
                    searchChat(s);
                }else {
                    loadChats();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (!TextUtils.isEmpty(s.trim())){
                    searchChat(s);
                }else {
                    loadChats();
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
        }else if (id==R.id.action_create_group){
            startActivity(new Intent(getActivity(), GroupCreateActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}