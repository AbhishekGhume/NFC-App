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
        final MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.actionbar, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.menu_setI2CSlave) {
            new AlertDialog.Builder(this)
                    .setTitle(getResources().getString(R.string.title_tag_mode_change))
                    .setMessage(getResources().getString(R.string.message_tag_mode_change))
                    .setPositiveButton(getResources().getString(R.string.tag_mode_change_yes),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    byte[] response = sendCommand(cmd_activateI2CSlave);
                                    if (response != null)
                                        Snackbar.make(findViewById(android.R.id.content), "Tag correctly configured, " +
                                                        "please reset the tag to set the new configuration", TOAST_LENGTH)
                                                .show();
                                }
                            })
                    .setNegativeButton(getResources().getString(R.string.tag_mode_change_no),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    dialog.dismiss();
                                }
                            }).show();
            return true;
        } else if (itemId == R.id.menu_setI2CMaster) {
            new AlertDialog.Builder(this)
                    .setTitle(getResources().getString(R.string.title_tag_mode_change))
                    .setMessage(getResources().getString(R.string.message_tag_mode_change))
                    .setPositiveButton(getResources().getString(R.string.tag_mode_change_yes),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    byte[] response = sendCommand(cmd_activateI2CMaster);
                                    if (response != null)
                                        Snackbar.make(findViewById(android.R.id.content), "Tag correctly configured, " +
                                                        "please reset the tag to set the new configuration", TOAST_LENGTH)
                                                .show();
                                }
                            })
                    .setNegativeButton(getResources().getString(R.string.tag_mode_change_no),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    dialog.dismiss();
                                }
                            }).show();
            return true;
        } else if (itemId == R.id.menu_setGPIOPWM) {
            new AlertDialog.Builder(this)
                    .setTitle(getResources().getString(R.string.title_tag_mode_change))
                    .setMessage(getResources().getString(R.string.message_tag_mode_change))
                    .setPositiveButton(getResources().getString(R.string.tag_mode_change_yes),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    byte[] response = sendCommand(cmd_activateGPIOPWM);
                                    if (response != null)
                                        Snackbar.make(findViewById(android.R.id.content), "Tag correctly configured, " +
                                                        "please reset the tag to set the new configuration", TOAST_LENGTH)
                                                .show();
                                }
                            })
                    .setNegativeButton(getResources().getString(R.string.tag_mode_change_no),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    dialog.dismiss();
                                }
                            }).show();
            return true;
        } else if (itemId == R.id.menu_getUseCaseConfig) {
            Constants.UseCase useCaseConfig = getUseCaseConfig();

            if (useCaseConfig == Constants.UseCase.I2C_SLAVE) {
                Snackbar.make(findViewById(android.R.id.content),
                        "I2C slave configuration", TOAST_LENGTH).show();
            } else if (useCaseConfig == Constants.UseCase.I2C_MASTER) {
                Snackbar.make(findViewById(android.R.id.content),
                        "I2C master configuration", TOAST_LENGTH).show();
            } else if (useCaseConfig == Constants.UseCase.PWM_GPIO) {
                Snackbar.make(findViewById(android.R.id.content),
                        "GPIO/PWM configuration", TOAST_LENGTH).show();
            } else if (useCaseConfig == Constants.UseCase.HOST_INTERFACES_DISABLED) {
                Snackbar.make(findViewById(android.R.id.content),
                        "Host interfaces disabled configuration",
                        TOAST_LENGTH).show();
            } else {
                Snackbar.make(findViewById(android.R.id.content),
                        "Could not read the configuration",
                        TOAST_LENGTH).show();
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public Constants.UseCase getUseCaseConfig() {
        Constants.UseCase resp;

        try {
            resp = parseUseCaseConfig(sendCommand(cmd_readTagConfig));
        } catch (Exception e){
            e.printStackTrace();
            resp = Constants.UseCase.NOT_DETECTED; // Set to default mode
            Snackbar.make(findViewById(android.R.id.content),
                    "There has been an error, please try again",
                    TOAST_LENGTH).show();
        }

        return resp;
    }

}