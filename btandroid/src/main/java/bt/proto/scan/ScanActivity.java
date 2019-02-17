package bt.proto.scan;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
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
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

import bt.proto.MainActivity;
import bt.proto.R;
import bt.proto.database.EntryDatabase;
import bt.proto.database.SettingsEntry;
import bt.proto.device.GattService;
import bt.proto.device.deviceActivity;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;


public class ScanActivity extends AppCompatActivity implements scanInterface {

    private final String TAG = "ScanActivity";

    private LayoutInflater layoutInflater;
    private RecyclerView recyclerView;
    private scanAdapter adapter;
    private int clicked;
    List<scanEntry> scanEntries;

    static GattService BoundService;
    Intent intentGATT = null;
    private String Remotemac;
    private TextView macText;

    private View scanContainer;
    private TextView scanButton;
    private Button connectButton;


    private TextView results;
    private boolean resultsFound = false;

    private scanController scanController;
    private int selectedPosition=-1;

    private EntryDatabase entryDatabase;
    private SettingsEntry settingsEntry;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.layout_scan_main);

        Log.i(TAG, "onCreate");

        layoutInflater = getLayoutInflater();

        entryDatabase = EntryDatabase.getDataBase(this);

        scanContainer = findViewById(R.id.scan_container);
        scanContainer.setVisibility(GONE);

        scanButton = findViewById(R.id.scanButton);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(scanContainer.getVisibility()==GONE){
                    scanContainer.setVisibility(VISIBLE);

                    scanController = new scanController(ScanActivity.this);
                    scanController.startScan();
                    recyclerView = findViewById(R.id.scan_list);

                    results = findViewById(R.id.scan_info);

                    resultsFound = false;
                    Animation anim = new AlphaAnimation(0.5f, 1.0f);
                    anim.setDuration(1500);
                    anim.setStartOffset(20);
                    anim.setRepeatMode(Animation.REVERSE);
                    anim.setRepeatCount(Animation.INFINITE);
                    results.setText(R.string.scanScanning);
                    results.startAnimation(anim);

                    connectButton = findViewById(R.id.connect);
                    connectButton.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            Intent DeviceIntent = new Intent(getBaseContext(), deviceActivity.class);
                            DeviceIntent.putExtra("MAC",Remotemac);
                            startActivity(DeviceIntent);

                        }
                    });

                    if(!resultsFound){
                        Animation anim1 = new AlphaAnimation(0.5f, 1.0f);
                        anim1.setDuration(1500);
                        anim1.setStartOffset(20);
                        anim1.setRepeatMode(Animation.REVERSE);
                        anim1.setRepeatCount(Animation.INFINITE);
                        results.setText(R.string.scanScanning);
                        results.startAnimation(anim1);
                    }

                }
                else{
                    scanContainer.setVisibility(GONE);
                }
            }
        });


        new Thread(new Runnable() {
            @Override
            public void run() {
                if (entryDatabase.settingsDao().CheckIfEmpty() == 0) {

                    SettingsEntry initSettingsEntry = new SettingsEntry(0,"");
                    entryDatabase.settingsDao().insertEntry(initSettingsEntry);
                    Log.i(TAG,"new settingsEntry created");
                    settingsEntry = initSettingsEntry;
                }
                else{
                    settingsEntry = entryDatabase.settingsDao().getSettingsEntry();
                }
                Log.i(TAG,"settings mac: "+settingsEntry.getMac());
            }
        }).start();

    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        Intent mainActivity= new Intent(ScanActivity.this, MainActivity.class);
        ScanActivity.this.startActivity(mainActivity);
    }


    @Override
    public void setUpAdapterAndView(List<scanEntry> scanEntries) {

        this.scanEntries = scanEntries;

        if(scanEntries.size()==0){
            if(!(ScanActivity.this.isFinishing())) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        results.clearAnimation();
                         results.setText(getString(R.string.scanNoResultsFound));
                        //ErrorDialogue();
                    }
                });
            }
        }

        if(scanEntries.size()>=1 ){
            if(!(ScanActivity.this.isFinishing())){
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        results.clearAnimation();
                        results.setText(getString(R.string.scanResultsFound));
                    }
                });
            }
        }

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                resultsFound=true;

                results.clearAnimation();

                recyclerView.setLayoutManager(linearLayoutManager);
                adapter = new scanAdapter();
                recyclerView.setAdapter(adapter);

            }
        });

    }

    @Override
    public void chooseScanResultAt(int position) {

        //scanEntries.remove(position);
        //adapter.notifyItemRemoved(position);
    }

    @Override
    public void forgetScanResultAt(int position) {

    }

    private class scanAdapter extends RecyclerView.Adapter<scanAdapter.scanViewHolder>{


        @NonNull
        @Override
        public scanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View v = layoutInflater.inflate(R.layout.layout_scan_result, parent, false);
            return new scanViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull scanViewHolder holder, int position) {

            scanEntry currentItem = scanEntries.get(position);

            String name = currentItem.getName();
            final String mac = currentItem.getMac();
            final Boolean checked = currentItem.getChecked();

            if(name.isEmpty()){
                name = "unknown device";
            }

            holder.name.setText(name);
            holder.mac.setText(mac);

            if(checked){
                holder.checkBox.setChecked(true);
            }
            else{
                holder.checkBox.setChecked(false);
            }

            clicked = position;


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedPosition = clicked;
                    Log.i(TAG,"mac clicked: "+mac);

                    Remotemac = mac;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            settingsEntry.setMac(mac);
                            entryDatabase.settingsDao().updateEntry(settingsEntry);
                        }
                    });
                    selectMac(mac);
                    notifyDataSetChanged();

                }
            });
        }

        private void selectMac(String mac) {

            for (int j = 0; j < scanEntries.size(); j++) {
                scanEntries.get(j).setChecked(false);
            }

            for (int j = 0; j < scanEntries.size(); j++) {
                if (scanEntries.get(j).getMac().equals(mac)) {
                    if (!scanEntries.get(j).getChecked()) {
                        scanEntries.get(j).setChecked(true);
                    }
                }
            }
        }

        @Override
        public int getItemCount() {
            return scanEntries.size();
        }


        class scanViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

            private ViewGroup scanContainer;
            private TextView name;
            private TextView mac;
            private CheckBox checkBox;


            scanViewHolder(View scanView) {
                super(scanView);

                this.scanContainer=(ViewGroup) scanView.findViewById(R.id.root_scan_result_item);
                this.name = (TextView) scanView.findViewById(R.id.scan_root_name_text);
                this.mac = (TextView) scanView.findViewById(R.id.scan_root_mac_text);
                this.checkBox = (CheckBox) scanView.findViewById(R.id.scan_root_checkBox);
                this.scanContainer.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                scanEntry scanEntry = scanEntries.get(
                        this.getAdapterPosition());
                selectMac(mac.toString());
                scanController.onScanResultClick(scanEntry, v);
            }
        }



    }






}


