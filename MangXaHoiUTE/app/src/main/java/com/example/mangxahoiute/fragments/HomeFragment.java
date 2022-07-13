package com.example.mangxahoiute.fragments;

import android.content.Context;
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

import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mangxahoiute.AddPostActivity;
import com.example.mangxahoiute.MainActivity;
import com.example.mangxahoiute.R;
import com.example.mangxahoiute.adapters.AdapterPost;
import com.example.mangxahoiute.adapters.AdapterTypePost;
import com.example.mangxahoiute.adapters.AdapterTypePostGridView;
import com.example.mangxahoiute.models.ModelLock;
import com.example.mangxahoiute.models.ModelPost;
import com.example.mangxahoiute.models.ModelTypePost;
import com.example.mangxahoiute.notifications.Data;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.protobuf.DescriptorProtos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;


public class HomeFragment extends Fragment {

    FirebaseAuth firebaseAuth;
    RecyclerView recyclerView,recyclerViewTypePostGridView;
    List<ModelPost> postList;
    List<ModelPost> postListCopy;
    //
    List<ModelTypePost> typePostList;
    AdapterPost adapterPost;
    DatabaseReference typePDbRef,typeRef;
    String typePt;
    public static String ddPts,tPts;
    public static String idDeletePost;

    AdapterTypePost adapterTypePost;
    //
    AdapterTypePostGridView adapterTypePostGridView;

    List<ModelTypePost> usersList;
    public static boolean tonTaiTrongList;
    String email;
    Context context;
    private SharedPreferences shf;
    public static String idPts,typePost;
    ModelPost modelPost;
    FrameLayout fr;
    TextView hienThiId,hienThiCategory,hienThiPosts;

    public HomeFragment() {
        // Required empty public constructor
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        firebaseAuth= FirebaseAuth.getInstance();
        //setup recyclerView của post bài đăng
        recyclerView=view.findViewById(R.id.postRecyclerview);
        hienThiCategory=view.findViewById(R.id.categoryTv);
        hienThiPosts=view.findViewById(R.id.postsTv);
//        hienThiId=view.findViewById(R.id.hienThiIdPost);
        LinearLayoutManager layoutManager= new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);

        recyclerViewTypePostGridView=view.findViewById(R.id.typePostRecyclerview);
        hienThiId=view.findViewById(R.id.hienThiIdPost);

//        GridLayoutManager layoutManager= new GridLayoutManager(getActivity(),2);
//        layoutManager.setStackFromEnd(true);
//        layoutManager.setReverseLayout(true);
//        recyclerViewTypePostGridView.setLayoutManager(layoutManager);
        recyclerViewTypePostGridView.setHasFixedSize(true);
        recyclerViewTypePostGridView.setLayoutManager(new GridLayoutManager(getActivity(),2));

        postList= new ArrayList<>();
        postListCopy= new ArrayList<>();
        //
        typePostList= new ArrayList<>();
        readData(new FirebaseCallback() {
            @Override
            public void onCallBack(List<ModelPost> list) {
//                Toast.makeText(context, list.get(0).getpId(), Toast.LENGTH_SHORT).show();
                if (list.size()==1){
                    idPts=list.get(0).getpId();
                    typePost=list.get(0).getpTypePost();
                    hienThiId.setText(idPts);
                    hienThiId.setHint(typePost);
                }
                else{
                    hienThiId.setText("");
                    hienThiId.setHint("");
                }
            }
        });



//        loadPost();
//        layGiaTriIdPost();
//        checkStatusPost();
//        Toast.makeText(getActivity(), layGiaTriIdPost(), Toast.LENGTH_SHORT).show();
        // Đã giảm thời gian từ 2 giây xuống 1 giây

        counter(2000);
        checkDeletePost();


        return view;
    }

    private void checkLocked() {
        String uid=FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid).child("Lock").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds:snapshot.getChildren()){
                    ModelLock modelLock= ds.getValue(ModelLock.class);
                    String status=modelLock.getStatus();
                    String reason=modelLock.getReasonLock();

                    if (status==null){

                    }
                    else if (status.equals("false")){
                        Toast.makeText(getActivity(), "Your account has been locked...", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getActivity(), MainActivity.class));
                        getActivity().finish();
//                        startActivity(new Intent(getActivity(), MainActivity.class));

                    }
