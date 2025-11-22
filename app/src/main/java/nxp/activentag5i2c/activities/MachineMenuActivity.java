package nxp.activentag5i2c.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import nxp.activentag5i2c.R;

public class MachineMenuActivity extends BaseActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machine_menu);

        // You can set a title if you want
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Machine Menu");
        }

        Button buttonMachineInfo = findViewById(R.id.button_machine_info);
        Button buttonMachineSetup = findViewById(R.id.button_machine_setup);
        Button buttonVendSetup = findViewById(R.id.button_vend_setup);
        Button buttonServiceMenu = findViewById(R.id.button_service_menu);

        // "machine info" button launches the SettingsActivity
        buttonMachineSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MachineMenuActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        // Other buttons as per your screenshot (currently show a placeholder message)
//        buttonMachineInfo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(MachineMenuActivity.this, "Machine Setup not implemented", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        buttonVendSetup.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(MachineMenuActivity.this, "Vend Setup not implemented", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        buttonServiceMenu.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(MachineMenuActivity.this, "Service Menu not implemented", Toast.LENGTH_SHORT).show();
//            }
//        });
    }
}