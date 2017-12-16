package com.hamza.rasa.shaab;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
//import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.SyncFailedException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.hamza.rasa.shaab.BluetoothManager.BTMODULEUUID;

public class MainActivity extends AppCompatActivity {

    private android.os.Handler bluetoothIn;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private MainActivity.ConnectedThread mConnectedThread;
    private Button btnConnect;
    private TextView txtShaab;
    private Button btnClear;
    private Button btnSend;
    private LinearLayout layInfo;
    private String address;
    private final int handlerState = 0;
    private StringBuilder dataBuilder = new StringBuilder(100);
    private final String FileName = "Shaab.csv";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        layInfo = (LinearLayout) findViewById(R.id.layInfo);


        txtShaab = (TextView) findViewById(R.id.textShaab);
        String udata = "Shaab";
        SpannableString content = new SpannableString(udata);
        content.setSpan(new UnderlineSpan(), 0, udata.length(), 0);
        txtShaab.setText(content);

        txtShaab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://shaab-co.com/"));
                startActivity(browserIntent);
            }
        });


        btnSend = (Button) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (layInfo.getChildCount() > 0) {
                    dataBuilder.setLength(0);
                    dataBuilder.append("#,Date,Serial,TestType,Test1,Test2,Test3,Test4,Test5,Test6,Test7,Test8,Test9,Average\n");
                    //dataBuilder.append("#,Date,Serial,TestType,Hardness\n");
                    for (int i = 0; i < layInfo.getChildCount(); i++) {
                        TextView txt = (TextView) layInfo.getChildAt(i);
                        while (txt.getText().toString().contains("  ")) {
                            txt.setText( txt.getText().toString().replace("  ", " "));
                        }
                        String row = txt.getText().toString().trim().replace(" ", ",");
                        dataBuilder.append((i + 1) + "," + row + "\n");
                    }
                    Mail(getApplication(), FileName, "Hardness Test Information", dataBuilder.toString());
                } else {
                    Toast.makeText(MainActivity.this, "Empty List", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnClear = (Button) findViewById(R.id.btnClear);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layInfo.removeAllViewsInLayout();
                layInfo.removeAllViews();
                cntr=0;
                String udata = "Shaab";
                SpannableString content = new SpannableString(udata);
                content.setSpan(new UnderlineSpan(), 0, udata.length(), 0);
                txtShaab.setText(content);
            }
        });

        btnConnect = (Button) findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btSocket != null && btSocket.isConnected())
                    Disconnect();
                else
                    Connect(address);
            }
        });

        Button btnBluetoothList = (Button) findViewById(R.id.btnBluetoothList);
        btnBluetoothList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DeviceListActivity.class);
                startActivity(intent);
            }
        });
        checkBTState();
        bluetoothIn = blueToothHandler;
    }

    public void Mail(Context context, String fileName, String sSubject, String txt) {
        WriteFile(context, fileName, txt);
        WriteToExternal(context, fileName);
        ByMail(context, fileName, sSubject);
    }

    public static void WriteToExternal(Context context, String filename) {
        try {
            File file = new File(context.getExternalFilesDir(null), filename); //Get file location from external source
            InputStream is = new FileInputStream(context.getFilesDir() + File.separator + filename); //get file location from internal
            OutputStream os = new FileOutputStream(file); //Open your OutputStream and pass in the file you want to write to
            byte[] toWrite = new byte[is.available()]; //Init a byte array for handing data transfer
//            Log.i("Available ", is.available() + "");
            int result = is.read(toWrite); //Read the data from the byte array
//            Log.i("Result", result + "");
            os.write(toWrite); //Write it to the output stream
            is.close(); //Close it
            os.close(); //Close it
//            Log.i("Copying to", "" + context.getExternalFilesDir(null) + File.separator + filename);
//            Log.i("Copying from", context.getFilesDir() + File.separator + filename + "");
        } catch (Exception e) {
            //Log.i("File write failed: " , e.getLocalizedMessage());
            Toast.makeText(context, "File write failed: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show(); //if there's an error, make a piece of toast and serve it up
        }
    }

    public static void ByMail(Context context, String filename, String sSubject) {
//        Log.i("Copying to", "" + context.getExternalFilesDir(null) + File.separator + filename);

        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        // set the type to 'email'
        emailIntent.setType("vnd.android.cursor.dir/email");
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        String to[] = {"Hamzerasaee@gmail.com"};//UserManagement.getMainUser().getEmail()};
//        emailIntent.putExtra(Intent.EXTRA_EMAIL, to);

        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Sent From Shaab Hardness Tester");

        // the attachment
//        ArrayList<Uri> uris = new ArrayList<>(2);
//
        Uri path = Uri.fromFile(new File(context.getExternalFilesDir(null) + File.separator + filename));
//
//        uris.add(path);

//        String mPath = Environment.getExternalStorageDirectory().toString() + "/Shaab/" + filename;
//        System.out.println("mPath:" + mPath);
//        File img = new File(mPath);
//        if (img.exists()) {
//            uris.add(Uri.fromFile(img));
//
//        }
        //emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        emailIntent.putExtra(Intent.EXTRA_STREAM, path);
//        emailIntent.putExtra(Intent.EXTRA_STREAM, img);
        // the mail subject
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, sSubject);

        Intent intent = Intent.createChooser(emailIntent, "Send email...");
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


    public void WriteFile(Context context, String fileName, String data) {
        try {
            FileOutputStream fileout = context.openFileOutput(fileName, MODE_PRIVATE);
            OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);
            outputWriter.write(data);
            outputWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // private final int RowLength = 35;
    int cntr=0;
    private StringBuilder receivedMSG = new StringBuilder(100);
    private Handler blueToothHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == handlerState) {                                     //if message is what we want

                String readMessage = (String) msg.obj;
                receivedMSG.append(readMessage);
                //int length = receivedMSG.length();
                int end = receivedMSG.indexOf("*");
                //if (length >= RowLength) {
                if (end != -1) {
                    TextView textView = new TextView(MainActivity.this);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    textView.setLayoutParams(layoutParams);
                    layInfo.addView(textView);
                    //int end =receivedMSG.indexOf("*");
                    textView.setText(receivedMSG.substring(0, end));
                    if(cntr++%2==0)
                        textView.setBackgroundColor(Color.GRAY);
                    else
                        textView.setBackgroundColor(Color.WHITE);

                    textView.setTextColor(Color.BLACK);
                    receivedMSG.delete(0, end + 1);
                }
                txtShaab.setText(receivedMSG.length() + "Receiving...");
                if (receivedMSG.length() == 3 && receivedMSG.charAt(0) == 'E' && receivedMSG.charAt(1) == 'O' && receivedMSG.charAt(2) == 'S') {
                    receivedMSG.setLength(0);
                    String udata = "Shaab";
                    SpannableString content = new SpannableString(udata);
                    content.setSpan(new UnderlineSpan(), 0, udata.length(), 0);
                    txtShaab.setText(content);
                    cntr=0;
                }

            }
        }
    };

    private void SendingByte(byte b) {
        if (btSocket != null && btSocket.isConnected())
            mConnectedThread.writeByte(b);
    }

    private void SendingByte(char b) {
        if (btSocket != null && btSocket.isConnected())
            mConnectedThread.writeByte(b);
    }

    private void SendString(String s) {
        if (btSocket != null && btSocket.isConnected())
            mConnectedThread.writeString(s);
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    private void Connect(String address) {
        btnConnect.setBackgroundResource(R.drawable.bluetooth_connect);
        if (btSocket != null && btSocket.isConnected()) {
            Toast.makeText(MainActivity.this, "Already Connected", Toast.LENGTH_SHORT).show();
            // txtMessage.setText("Already Connected");
        } else {
            Toast.makeText(MainActivity.this, "Connecting", Toast.LENGTH_SHORT).show();
            //txtMessage.setText("Connecting");
            if (address == null) {
                Toast.makeText(MainActivity.this, "Invalid Address", Toast.LENGTH_SHORT).show();
            } else {
                BluetoothDevice device = btAdapter.getRemoteDevice(address);

                try {
                    btSocket = createBluetoothSocket(device);
                } catch (IOException e) {
                    //txtMessage.setText("Socket creation failed");
                    Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
                }
                // Establish the Bluetooth socket connection.
                try {
                    btSocket.connect();
                    //txtMessage.setText("Socket Connected");
                    Toast.makeText(getBaseContext(), "Socket Connected", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    //txtMessage.setText(e.getMessage());
                    Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
                mConnectedThread = new ConnectedThread(btSocket);
                mConnectedThread.start();
                if (btSocket.isConnected()) {
                    btnConnect.setBackgroundResource(R.drawable.bluetooth_disconnect);
                    //btnConnect.setText("Disconnect");
                    //txtMessage.setText("Connected");
                }
            }
        }
        //
        // Toast.makeText(getBaseContext(), "Connected", Toast.LENGTH_LONG).show();
    }

    private void Disconnect() {
        if (btSocket.isConnected()) {
            try {
                btSocket.close();
                // btnConnect.setText("Connect");
                btnConnect.setBackgroundResource(R.drawable.bluetooth_connect);
                Toast.makeText(getBaseContext(), "Socket Closed", Toast.LENGTH_LONG).show();
            } catch (IOException e2) {
                Toast.makeText(getBaseContext(), "Socket Closed Problem", Toast.LENGTH_LONG).show();

                //insert code to deal with this
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            //  handler.removeCallbacks(runnable);
            //Don't leave Bluetooth sockets open when leaving activity
            btnConnect.setBackgroundResource(R.drawable.bluetooth_connect);
            if (btSocket != null && btSocket.isConnected())
                btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        address = BluetoothManager.getAddress();
        btnConnect.setBackgroundResource(R.drawable.bluetooth_connect);
        // System.out.println("address:" + address);
    }

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {

        if (btAdapter == null) {
            Toast.makeText(getBaseContext(), "Device does not support bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }


    //create new class for connect thread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[2048];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);            //read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        //write method
        public void writeString(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "Connection Failure\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                finish();

            }
        }

        //write method
        public void writeByte(byte input) {
            byte[] msgBuffer = new byte[1];           //converts entered String into bytes
            try {
                msgBuffer[0] = input;
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "Connection Failure\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                finish();

            }
        }

        //write method
        public void writeByte(char input) {
            byte[] msgBuffer = new byte[1];           //converts entered String into bytes
            try {
                msgBuffer[0] = (byte) input;
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "Connection Failure\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                finish();

            }
        }
    }
}
