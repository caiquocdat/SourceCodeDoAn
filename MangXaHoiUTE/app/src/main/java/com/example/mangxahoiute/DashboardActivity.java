package com.example.mangxahoiute;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.PopupMenu;

import com.example.mangxahoiute.fragments.ApproveFragment;
import com.example.mangxahoiute.fragments.ChatListFragment;
import com.example.mangxahoiute.fragments.GroupChatFragment;
import com.example.mangxahoiute.fragments.HomeFragment;
import com.example.mangxahoiute.fragments.NotificationFragment;
import com.example.mangxahoiute.fragments.ProfileFragment;
import com.example.mangxahoiute.fragments.UsersFragment;
import com.example.mangxahoiute.notifications.Token;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class DashboardActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    String mUID;
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    ActionBar actionBar;
    BottomNavigationView navigationView;
    String tokenn;
    FirebaseUser currentUser;
    String emailCurrent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        actionBar=getSupportActionBar();
        actionBar.setTitle("Home");
        firebaseAuth=FirebaseAuth.getInstance();
        currentUser=FirebaseAuth.getInstance().getCurrentUser();
        emailCurrent=currentUser.getEmail();

        //bottom navigationview
        anhXa();
        navigationView.setOnNavigationItemSelectedListener(selectedListener);
        HomeFragment fragment1= new HomeFragment();
        FragmentTransaction ft1=getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.content,fragment1,"");
        ft1.commit();

        checkUserStatus();
//        FirebaseMessaging.getInstance().getToken()
//                .addOnCompleteListener(new OnCompleteListener<String>() {
//                    @Override
//                    public void onComplete(@NonNull Task<String> task) {
//                        if (task.isSuccessful()) {
//                            updateToken(task.getResult());
//                        }
//                    }
//                });
    }

    @Override
    protected void onResume() {
        checkUserStatus();
        super.onResume();
    }

    public void updateToken(String token){
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken =new Token(token);
        ref.child(mUID).setValue(mToken);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener=
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    //handle item clicks
                    switch (item.getItemId()){
                        case R.id.nav_home:
                            actionBar.setTitle("Home");
                            //home fragment
                            HomeFragment fragment1= new HomeFragment();
                            FragmentTransaction ft1=getSupportFragmentManager().beginTransaction();
                            ft1.replace(R.id.content,fragment1,"");
                            ft1.commit();
                            return true;
                        case R.id.nav_users:
                            actionBar.setTitle("Users");
                            //users fragment
                            UsersFragment fragment2= new UsersFragment();
                            FragmentTransaction ft2=getSupportFragmentManager().beginTransaction();
                            ft2.replace(R.id.content,fragment2,"");
                            ft2.commit();
                            return true;
                        case R.id.nav_more:
                            showMoreOptions();
                            return true;
                        case R.id.nav_chat:
                            actionBar.setTitle("Chats");
                            //profile fragment
                            ChatListFragment fragment4= new ChatListFragment();
                            FragmentTransaction ft4=getSupportFragmentManager().beginTransaction();
                            ft4.replace(R.id.content,fragment4,"");
                            ft4.commit();
                            return true;

                        case R.id.nav_notification:
                            actionBar.setTitle("Notifications");
                            //profile fragment
                            NotificationFragment fragment5= new NotificationFragment();
                            FragmentTransaction ft5=getSupportFragmentManager().beginTransaction();
                            ft5.replace(R.id.content,fragment5,"");
                            ft5.commit();
                            return true;
                    }
                    return false;
                }
            };

    private void showMoreOptions() {
        PopupMenu popupMenu=new PopupMenu(this,navigationView, Gravity.END);
        popupMenu.getMenu().add(Menu.NONE,0,0,"Profile");
        popupMenu.getMenu().add(Menu.NONE,1,0,"Group Chat");
        if (emailCurrent.equals("adminutesocial123@gmail.com")){
            popupMenu.getMenu().add(Menu.NONE,2,0,"Approve Post");
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id=item.getItemId();
                if (id==0){
                    actionBar.setTitle("Profile");
                    //profile fragment
                    ProfileFragment fragment3= new ProfileFragment();
                    FragmentTransaction ft3=getSupportFragmentManager().beginTransaction();
                    ft3.replace(R.id.content,fragment3,"");
                    ft3.commit();
                }else if (id==1){
                    actionBar.setTitle("Group Chats");
                    //profile fragment
                    GroupChatFragment fragment6= new GroupChatFragment();
                    FragmentTransaction ft6=getSupportFragmentManager().beginTransaction();
                    ft6.replace(R.id.content,fragment6,"");
                    ft6.commit();
                }else if (id==2){
                    actionBar.setTitle("Approve Post");
                    //profile fragment
                    ApproveFragment fragment7= new ApproveFragment();
                    FragmentTransaction ft7=getSupportFragmentManager().beginTransaction();
                    ft7.replace(R.id.content,fragment7,"");
                    ft7.commit();
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void anhXa() {
        navigationView=findViewById(R.id.navigation);
    }

    private void checkUserStatus(){
        gso=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        gsc= GoogleSignIn.getClient(this,gso);
        GoogleSignInAccount account=GoogleSignIn.getLastSignedInAccount(this);


        FirebaseUser user=firebaseAuth.getCurrentUser();
        if (user!=null){
            mUID=user.getUid();
            SharedPreferences sp = getSharedPreferences("SP_USER",MODE_PRIVATE);
            SharedPreferences.Editor editor=sp.edit();
            editor.putString("Current_USERID",mUID);
            editor.apply();

            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            if (task.isSuccessful()) {
                                updateToken(task.getResult());
                            }
                        }
                    });

        }else {
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();

        }
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        super.onStart();
    }


}