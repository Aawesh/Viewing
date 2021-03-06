package viewing.com.viewing;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
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
    private Sensor mSensor;

    double[] gravity = new double[3];
    double[] linear_acceleration = new double[3];
    double magnitude = 0;
    final double alpha = 0.8;
    boolean start = false;

    String filename;
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
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

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
        // In this example, alpha is calculated as t / (t + dT),
        // where t is the low-pass filter's time-constant and
        // dT is the event delivery rate.

        // Isolate the force of gravity with the low-pass filter.
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        // Remove the gravity contribution with the high-pass filter.
        linear_acceleration[0] = event.values[0] - gravity[0];
        linear_acceleration[1] = event.values[1] - gravity[1];
        linear_acceleration[2] = event.values[2] - gravity[2];

        magnitude = Math.sqrt(Math.pow(linear_acceleration[0],2) + Math.pow(linear_acceleration[1],2) + Math.pow(linear_acceleration[2],2));

        String msg = String.valueOf(linear_acceleration[0]) + "," + String.valueOf(linear_acceleration[1]) + "," + String.valueOf(linear_acceleration[2]) + "," + String.valueOf(magnitude)+"\n";
        String log = String.valueOf(linear_acceleration[0]) + "\n" + String.valueOf(linear_acceleration[1]) + "\n" + String.valueOf(linear_acceleration[2]) + "\n" + String.valueOf(magnitude);
        Log.d("Sensor Values x,y,z,m", log );
        textView.setText(log);

        writeToFile(msg,filename);
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
                    mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
                    filename = editText.getText().toString();
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

        String path = Environment.getExternalStorageDirectory() + File.separator + "Viewing";
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
