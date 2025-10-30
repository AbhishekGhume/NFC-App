package nxp.activentag5i2c.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import nxp.activentag5i2c.R;
import nxp.activentag5i2c.utils.Constants;

import static nxp.activentag5i2c.nfc.RFCommands.cmd_activateGPIOPWM;
import static nxp.activentag5i2c.nfc.RFCommands.cmd_activateI2CMaster;
import static nxp.activentag5i2c.nfc.RFCommands.cmd_activateI2CSlave;
import static nxp.activentag5i2c.nfc.RFCommands.cmd_readTagConfig;
import static nxp.activentag5i2c.utils.Constants.TOAST_LENGTH;
import static nxp.activentag5i2c.utils.Parser.parseUseCaseConfig;

public class BaseActionBarActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // No menu needed for Pass-Through only
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public Constants.UseCase getUseCaseConfig() {
        Constants.UseCase resp;
        try {
            resp = parseUseCaseConfig(sendCommand(cmd_readTagConfig));
        } catch (Exception e){
            e.printStackTrace();
            resp = Constants.UseCase.NOT_DETECTED;
            Snackbar.make(findViewById(android.R.id.content),
                    "There has been an error, please try again",
                    TOAST_LENGTH).show();
        }
        return resp;
    }

}