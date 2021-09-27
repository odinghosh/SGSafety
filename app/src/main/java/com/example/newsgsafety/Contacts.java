package com.example.newsgsafety;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Map;

public class Contacts extends AppCompatActivity {

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;
    Button addButton;
    Button removeButton;
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
                db.update("friend_list", FieldValue.arrayUnion(user));
                refresh();

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

    public void hazards (View view){
        startActivity(new Intent(getApplicationContext(),Hazards.class));
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



}