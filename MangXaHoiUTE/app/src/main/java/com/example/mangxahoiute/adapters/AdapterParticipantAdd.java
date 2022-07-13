package com.example.mangxahoiute.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mangxahoiute.R;
import com.example.mangxahoiute.models.ModelUsers;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class AdapterParticipantAdd extends RecyclerView.Adapter<AdapterParticipantAdd.HolderParticipantAdd> {

    private Context context;
    private ArrayList<ModelUsers> userList;
    private String groupId,myGroupRole;
    private SearchView searchView;

    public AdapterParticipantAdd(Context context, ArrayList<ModelUsers> userList, String groupId, String myGroupRole) {
        this.context = context;
        this.userList = userList;
        this.groupId = groupId;
        this.myGroupRole = myGroupRole;
    }

    @NonNull
    @Override
    public HolderParticipantAdd onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.row_participant_add,parent,false);
        return new HolderParticipantAdd(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderParticipantAdd holder, int position) {
        ModelUsers modelUsers=userList.get(position);
        String name=modelUsers.getName();
        String email=modelUsers.getEmail();
        String image=modelUsers.getImage();
        String uid=modelUsers.getUid();

        holder.nameTv.setText(name);
        holder.emailTv.setText(email);
        try {
            Picasso.get().load(image).placeholder(R.drawable.ic_default_img).into(holder.avatarIv);
        }catch (Exception e){
            holder.avatarIv.setImageResource(R.drawable.ic_default_img);
        }

        checkIfAlreadyExists(modelUsers,holder);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Groups");
                ref.child(groupId).child("Participants").child(uid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    String hisPreviousRole=""+snapshot.child("role").getValue();
                                    String[] options;
                                    AlertDialog.Builder builder= new AlertDialog.Builder(context);
                                    builder.setTitle("Choose Option");
                                    if (myGroupRole.equals("creator")){
                                        if (hisPreviousRole.equals("admin")){
                                            options=new String[]{"Remove Admin","Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (which==0){
                                                        removeAdmin(modelUsers);
                                                    }else{
                                                        removeParticipant(modelUsers);
                                                    }
                                                }
                                            }).show();
                                        }
                                        else if(hisPreviousRole.equals("participant")){
                                            options = new String[]{"Make Admin","Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (which==0){
                                                        makeAdmin(modelUsers);
                                                    }else{
                                                        removeParticipant(modelUsers);
                                                    }
                                                }
                                            }).show();

                                        }
                                    }
                                    else if(myGroupRole.equals("admin")){
                                        if (hisPreviousRole.equals("creator")){
                                            Toast.makeText(context, "Creator of Group...", Toast.LENGTH_SHORT).show();
                                        }
                                        else if(hisPreviousRole.equals("admin")){
                                            options=new String[]{"Remove Admin","Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (which==0){
                                                        removeAdmin(modelUsers);
                                                    }else{
                                                        removeParticipant(modelUsers);
                                                    }
                                                }
                                            }).show();

                                        }
                                        else if(hisPreviousRole.equals("participant")){
                                            options=new String[]{"Make Admin","Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (which==0){
                                                        makeAdmin(modelUsers);
                                                    }else{
                                                        removeParticipant(modelUsers);
                                                    }
                                                }
                                            }).show();

                                        }

                                    }
                                }else{
                                    AlertDialog.Builder builder=new AlertDialog.Builder(context);
                                    builder.setTitle("Add Participant")
                                    .setMessage("Add this user in this group?")
                                    .setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            addParticipant(modelUsers);
                                        }
                                    })
                                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).show();

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }
        });

    }

    private void addParticipant(ModelUsers modelUsers) {
        String timestamp=""+System.currentTimeMillis();
        HashMap<String,String> hashMap=new HashMap<>();
        hashMap.put("uid",modelUsers.getUid());
        hashMap.put("role","participant");
        hashMap.put("timestamp",""+timestamp);

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelUsers.getUid()).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Added successfully...", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void makeAdmin(ModelUsers modelUsers) {
        HashMap<String,Object> hashMap= new HashMap<>();
        hashMap.put("role","admin");
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Participants").child(modelUsers.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "The user is now admin...", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void removeParticipant(ModelUsers modelUsers) {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Participants").child(modelUsers.getUid()).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    private void removeAdmin(ModelUsers modelUsers) {
        HashMap<String,Object> hashMap= new HashMap<>();
        hashMap.put("role","participant");
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupId).child("Participants").child(modelUsers.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "The user is no longer admin...", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkIfAlreadyExists(ModelUsers modelUsers, HolderParticipantAdd holder) {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelUsers.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            String hisRole=""+snapshot.child("role").getValue();
                            holder.statusTv.setText(hisRole);
                        }else{
                            holder.statusTv.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }


    class HolderParticipantAdd extends RecyclerView.ViewHolder{

        private ImageView avatarIv;
        private TextView nameTv,emailTv,statusTv;


        public HolderParticipantAdd(@NonNull View itemView) {
            super(itemView);
            avatarIv=itemView.findViewById(R.id.avatarIv);
            nameTv=itemView.findViewById(R.id.nameTv);
            emailTv=itemView.findViewById(R.id.emailTv);
            statusTv=itemView.findViewById(R.id.statusTv);
        }
    }
}
