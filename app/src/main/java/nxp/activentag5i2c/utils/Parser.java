package nxp.activentag5i2c.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

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

}
