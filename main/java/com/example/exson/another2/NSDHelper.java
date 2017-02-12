package com.example.exson.another2;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;
import android.os.StrictMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.WIFI_SERVICE;

public class NSDHelper {
    private Context mContext;

    private static NSDHelper mInstance;
    private NsdManager mNSDManager;
    private NsdServiceInfo mDeviceInfo;

    private NSDDiscoverListener mDiscoverListener;
    private NSDRegisterListener mRegisterListener;

    private final String SERVICE_TYPE = "_http._tcp.";
    private final String TAG = "NSDHelper";

    private int mSocket = 10000;

    private String mNames;
    private String mUser;
    private SharedPreferences mPreferences;

    private NSDHelper(Context context) {

        // initializations
        mContext = context;
        mNSDManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        mDeviceInfo = new NsdServiceInfo();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mDiscoverListener = new NSDDiscoverListener();
        mRegisterListener = new NSDRegisterListener();

        mPreferences = mContext.getSharedPreferences("WiShare", MODE_PRIVATE);
        mUser = mPreferences.getString ("Fullname", "");

        try {
            int ipAddress = ((WifiManager) context.getSystemService(WIFI_SERVICE)).getConnectionInfo().getIpAddress();
            mDeviceInfo.setHost(InetAddress.getByAddress(BigInteger.valueOf(ipAddress).toByteArray()));
            mDeviceInfo.setPort((new ServerSocket(mSocket)).getLocalPort());
            mDeviceInfo.setServiceType(SERVICE_TYPE);
            mDeviceInfo.setServiceName(mUser);
//            mDeviceInfo.setServiceName(String.format(Locale.US, "%s %d", context.getResources().getString(R.string.app_name), ipAddress));
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
    }

    static NSDHelper getInstance(Context context) {
        if (mInstance==null) {
            mInstance = new NSDHelper(context);
        }

        return mInstance;
    }

    void start() {
        mNSDManager.registerService(mDeviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegisterListener);
    }

    void stop() {
        mNSDManager.unregisterService(mRegisterListener);
        mNSDManager.stopServiceDiscovery(mDiscoverListener);
    }

    public String getmNames() {
        return mNames;
    }

    private class NSDResolveService implements NsdManager.ResolveListener {

        @Override
        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
            Toast.makeText(mContext, String.format(Locale.US, "Failed to resolve service: %d", errorCode), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onServiceResolved(NsdServiceInfo serviceInfo) {
            Toast.makeText(mContext, String.format(Locale.US, "Successfully resolved service: %s, %s", serviceInfo.getHost().getHostName(), serviceInfo.getServiceName()), Toast.LENGTH_LONG).show();

        }
    }

    private class NSDDiscoverListener implements NsdManager.DiscoveryListener {
        @Override
        public void onStartDiscoveryFailed(String serviceType, int errorCode) {
        }

        @Override
        public void onStopDiscoveryFailed(String serviceType, int errorCode) {
        }

        @Override
        public void onDiscoveryStarted(String serviceType) {
        }

        @Override
        public void onDiscoveryStopped(String serviceType) {
        }

        @Override
        public void onServiceFound(NsdServiceInfo serviceInfo) {
            if (!serviceInfo.getServiceName().equals(mDeviceInfo.getServiceName())) {
                Toast.makeText(mContext, "Service Found", Toast.LENGTH_LONG).show();
                mNSDManager.resolveService(serviceInfo, new NSDResolveService());
            }
        }

        @Override
        public void onServiceLost(NsdServiceInfo serviceInfo) {
            Toast.makeText(mContext, "Service Lost", Toast.LENGTH_LONG).show();
        }
    }

    private class NSDRegisterListener implements NsdManager.RegistrationListener {
        @Override
        public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {

        }

        @Override
        public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {

        }

        @Override
        public void onServiceRegistered(NsdServiceInfo serviceInfo) {
            Toast.makeText(mContext, "Successfully registered to Network", Toast.LENGTH_LONG).show();
            mNSDManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoverListener);
        }

        @Override
        public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
            Toast.makeText(mContext, "Successfully unregistered to Network", Toast.LENGTH_LONG).show();
        }
    }

}
