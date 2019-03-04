package bt.proto.device;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import bt.proto.R;

public class deviceActivity extends AppCompatActivity implements serviceEntryInterface, characteristicEntryInterface {

    private final String TAG = "deviceActivity";

    private Intent intentGATT;

    private View serviceContainer;
    private View characteristicsContainer;

    static GattService BoundService;
    private TextView deviceName;

    private LayoutInflater layoutInflater;

    private RecyclerView serviceRecyclerView;
    //private RecyclerView characteristicRecycleView;



    private serviceAdapter serviceAdapter;
    private characteristicAdapter characteristicAdapter;

    private serviceEntryController serviceEntryController;
    private characteristicEntryController characteristicEntryController;

    private int serviceClicked;
    private int characteristicClicked;

    private List<String> ServiceUuidList = new ArrayList<>();
    private List<String> ServiceLongUuidList = new ArrayList<>();
    private List<String> serviceNameList = new ArrayList<>();
    private List<serviceEntry> serviceListUI = new ArrayList<>();

    private List<String> characteristicUuidList = new ArrayList<>();
    private List<String> characteristicLongUuidList = new ArrayList<>();
    private List<String> characteristicNameList = new ArrayList<>();
    private List<characteristicEntry> characteristicListUi = new ArrayList<>();
    private List<BluetoothGattCharacteristic> characteristics;

