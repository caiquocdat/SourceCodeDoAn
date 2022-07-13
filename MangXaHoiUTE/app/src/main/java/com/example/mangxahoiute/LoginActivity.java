package com.example.mangxahoiute;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mangxahoiute.models.ModelLock;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {
    EditText mEmailEt,mPasswordEt;
    TextView notHaveAccntTv,mRecoverPassTv;
    Button mLoginBtn;
    private FirebaseAuth mAuth;
    ProgressDialog progressDialog;
    CheckBox cbRemember;
    SharedPreferences sharedPreferences;
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    ImageView img_google,img_facebook;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_login);
        anhXa();
        sharedPreferences=getSharedPreferences("dataLogin",MODE_PRIVATE);
        mEmailEt.setText(sharedPreferences.getString("taikhoan",""));
        mPasswordEt.setText(sharedPreferences.getString("matkhau",""));
        cbRemember.setChecked(sharedPreferences.getBoolean("checked",false));


        //Configure Google Sign In
        gso= new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        gsc= GoogleSignIn.getClient(this,gso);
        gsc.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    FirebaseAuth.getInstance().signOut();

                }
            }
        });


        //In the onCreate() method, initialize the FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=mEmailEt.getText().toString().trim();
                String password=mPasswordEt.getText().toString().trim();
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    mEmailEt.setError("Invalid Email");
                    mEmailEt.setFocusable(true);
                }
                else{
                    if (password.length()<1){
                        mPasswordEt.setError("Password not entered");
                        mPasswordEt.setFocusable(true);
                    }
                    else{
                        loginUser(email,password);
                    }
                }
            }
        });
        notHaveAccntTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
                finish();
            }
        });
        mRecoverPassTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRecoverDialog(Gravity.CENTER);
            }
        });
        img_google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignIn_Google();
            }
        });
        img_facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        progressDialog= new ProgressDialog(this);

    }

    private void SignIn_Google() {
        Intent intent= gsc.getSignInIntent();
        startActivityForResult(intent,100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==100){
            Task<GoogleSignInAccount> task=GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account=task.getResult(ApiException.class);

//                HomeActivity();

                firebaseAuthWithGoogle(account);

            } catch (ApiException e) {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential= GoogleAuthProvider.getCredential(account.getIdToken(),null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful())
                {
                    FirebaseUser user= mAuth.getCurrentUser();
                    if (task.getResult().getAdditionalUserInfo().isNewUser()){
                        //Get user email and uid from auth
                        String email=user.getEmail();
                        String uid=user.getUid();
                        HashMap<Object,String> hashMap=new HashMap<>();
                        hashMap.put("email",email);
                        hashMap.put("uid",uid);
                        hashMap.put("name",""); //add later
                        hashMap.put("type","Sinh Viên"); //add later
                        hashMap.put("onlineStatus","online"); //add later
                        hashMap.put("typingTo","noOne"); //add later
                        hashMap.put("phone","");//add later
                        hashMap.put("image","");//add later
                        hashMap.put("cover","");//add later
                        //firebase database instance
                        FirebaseDatabase database= FirebaseDatabase.getInstance();
                        //path to store user data name Users
                        DatabaseReference reference=database.getReference("Users");
                        //put data hasmap in database
                        reference.child(uid).setValue(hashMap);
                    }
                    checkLock(user.getUid());
//                    Toast.makeText(LoginActivity.this, ""+user.getEmail(), Toast.LENGTH_SHORT).show();
//                    startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
//                    finish();
                }
                else{
                    Toast.makeText(LoginActivity.this, "Login Failed...", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void HomeActivity() {
        finish();
        Intent intent=new Intent(LoginActivity.this, DashboardActivity.class);
        startActivity(intent);

    }

    private void openRecoverDialog(int center) {
        final Dialog dialog= new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_dialog_recover_passwrod);
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
        EditText edtRecover=dialog.findViewById(R.id.edt_email);
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
                String email=edtRecover.getText().toString().trim();
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    edtRecover.setError("Invalid Email");
                    edtRecover.setFocusable(true);
                }
                else{
                beginRecovery(email);
                }
            }
        });
        dialog.show();
    }

    private void beginRecovery(String email) {
        progressDialog.setMessage("Sending email...");
        progressDialog.show();
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()){
                    Toast.makeText(LoginActivity.this, "Email sent", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(LoginActivity.this, "Failed...", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginUser(String email, String password) {
        progressDialog.setMessage("Logging In...");
        progressDialog.show();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            // Sign in success, update UI with the signed-in user's information

                            FirebaseUser user = mAuth.getCurrentUser();
                            checkLock(user.getUid());
                            if(cbRemember.isChecked()){
                                 SharedPreferences.Editor editor=sharedPreferences.edit();
                                 editor.putString("taikhoan",email);
                                 editor.putString("matkhau",password);
                                 editor.putBoolean("checked",true);
                                 editor.commit();
                            }
                            else{
                                SharedPreferences.Editor editor=sharedPreferences.edit();
                                editor.remove("taikhoan");
                                editor.remove("matkhau");
                                editor.remove("checked");
                                editor.commit();
                            }
                        } else {
                            progressDialog.dismiss();
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, "Tài khoản hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void openNotificationDialog(int center,String reasonLocked) {
        final Dialog dialog= new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_dialog_notification_lock);
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
        TextView reasonLock=dialog.findViewById(R.id.reasonLock);
        TextView sendEmailToAdmin=dialog.findViewById(R.id.sendEmailToAdmin);
        reasonLock.setText("Your account has been locked because: "+reasonLocked+"."+"\n For more information, please contact admin.");
        Button btnOK=dialog.findViewById(R.id.btn_ok);

        sendEmailToAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:adminutesocial123@gmail.com")));
            }
        });

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void checkLock(String uid) {
        DatabaseReference ref1as=FirebaseDatabase.getInstance().getReference("Users");
        ref1as.child(uid).child("Lock").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount()==0){
                        startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                        finish();
                }else {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        ModelLock modelLock = ds.getValue(ModelLock.class);
                        String status = modelLock.getStatus();
                        String reasonLock=modelLock.getReasonLock();
                        String time=modelLock.getTimestamp();
//                        Toast.makeText(LoginActivity.this, status+""+reasonLock+""+time, Toast.LENGTH_SHORT).show();
                        if (!isFinishing()) {
                            if (status.equals("false")) {
                                //dùng để kiểm tra trước khi mở dialog
                                if (!isFinishing()) {
                                    openNotificationDialog(Gravity.CENTER, reasonLock);
                                }
                                Toast.makeText(LoginActivity.this, "Your account has been locked", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
//                        else {
//                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
//                            finish();
//                        }
//                    ModelNotification modelNotification=ds.getValue(ModelNotification.class);
//                    Toast.makeText(ThereProfileActivity.this, ""+modelNotification.getNotification(), Toast.LENGTH_SHORT).show();

                    }
                    startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void anhXa() {
        mEmailEt=findViewById(R.id.emailEt);
        mPasswordEt=findViewById(R.id.passwordEt);
        mLoginBtn=findViewById(R.id.loginBtn);
        notHaveAccntTv=findViewById(R.id.nothave_accountTv);
        cbRemember=findViewById(R.id.checkBoxRemember);
        mRecoverPassTv=findViewById(R.id.recover_passowordTv);
        img_google=findViewById(R.id.imgGoogle);
        img_facebook=findViewById(R.id.imgFacebook);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

}