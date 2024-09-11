package upsay.decouverteandroid.e_cockpit_dashboard;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// used for wainting second
import android.os.Handler;
import android.os.Looper;

public class MainActivity extends AppCompatActivity {

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
    //  setup is set

    ImageView imgBtIndicator = findViewById(R.id.imgBtIndicator);
    Button refreshButton = findViewById(R.id.bpRefresh);
    TextView txtEtatBt = findViewById(R.id.txtEtatBt);
    TextView txtAddressMac = findViewById(R.id.txtAddressMac);

    // check BT device and get Mac address
    refreshButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            refreshButton.setText("Searching ...");

            // delay the MAC address check and UI update by 1 second
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    String macAddress = Bluetooth.getConnectedDeviceMac();

                    if (macAddress.equals("No connected Bluetooth device found") || macAddress.equals("Bluetooth not enabled or not supported")) {
                        // Optional: You could reset the button text back to default here
                        refreshButton.setText("Not found");
                        imgBtIndicator.setImageResource(R.drawable.red_circle);
                        txtEtatBt.setText("Not connected");
                        txtAddressMac.setText("MAC Address: ");
                        refreshButton.setText("Refresh");
                        return;
                    } else {
                        imgBtIndicator.setImageResource(R.drawable.green_circle);
                        txtEtatBt.setText("Connected");
                        txtAddressMac.setText("MAC Address: " + macAddress);

                        refreshButton.setText("Found");
                    }
                    refreshButton.setText("Refresh");
                }
            }, 1000);  // Delay of 1 second
        }
    });

    }
}