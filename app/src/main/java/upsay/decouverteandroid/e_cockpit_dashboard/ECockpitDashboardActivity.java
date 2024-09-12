package upsay.decouverteandroid.e_cockpit_dashboard;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

public class ECockpitDashboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_e_cockpit_dashboard);

        setContentView(R.layout.activity_e_cockpit_dashboard);

        Button cancelButton = findViewById(R.id.bpGotoMain);
        cancelButton.setOnClickListener(v -> finish());

    }
}

