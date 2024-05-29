package com.play.ME;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MypageActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView tvName, tvEmail;
    private Button btnLogout;
    private DatabaseReference mDatabaseRef;
    private DatabaseReference mUserRef;
    private String userName;
    private Button btnSetting,btnMyPosts;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        tvName = findViewById(R.id.tv_name);
        tvEmail = findViewById(R.id.tv_email);
        btnLogout = findViewById(R.id.btn_logout);
        btnSetting = findViewById(R.id.btn_setting); // Ensure this ID matches the one in your XML layout
        btnMyPosts = findViewById(R.id.btn_my_posts);

        mDatabaseRef = FirebaseDatabase.getInstance().getReference("takeiteasy");
        mUserRef = FirebaseDatabase.getInstance().getReference("takeiteasy").child("UserAccount").child(mAuth.getCurrentUser().getUid());

        mUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserAccount userAccount = dataSnapshot.getValue(UserAccount.class);
                if (userAccount != null) {
                    userName = userAccount.getUserName();

                    if (currentUser != null) {
                        tvName.setText(userName);
                        tvEmail.setText(currentUser.getEmail());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors.
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(MypageActivity.this, LoginActivity.class));
                finish();
            }
        });

        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MypageActivity.this, EditProfileActivity.class);
                startActivity(intent);
            }
        });

        btnMyPosts.setOnClickListener(new View.OnClickListener() { // Add this block
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MypageActivity.this, WrittenPostActivity.class);
                startActivity(intent);
            }
        });
    }
}
