package viewing.com.viewing;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener{
    private final String TAG = "MainActivity";

    private SensorManager mSensorManager;
    private Sensor mSensorLinearAcceleration;
    private Sensor mSensorGyroscope;

    double[] normalized_gyro = new double[3];
    double[] raw_accel = new double[3];
    double[] raw_gyro = new double[3];
    double[] linear_acceleration = new double[3];

    double rawAccelMagnitude = 0.0;
    double rawOmegaMagnitude = 0.0;
    final double alpha = 0.8;
    String speed = "";
    boolean start = false;

    private Button button;
    private EditText editText;
    private TextView textView;

    private RelativeLayout rLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button)findViewById(R.id.button1);
        editText = (EditText)findViewById(R.id.editText);
        textView = (TextView) findViewById(R.id.textView);

        rLayout = (RelativeLayout)findViewById(R.id.mainLayout);


        button.setOnClickListener(this);
        rLayout.setOnClickListener(this);

        button.setClickable(false);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorLinearAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mSensorGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        System.out.println("Build.VERSION.SDK_INT >= 23 = " + Build.VERSION.SDK_INT);
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 12);

                return;
            }
        }else{
            button.setClickable(true);
        }
    }




    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        if(event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
            raw_accel[0] = String.valueOf(event.values[0]).equalsIgnoreCase("NaN")? 0:event.values[0];
            raw_accel[1] = String.valueOf(event.values[1]).equalsIgnoreCase("NaN")? 0:event.values[1];
            raw_accel[2] = String.valueOf(event.values[2]).equalsIgnoreCase("NaN")? 0:event.values[2];
            rawAccelMagnitude = Math.sqrt(Math.pow(raw_accel[0],2) + Math.pow(raw_accel[1],2) + Math.pow(raw_accel[2],2));
        }

        if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
            raw_gyro[0] = String.valueOf(event.values[0]).equalsIgnoreCase("NaN")? 0:event.values[0];
            raw_gyro[1] = String.valueOf(event.values[1]).equalsIgnoreCase("NaN")? 0:event.values[1];
            raw_gyro[2] = String.valueOf(event.values[2]).equalsIgnoreCase("NaN")? 0:event.values[2];
            rawOmegaMagnitude = Math.sqrt(Math.pow(raw_gyro[0],2) + Math.pow(raw_gyro[1],2) + Math.pow(raw_gyro[2],2));

            rawOmegaMagnitude = rawOmegaMagnitude == 0.0?1.0:rawOmegaMagnitude;

            normalized_gyro[0] = raw_gyro[0]/rawOmegaMagnitude;
            normalized_gyro[1] = raw_gyro[1]/rawOmegaMagnitude;
            normalized_gyro[2] = raw_gyro[2]/rawOmegaMagnitude;
        }

        // Calculate the angular speed of the sample


        String sensor_normalized = String.valueOf(speed + "," +raw_accel[0]) + "," + String.valueOf(raw_accel[1]) + "," + String.valueOf(raw_accel[2]) + "," + String.valueOf(rawAccelMagnitude)+
                ","+String.valueOf(normalized_gyro[0]) + "," + String.valueOf(normalized_gyro[1]) + "," + String.valueOf(normalized_gyro[2]) + "," + String.valueOf(rawOmegaMagnitude)+"\n";

        String log = String.valueOf(raw_accel[0]) + "\n" + String.valueOf(raw_accel[1]) + "\n" + String.valueOf(raw_accel[2]) + "\n" + String.valueOf(rawAccelMagnitude)+
                "\n"+String.valueOf(normalized_gyro[0]) + "\n" + String.valueOf(normalized_gyro[1]) + "\n" + String.valueOf(normalized_gyro[2]) + "\n" + String.valueOf(rawOmegaMagnitude);

        Log.d("Sensor Values",log );
        textView.setText(log);

        writeToFile(sensor_normalized,"sensor_data_"+speed);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onClick(View v) {

        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

        switch (v.getId()){
            case R.id.button1:
                if(!start){
                    mSensorManager.registerListener(this, mSensorLinearAcceleration, SensorManager.SENSOR_DELAY_NORMAL);
                    mSensorManager.registerListener(this, mSensorGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
                    speed = editText.getText().toString();
                    start = true;
                    button.setText("Stop");
                }else{
                    mSensorManager.unregisterListener(this);
                    start = false ;
                    button.setText("Start");
                }
                break;
        }
    }


    public void writeToFile(String data,String filename) {

        String path = Environment.getExternalStorageDirectory() + File.separator + "Treadmill";
        // Create the folder.
        File folder = new File(path);
        if (!folder.exists()) {
            // Make it, if it doesn't exit
            folder.mkdirs();
        }

        // Create the file.
        File file = new File(folder, filename+".csv");

        try {
            if(!file.exists()){
                file.createNewFile();
            }
            FileOutputStream fOut = new FileOutputStream(file,true);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(data);


            Log.d(TAG," Successful data write ");
            myOutWriter.close();

            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 12: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0   && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    button.setClickable(true);

                } else {

                    button.setClickable(false);

                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
