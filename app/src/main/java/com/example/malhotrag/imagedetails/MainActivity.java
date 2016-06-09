package com.example.malhotrag.imagedetails;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    TextView Exif;
    Button button;
    ImageView imageView;
    private boolean valid = false;
    Double Latitude, Longitude;
    String imagefile = Environment.getExternalStorageDirectory().toString() + "/PDF/img.jpg";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.imageView);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
        Bitmap bm = BitmapFactory.decodeFile(imagefile);
        imageView.setImageBitmap(bm);
        Exif = (TextView)findViewById(R.id.exif);
        Exif.setText(ReadExif(imagefile));


    }
    public String ReadExif(String file){
        try {
            ExifInterface exif = new ExifInterface(file);
            String attrLATITUDE = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            String attrLATITUDE_REF = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
            String attrLONGITUDE = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            String attrLONGITUDE_REF = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);

            if((attrLATITUDE !=null) && (attrLATITUDE_REF !=null) && (attrLONGITUDE != null) && (attrLONGITUDE_REF !=null))
            {
                valid = true;

                if(attrLATITUDE_REF.equals("N")){
                    Latitude = convertToDegree(attrLATITUDE);
                }
                else{
                    Latitude = 0 - convertToDegree(attrLATITUDE);
                }

                if(attrLONGITUDE_REF.equals("E")){
                    Longitude = convertToDegree(attrLONGITUDE);
                }
                else{
                    Longitude = 0 - convertToDegree(attrLONGITUDE);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
        }
        return String.valueOf(Longitude) + " , " + String.valueOf(Latitude);
    }

    private Double convertToDegree(String stringDMS){
        Double result;
        String[] DMS = stringDMS.split(",", 3);

        String[] stringD = DMS[0].split("/", 2);
        Double D0 = new Double(stringD[0]);
        Double D1 = new Double(stringD[1]);
        Double FloatD = D0/D1;

        String[] stringM = DMS[1].split("/", 2);
        Double M0 = new Double(stringM[0]);
        Double M1 = new Double(stringM[1]);
        Double FloatM = M0/M1;

        String[] stringS = DMS[2].split("/", 2);
        Double S0 = new Double(stringS[0]);
        Double S1 = new Double(stringS[1]);
        Double FloatS = S0/S1;

        result = new Double(FloatD + (FloatM/60) + (FloatS/3600));

        return result;
    }
    public boolean isValid()
    {
        return valid;
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(getApplicationContext(),MapsActivity.class);
        intent.putExtra("Lat",Latitude.toString());
        intent.putExtra("Lng",Longitude.toString());
        startActivity(intent);
    }

    /*@Override
    public String toString() {
        // TODO Auto-generated method stub
        return (String.valueOf(Latitude) + ", " + String.valueOf(Longitude));
    }*/

    /*public int getLatitudeE6(){
        return (int)(Latitude*1000000);
    }

    public int getLongitudeE6(){
        return (int)(Longitude*1000000);
    }*/
}
