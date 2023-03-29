package com.example.campusguessr;

import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;


//source: https://codelabs.developers.google.com/codelabs/camerax-getting-started/

public class CameraCapture extends AppCompatActivity implements SensorEventListener {

    // variables for location capturing
    private int REQUEST_CODE_PERMISSIONS = 10; //arbitrary number, can be changed accordingly
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA"
            , "android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"};
    private FusedLocationProviderClient fusedLocationClient;
    private String currentCoords;
    private String lastCoords;

    // variables for orientation capturing
    private SensorManager sensorManager;
    private SensorEvent acc_event;
    private SensorEvent mag_event;
    TextureView txView;
    TextView locationText;
    TextView orientationText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_capture);

        // initializing views
        txView = findViewById(R.id.view_finder);
        locationText = findViewById(R.id.locationCapText);
        orientationText = findViewById(R.id.orientationCapText);

        // fusedLocationClient utilizes Google Play services location API
        // for a more accurate and efficient location capturing
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // initializing orientation capturing stuff
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // set up listener for the accelerometer
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);

        // set up listener for the magnetic field sensor
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_NORMAL);

        // must check that all permissions are granted before starting up the camera
        if (allPermissionsGranted()) {
            startCamera(); //start camera if permission has been granted by user
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    private void startCamera() {
        // need to call getLocation() once in order for FusedLocationClient
        // to be ready for the getLocation() call in onClick()
        getLocation();
        //make sure there isn't another camera instance running before starting
        CameraX.unbindAll();

        /* start preview */
        int aspRatioW = txView.getWidth(); //get width of screen
        int aspRatioH = txView.getHeight(); //get height
        Rational asp = new Rational(aspRatioW, aspRatioH); //aspect ratio
        Size screen = new Size(aspRatioW, aspRatioH); //size of the screen

        //config obj for preview/viewfinder thingy.
        PreviewConfig pConfig = new PreviewConfig.Builder().setTargetAspectRatio(asp).setTargetResolution(screen).build();
        Preview preview = new Preview(pConfig); //lets build it

        preview.setOnPreviewOutputUpdateListener(
                new Preview.OnPreviewOutputUpdateListener() {
                    //to update the surface texture we have to destroy it first, then re-add it
                    @Override
                    public void onUpdated(Preview.PreviewOutput output) {
                        ViewGroup parent = (ViewGroup) txView.getParent();
                        parent.removeView(txView);
                        parent.addView(txView, 0);

                        txView.setSurfaceTexture(output.getSurfaceTexture());
                        updateTransform();
                    }
                });

        /* image capture */

        //config obj, selected capture mode
        ImageCaptureConfig imgCapConfig = new ImageCaptureConfig.Builder().setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation()).build();
        final ImageCapture imgCap = new ImageCapture(imgCapConfig);

        findViewById(R.id.capture_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /////// location capture ///////
                getLocation();
                String locationCoord = currentCoords;
                if (locationCoord != null) {
                    locationText.setText("Location: " + locationCoord);
                } else {
                    locationText.setText("Location: NULL");
                }

                /////// orientation capture ///////
                float[] orientationValues = new float[3];
                float[] rotationMatrix = new float[9];
                SensorManager.getRotationMatrix(rotationMatrix, null, acc_event.values, mag_event.values);
                SensorManager.getOrientation(rotationMatrix,orientationValues);
                // note: orientationValues[0] is azimuth, which is the "absolute heading of yaw"
                // orientationValues[1] is pitch
                // orientationValues[2] is roll
                String orientationString = "A: " + orientationValues[0] + " P: " + orientationValues[1]
                        + "\nR: " + orientationValues[2];
                orientationText.setText(orientationString);

                /////// image capture ///////
                File file = new File(getFilesDir() + "/" + System.currentTimeMillis() + ".jpg");
                imgCap.takePicture(file, new ImageCapture.OnImageSavedListener() {
                    @Override
                    public void onImageSaved(@NonNull File file) {
                        String msg = "Photo capture succeeded: " + file.getAbsolutePath();
                        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) {
                        String msg = "Photo capture failed: " + message;
                        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
                        if (cause != null) {
                            cause.printStackTrace();
                        }
                    }
                });
            }
        });

        /* image analyser */

        ImageAnalysisConfig imgAConfig = new ImageAnalysisConfig.Builder().setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE).build();
        ImageAnalysis analysis = new ImageAnalysis(imgAConfig);

        analysis.setAnalyzer(
                new ImageAnalysis.Analyzer() {
                    @Override
                    public void analyze(ImageProxy image, int rotationDegrees) {
                        // analyze code here
                    }
                });

        //bind to lifecycle:
        CameraX.bindToLifecycle((LifecycleOwner) this, analysis, imgCap, preview);
    }

    private void updateTransform() {
        /*
         * compensates the changes in orientation for the viewfinder, bc the rest of the layout stays in portrait mode.
         * imgCap does this already, this class can be commented out or be used to optimise the preview
         */
        Matrix mx = new Matrix();
        float w = txView.getMeasuredWidth();
        float h = txView.getMeasuredHeight();

        float centreX = w / 2f; //calc centre of the viewfinder
        float centreY = h / 2f;

        int rotationDgr;
        int rotation = (int) txView.getRotation(); //cast to int bc switches don't like floats

        switch (rotation) { //correct output to account for display rotation
            case Surface.ROTATION_0:
                rotationDgr = 0;
                break;
            case Surface.ROTATION_90:
                rotationDgr = 90;
                break;
            case Surface.ROTATION_180:
                rotationDgr = 180;
                break;
            case Surface.ROTATION_270:
                rotationDgr = 270;
                break;
            default:
                return;
        }

        mx.postRotate((float) rotationDgr, centreX, centreY);
        txView.setTransform(mx); //apply transformations to textureview
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //start camera when permissions have been granted otherwise exit app
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private boolean allPermissionsGranted() {
        //check if req permissions have been granted
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // Note: getCurrentLocation is a more accurate method of retrieving location
        //fusedLocationClient.getLastLocation()
        //        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
        //            @Override
        //            public void onSuccess(Location location) {
        //                if (location != null) {
        //                    lastCoords = Double.toString(location.getLatitude()) + ", " + Double.toString(location.getLongitude());
        //                }
        //            }
        //        });
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(CameraCapture.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            currentCoords = location.getLatitude() + ", " + location.getLongitude();
                        }
                    }
                });
        return;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // set acc_event to the most recent accelerometer reading
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            acc_event = event;
        }

        // set mag_event to the most recent magnetic field reading
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mag_event = event;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // need this method because android studio will throw an error without it
    }
}