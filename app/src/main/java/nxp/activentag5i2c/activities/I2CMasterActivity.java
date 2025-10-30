package nxp.activentag5i2c.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.mobileknowledge.library.utils.Utils;

import nxp.activentag5i2c.R;
import nxp.activentag5i2c.utils.Constants;

import static nxp.activentag5i2c.nfc.RFCommands.cmd_GetTempI2CMasterCommand;
import static nxp.activentag5i2c.nfc.RFCommands.cmd_GetXaccMSBI2CMasterCommand;
import static nxp.activentag5i2c.nfc.RFCommands.cmd_GetXmagMSBI2CMasterCommand;
import static nxp.activentag5i2c.nfc.RFCommands.cmd_GetYaccMSBI2CMasterCommand;
import static nxp.activentag5i2c.nfc.RFCommands.cmd_GetYmagMSBI2CMasterCommand;
import static nxp.activentag5i2c.nfc.RFCommands.cmd_GetZaccMSBI2CMasterCommand;
import static nxp.activentag5i2c.nfc.RFCommands.cmd_GetZmagMSBI2CMasterCommand;
import static nxp.activentag5i2c.nfc.RFCommands.cmd_i2cMasterConfigStatus;
import static nxp.activentag5i2c.nfc.RFCommands.cmd_readI2CMasterCommand;
import static nxp.activentag5i2c.nfc.RFCommands.cmd_readSRAMI2CMaster;
import static nxp.activentag5i2c.nfc.RFCommands.cmd_setActiveModeI2CMasterCommand;
import static nxp.activentag5i2c.nfc.RFCommands.cmd_setControlReg2I2CMasterCommand;
import static nxp.activentag5i2c.nfc.RFCommands.cmd_setHybridModeI2CMasterCommand;
import static nxp.activentag5i2c.nfc.RFCommands.cmd_setStandByModeI2CMasterCommand;
import static nxp.activentag5i2c.utils.Constants.TOAST_LENGTH;

