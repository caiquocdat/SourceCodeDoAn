package com.example.mangxahoiute.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Layout;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mangxahoiute.PostDetailActivity;
import com.example.mangxahoiute.R;
import com.example.mangxahoiute.models.ModelNotification;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AdapterNotification extends RecyclerView.Adapter<AdapterNotification.HolderNotification> {
    private Context context;
    private ArrayList<ModelNotification> notificationsList;
    private FirebaseAuth firebaseAuth;


    public AdapterNotification(Context context, ArrayList<ModelNotification> notificationsList) {
        this.context = context;
        this.notificationsList = notificationsList;
        firebaseAuth=FirebaseAuth.getInstance();

    }

    @NonNull
    @Override
    public HolderNotification onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.row_notification,parent,false);

        return new HolderNotification(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderNotification holder, int position) {

        ModelNotification model=notificationsList.get(position);
        String name=model.getsName();
        String notification=model.getNotification();
        String type=model.getsType();
        String timestamp=model.getTimestamp();
        String image=model.getsImage();
        String senderUid=model.getsUid();
        String pId=model.getpId();
        Calendar calendar= Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String pTime= DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(senderUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds:snapshot.getChildren()){
                            String name=""+ds.child("name").getValue();
                            String image=""+ds.child("image").getValue();
                            String email=""+ds.child("email").getValue();
                            String type=""+ds.child("type").getValue();

                            model.setsName(name);
                            model.setsImage(image);
                            model.setsEmail(email);
                            model.setsType(type);

                            holder.nameTv.setText(name);
                            holder.typeTv.setText(type);
                            try {
                                Picasso.get().load(image).placeholder(R.drawable.ic_default_img).into(holder.avatarIv);
                            }catch (Exception e){
                                holder.avatarIv.setImageResource(R.drawable.ic_default_img);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });




        holder.notificationTv.setText(notification);
        holder.timeTv.setText(pTime);

        //click notification to open post
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId",pId);
                context.startActivity(intent);

            }
        });
        // delete post
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure to delete this notification?");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
                        ref.child(firebaseAuth.getUid()).child("Notifications").child(timestamp).removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(context, "Notification deleted...", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
                return false;
            }
        });


    }

    @Override
    public int getItemCount() {
        return notificationsList.size();
    }

    class HolderNotification extends RecyclerView.ViewHolder{
        ImageView avatarIv;
        TextView nameTv,notificationTv,timeTv,typeTv;

        public HolderNotification(@NonNull View itemView) {
            super(itemView);
            avatarIv=itemView.findViewById(R.id.avatarIv);
            nameTv=itemView.findViewById(R.id.nameTv);
            timeTv=itemView.findViewById(R.id.timeTv);
            typeTv=itemView.findViewById(R.id.typeTv);
            notificationTv=itemView.findViewById(R.id.notificationTv);

        }
    }
}
