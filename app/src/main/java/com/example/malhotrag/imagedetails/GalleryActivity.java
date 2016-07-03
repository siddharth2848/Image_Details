package com.example.malhotrag.imagedetails;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;

public class GalleryActivity extends AppCompatActivity {

    ArrayList<String> imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        GridView gridview = (GridView) findViewById(R.id.gridview);
        imagePath = new ArrayList<>();

        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            String imagefilename = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            imagePath.add(imagefilename);
            //Long latitide = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.LATITUDE));
            //Long longitude = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.LONGITUDE));
            Log.e("TAG", "filepath: " + imagefilename);
        }
        gridview.setAdapter(new ImageAdapter(this, imagePath));
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Log.e("Path", imagePath.get(position));
                Toast.makeText(GalleryActivity.this, "" + position, Toast.LENGTH_SHORT).show();
            }
        });
    }


}