//                    ModelNotification modelNotification=ds.getValue(ModelNotification.class);
//                    Toast.makeText(ThereProfileActivity.this, ""+modelNotification.getNotification(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkDeletePost() {
        SharedPreferences shf = getActivity().getSharedPreferences("getIDPt", MODE_PRIVATE);
        String idPost = shf.getString("Id", "");
//        Toast.makeText(context, ""+idPost, Toast.LENGTH_SHORT).show();
        typePDbRef = FirebaseDatabase.getInstance().getReference("Posts");
        Query userQuery = typePDbRef.orderByChild("pId").equalTo(idPost);
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount() == 0) {
                    SharedPreferences shf = context.getSharedPreferences("getIDPt", MODE_PRIVATE);
                    SharedPreferences.Editor editor = shf.edit();
                    editor.putString("Id", idDeletePost);
                    editor.putString("key", "key");
                    editor.apply();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    private void counter(int min){

        CountDownTimer timer= new CountDownTimer(min,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                //Đặt try catch
                try {
                readDataDelete(new FirebaseCallbackDelete() {
                    @Override
                    public void onCallBack(List<ModelPost> list) {
                        if (list.size()==0){
                            idDeletePost="";
                        }
                        else {
                            idDeletePost = list.get(list.size() - 1).getpId();
                            shf = context.getSharedPreferences("getIDPtLast", MODE_PRIVATE);
                            SharedPreferences.Editor editor = shf.edit();
                            editor.putString("Id", idDeletePost);
                            editor.putString("key", "");
                            editor.apply();
                        }

                    }
                });}catch (Exception e){

                }
                try {
                    ddPts=hienThiId.getText().toString();
                    //Gặp lỗi này thì chuyển lại StatusPost thành false và đăng nhập lại app
                    // Có thể do mạng, 1 tăng thời gian đếm ngược từ 2 lên 3,4 hoặc đăng nhập lại
                    tPts=hienThiId.getHint().toString();
                } catch (Exception e){
                    Toast.makeText(context, "Kiểm tra lại đường truyền mạng", Toast.LENGTH_SHORT).show();
                }

                hienThiCategory.setText("Category");
                hienThiPosts.setText("Posts");
                if (ddPts==""||tPts==""){
                    Toast.makeText(context, "Kiểm tra lại đường truyền mạng, tất cả bài đăng đã được phê duyệt", Toast.LENGTH_SHORT).show();
                }else{
                    tPts=hienThiId.getHint().toString();
                    checkTypePost(tPts);
                    checkStatusPost();
                    shf = context.getSharedPreferences("getIDPt",MODE_PRIVATE);
                    SharedPreferences.Editor editor=shf.edit();
                    editor.putString("Id",ddPts);
                    editor.putString("key","");
                    editor.apply();
                    loadPostApproved();
                }
            }
        };
        timer.start();
    }
    private void layGiaTriIdPost(){

//        SharedPreferences shf = getActivity().getSharedPreferences("getId", MODE_PRIVATE);
//        String typePt=shf.getString("Id","");
        FirebaseUser fUser= FirebaseAuth.getInstance().getCurrentUser();
        String emailPost=fUser.getEmail();
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                String IdPost="";

                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelPost modelPost=ds.getValue(ModelPost.class);
                    if (modelPost.getCheckNotification().equals("false") &&
                            modelPost.getuEmail().equals(emailPost)
                    ){

                        IdPost=modelPost.getpTime()+"";
//                        Toast.makeText(getActivity(), IdPost, Toast.LENGTH_SHORT).show();
//                        shf = context.getSharedPreferences("getIdPost",MODE_PRIVATE);
//                        SharedPreferences.Editor editor=shf.edit();
//                        editor.putString("Id",IdPost);
//                        editor.apply();
//                        HashMap<String,Object> result= new HashMap<>();
//                        result.put("checkNotification","false");
//                        result.put("uEmail",emailPost);
//                        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Posts");
//                        databaseReference.child(IdPost).updateChildren(result);

                    }

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//        SharedPreferences shf = getActivity().getSharedPreferences("getIdPost", MODE_PRIVATE);
//        String idPost=shf.getString("Id","");
//        return idPost;
    }

    private void readDataDelete(FirebaseCallbackDelete firebaseCallbackDelete){
        FirebaseUser fUser= FirebaseAuth.getInstance().getCurrentUser();
        String emailPost=fUser.getEmail();
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postListCopy.clear();
                String IdPost="";

                for (DataSnapshot ds: snapshot.getChildren()){
                    modelPost=ds.getValue(ModelPost.class);


                    if (
                            modelPost.getuEmail().equals(emailPost)
                    ){

                        IdPost=modelPost.getpTime()+"";
                        postListCopy.add(modelPost);
//
                    }
//


                }
                firebaseCallbackDelete.onCallBack(postListCopy);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private interface FirebaseCallbackDelete{
        void onCallBack(List<ModelPost> list);
    }

    private void readData(FirebaseCallback firebaseCallback){
        FirebaseUser fUser= FirebaseAuth.getInstance().getCurrentUser();
        String emailPost=fUser.getEmail();
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                String IdPost="";

                for (DataSnapshot ds: snapshot.getChildren()){
                    modelPost=ds.getValue(ModelPost.class);
                    if (modelPost.getCheckNotification().equals("false") &&
                            modelPost.getuEmail().equals(emailPost)
                    ){

                        IdPost=modelPost.getpTime()+"";
                        postList.add(modelPost);
//
                    }

                }
                firebaseCallback.onCallBack(postList);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private interface FirebaseCallback{
        void onCallBack(List<ModelPost> list);
    }
    private void checkStatusPost() {
//        try {
//            SharedPreferences shf = getContext().getSharedPreferences("typepost1",MODE_PRIVATE);
//            typePt=shf.getString("tpost","");
//
//        } catch (Exception e){
//
//        }
//        tPts=hienThiId.getHint().toString();
//        Toast.makeText(context, tPts, Toast.LENGTH_SHORT).show();

//        Toast.makeText(context, hienThiId.getHint().toString(), Toast.LENGTH_SHORT).show();
//        shf = context.getSharedPreferences("getIdPost", MODE_PRIVATE);
//        String idPost=shf.getString("Id","");
//        Toast.makeText(getActivity(), idPts, Toast.LENGTH_SHORT).show();
        typePDbRef=FirebaseDatabase.getInstance().getReference("Posts");
        Query userQuery=typePDbRef.orderByChild("pId").equalTo(hienThiId.getText().toString());
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds:snapshot.getChildren()){
                    String statusPost=""+ds.child("pStatus").getValue();
                    String checkNt=""+ds.child("checkNotification").getValue();
//                    Toast.makeText(context, statusPost, Toast.LENGTH_SHORT).show();
                    String timeStamp= String.valueOf(System.currentTimeMillis());
//                    Toast.makeText(AddPostActivity.this, ""+checkTypePost(txtTypePost), Toast.LENGTH_SHORT).show();
                    if (statusPost.equals("true")){
//                        tPts=hienThiId.getHint().toString();
//                        checkTypePost(tPts);
//                        HashMap<Object, String> hashMapTypePost = new HashMap<>();
//                        hashMapTypePost.put("tPost", txtTypePost);
//                        DatabaseReference reftPost = FirebaseDatabase.getInstance().getReference("TypePosts");
//                        reftPost.child(timeStamp).setValue(hashMapTypePost);
                        if (checkNt.equals("false")) {
                            Toast.makeText(context, "Your post has been approved by the admin", Toast.LENGTH_SHORT).show();
                            HashMap<String,Object> result= new HashMap<>();
                            result.put("checkNotification","true");
                            DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Posts");
                            databaseReference.child(hienThiId.getText().toString()).updateChildren(result);
                        }
                        loadPostApproved();
                    }
                    break;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkTypePost(String query) {
        FirebaseUser fUser= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("TypePosts");
        usersList= new ArrayList<>();
        adapterTypePost = new AdapterTypePost(getContext(),usersList);


//        String timeStamp= String.valueOf(System.currentTimeMillis());
//        HashMap<Object, String> hashMapTypePost = new HashMap<>();
//        hashMapTypePost.put("tPost", query);
//        DatabaseReference reftPost = FirebaseDatabase.getInstance().getReference("TypePosts");
//        reftPost.child(timeStamp).setValue(hashMapTypePost);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                usersList.clear();

                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelTypePost modelTypePost=ds.getValue(ModelTypePost.class);
                    if ((modelTypePost.gettPost().toLowerCase().equals(query.toLowerCase()))){
//                        Toast.makeText(AddPostActivity.this, "Đã tồn tại", Toast.LENGTH_SHORT).show();
//                        Toast.makeText(context, modelTypePost.gettPost()+"-"+query, Toast.LENGTH_SHORT).show();
                        tonTaiTrongList=true;
                        break;
                    }else{
                        tonTaiTrongList=false;
                    }
                }
//                Toast.makeText(context, tonTaiTrongList+"", Toast.LENGTH_SHORT).show();
                if (tonTaiTrongList==false){
//                    Toast.makeText(context, query, Toast.LENGTH_SHORT).show();
                    String timeStamp= String.valueOf(System.currentTimeMillis());
                    HashMap<Object, String> hashMapTypePost = new HashMap<>();
                    hashMapTypePost.put("tPost", query);
                    hashMapTypePost.put("image","");
                    DatabaseReference reftPost = FirebaseDatabase.getInstance().getReference("TypePosts");
                    reftPost.child(timeStamp).setValue(hashMapTypePost);
                }



                //refresh  adapter

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadPost() {
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelPost modelPost=ds.getValue(ModelPost.class);
                    postList.add(modelPost);
                    adapterPost= new AdapterPost(getActivity(),postList);
                    recyclerView.setAdapter(adapterPost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(),""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void loadPostApproved() {
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelPost modelPost=ds.getValue(ModelPost.class);
                    if (modelPost.getpStatus().contains("true")) {
                        postList.add(modelPost);
                    }
                    adapterPost= new AdapterPost(getActivity(),postList);
                    recyclerView.setAdapter(adapterPost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(getActivity(),""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTypePost() {
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("TypePosts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                typePostList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelTypePost modelTypePost=ds.getValue(ModelTypePost.class);
//                    Toast.makeText(context, modelTypePost.gettPost(), Toast.LENGTH_SHORT).show();
                    typePostList.add(modelTypePost);
                    adapterTypePostGridView= new AdapterTypePostGridView(getActivity(),typePostList);
                    recyclerViewTypePostGridView.setAdapter(adapterTypePostGridView);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(getActivity(),""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchPosts(String searchQuery){
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelPost modelPost=ds.getValue(ModelPost.class);
                    if (modelPost.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                    modelPost.getpDescr().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            modelPost.getuType().toLowerCase().contains(searchQuery.toLowerCase())
                            ||modelPost.getuName().toLowerCase().contains(searchQuery.toLowerCase())
                    ){
                        postList.add(modelPost);
                    }
                    adapterPost= new AdapterPost(getActivity(),postList);
                    recyclerView.setAdapter(adapterPost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(getActivity(),""+error.getMessage(), Toast.LENGTH_SHORT).show();
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
        menu.findItem(R.id.acciton_logout).setVisible(false);
        menu.findItem(R.id.acciton_myQrCode).setVisible(false);
        menu.findItem(R.id.acciton_scan_qrcode).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_add_participant_group).setVisible(false);
        menu.findItem(R.id.action_groupinfo).setVisible(false);
//        menu.findItem(R.id.acciton_add_post).setVisible(false);
        menu.findItem(R.id.acciton_add_post).setEnabled(false);
        menu.findItem(R.id.action_videocall).setVisible(false);
        CountDownTimer timer= new CountDownTimer(2000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                menu.findItem(R.id.acciton_add_post).setEnabled(true);
            }
        };
        timer.start();
        MenuItem item= menu.findItem(R.id.acciton_search);
        SearchView searchView= (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query)){
                    searchPosts(query);
                }else{
//                    loadPost();
                    loadPostApproved();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (!TextUtils.isEmpty(query)){
                    searchPosts(query);
                }else{
//                    loadPost();
                    loadPostApproved();
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
        }
//        chaylai:
//        {

            if (id == R.id.acciton_add_post) {
                SharedPreferences shf = getActivity().getSharedPreferences("getIDPt", MODE_PRIVATE);
                String idPost = shf.getString("Id", "");
//                String idPost=idDeletePost;
//                Toast.makeText(context, idDeletePost, Toast.LENGTH_SHORT).show();
                String key = shf.getString("key", "");
//                Toast.makeText(context, idPost, Toast.LENGTH_SHORT).show();
                if (idPost == "") {
                    startActivity(new Intent(getActivity(), AddPostActivity.class));
                } else {
                    typePDbRef = FirebaseDatabase.getInstance().getReference("Posts");
                    Query userQuery = typePDbRef.orderByChild("pId").equalTo(idPost);

                    userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            //Kiểm tra khi xóa bài post
//                            Toast.makeText(context, snapshot.getChildrenCount() + "" + idPost, Toast.LENGTH_SHORT).show();
                            if (snapshot.getChildrenCount() == 0) {
                                SharedPreferences shf = context.getSharedPreferences("getIDPt", MODE_PRIVATE);
                                SharedPreferences.Editor editor = shf.edit();
                                editor.putString("Id", idDeletePost);
                                editor.putString("key", "key");
                                editor.apply();
                            } else {
                                SharedPreferences shff = context.getSharedPreferences("getIDPt", MODE_PRIVATE);
                                SharedPreferences.Editor editors = shff.edit();
                                editors.putString("Id", idPost);
                                editors.putString("key", "");
                                editors.apply();
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    String statusPost = "" + ds.child("pStatus").getValue();
                                    String uidUserOfPost=""+ds.child("uid").getValue();
                                    String myUid=FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    if (statusPost.equals("false")&&(uidUserOfPost.equals(myUid))) {
                                        Toast.makeText(context, "Your post has not been approved by the admin, you cannot post new articles", Toast.LENGTH_SHORT).show();
                                    } else {
                                        startActivity(new Intent(getActivity(), AddPostActivity.class));
                                        SharedPreferences shffs = getActivity().getSharedPreferences("editTypePt", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = shffs.edit();
                                        editor.putString("editPt", "");
                                        editor.apply();

                                        SharedPreferences shffs2 = getActivity().getSharedPreferences("editTyPt", MODE_PRIVATE);
                                        SharedPreferences.Editor editor1 = shffs2.edit();
                                        editor1.putString("editPt", "addpost");
                                        editor1.apply();
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

//            String timeStamp= String.valueOf(System.currentTimeMillis());
//            HashMap<Object,String> hashMapTypePost= new HashMap<>();
//            hashMapTypePost.put("tPost", "Tìm việc");
//            DatabaseReference reftPost= FirebaseDatabase.getInstance().getReference("TypePosts");
//            reftPost.child(timeStamp).setValue(hashMapTypePost);


            }
            SharedPreferences shfs = getActivity().getSharedPreferences("getIDPt", MODE_PRIVATE);
            String idPostDelete = shfs.getString("Id", "");
            String keyDelete = shfs.getString("key", "");
//            if (keyDelete.equals("key")){
//                Toast.makeText(context, "ok", Toast.LENGTH_SHORT).show();
//                break chaylai;
//            }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        checkLocked();
        hienThiCategory.setText("Category");
        hienThiPosts.setText("Posts");
        loadPostApproved();

        //
        loadTypePost();
//        checkStatusPost();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context=context;

    }



}