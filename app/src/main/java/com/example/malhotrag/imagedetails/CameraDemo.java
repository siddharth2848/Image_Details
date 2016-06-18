package com.example.malhotrag.imagedetails;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * Created by Malhotra G on 6/9/2016.
 */
public class CameraDemo extends AppCompatActivity implements SurfaceHolder.Callback, View.OnClickListener {

    static final int FOTO_MODE = 0;
    double lat;
    double lon;
    Button bt;
    private LocationManager locationManager;
    private SurfaceView surefaceView;
    private SurfaceHolder surefaceHolder;
    private LocationListener locationListener;
    private Camera camera;
    private String make;
    private String model;
    private String imei;
    /*AutoFocusCallback myAutoFocusCallback = new AutoFocusCallback() {

        @Override
        public void onAutoFocus(boolean arg0, Camera arg1) {
            Toast.makeText(getApplicationContext(), "'It is ready to take the photograph !!!", Toast.LENGTH_SHORT).show();
        }
    };*/
    Camera.PictureCallback pictureCallBack = new Camera.PictureCallback() {

        public void onPictureTaken(byte[] data, Camera camera) {
            if (data != null) {
                Intent imgIntent = new Intent();
                storeByteImage(data);
                camera.startPreview();
                setResult(FOTO_MODE, imgIntent);
            }

        }
    };
    private Location thislocation;
    private boolean previewRunning = false;

    private static String formatLatLongString(double d) {
        StringBuilder b = new StringBuilder();
        b.append((int) d);
        b.append("/1,");
        d = (d - (int) d) * 60;
        b.append((int) d);
        b.append("/1,");
        d = (d - (int) d) * 60000;
        b.append((int) d);
        b.append("/1000");
        return b.toString();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        surefaceView = (SurfaceView) findViewById(R.id.surface_camera);
        surefaceView.setOnClickListener(this);
        surefaceHolder = surefaceView.getHolder();
        surefaceHolder.addCallback(this);
        surefaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        bt = (Button) findViewById(R.id.btncapture);
        bt.setOnClickListener(this);

        locationListener = new LocationListener() {

            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            public void onProviderEnabled(String provider) {

            }

            public void onProviderDisabled(String provider) {

            }

            public void onLocationChanged(Location location) {

                CameraDemo.this.gpsLocationReceived(location);

                if (ActivityCompat.checkSelfPermission(CameraDemo.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(CameraDemo.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    lat = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLatitude();
                    lon = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLongitude();
                }

            }
        };


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria locationCritera = new Criteria();
        locationCritera.setAccuracy(Criteria.ACCURACY_COARSE);
        locationCritera.setAltitudeRequired(false);
        locationCritera.setBearingRequired(false);
        locationCritera.setCostAllowed(true);
        locationCritera.setPowerRequirement(Criteria.NO_REQUIREMENT);
        String providerName = locationManager.getBestProvider(locationCritera, true);

        if (providerName != null && locationManager.isProviderEnabled(providerName)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

            }
            locationManager.requestLocationUpdates(providerName, 20000, 100, CameraDemo.this.locationListener);
        } else {
            // Provider not enabled, prompt user to enable it
            Toast.makeText(CameraDemo.this, "please_turn_on_gps", Toast.LENGTH_LONG).show();
            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            CameraDemo.this.startActivity(myIntent);
        }

        if (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null) {

            lat = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude();
            lon = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude();
        } else if (locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null) {
            Log.e("TAG", "Inside NETWORK");

            lat = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLatitude();
            lon = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLongitude();

        } else {

            Log.e("TAG", "else +++++++ ");
            lat = -1;
            lon = -1;
        }

    }

    protected void gpsLocationReceived(Location location) {

        thislocation = location;
    }

    public boolean storeByteImage(byte[] data) {

        /*String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File myDir = new File(root + "/saved_images");
        myDir.mkdirs();
        File filename = new File(myDir, "man.jpeg");*/


        String filename = Environment.getExternalStorageDirectory() + String.format("/%d.jpeg", System.currentTimeMillis());
        Log.e("TAG", "filename = " + filename);

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(filename);
            try {
                fileOutputStream.write(data);
                Log.e("TAG", "Image file created, size in bytes = " + data.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
            fileOutputStream.flush();
            fileOutputStream.close();

            Toast.makeText(CameraDemo.this, "lat: " + lat + " ,lng: " + lon, Toast.LENGTH_SHORT).show();

            Log.e("TAG", "lat =" + lat + "  lon :" + lon);
            ExifInterface exif = new ExifInterface(filename.toString());
            createExifData(exif, lat, lon);
            exif.saveAttributes();

            /*Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
            while (cursor.moveToNext()) {
                String imagefilename = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                Long latitide = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.LATITUDE));
                Long longitude = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.LONGITUDE));

                Log.e("TAG", "filepath: " + imagefilename + " latitude = " + latitide + "  longitude = " + longitude);
            }*/

            return true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void createExifData(ExifInterface exif, double lattude, double longitude) {

        if (lattude < 0) {
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "S");
            lattude = -lattude;
        } else {
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "N");
        }

        exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE,
                formatLatLongString(lattude));

        if (longitude < 0) {
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "W");
            longitude = -longitude;
        } else {
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "E");
        }
        exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE,
                formatLatLongString(longitude));

        try {
            exif.saveAttributes();
        } catch (IOException e) {

            e.printStackTrace();
        }
        make = android.os.Build.MANUFACTURER; // get the make of the device
        model = android.os.Build.MODEL; // get the model of the divice

        exif.setAttribute(ExifInterface.TAG_MAKE, make);
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        imei = telephonyManager.getDeviceId();
        exif.setAttribute(ExifInterface.TAG_MODEL, model + " - " + imei);

        exif.setAttribute(ExifInterface.TAG_DATETIME, (new Date(System.currentTimeMillis())).toString()); // set the date & time

        Log.e("TAG", "Information : lat =" + lattude + "  lon =" + longitude + "  make = " + make + "  model =" + model + "  imei=" + imei + " time =" + (new Date(System.currentTimeMillis())).toString());
    }

    protected boolean isRouteDisplayed() {
        return false;
    }


    @Override
    public void onClick(View v) {

        camera.takePicture(null, pictureCallBack, pictureCallBack);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        camera = Camera.open();
        Camera.Parameters parameters = camera.getParameters();
        camera.setDisplayOrientation(90);

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        if (previewRunning) {
            camera.stopPreview();
        }

        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.startPreview();
        previewRunning = true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        previewRunning = false;
        camera.release();

    }

    /*public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.cameramenu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item01:
                Toast.makeText(this, "Pressed !", Toast.LENGTH_LONG).show();
                break;
            case R.id.item03:
                System.exit(0);
                break;
        }
        return true;
    }*/


    @Override
    protected void onStop() {
        super.onStop();
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            locationManager.removeUpdates(this.locationListener);
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

    }
}