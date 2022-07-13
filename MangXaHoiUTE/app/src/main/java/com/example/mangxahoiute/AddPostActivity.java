package com.example.mangxahoiute;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.mangxahoiute.adapters.AdapterTypePost;
import com.example.mangxahoiute.models.ModelContent;
import com.example.mangxahoiute.models.ModelTypePost;
import com.example.mangxahoiute.models.ModelUsers;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AddPostActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    DatabaseReference userDbRef,typePDbRef;
    ActionBar actionBar;
    private static final int CAMERA_REQUEST_CODE=100;
    private static final int STORAGE_REQUEST_CODE=200;

    private static final int IMAGE_PICK_CAMERA_CODE=300;
    private static final int  IMAGE_PICK_GALLERY_CODE=400;

    String[] cameraPermissions;
    String[] storagePermissions;
    public static boolean tonTaiTrongList;



    EditText titleEt,descriptionEt,typePotstEt;
    ImageView imageIv;
    Button uploadBtn,backBtn;
    SearchView searchView;

    String name,email,uid,dp,type;
    String editTitle,editDescription,editTypePost;
    public static String editImage;
    public static String editPostId;
    Uri image_rui =null;
    ProgressDialog pd;
    RecyclerView recyclerView;
    AdapterTypePost adapterTypePost;
    List<ModelTypePost> usersList;
    String typePt;
    SharedPreferences shf;
    LinearLayout lnrecv;
    String listTypePost="";
    String timeStamp= String.valueOf(System.currentTimeMillis());
    Bitmap imageB,imageBEditPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        actionBar= getSupportActionBar();
        actionBar.setTitle("Add New Post");

        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        cameraPermissions= new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions= new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        pd=new ProgressDialog(this);
        firebaseAuth=FirebaseAuth.getInstance();
        checkUserStatus();

        anhXa();
        uploadBtn.setVisibility(View.VISIBLE);
        try {
            SharedPreferences shf = getSharedPreferences("typepost1", MODE_PRIVATE);
            typePt=shf.getString("tpost","");
            typePotstEt.setText(typePt);
        } catch (Exception e){
//            Toast.makeText(this, "Null", Toast.LENGTH_SHORT).show();
        }

        Intent intent= getIntent();
        String action=intent.getAction();
        String typeShare=intent.getType();


        String isUpdateKey=""+intent.getStringExtra("key");
        editPostId=""+intent.getStringExtra("editPostId");

        Intent intent1=getIntent();
        String isUpdateKeyTypePost=""+intent1.getStringExtra("keyTypePts");
//        Toast.makeText(this, isUpdateKeyTypePost, Toast.LENGTH_SHORT).show();
//        Toast.makeText(this, isUpdateKey+""+isUpdateKeyTypePost, Toast.LENGTH_SHORT).show();

        SharedPreferences shfs = getSharedPreferences("editTypePt", MODE_PRIVATE);
        String isUpdateKeyTypePost2=shfs.getString("editPt","");

        SharedPreferences shfes = getSharedPreferences("editTyPt", MODE_PRIVATE);
        String isUpdateKeyTypePost3=shfes.getString("editPt","");
//        Toast.makeText(this, isUpdateKeyTypePost3, Toast.LENGTH_SHORT).show();

        //Xóa dữ liêu của các edittext
//        SharedPreferences sharedPreferences1 = getSharedPreferences("saveEditText", MODE_PRIVATE);
//        SharedPreferences.Editor editor1a=sharedPreferences1.edit();
//        editor1a.remove("key");
//        editor1a.remove("description");
//        editor1a.remove("title");
//        editor1a.remove("image");
//        editor1a.remove("email");
//        editor1a.apply();


        SharedPreferences sharedPreferences = getSharedPreferences("saveEditText", MODE_PRIVATE);
        String key=sharedPreferences.getString("key","");
        String emailkt=sharedPreferences.getString("email","");
        String title=sharedPreferences.getString("title","");
        String image=sharedPreferences.getString("image","");
        String description=sharedPreferences.getString("description","");

        //Lấy dữ liệu của edittext của editpost
        SharedPreferences sharedPreferencesEditPost = getSharedPreferences("saveEditTextOfEditPost", MODE_PRIVATE);
        String keyEditPost=sharedPreferencesEditPost.getString("key","");
        String emailktEditPost=sharedPreferencesEditPost.getString("email","");
        String idEditPt=sharedPreferencesEditPost.getString("idpost","");
        String titleEditPost=sharedPreferencesEditPost.getString("title","");
        String imageEditPost=sharedPreferencesEditPost.getString("image","");
        String descriptionEditPost=sharedPreferencesEditPost.getString("description","");






