package com.hackaton.bordarga.locationtest;

import android.Manifest;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.opengl.GLSurfaceView;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SensorEventListener{
    private FusedLocationProviderClient mFusedLocation;
    private LocationCallback mLocationCallback;
    private LocationRequest lr;
    private final int PERMISSION_FINE_LOCATION_CODE = 10;
    private final int REQUEST_CHECK_SETTINGS = 20;
    private boolean trackUpdates = false;

    private GLSurfaceView mMySurface;
    private MyRenderer mRenderer;

    private Button moveUpBt;
    private Button moveDownBt;
    private Button moveLeftBt;
    private Button moveRightBt;
    private Button moveForwardBt;
    private Button moveBackwardBt;
    private Button turnUpBt;
    private Button turnDownBt;
    private Button turnRightBt;
    private Button turnLeftBt;

    private TextView logText;
    private TextView logText2;

    private Location targetLocation;
    private SensorManager sManager;
    private Sensor gyroscopeSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        targetLocation = new Location("prov");
        targetLocation.setLatitude(38.426394);
        targetLocation.setLongitude(-0.450412);

        mMySurface = (GLSurfaceView)findViewById(R.id.my_surface);
        logText = (TextView)findViewById(R.id.log_text);
        logText2 = (TextView)findViewById(R.id.log_text2);

        moveUpBt = (Button)findViewById(R.id.turn_up_bt);
        moveDownBt = (Button)findViewById(R.id.turn_down_bt);
        moveLeftBt = (Button)findViewById(R.id.turn_left_bt);
        moveRightBt = (Button)findViewById(R.id.turn_right_bt);
        moveForwardBt = (Button)findViewById(R.id.turn_forward_bt);
        moveBackwardBt = (Button)findViewById(R.id.turn_backward_bt);
        turnUpBt = (Button)findViewById(R.id.rotate_up_bt);
        turnDownBt = (Button)findViewById(R.id.rotate_down_bt);
        turnLeftBt = (Button)findViewById(R.id.rotate_left_bt);
        turnRightBt = (Button)findViewById(R.id.rotate_right_bt);


        moveUpBt.setOnClickListener(this);
        moveDownBt.setOnClickListener(this);
        moveLeftBt.setOnClickListener(this);
        moveRightBt.setOnClickListener(this);
        moveForwardBt.setOnClickListener(this);
        moveBackwardBt.setOnClickListener(this);
        turnUpBt.setOnClickListener(this);
        turnDownBt.setOnClickListener(this);
        turnLeftBt.setOnClickListener(this);
        turnRightBt.setOnClickListener(this);

        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                //Log.e("Location_update", "Latitud: " + locationResult.getLastLocation().getLatitude() +
                //    " Longitud: " + locationResult.getLastLocation().getLongitude());
                double distance = getDistance(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude(),
                        targetLocation.getLatitude(), targetLocation.getLongitude());

                //Log.e("update", "distancia es " + distance);



               // mRenderer.getSquare(0).setPosition(new float[]{0.0f, 0.0f, (float)distance * -1});

           }
        };

        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
        startUpdates();


        mMySurface.setEGLContextClientVersion(2);
        mRenderer = new MyRenderer();
        mMySurface.setRenderer(mRenderer);

        sManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        gyroscopeSensor = sManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
    }

    private void createLocationRequest() {
        lr = new LocationRequest();
        lr.setInterval(0)
                .setFastestInterval(0)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(lr);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                trackUpdates = true;
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case CommonStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(MainActivity.this,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(trackUpdates)
            startUpdates();
        sManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);

    }

    private void startUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }else
            mFusedLocation.requestLocationUpdates(lr, mLocationCallback, null);
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.turn_up_bt:
                mRenderer.processInput(MyRenderer.InputType.MOVE_UP);
                break;

            case R.id.turn_down_bt:
                mRenderer.processInput(MyRenderer.InputType.MOVE_DOWN);
                break;

            case R.id.turn_forward_bt:
                mRenderer.processInput(MyRenderer.InputType.MOVE_FORWARD);
                break;

            case R.id.turn_backward_bt:
                mRenderer.processInput(MyRenderer.InputType.MOVE_BACKWARD);
                break;

            case R.id.turn_right_bt:
                mRenderer.processInput(MyRenderer.InputType.MOVE_RIGHT);
                break;

            case R.id.turn_left_bt:
                mRenderer.processInput(MyRenderer.InputType.MOVE_LEFT);
                break;

            case R.id.rotate_down_bt:
                mRenderer.processInput(MyRenderer.InputType.TURN_DOWN);
                break;

            case R.id.rotate_up_bt:
                mRenderer.processInput(MyRenderer.InputType.TURN_UP);
                Toast.makeText(getApplicationContext(), "a", Toast.LENGTH_SHORT).show();
                break;

            case R.id.rotate_left_bt:
                mRenderer.processInput(MyRenderer.InputType.TURN_LEFT);
                break;

            case R.id.rotate_right_bt:
                mRenderer.processInput(MyRenderer.InputType.TURN_RIGHT);
                break;
        }
    }

    private double getDistance(double la1, double lo1, double la2, double lo2){
        double p = Math.PI / 180;
        double a = 0.5 - Math.cos((la2 - la1) * p) / 2 + Math.cos(la1 * p) * Math.cos(la2 * p) *
                (1 - Math.cos((lo2 - lo1) * p)) / 2;

        return 12742 * Math.asin(Math.sqrt(a)) * 1000;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        logText.setText(String.format("[0]: %.2f\n[1]: %.2f\n[2]: %.2f\n[3]: %.2f\n",
            event.values[0], event.values[1], event.values[2], event.values[3]));
        event.values[0] -= 0.50f;

        logText2.setText(String.format("[0]: %.2f\n[1]: %.2f\n",
                event.values[0] * 100, ((-90.0f - event.values[1]) + (event.values[1] * (-1 * 264.7058f)))));

        //mRenderer.setYawPitchRoll(new float[]{-90.0f - event.values[1] * 100, event.values[0] * 100,0.0f});
        mRenderer.setYawPitchRoll(new float[]{(-90.0f - event.values[1]) + (event.values[1] * (-1 * 264.7058f)) , event.values[0] * 100,0.0f});
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }



    @Override
    protected void onPause() {
        super.onPause();
        sManager.unregisterListener(this);
    }
}
