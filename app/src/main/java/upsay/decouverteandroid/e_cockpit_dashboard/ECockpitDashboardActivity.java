package upsay.decouverteandroid.e_cockpit_dashboard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import java.io.IOException;

import android.util.Log;

import de.nitri.gauge.Gauge;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class ECockpitDashboardActivity extends AppCompatActivity {
    private Handler handler = new Handler(Looper.getMainLooper());

    private Bluetooth bluetooth;
    String macAddress;
    String deviceName;

    private OBDII obd2;
    public static boolean isRequesting = false;

    private Button bpGotoMain;
    private ImageButton ibPNightMode;
    private TextView txtRPM; // used for debug
    private TextView txtFuelLevel;
    private TextView txtAmbientAirTemp;
    private TextView txtEngineOilTemp;
    private TextView txtCoolantTemp;
    private TextView txtIntakeAirTemp;
    private TextView txtTemperature;
    private TextView txtCoolantTemperature;
    private ImageView led1;
    private ImageView led2;
    private ImageView led3;
    private ImageView led4;
    private ImageView led5;

    private int rpmRequestCount = 0;

    private Gauge gauge;

    private ProgressBar gaugeProgressBar;

    private MapView mapView;
    private GoogleMap googleMap;

    private Handler blinkHandler = new Handler();
    private Runnable blinkRunnable;
    private boolean isBlinking = false;

    private static final String DEFAULT_MODE = "ThemePrefs";
    private static final String NIGHT_MODE = "isNightMode";

    private boolean debugView = false; // true to debug

    private static final String TAG = "ECockpitDashboard";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_e_cockpit_dashboard);

        //updateFragmentBackground();

        // get bluetooth object from Main
        Intent intent = getIntent();
        bluetooth = (Bluetooth) intent.getSerializableExtra("BluetoothObject");

        if (bluetooth != null) {
            macAddress = bluetooth.getMacAddress();
            deviceName = bluetooth.getDeviceName();
            // Use macAddress and deviceName as needed
        } else {
            Log.e("ECockpitDashboard", "Bluetooth object is null!");
        }

        if (isNightModeEnabled()) {
            setTheme(R.style.AppTheme_Night);
        } else {
            setTheme(R.style.AppTheme);
        }

        Log.i(TAG, "MAC address is :" + macAddress);
        Log.i(TAG, "Device name is :" + deviceName);


        bpGotoMain = findViewById(R.id.bpGotoMain);
        ibPNightMode = findViewById(R.id.ibPNightMode);
        txtRPM = findViewById(R.id.txtRPM);
        txtFuelLevel = findViewById(R.id.txtFuelLevel);
        txtAmbientAirTemp = findViewById(R.id.txtAmbientAirTemp);
        txtEngineOilTemp = findViewById(R.id.txtEngineOilTemp);
        txtCoolantTemp = findViewById(R.id.txtCoolantTemp);
        txtIntakeAirTemp = findViewById(R.id.txtIntakeAirTemp);
        txtTemperature = findViewById(R.id.txtTemperature);
        txtCoolantTemperature = findViewById(R.id.txtCoolantTemperature);
        gaugeProgressBar = findViewById(R.id.progressBar);
        led1 = findViewById(R.id.led1);
        led2 = findViewById(R.id.led2);
        led3 = findViewById(R.id.led3);
        led4 = findViewById(R.id.led4);
        led5 = findViewById(R.id.led5);

        gauge = findViewById((R.id.gauge));
        int faceColor = ContextCompat.getColor(this, R.color.face);
        gauge.setDrawingCacheBackgroundColor(faceColor);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(googleMap -> {
            this.googleMap = googleMap;

            // set initial location
            LatLng initialLocation = new LatLng(48.8317, 2.3879);
            googleMap.addMarker(new MarkerOptions().position(initialLocation).title("Initial Location"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 10));
        });

        launchTest();


        // DEBBUGIN VEHICLE DATA ;
        setDebugView(false);

        // >>>> debug gauge > incrementation
        //testRPMGauge();
        //testLinearGauge();

        // try to connect to the OBDII device using the MAC address
        // DEGUG UNCOMMENT LINE :
        if (!MainActivity.emulatorMode) {
            if (!macAddress.equals("No connected Bluetooth device found") && !macAddress.equals("Bluetooth not enabled or not supported")) {
                try {
                    bluetooth.connect(macAddress);
                    obd2 = new OBDII(bluetooth, this); // pass Bluetooth and activity to OBDII constructor
                    Toast.makeText(this, "Connected to OBDII device", Toast.LENGTH_SHORT).show(); // message on screen

                    initializeOBDConnection();

                } catch (IOException e) {
                    e.printStackTrace();
                    txtRPM.setText("Error connecting to device");
                }
            } else {
                txtRPM.setText("Error MAC Address : " + macAddress);  // display error if no device found or Bluetooth issue
            }
        } else {
            Log.d(TAG, "Skipping Bluetooth connection logic as emulatorMod is true");
        }


        // stop RPM requests when bpGotoMain button is pressed and finish the activity
        bpGotoMain.setOnClickListener(v -> {
            stopRequestLoop();
            if (bluetooth != null) {
                try {
                    bluetooth.disconnect();
                    Toast.makeText(this, "OBDII device disconnected", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            finish();  // close the activity
        });


        ibPNightMode.setOnClickListener(view -> {
            toggleNightMode();
        });


    }


    private void initializeOBDConnection() {
        if (obd2 != null) {
            try {
                obd2.initializeConnection();
                startRequestLoop();
            } catch (InterruptedException | IOException e) {
                Log.e("ECockpitDashboard", "OBD-II initialization interrupted: " + e.getMessage());
            }
        } else {
            Log.e("ECockpitDashboard", "OBD-II device not initialized.");
        }
    }


    private void setDebugView(boolean debugView){
        if(debugView){
            txtFuelLevel.setVisibility(View.VISIBLE);
            txtAmbientAirTemp.setVisibility(View.VISIBLE);
            txtEngineOilTemp.setVisibility(View.VISIBLE);
            txtCoolantTemp.setVisibility(View.VISIBLE);
            txtIntakeAirTemp.setVisibility(View.VISIBLE);
            txtRPM.setVisibility(View.VISIBLE);
            bpGotoMain.setVisibility(View.VISIBLE);
        }
        else{
            txtFuelLevel.setVisibility(View.GONE);
            txtAmbientAirTemp.setVisibility(View.GONE);
            txtEngineOilTemp.setVisibility(View.GONE);
            txtCoolantTemp.setVisibility(View.GONE);
            txtIntakeAirTemp.setVisibility(View.GONE);
            txtRPM.setVisibility(View.GONE);
            bpGotoMain.setVisibility(View.GONE);
        }
    }


    // start a loop to request data every 200ms
    private void startRequestLoop() {
        isRequesting = true;

        Runnable requestTask = new Runnable() {
            @Override
            public void run() {
                if (isRequesting) {
                    try {
                        stopRequestLoop();
                        if (rpmRequestCount < 4) {
                            // request and process RPM data
                            //requestRPMData(); //old
                            obd2.rpmRequest();
                            Log.i(TAG, "Request ongoing... (RPM)");
                        } else {
                            // call alternate data requests after 4 RPM requests
                            switch (rpmRequestCount) {
                                case 4:
                                    //requestCoolantTempData(); // old
                                    obd2.requestCoolantTempData();
                                    Log.i(TAG, "Request ongoing... (Coolant Temperature)");
                                    break;
                                case 5:
                                    //requestIntakeAirTempData(); //old
                                    obd2.requestIntakeAirTempData();
                                    Log.i(TAG, "Request ongoing... (Intake Air Temperature)");
                                    break;
                            }
                        }
                        // increment or reset the RPM counter
                        rpmRequestCount = (rpmRequestCount + 1) % 6; // 4 RPM requests + 2 other data requests

                        // add a delay before the next request
                        handler.postDelayed(this, 200); // between 200 to 300ms
                    } catch (Exception e) {
                        // log the error and display a message on the UI
                        Log.e(TAG, "Error during data request", e);
                        runOnUiThread(() -> txtRPM.setText("Error retrieving data"));

                        // optionally, stop the loop on failure
                        stopRequestLoop();
                    }
                }
            }
        };

        // Start the task for the first time
        handler.post(requestTask);
    }


    // stop the loop for requesting RPM data
    private void stopRequestLoop() {
        if(!isRequesting){
            handler.removeCallbacksAndMessages(null);  // Stop all callbacks
        }
    }


    // check if works
    public void setRPM(int rpm){
        gauge.moveToValue(rpm / 100);  // smooth
        //gauge.setValue(Float.parseFloat(rpm/100));
        gauge.setLowerText(String.valueOf(rpm));
        txtRPM.setText("RPM: " + rpm);
        rpmLED(rpm);
        Log.i(TAG, "Gauge RPM");
    }


    // check if works
    public void setCoolantTemp(int coolantTemp) {
        txtCoolantTemperature.setText(coolantTemp + " °C");
        gaugeProgressBar.setProgress(coolantTemp);
    }


    // check if works
    public void setIntakeAirTemp(int IntakeAirTemp) {
        txtTemperature.setText(IntakeAirTemp + " °C");
    }


    private void testLinearGauge() {
        // create a new thread for simulating temperature changes
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int temp = 50; temp <= 120; temp += 10) {
                    // Capture the temp value for use inside the inner Runnable
                    final int finalTemp = temp;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Update your progress bar with the current temperature value
                            Log.w(TAG, "Setting progress to " + finalTemp);
                            txtCoolantTemperature.setText(finalTemp + " °C");  // Update text

                            gaugeProgressBar.setProgress(finalTemp);
                        }
                    });

                    // Sleep for 500 milliseconds to simulate a delay
                    try {
                        Thread.sleep(500); // 500 ms delay
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }


    private void testRPMGauge() {
        // create a new thread for simulating temperature changes
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int rpm = 0; rpm <= 8000; rpm += 500) {
                    // Capture the temp value for use inside the inner Runnable
                    final int finalRPM = rpm;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Update your progress bar with the current temperature value
                            Log.w(TAG, "Setting progress to " + finalRPM);

                            gauge.moveToValue(finalRPM);  // smooth
                            //gauge.setValue(Float.parseFloat(rpm));
                            gauge.setLowerText(String.valueOf(finalRPM));
                        }
                    });

                    // Sleep for 500 milliseconds to simulate a delay
                    try {
                        Thread.sleep(500); // 500 ms delay
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }



    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }


    private void launchTest(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int rpm = 0; rpm <= 8000; rpm += 250) {
                    // capture the temp value for use inside the inner Runnable
                    final int finalRPM = rpm;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.w(TAG, "Setting progress to " + finalRPM);
                            setRPM(finalRPM);
                        }
                    });

                    // sleep to simulate a delay
                    try {
                        Thread.sleep(150); // 500 ms delay
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int temp = 50; temp <= 130; temp += 2) {
                    // Capture the temp value for use inside the inner Runnable
                    final int finalTemp = temp;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Update your progress bar with the current temperature value
                            Log.w(TAG, "Setting progress to " + finalTemp);
                            setCoolantTemp(finalTemp);
                        }
                    });

                    // Sleep for 500 milliseconds to simulate a delay
                    try {
                        Thread.sleep(100); // 500 ms delay
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }


    private void rpmLED(int rpm){
        if (isBlinking && rpm <= 6000) {
            stopBlinking();
        }
        // reset all LEDs to gray as default
        led1.setImageResource(R.drawable.gray_circle);
        led2.setImageResource(R.drawable.gray_circle);
        led3.setImageResource(R.drawable.gray_circle);
        led4.setImageResource(R.drawable.gray_circle);
        led5.setImageResource(R.drawable.gray_circle);

        // set LED colors based on RPM thresholds
        if (rpm > 6000) {
            startBlinking();
        } else if (rpm > 5500) {
            led1.setImageResource(R.drawable.green_circle);
            led2.setImageResource(R.drawable.green_circle);
            led3.setImageResource(R.drawable.orange_circle);
            led4.setImageResource(R.drawable.red_circle);
            led5.setImageResource(R.drawable.red_circle);
        } else if (rpm > 5000) {
            led1.setImageResource(R.drawable.green_circle);
            led2.setImageResource(R.drawable.green_circle);
            led3.setImageResource(R.drawable.orange_circle);
            led4.setImageResource(R.drawable.red_circle);
        } else if (rpm > 4500) {
            led1.setImageResource(R.drawable.green_circle);
            led2.setImageResource(R.drawable.green_circle);
            led3.setImageResource(R.drawable.orange_circle);
        } else if (rpm > 4000) {
            led1.setImageResource(R.drawable.green_circle);
            led2.setImageResource(R.drawable.green_circle);
        } else if (rpm > 3600) {
            led1.setImageResource(R.drawable.green_circle);
        }
    }


    private void startBlinking() {
        if (isBlinking) return;

        isBlinking = true;
        blinkRunnable = new Runnable() {
            private boolean isBlue = true;

            @Override
            public void run() {
                int color = isBlue ? R.drawable.blue_circle : R.drawable.gray_circle;
                led1.setImageResource(color);
                led2.setImageResource(color);
                led3.setImageResource(color);
                led4.setImageResource(color);
                led5.setImageResource(color);
                isBlue = !isBlue;
                blinkHandler.postDelayed(this, 300); // Delay for blinking
            }
        };
        blinkHandler.post(blinkRunnable);
    }


    private void stopBlinking() {
        if (isBlinking && blinkRunnable != null) {
            blinkHandler.removeCallbacks(blinkRunnable);
            isBlinking = false;
        }
    }


    private boolean isNightModeEnabled() {
        SharedPreferences preferences = getSharedPreferences(DEFAULT_MODE, MODE_PRIVATE);

        return preferences.getBoolean(NIGHT_MODE, false);
    }

    private void toggleNightMode() {
        boolean isNightMode = isNightModeEnabled();

        // save the new preference
        SharedPreferences.Editor editor = getSharedPreferences(DEFAULT_MODE, MODE_PRIVATE).edit();
        editor.putBoolean(NIGHT_MODE, !isNightMode);
        editor.apply();

        // update the fragment's background dynamically
        updateFragmentBackground();
    }


    private void updateFragmentBackground() {
        boolean isNightMode = isNightModeEnabled();
        ConstraintLayout fragmentRootView = findViewById(R.id.fragmentRootView);

        if (fragmentRootView != null) {
            int color = isNightMode ? Color.BLACK : Color.WHITE;
            fragmentRootView.setBackgroundColor(color);
        }

        if (ibPNightMode != null) {
            if (isNightMode) {
                ibPNightMode.setImageResource(R.drawable.moon);
            } else {
                ibPNightMode.setImageResource(R.drawable.sun);
            }
        }
    }









}

