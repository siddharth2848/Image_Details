package com.example.malhotrag.imagedetails;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by Malhotra G on 6/9/2016.
 */
public class CameraDemo extends AppCompatActivity implements SurfaceHolder.Callback {
    static Camera mCamera = null;
    SurfaceView mSurfaceView;
    SurfaceHolder mSurfaceHolder;
    boolean mPreviewRunning;
    Button btncapture;
    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] imageData, Camera c) {

            Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
            String file_path = saveToInternalSorage(bitmap);
            Toast.makeText(getApplicationContext(), "Image stored succesfully at " + file_path, Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        btncapture = (Button) findViewById(R.id.btncapture);
        mSurfaceView = (SurfaceView) findViewById(R.id.surface_camera);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        btncapture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //take picture here
                mCamera.takePicture(null, null, mPictureCallback);
            }
        });
    }

    private String saveToInternalSorage(Bitmap bitmapImage) {
        //ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        //File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        //File mypath=new File(directory,"marina1.jpg");
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File myDir = new File(root + "/saved_images");
        myDir.mkdirs();
        File file = new File(myDir, "man.jpeg");
        FileOutputStream fos = null;

        Matrix matrix = new Matrix();
        matrix.postRotate(180);
        matrix.postRotate(180);

        bitmapImage = Bitmap.createBitmap(bitmapImage, 0, 0, bitmapImage.getWidth(), bitmapImage.getHeight(), matrix, true);
        try {

            fos = new FileOutputStream(file);

            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file.toString();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int cameraCount = Camera.getNumberOfCameras();
        Toast.makeText(getApplicationContext(), String.valueOf(cameraCount), Toast.LENGTH_SHORT).show();
        mCamera = Camera.open(1);
        if (Build.VERSION.SDK_INT >= 8) mCamera.setDisplayOrientation(90);
        //set camera to continually auto-focus
        Camera.Parameters params = mCamera.getParameters();
//*EDIT*//params.setFocusMode("continuous-picture");
//It is better to use defined constraints as opposed to String, thanks to AbdelHady
        //params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        //List<String> flashModes = params.getSupportedFlashModes();
        List<Camera.Size> sizes = params.getSupportedPreviewSizes();
        Camera.Size cs = sizes.get(0);
        params.setPreviewSize(cs.width, cs.height);
        /*if (flashModes.contains(android.hardware.Camera.Parameters.FLASH_MODE_AUTO))
        {
            //Toast.makeText(getApplicationContext(),"AUTO",Toast.LENGTH_SHORT).show();
            //params.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
        }*/
        //params.set("camera-id",2);
        mCamera.setParameters(params);


    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w,
                               int h) {
        if (mPreviewRunning) {
            mCamera.stopPreview();
        }
        Camera.Parameters p = mCamera.getParameters();
        p.setPreviewSize(w, h);
        mCamera.setParameters(p);
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
        mPreviewRunning = true;

    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mPreviewRunning = false;
        mCamera.release();

    }
}
