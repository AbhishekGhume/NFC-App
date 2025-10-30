package nxp.activentag5i2c.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.Date;

import nxp.activentag5i2c.R;
import nxp.activentag5i2c.utils.Constants;

import static nxp.activentag5i2c.nfc.RFCommands.cmd_activateGPIOPWM;
import static nxp.activentag5i2c.nfc.RFCommands.cmd_activateI2CMaster;
import static nxp.activentag5i2c.nfc.RFCommands.cmd_activateI2CSlave;
import static nxp.activentag5i2c.nfc.RFCommands.cmd_readI2cAddr;
import static nxp.activentag5i2c.utils.Constants.TOAST_LENGTH;

public class MainActivity extends BaseActionBarActivity{

    public String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/aaTutorial";
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View view = getLayoutInflater().inflate(R.layout.abs_layout, null);
        ActionBar.LayoutParams params = new ActionBar.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);

        TextView Title = (TextView) view.findViewById(R.id.actionbar_title);

        getSupportActionBar().setCustomView(view,params);
        getSupportActionBar().setDisplayShowCustomEnabled(true); //show custom title
        getSupportActionBar().setDisplayShowTitleEnabled(false); //hide the default title

        //Buttons definition
        Button buttonPassThrough = findViewById(R.id.button_passthrough);
        buttonPassThrough.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, PassThroughActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });

        Button buttonPWM = findViewById(R.id.button_pwm);
        buttonPWM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, PWMActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });

        Button buttonGPIO = findViewById(R.id.button_gpio);
        buttonGPIO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, GPIOActivity.class);
                intent.putExtra(Constants.ARGUMENT_ACTION_READ_GPIOINPUT, false); //Set false as default configuration
                MainActivity.this.startActivity(intent);
            }
        });

        Button buttonI2CMaster = findViewById(R.id.button_i2c_master);
        buttonI2CMaster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, I2CMasterActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });

        Button buttonALM = findViewById(R.id.button_ALM);
        buttonALM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ALMActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });

        Button buttonSetI2cSlaveMode = findViewById(R.id.button_set_i2c_slave_mode);
        buttonSetI2cSlaveMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(getResources().getString(R.string.title_tag_mode_change))
                        .setMessage(getResources().getString(R.string.message_tag_mode_change))
                        .setPositiveButton(getResources().getString(R.string.tag_mode_change_yes),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        byte[] response = sendCommand(cmd_activateI2CSlave);
                                        byte[] i2cAddr = sendCommand(cmd_readI2cAddr);
                                        if (response != null && i2cAddr[1] == 0x54)
                                            Snackbar.make(findViewById(android.R.id.content), "Tag correctly configured, " +
                                                    "please reset the tag to set the new configuration", TOAST_LENGTH)
                                                    .show();
                                        else if(response != null && i2cAddr[1] != 0x54)
                                            Snackbar.make(findViewById(android.R.id.content), "NTAG5 I²C Slave Address is " + Integer.toHexString(i2cAddr[1]) + "h! Re-configure FW !", TOAST_LENGTH)
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
            }
        });

        Button buttonSetI2cMasterMode = findViewById(R.id.button_set_i2c_master_mode);
        buttonSetI2cMasterMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(MainActivity.this)
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
            }
        });

        Button buttonSetGpioPwmMode = findViewById(R.id.button_set_gpio_pwm_mode);
        buttonSetGpioPwmMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(MainActivity.this)
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
            }
        });

        // Create directory and txt files for logs
        mkFolder("NTAG5 I2C Logs");
        createLogFile("MasterChannel-Logs");
        createLogFile("GPIO-Logs");
        createLogFile("PWM-Logs");
        createLogFile("Pass-Through-Logs");

        Date currentTime = Calendar.getInstance().getTime();

        writeLogFile("MasterChannel-Logs", "\n" + currentTime.toString() + "\n\n");
        writeLogFile("GPIO-Logs", "\n" + currentTime.toString() + "\n\n");
        writeLogFile("PWM-Logs", "\n" + currentTime.toString() + "\n\n");
        writeLogFile("Pass-Through-Logs", "\n" + currentTime.toString() + "\n\n");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (tag.getTechList()[0].equals("android.nfc.tech.NfcV")) {
            switch (getUseCaseConfig()) {
                case I2C_SLAVE:
                    Snackbar.make(findViewById(android.R.id.content),
                            "I2C slave configuration", TOAST_LENGTH).show();
                    break;
                case I2C_MASTER:
                    Snackbar.make(findViewById(android.R.id.content),
                            "I2C master configuration", TOAST_LENGTH).show();
                    break;
                case PWM_GPIO:
                    Snackbar.make(findViewById(android.R.id.content),
                            "GPIO/PWM configuration", TOAST_LENGTH).show();
                    break;
                case HOST_INTERFACES_DISABLED:
                    Snackbar.make(findViewById(android.R.id.content),
                            "Host interfaces disabled configuration",
                            TOAST_LENGTH).show();
                    break;
                default:
                    Snackbar.make(findViewById(android.R.id.content),
                            "Could not read the configuration",
                            TOAST_LENGTH).show();
            }
        } else{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.dialog_tag_not_supported_title))
                    .setMessage(getResources().getString(R.string.dialog_tag_not_supported_msg))
                    .setPositiveButton(getResources().getString(R.string.dialog_tag_not_supported_btn),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    dialog.dismiss();
                                }
                            }).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public int mkFolder(String folderName){ // make a folder under Environment.DIRECTORY_DCIM
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)){
            Log.d("myAppName", "Error: external storage is unavailable");
            return 0;
        }
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            Log.d("myAppName", "Error: external storage is read only.");
            return 0;
        }
        Log.d("myAppName", "External storage is not read only or unavailable");

        if (ContextCompat.checkSelfPermission(this, // request permission when it is not granted.
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d("myAppName", "permission:WRITE_EXTERNAL_STORAGE: NOT granted!");
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),folderName);
        int result = 0;
        if (folder.exists()) {
            Log.d("myAppName","folder exist:"+folder.toString());
            result = 2; // folder exist
        }else{
            try {
                if (folder.mkdir()) {
                    Log.d("myAppName", "folder created:" + folder.toString());
                    result = 1; // folder created
                } else {
                    Log.d("myAppName", "creat folder fails:" + folder.toString());
                    result = 0; // creat folder fails
                }
            }catch (Exception ecp){
                ecp.printStackTrace();
            }
        }
        return result;
    }

    public void createLogFile(String filename){
        try {
            String rootPath = this.getExternalFilesDir(null) + "/"+ filename +".txt";
            File root = new File(rootPath);
            if (!root.exists()) {
                root.mkdirs();
            }
            File f = new File(rootPath);
            if (f.exists()) {
                f.delete();
            }
            f.createNewFile();

            FileOutputStream out = new FileOutputStream(f);

            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void
    writeLogFile(String filename, String data) {
        String rootPath = this.getExternalFilesDir(null) + "/"+ filename +".txt";
        File file = new File(rootPath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        try {
            FileWriter fileWriter = new FileWriter(file, true);

            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(data + "\n");
            bufferedWriter.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
