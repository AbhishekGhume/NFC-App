package nxp.activentag5i2c.activities;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import nxp.activentag5i2c.R;

public class ToggleActivity extends BaseActionBarActivity {

    // Using the same preferences file as ValueAdjustActivity
    public static final String PREFS_NAME = "MachineSettings";

    private TextView textSettingTitle;
    private Button buttonOption1;
    private Button buttonOption2;
    private Button buttonDone;

    private String settingMode;
    private String settingKey;
    private String option1Text;
    private String option2Text;
    private String title;
    private String selectedValue;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toggle);

        settingMode = getIntent().getStringExtra("SETTING_MODE");
        if (settingMode == null) {
            Toast.makeText(this, "Error: No setting mode provided", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Configure activity based on the mode
        switch (settingMode) {
            case "EXTRA_RINSE":
                title = "extra rinse";
                settingKey = "extra_rinse_value";
                option1Text = "on";
                option2Text = "off";
                selectedValue = prefs.getString(settingKey, "off"); // Default "off"
                break;
            case "TEMP_UNIT":
                title = "temp unit";
                settingKey = "temp_unit_value";
                option1Text = "C";
                option2Text = "F";
                selectedValue = prefs.getString(settingKey, "C"); // Default "C"
                break;
            default:
                Toast.makeText(this, "Error: Unknown setting mode", Toast.LENGTH_LONG).show();
                finish();
                return;
        }

        // Find views
        textSettingTitle = findViewById(R.id.text_setting_title);
        buttonOption1 = findViewById(R.id.button_option_1);
        buttonOption2 = findViewById(R.id.button_option_2);
        buttonDone = findViewById(R.id.button_done);

        // Set initial state
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title.toUpperCase());
        }
        textSettingTitle.setText(title);
        buttonOption1.setText(option1Text);
        buttonOption2.setText(option2Text);

        updateButtonHighlight();

        // Set click listeners
        buttonOption1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedValue = option1Text;
                updateButtonHighlight();
            }
        });

        buttonOption2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedValue = option2Text;
                updateButtonHighlight();
            }
        });

        buttonDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save the value to SharedPreferences
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(settingKey, selectedValue);
                editor.apply();

                // Here you would also add your NFC logic to write the value to the tag
                Toast.makeText(ToggleActivity.this, "Value saved: " + selectedValue, Toast.LENGTH_SHORT).show();

                finish(); // Go back to the previous screen
            }
        });
    }

    private void updateButtonHighlight() {
        if (selectedValue.equals(option1Text)) {
            buttonOption1.getBackground().setColorFilter(Color.CYAN, PorterDuff.Mode.MULTIPLY);
            buttonOption2.getBackground().clearColorFilter();
        } else {
            buttonOption2.getBackground().setColorFilter(Color.CYAN, PorterDuff.Mode.MULTIPLY);
            buttonOption1.getBackground().clearColorFilter();
        }
    }
}