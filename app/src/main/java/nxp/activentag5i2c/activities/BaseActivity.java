package nxp.activentag5i2c.activities;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences; // 1. ADD IMPORT
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.mobileknowledge.library.utils.Utils;

import java.io.IOException;

import nxp.activentag5i2c.R;
import nxp.activentag5i2c.nfc.RFCommands;

public class BaseActivity extends AppCompatActivity {
    private final String TAG = BaseActivity.class.getSimpleName();
    private static final int ENABLE_NFC_REQUEST_CODE = 0x11;

    private NfcAdapter mNfcAdapter;
    private static NfcV nfcvTag;
    Tag tag;

    private PendingIntent mPendingIntent;
    private IntentFilter[] writeTagFilters;
    private String[][] mTechLists;

    // 2. ADD COMMAND CONSTANTS FOR RE-AUTH
    private static final byte[] cmd_getRandomNumber_base = {0x12, (byte) 0xB2, 0x04};
    private static final byte CMD_SET_PASSWORD_base = (byte) 0xB3;
    private static final byte PWD_IDENTIFIER_WRITE_base = 0x02;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkNFC();
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        setNfcIntent();
    }


    private void setNfcIntent() {
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(
                getApplicationContext(), getClass())
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        writeTagFilters = new IntentFilter[]{tagDetected};
        mTechLists = new String[][]{new String[]{
                NfcV.class.getName()
        }};
    }

    @Override
    protected void onActivityResult(final int requestCode,
                                    final int resultCode, final Intent data) {
        if (requestCode == ENABLE_NFC_REQUEST_CODE) {
            mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
            if (!mNfcAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.nfc_not_enabled), Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void checkNFC() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC)) {
            mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
            if (mNfcAdapter != null && !mNfcAdapter.isEnabled()) {
                new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.dialog_nfc_not_enabled_title))
                        .setMessage(getResources().getString(R.string.dialog_nfc_not_enabled_msg))
                        .setPositiveButton(getResources().getString(R.string.dialog_nfc_not_enabled_positive_btn),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        startActivityForResult(new Intent(android.provider.Settings.ACTION_NFC_SETTINGS), ENABLE_NFC_REQUEST_CODE);
                                    }
                                })
                        .setNegativeButton(getResources().getString(R.string.dialog_nfc_not_enabled_negative_btn),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.nfc_not_enabled), Toast.LENGTH_LONG).show();
                                        finish();
                                    }
                                }).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.nfc_not_enabled), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    // (onResume method is unchanged)
    @Override
    protected void onResume() {
        super.onResume();
        if (mNfcAdapter != null) {
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, writeTagFilters, mTechLists);
        }
    }

    // (onPause method is unchanged)
    @Override
    protected void onPause() {
        super.onPause();
        if (mNfcAdapter != null)
            mNfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        // 3. super.onNewIntent()
        super.onNewIntent(intent);

        tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Log.d(TAG, "Card ID: " + Utils.byteArrayToHex(tag.getId()));

        String[] techList = tag.getTechList();

        if (techList[0].equals("android.nfc.tech.NfcV")) {
            byte[] tagUid = tag.getId();
            nfcvTag = NfcV.get(tag);
            byte[] select_command = RFCommands.cmd_select;
            System.arraycopy(tagUid, 0, select_command, 2, 8);

            if (nfcvTag != null) {
                try {
                    nfcvTag.connect();
                    byte[] select_respo = nfcvTag.transceive(select_command);
                    Log.d(TAG, "Select response: " + Utils.byteArrayToHex(select_respo));

                    // 4. THE RE-AUTHENTICATION CALL
                    if (reAuthenticate()) {
                        Log.d(TAG, "onNewIntent: Re-authentication successful.");
                    } else {
                        Log.w(TAG, "onNewIntent: Re-authentication failed or no password saved.");
                        // We don't show a toast here, as it would be annoying.
                        // If auth is needed, the command in PassThroughActivity will just fail.
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * This method sends RF commands to the connected NFC-V tag
     */
    byte[] sendCommand(byte[] command) {
        // 5. ADD A NULL CHECK FOR nfcvTag
        if (nfcvTag == null) {
            Log.e(TAG, "sendCommand: nfcvTag is null. No tag connected.");
            return null;
        }

        // 6. ADD CHECK FOR CONNECTION
        if (!nfcvTag.isConnected()) {
            Log.w(TAG, "sendCommand: Tag not connected. Attempting to reconnect...");
            try {
                nfcvTag.connect();
            } catch (IOException e) {
                Log.e(TAG, "sendCommand: Reconnect failed.", e);
                return null; // Reconnect failed
            }
        }

        byte[] response;
        try {
            response = nfcvTag.transceive(command);
            Log.d(TAG, "command response: " + Utils.byteArrayToHex(response));
        } catch (Exception e) {
            e.printStackTrace();
            response = null;
        }
        return response;
    }

    /**
     * Attempts to re-authenticate the NFC session using a saved password.
     * This is called every time a new tag is tapped.
     * @return true if authentication was successful, false otherwise.
     */
    private boolean reAuthenticate() {
        // 1. Get saved password
        SharedPreferences prefs = getSharedPreferences("NFC_AUTH", MODE_PRIVATE);
        String pwdHex = prefs.getString("password_hex", null);

        if (pwdHex == null) {
            Log.w(TAG, "reAuthenticate: No password saved. Cannot re-authenticate.");
            return false; // No password saved
        }

        byte[] yourPwdBytes = Utils.hexToByteArray(pwdHex);
        if (yourPwdBytes == null || yourPwdBytes.length != 4) {
            Log.e(TAG, "reAuthenticate: Saved password is corrupt.");
            return false;
        }
        Log.d(TAG, "reAuthenticate: Found password. Starting auth...");

        // --- NFC Step 1: GET RANDOM NUMBER ---
        byte[] response_RN = sendCommand(cmd_getRandomNumber_base);
        byte[] randomNum;

        if (response_RN != null && response_RN.length >= 3 && response_RN[0] == 0x00) {
            randomNum = new byte[]{response_RN[1], response_RN[2]};
            Log.d(TAG, "reAuthenticate: Got Random Number.");
        } else {
            Log.e(TAG, "reAuthenticate: GET RANDOM NUMBER failed.");
            return false;
        }

        // --- NFC Step 2: Calculate XOR_Password ---
        byte[] concatRn = {randomNum[0], randomNum[1], randomNum[0], randomNum[1]};
        byte[] xorPassword = new byte[4];
        for (int i = 0; i < 4; i++) {
            xorPassword[i] = (byte) (yourPwdBytes[i] ^ concatRn[i]);
        }
        Log.d(TAG, "reAuthenticate: Calculated XOR Password.");

        // --- NFC Step 3: SET PASSWORD ---
        byte[] cmd_setPassword = {
                0x12,
                CMD_SET_PASSWORD_base,
                0x04,
                PWD_IDENTIFIER_WRITE_base,
                xorPassword[0],
                xorPassword[1],
                xorPassword[2],
                xorPassword[3]
        };

        byte[] response_SP = sendCommand(cmd_setPassword);

        if (response_SP != null && response_SP.length >= 1 && response_SP[0] == 0x00) {
            Log.d(TAG, "reAuthenticate: SET PASSWORD Success.");
            return true; // SUCCESS
        } else {
            Log.e(TAG, "reAuthenticate: SET PASSWORD failed.");
            return false; // Auth failed
        }
    }
}