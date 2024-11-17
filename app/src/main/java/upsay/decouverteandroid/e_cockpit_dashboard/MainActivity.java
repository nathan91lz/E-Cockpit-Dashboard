package upsay.decouverteandroid.e_cockpit_dashboard;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// used for wainting second
import android.os.Handler;
import android.os.Looper;

// used for bluetooth permisions from manifest.xml
import android.Manifest;



public class MainActivity extends AppCompatActivity {
    Bluetooth bluetooth = new Bluetooth();

    private Button gotoECockpitFragment;
    private Button gotoBluetoothDeviceListFragment;

    public String macAddress = bluetooth.getMacAddress();
    public String deviceName = bluetooth.getDeviceName();
    public final String OBDIIPaired = "raspberrypi"; // DEGUG
//    public final String OBDIIPaired = "OBDII";

    public static boolean emulatorMode = false;

    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView imgBtIndicator = findViewById(R.id.imgBtIndicator);
        Button refreshButton = findViewById(R.id.bpRefresh);
        Button getOBDPaired = findViewById(R.id.bpGetOBDPaired);
        TextView txtEtatBt = findViewById(R.id.txtEtatBt);
        TextView txtAddressMac = findViewById(R.id.txtAddressMac);
        TextView txtAddressMacOwn = findViewById(R.id.txtAddressMacOwn);
        gotoECockpitFragment = findViewById(R.id.bpGotoCockpit);
        gotoBluetoothDeviceListFragment = findViewById(R.id.bpGotoBluetoothDevice);

        // DEBUG :
//        Log.i(TAG, "mac adress : " + bluetooth.getMacAddress());
//        Log.i(TAG, "device name: " + bluetooth.getDeviceName());


        // BLUETOOTH EMULATOR :
        boolean isEmulator = Build.FINGERPRINT.contains("generic");

        // check bluetooth permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkBluetoothPermissions();
        }

        if (isEmulator) {
            emulatorMode = true;
            refreshButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    refreshButton.setText("Searching ...");

                    // Delay the MAC address check and UI update by 1 second
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            macAddress = bluetooth.getMacAddress();
                            deviceName = bluetooth.getDeviceName();
                            if (macAddress.equals("No connected Bluetooth device found") || macAddress.equals("Bluetooth not enabled or not supported")) {
                                refreshButton.setText("Not found");
                                imgBtIndicator.setImageResource(R.drawable.red_circle);
                                txtEtatBt.setText("Not paired");
                                txtAddressMac.setText("MAC Address: ");
                                refreshButton.setText("Refresh");

                                gotoECockpitFragment.setVisibility(View.GONE);
                            } else {
                                bluetooth.getOBDPairedDeviceMacAddress(OBDIIPaired);
                                imgBtIndicator.setImageResource(R.drawable.green_circle);
                                txtEtatBt.setText("Paired (Emulator)");
                                txtAddressMac.setText("MAC Address: " + macAddress + "\nDevice Name: " + deviceName);

                                refreshButton.setText("Found");

                                gotoECockpitFragment.setVisibility(View.VISIBLE);
                            }
                            refreshButton.setText("Refresh");
                        }
                    }, 200);  // delay of 1 second
                }
            });

            // used to bypass main fragment
