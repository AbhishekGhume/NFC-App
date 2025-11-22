package nxp.activentag5i2c.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mobileknowledge.library.utils.Utils;

// 1. ADD THESE IMPORTS for the new password logic
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import nxp.activentag5i2c.R;

public class LoginActivity extends BaseActionBarActivity {

    private final String TAG = LoginActivity.class.getSimpleName();

    private EditText editTextPassword;

    final byte[] cmd_getRandomNumber = {0x12, (byte) 0xB2, 0x04};
    final byte CMD_SET_PASSWORD = (byte) 0xB3;
    final byte PWD_IDENTIFIER_WRITE = 0x02;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        editTextPassword = findViewById(R.id.editText_password);

        // 3. Update hint text to be more instructive
        editTextPassword.setHint("Enter Password & Tap Tag");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent); // This connects the tag and calls BaseActivity's onNewIntent
        if (tag.getTechList()[0].equals("android.nfc.tech.NfcV")) {

            // 4. CALL THE AUTHENTICATION LOGIC on NFC tap
            Log.d(TAG, "NFC Tag Detected. Attempting login...");
            validateCredentialsAndAuthenticateNFC();

        } else {
            // Handle non-NfcV tags if necessary
            Snackbar.make(findViewById(android.R.id.content),
                    "Tag not supported.", Toast.LENGTH_SHORT).show();
        }
    }

    public void validateCredentialsAndAuthenticateNFC() {
        String password = editTextPassword.getText().toString();

        // 5. USE NEW PASSWORD VALIDATION (1-32 chars)
        if (password.isEmpty()) {
            Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_LONG).show();
            return;
        }
        if (password.length() > 32) {
            Toast.makeText(this, "Password cannot be more than 32 characters", Toast.LENGTH_LONG).show();
            return;
        }

        // 6. GENERATE 4-BYTE KEY (same as ChangePasswordActivity)
        byte[] yourPwdBytes = generateNfcKey(password);
        if (yourPwdBytes == null) {
            Toast.makeText(this, "Password encoding error", Toast.LENGTH_LONG).show();
            return;
        }

        // --- NFC Step 1: GET RANDOM NUMBER ---
        byte[] response_RN = sendCommand(cmd_getRandomNumber);
        byte[] randomNum;

        if (response_RN != null && response_RN.length >= 3 && response_RN[0] == 0x00) {
            randomNum = new byte[]{response_RN[1], response_RN[2]};
            Log.d(TAG, "NFC Step 1 Success. Random Number: 0x" + Utils.byteArrayToHex(randomNum));
        } else {
            Log.e(TAG, "NFC Step 1 Failed. Response: " + (response_RN != null ? Utils.byteArrayToHex(response_RN) : "null"));
            Toast.makeText(this, "NFC Error: Could not get random number. Tap tag again.", Toast.LENGTH_LONG).show();
            return;
        }

        // --- NFC Step 2: Calculate XOR_Password ---
        byte[] concatRn = {randomNum[0], randomNum[1], randomNum[0], randomNum[1]};
        byte[] xorPassword = new byte[4];
        for (int i = 0; i < 4; i++) {
            xorPassword[i] = (byte) (yourPwdBytes[i] ^ concatRn[i]);
        }
        Log.d(TAG, "NFC Step 2 Success. XOR Password: 0x" + Utils.byteArrayToHex(xorPassword));

        // --- NFC Step 3: SET PASSWORD ---
        byte[] cmd_setPassword = {
                0x12,
                CMD_SET_PASSWORD,
                0x04,
                PWD_IDENTIFIER_WRITE,
                xorPassword[0],
                xorPassword[1],
                xorPassword[2],
                xorPassword[3]
        };

        Log.d(TAG, "Sending SET PASSWORD cmd: " + Utils.byteArrayToHex(cmd_setPassword));
        byte[] response_SP = sendCommand(cmd_setPassword);

        if (response_SP != null && response_SP.length >= 1 && response_SP[0] == 0x00) {
            Log.d(TAG, "NFC Step 3 Success. Response: 0x00");
            Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();

            // --- Save the successful 4-BYTE KEY to SharedPreferences ---
            SharedPreferences prefs = getSharedPreferences("NFC_AUTH", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            // Save the 4-byte key as a hex string
            editor.putString("password_hex", Utils.byteArrayToHex(yourPwdBytes));
            editor.apply();
            Log.d(TAG, "4-byte key saved to SharedPreferences.");

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();

        } else {
            String errorHex = (response_SP != null) ? Utils.byteArrayToHex(response_SP) : "null";
            Log.e(TAG, "NFC Step 3 Failed. Response: 0x" + errorHex);
            Toast.makeText(this, "Invalid Password.", Toast.LENGTH_LONG).show();
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