//        Toast.makeText(this, emailkt+key+title+description, Toast.LENGTH_SHORT).show();
        //Hiển thị typepost từ TypePostActivity


        //Đổ dữ liệu vào edittext của addpost
        if (key.equals("check")&&email.equals(emailkt)&&isUpdateKeyTypePost3.equals("addpost")){
            titleEt.setText(title);
            descriptionEt.setText(description);
            typePotstEt.setText(typePt);
            if (!image.equals("")){
                imageB=decodeToBase64(image);
                imageIv.setImageBitmap(imageB);
            }

//            imageIv.setImageBitmap(imageB);
//                    typePotstEt.setText(editTypePost);
        }

//        Toast.makeText(this, isUpdateKeyTypePost3, Toast.LENGTH_SHORT).show();
        //Đổ dữ liệu vào editext của editpost

//        if (keyEditPost.equals("check")&&email.equals(emailktEditPost)&&isUpdateKeyTypePost3.equals("editpost")){
//            editPostId=idEditPt;
//            titleEt.setText(titleEditPost);
//            descriptionEt.setText(descriptionEditPost);
//            typePotstEt.setText(typePt);
//            if (!imageEditPost.equals("")){
//                imageBEditPost=decodeToBase64(imageEditPost);
//                imageIv.setImageBitmap(imageBEditPost);
//            }
////                    typePotstEt.setText(editTypePost);
//
//            //Xóa dữ liêu của các edittext
////            SharedPreferences sharedPreferences1 = getSharedPreferences("saveEditTextOfEditPost", MODE_PRIVATE);
////            SharedPreferences.Editor editor1a=sharedPreferences1.edit();
////            editor1a.remove("key");
////            editor1a.remove("idpost");
////            editor1a.remove("description");
////            editor1a.remove("title");
////            editor1a.remove("image");
////            editor1a.remove("email");
////            editor1a.apply();
//        }

        if (isUpdateKey.equals("editPost")&&isUpdateKeyTypePost2.equals("editPost")){
            SharedPreferences.Editor editor=shfs.edit();
            editor.remove("editPt");
            editor.apply();
            isUpdateKeyTypePost2="";
        }
//        Toast.makeText(this, isUpdateKeyTypePost2, Toast.LENGTH_SHORT).show();
        if ((isUpdateKey.equals("editPost")||isUpdateKeyTypePost.equals("editPost")||isUpdateKeyTypePost2.equals("editPost"))
        &&isUpdateKeyTypePost3.equals("editpost")){
            actionBar.setTitle("Update Post");
            uploadBtn.setText("Update");
//            Toast.makeText(this, editPostId, Toast.LENGTH_SHORT).show();
            loadPostData(editPostId);
            //Điều kiện kiểm tra của else if ((!isUpdateKey.equals("editPost"))&&(!isUpdateKeyTypePost.equals("editPost"))&&(!isUpdateKeyTypePost2.equals("editPost")))
        }else {
            actionBar.setTitle("Add New Post");
            uploadBtn.setText("Upload");
        }

        actionBar.setSubtitle(email);

        if (keyEditPost.equals("check")&&email.equals(emailktEditPost)&&isUpdateKeyTypePost3.equals("editpost")){
            editPostId=idEditPt;
//            Toast.makeText(this, editPostId, Toast.LENGTH_SHORT).show();
            titleEt.setText(titleEditPost);
            descriptionEt.setText(descriptionEditPost);
            typePotstEt.setText(typePt);
            if (!imageEditPost.equals("")){
                imageBEditPost=decodeToBase64(imageEditPost);
                imageIv.setImageBitmap(imageBEditPost);
            }
//                    typePotstEt.setText(editTypePost);

            //Xóa dữ liêu của các edittext
//            SharedPreferences sharedPreferences1 = getSharedPreferences("saveEditTextOfEditPost", MODE_PRIVATE);
//            SharedPreferences.Editor editor1a=sharedPreferences1.edit();
//            editor1a.remove("key");
//            editor1a.remove("idpost");
//            editor1a.remove("description");
//            editor1a.remove("title");
//            editor1a.remove("image");
//            editor1a.remove("email");
//            editor1a.apply();
        }




        userDbRef= FirebaseDatabase.getInstance().getReference("Users");
        Query query=userDbRef.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    name="" +ds.child("name").getValue();
                    email="" +ds.child("email").getValue();
                    dp="" +ds.child("image").getValue();
                    type=""+ds.child("type").getValue();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//        anhXa();

