package nxp.activentag5i2c.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import nxp.activentag5i2c.R;

public class ValueAdjustActivity extends BaseActionBarActivity {

    public static final String PREFS_NAME = "MachineSettings";

    private TextView textCurrentValue;
    private TextView textSettingTitle;
    private Button buttonPlus;
    private Button buttonMinus;
    private Button buttonDone;

    private String settingMode;
    private String settingKey;
    private int currentValue;
    private int minValue;
    private int maxValue;
    private int step;
    private String title;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_value_adjust);

        // Get the mode from the intent
        settingMode = getIntent().getStringExtra("SETTING_MODE");
        if (settingMode == null) {
            Toast.makeText(this, "Error: No setting mode provided", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Configure activity based on the mode
        switch (settingMode) {
            case "WASH_TIME":
                title = "wash time";
                settingKey = "wash_time_value";
                minValue = 0;
                maxValue = 5;
                step = 1;
                currentValue = prefs.getInt(settingKey, 0); // Default 0
                break;
            case "RINSE_TIME":
                title = "rinse time";
                settingKey = "rinse_time_value";
                minValue = 0;
                maxValue = 5;
                step = 1;
                currentValue = prefs.getInt(settingKey, 0); // Default 0
                break;
            case "SPIN_SPEED":
                title = "spin speed";
                settingKey = "spin_speed_value";
                minValue = 700;
                maxValue = 900;
                step = 50;
                currentValue = prefs.getInt(settingKey, 700); // Default 700
                break;
            default:
                Toast.makeText(this, "Error: Unknown setting mode", Toast.LENGTH_LONG).show();
                finish();
                return;
        }

        // Find views
        textCurrentValue = findViewById(R.id.text_current_value);
        textSettingTitle = findViewById(R.id.text_setting_title);
        buttonPlus = findViewById(R.id.button_plus);
        buttonMinus = findViewById(R.id.button_minus);
        buttonDone = findViewById(R.id.button_done);

        // Set initial state
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title.toUpperCase());
        }
        textSettingTitle.setText(title);
        updateValueDisplay();

        // Set click listeners
        buttonPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentValue + step <= maxValue) {
                    currentValue += step;
                    updateValueDisplay();
                }
            }
        });

        buttonMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentValue - step >= minValue) {
                    currentValue -= step;
                    updateValueDisplay();
                }
            }
        });

        buttonDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save the value to SharedPreferences
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(settingKey, currentValue);
                editor.apply();

                Toast.makeText(ValueAdjustActivity.this, "Value saved: " + currentValue, Toast.LENGTH_SHORT).show();

                finish(); // Go back to the previous screen
            }
        });
    }

    private void updateValueDisplay() {
        textCurrentValue.setText(String.valueOf(currentValue));
    }
}