    private List<BluetoothGattDescriptor> descriptors;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.layout_device_main);

        serviceContainer = findViewById(R.id.service_container);
        serviceRecyclerView = findViewById(R.id.service_list);

        characteristicsContainer = findViewById(R.id.characteristic_container);

        layoutInflater = getLayoutInflater();

        String MAC = getIntent().getExtras().getString("MAC");
        connectToMac(MAC);


        deviceName = findViewById(R.id.deviceMac);
        deviceName.setText(MAC);

        LocalBroadcastManager.getInstance(this).registerReceiver(StateReceiver
                , new IntentFilter("GattMessage"));

    }

    private void connectToMac(String mac){

        intentGATT = new Intent(this, GattService.class);
        Bundle gattBundle = new Bundle();
        gattBundle.putString("MAC", mac);
        intentGATT.putExtras(gattBundle);

        this.bindService(intentGATT, gattServiceConnection, Context.BIND_AUTO_CREATE);
        this.startService(intentGATT);
        Log.i(TAG,"Connection initiated");

    }

    private ServiceConnection gattServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            GattService.MyBinder myBinder = (GattService.MyBinder) service;
            BoundService = myBinder.getService();
        }
    };

    private BroadcastReceiver StateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle intentbundle = intent.getExtras();
            Log.i(TAG,""+intentbundle);

            if(intent.getStringExtra("packetContents").equals("ACK")){
                try{
                    String state = intent.getStringExtra("state");
                    Log.i(TAG,"device "+state);
                    return;
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

            if(intent.getStringExtra("packetContents").equals("servicesDiscovered")) {
                try{
                    List<BluetoothGattService> services = BoundService.getServices();

                    for(int i = 0; i < services.size(); i++){
                        Log.i(TAG,"services: "+services.get(i).getUuid().toString());
                    }

                    MapServiceUuids(services);

                    Log.i(TAG,"servicesList: "+serviceNameList.toString());

                    serviceEntryController = new serviceEntryController(deviceActivity.this);
                    for(int i =0;i<serviceNameList.size();i++){
                        serviceEntry serviceEntry = new serviceEntry(serviceNameList.get(i), ServiceUuidList.get(i));
                        serviceListUI.add(serviceEntry);
                    }

                    serviceEntryController.createServiceList(serviceListUI);


                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

        }
    };

    private String getStringResourceByName(String aString) {
        String packageName = getPackageName();
        int resId = getResources().getIdentifier(aString, "string", packageName);
        return getString(resId);
    }

    private void MapServiceUuids(List<BluetoothGattService> services){

        for(int i=0;i<services.size();i++){
            String longUuid = services.get(i).getUuid().toString();
            ServiceLongUuidList.add(longUuid);
            String[] shortUuid = longUuid.split("-");
            String knaiftunk = shortUuid[0].replace("0000","0x");

            try {
                String mapToUUID = "a"+knaiftunk;
                serviceNameList.add(getStringResourceByName(mapToUUID));
                ServiceUuidList.add(knaiftunk);
            } catch (Resources.NotFoundException e) {
                serviceNameList.add("Unknown Service");
                ServiceUuidList.add(longUuid);

            }

        }
        Log.i(TAG,""+serviceNameList.toString());
        Log.i(TAG,""+ServiceUuidList.toString());
    }

    private void MapCharacteristicUuids(List<BluetoothGattCharacteristic> characteristics){

        for(int i=0;i<characteristics.size();i++){
            String longUuid = characteristics.get(i).getUuid().toString();
            characteristicLongUuidList.add(longUuid);
            String[] shortUuid = longUuid.split("-");
            String knaiftunk = shortUuid[0].replace("0000","0x");

            try {
                String mapToUUID = "a"+knaiftunk;
                characteristicNameList.add(getStringResourceByName(mapToUUID));
                characteristicUuidList.add(knaiftunk);
            } catch (Resources.NotFoundException e) {
                characteristicNameList.add("Unknown Characteristic");
                characteristicUuidList.add(longUuid);

            }

        }
        Log.i(TAG,""+serviceNameList.toString());
        Log.i(TAG,""+ServiceUuidList.toString());
    }

    @Override
    public void setUpServiceAdapterAndView(List<serviceEntry> serviceEntries) {


            this.serviceListUI = serviceEntries;

            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    serviceRecyclerView.setLayoutManager(linearLayoutManager);
                    serviceAdapter = new serviceAdapter();
                    serviceRecyclerView.setAdapter(serviceAdapter);

                }
            });

        }




    private class serviceAdapter extends RecyclerView.Adapter<serviceAdapter.serviceViewHolder>{


        @NonNull
        @Override
        public serviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View v = layoutInflater.inflate(R.layout.layout_device_service_root, parent, false);
            return new serviceViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull final serviceViewHolder holder, int position) {

            serviceEntry currentItem = serviceListUI.get(position);

            final String name = currentItem.getName();
            final String uuid = currentItem.getUuid();
            final RecyclerView characteristicRecycleView;

            holder.name.setText(name);
            holder.uuid.setText(uuid);
            serviceClicked = position;


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    Log.i(TAG,"serviceClicked uuid: "+uuid.substring(2));

                    for(int i=0;i<serviceListUI.size();i++){
                        String splicedUuid = ServiceLongUuidList.get(i).substring(4,8);
                        Log.i(TAG,"splicedUuid: "+splicedUuid);
                        if(uuid.substring(2).equals(splicedUuid)){
                            characteristics = BoundService.getCharacteristics(ServiceLongUuidList.get(i));
                        }
                    }

                    MapCharacteristicUuids(characteristics);


                    for(int i=0; i < characteristics.size() ; i++){
                        Log.i(TAG,"characteristic: "+characteristics.get(i).getUuid());

                        characteristicEntryController = new characteristicEntryController(deviceActivity.this);
                        for(int a =0;a<characteristics.size();a++){
                            characteristicEntry characteristicEntry = new characteristicEntry(characteristicNameList.get(a), characteristicUuidList.get(a));
                            characteristicListUi.add(characteristicEntry);
                        }

                        Log.i(TAG,characteristicListUi.toString());

                        characteristicEntryController.createCharacteristicList(characteristicListUi);
                    }

                }
            });

        }


        @Override
        public int getItemCount() {
            return serviceListUI.size();
        }

        class serviceViewHolder extends RecyclerView.ViewHolder{

            private ViewGroup serviceContainer;
            private TextView name;
            private TextView uuid;
            private RecyclerView characteristicContainer;

            serviceViewHolder(View serviceView) {
                super(serviceView);

                this.serviceContainer=(ViewGroup) serviceView.findViewById(R.id.service_container);
                this.name = (TextView) serviceView.findViewById(R.id.device_service_result_service_name);
                this.uuid = (TextView) serviceView.findViewById(R.id.device_service_result_service_uuid);
                this.characteristicContainer = serviceView.findViewById(R.id.characteristic_list);
            }

        }

    }

    @Override
    public void setUpCharacteristicAdapterAndView(List<characteristicEntry> characteristicEntriesEntries) {


        this.characteristicListUi = characteristicEntriesEntries;

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                characteristicRecycleView.setLayoutManager(linearLayoutManager);
                characteristicAdapter = new characteristicAdapter();
                characteristicRecycleView.setAdapter(characteristicAdapter);

            }
        });

    }

    private class characteristicAdapter extends RecyclerView.Adapter<characteristicAdapter.characteristicViewHolder>{


        @NonNull
        @Override
        public characteristicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View v = layoutInflater.inflate(R.layout.layout_device_characteristic_root, parent, false);
            return new characteristicViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull characteristicViewHolder holder, int position) {

            characteristicEntry  currentItem = characteristicListUi.get(position);

            final String name = currentItem.getName();
            final String uuid = currentItem.getUuid();
            RecyclerView descriptorRecycleView = findViewById(R.id.characteristic_list);

            holder.name.setText(name);
            holder.uuid.setText(uuid);
            characteristicClicked = position;


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    Log.i(TAG,"characteristic Clicked uuid: "+uuid.substring(2));

                    for(int i=0;i<characteristicListUi.size();i++){
                        String splicedUuid = characteristicLongUuidList.get(i).substring(4,8);
                        Log.i(TAG,"splicedUuid: "+splicedUuid);
                        if(uuid.substring(2).equals(splicedUuid)){
                            descriptors = BoundService.getDescriptors(characteristicLongUuidList.get(i));
                        }
                    }

                    //characteristics = BoundService.getCharacteristics(uuid);
                    for(int i=0; i < descriptors.size() ; i++){
                        Log.i(TAG,"descriptors: "+descriptors.get(i));
                    }

                }
            });

        }


        @Override
        public int getItemCount() {
            return serviceListUI.size();
        }

        class characteristicViewHolder extends RecyclerView.ViewHolder{

            private ViewGroup characteristicContainer;
            private TextView name;
            private TextView uuid;

            characteristicViewHolder(View characteristicView) {
                super(characteristicView);

                this.characteristicContainer=(ViewGroup) characteristicView.findViewById(R.id.characteristic_container);
                this.name = (TextView) characteristicView.findViewById(R.id.characteristicName);
                this.uuid = (TextView) characteristicView.findViewById(R.id.characteristicUUID);
            }
        }

    }

}
