package com.example.dronesapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    static final UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    InputStream inputStream = null;
    TextView text;
    OutputStream outputStream;
    BluetoothSocket btSocket = null;
    Button sendButton;
    Button closeButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text = findViewById(R.id.txt);

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