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

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import nxp.activentag5i2c.R;

public class LoginActivity extends BaseActivity {

    private final String TAG = LoginActivity.class.getSimpleName();

    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button buttonLogin;

    // Hardcoded username
    private static final String VALID_USERNAME = "GHUMEAJ";
    private static final String DEFAULT_PASSWORD = "1234";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextUsername = findViewById(R.id.editText_username);
        editTextPassword = findViewById(R.id.editText_password);
        buttonLogin = findViewById(R.id.button_login);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });
    }

    private void performLogin() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString();

        // Validate inputs
        if (username.isEmpty()) {
            Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate username
        if (!username.equals(VALID_USERNAME)) {
            Toast.makeText(this, "Invalid Username", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the stored password from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("NFC_AUTH", MODE_PRIVATE);
        String storedPassword = prefs.getString("password_plain", DEFAULT_PASSWORD);

        // Validate password
        if (!password.equals(storedPassword)) {
            Toast.makeText(this, "Invalid Password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Credentials are valid - generate and save the 4-byte NFC key
        byte[] nfcKey = generateNfcKey(password);
        if (nfcKey == null) {
            Toast.makeText(this, "Password encoding error", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save the 4-byte key to SharedPreferences for NFC operations
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("password_hex", Utils.byteArrayToHex(nfcKey));
        editor.apply();

        Log.d(TAG, "Login successful. 4-byte NFC key saved to SharedPreferences.");
        Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();

        // Navigate to MainActivity
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Converts a password string into a 4-byte NFC key using XOR-fold logic.
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

    @Override
    protected void onNewIntent(Intent intent) {
        // Override to do nothing - we don't need NFC tap for login anymore
        super.onNewIntent(intent);
    }
}