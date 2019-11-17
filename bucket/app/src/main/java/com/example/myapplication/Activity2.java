package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SVBar;

public class Activity2 extends AppCompatActivity {

    private ColorPicker picker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2);
        picker = (ColorPicker) findViewById(R.id.picker);
        SVBar svBar = (SVBar) findViewById(R.id.svbar);
        picker.addSVBar(svBar);
        picker.setShowOldCenterColor(false); //this line causes error


    }
    protected void onBackPressed(Bundle savedInstanceState){
        Integer color = picker.getColor();
        Intent i = new Intent(Activity2.this, MainActivity.class);
        i.putExtra("intVariableName",color);
        Log.i("TAG","loading"+color);
        startActivity(i);

    }
    public void back(View view) {
        Integer color = picker.getColor();
        Intent i = new Intent(Activity2.this, MainActivity.class);
        i.putExtra("intVariableName",color);
        i.putExtra("source", 1); // 1 => cpicker
        MainActivity.c = color;
        Log.i("TAG","loading"+color);
        startActivity(i);
    }
}
