package com.example.exson.nsdtest1;

import android.app.Activity;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class NsdChatActivity extends Activity {

    NsdHelper mNsdHelper;

    private ListView mStatusView;
    private Handler mUpdateHandler;

    public static final String TAG = "NsdChat";

    ChatConnection mConnection;

    private ConversationListAdapter mConvoListAdapter;

    ArrayAdapter<String> content;
    ListView list;
    ArrayList<String> mes;
    List<String> dev;
    String messageString, messageName, chatLine;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mStatusView = (ListView) findViewById(R.id.status);

        list = (ListView) findViewById(R.id.names);

        mUpdateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                chatLine = msg.getData().getString("msg");
                addChatLine(chatLine);
            }
        };

        mConnection = new ChatConnection(mUpdateHandler);

        mNsdHelper = new NsdHelper(this);
        mNsdHelper.initializeNsd();

        mConvoListAdapter = new ConversationListAdapter(getApplicationContext(), R.layout.message);
        mStatusView.setAdapter(mConvoListAdapter);

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
        NsdServiceInfo service = mNsdHelper.getChosenServiceInfo();
        if (service != null) {
            Log.d(TAG, "Connecting.");
            mConnection.connectToServer(service.getHost(),
                    service.getPort());
        } else {
            Log.d(TAG, "No service to connect to!");
        }
    }

    public void clickSend(View v) {
        EditText messageView = (EditText) this.findViewById(R.id.chatInput);

        if (messageView != null) {
            messageString = messageView.getText().toString();
            messageName = mNsdHelper.getmServiceName();
            if (!messageString.isEmpty()) {
                mConnection.sendMessage(messageName + ":" + messageString);
            }
            messageView.setText("");
        }
    }

    public void  addChatLine(String line) {
        String[] content = line.split(":");
        Log.d(TAG, String.valueOf(content));
        String mName = String.valueOf(content[0]);
        Log.d(TAG, String.valueOf(content[0]));
        String mMessage = String.valueOf(content[1]);
        Log.d(TAG, String.valueOf(content[1]));

        mConvoListAdapter.add(new Conversation(mName, mMessage));

//        mStatusView.append("\n" + line);
    }

    @Override
    protected void onPause() {
        if (mNsdHelper != null) {
            mNsdHelper.stopDiscovery();
        }
        super.onPause();
    }

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
}