package nxp.activentag5i2c.utils;

public class Constants {

    public static final String ARGUMENT_ACTION_READ_GPIOINPUT = "com.nxp.stiwearable.READGPIOINPUT";

    public enum UseCase {
        I2C_SLAVE, I2C_MASTER, PWM_GPIO, HOST_INTERFACES_DISABLED, NOT_DETECTED
    }

    //PWM
    public static final String[] PRESCALAR = {"00b", "01b", "10b", "11b"};
    public static final String[] RESOLUTION = {"6-bit", "8-bit", "10-bit", "12-bit"};
    public static final String[] PWM_FREQUENCY = {"413 Hz", "206 Hz", "103 Hz", "52 Hz",
            "1.7 KHz", "825 Hz", "412.6 Hz", "206,2 Hz",
            "6.6 KHz", "3.3 KHz", "1.7 KHz", "825 Hz",
            "26.4 KHz", "13.2 KHz", "6.6 KHz", "3.3 KHz"};

    //GPIO
    public static final String[] SLEW_RATE = {"Low Speed GPIO (0b)", "High Speed GPIO (1b)"};
    public static final String[] PAD_CONFIG = {"Receiver disabled (00b)", "Plain input with weak pull-up (01b)",
            "Plain input (10b)", " Plain input with weak pull-down (11b)"};

    //I2C MASTER
    public static final String[] DEFAULT_COMMANDS = {"Configure sensor", "Get Temperature",
            "Get Accelerometer data X-axis", "Get Accelerometer data Y-axis", "Get Accelerometer data Z-axis",
            "Get Magnetometer data X-axis", "Get Magnetometer data Y-axis", "Get Magnetometer data Z-axis"};

    public enum I2CMASTER_COMMANDS {
        CONFIG_SENSOR, GET_TEMP, GET_ACCEL_X, GET_ACCEL_Y, GET_ACCEL_Z,
        GET_MAGNETO_X, GET_MAGNETO_Y, GET_MAGNETO_Z
    }

    //PASS-THOUGH
    public enum PassThroughDirection {
        RF_I2C, I2C_RF
    }

    //ALM
    public static final String[] RESISTOR = {"High Impedance", "300 Ohm", "1 kOhm", "2.2 kOhm"};
    public static final String[] FIELD_THRESHOLD = {"35 mV","44 mV", "53 mV", "62 mV", "71 mV", "80 mV", "89 mV", "98 mV"};
    public static final String[] TUNING_TOPOLOGY = {"000"};
    public static final String[] ENABLE_RESISTOR = {"Resistor disabled", "Resistor enabled during ramp"};


    public static final int SRAM_WRITE_SIZE = 240;
    public static final int SRAM_READ_SIZE = 252;
    public static int SRAM_LOOP_SIZE = 5;

    // Toast length
    public static final int TOAST_LENGTH = 6000;

}
