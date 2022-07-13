package com.example.mangxahoiute.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mangxahoiute.R;
import com.example.mangxahoiute.ThereProfileActivity;
import com.example.mangxahoiute.models.ModelComment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterComment extends RecyclerView.Adapter<AdapterComment.MyHolder>{

    Context context;
    List<ModelComment> commentList;
    String myUid,postId;

    public AdapterComment(Context context, List<ModelComment> commentList, String myUid, String postId) {
        this.context = context;
        this.commentList = commentList;
        this.myUid = myUid;
        this.postId = postId;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.row_comments,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        String uid=commentList.get(position).getUid();
        String name=commentList.get(position).getuName();
        String email=commentList.get(position).getuEmail();
        String image=commentList.get(position).getuDp();
        String cid=commentList.get(position).getcId();
        String comment=commentList.get(position).getComment();
        String timestamp=commentList.get(position).getTimestamp();
        String type=commentList.get(position).getuType();

        Calendar calendar= Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String pTime= DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();

        holder.nameTv.setText(name);
        holder.commentTv.setText(comment);
        holder.timeTv.setText(pTime);
        holder.typeTv.setText(type);
        try {
            Picasso.get().load(image).placeholder(R.drawable.ic_default_img).into(holder.avatarIv);
        }catch (Exception e){
            Picasso.get().load(image).placeholder(R.drawable.ic_default_img).into(holder.avatarIv);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myUid.equals(uid)){
                    AlertDialog.Builder builder=new AlertDialog.Builder(v.getRootView().getContext());
                    builder.setTitle("Delete");
                    builder.setMessage("Are you sure to delete this comment?");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteComment(cid);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                }else{
//                    Toast.makeText(context, "Can't delete other's comment...", Toast.LENGTH_SHORT).show();
                    Intent intent= new Intent(context, ThereProfileActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("uid",uid);
                    context.startActivity(intent);
                }
            }
        });

    }

    private void deleteComment(String cid) {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.child("Comments").child(cid).removeValue();

        //update the comments count
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String comments=""+snapshot.child("pComments").getValue();
                int newCommentVal= Integer.parseInt(comments)-1;
                ref.child("pComments").setValue(""+newCommentVal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        ImageView avatarIv;
        TextView nameTv,commentTv,timeTv,typeTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            avatarIv=itemView.findViewById(R.id.avatarIv);
            nameTv=itemView.findViewById(R.id.nameTv);
            commentTv=itemView.findViewById(R.id.commentTv);
            timeTv=itemView.findViewById(R.id.timeTv);
            typeTv=itemView.findViewById(R.id.typeTv);
        }
    }
}
