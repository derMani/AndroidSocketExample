package de.hsos.sawida.asc;
import android.content.Context;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anedo_service_component);

        Button start = (Button)findViewById(R.id.button_startService);
        Button stop = (Button)findViewById(R.id.buttonbuttton_stop_service);

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

}
