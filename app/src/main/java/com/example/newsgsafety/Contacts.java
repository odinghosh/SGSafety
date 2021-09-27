package com.example.newsgsafety;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class Contacts extends AppCompatActivity {

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;
    Button addButton;
    Button removeButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        fAuth       = FirebaseAuth.getInstance();
        fStore      = FirebaseFirestore.getInstance();
        addButton = findViewById(R.id.add_contact);
        removeButton = findViewById(R.id.remove_contact);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText contact = findViewById(R.id.contact_editor);
                String user = contact.getText().toString();
                userID = fAuth.getCurrentUser().getUid();
                DocumentReference db = fStore.collection("users").document(userID);
                db.update("friend_list", FieldValue.arrayUnion(user));
            }
        });
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText contact = findViewById(R.id.contact_editor);
                String user = contact.getText().toString();
                userID = fAuth.getCurrentUser().getUid();
                DocumentReference db = fStore.collection("users").document(userID);
                db.update("friend_list", FieldValue.arrayRemove(user));
            }
        });
    }

    public void main (View view){
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
        finish();
    }

    public void contacts (View view){
        startActivity(new Intent(getApplicationContext(),Contacts.class));
        finish();
    }

    public void hazards (View view){
        startActivity(new Intent(getApplicationContext(),Hazards.class));
        finish();
    }

    public void settings (View view){
        startActivity(new Intent(getApplicationContext(),Settings.class));
        finish();
    }

    public void addContact(){
        TextView contact = findViewById(R.id.textView4);
        String user = contact.getText().toString();
        userID = fAuth.getCurrentUser().getUid();
        DocumentReference db = fStore.collection("users").document(userID);
        db.update("friend_list", FieldValue.arrayUnion(user));



    }

    public void removeContact(){
        TextView contact = findViewById(R.id.textView4);
        String user = contact.getText().toString();
        userID = fAuth.getCurrentUser().getUid();
        DocumentReference db = fStore.collection("users").document(userID);
        db.update("friend_list", FieldValue.arrayRemove(user));
    }
}