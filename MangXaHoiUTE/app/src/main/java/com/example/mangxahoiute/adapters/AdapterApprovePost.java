package com.example.mangxahoiute.adapters;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.format.DateFormat;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mangxahoiute.AddPostActivity;
import com.example.mangxahoiute.PostDetailActivity;
import com.example.mangxahoiute.R;
import com.example.mangxahoiute.ThereProfileActivity;
import com.example.mangxahoiute.models.ModelPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class AdapterApprovePost extends RecyclerView.Adapter<AdapterApprovePost.MyHolder> {

    Context context;
    List<ModelPost> postList;
    String myUid;
    DatabaseReference typePDbRef;
    private DatabaseReference likesRef;
    private DatabaseReference postsRef;
    boolean mProcessLike=false;
    ProgressDialog pd;

    public AdapterApprovePost(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;
        myUid= FirebaseAuth.getInstance().getCurrentUser().getUid();
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        postsRef =FirebaseDatabase.getInstance().getReference().child("Posts");
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.row_posts_approve,parent,false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        String uid=postList.get(position).getUid();
        String uEmail=postList.get(position).getuEmail();
        String uName=postList.get(position).getuName();
        String uType=postList.get(position).getuType();
        String uDp=postList.get(position).getuDp();
        String pId=postList.get(position).getpId();
        String pTitle=postList.get(position).getpTitle();
        String pDescription=postList.get(position).getpDescr();
        String pImage=postList.get(position).getpImage();
        String pTimeStamp=postList.get(position).getpTime();
        String pLikes=postList.get(position).getpLikes();
        String pComments=postList.get(position).getpComments();
        String pTypePost=postList.get(position).getpTypePost();

        Calendar calendar= Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        String pTime= DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();

        holder.pTypePostTv.setText(pTypePost);
        holder.uNameTv.setText(uName);
        holder.pTimeTv.setText(pTime);
        holder.uTypeTv.setText(uType);
        holder.pTitleTv.setText(pTitle);
        holder.pDescriptionTv.setText(pDescription);
        holder.pLikesTv.setText(pLikes+" Like");
        holder.pCommentsTv.setText(pComments+" Comment");
//        setLikes(holder,pId);

        try {
            Picasso.get().load(uDp).placeholder(R.drawable.ic_default_img).into(holder.uPictureIv);
        }catch (Exception e){

        }

        //hide ImageView when image noImage
        if (pImage.equals("noImage")){
            holder.pImageIv.setVisibility(View.GONE);
        }else{
            holder.pImageIv.setVisibility(View.VISIBLE);
            try {
                Picasso.get().load(pImage).into(holder.pImageIv);
            }catch (Exception e){

            }
        }

        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions(holder.moreBtn,uid,myUid,pId,pImage);

            }
        });
        holder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String myUid=FirebaseAuth.getInstance().getCurrentUser().getUid();
                if (!myUid.equals(uid)){
                    Intent intent= new Intent(context, ThereProfileActivity.class);
                    intent.putExtra("uid",uid);
                    context.startActivity(intent);
                }
            }
        });
    }
    private void showMoreOptions(ImageButton moreBtn, String uid, String myUid, String pId, String pImage) {
        PopupMenu popupMenu=new PopupMenu(context,moreBtn, Gravity.END);
//        if (uid.equals(myUid)){
//            //add items in menu
//            popupMenu.getMenu().add(Menu.NONE,0,0,"Delete");
//            popupMenu.getMenu().add(Menu.NONE,1,0,"Edit");
//        }
        popupMenu.getMenu().add(Menu.NONE,0,0,"Approve");
        popupMenu.getMenu().add(Menu.NONE,1,0,"No");


        //item click
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id= item.getItemId();
                if (id==0){
//                    beginDelete(pId,pImage);
                    approvePost(pId);


                }
//                else
//                if (id==1){
//                    SharedPreferences shf = context.getSharedPreferences("getIDPt", MODE_PRIVATE);
//                    String idPost=shf.getString("Id","");
////                        SharedPreferences shf = context.getSharedPreferences("getIDPtLast", MODE_PRIVATE);
////                        String idPost=shf.getString("Id","");
//                    typePDbRef = FirebaseDatabase.getInstance().getReference("Posts");
//                    Query userQuery = typePDbRef.orderByChild("pId").equalTo(idPost);
//                    userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            if (snapshot.getChildrenCount()==0){
//                                Toast.makeText(context, "Your post has not been approved by the admin, you cannot post new articles", Toast.LENGTH_SHORT).show();
//                            }else{
//                                for (DataSnapshot ds : snapshot.getChildren()) {
//                                    String statusPost = "" + ds.child("pStatus").getValue();
//                                    if (statusPost.equals("false")) {
//                                        Toast.makeText(context, "Your post has not been approved by the admin, you cannot update articles", Toast.LENGTH_SHORT).show();
//                                    } else {
//                                        Intent intent = new Intent(context, AddPostActivity.class);
//                                        intent.putExtra("key", "editPost");
//                                        intent.putExtra("editPostId", pId);
//                                        context.startActivity(intent);
//
//                                        SharedPreferences shffs2 = context.getSharedPreferences("editTyPt", MODE_PRIVATE);
//                                        SharedPreferences.Editor editor1 = shffs2.edit();
//                                        editor1.putString("editPt", "editpost");
//                                        editor1.apply();
//
//
//                                    }
//                                }
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//
//                        }
//                    });
//
//
//                }
                else if(id==1){
//                    Intent intent=new Intent(context, PostDetailActivity.class);
//                    intent.putExtra("postId",pId);
//                    context.startActivity(intent);
                    openRecoverDialog(Gravity.CENTER,uid,pId,pImage);

                }

                return false;
            }
        });
        //show menu
        popupMenu.show();

    }
    private void openRecoverDialog(int center,String uid,String pId,String pImage) {
        final Dialog dialog= new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_dialog_reason_disapproval);
        Window window=dialog.getWindow();
        if (window==null){
            return;
        }
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowAttributes= window.getAttributes();
        windowAttributes.gravity=center;
        window.setAttributes(windowAttributes);
        if (Gravity.BOTTOM ==  center){
            dialog.setCancelable(true);
        }
        else{
            dialog.setCancelable(false);
        }
        EditText edtReason=dialog.findViewById(R.id.edt_reason);
        Button btnCancel=dialog.findViewById(R.id.btn_cancel);
        Button btnOK=dialog.findViewById(R.id.btn_ok);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beginDelete(pId,pImage);
                String reason=edtReason.getText().toString().trim();
                addToHisNotifications(""+uid,""+pId,"Your post was not approved because: "+reason);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void addToHisNotifications(String hisUid, String pId, String notification) {
        String timestamp=""+System.currentTimeMillis();
        HashMap<Object,String> hashMap= new HashMap<>();
        hashMap.put("pId",pId);
        hashMap.put("timestamp",timestamp);
        hashMap.put("pUid",hisUid);
        hashMap.put("notification",notification);
        hashMap.put("sUid",myUid);
//        hashMap.put("sName",myUid);
//        hashMap.put("sEmail",myUid);
//        hashMap.put("sImage",myUid);
//        hashMap.put("sType",myUid);

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUid).child("Notifications").child(timestamp).setValue(hashMap)
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

    private void approvePost(String pId) {
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("pStatus","true");
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");
        ref.child(pId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Successful Approval", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void beginDelete(String pId, String pImage) {
        if (pImage.equals("noImage")){
            deleteWithoutImage(pId);
        }else{
            deleteWithImage(pId,pImage);
        }
    }

    private void deleteWithImage(String pId, String pImage) {
        ProgressDialog pd= new ProgressDialog(context);
        pd.setMessage("Deleting...");

        StorageReference picRef= FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Query fquery=FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds :snapshot.getChildren()){
                                    ds.getRef().removeValue();
                                }
//                                Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show();
                                pd.dismiss();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
            }
        });
    }

    private void deleteWithoutImage(String pId) {
        ProgressDialog pd= new ProgressDialog(context);
        pd.setMessage("Deleting...");
        Query fquery=FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds :snapshot.getChildren()){
                    ds.getRef().removeValue();
                }
