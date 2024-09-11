package upsay.decouverteandroid.e_cockpit_dashboard;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

    // Initialize the ImageView and Button
    ImageView imgBtIndicator = findViewById(R.id.imgBtIndicator);
    Button circleButton = findViewById(R.id.bpTest);
    TextView txtEtatBt = findViewById(R.id.txtEtatBt);

    // device connected status
    circleButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            imgBtIndicator.setImageResource(R.drawable.green_circle);
            txtEtatBt.setText("Connected");
        }
    });

    }
}