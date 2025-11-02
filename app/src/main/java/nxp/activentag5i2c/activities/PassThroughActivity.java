//package nxp.activentag5i2c.activities;
//
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.support.design.widget.Snackbar;
//import android.view.View;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//import android.widget.ToggleButton;
//
//import com.mobileknowledge.library.utils.Utils;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//
//import nxp.activentag5i2c.R;
//import nxp.activentag5i2c.utils.Constants;
//import nxp.activentag5i2c.utils.Parser;
//
//import static nxp.activentag5i2c.nfc.RFCommands.cmd_readSRAM;
//import static nxp.activentag5i2c.nfc.RFCommands.cmd_readTagStatus;
//import static nxp.activentag5i2c.nfc.RFCommands.cmd_writeSRAM;
//import static nxp.activentag5i2c.utils.Constants.SRAM_LOOP_SIZE;
//import static nxp.activentag5i2c.utils.Constants.SRAM_READ_SIZE;
//import static nxp.activentag5i2c.utils.Constants.SRAM_WRITE_SIZE;
//import static nxp.activentag5i2c.utils.Constants.TOAST_LENGTH;
//
//public class PassThroughActivity extends MainActivity {
//
//    private StringBuilder logTextPassThrough = new StringBuilder();
//    private TextView textLog;
//    private final byte[] sramDataToWrite = new byte[SRAM_WRITE_SIZE];
//
//    private TextView textReadSRAM;
//    private TextView textDirection;
//
//    private ToggleButton buttonStartDemo;
//
//    private boolean stopLoop;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_passthrough);
//
//        LinearLayout linearLayoutLog = findViewById(R.id.linearLayoutLog);
//        buttonStartDemo = findViewById(R.id.buttonStartDemo);
//
//        textLog = linearLayoutLog.findViewById(R.id.textLog);
//
//        TextView textWriteSRAM = findViewById(R.id.textWriteSRAM);
//        textReadSRAM = findViewById(R.id.textReadSRAM);
//        textDirection = findViewById(R.id.textDirection);
//
//        stopLoop = false;
//
//        // Initialize SRAM data for RF->I2C mode, not possible to send the whole SRAM (256 bytes)
//        // in a RF command because of the NFC API limitation,
//        // thus the first 2 blocks of the SRAM will not be written and the final lenght is 250 bytes.
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < sramDataToWrite.length; i++) {
//            sramDataToWrite[i] = (byte) i;
//            if (i < SRAM_WRITE_SIZE - 1) {
//                sb.append("0x").append(Integer.toHexString(sramDataToWrite[i] & 0xFF).toUpperCase()).append(", ");
//            } else if (i == SRAM_WRITE_SIZE - 1) {
//                sb.append("0x").append(Integer.toHexString(sramDataToWrite[i] & 0xFF).toUpperCase());
//            }
//        }
//        textWriteSRAM.setText(sb.toString());
//
//        buttonStartDemo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (buttonStartDemo.isChecked()) {
//                    // When pushing the toggle button start the asynctask to continuously
//                    // perform the demo
//                    stopLoop = false;
//                    new SRAMLoop().execute();
//                    buttonStartDemo.setBackgroundResource(R.drawable.button_pushed_passthrough);
//                    buttonStartDemo.setTextColor(getResources().getColor((R.color.buttonBlue)));
//                    buttonStartDemo.setPadding(20, 0, 20, 0);
//                } else {
//                    stopLoop = true;
//                    buttonStartDemo.setBackgroundResource(R.drawable.button_shape);
//                    buttonStartDemo.setTextColor(getResources().getColor((R.color.buttonWhite)));
//                    buttonStartDemo.setPadding(20, 0, 20, 0);
//                }
//            }
//        });
//
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        stopLoop = true;
//    }
//
//    private class SRAMLoop extends AsyncTask<Void, Constants.PassThroughDirection, Boolean> {
//        byte[] finalCommandWriteSRAM;
//        byte[] responseWriteSRAM;
//        final byte[] sramDataRead = new byte[SRAM_READ_SIZE];
//        byte[] responseRead;
//
//        @Override
//        protected Boolean doInBackground(Void... params) {
//            int readCounter = 0;
//            int writeCounter = 0;
//
//            try {
//                //Add to the WRITE SRAM command the fixed data created at startup
//                ByteArrayOutputStream copySRAMData = new ByteArrayOutputStream();
//                try {
//                    copySRAMData.write(cmd_writeSRAM); // WRITE_SRAM command
//                    copySRAMData.write(sramDataToWrite); // Add SRAM fixed data
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                finalCommandWriteSRAM = copySRAMData.toByteArray();
//
//                while (!stopLoop) {
//                    byte[] responseTagStatus = sendCommand(cmd_readTagStatus);
//
//                    //Check if PT_TRANSFER_DIR == RF->I2C direction
//                    if (Parser.IsBitSet(responseTagStatus[1], 2)) {
//                        readCounter = 0;
//
//                        //In FW and Android app the pass-through mode will loop 5 times in each
//                        //direction in a continuous loop
//                        if (writeCounter < SRAM_LOOP_SIZE) {
//                            //Check if SRAM_DATA_READY != 1, means that the I2C has read the SRAM
//                            if (!Parser.IsBitSet(responseTagStatus[1], 5)) {
//                                responseWriteSRAM = sendCommand(finalCommandWriteSRAM);
//                                publishProgress(Constants.PassThroughDirection.RF_I2C);
//                                writeCounter++;
//                            }
//                        }
//                    } else {
//                        writeCounter = 0;
//
//                        //In FW and Android app the pass-through mode will loop 5 times in each
//                        //direction in a continuous loop
//                        if (readCounter < SRAM_LOOP_SIZE) {
//                            // Check if SRAM_DATA_READY == 1, this means that the I2C interface
//                            // has written the SRAM and it is ready to be read via RF.
//                            if (Parser.IsBitSet(responseTagStatus[1], 5)) {
//                                responseRead = sendCommand(cmd_readSRAM);
//                                publishProgress(Constants.PassThroughDirection.I2C_RF);
//                                readCounter++;
//                            }
//                        }
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                Snackbar.make(findViewById(android.R.id.content), "Operation interrupted! Please try again", TOAST_LENGTH)
//                        .show();
//                return false;
//            }
//            return true;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean result) {
//            if (result) {
//                // Write the SRAM write and read commands in the log and the response (only display once is OK because
//                // the other times in the loop are the same command/response)
//                writeSendLog(finalCommandWriteSRAM);
//                writeReceiveLog(responseWriteSRAM);
//
//                writeSendLog(cmd_readSRAM);
//                writeReceiveLog(responseRead);
//
//                textLog.setText(logTextPassThrough.toString());
//            }
//            buttonStartDemo.setBackgroundResource(R.drawable.button_shape);
//            buttonStartDemo.setTextColor(getResources().getColor((R.color.buttonWhite)));
//            buttonStartDemo.setPadding(20, 0, 20, 0);
//            buttonStartDemo.setChecked(false);
//        }
//
//        @Override
//        protected void onPreExecute() {
//        }
//
//        @Override
//        protected void onProgressUpdate(Constants.PassThroughDirection... values) {
//            if (values[0] == Constants.PassThroughDirection.RF_I2C) { //RF->I2C direction
//                textDirection.setText(getResources().getString(R.string.pt_direction_rf_i2c));
//            } else { //I2C->RF direction
//                textDirection.setText(getResources().getString(R.string.pt_direction_i2c_rf));
//
//                try {
//                    textReadSRAM.setText("");
//                    //Store the data read from the SRAM, removing the first byte received (ACK from tag)
//                    System.arraycopy(responseRead, 1, sramDataRead, 0, sramDataRead.length);
//
//                    //Set the data read in the TextBox
//                    StringBuilder sb = new StringBuilder();
//                    for (int i = 0; i < sramDataRead.length; i++) {
//                        if (i < SRAM_READ_SIZE - 1) {
//                            sb.append("0x").append(Integer.toHexString(sramDataRead[i] & 0xFF).toUpperCase()).append(", ");
//                        } else if (i == SRAM_READ_SIZE - 1) {
//                            sb.append("0x").append(Integer.toHexString(sramDataRead[i] & 0xFF).toUpperCase());
//                        }
//                    }
//                    textReadSRAM.setText(sb.toString());
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//
//    //This method is used to add the NFC command to the communication log view
//    private void writeSendLog(byte[] command) {
//        logTextPassThrough = new StringBuilder();
//        logTextPassThrough.append("NFC -> ").append(Utils.byteArrayToHex(command));
//        writeLogFile("GPIO-Logs", logTextPassThrough.toString());
//        logTextPassThrough.append(System.getProperty("line.separator"));
//        writeLogFile("GPIO-Logs", logTextPassThrough.toString());
//
//    }
//
//    //This method is used to add the NFC response to the communication log view
//    private void writeReceiveLog(byte[] response) {
//        if (response != null) {
//            logTextPassThrough.append("TAG <- ").append(Utils.byteArrayToHex(response));
//            writeLogFile("GPIO-Logs", logTextPassThrough.toString());
//            logTextPassThrough.append(System.getProperty("line.separator"));
//            writeLogFile("GPIO-Logs", logTextPassThrough.toString());
//        }
//    }
//
//}







//package nxp.activentag5i2c.activities;
//
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.support.design.widget.Snackbar;
//import android.view.View;
//import android.widget.EditText;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//import android.widget.ToggleButton;
//
//import com.mobileknowledge.library.utils.Utils;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//
//import nxp.activentag5i2c.R;
//import nxp.activentag5i2c.utils.Constants;
//import nxp.activentag5i2c.utils.Parser;
//
//import static nxp.activentag5i2c.nfc.RFCommands.cmd_readSRAM;
//import static nxp.activentag5i2c.nfc.RFCommands.cmd_readTagStatus;
//import static nxp.activentag5i2c.nfc.RFCommands.cmd_writeSRAM;
//import static nxp.activentag5i2c.utils.Constants.SRAM_LOOP_SIZE;
//import static nxp.activentag5i2c.utils.Constants.SRAM_READ_SIZE;
//import static nxp.activentag5i2c.utils.Constants.SRAM_WRITE_SIZE;
//import static nxp.activentag5i2c.utils.Constants.TOAST_LENGTH;
//
//public class PassThroughActivity extends MainActivity {
//
//    private StringBuilder logTextPassThrough = new StringBuilder();
//    private TextView textLog;
//    private final byte[] sramDataToWrite = new byte[SRAM_WRITE_SIZE];
//
//    private TextView textReadSRAM;
//    private TextView textDirection;
//    private TextView textWriteSRAM;
//    private EditText editWriteSRAMInput;
//
//    private ToggleButton buttonStartDemo;
//    private boolean stopLoop;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_passthrough);
//
//        LinearLayout linearLayoutLog = findViewById(R.id.linearLayoutLog);
//        buttonStartDemo = findViewById(R.id.buttonStartDemo);
//        textLog = linearLayoutLog.findViewById(R.id.textLog);
//        textWriteSRAM = findViewById(R.id.textWriteSRAM);
//        textReadSRAM = findViewById(R.id.textReadSRAM);
//        textDirection = findViewById(R.id.textDirection);
//        editWriteSRAMInput = findViewById(R.id.editWriteSRAMInput);
//
//        stopLoop = false;
//
//        buttonStartDemo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (buttonStartDemo.isChecked()) {
//                    // Parse user input into sramDataToWrite[]
//                    String input = editWriteSRAMInput.getText().toString().trim();
//                    if (!input.isEmpty()) {
//                        try {
//                            String[] parts = input.split(",");
//                            for (int i = 0; i < parts.length && i < sramDataToWrite.length; i++) {
//                                String part = parts[i].trim().replace("0x", "").replace("0X", "");
//                                sramDataToWrite[i] = (byte) Integer.parseInt(part, 16);
//                            }
//                            textWriteSRAM.setText(input);
//                        } catch (Exception e) {
//                            Snackbar.make(v, "Invalid input! Use format: 0x01, 0x02, 0x03", Snackbar.LENGTH_LONG).show();
//                            buttonStartDemo.setChecked(false);
//                            return;
//                        }
//                    } else {
//                        Snackbar.make(v, "Please enter SRAM data first!", Snackbar.LENGTH_LONG).show();
//                        buttonStartDemo.setChecked(false);
//                        return;
//                    }
//
//                    stopLoop = false;
//                    new SRAMLoop().execute();
//                    buttonStartDemo.setBackgroundResource(R.drawable.button_pushed_passthrough);
//                    buttonStartDemo.setTextColor(getResources().getColor((R.color.buttonBlue)));
//                    buttonStartDemo.setPadding(20, 0, 20, 0);
//                } else {
//                    stopLoop = true;
//                    buttonStartDemo.setBackgroundResource(R.drawable.button_shape);
//                    buttonStartDemo.setTextColor(getResources().getColor((R.color.buttonWhite)));
//                    buttonStartDemo.setPadding(20, 0, 20, 0);
//                }
//            }
//        });
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        stopLoop = true;
//    }
//
//    private class SRAMLoop extends AsyncTask<Void, Constants.PassThroughDirection, Boolean> {
//        byte[] finalCommandWriteSRAM;
//        byte[] responseWriteSRAM;
//        final byte[] sramDataRead = new byte[SRAM_READ_SIZE];
//        byte[] responseRead;
//
//        @Override
//        protected Boolean doInBackground(Void... params) {
//            int readCounter = 0;
//            int writeCounter = 0;
//
//            try {
//                ByteArrayOutputStream copySRAMData = new ByteArrayOutputStream();
//                copySRAMData.write(cmd_writeSRAM);
//                copySRAMData.write(sramDataToWrite);
//                finalCommandWriteSRAM = copySRAMData.toByteArray();
//
//                while (!stopLoop) {
//                    byte[] responseTagStatus = sendCommand(cmd_readTagStatus);
//
//                    if (Parser.IsBitSet(responseTagStatus[1], 2)) {
//                        readCounter = 0;
//
//                        if (writeCounter < SRAM_LOOP_SIZE) {
//                            if (!Parser.IsBitSet(responseTagStatus[1], 5)) {
//                                responseWriteSRAM = sendCommand(finalCommandWriteSRAM);
//                                publishProgress(Constants.PassThroughDirection.RF_I2C);
//                                writeCounter++;
//                            }
//                        }
//                    } else {
//                        writeCounter = 0;
//
//                        if (readCounter < SRAM_LOOP_SIZE) {
//                            if (Parser.IsBitSet(responseTagStatus[1], 5)) {
//                                responseRead = sendCommand(cmd_readSRAM);
//                                publishProgress(Constants.PassThroughDirection.I2C_RF);
//                                readCounter++;
//                            }
//                        }
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                Snackbar.make(findViewById(android.R.id.content),
//                        "Operation interrupted! Please try again", TOAST_LENGTH).show();
//                return false;
//            }
//            return true;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean result) {
//            if (result) {
//                writeSendLog(finalCommandWriteSRAM);
//                writeReceiveLog(responseWriteSRAM);
//                writeSendLog(cmd_readSRAM);
//                writeReceiveLog(responseRead);
//                textLog.setText(logTextPassThrough.toString());
//            }
//            buttonStartDemo.setBackgroundResource(R.drawable.button_shape);
//            buttonStartDemo.setTextColor(getResources().getColor((R.color.buttonWhite)));
//            buttonStartDemo.setPadding(20, 0, 20, 0);
//            buttonStartDemo.setChecked(false);
//        }
//
//        @Override
//        protected void onProgressUpdate(Constants.PassThroughDirection... values) {
//            if (values[0] == Constants.PassThroughDirection.RF_I2C) {
//                textDirection.setText(getResources().getString(R.string.pt_direction_rf_i2c));
//            } else {
//                textDirection.setText(getResources().getString(R.string.pt_direction_i2c_rf));
//
//                try {
//                    textReadSRAM.setText("");
//                    System.arraycopy(responseRead, 1, sramDataRead, 0, sramDataRead.length);
//
//                    StringBuilder sb = new StringBuilder();
//                    for (int i = 0; i < sramDataRead.length; i++) {
//                        if (i < SRAM_READ_SIZE - 1)
//                            sb.append("0x").append(Integer.toHexString(sramDataRead[i] & 0xFF).toUpperCase()).append(", ");
//                        else
//                            sb.append("0x").append(Integer.toHexString(sramDataRead[i] & 0xFF).toUpperCase());
//                    }
//                    textReadSRAM.setText(sb.toString());
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//
//    private void writeSendLog(byte[] command) {
//        logTextPassThrough = new StringBuilder();
//        logTextPassThrough.append("NFC -> ").append(Utils.byteArrayToHex(command));
//        writeLogFile("GPIO-Logs", logTextPassThrough.toString());
//        logTextPassThrough.append(System.getProperty("line.separator"));
//        writeLogFile("GPIO-Logs", logTextPassThrough.toString());
//    }
//
//    private void writeReceiveLog(byte[] response) {
//        if (response != null) {
//            logTextPassThrough.append("TAG <- ").append(Utils.byteArrayToHex(response));
//            writeLogFile("GPIO-Logs", logTextPassThrough.toString());
//            logTextPassThrough.append(System.getProperty("line.separator"));
//            writeLogFile("GPIO-Logs", logTextPassThrough.toString());
//        }
//    }
//}




















// code with write and read operations with user input text 👇



//package nxp.activentag5i2c.activities;
//
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.support.design.widget.Snackbar;
//import android.view.View;
//import android.widget.EditText;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//import android.widget.ToggleButton;
//
//import com.mobileknowledge.library.utils.Utils;
//
//import java.io.ByteArrayOutputStream;
//
//import nxp.activentag5i2c.R;
//import nxp.activentag5i2c.utils.Constants;
//import nxp.activentag5i2c.utils.Parser;
//
//import static nxp.activentag5i2c.nfc.RFCommands.cmd_readSRAM;
//import static nxp.activentag5i2c.nfc.RFCommands.cmd_readTagStatus;
//import static nxp.activentag5i2c.nfc.RFCommands.cmd_writeSRAM;
//import static nxp.activentag5i2c.utils.Constants.SRAM_LOOP_SIZE;
//import static nxp.activentag5i2c.utils.Constants.SRAM_READ_SIZE;
//import static nxp.activentag5i2c.utils.Constants.SRAM_WRITE_SIZE;
//import static nxp.activentag5i2c.utils.Constants.TOAST_LENGTH;
//
//public class PassThroughActivity extends MainActivity {
//
//    private StringBuilder logTextPassThrough = new StringBuilder();
//    private TextView textLog;
//    private final byte[] sramDataToWrite = new byte[SRAM_WRITE_SIZE];
//
//    private TextView textReadSRAM;
//    private TextView textDirection;
//    private TextView textWriteSRAM;
//    private EditText editWriteSRAMInput;
//
//    private ToggleButton buttonStartDemo;
//    private boolean stopLoop;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_passthrough);
//
//        LinearLayout linearLayoutLog = findViewById(R.id.linearLayoutLog);
//        buttonStartDemo = findViewById(R.id.buttonStartDemo);
//        textLog = linearLayoutLog.findViewById(R.id.textLog);
//        textWriteSRAM = findViewById(R.id.textWriteSRAM);
//        textReadSRAM = findViewById(R.id.textReadSRAM);
//        textDirection = findViewById(R.id.textDirection);
//        editWriteSRAMInput = findViewById(R.id.editWriteSRAMInput);
//
//        stopLoop = false;
//
//        buttonStartDemo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (buttonStartDemo.isChecked()) {
//                    // Convert user input string to bytes
//                    String input = editWriteSRAMInput.getText().toString().trim();
//                    if (!input.isEmpty()) {
//                        try {
//                            byte[] userBytes = input.getBytes("UTF-8");
//
//                            int len = Math.min(userBytes.length, SRAM_WRITE_SIZE);
//                            System.arraycopy(userBytes, 0, sramDataToWrite, 0, len);
//
//                            // Fill remaining bytes with zeros
//                            for (int i = len; i < SRAM_WRITE_SIZE; i++) {
//                                sramDataToWrite[i] = 0x00;
//                            }
//
//                            // Convert to hex string for display
//                            StringBuilder sb = new StringBuilder();
//                            for (int i = 0; i < len; i++) {
//                                sb.append("0x")
//                                        .append(String.format("%02X", sramDataToWrite[i]))
//                                        .append(i < len - 1 ? ", " : "");
//                            }
//
//                            textWriteSRAM.setText(sb.toString());
//
//                        } catch (Exception e) {
//                            Snackbar.make(v, "Error encoding input to bytes!", Snackbar.LENGTH_LONG).show();
//                            buttonStartDemo.setChecked(false);
//                            return;
//                        }
//                    } else {
//                        Snackbar.make(v, "Please enter text first!", Snackbar.LENGTH_LONG).show();
//                        buttonStartDemo.setChecked(false);
//                        return;
//                    }
//
//                    stopLoop = false;
//                    new SRAMLoop().execute();
//                    buttonStartDemo.setBackgroundResource(R.drawable.button_pushed_passthrough);
//                    buttonStartDemo.setTextColor(getResources().getColor(R.color.buttonBlue));
//                    buttonStartDemo.setPadding(20, 0, 20, 0);
//                } else {
//                    stopLoop = true;
//                    buttonStartDemo.setBackgroundResource(R.drawable.button_shape);
//                    buttonStartDemo.setTextColor(getResources().getColor(R.color.buttonWhite));
//                    buttonStartDemo.setPadding(20, 0, 20, 0);
//                }
//            }
//        });
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        stopLoop = true;
//    }
//
//    private class SRAMLoop extends AsyncTask<Void, Constants.PassThroughDirection, Boolean> {
//        byte[] finalCommandWriteSRAM;
//        byte[] responseWriteSRAM;
//        final byte[] sramDataRead = new byte[SRAM_READ_SIZE];
//        byte[] responseRead;
//
//        @Override
//        protected Boolean doInBackground(Void... params) {
//            int readCounter = 0;
//            int writeCounter = 0;
//
//            try {
//                ByteArrayOutputStream copySRAMData = new ByteArrayOutputStream();
//                copySRAMData.write(cmd_writeSRAM);
//                copySRAMData.write(sramDataToWrite);
//                finalCommandWriteSRAM = copySRAMData.toByteArray();
//
//                while (!stopLoop) {
//                    byte[] responseTagStatus = sendCommand(cmd_readTagStatus);
//
//                    if (Parser.IsBitSet(responseTagStatus[1], 2)) {
//                        readCounter = 0;
//
//                        if (writeCounter < SRAM_LOOP_SIZE) {
//                            if (!Parser.IsBitSet(responseTagStatus[1], 5)) {
//                                responseWriteSRAM = sendCommand(finalCommandWriteSRAM);
//                                publishProgress(Constants.PassThroughDirection.RF_I2C);
//                                writeCounter++;
//                            }
//                        }
//                    } else {
//                        writeCounter = 0;
//
//                        if (readCounter < SRAM_LOOP_SIZE) {
//                            if (Parser.IsBitSet(responseTagStatus[1], 5)) {
//                                responseRead = sendCommand(cmd_readSRAM);
//                                publishProgress(Constants.PassThroughDirection.I2C_RF);
//                                readCounter++;
//                            }
//                        }
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                Snackbar.make(findViewById(android.R.id.content),
//                        "Operation interrupted! Please try again", TOAST_LENGTH).show();
//                return false;
//            }
//            return true;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean result) {
//            if (result) {
//                writeSendLog(finalCommandWriteSRAM);
//                writeReceiveLog(responseWriteSRAM);
//                writeSendLog(cmd_readSRAM);
//                writeReceiveLog(responseRead);
//                textLog.setText(logTextPassThrough.toString());
//            }
//            buttonStartDemo.setBackgroundResource(R.drawable.button_shape);
//            buttonStartDemo.setTextColor(getResources().getColor(R.color.buttonWhite));
//            buttonStartDemo.setPadding(20, 0, 20, 0);
//            buttonStartDemo.setChecked(false);
//        }
//
//        @Override
//        protected void onProgressUpdate(Constants.PassThroughDirection... values) {
//            if (values[0] == Constants.PassThroughDirection.RF_I2C) {
//                textDirection.setText(getResources().getString(R.string.pt_direction_rf_i2c));
//            } else {
//                textDirection.setText(getResources().getString(R.string.pt_direction_i2c_rf));
//
//                try {
//                    textReadSRAM.setText("");
//                    System.arraycopy(responseRead, 1, sramDataRead, 0, sramDataRead.length);
//
//                    StringBuilder sb = new StringBuilder();
//                    for (int i = 0; i < sramDataRead.length; i++) {
//                        sb.append("0x")
//                                .append(String.format("%02X", sramDataRead[i]))
//                                .append(i < sramDataRead.length - 1 ? ", " : "");
//                    }
//                    textReadSRAM.setText(sb.toString());
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//
//    private void writeSendLog(byte[] command) {
//        logTextPassThrough = new StringBuilder();
//        logTextPassThrough.append("NFC -> ").append(Utils.byteArrayToHex(command));
//        writeLogFile("GPIO-Logs", logTextPassThrough.toString());
//        logTextPassThrough.append(System.getProperty("line.separator"));
//        writeLogFile("GPIO-Logs", logTextPassThrough.toString());
//    }
//
//    private void writeReceiveLog(byte[] response) {
//        if (response != null) {
//            logTextPassThrough.append("TAG <- ").append(Utils.byteArrayToHex(response));
//            writeLogFile("GPIO-Logs", logTextPassThrough.toString());
//            logTextPassThrough.append(System.getProperty("line.separator"));
//            writeLogFile("GPIO-Logs", logTextPassThrough.toString());
//        }
//    }
//}






















// code with only write operation with user input text 👇


package nxp.activentag5i2c.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

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
    private ToggleButton buttonStartDemo;

    private boolean stopLoop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passthrough);

        LinearLayout linearLayoutLog = findViewById(R.id.linearLayoutLog);
        buttonStartDemo = findViewById(R.id.buttonStartDemo);
        textLog = linearLayoutLog.findViewById(R.id.textLog);
        textWriteSRAM = findViewById(R.id.textWriteSRAM);
        textDirection = findViewById(R.id.textDirection);
        editWriteSRAMInput = findViewById(R.id.editWriteSRAMInput);

        stopLoop = false;

        buttonStartDemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buttonStartDemo.isChecked()) {
                    // Convert user text into bytes
                    String input = editWriteSRAMInput.getText().toString().trim();
                    if (input.isEmpty()) {
                        Snackbar.make(v, "Please enter text first!", Snackbar.LENGTH_LONG).show();
                        buttonStartDemo.setChecked(false);
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
                        Snackbar.make(v, "Error converting text to bytes!", Snackbar.LENGTH_LONG).show();
                        buttonStartDemo.setChecked(false);
                        return;
                    }

                    stopLoop = false;
                    new SRAMWriteTask().execute();
                    buttonStartDemo.setBackgroundResource(R.drawable.button_pushed_passthrough);
                    buttonStartDemo.setTextColor(getResources().getColor(R.color.buttonBlue));
                } else {
                    stopLoop = true;
                    buttonStartDemo.setBackgroundResource(R.drawable.button_shape);
                    buttonStartDemo.setTextColor(getResources().getColor(R.color.buttonWhite));
                }
            }
        });
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
                writeSendLog(finalCommandWriteSRAM);
                writeReceiveLog(responseWriteSRAM);
                textLog.setText(logTextPassThrough.toString());
            }
            buttonStartDemo.setBackgroundResource(R.drawable.button_shape);
            buttonStartDemo.setTextColor(getResources().getColor(R.color.buttonWhite));
            buttonStartDemo.setChecked(false);
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