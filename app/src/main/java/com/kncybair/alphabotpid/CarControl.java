/*
  MIT License 2019
  ---
  Alphabot PID tester Android application
  version: 0.1
  Purpose: PID tester is for experimental determination of Kp, Ki and Kd parameters for LF Alphabot
  File: CarControl.java
  ---
  @author: Krzysztof Stezala
  ---
  Provided by CybAiR Science Club at
  Institute of Control, Robotics and Information Engineering of
  Poznan University of Technology
*/

package com.kncybair.alphabotpid;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Debug;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class CarControl extends AppCompatActivity {

    int step;
    int max_value_sb;
    int min_value_sb;

    int kp_val;
    int ki_val;
    int kd_val;

    SeekBar Kp_bar, Ki_bar, Kd_bar;
    TextView Kp_label, Ki_label, Kd_label;
    Button sendBtn, cutBtn;

    String address = null;
    private ProgressDialog progressDialog;
    BluetoothAdapter bluetoothAdapter = null;
    BluetoothSocket bluetoothSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent newIntent = getIntent();
        address = newIntent.getStringExtra(MainActivity.EXTRA_ADDRESS);
        setContentView(R.layout.activity_car_control);

        step = 1;
        max_value_sb = 20;
        min_value_sb = 0;

        Kp_bar =  findViewById(R.id.kp_bar);
        Ki_bar =  findViewById(R.id.ki_bar);
        Kd_bar =  findViewById(R.id.kd_bar);
        Kp_label = findViewById(R.id.pValue);
        Ki_label = findViewById(R.id.iValue);
        Kd_label = findViewById(R.id.dValue);

        sendBtn = findViewById(R.id.sendButton);
        cutBtn = findViewById(R.id.cutButton);

        /*
        Kp_bar.setMax((max_value_sb-min_value_sb)/step);
        Ki_bar.setMax((max_value_sb-min_value_sb)/step);
        Kd_bar.setMax((max_value_sb-min_value_sb)/step);
        */

        Kp_bar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener()
                {
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress,
                                                  boolean fromUser)
                    {
                        // Ex :
                        // And finally when you want to retrieve the value in the range you
                        // wanted in the first place -> [3-5]
                        //
                        // if progress = 13 -> value = 3 + (13 * 0.1) = 4.3
                        kp_val = min_value_sb + (progress * step);
                        Kp_label.setText(Integer.toString(kp_val));


                    }
                }
        );

        Ki_bar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener()
                {
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress,
                                                  boolean fromUser)
                    {
                        // Ex :
                        // And finally when you want to retrieve the value in the range you
                        // wanted in the first place -> [3-5]
                        //
                        // if progress = 13 -> value = 3 + (13 * 0.1) = 4.3
                        ki_val = min_value_sb + (progress * step);
                        Ki_label.setText(Integer.toString(ki_val));


                    }
                }
        );

        Kd_bar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener()
                {
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress,
                                                  boolean fromUser)
                    {
                        // Ex :
                        // And finally when you want to retrieve the value in the range you
                        // wanted in the first place -> [3-5]
                        //
                        // if progress = 13 -> value = 3 + (13 * 0.1) = 4.3
                        kd_val = min_value_sb + (progress * step);
                        Kd_label.setText(Integer.toString(kd_val));

                    }
                }
        );

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendPID();
            }
        });

        cutBtn.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cutPower();
            }
        }));

        // Call the class to connect
        new ConnectBT().execute();

    }

    String COMMAND;
    private void sendData(String command){
        if(bluetoothSocket!=null){
            try{
                Log.d(COMMAND,command);
                bluetoothSocket.getOutputStream().write(command.getBytes());
            }
            catch (IOException e){
                Toast.makeText(CarControl.this,"Fuck it", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void cutPower() {
        String command = "0|0|0|";

        //send data
        sendData(command);
    }

    private void sendPID(){
        //get data from seekbars
        String command = kp_val + "|" + ki_val + "|" + kd_val + "|";
        Log.d(COMMAND,command);

        //send data
        sendData(command);
        //sendData("N;");

    }

    private void doNothing() {
        sendData("N");
    }



    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean connectSuccess = true;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(CarControl.this, "Connecting...", "Please wait.");
        }

        @Override
        protected Void doInBackground(Void... devices){
            try{
                if(bluetoothSocket == null || !isBtConnected){
                    // get the mobile bt device
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    // connect to the devices's address and checks if it's available
                    BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(address);
                    // create a RFCOMM (SPP) connection
                    bluetoothSocket = remoteDevice.createInsecureRfcommSocketToServiceRecord(myUUID);
                    // cancel discovery process
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    // start connection to the remote device
                    bluetoothSocket.connect();
                }
            }
            catch(IOException e){
                // if connection failed, exception is catched
                connectSuccess = false;
            }
            return null;
        }

        // after doInBackground, it checks if everything went fine
        @Override
        protected void onPostExecute(Void result){
            super.onPostExecute(result);

            if(!connectSuccess){
                Toast.makeText(CarControl.this,"Connection failed :(", Toast.LENGTH_SHORT).show();
                finish();
            }
            else{
                Toast.makeText(CarControl.this,"Connected :)", Toast.LENGTH_SHORT).show();
                isBtConnected = true;
            }
            progressDialog.dismiss();
        }
    }

}
