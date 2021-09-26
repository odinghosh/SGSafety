package com.example.newsgsafety;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {

    EditText mEmail,mPassword;
    Button mLoginBtn;
    TextView mCreateBtn;
    FirebaseAuth fAuth;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmail      = findViewById(R.id.emailaddress2);
        mPassword   = findViewById(R.id.password2);
        fAuth       = FirebaseAuth.getInstance();
        mLoginBtn   = findViewById(R.id.login);
        mCreateBtn  = findViewById(R.id.gotosignup);
        progressBar = findViewById(R.id.progressBar2);

        mLoginBtn.setOnClickListener(view -> {

            String email        = mEmail.getText().toString().trim();
            String password     = mPassword.getText().toString().trim();

            if(TextUtils.isEmpty(email)){
                mEmail.setError("Email is required!");
                return;
            }
            if(TextUtils.isEmpty(password)){
                mPassword.setError("Password is required!");
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            // Authenticate User

            fAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    Toast.makeText(Login.this,"Login Successful!",Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                }
                else{
                    Toast.makeText(Login.this,"Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                }
            });

        });

        mCreateBtn.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(),Signup.class)));

    }
}