//            Intent intent = new Intent(MainActivity.this, ECockpitDashboardActivity.class);
//            startActivity(intent);

        } else if (!emulatorMode){
            // used to bypass main fragment /!\
            // Auto run :
//            Intent intent = new Intent(MainActivity.this, ECockpitDashboardActivity.class);
//            startActivity(intent);

            // If not on an emulator, check Bluetooth state
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            // Check BT device and get Mac address
            refreshButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    refreshButton.setText("Searching ...");

                    // Delay the MAC address check and UI update by 1 second
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            macAddress = bluetooth.getMacAddress();
                            deviceName = bluetooth.getDeviceName();
                            if (macAddress.equals("No connected Bluetooth device found") || macAddress.equals("Bluetooth not enabled or not supported")) {
                                refreshButton.setText("Not found");
                                imgBtIndicator.setImageResource(R.drawable.red_circle);
                                txtEtatBt.setText("Not paired");
                                txtAddressMac.setText("MAC Address: ");
                                refreshButton.setText("Refresh");

                                gotoECockpitFragment.setVisibility(View.GONE);
                            } else {
                                imgBtIndicator.setImageResource(R.drawable.green_circle);
                                txtEtatBt.setText("Paired");
                                txtAddressMac.setText("MAC Address: " + macAddress + "\nDevice Name: " + deviceName);

                                refreshButton.setText("Found");

                                gotoECockpitFragment.setVisibility(View.VISIBLE);
                            }
                            refreshButton.setText("Refresh");
                        }
                    }, 200);
                }
            });
        }



        // go to fragment page: e-cockpit
        gotoECockpitFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "gotoECockpitFragment Clicked");
                Intent intent = new Intent(MainActivity.this, ECockpitDashboardActivity.class);
                intent.putExtra("BluetoothObject", bluetooth); // pass the Bluetooth object to dashboard
                startActivity(intent);
            }
        });

        // go to fragment page: e-cockpit
        gotoBluetoothDeviceListFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "gotoBluetoothDeviceListFragment Clicked");
                Intent intent = new Intent(MainActivity.this, BluetoothDeviceListActivity.class);
                startActivity(intent);
            }
        });

        // initial check to set the visibility of the button on activity start
        checkDeviceConnection(bluetooth.getMacAddress());

        getOBDPaired.setOnClickListener(v -> {
            macAddress = bluetooth.getOBDPairedDeviceMacAddress(OBDIIPaired); // get the MAC address of the paired device
            deviceName = bluetooth.getConnectedDeviceNameFromMacAddress(macAddress);

            Log.i(TAG, "mac adress : " + macAddress);
            Log.i(TAG, "device name: " + deviceName);

            if (macAddress.equals("No connected Bluetooth device found") || macAddress.equals("Bluetooth not enabled or not supported")) {
                refreshButton.setText("Not found");
                imgBtIndicator.setImageResource(R.drawable.red_circle);
                txtEtatBt.setText("Not paired");
                txtAddressMac.setText("MAC Address: ");
                refreshButton.setText("Refresh");
                gotoECockpitFragment.setVisibility(View.GONE);
            } else {
                imgBtIndicator.setImageResource(R.drawable.green_circle);
                txtEtatBt.setText("Paired");
                txtAddressMac.setText("MAC Address: " + macAddress + "\nDevice Name: " + deviceName);
                refreshButton.setText("Found");
                gotoECockpitFragment.setVisibility(View.VISIBLE);
            }
        });


        // delay for a short period before checking Bluetooth state
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            macAddress = bluetooth.getMacAddress();
            deviceName = bluetooth.getDeviceName();
            // check if a valid Bluetooth MAC address is available
            if (macAddress.equals("No connected Bluetooth device found") || macAddress.equals("Bluetooth not enabled or not supported")) {
                refreshButton.setText("Not found");
                imgBtIndicator.setImageResource(R.drawable.red_circle);
                txtEtatBt.setText("Not paired");
                txtAddressMac.setText("MAC Address: ");
                refreshButton.setText("Refresh");
                gotoECockpitFragment.setVisibility(View.GONE);
            } else {
                imgBtIndicator.setImageResource(R.drawable.green_circle);
                txtEtatBt.setText("Paired");
                txtAddressMac.setText("MAC Address: " + macAddress + "\nDevice Name: " + deviceName);
                refreshButton.setText("Found");
                gotoECockpitFragment.setVisibility(View.VISIBLE);
            }
            Log.i(TAG, "mac adress : " + macAddress);
            Log.i(TAG, "device name: " + deviceName);
        }, 100);

    }

    private void checkDeviceConnection(String macAddress) {
        if (macAddress.equals("No connected Bluetooth device found") || macAddress.equals("Bluetooth not enabled or not supported")) {
            gotoECockpitFragment.setVisibility(View.GONE);
        } else if (macAddress.equals("00:11:22:33:44:55")) {
            gotoECockpitFragment.setVisibility(View.VISIBLE);
        } else {
            gotoECockpitFragment.setVisibility(View.VISIBLE);
        }
    }


    private void checkBluetoothPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN},
                    REQUEST_BLUETOOTH_PERMISSIONS);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
        }
    }




}