//                Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public int getItemCount() {
        return postList.size();
    }


    class MyHolder extends RecyclerView.ViewHolder{
        ImageView uPictureIv,pImageIv;
        TextView uNameTv,pTimeTv,pTitleTv,pDescriptionTv,pLikesTv,uTypeTv,pCommentsTv,pTypePostTv;
        ImageButton moreBtn;
        Button likeBtn,commentBtn,shareBtn;
        LinearLayout profileLayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            uPictureIv=itemView.findViewById(R.id.uPictureIv);
            pTypePostTv=itemView.findViewById(R.id.pTypePostTv);
            pImageIv=itemView.findViewById(R.id.pImageIv);
            uNameTv=itemView.findViewById(R.id.uNameTv);
            uTypeTv=itemView.findViewById(R.id.uTypeTv);
            pTimeTv=itemView.findViewById(R.id.pTimeTv);
            pTitleTv=itemView.findViewById(R.id.pTitleTv);
            pDescriptionTv=itemView.findViewById(R.id.pDescriptionTv);
            pLikesTv=itemView.findViewById(R.id.pLikesTv);
            pCommentsTv=itemView.findViewById(R.id.pCommentsTv);
            moreBtn=itemView.findViewById(R.id.moreApproveBtn);
            likeBtn=itemView.findViewById(R.id.likeBtn);
            commentBtn=itemView.findViewById(R.id.commentBtn);
            shareBtn=itemView.findViewById(R.id.shareBtn);
            profileLayout=itemView.findViewById(R.id.profileLayout);
        }
    }
}
