package nxp.activentag5i2c.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
// 1. REMOVE ToggleButton import
// import android.widget.ToggleButton;

import com.mobileknowledge.library.utils.Utils;

import java.io.ByteArrayOutputStream;

import nxp.activentag5i2c.R;
import nxp.activentag5i2c.utils.Constants;
import nxp.activentag5i2c.utils.Parser;

import static nxp.activentag5i2c.nfc.RFCommands.cmd_writeSRAM;
import static nxp.activentag5i2c.nfc.RFCommands.cmd_readTagStatus;
import static nxp.activentag5i2c.utils.Constants.SRAM_LOOP_SIZE;
import static nxp.activentag5i2c.utils.Constants.SRAM_WRITE_SIZE;
import static nxp.activentag5i2c.utils.Constants.TOAST_LENGTH;

public class PassThroughActivity extends MainActivity {

    private StringBuilder logTextPassThrough = new StringBuilder();
    private TextView textLog;
    private final byte[] sramDataToWrite = new byte[SRAM_WRITE_SIZE];

    private TextView textDirection;
    private TextView textWriteSRAM;
    private EditText editWriteSRAMInput;
    // 2. REMOVE button variable
    // private ToggleButton buttonStartDemo;

    private boolean stopLoop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passthrough);

        LinearLayout linearLayoutLog = findViewById(R.id.linearLayoutLog);
        textLog = linearLayoutLog.findViewById(R.id.textLog);
        textWriteSRAM = findViewById(R.id.textWriteSRAM);
        textDirection = findViewById(R.id.textDirection);
        editWriteSRAMInput = findViewById(R.id.editWriteSRAMInput);

        stopLoop = false;

    }

    // 5. ADD onNewIntent method to trigger the write task
    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent); // Connects the tag from BaseActivity

        // Check for correct tag type
        if (tag.getTechList()[0].equals("android.nfc.tech.NfcV")) {
            // Call the write logic
            startSramWrite();
        } else {
            Snackbar.make(findViewById(android.R.id.content),
                    "Tag not supported.", Snackbar.LENGTH_SHORT).show();
        }
    }

    // 6. CREATE startSramWrite() method from the button's old logic
    private void startSramWrite() {
        // Convert user text into bytes
        String input = editWriteSRAMInput.getText().toString().trim();
        if (input.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), "Please enter text first!", Snackbar.LENGTH_LONG).show();
            return;
        }

        try {
            byte[] userBytes = input.getBytes("UTF-8");

            int len = Math.min(userBytes.length, SRAM_WRITE_SIZE);
            System.arraycopy(userBytes, 0, sramDataToWrite, 0, len);

            // Fill remaining with zeros
            for (int i = len; i < SRAM_WRITE_SIZE; i++) {
                sramDataToWrite[i] = 0x00;
            }

            // Display data as hex
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < len; i++) {
                sb.append("0x")
                        .append(String.format("%02X", sramDataToWrite[i]))
                        .append(i < len - 1 ? ", " : "");
            }
            textWriteSRAM.setText(sb.toString());

        } catch (Exception e) {
            Snackbar.make(findViewById(android.R.id.content), "Error converting text to bytes!", Snackbar.LENGTH_LONG).show();
            return;
        }

        stopLoop = false;
        new SRAMWriteTask().execute();
        Snackbar.make(findViewById(android.R.id.content), "NFC Tag Detected. Starting write...", Snackbar.LENGTH_SHORT).show();
    }


    @Override
    protected void onPause() {
        super.onPause();
        stopLoop = true;
    }

    /**
     * AsyncTask for continuous write loop only (RF → I²C)
     */
    private class SRAMWriteTask extends AsyncTask<Void, Void, Boolean> {
        byte[] finalCommandWriteSRAM;
        byte[] responseWriteSRAM;

        @Override
        protected Boolean doInBackground(Void... params) {
            int writeCounter = 0;

            try {
                ByteArrayOutputStream copySRAMData = new ByteArrayOutputStream();
                copySRAMData.write(cmd_writeSRAM);
                copySRAMData.write(sramDataToWrite);
                finalCommandWriteSRAM = copySRAMData.toByteArray();

                while (!stopLoop && writeCounter < SRAM_LOOP_SIZE) {
                    byte[] responseTagStatus = sendCommand(cmd_readTagStatus);

                    // Bit 2 means I²C not accessing SRAM (safe to write)
                    if (Parser.IsBitSet(responseTagStatus[1], 2)) {
                        if (!Parser.IsBitSet(responseTagStatus[1], 5)) {
                            // Save the response to the class variable
                            responseWriteSRAM = sendCommand(finalCommandWriteSRAM);
                            publishProgress();
                            writeCounter++;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Snackbar.make(findViewById(android.R.id.content),
                        "Write operation interrupted! Try again.", TOAST_LENGTH).show();
                return false;
            }
            return true;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            textDirection.setText(getResources().getString(R.string.pt_direction_rf_i2c));
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                // Generate Hex Strings for display
                String commandHex = Utils.byteArrayToHex(finalCommandWriteSRAM);
                String responseHex = (responseWriteSRAM != null)
                        ? Utils.byteArrayToHex(responseWriteSRAM)
                        : "No Response";

                // Build the log message
                logTextPassThrough = new StringBuilder();
                logTextPassThrough.append("--- WRITE SUCCESS ---").append("\n");
                logTextPassThrough.append("CMD (Sent): ").append(commandHex).append("\n");
                logTextPassThrough.append("RSP (Recv): ").append(responseHex).append("\n");

                // Update the TextView on screen
                textLog.setText(logTextPassThrough.toString());

                // Keep your existing file logging if needed
                writeLogFile("GPIO-Logs", logTextPassThrough.toString());
            }
        }
    }

    private void writeSendLog(byte[] command) {
        logTextPassThrough = new StringBuilder();
        logTextPassThrough.append("NFC -> ").append(Utils.byteArrayToHex(command));
        writeLogFile("GPIO-Logs", logTextPassThrough.toString());
        logTextPassThrough.append(System.lineSeparator());
    }

    private void writeReceiveLog(byte[] response) {
        if (response != null) {
            logTextPassThrough.append("TAG <- ").append(Utils.byteArrayToHex(response));
            writeLogFile("GPIO-Logs", logTextPassThrough.toString());
            logTextPassThrough.append(System.lineSeparator());
        }
    }
}