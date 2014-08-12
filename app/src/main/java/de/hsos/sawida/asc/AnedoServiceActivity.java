package de.hsos.sawida.asc;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;



public class AnedoServiceActivity extends Activity {

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            String message = intent.getStringExtra("message");
            Log.d("receiver", "Got message: " + message);
            updateUIMessageHandler = new Handler();
            updateUIMessageHandler.post(new UpdateUIThread(message));
        }
    };

    private TextView text;
    private boolean started;
    private Handler updateUIMessageHandler;
    private RemoteServiceConnection conn = null;

    private IRemoteService remoteService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anedo_service_component);

        Button start = (Button)findViewById(R.id.button_startService);
        Button stop = (Button)findViewById(R.id.buttonbuttton_stop_service);

        Button bind = (Button)findViewById(R.id.buttonBindService);

        Button invoke = (Button)findViewById(R.id.buttonInvokeService);

        invoke.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                invokeService();
            }
        });


        bind.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                bindService();
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                startService();
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                stopService();
            }
        });

        text =  (TextView)findViewById( R.id.textView_log);
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("msgReceived"));

    }

    @Override
    protected void onPause() {
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.anedo_service_component, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startService(){
        if (started) {
            Toast.makeText(AnedoServiceActivity.this, "Service already started", Toast.LENGTH_SHORT).show();
        } else {



            Intent i = new Intent();
            String nameOfClass = "AnedoServiceComponent";
            try {
                Class<?> clazz = Class.forName("de.hsos.sawida.asc."+ nameOfClass);
                i.setClass(this,clazz);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            startService(i);
            started = true;
            updateServiceStatus();
            Log.d( getClass().getSimpleName(), "startService()" );
        }

    }
    private void stopService() {
        if (!started) {
            Toast.makeText(AnedoServiceActivity.this, "Service not yet started", Toast.LENGTH_SHORT).show();
        } else {
            Intent i = new Intent();
            String nameOfClass = "AnedoServiceComponent";

            try {
                Class<?> clazz = Class.forName("de.hsos.sawida.asc."+ nameOfClass);
                i.setClass(this,clazz);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            stopService(i);
            started = false;
            updateServiceStatus();
            Log.d( getClass().getSimpleName(), "stopService()" );
        }
    }

    private void updateServiceStatus() {
        String startStatus = started ? "started" : "not started";
        String statusText = "Service status: " + startStatus;
        TextView t = (TextView)findViewById( R.id.textView_asc_serviceStatus);
        t.setText( statusText );
    }

    class UpdateUIThread implements Runnable {

        private String msg;

        public UpdateUIThread(String str) {
            this.msg = str;
        }

        @Override
        public void run() {
            text.setText("Client Says: " + msg + "\n");
        }
    }


    private void bindService() {
        if(conn == null) {
            conn = new RemoteServiceConnection();
            Intent i = new Intent();
            String nameOfClass = "AnedoServiceComponent";
            try {
                Class<?> clazz = Class.forName("de.hsos.sawida.asc."+ nameOfClass);
                i.setClass(this,clazz);
                bindService(i, conn, Context.BIND_AUTO_CREATE);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
;
            Log.d( getClass().getSimpleName(), "bindService()" );
        } else {
            Toast.makeText(this, "Cannot bind - service already bound", Toast.LENGTH_SHORT).show();
        }
    }

    private void releaseService() {
        if(conn != null) {
            unbindService(conn);
            conn = null;
            Log.d( getClass().getSimpleName(), "releaseService()" );
        } else {
            Toast.makeText(this, "Cannot unbind - service not bound", Toast.LENGTH_SHORT).show();
        }
    }
    private void invokeService() {
        if(conn == null) {
            Toast.makeText(this, "Cannot invoke - service not bound", Toast.LENGTH_SHORT).show();
        } else {
            try {
                String city = remoteService.getOsnabrueck();

                Toast.makeText(this, city, Toast.LENGTH_SHORT).show();
                Log.d( getClass().getSimpleName(), "invokeService()" );
            } catch (RemoteException re) {
                Log.e( getClass().getSimpleName(), "RemoteException" );
            }
        }
    }


    class RemoteServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName className,
                                       IBinder boundService ) {
            remoteService = IRemoteService.Stub.asInterface((IBinder)boundService);
            Log.d( getClass().getSimpleName(), "onServiceConnected()" );
        }

        public void onServiceDisconnected(ComponentName className) {
            remoteService = null;
            updateServiceStatus();
            Log.d(getClass().getSimpleName(), "onServiceDisconnected" );
        }
    };

}
