package com.example.mangxahoiute.fragments;

import android.Manifest;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentBreadCrumbs;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.service.autofill.Dataset;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.mangxahoiute.AddPostActivity;
import com.example.mangxahoiute.LoginActivity;
import com.example.mangxahoiute.MainActivity;
import com.example.mangxahoiute.MyQrCodeActivity;
import com.example.mangxahoiute.R;
import com.example.mangxahoiute.adapters.AdapterPost;
import com.example.mangxahoiute.models.ModelPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;


public class ProfileFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    DatabaseReference typePDbRef;
    Context context;
    //storage
    StorageReference storageReference;
    // path where images of user profile and cover will be stored
    String storagePath="Users_Profile_Cover_Imgs/";
    //View from xml
    ImageView avatarIv,coverIv;
    TextView nameTv,emailTv,phoneTv,typeTv;
    FloatingActionButton fab;
    RecyclerView postsRecyclerView;

    ProgressDialog pd;
    private static final int CAMERA_REQUEST_CODE=100;
    private static final int STORAGE_REQUEST_CODE=200;
    private static final int IMAGE_PICK_GALLERY_CODE=300;
    private static final int IMAGE_PICK_CAMERA_CODE=400;

    String cameraPermissions[];
    String storagePermissions[];


    List<ModelPost> postList;
    AdapterPost adapterPost;
    String uid;

    //uri of picked image
    Uri image_uri;
    //check profile or cover
    String profileOrCoverPhoto;

    public ProfileFragment() {
        // Required empty public constructor
    }





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view =inflater.inflate(R.layout.fragment_profile, container, false);
        firebaseAuth=FirebaseAuth.getInstance();
        user=firebaseAuth.getCurrentUser();
        firebaseDatabase=FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference("Users");
        storageReference= FirebaseStorage.getInstance().getReference();
        //init arrays of permissions
        cameraPermissions= new String[] {Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions= new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        //init views
        avatarIv=view.findViewById(R.id.avatarIv);
        coverIv=view.findViewById(R.id.coverIv);
        nameTv=view.findViewById(R.id.nameTv);
        emailTv=view.findViewById(R.id.emailTv);
        phoneTv=view.findViewById(R.id.phoneTv);
        typeTv=view.findViewById(R.id.typeTv);
        fab=view.findViewById(R.id.fab);
        postsRecyclerView=view.findViewById(R.id.recyclerview_posts);



        pd=new ProgressDialog(getActivity());
        new ReadJSONMain().execute("https://pastebin.com/raw/dXGqwmqf");
        Query query=databaseReference.orderByChild("email").equalTo(user.getEmail());

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check until required data get
                for (DataSnapshot ds:snapshot.getChildren()){
                    //get data
                    String name=""+ds.child("name").getValue();
                    String type=""+ds.child("type").getValue();
                    String email=""+ds.child("email").getValue();
                    String phone=""+ds.child("phone").getValue();
                    String image=""+ds.child("image").getValue();
                    String cover=""+ds.child("cover").getValue();
                    //set data
                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);
                    typeTv.setText(type);
                    try {
                        // if image is received then set
                        Picasso.get().load(image).into(avatarIv);
                    }
                    catch (Exception e){
                        // if there is any exception while getting image then set default
                        Picasso.get().load(R.drawable.ic_default_img_white).into(avatarIv);
                    }
                    try {
                        // if image is received then set
                        Picasso.get().load(cover).into(coverIv);
                    }
                    catch (Exception e){
                        // if there is any exception while getting image then set default

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });

        postList= new ArrayList<>();
        checkUserStatus();
        loadMyPosts();
        return view;
    }

    private void loadMyPosts() {
        LinearLayoutManager layoutManager= new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        postsRecyclerView.setLayoutManager(layoutManager);

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
        Query query=ref.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelPost myPosts=ds.getValue(ModelPost.class);
                    postList.add(myPosts);
                    adapterPost= new AdapterPost(getActivity(),postList);
                    postsRecyclerView.setAdapter(adapterPost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void searchMyPosts(String searchQuery) {
        LinearLayoutManager layoutManager= new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        postsRecyclerView.setLayoutManager(layoutManager);

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
        Query query=ref.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelPost myPosts=ds.getValue(ModelPost.class);
                    if (myPosts.getpTitle().toLowerCase().contains(searchQuery.toLowerCase())||
                    myPosts.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())){
                        postList.add(myPosts);
                    }
                    adapterPost= new AdapterPost(getActivity(),postList);
                    postsRecyclerView.setAdapter(adapterPost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private boolean checkStoragePermission(){
        boolean result= ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==(PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestStoragePermission(){
        requestPermissions(storagePermissions,STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result= ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.CAMERA)
                ==(PackageManager.PERMISSION_GRANTED);
        boolean result1= ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==(PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }
    private void requestCameraPermission(){
      requestPermissions(cameraPermissions,CAMERA_REQUEST_CODE);
    }
    private void showEditProfileDialog() {
        String option[]={"Edit Profile Picture","Edit Cover Photo","Edit Name","Edit Phone","Edit Type","Change Password"};
        AlertDialog.Builder builder= new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose Action");
        builder.setItems(option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which==0){
                    pd.setMessage("Updating Profile Picture");
                    profileOrCoverPhoto="image";
                    showImagePicDialog();

                }else if(which==1){
                    pd.setMessage("Updating Cover Photo");
                    profileOrCoverPhoto="cover";
                    showImagePicDialog();

                }else if(which==2){
                    pd.setMessage("Updating Name");
//                    showNamePhoneUpdateDialog("name");
                    showNameUpdateDialogCopy("name");

                }else if(which==3){
                    pd.setMessage("Updating Phone");
//                    showNamePhoneUpdateDialog("phone");
                    showPhoneUpdateDialogCopy("phone");
                } else if(which==4){
                    pd.setMessage("Updating Type");
//                    showNamePhoneUpdateDialog("phone");
                    showTypeUpdateDialogCopy("type");
                }else if(which==5){
                    pd.setMessage("Changing Password");
//                    showNamePhoneUpdateDialog("phone");
                    showChangePasswordDialog();
                }
            }
        });
        builder.create().show();
    }

    private void showChangePasswordDialog() {

        View view=LayoutInflater.from(getActivity()).inflate(R.layout.dialog_update_password,null);

        EditText passwordEt=view.findViewById(R.id.passwordEt);
        EditText newPasswordEt=view.findViewById(R.id.newPasswordEt);
        Button updatePasswordBtn=view.findViewById(R.id.updatePasswordBtn);



        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setView(view);
        AlertDialog dialog=builder.create();
        dialog.show();

        updatePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPassword=passwordEt.getText().toString().trim();
                String newPassword=newPasswordEt.getText().toString().trim();
                if (TextUtils.isEmpty(oldPassword)){
                    Toast.makeText(getActivity(), "Enter your current password...", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (newPassword.length()<6){
                    Toast.makeText(getActivity(), "Password length must atleast 6 characters...", Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
                updatePassword(oldPassword,newPassword);

            }
        });

    }

    private void updatePassword(String oldPassword, String newPassword) {
        pd.show();
        FirebaseUser user=firebaseAuth.getCurrentUser();
        AuthCredential authCredential= EmailAuthProvider.getCredential(user.getEmail(),oldPassword);
        user.reauthenticate(authCredential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        user.updatePassword(newPassword)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        pd.dismiss();
                                        Toast.makeText(getActivity(), "Password Updated...", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                pd.dismiss();
                                Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void showPhoneUpdateDialogCopy(String key){
        final Dialog dialog= new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_dialog_update_phone);
        Window window=dialog.getWindow();
        if (window==null){
            return;
        }
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowAttributes= window.getAttributes();
//        windowAttributes.gravity=center;
        window.setAttributes(windowAttributes);
        if (Gravity.CENTER == 100 ){
            dialog.setCancelable(true);
        }
        else{
            dialog.setCancelable(false);
        }
        EditText edtRecover=dialog.findViewById(R.id.edt_phone);
        Button btnNoThanks=dialog.findViewById(R.id.btn_no_thanks);
        Button btnRecover=dialog.findViewById(R.id.btn_recover);
        btnNoThanks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        btnRecover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value=edtRecover.getText().toString().trim();
                if(TextUtils.isEmpty(value)){
                    edtRecover.setError("Ented Phone");
                    edtRecover.setFocusable(true);
                }
                else{
                    pd.show();
                    HashMap<String,Object> result= new HashMap<>();
                    result.put(key,value);
                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Updated...", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        dialog.show();
    }
    private void showConfirmDialog(){
        final Dialog dialog= new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_dialog_confirm);
        Window window=dialog.getWindow();
        if (window==null){
            return;
        }
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowAttributes= window.getAttributes();
//        windowAttributes.gravity=center;
        window.setAttributes(windowAttributes);
        if (Gravity.CENTER == 100 ){
            dialog.setCancelable(true);
        }
        else{
            dialog.setCancelable(false);
        }
        Button btnNoThanks=dialog.findViewById(R.id.btn_no_thanks);
        Button btnYes=dialog.findViewById(R.id.btn_yes);
        Button btnSentEmail=dialog.findViewById(R.id.btn_sentEmail);
        // no
        btnNoThanks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        // yes
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
            }
        });
        // sent email
        btnSentEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sentEmail();
            }
        });
        dialog.show();
    }
    private void showTypeUpdateDialogCopy(String key){
        final Dialog dialog= new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_dialog_update_type);
        Window window=dialog.getWindow();
        if (window==null){
            return;
        }
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowAttributes= window.getAttributes();
//        windowAttributes.gravity=center;
        window.setAttributes(windowAttributes);
        if (Gravity.CENTER == 100 ){
            dialog.setCancelable(true);
        }
        else{
            dialog.setCancelable(false);
        }
