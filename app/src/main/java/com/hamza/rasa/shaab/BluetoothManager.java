package com.hamza.rasa.shaab;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Hamza on 1/30/2017.
 */

public class BluetoothManager {
    public static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String address;
    public onBluetoothListener setBluetoothListener = null;

    public static String getAddress() {
        return address;
    }

    public static void setAddress(String _address) {
        address = _address;
    }

    public static BluetoothSocket CreateBluetoothSocket(BluetoothDevice device) throws IOException {

        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    public static ArrayAdapter<String> getPairedDevices(Context context) {
        ArrayAdapter<String> mPairedDevicesArrayAdapter = new ArrayAdapter<String>(context, R.layout.device_name);
        BluetoothAdapter mBtAdapter;
        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // Get a set of currently paired devices and append to 'pairedDevices'
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        mPairedDevicesArrayAdapter.add("Nothing");
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = "None_Paired";// getResources().getText(R.string.none_paired).toString();
            mPairedDevicesArrayAdapter.add(noDevices);
        }

        return mPairedDevicesArrayAdapter;
    }

    public void checkBTState(Context context) {
        BluetoothAdapter mBtAdapter;
        // Check device has Bluetooth and that it is turned on
        mBtAdapter = BluetoothAdapter.getDefaultAdapter(); // CHECK THIS OUT THAT IT WORKS!!!
        if (mBtAdapter == null) {
            Toast.makeText(context, "Device does not support Bluetooth", Toast.LENGTH_SHORT).show();
        } else {
            if (mBtAdapter.isEnabled()) {
                if (setBluetoothListener != null) {
                    System.out.println("Bluetooth ON");
                    setBluetoothListener.onBluetooth();
                }
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                enableBtIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    context.startActivity(enableBtIntent);
                }
                mBtAdapter = BluetoothAdapter.getDefaultAdapter(); // CHECK THIS OUT THAT IT WORKS!!!
                boolean bt = mBtAdapter.isEnabled();
                if (bt) {
                    if (setBluetoothListener != null) {
                        System.out.println("Bluetooth ON");
                        setBluetoothListener.onBluetooth();
                    }
                }
            }
        }
    }

    public interface onBluetoothListener {
        void onBluetooth();
    }


}