//       searchView.setQuery(typePt,false);
        //
//        Intent intent=getIntent();
//        typePt=intent.getStringExtra("typePost");
//        if (typePt==null){
//            typePt="";
//        }






//        typePDbRef=FirebaseDatabase.getInstance().getReference("Posts");
//        Query userQuery=typePDbRef.orderByChild("pId").equalTo("1651483655854");
//        userQuery.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for (DataSnapshot ds:snapshot.getChildren()){
//                    String txtTypePost= typePotstEt.getText().toString();
//                    String statusPost=""+ds.child("pStatus").getValue();
//                    String checkNt=""+ds.child("checkNotification").getValue();
//                    String timeStamp= String.valueOf(System.currentTimeMillis());
////                    Toast.makeText(AddPostActivity.this, ""+checkTypePost(txtTypePost), Toast.LENGTH_SHORT).show();
//                    if (statusPost.equals("true")){
//                      checkTypePost(txtTypePost);
////                        HashMap<Object, String> hashMapTypePost = new HashMap<>();
////                        hashMapTypePost.put("tPost", txtTypePost);
////                        DatabaseReference reftPost = FirebaseDatabase.getInstance().getReference("TypePosts");
////                        reftPost.child(timeStamp).setValue(hashMapTypePost);
//                        if (checkNt.equals("false")) {
//                            Toast.makeText(AddPostActivity.this, "Your post has been approved by the admin", Toast.LENGTH_SHORT).show();
//                            HashMap<String,Object> result= new HashMap<>();
//                            result.put("checkNotification","true");
//                            DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Posts");
//                            databaseReference.child("1651483655854").updateChildren(result);
//                        }
//                    }
//                    break;
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });



//        recyclerView.setHasFixedSize(true);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        usersList= new ArrayList<>();
//        adapterTypePost= new AdapterTypePost(this,usersList);
//        recyclerView.setAdapter(adapterTypePost);
//        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("TypePosts");
//        ref.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for (DataSnapshot ds : snapshot.getChildren()){
//                    ModelTypePost modelTypePost= ds.getValue(ModelTypePost.class);
//                    usersList.add(modelTypePost);
//                }
//                adapterTypePost.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });


//        getAllUsers();



