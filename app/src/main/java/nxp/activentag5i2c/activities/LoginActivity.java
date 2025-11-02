package nxp.activentag5i2c.activities;

import android.content.Intent;
import android.content.SharedPreferences; // 1. ADD IMPORT
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mobileknowledge.library.utils.Utils;

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
        Button buttonLogin = findViewById(R.id.button_login);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateCredentialsAndAuthenticateNFC();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (tag.getTechList()[0].equals("android.nfc.tech.NfcV")) {
            Snackbar.make(findViewById(android.R.id.content),
                    "NFC Tag Detected. Ready to Authenticate.", Toast.LENGTH_SHORT).show();
        }
    }

    public void validateCredentialsAndAuthenticateNFC() {
        String password = editTextPassword.getText().toString();

        if (password.length() != 4) {
            Toast.makeText(LoginActivity.this, "Password must be exactly 4 characters.", Toast.LENGTH_LONG).show();
            return;
        }

        // This is the password we will try to use
        byte[] yourPwdBytes = password.getBytes();

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

            // --- 2. ADD THIS SECTION ---
            // Save the *successful* password to SharedPreferences
            SharedPreferences prefs = getSharedPreferences("NFC_AUTH", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            // Save the password as a hex string so we can retrieve it easily
            editor.putString("password_hex", Utils.byteArrayToHex(yourPwdBytes));
            editor.apply();
            Log.d(TAG, "Password saved to SharedPreferences.");
            // --- END OF ADDITION ---

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();

        } else {
            String errorHex = (response_SP != null) ? Utils.byteArrayToHex(response_SP) : "null";
            Log.e(TAG, "NFC Step 3 Failed. Response: 0x" + errorHex);
            Toast.makeText(this, "Invalid Password.", Toast.LENGTH_LONG).show();
        }
    }
}