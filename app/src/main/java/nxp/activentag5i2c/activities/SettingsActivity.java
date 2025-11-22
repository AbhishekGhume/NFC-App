package nxp.activentag5i2c.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import nxp.activentag5i2c.R;

public class SettingsActivity extends BaseActionBarActivity {

    public static final String PREFS_NAME = "MachineSettings";
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    // UI Elements
    private TextView textWashValue, textRinseValue, textSpinValue;
    private Button btnWashMinus, btnWashPlus, btnRinseMinus, btnRinsePlus, btnSpinMinus, btnSpinPlus;
    private RadioGroup rgExtraRinse, rgTempUnit;
    private RadioButton rbRinseOn, rbRinseOff, rbTempC, rbTempF;
    private Button btnChangePassword;

    // Value holders
    private int washTime, rinseTime, spinSpeed;
    private final int WASH_MIN = 0, WASH_MAX = 5, WASH_STEP = 1;
    private final int RINSE_MIN = 0, RINSE_MAX = 5, RINSE_STEP = 1;
    private final int SPIN_MIN = 700, SPIN_MAX = 900, SPIN_STEP = 50;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide(); // Hide action bar like ChangePasswordActivity
        }

        // Init SharedPreferences
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        editor = prefs.edit();

        // Find all views
        findViews();

        // Load saved values
        loadValues();

        // Setup listeners
        setupClickListeners();
        setupToggleListeners();
    }

    private void findViews() {
        // Wash Time
        textWashValue = findViewById(R.id.text_wash_value);
        btnWashMinus = findViewById(R.id.btn_wash_minus);
        btnWashPlus = findViewById(R.id.btn_wash_plus);

        // Rinse Time
        textRinseValue = findViewById(R.id.text_rinse_value);
        btnRinseMinus = findViewById(R.id.btn_rinse_minus);
        btnRinsePlus = findViewById(R.id.btn_rinse_plus);

        // Spin Speed
        textSpinValue = findViewById(R.id.text_spin_value);
        btnSpinMinus = findViewById(R.id.btn_spin_minus);
        btnSpinPlus = findViewById(R.id.btn_spin_plus);

        // Extra Rinse
        rgExtraRinse = findViewById(R.id.rg_extra_rinse);
        rbRinseOn = findViewById(R.id.rb_rinse_on);
        rbRinseOff = findViewById(R.id.rb_rinse_off);

        // Temp Unit
        rgTempUnit = findViewById(R.id.rg_temp_unit);
        rbTempC = findViewById(R.id.rb_temp_c);
        rbTempF = findViewById(R.id.rb_temp_f);

        // Change Password
        btnChangePassword = findViewById(R.id.button_change_password);
    }

    private void loadValues() {
        // Load numeric values
        washTime = prefs.getInt("wash_time_value", 0);
        rinseTime = prefs.getInt("rinse_time_value", 0);
        spinSpeed = prefs.getInt("spin_speed_value", 700);

        updateValueDisplays();

        // Load toggle values
        String extraRinse = prefs.getString("extra_rinse_value", "off");
        if (extraRinse.equals("on")) {
            rbRinseOn.setChecked(true);
        } else {
            rbRinseOff.setChecked(true);
        }

        String tempUnit = prefs.getString("temp_unit_value", "C");
        if (tempUnit.equals("F")) {
            rbTempF.setChecked(true);
        } else {
            rbTempC.setChecked(true);
        }
    }

    private void updateValueDisplays() {
        textWashValue.setText(String.valueOf(washTime));
        textRinseValue.setText(String.valueOf(rinseTime));
        textSpinValue.setText(String.valueOf(spinSpeed));
    }

    private void setupClickListeners() {
        // --- Wash Time ---
        btnWashPlus.setOnClickListener(v -> {
            if (washTime + WASH_STEP <= WASH_MAX) {
                washTime += WASH_STEP;
                textWashValue.setText(String.valueOf(washTime));
                editor.putInt("wash_time_value", washTime).apply();
            }
        });
        btnWashMinus.setOnClickListener(v -> {
            if (washTime - WASH_STEP >= WASH_MIN) {
                washTime -= WASH_STEP;
                textWashValue.setText(String.valueOf(washTime));
                editor.putInt("wash_time_value", washTime).apply();
            }
        });

        // --- Rinse Time ---
        btnRinsePlus.setOnClickListener(v -> {
            if (rinseTime + RINSE_STEP <= RINSE_MAX) {
                rinseTime += RINSE_STEP;
                textRinseValue.setText(String.valueOf(rinseTime));
                editor.putInt("rinse_time_value", rinseTime).apply();
            }
        });
        btnRinseMinus.setOnClickListener(v -> {
            if (rinseTime - RINSE_STEP >= RINSE_MIN) {
                rinseTime -= RINSE_STEP;
                textRinseValue.setText(String.valueOf(rinseTime));
                editor.putInt("rinse_time_value", rinseTime).apply();
            }
        });

        // --- Spin Speed ---
        btnSpinPlus.setOnClickListener(v -> {
            if (spinSpeed + SPIN_STEP <= SPIN_MAX) {
                spinSpeed += SPIN_STEP;
                textSpinValue.setText(String.valueOf(spinSpeed));
                editor.putInt("spin_speed_value", spinSpeed).apply();
            }
        });
        btnSpinMinus.setOnClickListener(v -> {
            if (spinSpeed - SPIN_STEP >= SPIN_MIN) {
                spinSpeed -= SPIN_STEP;
                textSpinValue.setText(String.valueOf(spinSpeed));
                editor.putInt("spin_speed_value", spinSpeed).apply();
            }
        });

        // --- Change Password ---
        btnChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });
    }

    private void setupToggleListeners() {
        rgExtraRinse.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_rinse_on) {
                editor.putString("extra_rinse_value", "on").apply();
            } else {
                editor.putString("extra_rinse_value", "off").apply();
            }
        });

        rgTempUnit.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_temp_c) {
                editor.putString("temp_unit_value", "C").apply();
            } else {
                editor.putString("temp_unit_value", "F").apply();
            }
        });
    }

    // You can add your onNewIntent logic here to write all settings
    // to the NFC tag when it is tapped.
    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent); // This will connect and call reAuthenticate() from BaseActivity
        if (tag.getTechList()[0].equals("android.nfc.tech.NfcV")) {
            // A tag has been tapped!
            // This is where you would get all values from SharedPreferences
            // and write them to the tag using sendCommand()

            // Example:
            int currentWashTime = prefs.getInt("wash_time_value", 0);
            int currentSpinSpeed = prefs.getInt("spin_speed_value", 700);
            String currentTempUnit = prefs.getString("temp_unit_value", "C");

            // ... build your NFC command byte array ...
            // byte[] writeSettingsCommand = ...
            // sendCommand(writeSettingsCommand);

            Toast.makeText(this, "NFC Tag Detected. Writing settings...", Toast.LENGTH_SHORT).show();
        }
    }
}