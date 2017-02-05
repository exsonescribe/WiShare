package com.example.exson.another1;

import android.app.Activity;
import android.content.Intent;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class NsdChatActivity extends Activity {

    NsdHelper mNsdHelper;

    private ListView mStatusView;
    private Handler mUpdateHandler;

    public static final String TAG = "NsdChat";

    ChatConnection mConnection;

    ArrayAdapter<String> content;
    ListView list;
    ArrayList<String> mes;
    List<String> dev;
    String messageString, messageName, chatLine;

    private static final int REQUEST_PICK_FILE = 1;
    private File selectedFile;

    private boolean filePathProvided;
    private File fileToSend;
    private Intent clientServiceIntent;
    private boolean transferActive;

    NsdServiceInfo service;

    InetAddress address;
    int port;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        list = (ListView) findViewById(R.id.names);

//        mUpdateHandler = new Handler() {
//            @Override
//            public void handleMessage(File msg) {
//                chatLine = msg.getData().getString("msg");
//                addChatLine(chatLine);
//            }
//        };

        mConnection = new ChatConnection(mUpdateHandler);

        mNsdHelper = new NsdHelper(this);
        mNsdHelper.initializeNsd();

    }

    public void clickAdvertise(View v) {
        // Register service
        if(mConnection.getLocalPort() > -1) {
            mNsdHelper.registerService(mConnection.getLocalPort());
        } else {
            Log.d(TAG, "ServerSocket isn't bound.");
        }
    }

    public void clickDiscover(View v) {
        mNsdHelper.discoverServices();
        if (dev != null) {
            dev.removeAll(mNsdHelper.getDevices());
        }
        else {
            addDev();
        }
    }

    public void addDev() {
        dev = mNsdHelper.getDevices();
        mes = new ArrayList<String>(dev);
        content = new ArrayAdapter<String>(NsdChatActivity.this, R.layout.names, mes);
        list.setAdapter(content);
    }

    public void clickConnect(View v) {
        service = mNsdHelper.getChosenServiceInfo();
        if (service != null) {
            Log.d(TAG, "Connecting.");
            mConnection.connectToServer(service.getHost(),
                    service.getPort());
        } else {
            Log.d(TAG, "No service to connect to!");
        }
    }


//    @Override
//    protected void onPause() {
//        if (mNsdHelper != null) {
//            mNsdHelper.stopDiscovery();
//        }
//        super.onPause();
//    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNsdHelper != null) {
            mNsdHelper.discoverServices();
        }
    }

    @Override
    protected void onDestroy() {
        mNsdHelper.tearDown();
        mConnection.tearDown();
        super.onDestroy();
    }

    public void clickBrowse(View view) {
        Intent intent = new Intent(this, FilePicker.class);
        startActivityForResult(intent, REQUEST_PICK_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_PICK_FILE) {
            if (resultCode == RESULT_OK) {
                // successfully picked the file
                if (data.hasExtra(FilePicker.EXTRA_FILE_PATH)) {
                    selectedFile = new File
                            (data.getStringExtra(FilePicker.EXTRA_FILE_PATH));

                    Toast.makeText(getApplicationContext(),
                            selectedFile.toString(), Toast.LENGTH_SHORT)
                            .show();

                }
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled file select
                Toast.makeText(getApplicationContext(),
                        "User cancelled file select", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to pick file
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to pick file", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    public void clickSend(View view) {


        if (selectedFile != null) {
           mConnection.sendMessage(selectedFile);
        }
    }
}