package com.jdstanton.samsung.deviceid;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.os.Build;
import android.widget.Toast;

import com.samsung.android.knox.EnterpriseDeviceManager;
import com.samsung.android.knox.deviceinfo.DeviceInventory;
import com.samsung.android.knox.restriction.RestrictionPolicy;
import com.samsung.android.knox.EnterpriseDeviceManager;
import com.samsung.android.knox.license.EnterpriseLicenseManager;
import com.samsung.android.knox.container.KnoxContainerManager;

import java.util.Arrays;

import static android.content.ContentValues.TAG;

public class MainActivity extends Activity {

    private MainActivity mActivity;

    private static final int REQUEST_PERMISSIONS = 100;
    private static final String PERMISSIONS_REQUIRED[] = new String[]{
            Manifest.permission.READ_PHONE_STATE
    };
    /*
    // Interaction with the DevicePolicyManager
    DevicePolicyManager mDPM;
    ComponentName mDeviceAdmin;
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        TextView imeiTextView = (TextView) findViewById(R.id.textView);
        imeiTextView.setText("");
        printADBid(); //Print the ADB ID (Android P shows this the same as the serial number)
        printSERIAL();  //Print the serial number returned by Knox API
        printIMEI(); //Print the IMEI
        printAndroidID();  //Print the Android ID
    }

    private boolean checkPermission(String permissions[]) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    private void checkPermissions() {
        boolean permissionsGranted = checkPermission(PERMISSIONS_REQUIRED);
        if (permissionsGranted) {
            //do something
        } else {
            boolean showRationale = true;
            for (String permission: PERMISSIONS_REQUIRED) {
                showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
                if (!showRationale) {
                    break;
                }
            }

            String dialogMsg = showRationale ? "We need some permissions to run this APP!" : "Please grant permissions.";

            new AlertDialog.Builder(this)
                    .setTitle("Permissions Required")
                    .setMessage(dialogMsg)
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(mActivity, PERMISSIONS_REQUIRED, REQUEST_PERMISSIONS);
                        }
                    }).create().show();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("MainActivity", "requestCode: " + requestCode);
        Log.d("MainActivity", "Permissions:" + Arrays.toString(permissions));
        Log.d("MainActivity", "grantResults: " + Arrays.toString(grantResults));

        if (requestCode == REQUEST_PERMISSIONS) {
            boolean hasGrantedPermissions = true;
            for (int i=0; i<grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    hasGrantedPermissions = false;
                    break;
                }
            }

            if (!hasGrantedPermissions) {
                finish();
            }

        } else {
            finish();
        }

        TextView imeiTextView = (TextView) findViewById(R.id.textView);
        imeiTextView.setText("");
        printADBid(); //Print the ADB ID (Android P shows this the same as the serial number)
        printSERIAL();  //Print the serial number returned by Knox API
        printIMEI(); //Print the IMEI
        printAndroidID();  //Print the Android ID

    }

    public String getDeviceSerial(){
        checkPermissions();
        boolean permissionsGranted = checkPermission(PERMISSIONS_REQUIRED);
        if(permissionsGranted) {
            Integer SDKVersion = android.os.Build.VERSION.SDK_INT;
            if(SDKVersion < 26){
                return Build.SERIAL;
            }
            else {
                return Build.getSerial();
            }
        }
        else{
            return "Not Found";
        }
    }

    void printSERIAL(){
        try {
            //Knox stuff
            //Class cls = Class.forName ("EnterpriseDeviceManager");
            TextView imeiTextView = (TextView) findViewById(R.id.textView);
            EnterpriseDeviceManager edm = EnterpriseDeviceManager.getInstance(this);
            DeviceInventory deviceInventoryPolicy = edm.getDeviceInventory();
            String serialNumber = deviceInventoryPolicy.getSerialNumber();
            if (null != serialNumber) {
                imeiTextView.append("Serial Number: " + serialNumber + "\n\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    void printIMEI(){
        try {
            TextView imeiTextView = (TextView) findViewById(R.id.textView);
            checkPermissions();
            boolean permissionsGranted = checkPermission(PERMISSIONS_REQUIRED);
            if(permissionsGranted){
                TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
                String meid = telephonyManager.getDeviceId();
                if(meid == null){meid="None";}
                imeiTextView.append("IMEI / MEID: " + meid + "\n\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void printADBid(){
        try {
            TextView imeiTextView = (TextView) findViewById(R.id.textView);
            checkPermissions();
            boolean permissionsGranted = checkPermission(PERMISSIONS_REQUIRED);
            if(permissionsGranted){
                String serial = getDeviceSerial();
                imeiTextView.append("ADB ID: " + serial + "\n\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void printAndroidID(){
        TextView imeiTextView = (TextView) findViewById(R.id.textView);
        try {
            String androidID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
            imeiTextView.append("Android ID: " + androidID + "\n\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}

