package nxp.activentag5i2c.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import nxp.activentag5i2c.models.ALMConfiguration;
import nxp.activentag5i2c.models.GPIOConfiguration;
import nxp.activentag5i2c.models.PWMConfiguration;
import nxp.activentag5i2c.nfc.RFCommands;
import nxp.activentag5i2c.utils.Constants.UseCase;

public class Parser {
    /*******************************************************************************************
     * General parsers
     ******************************************************************************************/
    public static UseCase parseUseCaseConfig(byte[] tagConfigResponse) {
        UseCase useCase = null;

        if ((tagConfigResponse[2] & 0x30) == 0x00) {
            useCase = UseCase.I2C_SLAVE;
        } else if ((tagConfigResponse[2] & 0x30) == 0x10) {
            useCase = UseCase.I2C_MASTER;
        } else if ((tagConfigResponse[2] & 0x30) == 0x20) {
            useCase = UseCase.PWM_GPIO;
        } else if ((tagConfigResponse[2] & 0x30) == 0x30) {
            useCase = UseCase.HOST_INTERFACES_DISABLED;
        }

        return useCase;
    }

    public static boolean IsBitSet(byte value, int index )
    {
        int mask = 1<<index;
        return (value & mask) != 0;
    }

    /*******************************************************************************************
     * GPIO parsers
     ******************************************************************************************/
    public static List<GPIOConfiguration> parseGPIORead(List<GPIOConfiguration> gpioConfigurationList
            , byte[] gpioConfig, byte[] tagConfig) {
        if (gpioConfig[1] == 0x00) {
            gpioConfigurationList.get(0).setDirection(0);
            gpioConfigurationList.get(1).setDirection(0);
        } else if (gpioConfig[1] == 0x04) {
            gpioConfigurationList.get(0).setDirection(1);
            gpioConfigurationList.get(1).setDirection(0);
        } else if (gpioConfig[1] == 0x08) {
            gpioConfigurationList.get(0).setDirection(0);
            gpioConfigurationList.get(1).setDirection(1);
        } else if (gpioConfig[1] == 0x0C) {
            gpioConfigurationList.get(0).setDirection(1);
            gpioConfigurationList.get(1).setDirection(1);
        }

        if ((tagConfig[3] & 0xC0) == 0x00) {
            gpioConfigurationList.get(1).setPadConfiguration(0);
        } else if ((tagConfig[3] & 0xC0) == 0x40) {
            gpioConfigurationList.get(1).setPadConfiguration(1);
        } else if ((tagConfig[3] & 0xC0) == 0x80) {
            gpioConfigurationList.get(1).setPadConfiguration(2);
        } else if ((tagConfig[3] & 0xC0) == 0xC0) {
            gpioConfigurationList.get(1).setPadConfiguration(3);
        }

        if ((tagConfig[3] & 0x30) == 0x00) {
            gpioConfigurationList.get(0).setPadConfiguration(0);
        } else if ((tagConfig[3] & 0x30) == 0x10) {
            gpioConfigurationList.get(0).setPadConfiguration(1);
        } else if ((tagConfig[3] & 0x30) == 0x20) {
            gpioConfigurationList.get(0).setPadConfiguration(2);
        } else if ((tagConfig[3] & 0x30) == 0x30) {
            gpioConfigurationList.get(0).setPadConfiguration(3);
        }

        if ((tagConfig[3] & 0x03) == 0x00) {
            gpioConfigurationList.get(0).setSlewRate(0);
            gpioConfigurationList.get(1).setSlewRate(0);
        } else if ((tagConfig[3] & 0x03) == 0x01) {
            gpioConfigurationList.get(0).setSlewRate(1);
            gpioConfigurationList.get(1).setSlewRate(0);
        } else if ((tagConfig[3] & 0x03) == 0x02) {
            gpioConfigurationList.get(0).setSlewRate(0);
            gpioConfigurationList.get(1).setSlewRate(1);
        } else if ((tagConfig[3] & 0x03) == 0x03) {
            gpioConfigurationList.get(0).setSlewRate(1);
            gpioConfigurationList.get(1).setSlewRate(1);
        }

        return gpioConfigurationList;
    }

