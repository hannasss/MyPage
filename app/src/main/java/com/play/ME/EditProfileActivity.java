package com.play.ME;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etCurrentPassword, etName, etEmail, etPassword, etConfirmPassword;
    private Button btnSave;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRef;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSave = findViewById(R.id.btnSave);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mDatabaseRef = FirebaseDatabase.getInstance().getReference("takeiteasy").child("UserAccount").child(currentUser.getUid());

        // 기존 사용자 정보 불러오기
        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserAccount userAccount = dataSnapshot.getValue(UserAccount.class);
                if (userAccount != null) {
                    etName.setText(userAccount.getUserName());
                    etEmail.setText(userAccount.getEmailId());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(EditProfileActivity.this, "사용자 정보를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });
    }

    private void updateProfile() {
        String currentPassword = etCurrentPassword.getText().toString();
        String name = etName.getText().toString();
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(currentPassword)) {
            Toast.makeText(this, "현재 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email)) {
            Toast.makeText(this, "이름과 이메일을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!TextUtils.isEmpty(password) && !password.equals(confirmPassword)) {
            Toast.makeText(this, "새 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 현재 비밀번호로 사용자 재인증
        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), currentPassword);
        currentUser.reauthenticate(credential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        String newPassword = TextUtils.isEmpty(password) ? currentPassword : password;
                        updateUserDetails(name, email, newPassword);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditProfileActivity.this, "현재 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUserDetails(String name, String email, String password) {
        UserAccount userAccount = new UserAccount();
        userAccount.setUserName(name);
        userAccount.setEmailId(email);
        userAccount.setIdToken(currentUser.getUid());
        userAccount.setPassword(password);

        // Firebase Realtime Database 업데이트
        mDatabaseRef.setValue(userAccount)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // 비밀번호 변경이 필요할 경우
                        if (!password.equals(etCurrentPassword.getText().toString())) {
                            currentUser.updatePassword(password)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(EditProfileActivity.this, "정보가 성공적으로 업데이트되었습니다.", Toast.LENGTH_SHORT).show();
                                            finish(); // 정보 업데이트 후 마이페이지로 돌아감
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(EditProfileActivity.this, "비밀번호 업데이트에 실패했습니다.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(EditProfileActivity.this, "정보가 성공적으로 업데이트되었습니다.", Toast.LENGTH_SHORT).show();
                            finish(); // 정보 업데이트 후 마이페이지로 돌아감
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditProfileActivity.this, "정보 업데이트에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
