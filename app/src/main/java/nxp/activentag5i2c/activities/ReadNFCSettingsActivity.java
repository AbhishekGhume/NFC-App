package nxp.activentag5i2c.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mobileknowledge.library.utils.Utils;

import nxp.activentag5i2c.R;

public class ReadNFCSettingsActivity extends BaseActionBarActivity {

    private final String TAG = ReadNFCSettingsActivity.class.getSimpleName();
    private TextView textStatus;
    private Button buttonRetry;

    // SRAM Read command for Block 0 (Read 20 bytes = 5 blocks)
    private static final byte[] cmd_readSettingsBlocks = {
            (byte) 0x12,  // FLAGS
            (byte) 0xD2,  // READ I2C command
            (byte) 0x04,
            (byte) 0x00,  // Starting address (Block 0)
            (byte) 0x14   // Length: 20 bytes (5 blocks × 4 bytes)
    };

    // Settings constants - Block offsets
    private static final int WASH_TIME_OFFSET = 0;
    private static final int RINSE_TIME_OFFSET = 4;
    private static final int SPIN_SPEED_OFFSET = 8;
    private static final int EXTRA_RINSE_OFFSET = 12;
    private static final int TEMP_UNIT_OFFSET = 16;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_nfc_settings);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Read Machine Settings");
        }

        textStatus = findViewById(R.id.text_status);
        buttonRetry = findViewById(R.id.button_retry);

        textStatus.setText("Please tap NFC tag to read settings...");

        buttonRetry.setOnClickListener(v -> {
            textStatus.setText("Waiting for tag...");
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (tag.getTechList()[0].equals("android.nfc.tech.NfcV")) {
            Snackbar.make(findViewById(android.R.id.content),
                    "Tag detected! Reading settings...", Snackbar.LENGTH_SHORT).show();

            readSettingsFromTag();
        } else {
            Toast.makeText(this, "Tag not supported", Toast.LENGTH_SHORT).show();
        }
    }

    private void readSettingsFromTag() {
        try {
            // Send command to read 20 bytes (5 blocks)
            byte[] response = sendCommand(cmd_readSettingsBlocks);

            if (response != null && response.length >= 21 && response[0] == 0x00) {
                // Success! Parse the data
                parseAndSaveSettings(response);
            } else {
                String errorMsg = (response != null) ? Utils.byteArrayToHex(response) : "null";
                Log.e(TAG, "Read failed. Response: " + errorMsg);
                Toast.makeText(this, "Failed to read settings from tag", Toast.LENGTH_LONG).show();
                textStatus.setText("Error reading tag. Tap again to retry.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception during read", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            textStatus.setText("Error occurred. Tap again to retry.");
        }
    }

    private void parseAndSaveSettings(byte[] response) {
        try {
            // Response format: [Status, Data...]
            // Skip first byte (status = 0x00)
            byte[] data = new byte[response.length - 1];
            System.arraycopy(response, 1, data, 0, data.length);

            // Parse individual settings
            int washTime = data[WASH_TIME_OFFSET] & 0xFF;
            int rinseTime = data[RINSE_TIME_OFFSET] & 0xFF;

            // Spin speed is 2 bytes (Little Endian)
            int spinSpeed = ((data[SPIN_SPEED_OFFSET + 1] & 0xFF) << 8) |
                    (data[SPIN_SPEED_OFFSET] & 0xFF);

            // Extra Rinse: 0 = off, 1 = on
            int extraRinseValue = data[EXTRA_RINSE_OFFSET] & 0xFF;
            String extraRinse = (extraRinseValue == 1) ? "on" : "off";

            // Temperature Unit: 0 = Celsius, 1 = Fahrenheit
            int tempUnitValue = data[TEMP_UNIT_OFFSET] & 0xFF;
            String tempUnit = (tempUnitValue == 1) ? "F" : "C";

            // Validate ranges
            if (!isValidWashTime(washTime)) {
                Log.w(TAG, "Invalid wash time: " + washTime + ", using default 0");
                washTime = 0;
            }
            if (!isValidRinseTime(rinseTime)) {
                Log.w(TAG, "Invalid rinse time: " + rinseTime + ", using default 0");
                rinseTime = 0;
            }
            if (!isValidSpinSpeed(spinSpeed)) {
                Log.w(TAG, "Invalid spin speed: " + spinSpeed + ", using default 700");
                spinSpeed = 700;
            }

            // Save to SharedPreferences
            SharedPreferences prefs = getSharedPreferences("MachineSettings", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("wash_time_value", washTime);
            editor.putInt("rinse_time_value", rinseTime);
            editor.putInt("spin_speed_value", spinSpeed);
            editor.putString("extra_rinse_value", extraRinse);
            editor.putString("temp_unit_value", tempUnit);
            editor.apply();

            Log.d(TAG, "Settings saved - Wash: " + washTime + ", Rinse: " + rinseTime +
                    ", Spin: " + spinSpeed + ", Extra Rinse: " + extraRinse + ", Temp: " + tempUnit);

            // Display success message
            textStatus.setText("Settings loaded successfully!\n\n" +
                    "Wash Time: " + washTime + "\n" +
                    "Rinse Time: " + rinseTime + "\n" +
                    "Spin Speed: " + spinSpeed + "\n" +
                    "Extra Rinse: " + extraRinse.toUpperCase() + "\n" +
                    "Temp Unit: " + tempUnit + "\n\n" +
                    "Opening settings page...");

            // Launch SettingsActivity after a short delay
            findViewById(android.R.id.content).postDelayed(() -> {
                Intent intent = new Intent(ReadNFCSettingsActivity.this, SettingsActivity.class);
                startActivity(intent);
                finish();
            }, 1500);

        } catch (Exception e) {
            Log.e(TAG, "Error parsing settings", e);
            Toast.makeText(this, "Error parsing settings", Toast.LENGTH_LONG).show();
            textStatus.setText("Parse error. Tap again to retry.");
        }
    }

    private boolean isValidWashTime(int value) {
        return value >= 0 && value <= 5;
    }

    private boolean isValidRinseTime(int value) {
        return value >= 0 && value <= 5;
    }

    private boolean isValidSpinSpeed(int value) {
        return value >= 700 && value <= 900;
    }
}