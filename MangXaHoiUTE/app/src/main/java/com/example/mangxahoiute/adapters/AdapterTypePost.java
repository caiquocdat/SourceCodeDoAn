package com.example.mangxahoiute.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mangxahoiute.AddPostActivity;
import com.example.mangxahoiute.ChatActivity;
import com.example.mangxahoiute.R;
import com.example.mangxahoiute.TypePostActivity;
import com.example.mangxahoiute.models.ModelTypePost;
import com.example.mangxahoiute.models.ModelUsers;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class AdapterTypePost extends RecyclerView.Adapter<AdapterTypePost.MyHolder> {

    Context context;
    List<ModelTypePost> usersList;


    public AdapterTypePost(Context context, List<ModelTypePost> usersList) {
        this.context = context;
        this.usersList = usersList;
    }

    @NonNull
    @Override
    public AdapterTypePost.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.row_type_post,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterTypePost.MyHolder holder, int position) {

//        holder.mTypePost.setText(tPost);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tPost=usersList.get(position).gettPost();
//                if (tPost==null){
//                    tPost="a";
//                }
                SharedPreferences shff =context.getSharedPreferences("editTypePost", MODE_PRIVATE);
                String typePt=shff.getString("editPts","");
                if (typePt.equals("editPost")){
                    SharedPreferences shffs = context.getSharedPreferences("editTypePt", MODE_PRIVATE);
                    SharedPreferences.Editor editor=shffs.edit();
                    editor.putString("editPt","editPost");
                    editor.apply();
                }

                Intent intent = new Intent(context, AddPostActivity.class);
//                intent.putExtra("typePost",tPost);
                context.startActivity(intent);
                //

                SharedPreferences shf = context.getSharedPreferences("typepost1", MODE_PRIVATE);
                SharedPreferences.Editor editor=shf.edit();
                editor.putString("tpost",tPost);
                editor.apply();

                SharedPreferences sharedPreferences =context.getSharedPreferences("saveEditText", MODE_PRIVATE);
                String emailkt=sharedPreferences.getString("email","");
                String title=sharedPreferences.getString("title","");
                String image=sharedPreferences.getString("image","");
                String description=sharedPreferences.getString("description","");

                SharedPreferences sharedPreferencess =context.getSharedPreferences("saveEditText", MODE_PRIVATE);
                SharedPreferences.Editor editor12=sharedPreferencess.edit();
                editor12.putString("key","check");
                editor12.putString("email",emailkt);
                editor12.putString("title",title);
                editor12.putString("image",image);
                editor12.putString("description",description);
                editor12.apply();

                //Lưu editext của editpost của editpost khi nhấn chọn typepost
                SharedPreferences sharedPreferencesEditPost =context.getSharedPreferences("saveEditTextOfEditPost", MODE_PRIVATE);
                String emailktEdtPost=sharedPreferencesEditPost.getString("email","");
                String idEdtPost=sharedPreferencesEditPost.getString("idpost","");
                String titleEdtPost=sharedPreferencesEditPost.getString("title","");
                String imageEdtPost=sharedPreferencesEditPost.getString("image","");
                String descriptionEdtPost=sharedPreferencesEditPost.getString("description","");

                //Truyền dữ liệu của các edittext về cho edit của addpostactivity
                SharedPreferences sharedPreferencessEditPost =context.getSharedPreferences("saveEditTextOfEditPost", MODE_PRIVATE);
                SharedPreferences.Editor editorEditPost=sharedPreferencessEditPost.edit();
                editorEditPost.putString("key","check");
                editorEditPost.putString("email",emailktEdtPost);
                editorEditPost.putString("idpost",idEdtPost);
                editorEditPost.putString("title",titleEdtPost);
                editorEditPost.putString("image",imageEdtPost);
                editorEditPost.putString("description",descriptionEdtPost);
                editorEditPost.apply();

                //Truyền dữ liệu của các edittext về cho addpostactivity

               /// đang làm intent lấy dữ liệu từ typepostActivity


            }
        });
        ModelTypePost modelTypePost = usersList.get(position);
        holder.mTypePost.setText(modelTypePost.gettPost());
    }

    @Override
    public int getItemCount() {

        return usersList.size();
    }

    public static class MyHolder extends RecyclerView.ViewHolder{

        TextView mTypePost;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            mTypePost=itemView.findViewById(R.id.typePostTv);



        }
    }
}
