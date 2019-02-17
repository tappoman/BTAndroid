package bt.proto.device;


import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import bt.proto.database.EntryDatabase;

import java.nio.charset.Charset;
import java.util.List;
import java.util.UUID;


public class GattService extends Service {

    public static final String TAG = "GattService";

    private static final UUID CONFIG_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private static final UUID SERVICE = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID CHAR = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");

    private static String MAC=null;
    private EntryDatabase entryDatabase;

    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mBluetoothDevice;
    BluetoothGatt mGatt;

    private IBinder mBinder = new MyBinder();

    private Handler reconnectHandler = new Handler();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if(intent!=null){

            try {
                Bundle bundle = intent.getExtras();
                MAC = bundle.getString("MAC");
            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.i(TAG,"received mac: "+MAC);

            final BluetoothManager bluetoothManager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            try{
                mBluetoothAdapter = bluetoothManager.getAdapter();
                if(!MAC.isEmpty()){
                    connectToDevice(MAC);
                }
                else{
                    stopSelf();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return START_NOT_STICKY;

    }


    @Override
    public IBinder onBind(Intent intent) {

        Log.i(TAG,"On Bind");
        return mBinder;
    }

    public void listServices(List<BluetoothGattService> services){

        Intent intent = new Intent("Gatt Services");
        for(int i=0 ; i<services.size() ; i++);{
            //intent.putExtra("service", services.get(i));
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }


    public void sendResults(String value, String MAC) {

        Intent intent = new Intent("Gatt");
        //Log.i(TAG,"value "+value);

        Long ts = System.currentTimeMillis() / 1000;

        if(value==null){
            return;
        }

        if(value.length()==0 || value.length()==1){
            return;
        }

        //Log.i(TAG,"sends: "+value);

        intent.putExtra("state", value);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void sendACK(String MAC, String state) {

        Intent intent = new Intent("GattMessage");
        intent.putExtra("name", MAC);
        intent.putExtra("state", state);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    @Override
    public void onCreate() {

        Log.i(TAG, "onCreate");

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        this.entryDatabase = EntryDatabase.getDataBase(this);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"on Destroy");



        if (mGatt != null) {
            mGatt.disconnect();
            mGatt.close();
            mGatt = null;

            //mBluetoothDevice = null;
            //mGatt.close();
            this.stopSelf();
        }


        //Log.d(TAG, "Gatt " + MAC + " Destroyed");
        //Log.d(TAG, "Gattservice Destroyed");

    }


    public void activateNormalfeed(){

        try {
            normalfeed(mGatt);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void connectToDevice(String mac) {

        final String temp = mac;

        if (!isConnected()) {
            mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(MAC);

            if(mGatt==null){
                mGatt = mBluetoothDevice.connectGatt(getApplicationContext(), false, gattCallback);
                Log.i(TAG, "gatt connection issued");
                return;
            }

            else{
                try {
                    //reconnectHandler.removeCallbacksAndMessages(null);
                    reconnectHandler.postDelayed(new Runnable() {
                        public void run() {
                            mGatt.close();
                            mGatt=null;
                            connectToDevice(MAC);
                        }
                    }, 5000);

                } catch (Exception e) {
                    //reconnectHandler.removeCallbacksAndMessages(null);
                    e.printStackTrace();
                }
            }

        }
    }

    public boolean isConnected() {
        if (mBluetoothAdapter.getBondedDevices().contains(mBluetoothDevice)) {
            return true;
        }
        return false;
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i(TAG, "on Connection State Change");
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTING:
                    Log.i(TAG, "STATE_CONNECTING");
                    sendACK(MAC, "CONNECTING");
                    break;
                case BluetoothProfile.STATE_CONNECTED:
                    boolean connected = true;
                    reconnectHandler.removeCallbacksAndMessages(null);

                    Log.i(TAG, "STATE_CONNECTED");
                    sendACK(MAC, "CONNECTED");
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    connected = false;
                    Log.e(TAG, "STATE_DISCONNECTED");
                    sendACK(MAC, "DISCONNECTED");

                    try {
                        if(mGatt!=null){
                            mGatt.disconnect();
                        }
                        //reconnectHandler.removeCallbacksAndMessages(null);
                        reconnectHandler.postDelayed(new Runnable() {
                            public void run() {
                                connectToDevice(MAC);
                            }
                        }, 5000);
                        break;
                    } catch (Exception e) {
                        //reconnectHandler.removeCallbacksAndMessages(null);
                        e.printStackTrace();
                        break;
                    }



                default:
                    connected = false;
                    Log.i(TAG, "STATE_OTHER");
                    sendACK(MAC, "STATE_OTHER");
                    break;
            }
        }


        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            //Log.i(TAG,"on Services Discovered");


            mGatt = gatt;
            List<BluetoothGattService> services = gatt.getServices();

            setNotifySensor(gatt);
        }

        /*
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
            //Log.i(TAG,"on Characteristic Read");
            //byte[] value = characteristic.getValue();
            //String uuid = characteristic.getUuid().toString();
            //Log.d(TAG, "value " + String.format("%skg", value[0]));
        }
        */

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic
                                                  characteristic, int status) {
            //Log.i(TAG,"on Characteristic Write");

            characteristic = gatt.getService(SERVICE)
                    .getCharacteristic(CHAR);
            gatt.readCharacteristic(characteristic);

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic
                                                    characteristic) {

            //Log.i(TAG,"On Characteristic Changed");

            byte[] bytevalue = characteristic.getValue();
            String parsablevalue = new String(bytevalue, Charset.forName("utf-8"));

            String stringValue=parsablevalue.replaceAll("[^\\p{Print}]","");

            sendResults(stringValue,MAC);


        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt,
                                      BluetoothGattDescriptor descriptor, int status) {

            //Log.i(TAG,"on Descriptor Write");

            BluetoothGattCharacteristic characteristic;
            characteristic = gatt.getService(SERVICE)
                    .getCharacteristic(CHAR);

            gatt.readCharacteristic(characteristic);
        }

        private void setNotifySensor(BluetoothGatt gatt) {

            //Log.i(TAG,"set notify sensor");

            BluetoothGattCharacteristic characteristic;
            characteristic = gatt.getService(SERVICE)
                    .getCharacteristic(CHAR);

            //local
            gatt.setCharacteristicNotification(characteristic, true);

            //remote

            BluetoothGattDescriptor desc = characteristic.getDescriptor(CONFIG_DESCRIPTOR);
            //Log.i(TAG,"descriptors "+characteristic.getDescriptors().toString());
            desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(desc);
            activateNormalfeed();
        }

    };


    private void normalfeed(BluetoothGatt gatt){

        Log.i(TAG, "normalfeed");

        BluetoothGattCharacteristic characteristic;

        try {
            characteristic = gatt.getService(SERVICE)
                    .getCharacteristic(CHAR);

            characteristic.setValue("0");
            mGatt.writeCharacteristic(characteristic);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public class MyBinder extends Binder {
        GattService getService() {
            return GattService.this;
        }
    }

}
