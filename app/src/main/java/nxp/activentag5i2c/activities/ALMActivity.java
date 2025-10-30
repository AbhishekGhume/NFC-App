package nxp.activentag5i2c.activities;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import nxp.activentag5i2c.R;
import nxp.activentag5i2c.models.ALMConfiguration;
import nxp.activentag5i2c.utils.Parser;

import static nxp.activentag5i2c.utils.Constants.ENABLE_RESISTOR;
import static nxp.activentag5i2c.utils.Constants.FIELD_THRESHOLD;
import static nxp.activentag5i2c.utils.Constants.RESISTOR;
import static nxp.activentag5i2c.utils.Constants.TOAST_LENGTH;
import static nxp.activentag5i2c.utils.Constants.TUNING_TOPOLOGY;
import static nxp.activentag5i2c.nfc.RFCommands.cmd_readALMConfiguration;

public class ALMActivity extends MainActivity{

    private Spinner rx_resistor, field_threshold, tun_topology, enable_res, tx_resistor;
    private ALMConfiguration almConfiguration;
    private RadioButton enable_phase_shift;
    private EditText static_phase_offset, damping_period, pll_delay, active_clamp_delay;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alm);

        almConfiguration = new ALMConfiguration();

        LinearLayout linearLayoutButtons = findViewById(R.id.alm_linearLayoutButtons);

        Button buttonRead = linearLayoutButtons.findViewById(R.id.read_alm);

        rx_resistor = findViewById(R.id.spinner_rx_res);
        tx_resistor = findViewById(R.id.spinner_tx_res);
        ArrayAdapter<String> adapterResistor = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, RESISTOR);
        rx_resistor.setAdapter(adapterResistor);
        tx_resistor.setAdapter(adapterResistor);

        field_threshold = findViewById(R.id.spinner_field_thresh);
        ArrayAdapter<String> adapterField = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, FIELD_THRESHOLD);
        field_threshold.setAdapter(adapterField);

        tun_topology = findViewById(R.id.spinner_tuning_top);
        ArrayAdapter<String> adapterTopology = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, TUNING_TOPOLOGY);
        tun_topology.setAdapter(adapterTopology);

        enable_res = findViewById(R.id.spinner_enable_res);
        ArrayAdapter<String> adapterEnablerRes = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, ENABLE_RESISTOR);
        enable_res.setAdapter(adapterEnablerRes);

        enable_phase_shift = findViewById(R.id.radio_enable_phase_shift);
        static_phase_offset = findViewById(R.id.phaseOffsetRead);
        damping_period = findViewById(R.id.dampingRead);
        pll_delay = findViewById(R.id.pll_delayRead);
        active_clamp_delay = findViewById(R.id.clampRead);

        buttonRead.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                readALM();
            }
        });
    }

    private void readALM() {
        try {
            byte[] response = sendCommand(cmd_readALMConfiguration);

            almConfiguration = Parser.parseALMRead(almConfiguration, response);

            updateUIElements();
        } catch (Exception e) {
            e.printStackTrace();
            Snackbar.make(findViewById(android.R.id.content), "Operation interrupted! " +
                    "Please try again", TOAST_LENGTH)
                    .show();
        }
    }

    private void updateUIElements() {
        rx_resistor.setSelection(almConfiguration.getrxResistorValue());
        tx_resistor.setSelection(almConfiguration.gettxResistorValue());
        field_threshold.setSelection(almConfiguration.getfieldThreshold());
        tun_topology.setSelection(almConfiguration.gettuningTopology());
        enable_res.setSelection(almConfiguration.getresistorEnabled());
        enable_phase_shift.setChecked(almConfiguration.getphaseShiftEnabled());
        static_phase_offset.setText(almConfiguration.getstaticPhaseOffset());
        damping_period.setText(almConfiguration.getdampingPeriod());
        pll_delay.setText(almConfiguration.getpllDelay());
        active_clamp_delay.setText(almConfiguration.getactiveClampDelay());
    }
}


    //byte[] response = new byte[] {
    //        (byte) 0xD8,
    //        (byte) 0x4B,
    //        (byte) 0x48,
    //        (byte) 0x12
    //};