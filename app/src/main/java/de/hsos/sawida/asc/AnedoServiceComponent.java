package de.hsos.sawida.asc;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import de.hsos.sawida.asc.IRemoteService;


public class AnedoServiceComponent extends Service implements LocationListener{
    Context context;
    private MySocketStarter mySocketStarter;
    private boolean socketOpen;
    private String provider;
    private LocationManager locationManager;
    private LocationListener listener = this;


    @Override
    public IBinder onBind(Intent intent) {
        Log.d(getClass().getSimpleName(), "onBind()");
        return myRemoteServiceStub;

    }
    private IRemoteService.Stub myRemoteServiceStub = new IRemoteService.Stub() {

        public String getOsnabrueck() throws RemoteException {
            return "Osnabruck";
        }

        @Override
        public Location getLocation() throws RemoteException {
            return locationManager.getLastKnownLocation(provider);
        }
    };


    public void onCreate() {
        super.onCreate();
        Log.d(getClass().getSimpleName(), "onCreate()");
        context = this;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
//        serviceHandler.removeCallbacks(myTask);
//        serviceHandler = null;
        Log.d(getClass().getSimpleName(),"onDestroy()");
        mySocketStarter.closeSocket();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        serviceHandler = new Handler();
//        serviceHandler.postDelayed(myTask, 1000L);


        mySocketStarter = new MySocketStarter();
        new Thread(mySocketStarter).start();

        getGEOLocation();

        Log.d(getClass().getSimpleName(), "onStart()");
        return super.onStartCommand(intent, flags, startId);
    }

    public void getGEOLocation() {

        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the locatioin provider -> use
        // default
        Criteria criteria = new Criteria();
        provider = LocationManager.NETWORK_PROVIDER;
        locationManager.requestLocationUpdates(provider,0,0,this);
        Location location = locationManager.getLastKnownLocation(provider);
            System.out.println("Provider " + provider + " has been selected.");
            onLocationChanged(location);
    }

    @Override
    public void onLocationChanged(Location location) {
        int lat = (int) (location.getLatitude());
        int lng = (int) (location.getLongitude());
        Log.d("INFO","Lat " + lat + " , Long "+ lng );
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {
        Toast.makeText(this, "Enabled new provider " + provider,
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProviderDisabled(String s) {

    }


    public class MySocketStarter implements Runnable {


        private ServerSocket serverSocket;

        Thread serverThread = null;

        public static final int SERVERPORT = 63500;


        public void run() {
            Socket socket = null;
            try {
                serverSocket = new ServerSocket(SERVERPORT);

            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Log.d("INFO", "Thread started, waiting for socket");
                    socket = serverSocket.accept();

                    CommunicationThread commThread = new CommunicationThread(socket);
                    new Thread(commThread).start();

                } catch (IOException e) {
                    Thread.currentThread().interrupt();
                    Log.d("INFO", "FAILED SOCKET ");
                }
            }
        }

        public void closeSocket() {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        class CommunicationThread implements Runnable {


            private Socket clientSocket;

            private BufferedReader input;

            public CommunicationThread(Socket clientSocket) {

                this.clientSocket = clientSocket;

                try {

                    this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            public void run() {

                while (!Thread.currentThread().isInterrupted()) {

                    try {

                        String read = input.readLine();
                        Intent intent = new Intent("msgReceived");
                        intent.putExtra("message",read);

                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);


                        if (read.equals("www.test.de")) {
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse("http://www.test.de"));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }
}


