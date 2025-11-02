package nxp.activentag5i2c.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mobileknowledge.library.utils.Utils;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import nxp.activentag5i2c.R;

public class ChangePasswordActivity extends BaseActionBarActivity {

    private final String TAG = ChangePasswordActivity.class.getSimpleName();

    private EditText editTextOldPassword;
    private EditText editTextNewPassword;
    private Button buttonChangePassword;

    // NFC Commands
    final byte[] cmd_getRandomNumber = {0x12, (byte) 0xB2, 0x04};
    final byte CMD_SET_PASSWORD = (byte) 0xB3;
    final byte CMD_WRITE_PASSWORD = (byte) 0xB4; // Command to write a new password
    final byte PWD_IDENTIFIER_WRITE = 0x02; // Identifier for the "Write" password

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        editTextOldPassword = findViewById(R.id.editText_old_password);
        editTextNewPassword = findViewById(R.id.editText_new_password);
        buttonChangePassword = findViewById(R.id.button_change_password);

        buttonChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This function is called when the tag is tapped (due to onNewIntent)
                // We just prompt the user to tap
                Toast.makeText(ChangePasswordActivity.this, "Please tap the tag to change password", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent); // This will connect and call reAuthenticate() from BaseActivity
        if (tag.getTechList()[0].equals("android.nfc.tech.NfcV")) {
            Snackbar.make(findViewById(android.R.id.content),
                    "NFC Tag Detected. Changing password...", Toast.LENGTH_SHORT).show();

            // Now that the tag is tapped, run the change password logic
            handleChangePassword();
        }
    }

    private void handleChangePassword() {
        String oldPasswordStr = editTextOldPassword.getText().toString();
        String newPasswordStr = editTextNewPassword.getText().toString();

        if (oldPasswordStr.isEmpty() || newPasswordStr.isEmpty()) {
            Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_LONG).show();
            return;
        }

        if (newPasswordStr.length() > 32) {
            Toast.makeText(this, "New password cannot be more than 32 characters", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            // --- 1. Generate 4-byte keys from user passwords ---
            byte[] oldPwdKey = generateNfcKey(oldPasswordStr);
            byte[] newPwdKey = generateNfcKey(newPasswordStr);

            if (oldPwdKey == null || newPwdKey == null) {
                Toast.makeText(this, "Password encoding error", Toast.LENGTH_LONG).show();
                return;
            }

            // --- 2. GET RANDOM NUMBER ---
            byte[] response_RN = sendCommand(cmd_getRandomNumber);
            byte[] randomNum;

            if (response_RN != null && response_RN.length >= 3 && response_RN[0] == 0x00) {
                randomNum = new byte[]{response_RN[1], response_RN[2]};
                Log.d(TAG, "NFC Step 1 Success. Random Number: " + Utils.byteArrayToHex(randomNum));
            } else {
                Log.e(TAG, "NFC Step 1 Failed. Response: " + (response_RN != null ? Utils.byteArrayToHex(response_RN) : "null"));
                Toast.makeText(this, "NFC Error: Could not get random number. Tap tag again.", Toast.LENGTH_LONG).show();
                return;
            }

            // --- 3. AUTHENTICATE WITH OLD PASSWORD (SET PASSWORD) ---
            byte[] concatRn = {randomNum[0], randomNum[1], randomNum[0], randomNum[1]};
            byte[] xorOldPassword = new byte[4];
            for (int i = 0; i < 4; i++) {
                xorOldPassword[i] = (byte) (oldPwdKey[i] ^ concatRn[i]);
            }

            byte[] cmd_setPassword = {
                    0x12,
                    CMD_SET_PASSWORD,
                    0x04,
                    PWD_IDENTIFIER_WRITE,
                    xorOldPassword[0],
                    xorOldPassword[1],
                    xorOldPassword[2],
                    xorOldPassword[3]
            };

            Log.d(TAG, "Sending SET PASSWORD (Auth) cmd: " + Utils.byteArrayToHex(cmd_setPassword));
            byte[] response_SP = sendCommand(cmd_setPassword);

            if (response_SP == null || response_SP.length < 1 || response_SP[0] != 0x00) {
                String errorHex = (response_SP != null) ? Utils.byteArrayToHex(response_SP) : "null";
                Log.e(TAG, "NFC Step 2 (Auth) Failed. Response: 0x" + errorHex);
                Toast.makeText(this, "Invalid Old Password.", Toast.LENGTH_LONG).show();
                return;
            }

            Log.d(TAG, "NFC Step 2 (Auth) Success.");

            // --- 4. WRITE NEW PASSWORD (WRITE PASSWORD) ---
            // This command (B4h) uses the *raw* 4-byte key, NOT the XORed one.
            byte[] cmd_writePassword = {
                    0x12,
                    CMD_WRITE_PASSWORD,
                    0x04,
                    PWD_IDENTIFIER_WRITE,
                    newPwdKey[0],
                    newPwdKey[1],
                    newPwdKey[2],
                    newPwdKey[3]
            };

            Log.d(TAG, "Sending WRITE PASSWORD cmd: " + Utils.byteArrayToHex(cmd_writePassword));
            byte[] response_WP = sendCommand(cmd_writePassword);

            if (response_WP != null && response_WP.length >= 1 && response_WP[0] == 0x00) {
                Log.d(TAG, "NFC Step 3 (Write) Success.");
                Toast.makeText(this, "Password Changed Successfully!", Toast.LENGTH_SHORT).show();

                // --- 5. UPDATE SharedPreferences with the NEW key ---
                SharedPreferences prefs = getSharedPreferences("NFC_AUTH", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("password_hex", Utils.byteArrayToHex(newPwdKey));
                editor.apply();
                Log.d(TAG, "New 4-byte key saved to SharedPreferences.");

                finish(); // Close this activity and go back to MainActivity

            } else {
                String errorHex = (response_WP != null) ? Utils.byteArrayToHex(response_WP) : "null";
                Log.e(TAG, "NFC Step 3 (Write) Failed. Response: 0x" + errorHex);
                Toast.makeText(this, "Failed to write new password.", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "handleChangePassword: Error", e);
            Toast.makeText(this, "An error occurred.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Converts a 1-32 character user password string into a 4-byte NFC key
     * using the same XOR-fold logic from LoginActivity.
     */
    private byte[] generateNfcKey(String password) {
        try {
            byte[] inputBytes = password.getBytes("UTF-8");
            byte[] paddedBytes = new byte[32];
            Arrays.fill(paddedBytes, (byte) 0x00);
            System.arraycopy(inputBytes, 0, paddedBytes, 0, inputBytes.length);

            byte[] nfcKey = new byte[4];
            for (int i = 0; i < 32; i++) {
                nfcKey[i % 4] ^= paddedBytes[i];
            }
            return nfcKey;
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "generateNfcKey: UTF-8 not supported", e);
            return null;
        }
    }
}
