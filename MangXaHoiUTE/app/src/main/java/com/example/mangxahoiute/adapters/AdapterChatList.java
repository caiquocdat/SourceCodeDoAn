package com.example.mangxahoiute.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.pm.PackageInfoCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mangxahoiute.ChatActivity;
import com.example.mangxahoiute.R;
import com.example.mangxahoiute.models.ModelUsers;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class AdapterChatList extends RecyclerView.Adapter<AdapterChatList.MyHolder> {
    Context context;
    List<ModelUsers> usersList;
    private HashMap<String,String> lastMessageMap;
    FirebaseAuth firebaseAuth;
    String myUid;

    public AdapterChatList(Context context, List<ModelUsers> usersList) {
        this.context = context;
        this.usersList = usersList;
        lastMessageMap = new HashMap<>();
        firebaseAuth=FirebaseAuth.getInstance();
        myUid=firebaseAuth.getUid();
    }



    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.row_chatlist,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        String hisUid=usersList.get(position).getUid();
        String userImage= usersList.get(position).getImage();
        String userName=usersList.get(position).getName();
        String type=usersList.get(position).getType();
        String lastMessage=lastMessageMap.get(hisUid);

        holder.nameTv.setText(userName);
        holder.typeTv.setText(type);

        holder.blockIv.setImageResource(R.drawable.ic_unblocked_green);
        checkIsBlocked(hisUid,holder,position);

        if (lastMessage==null || lastMessage.equals("default")){
            holder.lastMessageTv.setVisibility(View.GONE);
        }else{
            holder.lastMessageTv.setVisibility(View.VISIBLE);
            holder.lastMessageTv.setText(lastMessage);
        }

        try {
            Picasso.get().load(userImage).placeholder(R.drawable.ic_default_img).into(holder.profileIv);
        }catch (Exception e){
            Picasso.get().load(R.drawable.ic_default_img).into(holder.profileIv);
        }

        if (usersList.get(position).getOnlineStatus().equals("online")){
            holder.onlineStatusIv.setImageResource(R.drawable.circle_online);
        }else{
            holder.onlineStatusIv.setImageResource(R.drawable.circle_offline);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent= new Intent(context, ChatActivity.class);
//                intent.putExtra("hisUid",hisUid);
//                context.startActivity(intent);
                imBlockedORNot(hisUid);
            }
        });

        holder.blockIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (usersList.get(position).getIsBlocked().equals("true")){
                    unBlockUser(hisUid);
                }else{
                    blockUser(hisUid);
                }
            }
        });



    }

    private void imBlockedORNot(String hisUID){
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUID).child("BlockedUsers").orderByChild("uid").equalTo(myUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds:snapshot.getChildren()){
                            if (ds.exists()){
                                Toast.makeText(context, "You're blocked by that user, can't send message", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        Intent intent = new Intent(context, ChatActivity.class);
                        intent.putExtra("hisUid",hisUID);
                        context.startActivity(intent);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void checkIsBlocked(String hisUID, AdapterChatList.MyHolder holder, int position) {
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds:snapshot.getChildren()){
                            if (ds.exists()){
                                holder.blockIv.setImageResource(R.drawable.ic_blocked_red);
                                usersList.get(position).setIsBlocked("true");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void blockUser(String hisUID) {

        HashMap<String,String> hashMap= new HashMap<>();
        hashMap.put("uid",hisUID);


        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").child(hisUID).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Blocked Successfully...", Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void unBlockUser(String hisUID) {
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds:snapshot.getChildren()){
                            if (ds.exists()){
                                ds.getRef().removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(context, "Unblocked Successfully...", Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void setLastMessageMap(String userId,String lastMessage){
        lastMessageMap.put(userId,lastMessage);
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        ImageView profileIv,onlineStatusIv,blockIv;
        TextView nameTv,lastMessageTv,typeTv;


        public MyHolder(@NonNull View itemView) {
            super(itemView);
            profileIv=itemView.findViewById(R.id.profileIv);
            onlineStatusIv=itemView.findViewById(R.id.onlineStatusIv);
            nameTv=itemView.findViewById(R.id.nameTv);
            lastMessageTv=itemView.findViewById(R.id.lastMessgeTv);
            typeTv=itemView.findViewById(R.id.typeTv);
            blockIv=itemView.findViewById(R.id.blockIv);
        }
    }
}
