package com.example.myapplication;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
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
import android.graphics.Color;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

import static android.graphics.Bitmap.createBitmap;

public class fill {
    public Bitmap floodFill(Bitmap  image, int index, int target, int replacement) {
        int width = image.getWidth();
        int height = image.getHeight();
        Bitmap buffer = createBitmap(width, height, Bitmap.Config.ARGB_8888, true);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                buffer.setPixel(i, j, image.getPixel(i, j));
            }
        }
        Log.i("TAG","loading");
        if (target != replacement) {
            Queue<Integer> queue = new PriorityQueue<>();
            queue.add(index);
            while (queue.size() > 0) {
                Integer head = queue.poll();
                int x = head % width;
                int y = head / width;
                if (x >= 0 && x < width && y >= 0 && y < height) {
                    if (buffer.getPixel(x, y) == target) {
                        buffer.setPixel(x, y, replacement);
                        if (y != height - 1) queue.add((y + 1) * width + x);
                        if (y != 0) queue.add((y - 1) * width + x);
                        if (x != width - 1) queue.add(y * width + (x + 1));
                        if (x != 0) queue.add(y * width + (x - 1));
                    }
                }
            }
        }
        return buffer;
    }
}