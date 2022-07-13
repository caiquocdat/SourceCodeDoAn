package com.example.mangxahoiute;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    EditText mEmailEt,mPasswordEt;
    Button mRegisterBtn;
    ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    TextView mHaveAccountTv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_register);
//        ActionBar actionBar= getSupportActionBar();
//        actionBar.setTitle("Create Account");
//        actionBar.setDisplayHomeAsUpEnabled(true);
//        actionBar.setDisplayShowHomeEnabled(true);
        //init
        anhXa();

        mAuth = FirebaseAuth.getInstance();
        progressDialog= new ProgressDialog(this);
        progressDialog.setMessage("Registering User...");
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email= mEmailEt.getText().toString().trim();
                String password= mPasswordEt.getText().toString().trim();
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    mEmailEt.setError("Invalid Email");
                    mEmailEt.setFocusable(true);
                }
                else if(password.length()<6){
                    mPasswordEt.setError("Password lenght at least 6 characters");
                    mPasswordEt.setFocusable(true);
                }
                else{
                    registerUser(email,password);
                }
            }
        });
        mHaveAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                finish();
            }
        });
    }

    private void registerUser(String email, String password) {
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            progressDialog.dismiss();
                            FirebaseUser user = mAuth.getCurrentUser();
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
                            Toast.makeText(RegisterActivity.this, "Registered...\n"+user.getEmail(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, DashboardActivity.class));
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, "Đăng ký thất bại. "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void anhXa() {
        mEmailEt=findViewById(R.id.emailEt);
        mPasswordEt=findViewById(R.id.passwordEt);
        mRegisterBtn=findViewById(R.id.registerBtn);
        mHaveAccountTv= findViewById(R.id.have_accountTv);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}