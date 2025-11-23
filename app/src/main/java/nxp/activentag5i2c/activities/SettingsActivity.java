package nxp.activentag5i2c.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mobileknowledge.library.utils.Utils;

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

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent); // This will connect and call reAuthenticate() from BaseActivity
        if (tag.getTechList()[0].equals("android.nfc.tech.NfcV")) {

            Toast.makeText(this, "NFC Tag Detected. Writing settings...", Toast.LENGTH_SHORT).show();
            writeSettingsToTag();
        }
    }

    /**
     * Command to write all 5 settings to tag (20 bytes at Block 0)
     */
    private byte[] buildWriteSettingsCommand(int washTime, int rinseTime, int spinSpeed,
                                             String extraRinse, String tempUnit) {
        byte[] cmd = new byte[25]; // 5 header + 20 data

        cmd[0] = (byte) 0x02;  // FLAGS
        cmd[1] = (byte) 0xD3;  // WRITE I2C command
        cmd[2] = (byte) 0x04;
        cmd[3] = (byte) 0x00;  // Starting block address (Block 0)
        cmd[4] = (byte) 0x14;  // Length: 20 bytes (5 blocks)

        // Block 0: Wash Time
        cmd[5] = (byte) (washTime & 0xFF);
        cmd[6] = 0x00;
        cmd[7] = 0x00;
        cmd[8] = 0x00;

        // Block 1: Rinse Time
        cmd[9] = (byte) (rinseTime & 0xFF);
        cmd[10] = 0x00;
        cmd[11] = 0x00;
        cmd[12] = 0x00;

        // Block 2: Spin Speed (Little Endian)
        cmd[13] = (byte) (spinSpeed & 0xFF);
        cmd[14] = (byte) ((spinSpeed >> 8) & 0xFF);
        cmd[15] = 0x00;
        cmd[16] = 0x00;

        // Block 3: Extra Rinse (0 = off, 1 = on)
        cmd[17] = (byte) (extraRinse.equals("on") ? 1 : 0);
        cmd[18] = 0x00;
        cmd[19] = 0x00;
        cmd[20] = 0x00;

        // Block 4: Temperature Unit (0 = C, 1 = F)
        cmd[21] = (byte) (tempUnit.equals("F") ? 1 : 0);
        cmd[22] = 0x00;
        cmd[23] = 0x00;
        cmd[24] = 0x00;

        return cmd;
    }

    /**
     * Write settings to NFC tag when user taps tag in SettingsActivity
     */
    private void writeSettingsToTag() {
        try {
            int washTime = prefs.getInt("wash_time_value", 0);
            int rinseTime = prefs.getInt("rinse_time_value", 0);
            int spinSpeed = prefs.getInt("spin_speed_value", 700);
            String extraRinse = prefs.getString("extra_rinse_value", "off");
            String tempUnit = prefs.getString("temp_unit_value", "C");

            byte[] writeCmd = buildWriteSettingsCommand(washTime, rinseTime, spinSpeed,
                    extraRinse, tempUnit);
            byte[] response = sendCommand(writeCmd);

            if (response != null && response.length >= 1 && response[0] == 0x00) {
                Toast.makeText(this, "Settings written to tag successfully!", Toast.LENGTH_SHORT).show();
//                Log.d(TAG, "Settings written to tag successfully");
            } else {
                String errorMsg = (response != null) ? Utils.byteArrayToHex(response) : "null";
//                Log.e(TAG, "Write failed. Response: " + errorMsg);
                Toast.makeText(this, "Failed to write settings to tag", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
//            Log.e(TAG, "Error writing settings", e);
            Toast.makeText(this, "Error writing settings: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Show toast if settings were just loaded from NFC
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean justLoaded = prefs.getBoolean("settings_just_loaded", false);

        if (justLoaded) {
            Toast.makeText(this, "Settings loaded from NFC tag", Toast.LENGTH_SHORT).show();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("settings_just_loaded", false);
            editor.apply();
        }
    }
}