//        EditText edtRecover=dialog.findViewById(R.id.edt_phone);
        Button btnNoThanks=dialog.findViewById(R.id.btn_no_thanks);
        Button btnUpdateType=dialog.findViewById(R.id.btn_recover);
        RadioButton rdbSV=dialog.findViewById(R.id.sinhVienRdo);
        RadioButton rdbGV=dialog.findViewById(R.id.giaoVienRdo);
        btnNoThanks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

            // JSON object
//        new ReadJSONObject().execute("https://pastebin.com/raw/0dt3K77N");
            btnUpdateType.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    new ReadJSON().execute("https://pastebin.com/raw/g9TJW45Y");
                    if (rdbGV.isChecked()==true){
                        // nếu lỗi code không chạy vào nhánh này, thì thay đổi lại đường link
                        new ReadJSON().execute("https://pastebin.com/raw/dXGqwmqf");

                    dialog.dismiss();}
                    else if (rdbSV.isChecked()==true){
                        showSinhVien();
                    }
                }
            });





//        btnRecover.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String value=edtRecover.getText().toString().trim();
//                if(TextUtils.isEmpty(value)){
//                    edtRecover.setError("Ented Phone");
//                    edtRecover.setFocusable(true);
//                }
//                else{
//                    pd.show();
//                    HashMap<String,Object> result= new HashMap<>();
//                    result.put(key,value);
//                    databaseReference.child(user.getUid()).updateChildren(result)
//                            .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                @Override
//                                public void onSuccess(Void aVoid) {
//                                    pd.dismiss();
//                                    Toast.makeText(getActivity(), "Updated...", Toast.LENGTH_SHORT).show();
//                                }
//                            }).addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            pd.dismiss();
//                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                }
//            }
//        });
        dialog.show();
    }
    private class ReadJSON extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... strings) {
            StringBuilder content= new StringBuilder();
            try {
                URL url= new URL(strings[0]);
                InputStreamReader inputStreamReader= new InputStreamReader(url.openConnection().getInputStream());
                BufferedReader bufferedReader= new BufferedReader(inputStreamReader);
                String line="";
                while ((line=bufferedReader.readLine())!=null){
                    content.append(line);
                }
                bufferedReader.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return content.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject= new JSONObject(s);
                JSONArray array= jsonObject.getJSONArray("danhsachemailgiaovien");
                for (int i=0;i<array.length();i++){
                    JSONObject object=array.getJSONObject(i);
                    String email=object.getString("email");
                     String inputEmail=emailTv.getText().toString();
                    if (email.equals(inputEmail))
                    {
                        sentEmail();
                        return;
                    }
                }
                Toast.makeText(getActivity(),"Email đăng ký không phải là email của Giáo Viên trường Đại Học Sư Phạm Kỹ Thuật.", Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
    private class ReadJSONMain extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... strings) {
            StringBuilder content= new StringBuilder();
            try {
                URL url= new URL(strings[0]);
                InputStreamReader inputStreamReader= new InputStreamReader(url.openConnection().getInputStream());
                BufferedReader bufferedReader= new BufferedReader(inputStreamReader);
                String line="";
                while ((line=bufferedReader.readLine())!=null){
                    content.append(line);
                }
                bufferedReader.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return content.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject= new JSONObject(s);
                JSONArray array= jsonObject.getJSONArray("danhsachemailgiaovien");
                for (int i=0;i<array.length();i++){
                    JSONObject object=array.getJSONObject(i);
                    String email=object.getString("email");
                    String inputEmail=emailTv.getText().toString();
                    if (email.equals(inputEmail)&&user.isEmailVerified())
                    {
                        showGiaoVien();
                        return;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
    private void sentEmail() {
        if(user.isEmailVerified()){
            showGiaoVien();
            Toast.makeText(getContext(), "Email has been confirmed", Toast.LENGTH_SHORT).show();
        }
        else{
            user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(getActivity(), "Verification Email has been sent", Toast.LENGTH_SHORT).show();
                    showConfirmDialog();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("tag","onFailure: Email not sent "+e.getMessage());
                }
            });
        }
    }

    private void showGiaoVien() {
        String value="Giáo Viên";
        pd.show();
        HashMap<String,Object> result= new HashMap<>();
        result.put("type",value);
        databaseReference.child(user.getUid()).updateChildren(result)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        pd.dismiss();
                    }
                });
    }
    private void showSinhVien() {
        String value="Sinh Viên";
        pd.show();
        HashMap<String,Object> result= new HashMap<>();
        result.put("type",value);
        databaseReference.child(user.getUid()).updateChildren(result)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        pd.dismiss();
                    }
                });
    }

    private void showNameUpdateDialogCopy(String key){
        final Dialog dialog= new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_dialog_update_name);
        Window window=dialog.getWindow();
        if (window==null){
            return;
        }
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowAttributes= window.getAttributes();
//        windowAttributes.gravity=center;
        window.setAttributes(windowAttributes);
        if (Gravity.CENTER == 100 ){
            dialog.setCancelable(true);
        }
        else{
            dialog.setCancelable(false);
        }
        EditText edtRecover=dialog.findViewById(R.id.edt_name);
        Button btnNoThanks=dialog.findViewById(R.id.btn_no_thanks);
        Button btnRecover=dialog.findViewById(R.id.btn_recover);
        btnNoThanks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        btnRecover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value=edtRecover.getText().toString().trim();
                if(TextUtils.isEmpty(value)){
                    edtRecover.setError("Ented Name");
                    edtRecover.setFocusable(true);
                }
                else{
                    pd.show();
                    HashMap<String,Object> result= new HashMap<>();
                    result.put(key,value);
                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Updated...", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    if(key.equals("name")){
                        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");
                        Query query=ref.orderByChild("uid").equalTo(uid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds:snapshot.getChildren()){
                                    String child=ds.getKey();
                                    snapshot.getRef().child(child).child("uName").setValue(value);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds: snapshot.getChildren()){
                                    String child=ds.getKey();
                                    if (snapshot.child(child).hasChild("Comments")){
                                        String child1=""+snapshot.child(child).getKey();
                                        Query child2=FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                        child2.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot ds:snapshot.getChildren()){
                                                    String child=ds.getKey();
                                                    snapshot.getRef().child(child).child("uName").setValue(value);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

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
                }
            }
        });
        dialog.show();
    }
    private void showNamePhoneUpdateDialog(String key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update "+key);
        LinearLayout linearLayout= new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);
        EditText editText= new EditText(getActivity());
        editText.setHint("Enter "+key);
        linearLayout.addView(editText);
        builder.setView(linearLayout);
        //button update
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value= editText.getText().toString().trim();
                if (!TextUtils.isEmpty(value)){
                    pd.show();
                    HashMap<String,Object> result= new HashMap<>();
                    result.put(key,value);
                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Updated...", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else{
                    Toast.makeText(getActivity(), "Please enter "+key , Toast.LENGTH_SHORT).show();
                }
            }
        });
        //button cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void showImagePicDialog() {
        String option[]={"Camera","Gallery"};
        AlertDialog.Builder builder= new AlertDialog.Builder(getActivity());
        builder.setTitle("Pick Image From");
        builder.setItems(option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which==0){
                    if (!checkCameraPermission()){
                        requestCameraPermission();
                    }
                    else{
                        pickFromCamera();
                    }
                }else if(which==1){
                    if (!checkStoragePermission()){
                        requestStoragePermission();
                    }
                    else{
                        pickFromGallery();
                    }

                }
            }
        });
        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean cameraAccepted= grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted= grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted){
                        pickFromCamera();

                    }else{
                        Toast.makeText(getActivity(), "Please enable camera & storage permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length>0){
                    //1
                    boolean writeStorageAccepted= grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted){
                        pickFromGallery();

                    }else{
                        Toast.makeText(getActivity(), "Please enable storage permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode==RESULT_OK){
            if (requestCode==IMAGE_PICK_GALLERY_CODE){
                image_uri=data.getData();
                uploadProfileCoverPhoto(image_uri);
            }
            if (requestCode==IMAGE_PICK_CAMERA_CODE){
                uploadProfileCoverPhoto(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileCoverPhoto(Uri uri) {
        pd.show();
        String filePathAndName=storagePath+ ""+profileOrCoverPhoto+"_"+user.getUid();
        StorageReference storageReference2nd=storageReference.child(filePathAndName);
        storageReference2nd.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask= taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        Uri dowloadUri= uriTask.getResult();
                        if (uriTask.isSuccessful()){
                            HashMap<String,Object> results= new HashMap<>();
                            results.put(profileOrCoverPhoto,dowloadUri.toString());
                            databaseReference.child(user.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Image Update...", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                   pd.dismiss();
                                    Toast.makeText(getActivity(), "Error Updating Image...", Toast.LENGTH_SHORT).show();
                                }
                            });

                            if(profileOrCoverPhoto.equals("image")){
                                DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");
                                Query query=ref.orderByChild("uid").equalTo(uid);
                                query.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot ds:snapshot.getChildren()){
                                            String child=ds.getKey();
                                            snapshot.getRef().child(child).child("uDp").setValue(dowloadUri.toString());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot ds: snapshot.getChildren()){
                                            String child=ds.getKey();
                                            if (snapshot.child(child).hasChild("Comments")){
                                                String child1=""+snapshot.child(child).getKey();
                                                Query child2=FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                                child2.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        for (DataSnapshot ds:snapshot.getChildren()){
                                                            String child=ds.getKey();
                                                            snapshot.getRef().child(child).child("uDp").setValue(dowloadUri.toString());
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

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

                        }else{
                            pd.dismiss();
                            Toast.makeText(getActivity(), "Some error occured", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pickFromCamera() {
        ContentValues values= new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Temp Description");
        image_uri=getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
        Intent cameraIntent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_CODE);
    }

    private void pickFromGallery() {
        Intent galleryIntent= new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,IMAGE_PICK_GALLERY_CODE);
    }
    private void checkUserStatus(){
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if (user!=null){
            uid=user.getUid();

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
        menu.findItem(R.id.acciton_scan_qrcode).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_add_participant_group).setVisible(false);
        menu.findItem(R.id.action_groupinfo).setVisible(false);
        menu.findItem(R.id.action_videocall).setVisible(false);
        menu.findItem(R.id.acciton_add_post).setVisible(false);
        MenuItem item= menu.findItem(R.id.acciton_search);
        SearchView searchView=(SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query)){
                    searchMyPosts(query);
                }else{
                    loadMyPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (!TextUtils.isEmpty(query)){
                    searchMyPosts(query);
                }else{
                    loadMyPosts();
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

        if (id==R.id.acciton_myQrCode){
            startActivity(new Intent(getActivity(), MyQrCodeActivity.class));
        }

        if (id == R.id.acciton_add_post) {

            SharedPreferences shf = context.getSharedPreferences("getIDPt", MODE_PRIVATE);
            String idDeletePost=shf.getString("Id","");

//            SharedPreferences shf = getActivity().getSharedPreferences("getIDPtLast", MODE_PRIVATE);
//                String idDeletePost = shf.getString("Id", "");
            String idPost=idDeletePost;
//                Toast.makeText(context, idDeletePost, Toast.LENGTH_SHORT).show();
            String key = shf.getString("key", "");
            Toast.makeText(context, idPost, Toast.LENGTH_SHORT).show();
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
                                if (statusPost.equals("false")) {
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
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context=context;

    }
}