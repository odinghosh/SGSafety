package com.example.newsgsafety;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Flood extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_flood);

        init();
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

    public void init() {
        Bundle extras = getIntent().getExtras();
        String curLocation = extras.getString("location");
        String[] location = {curLocation} ; //For Testing
        String[] description = {"Risk of Lightning"}; //For Testing

        TableLayout stk = (TableLayout) findViewById(R.id.table_main);
        TableRow tbrow0 = new TableRow(this);
        TextView tv0 = new TextView(this);
        tv0.setPadding(30,15,30,15);
        tv0.setGravity(Gravity.CENTER);
        tv0.setText(" Location ");
        tv0.setTextSize(20);
        tv0.setTextColor(Color.BLACK);
        tv0.setBackgroundColor(Color.parseColor("#90EE90"));
        tbrow0.addView(tv0);
        TextView tv1 = new TextView(this);
        tv1.setPadding(30,15,30,15);
        tv0.setGravity(Gravity.CENTER);
        tv1.setText(" Description ");
        tv1.setTextSize(20);
        tv1.setTextColor(Color.BLACK);
        tv1.setBackgroundColor(Color.parseColor("#90EE90"));
        tbrow0.addView(tv1);
        stk.addView(tbrow0);
        for (int i = 0; i < location.length; i++) {
            TableRow tbrow = new TableRow(this);
            TextView t1v = new TextView(this);
            t1v.setText("" + location[i]);
            t1v.setTextColor(Color.BLACK);
            t1v.setPadding(30,15,30,15);
            t1v.setGravity(Gravity.CENTER);
            t1v.setTextSize(16);
            tbrow.addView(t1v);
            TextView t2v = new TextView(this);
            t2v.setText("" + description[i]);
            t2v.setTextColor(Color.BLACK);
            t1v.setPadding(30,15,30,15);
            t2v.setGravity(Gravity.CENTER);
            t2v.setTextSize(16);
            tbrow.addView(t2v);
            stk.addView(tbrow);
        }

    }

}