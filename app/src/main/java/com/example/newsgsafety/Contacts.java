package com.example.newsgsafety;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.model.DocumentCollections;

import java.util.ArrayList;
import java.util.Map;

public class Contacts extends AppCompatActivity {

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;
    ImageButton addButton;
    ImageButton removeButton;
    DocumentReference db;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        fAuth       = FirebaseAuth.getInstance();
        fStore      = FirebaseFirestore.getInstance();
        addButton = findViewById(R.id.add_contact);
        removeButton = findViewById(R.id.remove_contact);
        userID = fAuth.getCurrentUser().getUid();
        db = fStore.collection("users").document(userID);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("button pressed");
                EditText contact = findViewById(R.id.contact_editor);
                String user = contact.getText().toString();
                CollectionReference userList = fStore.collection("users");
                Query query = userList.whereEqualTo("username", user);
                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            {
                                if(task.getResult().getDocuments().size() > 0) {
                                    db.update("friend_list", FieldValue.arrayUnion(user));
                                    refresh();
                                } else {
                                    System.out.println("username not found.");
                                    Toast.makeText(Contacts.this, "username not found", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                });
            }
        });
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText contact = findViewById(R.id.contact_editor);
                String user = contact.getText().toString();
                db.update("friend_list", FieldValue.arrayRemove(user));
                refresh();
            }
        });
        refresh();

    }

    public void main (View view){
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
        finish();
    }

    public void contacts (View view){
        startActivity(new Intent(getApplicationContext(),Contacts.class));
        finish();
    }

    public void settings (View view){
        startActivity(new Intent(getApplicationContext(),Settings.class));
        finish();
    }

    public void refresh(){
        db.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();

                    Map<String, Object> userData =  document.getData();
                    ArrayList<String> contacts = (ArrayList<String>) userData.get("friend_list");
                    System.out.println(contacts);

                    TextView contactDisplay = findViewById(R.id.textView4);
                    String outputText = "";
                    for(int i = 0; i < contacts.size(); i++){
                      outputText += (contacts.get(i) + "\n");
                    }
                    contactDisplay.setText(outputText);
                }

            }
        });
    }


    boolean valid = false;
    public boolean validUser(String username){
        valid = false;
        CollectionReference db = fStore.collection("users");
        Query query = db.whereEqualTo("username", username);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult() != null){
                        valid = true;

                    }

                }

            }
        });

        return valid;

    }



}