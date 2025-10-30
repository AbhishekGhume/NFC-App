package nxp.activentag5i2c.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.mobileknowledge.library.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import nxp.activentag5i2c.R;
import nxp.activentag5i2c.models.GPIOConfiguration;
import nxp.activentag5i2c.utils.Parser;

import static nxp.activentag5i2c.nfc.RFCommands.cmd_gpioClearSessionOutput;
import static nxp.activentag5i2c.nfc.RFCommands.cmd_gpioSetSessionOutput;
import static nxp.activentag5i2c.nfc.RFCommands.cmd_readGPIOPWMConfig;
import static nxp.activentag5i2c.nfc.RFCommands.cmd_readTagConfig;
import static nxp.activentag5i2c.nfc.RFCommands.cmd_readTagStatus;
import static nxp.activentag5i2c.nfc.RFCommands.cmd_writeGPIOConfig;
import static nxp.activentag5i2c.nfc.RFCommands.cmd_writeGPIOSession;
import static nxp.activentag5i2c.utils.Constants.PAD_CONFIG;
import static nxp.activentag5i2c.utils.Constants.SLEW_RATE;
import static nxp.activentag5i2c.utils.Constants.TOAST_LENGTH;

public class GPIOActivity extends MainActivity {
    private StringBuilder logTextGPIO = new StringBuilder();
    private TextView textLog;
    private List<GPIOConfiguration> gpioConfigurationList;
    private ImageView imageGPIO_1, imageGPIO_0;
    private Spinner slewRateChannel0, slewRateChannel1, padConfigChannel1, padConfigChannel0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpio);

        gpioConfigurationList = new ArrayList<>();
        gpioConfigurationList.add(new GPIOConfiguration(0));
        gpioConfigurationList.add(new GPIOConfiguration(1));

        LinearLayout linearLayoutButtons = findViewById(R.id.linearLayoutButtons);
        LinearLayout linearLayoutLog = findViewById(R.id.linearLayoutLog);
        LinearLayout linearLayoutChannel1 = findViewById(R.id.linearLayout_1);
        LinearLayout linearLayoutChannel0 = findViewById(R.id.linearLayout_0);

        Button buttonRead = linearLayoutButtons.findViewById(R.id.read_gpio);
        Button buttonWrite = linearLayoutButtons.findViewById(R.id.write_gpio);

        Button checkInput = linearLayoutChannel1.findViewById(R.id.checkGPIOInput);
        Button clearOutput = linearLayoutChannel0.findViewById(R.id.clearGPIOOutput);

        textLog = linearLayoutLog.findViewById(R.id.textLog);

        slewRateChannel0 = findViewById(R.id.spinner_slew_rate_0);
        slewRateChannel1 = findViewById(R.id.spinner_slew_rate_1);
        ArrayAdapter<String> adapterSlewRate = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, SLEW_RATE);
        slewRateChannel0.setAdapter(adapterSlewRate);
        slewRateChannel1.setAdapter(adapterSlewRate);

        padConfigChannel1 = findViewById(R.id.spinner_pad_config_1);
        ArrayAdapter<String> adapterPadConfig = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, PAD_CONFIG);
        padConfigChannel1.setAdapter(adapterPadConfig);

        padConfigChannel0 = findViewById(R.id.spinner_pad_config_0);
        padConfigChannel0.setAdapter(adapterPadConfig);

        imageGPIO_0 = findViewById(R.id.gpio_icon_0);
        imageGPIO_0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pushGPIOOutput();
            }
        });

        imageGPIO_1 = findViewById(R.id.gpio_icon_1);
        imageGPIO_0 = findViewById(R.id.gpio_icon_0);

        slewRateChannel0.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                switch (position) {
                    case 0:
                        gpioConfigurationList.get(0).setSlewRate(0);
                        break;
                    case 1:
                        gpioConfigurationList.get(0).setSlewRate(1);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        slewRateChannel1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                switch (position) {
                    case 0:
                        gpioConfigurationList.get(1).setSlewRate(0);
                        break;
                    case 1:
                        gpioConfigurationList.get(1).setSlewRate(1);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        padConfigChannel1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                switch (position) {
                    case 0:
                        gpioConfigurationList.get(1).setPadConfiguration(0);
                        break;
                    case 1:
                        gpioConfigurationList.get(1).setPadConfiguration(1);
                        break;
                    case 2:
                        gpioConfigurationList.get(1).setPadConfiguration(2);
                        break;
                    case 3:
                        gpioConfigurationList.get(1).setPadConfiguration(3);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        padConfigChannel0.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                switch (position) {
                    case 0:
                        gpioConfigurationList.get(0).setPadConfiguration(0);
                        break;
                    case 1:
                        gpioConfigurationList.get(0).setPadConfiguration(1);
                        break;
                    case 2:
                        gpioConfigurationList.get(0).setPadConfiguration(2);
                        break;
                    case 3:
                        gpioConfigurationList.get(0).setPadConfiguration(3);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        gpioConfigurationList.get(1).setPadConfiguration(2);
        gpioConfigurationList.get(1).setSlewRate(1);
        gpioConfigurationList.get(0).setSlewRate(1);

        setUIElements();

        buttonRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readGPIO();
            }
        });

        buttonWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeGPIO();
            }
        });

        checkInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getGPIOInput();
            }
        });

        clearOutput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearGPIOOutput();
            }
        });

    }

    private void readGPIO() {
        try {
            writeSendLog(cmd_readGPIOPWMConfig);
            byte[] response = sendCommand(cmd_readGPIOPWMConfig);
            writeReceiveLog(response);

            writeSendLog(cmd_readTagConfig);
            byte[] response2 = sendCommand(cmd_readTagConfig);
            writeReceiveLog(response2);

            Snackbar.make(findViewById(android.R.id.content), "Tag correctly read", TOAST_LENGTH)
                    .show();

            gpioConfigurationList = Parser.parseGPIORead(gpioConfigurationList,
                    response, response2);

            imageGPIO_1.setImageResource(R.drawable.unpressed_button);

            setUIElements();

            textLog.setText(logTextGPIO.toString());
        } catch (Exception e) {
            e.printStackTrace();
            Snackbar.make(findViewById(android.R.id.content), "Operation interrupted! Please try again", TOAST_LENGTH)
                    .show();
        }
    }

    private void getGPIOInput() {
        try {
            writeSendLog(cmd_readTagStatus);
            byte[] response = sendCommand(cmd_readTagStatus);
            String responseHEX = Utils.byteArrayToHex(response);
            writeReceiveLog(response);

            Snackbar.make(findViewById(android.R.id.content),
                    "Tag correctly read", TOAST_LENGTH)
                    .show();

            boolean gpioInput = Parser.parseGPIOInput(response);

            // Get value of the gpio status passed as bundle and load image of
            // the corresponding button
            if (gpioInput) {
                imageGPIO_1.setImageResource(R.drawable.pressed_button);
            } else {
                imageGPIO_1.setImageResource(R.drawable.unpressed_button);
            }
            textLog.setText(logTextGPIO.toString());
        } catch (Exception e) {
            e.printStackTrace();
            Snackbar.make(findViewById(android.R.id.content), "Operation interrupted! Please try again", TOAST_LENGTH)
                    .show();
        }
    }

    private void clearGPIOOutput() {
        try {
            writeSendLog(cmd_gpioClearSessionOutput);
            byte[] response = sendCommand(cmd_gpioClearSessionOutput);
            writeReceiveLog(response);

            Snackbar.make(findViewById(android.R.id.content),
                    "Register correctly cleared", TOAST_LENGTH)
                    .show();

            imageGPIO_0.setImageResource(R.drawable.light_off);
            textLog.setText(logTextGPIO.toString());
        } catch (Exception e) {
            e.printStackTrace();
            Snackbar.make(findViewById(android.R.id.content), "Operation interrupted! Please try again", TOAST_LENGTH)
                    .show();
        }
    }

    private void pushGPIOOutput() {
        try {
            writeSendLog(cmd_gpioSetSessionOutput);
            byte[] response = sendCommand(cmd_gpioSetSessionOutput);
            writeReceiveLog(response);

            Snackbar.make(findViewById(android.R.id.content),
                    "Register correctly set", TOAST_LENGTH)
                    .show();

            imageGPIO_0.setImageResource(R.drawable.light_on);

            textLog.setText(logTextGPIO.toString());
        } catch (Exception e) {
            e.printStackTrace();
            Snackbar.make(findViewById(android.R.id.content), "Operation interrupted! Please try again", TOAST_LENGTH)
                    .show();
        }
    }

    private void writeGPIO() {
        byte[] tagConfig = Parser.getTagConfig(gpioConfigurationList);
        byte[] tagSession = Parser.getTagSession(gpioConfigurationList);

        try {
            writeSendLog(cmd_writeGPIOConfig);
            byte[] response = sendCommand(cmd_writeGPIOConfig);
            writeReceiveLog(response);

            writeSendLog(cmd_writeGPIOSession);
            byte[] response2 = sendCommand(cmd_writeGPIOSession);
            writeReceiveLog(response2);

            writeSendLog(tagConfig);
            byte[] response3 = sendCommand(tagConfig);
            writeReceiveLog(response3);

            writeSendLog(tagSession);
            byte[] response4 = sendCommand(tagSession);
            writeReceiveLog(response4);

            Snackbar.make(findViewById(android.R.id.content),
                    "Tag correctly written", TOAST_LENGTH)
                    .show();

            textLog.setText(logTextGPIO.toString());
        } catch (Exception e) {
            e.printStackTrace();
            Snackbar.make(findViewById(android.R.id.content), "Operation interrupted! Please try again", TOAST_LENGTH)
                    .show();
        }
    }

    private void writeAtNFCEvent() {
        byte[] tagConfig = Parser.getTagConfig(gpioConfigurationList);
        byte[] tagSession = Parser.getTagSession(gpioConfigurationList);

        try {
            byte[] response = sendCommand(cmd_writeGPIOConfig);

            byte[] response2 = sendCommand(cmd_writeGPIOSession);

            byte[] response3 = sendCommand(tagConfig);

            byte[] response4 = sendCommand(tagSession);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //This method is used to add the NFC command to the communication log view
    private void writeSendLog(byte[] command) {
        logTextGPIO = new StringBuilder();
        logTextGPIO.append("NFC -> ").append(Utils.byteArrayToHex(command));
        writeLogFile("GPIO-Logs", logTextGPIO.toString());
        logTextGPIO.append(System.getProperty("line.separator"));
        writeLogFile("GPIO-Logs", logTextGPIO.toString());
    }

    //This method is used to add the NFC response to the communication log view
    private void writeReceiveLog(byte[] response) {
        if(response != null) {
            logTextGPIO.append("TAG <- ").append(Utils.byteArrayToHex(response));
            writeLogFile("GPIO-Logs", logTextGPIO.toString());
            logTextGPIO.append(System.getProperty("line.separator"));
            writeLogFile("GPIO-Logs", logTextGPIO.toString());
        }
    }

    //Update UI elements at runtime with this method
    private void setUIElements() {
        slewRateChannel0.setSelection(gpioConfigurationList.get(0).getSlewRate());
        slewRateChannel1.setSelection(gpioConfigurationList.get(1).getSlewRate());
        padConfigChannel1.setSelection(gpioConfigurationList.get(1).getPadConfiguration());
        padConfigChannel0.setSelection(gpioConfigurationList.get(0).getPadConfiguration());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (tag.getTechList()[0].equals("android.nfc.tech.NfcV")) {
            writeAtNFCEvent();
        }
    }
}
