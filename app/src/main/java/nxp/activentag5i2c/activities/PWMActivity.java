package nxp.activentag5i2c.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mobileknowledge.library.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import nxp.activentag5i2c.R;
import nxp.activentag5i2c.models.PWMConfiguration;
import nxp.activentag5i2c.utils.Parser;

import static nxp.activentag5i2c.nfc.RFCommands.cmd_readGPIOPWMConfig;
import static nxp.activentag5i2c.nfc.RFCommands.cmd_readPWM0Reg;
import static nxp.activentag5i2c.nfc.RFCommands.cmd_readPWM1Reg;
import static nxp.activentag5i2c.utils.Constants.PRESCALAR;
import static nxp.activentag5i2c.utils.Constants.RESOLUTION;
import static nxp.activentag5i2c.utils.Constants.TOAST_LENGTH;

public class PWMActivity extends MainActivity {
    private TextView pwmFrequencyChannel0, pwmFrequencyChannel1, dutyCycleTextChannel0, dutyCycleTextChannel1,
            startTimeTextChannel0, startTimeTextChannel1, textLog;
    private StringBuilder logTextPWM = new StringBuilder();
    private List<PWMConfiguration> pwmConfigurationList;
    private Spinner resolutionChannel0, resolutionChannel1, prescalarChannel0, prescalarChannel1;
    private SeekBar dutyCycleChannel0, dutyCycleChannel1, startTimeChannel0, startTimeChannel1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pwm);

        //Create lists to manage PWM and GPIO channels, default parameters.
        pwmConfigurationList = new ArrayList<>();
        pwmConfigurationList.add(new PWMConfiguration());
        pwmConfigurationList.add(new PWMConfiguration());

        LinearLayout linearLayoutButtons = findViewById(R.id.linearLayoutButtons);
        LinearLayout linearLayoutLog = findViewById(R.id.linearLayoutLog);

        Button buttonRead = linearLayoutButtons.findViewById(R.id.read_pwm_button);
        Button buttonWrite = linearLayoutButtons.findViewById(R.id.write_pwm_button);

        textLog = linearLayoutLog.findViewById(R.id.textLog);

        resolutionChannel0 = findViewById(R.id.spinner_resolution_0);
        resolutionChannel1 = findViewById(R.id.spinner_resolution_1);
        ArrayAdapter<String> adapterResolution = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, RESOLUTION);
        resolutionChannel0.setAdapter(adapterResolution);
        resolutionChannel1.setAdapter(adapterResolution);

        prescalarChannel0 = findViewById(R.id.spinner_prescalar_0);
        prescalarChannel1 = findViewById(R.id.spinner_prescalar_1);
        ArrayAdapter<String> adapterPrescalar = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, PRESCALAR);
        prescalarChannel0.setAdapter(adapterPrescalar);
        prescalarChannel1.setAdapter(adapterPrescalar);

        pwmFrequencyChannel0 = findViewById(R.id.final_frequency_0);
        pwmFrequencyChannel1 = findViewById(R.id.final_frequency_1);

        dutyCycleChannel0 = findViewById(R.id.seek_bar_duty_cycle_0);
        dutyCycleChannel1 = findViewById(R.id.seek_bar_duty_cycle_1);

        dutyCycleTextChannel0 = findViewById(R.id.duty_cycle_0);
        dutyCycleTextChannel1 = findViewById(R.id.duty_cycle_1);

        startTimeChannel0 = findViewById(R.id.seek_bar_start_time_0);
        startTimeChannel1 = findViewById(R.id.seek_bar_start_time_1);

        startTimeTextChannel0 = findViewById(R.id.start_time_0);
        startTimeTextChannel1 = findViewById(R.id.start_time_1);

        resolutionChannel0.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                switch (position) {
                    case 0:
                        pwmConfigurationList.get(0).setResolution(0);
                        break;
                    case 1:
                        pwmConfigurationList.get(0).setResolution(1);
                        break;
                    case 2:
                        pwmConfigurationList.get(0).setResolution(2);
                        break;
                    case 3:
                        pwmConfigurationList.get(0).setResolution(3);
                        break;
                }
                pwmConfigurationList.get(0).setPwmFrequency();
                pwmFrequencyChannel0.setText("PWM frequency: " + pwmConfigurationList.get(0).getPwmFrequency());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        resolutionChannel1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                switch (position) {
                    case 0:
                        pwmConfigurationList.get(1).setResolution(0);
                        break;
                    case 1:
                        pwmConfigurationList.get(1).setResolution(1);
                        break;
                    case 2:
                        pwmConfigurationList.get(1).setResolution(2);
                        break;
                    case 3:
                        pwmConfigurationList.get(1).setResolution(3);
                        break;
                }
                pwmConfigurationList.get(1).setPwmFrequency();
                pwmFrequencyChannel1.setText("PWM frequency: " + pwmConfigurationList.get(1).getPwmFrequency());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        prescalarChannel0.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                switch (position) {
                    case 0:
                        pwmConfigurationList.get(0).setPrescalar(0);
                        break;
                    case 1:
                        pwmConfigurationList.get(0).setPrescalar(1);
                        break;
                    case 2:
                        pwmConfigurationList.get(0).setPrescalar(2);
                        break;
                    case 3:
                        pwmConfigurationList.get(0).setPrescalar(3);
                        break;
                }
                pwmConfigurationList.get(0).setPwmFrequency();
                pwmFrequencyChannel0.setText("PWM frequency: " + pwmConfigurationList.get(0).getPwmFrequency());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        prescalarChannel1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                switch (position) {
                    case 0:
                        pwmConfigurationList.get(1).setPrescalar(0);
                        break;
                    case 1:
                        pwmConfigurationList.get(1).setPrescalar(1);
                        break;
                    case 2:
                        pwmConfigurationList.get(1).setPrescalar(2);
                        break;
                    case 3:
                        pwmConfigurationList.get(1).setPrescalar(3);
                        break;
                }
                pwmConfigurationList.get(1).setPwmFrequency();
                pwmFrequencyChannel1.setText("PWM frequency: " + pwmConfigurationList.get(1).getPwmFrequency());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        dutyCycleChannel0.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress > 9 - startTimeChannel0.getProgress()){
                    pwmConfigurationList.get(0).setDutyCycle(9 - startTimeChannel0.getProgress());
                    dutyCycleChannel0.setProgress(9 - startTimeChannel0.getProgress());
//                    Toast.makeText(getApplicationContext(),"Cant go past " + (9 - startTimeChannel0.getProgress())*10 + "%", Toast.LENGTH_LONG).show();
                }else{
                    pwmConfigurationList.get(0).setDutyCycle(progress);
                    dutyCycleTextChannel0.setText("Duty cycle: " + progress * 10 + "%");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        dutyCycleChannel1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress > 9 - startTimeChannel1.getProgress()){
                    pwmConfigurationList.get(1).setDutyCycle(9 - startTimeChannel1.getProgress());
                    dutyCycleChannel1.setProgress(9 - startTimeChannel1.getProgress());
                }else{
                    pwmConfigurationList.get(1).setDutyCycle(progress);
                    dutyCycleTextChannel1.setText("Duty cycle: " + progress * 10 + "%");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        startTimeChannel0.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                pwmConfigurationList.get(0).setStartTime(progress);
                startTimeTextChannel0.setText("Start time: " + progress * 10 + "%");
                dutyCycleChannel0.setProgress(9 - progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        startTimeChannel1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                pwmConfigurationList.get(1).setStartTime(progress);
                startTimeTextChannel1.setText("Start time: " + progress * 10 + "%");
                dutyCycleChannel1.setProgress(9 - progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        setUIElements();

        buttonRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readPWM();
            }
        });

        buttonWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writePWM();
            }
        });
    }

    /*******************************************************************************************
     * PWM specific methods
     ******************************************************************************************/
    private void readPWM() {
        try {
            writeSendLog(cmd_readGPIOPWMConfig);
            byte[] response = sendCommand(cmd_readGPIOPWMConfig);
            writeReceiveLog(response);

            writeSendLog(cmd_readPWM0Reg);
            byte[] response2 = sendCommand(cmd_readPWM0Reg);
            writeReceiveLog(response2);

            writeSendLog(cmd_readPWM1Reg);
            byte[] response3 = sendCommand(cmd_readPWM1Reg);
            writeReceiveLog(response3);

            Snackbar.make(findViewById(android.R.id.content), "Tag correctly read", TOAST_LENGTH)
                    .show();

            pwmConfigurationList = Parser.parsePWMRead(pwmConfigurationList, response,
                    response2, response3);

            setUIElements();
            textLog.setText(logTextPWM.toString());
        } catch (Exception e) {
            e.printStackTrace();
            Snackbar.make(findViewById(android.R.id.content), "Operation interrupted! Please try again", TOAST_LENGTH)
                    .show();
        }
    }

    private void writePWM() {
        byte[] writeSessionPWM = Parser.getSessionPWMCommand(pwmConfigurationList);
        byte[] pwm0Reg = Parser.getPWM0Command(pwmConfigurationList);
        byte[] pwm1Reg = Parser.getPWM1Command(pwmConfigurationList);

        try {
            writeSendLog(writeSessionPWM);
            byte[] response = sendCommand(writeSessionPWM);
            writeReceiveLog(response);

            writeSendLog(pwm0Reg);
            byte[] response2 = sendCommand(pwm0Reg);
            writeReceiveLog(response2);

            writeSendLog(pwm1Reg);
            byte[] response3 = sendCommand(pwm1Reg);
            writeReceiveLog(response3);

            Snackbar.make(findViewById(android.R.id.content),
                    "Tag correctly written", TOAST_LENGTH)
                    .show();

            textLog.setText(logTextPWM.toString());
        }catch (Exception e) {
            e.printStackTrace();
            Snackbar.make(findViewById(android.R.id.content), "Operation interrupted! Please try again", TOAST_LENGTH)
                    .show();
        }
    }

    //This method is used to add the NFC command to the communication log view
    private void writeSendLog(byte[] command) {
        logTextPWM = new StringBuilder();
        logTextPWM.append("NFC -> ").append(Utils.byteArrayToHex(command));
        writeLogFile("PWM-Logs", logTextPWM.toString());
        logTextPWM.append(System.getProperty("line.separator"));
        writeLogFile("PWM-Logs", logTextPWM.toString());
    }

    //This method is used to add the NFC response to the communication log view
    private void writeReceiveLog(byte[] response) {
        if(response != null) {
            logTextPWM.append("TAG <- ").append(Utils.byteArrayToHex(response));
            writeLogFile("PWM-Logs", logTextPWM.toString());
            logTextPWM.append(System.getProperty("line.separator"));
            writeLogFile("PWM-Logs", logTextPWM.toString());
        }
    }

    //Update UI elements at runtime with this method
    @SuppressLint("SetTextI18n")
    private void setUIElements() {
        resolutionChannel0.setSelection(pwmConfigurationList.get(0).getResolution());
        resolutionChannel1.setSelection(pwmConfigurationList.get(1).getResolution());
        prescalarChannel0.setSelection(pwmConfigurationList.get(0).getPrescalar());
        prescalarChannel1.setSelection(pwmConfigurationList.get(1).getPrescalar());
        dutyCycleChannel0.setProgress(pwmConfigurationList.get(0).getDutyCycle());
        dutyCycleChannel1.setProgress(pwmConfigurationList.get(1).getDutyCycle());
        dutyCycleTextChannel0.setText("Duty cycle: " + dutyCycleChannel0.getProgress() * 10 + "%");
        dutyCycleTextChannel1.setText("Duty cycle: " + dutyCycleChannel1.getProgress() * 10 + "%");
        startTimeTextChannel0.setText("Start time: " + startTimeChannel0.getProgress() * 10 + "%");
        startTimeTextChannel1.setText("Start time: " + startTimeChannel1.getProgress() * 10 + "%");
        startTimeChannel0.setProgress(pwmConfigurationList.get(0).getStartTime());
        startTimeChannel1.setProgress(pwmConfigurationList.get(1).getStartTime());
    }
}