public class I2CMasterActivity extends MainActivity {
    private EditText editCommand;
    private TextView usefulCmdText, textLog, textResultSensor, dataReadTitle;
    private RadioButton radioDefault, radioCustom;
    private Spinner defaultCmdSpinner;
    private Constants.I2CMASTER_COMMANDS i2CMasterDefaultCommandSelected;
    private StringBuilder logTextI2CMaster = new StringBuilder();
    private  LinearLayout writeCommandExample;
    private ImageView writeCommandPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_i2c_master);

        LinearLayout linearLayoutLog = findViewById(R.id.linearLayoutLog);

        editCommand = findViewById(R.id.write_command);

        dataReadTitle = findViewById(R.id.data_read_title);
        usefulCmdText = findViewById(R.id.text_useful_commands);
        textResultSensor = findViewById(R.id.text_result_sensor);
        writeCommandExample = findViewById(R.id.linearLayoutTutorial);
        writeCommandPicture = findViewById(R.id.write_command_example_picture);


        Button buttonSendCommand = findViewById(R.id.send_button);
        Button buttonConfigSensor = findViewById(R.id.button_config_sensor);
        Button buttonGetTemp = findViewById(R.id.button_get_temp);
        Button buttonGetAccX = findViewById(R.id.button_get_acc_x);
        Button buttonGetAccY= findViewById(R.id.button_get_acc_y);
        Button buttonGetAccZ = findViewById(R.id.button_get_acc_z);

        defaultCmdSpinner = findViewById(R.id.spinner_useful_commands);

        textLog = linearLayoutLog.findViewById(R.id.textLog);

        textResultSensor.setVisibility(View.GONE);
        dataReadTitle.setVisibility(View.GONE);

        ArrayAdapter<String> adapterDirection = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, Constants.DEFAULT_COMMANDS);
        defaultCmdSpinner.setAdapter(adapterDirection);
        defaultCmdSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                switch (position) {
                    case 0:
                        i2CMasterDefaultCommandSelected = Constants.I2CMASTER_COMMANDS.CONFIG_SENSOR;
                        break;
                    case 1:
                        i2CMasterDefaultCommandSelected = Constants.I2CMASTER_COMMANDS.GET_TEMP;
                        break;
                    case 2:
                        i2CMasterDefaultCommandSelected = Constants.I2CMASTER_COMMANDS.GET_ACCEL_X;
                        break;
                    case 3:
                        i2CMasterDefaultCommandSelected = Constants.I2CMASTER_COMMANDS.GET_ACCEL_Y;
                        break;
                    case 4:
                        i2CMasterDefaultCommandSelected = Constants.I2CMASTER_COMMANDS.GET_ACCEL_Z;
                        break;
                    case 5:
                        i2CMasterDefaultCommandSelected = Constants.I2CMASTER_COMMANDS.GET_MAGNETO_X;
                        break;
                    case 6:
                        i2CMasterDefaultCommandSelected = Constants.I2CMASTER_COMMANDS.GET_MAGNETO_Y;
                        break;
                    case 7:
                        i2CMasterDefaultCommandSelected = Constants.I2CMASTER_COMMANDS.GET_MAGNETO_Z;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        radioDefault = findViewById(R.id.radioDefaultCmd);
        radioCustom = findViewById(R.id.radioCustomCmd);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioDefaultCmd) {
                    defaultCmdSpinner.setVisibility(View.VISIBLE);
                    usefulCmdText.setVisibility(View.VISIBLE);
                    editCommand.setVisibility(View.GONE);
                    writeCommandExample.setVisibility(View.GONE);
                } else if (checkedId == R.id.radioCustomCmd) {
                    defaultCmdSpinner.setVisibility(View.GONE);
                    usefulCmdText.setVisibility(View.GONE);
                    editCommand.setVisibility(View.VISIBLE);
                    writeCommandExample.setVisibility(View.VISIBLE);
                }
            }
        });

        buttonSendCommand.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ShowToast")
            @Override
            public void onClick(View v) {
                    if (editCommand.getText().toString().trim().length() == 0)
                        Snackbar.make(findViewById(android.R.id.content),
                                "Please introduce a command in the Text box", TOAST_LENGTH).show();
                    else {
                        try{
                            sendCustomI2CMasterCommand(Utils.hexToByteArray(editCommand.getText().toString()));
                        } catch (IllegalArgumentException e){
                            e.printStackTrace();
                            Snackbar.make(findViewById(android.R.id.content),
                                    "Invalid format for hex value", TOAST_LENGTH).show();
                        }
                    }
            }
        });

        buttonConfigSensor.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ShowToast")
            @Override
            public void onClick(View v) {
                configSensorI2CMaster();
            }
        });

        buttonGetTemp.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ShowToast")
            @Override
            public void onClick(View v) {
                getTempI2CMaster();
            }
        });

        buttonGetAccX.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ShowToast")
            @Override
            public void onClick(View v) {
                getAccelXI2CMaster();
            }
        });

        buttonGetAccY.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ShowToast")
            @Override
            public void onClick(View v) {
                getAccelYI2CMaster();
            }
        });

        buttonGetAccZ.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ShowToast")
            @Override
            public void onClick(View v) {
                getAccelZI2CMaster();
            }
        });

    }

    //For the custom I2C Master command, the command is retrieved from the editText and included as
    //parameter
    private void sendCustomI2CMasterCommand(byte[] command) {
        try {
            writeSendLog(command);
            byte[] response = sendCommand(command);
            writeReceiveLog(response);

            writeSendLog(cmd_readI2CMasterCommand);
            byte[] response2 = sendCommand(cmd_readI2CMasterCommand);
            writeReceiveLog(response2);

            writeSendLog(cmd_i2cMasterConfigStatus);
            byte[] response3 = sendCommand(cmd_i2cMasterConfigStatus);
            writeReceiveLog(response3);

            writeSendLog(cmd_readSRAMI2CMaster);
            byte[] responseSRAM = sendCommand(cmd_readSRAMI2CMaster);
            writeReceiveLog(responseSRAM);


            Snackbar.make(findViewById(android.R.id.content), "Command correctly sent", TOAST_LENGTH)
                    .show();

            //Check if the SRAM has been read to show the result in the textResultSensor.
            //In case there is nothing to show just hide the view.
            textResultSensor.setText(Utils.byteArrayToHex(responseSRAM)
                    .substring(2)); //Remove first byte (00) because is the success code of vicinity command, not user data.
            textResultSensor.setVisibility(View.VISIBLE);
            dataReadTitle.setVisibility(View.VISIBLE);


            textLog.setText(logTextI2CMaster.toString());
        }catch (Exception e) {
            e.printStackTrace();
            Snackbar.make(findViewById(android.R.id.content), "Operation interrupted! Please try again", TOAST_LENGTH)
                    .show();
        }
    }

    //First it is needed to configure the KW41Z combosensor, select standby mode. Then change to
    //work in hybrid mode so Accelerometer and Magnetometer are both active at the same time.
    //Also some configuration is added in the Control register and finally the sensor is entered in active state.
    //(Each time a command is sent, a read of ConfigStatus register is done to check the status of the I2C interface)
    private void configSensorI2CMaster() {
        try {
            writeSendLog(cmd_setStandByModeI2CMasterCommand);
            byte[] response = sendCommand(cmd_setStandByModeI2CMasterCommand);
            writeReceiveLog(response);

            writeSendLog(cmd_setHybridModeI2CMasterCommand);
            byte[] response2 = sendCommand(cmd_setHybridModeI2CMasterCommand);
            writeReceiveLog(response2);

            writeSendLog(cmd_setControlReg2I2CMasterCommand);
            byte[] response3 = sendCommand(cmd_setControlReg2I2CMasterCommand);
            writeReceiveLog(response3);

            writeSendLog(cmd_setActiveModeI2CMasterCommand);
            byte[] response4 = sendCommand(cmd_setActiveModeI2CMasterCommand);
            writeReceiveLog(response4);


            Snackbar.make(findViewById(android.R.id.content), "Command correctly sent", TOAST_LENGTH)
                    .show();

            textLog.setText(logTextI2CMaster.toString());
        }catch (Exception e) {
            e.printStackTrace();
            Snackbar.make(findViewById(android.R.id.content), "Operation interrupted! Please try again", TOAST_LENGTH)
                    .show();
        }
    }

    private void getTempI2CMaster() {
        try {
            writeSendLog(cmd_GetTempI2CMasterCommand);
            byte[] response = sendCommand(cmd_GetTempI2CMasterCommand);
            writeReceiveLog(response);

            readData();
        }catch (Exception e) {
            e.printStackTrace();
            Snackbar.make(findViewById(android.R.id.content), "Operation interrupted! Please try again", TOAST_LENGTH)
                    .show();
        }
    }

    private void getAccelXI2CMaster() {
        try {
            writeSendLog(cmd_GetXaccMSBI2CMasterCommand);
            byte[] response = sendCommand(cmd_GetXaccMSBI2CMasterCommand);
            writeReceiveLog(response);

            readData();
        }catch (Exception e) {
            e.printStackTrace();
            Snackbar.make(findViewById(android.R.id.content), "Operation interrupted! Please try again", TOAST_LENGTH)
                    .show();
        }
    }

    private void getAccelYI2CMaster() {
        try {
            writeSendLog(cmd_GetYaccMSBI2CMasterCommand);
            byte[] response = sendCommand(cmd_GetYaccMSBI2CMasterCommand);
            writeReceiveLog(response);

            readData();
        }catch (Exception e) {
            e.printStackTrace();
            Snackbar.make(findViewById(android.R.id.content), "Operation interrupted! Please try again", TOAST_LENGTH)
                    .show();
        }
    }

    private void getAccelZI2CMaster() {
        try {
            writeSendLog(cmd_GetZaccMSBI2CMasterCommand);
            byte[] response = sendCommand(cmd_GetZaccMSBI2CMasterCommand);
            writeReceiveLog(response);

            readData();
        }catch (Exception e) {
            e.printStackTrace();
            Snackbar.make(findViewById(android.R.id.content), "Operation interrupted! Please try again", TOAST_LENGTH)
                    .show();
        }
    }

    private void getMagnetXI2CMaster() {
        try {
            writeSendLog(cmd_GetXmagMSBI2CMasterCommand);
            byte[] response = sendCommand(cmd_GetXmagMSBI2CMasterCommand);
            writeReceiveLog(response);

            readData();
        }catch (Exception e) {
            e.printStackTrace();
            Snackbar.make(findViewById(android.R.id.content), "Operation interrupted! Please try again", TOAST_LENGTH)
                    .show();
        }
    }

    private void getMagnetYI2CMaster() {
        try {
            writeSendLog(cmd_GetYmagMSBI2CMasterCommand);
            byte[] response = sendCommand(cmd_GetYmagMSBI2CMasterCommand);
            writeReceiveLog(response);

            readData();
        }catch (Exception e) {
            e.printStackTrace();
            Snackbar.make(findViewById(android.R.id.content), "Operation interrupted! Please try again", TOAST_LENGTH)
                    .show();
        }
    }

    private void getMagnetZI2CMaster() {
        try {
            writeSendLog(cmd_GetZmagMSBI2CMasterCommand);
            byte[] response = sendCommand(cmd_GetZmagMSBI2CMasterCommand);
            writeReceiveLog(response);

            readData();

        }catch (Exception e) {
            e.printStackTrace();
            Snackbar.make(findViewById(android.R.id.content), "Operation interrupted! Please try again", TOAST_LENGTH)
                    .show();
        }
    }

    private void readData(){
        try {
            writeSendLog(cmd_readI2CMasterCommand);
            byte[] response2 = sendCommand(cmd_readI2CMasterCommand);
            writeReceiveLog(response2);

            writeSendLog(cmd_readSRAMI2CMaster);
            byte[] responseSRAM = sendCommand(cmd_readSRAMI2CMaster);
            writeReceiveLog(responseSRAM);

            Snackbar.make(findViewById(android.R.id.content), "Command correctly sent", TOAST_LENGTH)
                    .show();

            //Check if the SRAM has been read to show the result in the textResultSensor.
            //In case there is nothing to show just hide the view.
            textResultSensor.setText(Utils.byteArrayToHex(responseSRAM)
                    .substring(2)); //Remove first byte (00) because is the success code of vicinity command, not user data.
            textResultSensor.setVisibility(View.VISIBLE);
            dataReadTitle.setVisibility(View.VISIBLE);

            textLog.setText(logTextI2CMaster.toString());
        }catch (Exception e) {
            e.printStackTrace();
            Snackbar.make(findViewById(android.R.id.content), "Operation interrupted! Please try again", TOAST_LENGTH)
                    .show();
        }
    }

    //This method is used to add the NFC command to the communication log view
    private void writeSendLog(byte[] command){
        logTextI2CMaster = new StringBuilder();
        logTextI2CMaster.append("NFC -> ").append(Utils.byteArrayToHex(command));
        writeLogFile("MasterChannel-Logs", logTextI2CMaster.toString());
        logTextI2CMaster.append(System.getProperty("line.separator"));
        writeLogFile("MasterChannel-Logs", logTextI2CMaster.toString());
    }

    //This method is used to add the NFC response to the communication log view
    private void writeReceiveLog(byte[] response){
        if(response != null) {
            logTextI2CMaster.append("TAG <- ").append(Utils.byteArrayToHex(response));
            writeLogFile("MasterChannel-Logs", logTextI2CMaster.toString());
            logTextI2CMaster.append(System.getProperty("line.separator"));
            writeLogFile("MasterChannel-Logs", logTextI2CMaster.toString());

        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (tag.getTechList()[0].equals("android.nfc.tech.NfcV")) {
            configSensorI2CMaster();
        }
    }
}
