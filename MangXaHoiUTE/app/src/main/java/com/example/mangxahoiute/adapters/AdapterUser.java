package com.example.mangxahoiute.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mangxahoiute.ChatActivity;
import com.example.mangxahoiute.R;
import com.example.mangxahoiute.ThereProfileActivity;
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

public class AdapterUser extends RecyclerView.Adapter<AdapterUser.MyHolder> {
    Context context;
    List<ModelUsers> usersList;

    FirebaseAuth firebaseAuth;
    String myUid;

    public AdapterUser(Context context, List<ModelUsers> usersList) {
        this.context = context;
        this.usersList = usersList;

        firebaseAuth=FirebaseAuth.getInstance();
        myUid=firebaseAuth.getUid();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.row_users,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        String hisUID=usersList.get(position).getUid();
        String userImage=usersList.get(position).getImage();
        String userName=usersList.get(position).getName();
        String userEmail=usersList.get(position).getEmail();
        String userType=usersList.get(position).getType();

        holder.mNameTv.setText(userName);
        holder.mEmailTv.setText(userEmail);
        holder.mTypeTv.setText(userType);
        try {
            Picasso.get().load(userImage).placeholder(R.drawable.ic_default_img).into(holder.mAvatarIv);
        }
        catch (Exception e){

        }
        holder.blockIv.setImageResource(R.drawable.ic_unblocked_green);
        checkIsBlocked(hisUID,holder,position);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String myUid=FirebaseAuth.getInstance().getCurrentUser().getUid();
                if (!myUid.equals(hisUID)) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setItems(new String[]{"Profile", "Chat"}, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {
                                //profile
                                Intent intent = new Intent(context, ThereProfileActivity.class);
                                intent.putExtra("uid", hisUID);
                                context.startActivity(intent);
                            }
                            if (which == 1) {
                                //chat
//                            Intent intent = new Intent(context, ChatActivity.class);
//                            intent.putExtra("hisUid",hisUID);
//                            context.startActivity(intent);
                                imBlockedORNot(hisUID);
                            }
                        }
                    });
                    builder.create().show();
                }
            }
        });
        holder.blockIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (usersList.get(position).getIsBlocked().equals("true")){
                    unBlockUser(hisUID);
                }else{
                    blockUser(hisUID);
                }
            }
        });
    }
    private void imBlockedORNot(String hisUID){
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
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

    private void checkIsBlocked(String hisUID, MyHolder holder, int position) {
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

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        ImageView mAvatarIv,blockIv;
        TextView mNameTv,mEmailTv,mTypeTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            blockIv=itemView.findViewById(R.id.blockIv);
            mAvatarIv=itemView.findViewById(R.id.avatarIv);
            mNameTv=itemView.findViewById(R.id.nameTv);
            mEmailTv=itemView.findViewById(R.id.emailTv);
            mTypeTv=itemView.findViewById(R.id.typeTv);


        }
    }
}