    private static byte[] getTagConfigGPIOCommand(List<GPIOConfiguration> gpioConfigurationList) {
        byte[] cmd = new byte[]{(byte) 0x00, (byte) 0x20, (byte) 0x08, (byte) 0x00};

        if (gpioConfigurationList.get(0).getSlewRate() == 1) {
            cmd[2] |= 0x01;
        }

        if (gpioConfigurationList.get(1).getSlewRate() == 1) {
            cmd[2] |= 0x02;
        }

        if (gpioConfigurationList.get(0).getPadConfiguration() == 0) {
            cmd[2] |= 0x00;
        } else if (gpioConfigurationList.get(0).getPadConfiguration() == 1) {
            cmd[2] |= 0x10;
        } else if (gpioConfigurationList.get(0).getPadConfiguration() == 2) {
            cmd[2] |= 0x20;
        } else if (gpioConfigurationList.get(0).getPadConfiguration() == 3) {
            cmd[2] |= 0x30;
        }

        if (gpioConfigurationList.get(1).getPadConfiguration() == 0) {
            cmd[2] |= 0x00;
        } else if (gpioConfigurationList.get(1).getPadConfiguration() == 1) {
            cmd[2] |= 0x40;
        } else if (gpioConfigurationList.get(1).getPadConfiguration() == 2) {
            cmd[2] |= 0x80;
        } else if (gpioConfigurationList.get(1).getPadConfiguration() == 3) {
            cmd[2] |= 0xC0;
        }

        return cmd;
    }

    public static boolean parseGPIOInput(byte[] receivedCommand) {
        boolean status = false;

        if ((receivedCommand[2] & 0x10) == 0x10) {
            status = true;
        }

        return status;
    }

    public static byte[] getTagConfig(List<GPIOConfiguration> gpioConfigurations) {
        //CMD write GPIO
        byte[] cmdWriteGPIO = Parser.getTagConfigGPIOCommand(gpioConfigurations);
        byte[] finalCommand = RFCommands.cmd_writeTagConfig;

        ByteArrayOutputStream copyTagConfig = new ByteArrayOutputStream();
        try {
            copyTagConfig.write(finalCommand);
            copyTagConfig.write(cmdWriteGPIO);
        } catch (IOException e) {
            e.printStackTrace();
        }
        finalCommand = copyTagConfig.toByteArray();

        return finalCommand;
    }

    public static byte[] getTagSession(List<GPIOConfiguration> gpioConfigurations) {
        //CMD write GPIO
        byte[] cmdWriteGPIO = Parser.getTagConfigGPIOCommand(gpioConfigurations);
        byte[] finalCommand = RFCommands.cmd_writeTagConfigSession;

        ByteArrayOutputStream copyTagConfig = new ByteArrayOutputStream();
        try {
            copyTagConfig.write(finalCommand);
            copyTagConfig.write(cmdWriteGPIO);
        } catch (IOException e) {
            e.printStackTrace();
        }
        finalCommand = copyTagConfig.toByteArray();

        return finalCommand;
    }

