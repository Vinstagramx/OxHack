package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.os.Bundle;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    //private static int RESULT_LOAD_IMAGE = 1;
    RelativeLayout rl;
    public ImageView image_view;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void toastMe(View view) {
        // Toast myToast = Toast.makeText(this, message, duration);
//        Toast myToast = Toast.makeText(this, "big wanker",
//                Toast.LENGTH_SHORT);
//        myToast.show();
        Intent activity2 = new Intent(this, Activity2.class);
        startActivity(activity2);
    }

    public void loadImage(View view) {

        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        int RESULT_LOAD_IMG = 1;
        startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                //Bitmap selectedImage2 = Bitmap.createScaledBitmap(selectedImage, (int) Math.round(selectedImage.getWidth() / 1.7), (int) Math.round(selectedImage.getHeight() / 1.7), false);
                Bitmap selectedImage2 = Bitmap.createScaledBitmap(selectedImage, 600, 800, false);
                final ImageGenerator g = new ImageGenerator(selectedImage2);
                image_view = findViewById(R.id.imageView1);
                //g.blur(1, false);
                g.kmeans(5);
                g.blur(1, false);
                g.edge(1);
                g.fixGaps(15, PointType.WHITE);
                g.fixGaps(150, PointType.BLACK);
                image_view.setImageBitmap(g.write());
                //bg ll = (bg) findViewById(R.id.bg);
                //rl.setBackgroundResource(R.drawable.selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.i("TAG","SOME ERROR");
            }
    }
}}
