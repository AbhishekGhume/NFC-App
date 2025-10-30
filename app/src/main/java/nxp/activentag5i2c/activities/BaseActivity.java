package nxp.activentag5i2c.activities;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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

//BaseActivity only contains the NFC communication
public class BaseActivity extends AppCompatActivity {
    private final String TAG = BaseActivity.class.getSimpleName();
    private static final int ENABLE_NFC_REQUEST_CODE = 0x11;

    private NfcAdapter mNfcAdapter;
    private static NfcV nfcvTag;
    Tag tag;

    private PendingIntent mPendingIntent;
    private IntentFilter[] writeTagFilters;
    private String[][] mTechLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkNFC();

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        setNfcIntent();
    }

    private void setNfcIntent() {
        // Create a generic PendingIntent that will be delivered to this activity. The NFC stack will fill
        // in the intent with the details of the discovered tag before delivering it to this activity.
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

    /**
     * Check the availability of NFC and BLE interfaces and let the user enable them
     * if not active during the activity creation
     */
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

    @Override
    protected void onResume() {
        super.onResume();
        if (mNfcAdapter != null) {
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, writeTagFilters, mTechLists);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mNfcAdapter != null)
            mNfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);

        tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Log.d(TAG, "Card ID: " + Utils.byteArrayToHex(tag.getId()));

        String[] techList = tag.getTechList();

        //Check that the discovered tag is a vicinity tag
        if (techList[0].equals("android.nfc.tech.NfcV")) {
            byte[] tagUid = tag.getId();

            nfcvTag = NfcV.get(tag);

            //ISO/IEC 15693 tags can be operated in two modes:
            // Select mode and Addressed mode.
            //To work in the select mode it is needed to send a SELECT
            // command at the beginning of communic.
            //In the address mode, the tag UID is sent within each command.
            //This application works in SELECT MODE.
            byte[] select_command = RFCommands.cmd_select;
            System.arraycopy(tagUid, 0, select_command, 2, 8);

            if (nfcvTag != null) {
                try {
                    nfcvTag.connect();
                    byte[] select_respo = nfcvTag.transceive(select_command);
                    Log.d(TAG, "Select response: " +
                            Utils.byteArrayToHex(select_respo));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * This method sends RF commands to the connected NFC-V tag,
     * the command is included as parameter
     * the operation will be useful in MainActivity to distinguish
     * the operations in different fragments
     *
     * @param command
     */
    byte[] sendCommand(byte[] command) {
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
}
