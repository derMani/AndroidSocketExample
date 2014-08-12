package de.hsos.sawida.asc;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import de.hsos.sawida.asc.IRemoteService;


public class AnedoServiceComponent extends Service {
    Context context;
    private MySocketStarter mySocketStarter;
    private boolean socketOpen;

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(getClass().getSimpleName(), "onBind()");
        return myRemoteServiceStub;

    }
    private IRemoteService.Stub myRemoteServiceStub = new IRemoteService.Stub() {
        public String getOsnabrueck() throws RemoteException {
            return "Osnabruck";
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

        Log.d(getClass().getSimpleName(), "onStart()");
        return super.onStartCommand(intent, flags, startId);
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


