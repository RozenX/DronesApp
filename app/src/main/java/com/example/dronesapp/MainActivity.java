package com.example.dronesapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    static UUID mUUID;
    ParcelUuid[] uuids;
    InputStream inputStream = null;
    TextView text;
    OutputStream outputStream;
    BluetoothSocket btSocket = null;
    BluetoothAdapter adapter;
    Button sendButton;
    Button closeButton;
    ListView listView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.uuids);

        adapter = BluetoothAdapter.getDefaultAdapter();
        Method getUuidsMethod = null;
        try {
            getUuidsMethod = BluetoothAdapter.class.getDeclaredMethod("getUuids", null);

            uuids = (ParcelUuid[]) getUuidsMethod.invoke(adapter, null);
            text = findViewById(R.id.txt);

            for (ParcelUuid uuid : uuids) {
               listView.
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
        }

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        System.out.println(btAdapter.getBondedDevices());

        BluetoothDevice hc05 = btAdapter.getRemoteDevice("00:21:13:02:B6:5B");
        System.out.println(hc05.getName());

        int counter = 0;
        do {
            try {
                btSocket = hc05.createRfcommSocketToServiceRecord(mUUID);
                System.out.println(btSocket);
                btSocket.connect();
                System.out.println(btSocket.isConnected());
            } catch (IOException e) {
                e.printStackTrace();
            }
            counter++;
        } while (!btSocket.isConnected() && counter < 3);


        try {
            outputStream = btSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            inputStream = btSocket.getInputStream();
            inputStream.skip(inputStream.available());
        } catch (IOException e) {
            e.printStackTrace();
        }

        sendButton = findViewById(R.id.send_btn);
        sendButton.setOnClickListener(v -> send());

        closeButton = findViewById(R.id.close_btn);
        closeButton.setOnClickListener(v -> close());

        show();
    }

    public void send() {
        try {
            char c = 's';
            outputStream.write((int) c);
        }
        catch (IOException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void show() {
        try {
            long ms;
            while (btSocket.isConnected()) {
                ms = System.currentTimeMillis();
                if (ms >= 1000) {
                    text.setText(inputStream.read());
                }
            }
        } catch (IOException e) {
            Toast.makeText(this, "something went wrong",Toast.LENGTH_SHORT).show();
        }
    }

    public void close() {
        try {
            btSocket.close();
            System.out.println(btSocket.isConnected());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}