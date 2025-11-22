package nxp.activentag5i2c.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mobileknowledge.library.utils.Utils;

import nxp.activentag5i2c.R;

import static nxp.activentag5i2c.nfc.RFCommands.cmd_readSRAM;
import static nxp.activentag5i2c.utils.Constants.TOAST_LENGTH;

public class ReadSramActivity extends MainActivity {

    private StringBuilder logTextPassThrough = new StringBuilder();
    private TextView textLog;
    private TextView textDirection;

    private TextView textReadSRAM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_sram);

        LinearLayout linearLayoutLog = findViewById(R.id.linearLayoutLog);
        textLog = linearLayoutLog.findViewById(R.id.textLog);
        textDirection = findViewById(R.id.textDirection);

        textReadSRAM = findViewById(R.id.textReadSRAM);
    }

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent); // Connects the tag from BaseActivity

        // Check for correct tag type
        if (tag.getTechList()[0].equals("android.nfc.tech.NfcV")) {
            // 6. CALL read logic
            startSramRead();
        } else {
            Snackbar.make(findViewById(android.R.id.content),
                    "Tag not supported.", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void startSramRead() {
        new SRAMReadTask().execute();
        Snackbar.make(findViewById(android.R.id.content), "NFC Tag Detected. Starting read...", Snackbar.LENGTH_SHORT).show();
    }


    /**
     * AsyncTask for SRAM read operation (I²C → RF)
     */
    private class SRAMReadTask extends AsyncTask<Void, Void, byte[]> {

        byte[] responseReadSRAM;

        @Override
        protected byte[] doInBackground(Void... params) {
            try {
                responseReadSRAM = sendCommand(cmd_readSRAM);
                publishProgress(); // For updating direction
                return responseReadSRAM;
            } catch (Exception e) {
                e.printStackTrace();
                Snackbar.make(findViewById(android.R.id.content),
                        "Read operation interrupted! Try again.", TOAST_LENGTH).show();
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            textDirection.setText(getResources().getString(R.string.pt_direction_i2c_rf));
        }

        @Override
        protected void onPostExecute(byte[] result) {
            if (result != null && result[0] == 0x00) {
                // SUCCESS
                writeSendLog(cmd_readSRAM); // Log the command
                writeReceiveLog(result);    // Log the response
                textLog.setText(logTextPassThrough.toString());

                // Process and display the data
                // The actual data starts from the second byte (index 1)
                int dataLength = result.length - 1;
                byte[] data = new byte[dataLength];
                System.arraycopy(result, 1, data, 0, dataLength);

                String hexData = Utils.byteArrayToHex(data);
                String asciiData;

                try {
                    // Try to decode as UTF-8 first
                    asciiData = new String(data, "UTF-8");
                } catch (Exception e) {
                    // Fallback to basic ASCII
                    asciiData = new String(data, java.nio.charset.StandardCharsets.US_ASCII);
                }

                // Clean ASCII data (remove non-printables and find first null terminator)
                asciiData = asciiData.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");
                int nullIndex = asciiData.indexOf(0);
                if (nullIndex != -1) {
                    asciiData = asciiData.substring(0, nullIndex);
                }

                StringBuilder displayData = new StringBuilder();
                displayData.append("--- ASCII Data ---\n");
                displayData.append(asciiData.trim());
                displayData.append("\n\n--- HEX Data ---\n");

                // Format hex string with spaces
                for (int i = 0; i < hexData.length(); i += 2) {
                    displayData.append(hexData.substring(i, Math.min(i + 2, hexData.length())));
                    displayData.append(" ");
                    if ((i + 2) % 32 == 0) { // Newline every 16 bytes
                        displayData.append("\n");
                    }
                }

                textReadSRAM.setText(displayData.toString());
                Snackbar.make(findViewById(android.R.id.content), "SRAM Read Successful!", Snackbar.LENGTH_SHORT).show();

            } else {
                // FAILURE
                writeSendLog(cmd_readSRAM);
                writeReceiveLog(result);
                textLog.setText(logTextPassThrough.toString());
                textReadSRAM.setText("Error reading SRAM. See log.");
                Snackbar.make(findViewById(android.R.id.content), "Error reading SRAM.", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    // logging functions (copied from PassThroughActivity)
    private void writeSendLog(byte[] command) {
        logTextPassThrough = new StringBuilder();
        logTextPassThrough.append("NFC -> ").append(Utils.byteArrayToHex(command));
        writeLogFile("Pass-Through-Logs", logTextPassThrough.toString());
        logTextPassThrough.append(System.lineSeparator());
    }

    private void writeReceiveLog(byte[] response) {
        if (response != null) {
            logTextPassThrough.append("TAG <- ").append(Utils.byteArrayToHex(response));
            writeLogFile("Pass-Through-Logs", logTextPassThrough.toString());
            logTextPassThrough.append(System.lineSeparator());
        }
    }
}