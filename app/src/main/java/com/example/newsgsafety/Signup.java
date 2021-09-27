package com.example.newsgsafety;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Signup extends AppCompatActivity {

    public static final String TAG = "TAG";
    EditText mUsername,mEmail,mPassword,mRepassword;
    Button mRegisterBtn;
    TextView mLoginBtn;
    FirebaseAuth fAuth;
    ProgressBar progressBar;
    FirebaseFirestore fStore;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mUsername    = findViewById(R.id.username);
        mEmail       = findViewById(R.id.emailaddress);
        mPassword    = findViewById(R.id.password);
        mRepassword  = findViewById(R.id.repassword);
        mRegisterBtn = findViewById(R.id.register);
        mLoginBtn    = findViewById(R.id.gotologin);

        progressBar = findViewById(R.id.progressBar);
        fAuth       = FirebaseAuth.getInstance();
        fStore      = FirebaseFirestore.getInstance();

        if(fAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
        }

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username     = mUsername.getText().toString().trim();
                String email        = mEmail.getText().toString().trim();
                String password     = mPassword.getText().toString().trim();
                String repassword   = mRepassword.getText().toString().trim();

                if(TextUtils.isEmpty(username)){
                    mUsername.setError("Username is required!");
                    return;
                }
                if(TextUtils.isEmpty(email)){
                    mEmail.setError("Email is required!");
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    mPassword.setError("Password is required!");
                    return;
                }
                if(TextUtils.isEmpty(repassword)){
                    mRepassword.setError("Please verify your password");
                    return;
                }
                if(password.length()<8){
                    mPassword.setError("Your password must be at least 8 characters long!");
                    return;
                }
                if (!IsValidPassword(password)){
                    mPassword.setError("Your password should only have alphanumeric and accepted special characters");
                    return;
                }
                if(!TextUtils.equals(password,repassword)) {
                    mRepassword.setError("Your passwords do not match!");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(Signup.this,"User Created!",Toast.LENGTH_SHORT).show();
                            // Storing Username under the Unique ID, Refer to this when adding contacts
                            userID = fAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = fStore.collection("users").document(userID);
                            Map<String,Object> user = new HashMap<>();
                            user.put("username",username);
                            user.put("email",email);
                            user.put("friend_list", new ArrayList());
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d(TAG, "User Profile is created for" + userID);
                                }
                            });
                            startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        }
                        else{
                            Toast.makeText(Signup.this,"Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });

            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),Login.class));
            }
        });

    }

    public Boolean IsValidPassword(String password){
        return password.matches("^[a-zA-Z0-9-._!\"`'#%&,:;<>=@{}~$()*+/\\\\?\\[\\]^|]*$");
    }

}