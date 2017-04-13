package com.example.traviswentz.wifidirecttester;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pDevice;
import android.widget.Toast;
import android.widget.TextView;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;

public class MainActivity extends AppCompatActivity {

    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager manager;
    private Channel channel;
    private boolean isWifiP2pEnabled;
    Context CONTEXT=this;

    // might have to unicast to each peer on the list if broadcast is not an option?
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //  Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
    }

    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    public static String getDeviceStatus(int deviceStatus) {
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";

        }
    }

    public void showMessage(String str){
        Toast.makeText(CONTEXT, str,Toast.LENGTH_SHORT).show();
    }

    public void updateThisDevice(WifiP2pDevice device) {
        TextView view = (TextView)findViewById(R.id.mystatus);
        view.setText("My Name: " + device.deviceName + "\nMy Address: " + device.deviceAddress + "\nMy Status: " + getDeviceStatus(device.status));
        return;
    }

    @Override
    public void cancelDisconnect() {
        if (manager != null) {
            final DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
                    .findFragmentById(R.id.devicedetail);
            if (fragment.device == null
                    || fragment.device.status == WifiP2pDevice.CONNECTED) {
                disconnect();
            } else if (fragment.device.status == WifiP2pDevice.AVAILABLE
                    || fragment.device.status == WifiP2pDevice.INVITED) {

                manager.cancelConnect(channel, new ActionListener() {

                    @Override
                    public void onSuccess() {
                        showMessage("Aborting connection");
                        return;
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        showMessage("Connect abort request failed. Reason Code: " + reasonCode);
                    }
                });
            }
        }
        return;

    }

    @Override
    public void connect(WifiP2pConfig config) {
        // TODO Auto-generated method stub
        manager.connect(channel, config, new ActionListener() {

            @Override
            public void onSuccess() {
                // WifiDirectBroadcastReceiver will notify us

            }

            @Override
            public void onFailure(int reason) {
                showMessage ("Connect failed: "+reason);

            }
        });
        return;

    }

    @Override
    public void disconnect() {
        // TODO Auto-generated method stub

        final DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.devicedetail);
        fragment.resetViews();
        manager.removeGroup(channel, new ActionListener() {

            @Override
            public void onFailure(int reasonCode) {
                showMessage("Disconnect failed. Reason :" + reasonCode);

            }

            @Override
            public void onSuccess() {
                // fragment.getView().setVisibility(View.GONE);
                showMessage("Disconnected.");
            }

        });

    }
}