//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String s) {
//                if (!TextUtils.isEmpty(s.trim())){
//                    searchUsers(s);
//                }else {
//                    getAllUsers();
//                }
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String s) {
//                if (!TextUtils.isEmpty(s.trim())){
//                    searchUsers(s);
//                }else {
//                    getAllUsers();
//                }
//                return false;
//            }
//        });

        //lấy giá trị bộ nhớ tạm của Chức năng share bài
        if (Intent.ACTION_SEND.equals(action)&& typeShare!=null){
            if ("text/plain".equals(typeShare)){
                handleSendText(intent);

            }else if(typeShare.startsWith("image")){
                handleSendImage(intent);

            }
        }

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2= new Intent(AddPostActivity.this, DashboardActivity.class);
                startActivity(intent2);
                finish();
            }
        });

        typePotstEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent= new Intent(AddPostActivity.this, TypePostActivity.class);
                if (isUpdateKey.equals("editPost")){

                    try {
                        SharedPreferences sharedPreferencess = getSharedPreferences("saveEditTextOfEditPost", MODE_PRIVATE);
                        SharedPreferences.Editor editor1b=sharedPreferencess.edit();
                        editor1b.putString("key","");
                        editor1b.putString("idpost",editPostId);
                        editor1b.putString("email",email);
//                        Lỗi null khi các edittext chưa nhập
                        editor1b.putString("title",titleEt.getText().toString());

                        if (imageIv.getDrawable()==null){
                            String valueImage="noImage";
                            editor1b.putString("image","");
                        }else{
                            Bitmap bm=((BitmapDrawable)imageIv.getDrawable()).getBitmap();
                            editor1b.putString("image",encodeToBase64(bm));
                        }
                        editor1b.putString("description",descriptionEt.getText().toString());
                        editor1b.apply();
                    } catch (Exception e){
                        Toast.makeText(AddPostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    /// update sau
                    intent.putExtra("keyTypePost","editPost");
//                    Toast.makeText(AddPostActivity.this, "1", Toast.LENGTH_SHORT).show();
                    SharedPreferences shfs = getSharedPreferences("editTypePost", MODE_PRIVATE);
                    SharedPreferences.Editor editor1=shfs.edit();
                    editor1.putString("editPts","editPost");
                    editor1.apply();
                    startActivity(intent);
                }else{
                    try {

                        SharedPreferences sharedPreferencess = getSharedPreferences("saveEditText", MODE_PRIVATE);
                        SharedPreferences.Editor editor1b=sharedPreferencess.edit();
                        editor1b.putString("key","");
                    editor1b.putString("email",email);
//                        Lỗi null khi các edittext chưa nhập
                    editor1b.putString("title",titleEt.getText().toString());
//                        Toast.makeText(AddPostActivity.this,imageIv.getDrawable()+"", Toast.LENGTH_SHORT).show();
                     if (imageIv.getDrawable()==null){
                        String valueImage="noImage";
                        editor1b.putString("image","");
                    }else{
                         Bitmap bm=((BitmapDrawable)imageIv.getDrawable()).getBitmap();
                        editor1b.putString("image",encodeToBase64(bm));
                     }
                     editor1b.putString("description",descriptionEt.getText().toString());
                        editor1b.apply();
                    } catch (Exception e){
                        Toast.makeText(AddPostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    intent.putExtra("keyTypePost","addPost");
                    startActivity(intent);

                }

                //Lưu các editext vào sharePrefefences

                //Lỗi ngay dòng code này
//                Bitmap bm=((BitmapDrawable)imageIv.getDrawable()).getBitmap();
//                SharedPreferences sharedPreferencess = getSharedPreferences("saveEditText", MODE_PRIVATE);
//                SharedPreferences.Editor editor1b=sharedPreferencess.edit();
//                editor1b.putString("key","");
//                editor1b.putString("email",email);
//                editor1b.putString("title",titleEt.getText().toString());
////                if (imageIv.getDrawable()==null){
////                    String valueImage="noImage";
////                    editor1.putString("image",valueImage);
////                }else{
//////                    editor1.putString("image",encodeToBase64(bm));
////                }
//                editor1b.putString("image","");
//                editor1b.putString("description",descriptionEt.getText().toString());
//                editor1b.apply();



            }
        });


        imageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialog();
            }
        });

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = titleEt.getText().toString().trim();
                String description=descriptionEt.getText().toString().trim();
                if (TextUtils.isEmpty(title)){
                    Toast.makeText(AddPostActivity.this, "Enter title...", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(description)){
                    Toast.makeText(AddPostActivity.this, "Enter description...", Toast.LENGTH_SHORT).show();
                    return;
                }
//                Toast.makeText(AddPostActivity.this, isUpdateKeyTypePost3+"-"+"editpost", Toast.LENGTH_SHORT).show();
                if (isUpdateKeyTypePost3.equals("editpost")){
                    beginUpdate(title,description,editPostId);
                }else{
                    uploadData(title,description);
                }
                uploadBtn.setVisibility(View.INVISIBLE);
                //Xóa dữ liêu của các edittext
                SharedPreferences sharedPreferences1 = getSharedPreferences("saveEditText", MODE_PRIVATE);
                SharedPreferences.Editor editor1a=sharedPreferences1.edit();
                editor1a.remove("key");
                editor1a.remove("description");
                editor1a.remove("title");
                editor1a.remove("image");
                editor1a.remove("email");
                editor1a.apply();

//                if (image_rui==null){
//                    uploadData(title,description,"noImage");
//
//                }else{
//                    uploadData(title,description,String.valueOf(image_rui));
//                }
                SharedPreferences shf = getSharedPreferences("getId", MODE_PRIVATE);
                SharedPreferences.Editor editor=shf.edit();
                editor.putString("Id",timeStamp);
                editor.apply();
//                Intent intent= new Intent(AddPostActivity.this,HomeFragment.class);
//                startActivity(intent);
            }
        });

    }

    private void handleSendImage(Intent intent) {
        Uri imageUri=(Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        String sharedText=intent.getStringExtra(Intent.EXTRA_TEXT);
        String title=intent.getStringExtra(Intent.EXTRA_COMPONENT_NAME);
        String typePost=intent.getStringExtra(Intent.EXTRA_PACKAGE_NAME);
//        Toast.makeText(this, ""+typePost, Toast.LENGTH_SHORT).show();
        if (imageUri!=null){
            image_rui=imageUri;
            imageIv.setImageURI(image_rui);
            titleEt.setText(title);
            descriptionEt.setText(sharedText);
            typePotstEt.setText(typePost);
        }
    }

    private void handleSendText(Intent intent) {
        String sharedText=intent.getStringExtra(Intent.EXTRA_TEXT);
        String title=intent.getStringExtra(Intent.EXTRA_STREAM);
        String typePost=intent.getStringExtra(Intent.EXTRA_COMPONENT_NAME);

        if (sharedText!=null){
//            Toast.makeText(AddPostActivity.this, ""+sharedText+title+typePost, Toast.LENGTH_SHORT).show();
            titleEt.setText(title);
            descriptionEt.setText(sharedText);
            typePotstEt.setText(typePost);
        }
    }

    public static Bitmap decodeToBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }
    public static String encodeToBase64(Bitmap image) {
        Bitmap immage = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immage.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);
        return imageEncoded;
    }

    private void beginUpdate(String title, String description, String editPostId) {
        pd.setMessage("Updating Post...");
        pd.show();
        //Lỗi ở dòng này
        if (!editImage.equals("noImage")){
//            Toast.makeText(this, "Upload với ảnh cũ", Toast.LENGTH_SHORT).show();
            updateWasWithImage(title,description,editPostId);
        }else if(imageIv.getDrawable()!=null){
//            Toast.makeText(this, "Upload với ảnh thêm mới", Toast.LENGTH_SHORT).show();
            updateWithNowImage(title,description,editPostId);

        }else{
//            Toast.makeText(this, "Không có ảnh", Toast.LENGTH_SHORT).show();
            updateWithoutImage(title,description,editPostId);
        }
    }

    private void updateWithoutImage(String title, String description, String editPostId) {
        String txtTypePost= typePotstEt.getText().toString();
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("uid",uid);
        hashMap.put("uName",name);
        hashMap.put("uEmail",email);
        hashMap.put("uType",type);
        hashMap.put("uDp",dp);
        hashMap.put("pTitle",title);
        hashMap.put("pDescr",description);
        hashMap.put("pImage","noImage");
        hashMap.put("pTypePost", txtTypePost);
        hashMap.put("checkNotification","false");
        hashMap.put("pStatus","false");

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");
        ref.child(editPostId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this, "Update...", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
            }
        });

    }

    private void updateWithNowImage(String title, String description, String editPostId) {
        String timeStamp=String.valueOf(System.currentTimeMillis());
        String filePathAndName="Posts/"+"post_"+timeStamp;
        String txtTypePost= typePotstEt.getText().toString();


        Bitmap bitmap=((BitmapDrawable)imageIv.getDrawable()).getBitmap();
        ByteArrayOutputStream baos= new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
        byte[] data= baos.toByteArray();
        StorageReference ref= FirebaseStorage.getInstance().getReference().child(filePathAndName);
        ref.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String downloadUri=uriTask.getResult().toString();
                        if (uriTask.isSuccessful()){
                            HashMap<String,Object> hashMap=new HashMap<>();
                            hashMap.put("uid",uid);
                            hashMap.put("uName",name);
                            hashMap.put("uEmail",email);
                            hashMap.put("uType",type);
                            hashMap.put("uDp",dp);
                            hashMap.put("pTitle",title);
                            hashMap.put("pDescr",description);
                            hashMap.put("pImage",downloadUri);
                            hashMap.put("pTypePost", txtTypePost);
                            hashMap.put("checkNotification","false");
                            hashMap.put("pStatus","false");

                            DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");
                            ref.child(editPostId)
                                    .updateChildren(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            pd.dismiss();
                                            Toast.makeText(AddPostActivity.this, "Update...", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pd.dismiss();
                                }
                            });

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
            }
        });
    }

    private void updateWasWithImage(String title, String description, String editPostId) {
        StorageReference mPictureRef=FirebaseStorage.getInstance().getReferenceFromUrl(editImage);
        mPictureRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        String timeStamp=String.valueOf(System.currentTimeMillis());
                        String filePathAndName="Posts/"+"post_"+timeStamp;
                        String txtTypePost= typePotstEt.getText().toString();


                        Bitmap bitmap=((BitmapDrawable)imageIv.getDrawable()).getBitmap();
                        ByteArrayOutputStream baos= new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
                        byte[] data= baos.toByteArray();
                        StorageReference ref= FirebaseStorage.getInstance().getReference().child(filePathAndName);
                        ref.putBytes(data)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                                        while (!uriTask.isSuccessful());
                                        String downloadUri=uriTask.getResult().toString();
                                        if (uriTask.isSuccessful()){
                                            HashMap<String,Object> hashMap=new HashMap<>();
                                            hashMap.put("uid",uid);
                                            hashMap.put("uName",name);
                                            hashMap.put("uEmail",email);
                                            hashMap.put("uType",type);
                                            hashMap.put("uDp",dp);
                                            hashMap.put("pTitle",title);
                                            hashMap.put("pDescr",description);
                                            hashMap.put("pImage",downloadUri);
                                            hashMap.put("pTypePost", txtTypePost);
                                            hashMap.put("checkNotification","false");
                                            hashMap.put("pStatus","false");

                                            DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");
                                            ref.child(editPostId)
                                                    .updateChildren(hashMap)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            pd.dismiss();
                                                            Toast.makeText(AddPostActivity.this, "Update...", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    pd.dismiss();
                                                }
                                            });

                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                pd.dismiss();
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

    private void loadPostData(String editPostId) {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Posts");
        Query fquery= reference.orderByChild("pId").equalTo(editPostId);
        fquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds:snapshot.getChildren()){
                    editTitle=""+ ds.child("pTitle").getValue();
                    editDescription=""+ ds.child("pDescr").getValue();
                    editImage=""+ ds.child("pImage").getValue();
                    editTypePost=""+ds.child("pTypePost").getValue();


                    titleEt.setText(editTitle);
                    descriptionEt.setText(editDescription);
                    typePotstEt.setText(editTypePost);
//                    typePotstEt.setText(editTypePost);
                    if (!editImage.equals("noImage")){
                        try {
                            Picasso.get().load(editImage).into(imageIv);
                        }catch (Exception e){

                        }
                    }
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
        adapterTypePost = new AdapterTypePost(this,usersList);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelTypePost modelTypePost=ds.getValue(ModelTypePost.class);
                    if ((modelTypePost.gettPost().toLowerCase().contains(query.toLowerCase()))){
//                        Toast.makeText(AddPostActivity.this, "Đã tồn tại", Toast.LENGTH_SHORT).show();
                        tonTaiTrongList=true;
                        break;
                    }else{
                       tonTaiTrongList=false;
                    }
                }
                if (tonTaiTrongList==false){
                    String txtTypePost= typePotstEt.getText().toString();
                    String timeStamp= String.valueOf(System.currentTimeMillis());
                    HashMap<Object, String> hashMapTypePost = new HashMap<>();
                    hashMapTypePost.put("tPost", txtTypePost);
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


    private void searchUsers(String query) {
        FirebaseUser fUser= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("TypePosts");
        adapterTypePost = new AdapterTypePost(this,usersList);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelTypePost modelTypePost=ds.getValue(ModelTypePost.class);
                        if (modelTypePost.gettPost().toLowerCase().contains(query.toLowerCase())){
                            usersList.add(modelTypePost);

                        }
                    adapterTypePost.notifyDataSetChanged();
                    recyclerView.setAdapter(adapterTypePost);
                }
                //refresh  adapter

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getAllUsers() {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("TypePosts");
        adapterTypePost = new AdapterTypePost(this,usersList);
        recyclerView.setAdapter(adapterTypePost);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelTypePost modelTypePost=ds.getValue(ModelTypePost.class);
                    usersList.add(modelTypePost);


                }
                adapterTypePost.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void uploadData(String title, String description) {
        pd.setMessage("Publishing post...");
        pd.show();


        String filePathAndName="Posts/" +"post_" +timeStamp;
        String txtTypePost= typePotstEt.getText().toString();
        if (imageIv.getDrawable()!=null){

            Bitmap bitmap=((BitmapDrawable)imageIv.getDrawable()).getBitmap();
            ByteArrayOutputStream baos= new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
            byte[] data= baos.toByteArray();

            StorageReference ref= FirebaseStorage.getInstance().getReference().child(filePathAndName);
            ref.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask= taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            String downloadUri= uriTask.getResult().toString();
                            if (uriTask.isSuccessful()){
                                HashMap<Object,String> hashMap= new HashMap<>();

                                hashMap.put("uid",uid);
                                hashMap.put("uName",name);
                                hashMap.put("uEmail",email);
                                hashMap.put("uType",type);
                                hashMap.put("uDp",dp);
                                hashMap.put("pId",timeStamp);
                                hashMap.put("pTitle",title);
                                hashMap.put("pDescr",description);
                                hashMap.put("pImage",downloadUri);
                                hashMap.put("pTypePost", txtTypePost);
                                hashMap.put("checkNotification","false");
                                hashMap.put("pStatus","false");
                                hashMap.put("pTime",timeStamp);
                                //moi them
                                hashMap.put("pLikes","0");
                                hashMap.put("pComments","0");






                                DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");
                                ref.child(timeStamp).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                pd.dismiss();
                                                Toast.makeText(AddPostActivity.this, "Your post is being approved, please wait.", Toast.LENGTH_SHORT).show();
                                                checkContentPost(title,description,timeStamp,downloadUri);
                                                titleEt.setText("");
                                                descriptionEt.setText("");
                                                imageIv.setImageDrawable(null);
                                                imageIv.setImageURI(null);
                                                image_rui=null;
                                                searchView=null;

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                        Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }else{
            HashMap<Object,String> hashMap= new HashMap<>();
//            HashMap<Object,String> hashMapTypePost= new HashMap<>();
            hashMap.put("uid",uid);
            hashMap.put("uName",name);
            hashMap.put("uEmail",email);
            hashMap.put("uType",type);
            hashMap.put("uDp",dp);
            hashMap.put("pId",timeStamp);
            hashMap.put("pTitle",title);
            hashMap.put("pDescr",description);
            hashMap.put("pImage","noImage");
            hashMap.put("pTypePost",txtTypePost);
            hashMap.put("checkNotification","false");
            hashMap.put("pStatus","false");
            hashMap.put("pTime",timeStamp);
            //moi them
            hashMap.put("pLikes","0");
            hashMap.put("pComments","0");


//            hashMapTypePost.put("tPost",txtTypePost);
//            DatabaseReference reftPost= FirebaseDatabase.getInstance().getReference("TypePosts");
//            reftPost.child(timeStamp).setValue(hashMapTypePost);

            DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");
            ref.child(timeStamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            pd.dismiss();
                            String pImage="noImage";
                            Toast.makeText(AddPostActivity.this, "Your post is being approved, please wait.", Toast.LENGTH_SHORT).show();
                            checkContentPost(title,description,timeStamp,pImage);
                            titleEt.setText("");
                            descriptionEt.setText("");
                            imageIv.setImageURI(null);
                            image_rui=null;
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }


    }


    private void checkContentPost(String title, String description,String pId,String pImage) {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("CheckContent");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds:snapshot.getChildren()){
                    ModelContent modelContent=ds.getValue(ModelContent.class);
                    if (title.toLowerCase().contains(modelContent.getContent().toLowerCase())
                    ||description.toLowerCase().contains(modelContent.getContent().toLowerCase())){
                        beginDelete(pId,pImage);
                        String reason="Offensive content, in your post containing the words: "+modelContent.getContent();
                        addToHisNotifications(""+uid,""+pId,"Your post was not approved because "+reason);
                        Toast.makeText(AddPostActivity.this, "The post has been removed for offensive content...", Toast.LENGTH_SHORT).show();
                        checkLockUser(reason);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void checkLockUser(String reason){
        String timestampLocked= "" + System.currentTimeMillis();
        String falsee="false";
        HashMap<String,String> hashMap1 = new HashMap<>();
        hashMap1.put("timestamp", timestampLocked);
        hashMap1.put("reasonLock",reason);
        hashMap1.put("status", "false");
//        Toast.makeText(this, ""+uid+""+timestampLocked, Toast.LENGTH_SHORT).show();
        DatabaseReference ref1a = FirebaseDatabase.getInstance().getReference("Users");
        ref1a.child(uid).child("Lock").child(timestampLocked)
                .setValue(hashMap1).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });


    }

    private void addToHisNotifications(String hisUid, String pId, String notification) {
        String timestamp=""+System.currentTimeMillis();
        //lấy giá trị của myUid
        FirebaseUser fUser= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref1= FirebaseDatabase.getInstance().getReference("Users");
        ref1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds:snapshot.getChildren()){
                    ModelUsers modelUsers=ds.getValue(ModelUsers.class);
                    if (modelUsers.getType().equals("Admin")){
                       String myUid=modelUsers.getUid();
//                        String myUid=FirebaseAuth.getInstance().getCurrentUser().getUid();
                        HashMap<Object,String> hashMap= new HashMap<>();
                        hashMap.put("pId",pId);
                        hashMap.put("timestamp",timestamp);
                        hashMap.put("pUid",hisUid);
                        hashMap.put("notification",notification);
                        hashMap.put("sUid",myUid);

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
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


//        String myUid=FirebaseAuth.getInstance().getCurrentUser().getUid();
//        HashMap<Object,String> hashMap= new HashMap<>();
//        hashMap.put("pId",pId);
//        hashMap.put("timestamp",timestamp);
//        hashMap.put("pUid",hisUid);
//        hashMap.put("notification",notification);
//        hashMap.put("sUid",myUid);
//
////        hashMap.put("sName",myUid);
////        hashMap.put("sEmail",myUid);
////        hashMap.put("sImage",myUid);
////        hashMap.put("sType",myUid);
//
//        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
//        ref.child(hisUid).child("Notifications").child(timestamp).setValue(hashMap)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//
//            }
//        });

    }


    private void beginDelete(String pId, String pImage) {
        if (pImage.equals("noImage")){
            deleteWithoutImage(pId);
        }else{
            deleteWithImage(pId,pImage);
        }
    }

    private void deleteWithImage(String pId, String pImage) {
        ProgressDialog pd= new ProgressDialog(this);
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
        ProgressDialog pd= new ProgressDialog(this);
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

    private void showImagePickDialog() {
        String[] options={"Camera","Gallery"};

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Choose Image from");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which==0){
                    if (!checkCameraPermission()){
                        requestCameraPermission();
                    }
                    else{
                        pickFromCamera();
                    }

                }
                if (which==1){
                    if (!checkStoragePermission()){
                        requestStoragePermission();
                    }
                    else{
                        pickFormGallery();
                    }
                }
            }
        });
        builder.create().show();
    }

    private void pickFormGallery() {

        Intent intent= new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        ContentValues cv= new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE,"Temp Pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION,"Temp Descr");
        image_rui=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,cv);


        Intent intent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,image_rui);
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermission(){

        boolean result= ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==(PackageManager.PERMISSION_GRANTED);
        return result;

    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this,storagePermissions,STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){

        boolean result= ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) ==(PackageManager.PERMISSION_GRANTED);
        boolean result1= ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==(PackageManager.PERMISSION_GRANTED);
        return result && result1;

    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermissions,CAMERA_REQUEST_CODE);
    }

    private void anhXa() {
        titleEt=findViewById(R.id.pTitleEt);
        descriptionEt=findViewById(R.id.pDescriptionEt);
        imageIv=findViewById(R.id.pImageIv);
        uploadBtn=findViewById(R.id.pUploadBtn);
        typePotstEt=findViewById(R.id.pTypePostEt);
        backBtn=findViewById(R.id.back);
//        searchView=findViewById(R.id.pTypePostEt);
//        recyclerView=findViewById(R.id.typePost_recyclerView);

    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
    }

    private void checkUserStatus(){
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if (user!=null){
            email=user.getEmail();
            uid=user.getUid();

        }else {
            startActivity(new Intent(this, MainActivity.class));
            finish();

        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent intent2= new Intent(AddPostActivity.this,DashboardActivity.class);
        startActivity(intent2);
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        menu.findItem(R.id.acciton_add_post).setVisible(false);
        menu.findItem(R.id.acciton_search).setVisible(false);
        menu.findItem(R.id.acciton_scan_qrcode).setVisible(false);
        menu.findItem(R.id.acciton_logout).setVisible(false);
        menu.findItem(R.id.acciton_myQrCode).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_add_participant_group).setVisible(false);
        menu.findItem(R.id.action_groupinfo).setVisible(false);
        menu.findItem(R.id.action_videocall).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if (id==R.id.acciton_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean cameraAccepted= grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted=grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted){
                        pickFromCamera();
                    }
                    else{
                        Toast.makeText(this, "Camera & Storage both permissions are neccessary...", Toast.LENGTH_SHORT).show();
                    }
                }
                else{

                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean storageAccepted =grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted){
                        pickFormGallery();
                    }
                    else{
                        Toast.makeText(this, " Storage permissions neccessary...", Toast.LENGTH_SHORT).show();
                    }
                }
                else{

                }
            }
            break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode== RESULT_OK){
            if (requestCode==IMAGE_PICK_GALLERY_CODE){
                image_rui=data.getData();
                imageIv.setImageURI(image_rui);
            }
            else if (requestCode==IMAGE_PICK_CAMERA_CODE){
                imageIv.setImageURI(image_rui);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        //xóa dữ liệu của các edittext
        SharedPreferences sharedPreferences1 = getSharedPreferences("saveEditTextOfEditPost", MODE_PRIVATE);
        SharedPreferences.Editor editor1a=sharedPreferences1.edit();
        editor1a.remove("key");
        editor1a.remove("idpost");
        editor1a.remove("description");
        editor1a.remove("title");
        editor1a.remove("image");
        editor1a.remove("email");
        editor1a.apply();
        super.onDestroy();
    }
}