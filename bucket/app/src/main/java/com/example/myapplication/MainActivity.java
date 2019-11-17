package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import java.lang.Integer;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
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
    public static Integer c = Color.argb(255, 255,0,0);
    private static boolean first = true;
    private static Bitmap img;
    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        image_view = findViewById(R.id.imageView1);
        if (first) {
            first = false;
        } else {
            image_view.setImageBitmap(img);
        }
        image_view.setOnTouchListener( new ImageView.OnTouchListener(){
            //ImageView.setOnTouchListener handleTouch = new View.OnTouchListener(){
            @Override
            public boolean onTouch(View imageView1, MotionEvent event){
                Intent i = getIntent();
                final Point p1 = new Point();
                //p1.x = (int)event.getX() *(getScreenWidth() /image_view.getWidth() ); //x co-ordinate where the user touches on the screen
                //p1.y = (int)event.getY()*(getScreenHeight()/ image_view.getHeight());
                /*if (img != null) {
                    p1.x *= (image_view.getWidth() / img.getWidth());
                    p1.y *= (image_view.getHeight() / img.getHeight());
                }*/
                p1.x = (int)event.getX(); //x co-ordinate where the user touches on the screen
                p1.y = (int)event.getY();
//                img = image_view.getDrawingCache();
                Log.i("TOUCH EVENT", "x:" + p1.x + ",y:" + p1.y);
                //Bitmap bitmap = findViewById(R.id.imageView1).getDrawingCache();
                Integer targetColor = Color.argb(255, 255, 255, 255);
                fill f = new fill(); // TODO port scanline from kt
//                img = ScanFillKt.Fill(img,p1.y * img.getWidth() + p1.x,targetColor,c);
                img = f.floodFill(img,p1.y * img.getWidth() + p1.x,targetColor,c);
                ((ImageView)findViewById(R.id.imageView1)).setImageBitmap(img);
                return true;
            }
        });
    }

    public void toastMe(View view) {
        // Toast myToast = Toast.makeText(this, message, duration);
//        Toast myToast = Toast.makeText(this, "big wanker",
//                Toast.LENGTH_SHORT);
//        myToast.show();
        Intent activity2 = new Intent(this, Activity2.class);
        startActivity(activity2);
    }


    public void yeet(View view) {
        Intent activity3 = new Intent(this, Activity3.class);
        startActivity(activity3);
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
                img = g.write();
                image_view.setImageBitmap(img);
                img = Bitmap.createScaledBitmap(img, image_view.getWidth(), image_view.getHeight(), false);
                //bg ll = (bg) findViewById(R.id.bg);
                //rl.setBackgroundResource(R.drawable.selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.i("TAG","SOME ERROR");
            }
    }
}}