    /*******************************************************************************************
     * PWM parsers
     ******************************************************************************************/
    public static List<PWMConfiguration> parsePWMRead(List<PWMConfiguration> pwmConfigurationList
            , byte[] pwmConfig, byte[] pwm0DutyCycle, byte[] pwm1DutyCycle) {
        //It is needed to keep the real resolution of the channel for the start time and duty cycle calculation.
        int realResolutionChannel0 = 6, realResolutionChannel1 = 6;

        if ((pwmConfig[2] & 0xC0) == 0x00) {
            pwmConfigurationList.get(1).setPrescalar(0);
        } else if ((pwmConfig[2] & 0xC0) == 0x40) {
            pwmConfigurationList.get(1).setPrescalar(1);
        } else if ((pwmConfig[2] & 0xC0) == 0x80) {
            pwmConfigurationList.get(1).setPrescalar(2);
        } else if ((pwmConfig[2] & 0xC0) == 0xC0) {
            pwmConfigurationList.get(1).setPrescalar(3);
        }

        if ((pwmConfig[2] & 0x30) == 0x00) {
            pwmConfigurationList.get(0).setPrescalar(0);
        } else if ((pwmConfig[2] & 0x30) == 0x10) {
            pwmConfigurationList.get(0).setPrescalar(1);
        } else if ((pwmConfig[2] & 0x30) == 0x20) {
            pwmConfigurationList.get(0).setPrescalar(2);
        } else if ((pwmConfig[2] & 0x30) == 0x30) {
            pwmConfigurationList.get(0).setPrescalar(3);
        }

        if ((pwmConfig[2] & 0x0C) == 0x00) {
            pwmConfigurationList.get(1).setResolution(0);
            realResolutionChannel1 = 6;
        } else if ((pwmConfig[2] & 0x0C) == 0x04) {
            pwmConfigurationList.get(1).setResolution(1);
            realResolutionChannel1 = 8;
        } else if ((pwmConfig[2] & 0x0C) == 0x08) {
            pwmConfigurationList.get(1).setResolution(2);
            realResolutionChannel1 = 10;
        } else if ((pwmConfig[2] & 0x0C) == 0x0C) {
            pwmConfigurationList.get(1).setResolution(3);
            realResolutionChannel1 = 12;
        }

        if ((pwmConfig[2] & 0x03) == 0x00) {
            pwmConfigurationList.get(0).setResolution(0);
            realResolutionChannel0 = 6;
        } else if ((pwmConfig[2] & 0x03) == 0x01) {
            pwmConfigurationList.get(0).setResolution(1);
            realResolutionChannel0 = 8;
        } else if ((pwmConfig[2] & 0x03) == 0x02) {
            pwmConfigurationList.get(0).setResolution(2);
            realResolutionChannel0 = 10;
        } else if ((pwmConfig[2] & 0x03) == 0x03) {
            pwmConfigurationList.get(0).setResolution(3);
            realResolutionChannel0 = 12;
        }

        //For a better understanding of the parsing of PWM Start time and duty cycle it is needed to
        //check the product datasheet, in the PWM section you can find an example of PWM configuration.
        byte[] tmp_pwm0_on = {pwm0DutyCycle[2], pwm0DutyCycle[1]}; //Swap bytes of the configuration (MSB first)
                                                                    //for further operations.
        byte[] tmp_pwm0_off = {pwm0DutyCycle[4], pwm0DutyCycle[3]};
        byte[] tmp_pwm1_on = {pwm1DutyCycle[2], pwm1DutyCycle[1]};
        byte[] tmp_pwm1_off = {pwm1DutyCycle[4], pwm1DutyCycle[3]};

        ByteBuffer wrapped_pwm0_on = ByteBuffer.wrap(tmp_pwm0_on); // Wrap method takes big-endian by default
        short num_pwm0_on = wrapped_pwm0_on.getShort();

        ByteBuffer wrapped_pwm0_off = ByteBuffer.wrap(tmp_pwm0_off);
        short num_pwm0_off = wrapped_pwm0_off.getShort();

        ByteBuffer wrapped_pwm1_on = ByteBuffer.wrap(tmp_pwm1_on);
        short num_pwm1_on = wrapped_pwm1_on.getShort();

        ByteBuffer wrapped_pwm1_off = ByteBuffer.wrap(tmp_pwm1_off);
        short num_pwm1_off = wrapped_pwm1_off.getShort();

        pwmConfigurationList.get(0).
                setStartTime((int) (Math.ceil(
                        (num_pwm0_on / Math.pow(2, realResolutionChannel0)) * 10)));

        pwmConfigurationList.get(1).
                setStartTime((int) (Math.ceil(
                        (num_pwm1_on / Math.pow(2, realResolutionChannel1)) * 10)));

        num_pwm0_off -= num_pwm0_on;
        num_pwm1_off -= num_pwm1_on;

        pwmConfigurationList.get(0).
                setDutyCycle((int) (Math.ceil(
                        (num_pwm0_off / Math.pow(2, realResolutionChannel0)) * 10)));

        pwmConfigurationList.get(1).
                setDutyCycle((int) (Math.ceil(
                        (num_pwm1_off / Math.pow(2, realResolutionChannel1)) * 10)));

        return pwmConfigurationList;
    }

