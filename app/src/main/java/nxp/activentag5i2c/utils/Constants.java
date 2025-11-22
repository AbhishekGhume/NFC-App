package nxp.activentag5i2c.utils;

public class Constants {

    public static final String ARGUMENT_ACTION_READ_GPIOINPUT = "com.nxp.stiwearable.READGPIOINPUT";

    public enum UseCase {
        I2C_SLAVE, I2C_MASTER, PWM_GPIO, HOST_INTERFACES_DISABLED, NOT_DETECTED
    }

    //PASS-THOUGH
    public enum PassThroughDirection {
        RF_I2C, I2C_RF
    }

    public static final int SRAM_WRITE_SIZE = 4;
    public static final int SRAM_READ_SIZE = 252;
    public static int SRAM_LOOP_SIZE = 2;

    // Toast length
    public static final int TOAST_LENGTH = 6000;

}