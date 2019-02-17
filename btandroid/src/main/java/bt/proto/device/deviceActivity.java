package bt.proto.device;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import bt.proto.R;

public class deviceActivity extends AppCompatActivity {

    private final String TAG = "deviceActivity";

    private Intent intentGATT;

    static GattService BoundService;
    private TextView deviceName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.layout_device_main);

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
        this.startService(intentGATT);
        Log.i(TAG,"Connection initiated");

    }

    private android.content.ServiceConnection ServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //right_connect.setColorFilter(Color.parseColor("#df3442"));
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

            String state = intent.getStringExtra("state");

            Log.i(TAG,"device "+state);


        }
    };
}