    private static int translateResolution(int input) {
        int res = 6;

        switch (input) {
            case 0:
                res = 6;
                break;
            case 1:
                res = 8;
                break;
            case 2:
                res = 10;
                break;
            case 3:
                res = 12;
                break;
        }
        return res;
    }

    private static byte[] getTagConfigPWMCommand(List<PWMConfiguration> pwmConfigurationList) {
        byte[] cmd = new byte[]{(byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00};

        if (pwmConfigurationList.get(0).getPrescalar() == 0) {
            cmd[1] |= 0x00;
        } else if (pwmConfigurationList.get(0).getPrescalar() == 1) {
            cmd[1] |= 0x10;
        } else if (pwmConfigurationList.get(0).getPrescalar() == 2) {
            cmd[1] |= 0x20;
        } else if (pwmConfigurationList.get(0).getPrescalar() == 3) {
            cmd[1] |= 0x30;
        }

        if (pwmConfigurationList.get(1).getPrescalar() == 0) {
            cmd[1] |= 0x00;
        } else if (pwmConfigurationList.get(1).getPrescalar() == 1) {
            cmd[1] |= 0x40;
        } else if (pwmConfigurationList.get(1).getPrescalar() == 2) {
            cmd[1] |= 0x80;
        } else if (pwmConfigurationList.get(1).getPrescalar() == 3) {
            cmd[1] |= 0xC0;
        }

        if (pwmConfigurationList.get(0).getResolution() == 0) {
            cmd[1] |= 0x00;
        } else if (pwmConfigurationList.get(0).getResolution() == 1) {
            cmd[1] |= 0x01;
        } else if (pwmConfigurationList.get(0).getResolution() == 2) {
            cmd[1] |= 0x02;
        } else if (pwmConfigurationList.get(0).getResolution() == 3) {
            cmd[1] |= 0x03;
        }

        if (pwmConfigurationList.get(1).getResolution() == 0) {
            cmd[1] |= 0x00;
        } else if (pwmConfigurationList.get(1).getResolution() == 1) {
            cmd[1] |= 0x04;
        } else if (pwmConfigurationList.get(1).getResolution() == 2) {
            cmd[1] |= 0x08;
        } else if (pwmConfigurationList.get(1).getResolution() == 3) {
            cmd[1] |= 0x0C;
        }

        return cmd;
    }

    private static byte[] setPWM0Registers(List<PWMConfiguration> pwmConfigurationList) {
        //Get PWM0_ON
        double pwm0_on = (Math.ceil(Math.pow(2, translateResolution(pwmConfigurationList.get(0).getResolution())))) *
                (pwmConfigurationList.get(0).getStartTime() / (double) 10);

        byte[] pwm0_on_final = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort((short) pwm0_on).array();

        //Get PWM0_OFF
        double pwm0_off = (Math.ceil(Math.pow(2, translateResolution(pwmConfigurationList.get(0).getResolution())))) *
                (pwmConfigurationList.get(0).getDutyCycle() / (double) 10);

        pwm0_off += pwm0_on;

        byte[] pwm0_off_final = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort((short) pwm0_off).array();

        byte[] cmd = new byte[pwm0_on_final.length + pwm0_off_final.length];
        System.arraycopy(pwm0_on_final, 0, cmd, 0, pwm0_on_final.length);
        System.arraycopy(pwm0_off_final, 0, cmd, pwm0_on_final.length, pwm0_off_final.length);

        return cmd;
    }

    private static byte[] setPWM1Registers(List<PWMConfiguration> pwmConfigurationList) {
        //Get PWM0_ON
        double pwm1_on = (Math.ceil(Math.pow(2, translateResolution(pwmConfigurationList.get(1).getResolution())))) *
                (pwmConfigurationList.get(1).getStartTime() / (double) 10);

        byte[] pwm1_on_final = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort((short) pwm1_on).array();

        //Get PWM0_OFF
        double pwm1_off = (Math.ceil(Math.pow(2, translateResolution(pwmConfigurationList.get(1).getResolution())))) *
                (pwmConfigurationList.get(1).getDutyCycle() / (double) 10);

        pwm1_off += pwm1_on;

        byte[] pwm1_off_final = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort((short) pwm1_off).array();

        byte[] cmd = new byte[pwm1_on_final.length + pwm1_off_final.length];
        System.arraycopy(pwm1_on_final, 0, cmd, 0, pwm1_on_final.length);
        System.arraycopy(pwm1_off_final, 0, cmd, pwm1_on_final.length, pwm1_off_final.length);

        return cmd;
    }

//    public static byte[] getWriteConfigPWM(List<PWMConfiguration> pwmConfigurationList) {
//        //CMD write PWM
//        byte[] cmdWritePWM = Parser.getTagConfigPWMCommand(pwmConfigurationList);
//        byte[] finalCommand = RFCommands.cmd_writePWMConfig;
//
//        ByteArrayOutputStream copyTagConfig = new ByteArrayOutputStream();
//        try {
//            copyTagConfig.write(finalCommand);
//            copyTagConfig.write(cmdWritePWM);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        finalCommand = copyTagConfig.toByteArray();
//
//        return finalCommand;
//    }

    public static byte[] getSessionPWMCommand(List<PWMConfiguration> pwmConfigurationList) {
        //CMD write GPIO
        byte[] cmdWritePWM = Parser.getTagConfigPWMCommand(pwmConfigurationList);
        byte[] finalCommand = RFCommands.cmd_writePWMSession;

        ByteArrayOutputStream copyTagConfig = new ByteArrayOutputStream();
        try {
            copyTagConfig.write(finalCommand);
            copyTagConfig.write(cmdWritePWM);
        } catch (IOException e) {
            e.printStackTrace();
        }
        finalCommand = copyTagConfig.toByteArray();

        return finalCommand;
    }

    public static byte[] getPWM0Command(List<PWMConfiguration> pwmConfigurationList) {
        //CMD write PWM
        byte[] cmdWritePWM0 = Parser.setPWM0Registers(pwmConfigurationList);
        byte[] finalCommand = RFCommands.cmd_writePWM0Reg;

        ByteArrayOutputStream copyTagConfig = new ByteArrayOutputStream();
        try {
            copyTagConfig.write(finalCommand);
            copyTagConfig.write(cmdWritePWM0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        finalCommand = copyTagConfig.toByteArray();

        return finalCommand;
    }

    public static byte[] getPWM1Command(List<PWMConfiguration> pwmConfigurationList) {
        //CMD write PWM
        byte[] cmdWritePWM1 = Parser.setPWM1Registers(pwmConfigurationList);
        byte[] finalCommand = RFCommands.cmd_writePWM1Reg;

        ByteArrayOutputStream copyTagConfig = new ByteArrayOutputStream();
        try {
            copyTagConfig.write(finalCommand);
            copyTagConfig.write(cmdWritePWM1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        finalCommand = copyTagConfig.toByteArray();

        return finalCommand;
    }

    /*******************************************************************************************
     * ALM parsers
     ******************************************************************************************/
    public static ALMConfiguration parseALMRead(ALMConfiguration almConfiguration
            , byte[] almConfig) {

        almConfiguration.setAlm_conf_00(almConfig[1]);
        almConfiguration.setAlm_conf_01(almConfig[2]);
        almConfiguration.setAlm_conf_02(almConfig[3]);
        almConfiguration.setAlm_conf_03(almConfig[4]);

        return almConfiguration;

    }


}
