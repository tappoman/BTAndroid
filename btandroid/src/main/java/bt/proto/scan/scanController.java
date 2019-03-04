package bt.proto.scan;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class scanController {

    private final String TAG = "ScanController";
    private scanInterface view;
    List<scanEntry> scanEntries = new ArrayList<>();
    private boolean mScanning = true;
    private int SCAN_PERIOD = 5000;

    private BluetoothAdapter mBluetoothAdapter  = BluetoothAdapter.getDefaultAdapter();
    private BluetoothLeScanner mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

    private Handler mHandler = new Handler();


    scanController(scanInterface view){
        this.view=view;
        startScan();
    }

    private ScanCallback mLeScanCallback =
            new ScanCallback() {

                @Override
                public void onScanResult(int callbackType, final ScanResult result) {
                    //super.onScanResult(callbackType, result);
                    Log.i(TAG+"BLE", ""+result.getDevice());

                    String name = result.getDevice().getName();
                    String mac = result.getDevice().getAddress();

                    int rssi = result.getRssi();
                    //scanEntry newEntry = new scanEntry(mac,"",rssi);
                    for (int i=0;i<=scanEntries.size();i++){
                        if(scanEntries.isEmpty()){
                            if(name==null){
                                //scanEntry newEntry = new scanEntry(mac,"",rssi,false);
                                //scanEntries.add(newEntry);
                                break;
                            }
                            else{
                                scanEntry newEntry = new scanEntry(mac,name,rssi,false);
                                scanEntries.add(newEntry);
                                break;
                            }
                        }
                        if(scanEntries.get(i).getMac().equals(mac)){
                            break;
                        }
                        else{
                            if(i==scanEntries.size()-1){
                                if(name==null){
                                    //scanEntry newEntry = new scanEntry(mac,"",rssi,false);
                                    //scanEntries.add(newEntry);
                                    break;
                                }
                                else{
                                    scanEntry newEntry = new scanEntry(mac,name,rssi,false);
                                    scanEntries.add(newEntry);
                                    break;
                                }
                            }
                        }
                    }
                }

                @Override
                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);
                    Log.i(TAG, "scan error");
                }

            };

    public void startScan() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                Looper.prepare();
                scanLeDevice(true);
            }
        }).start();
    }

    private void sendScanResults() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                Looper.prepare();
                for(int i = 0;i<scanEntries.size();i++){
                    if(i==0){
                        continue;
                    }
                    else{
                        for(int j=0;j<scanEntries.size();j++){
                            if(scanEntries.get(i).getRssi()>scanEntries.get(j).getRssi()){
                                Collections.swap(scanEntries,i,j);
                            }
                        }
                    }

                }
                view.setUpAdapterAndView(scanEntries);
            }
        }).start();
    }


    private void scanLeDevice(final boolean enable) {

        ScanSettings mScanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).
                        setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES).build();

        if (enable) {

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    Log.i(TAG, "scanning stopped");
                    try{
                        mBluetoothLeScanner.stopScan(mLeScanCallback);
                        sendScanResults();
                    }
                    catch(IllegalStateException e){

                    }
                }
            }, SCAN_PERIOD);

            mScanning = true;
            Log.i(TAG, "scanning");

            mBluetoothLeScanner.startScan(mLeScanCallback);
        } else {
            mScanning = false;
            Log.i(TAG, "scanning stopped");

            mBluetoothLeScanner.stopScan(mLeScanCallback);
        }
    }


    void onScanResultClick(scanEntry scanEntry, View v) {
        Log.i(TAG, "clicked: "+scanEntry.getMac());
    }


    public void onScanResultSwiped(int position, final scanEntry scanEntry) {

        view.chooseScanResultAt(position);
    }

}
