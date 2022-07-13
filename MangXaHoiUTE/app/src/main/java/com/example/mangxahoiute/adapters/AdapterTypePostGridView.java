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
import androidx.recyclerview.widget.RecyclerView;

import com.example.mangxahoiute.R;
import com.example.mangxahoiute.ShowPostByCategory;
import com.example.mangxahoiute.models.ModelTypePost;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterTypePostGridView extends RecyclerView.Adapter<AdapterTypePostGridView.MyHolder> {
    Context context;
    List<ModelTypePost> usersList;

    public AdapterTypePostGridView(Context context, List<ModelTypePost> usersList) {
        this.context = context;
        this.usersList = usersList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.row_type_post_gridview,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        String tPost=usersList.get(position).gettPost();
        String imageType=usersList.get(position).getImage();
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tPost=usersList.get(position).gettPost();
                Intent intent= new Intent(context, ShowPostByCategory.class);
                intent.putExtra("typepost",tPost);
                context.startActivity(intent);
            }
        });
        ModelTypePost modelTypePost = usersList.get(position);
        holder.mTypePost.setText(modelTypePost.gettPost());
        try {
            Picasso.get().load(imageType).placeholder(R.drawable.ic_default_img).into(holder.mAvatarIv);
        }
        catch (Exception e){

        }

    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public  static class MyHolder extends RecyclerView.ViewHolder{
        TextView mTypePost;
        ImageView mAvatarIv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            mTypePost=itemView.findViewById(R.id.typePostTv);
            mAvatarIv=itemView.findViewById(R.id.avatarIv);
        }
    }
}
