package com.example.klienOrder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;


public class ClientService extends Service {
    private String ipAddress;
    private int COUNTER = 1;
    NotificationManager NM;
    static final int SERVER_PORT = 5000;
    Handler handler = new Handler();
    static Socket socket;
    PrintWriter printWriter;

    IBinder mBinder = new LocalBinder();

    public ClientService() {
    }

    public class LocalBinder extends Binder {
        public ClientService getClientServiceInstance() {
            return ClientService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    @SuppressWarnings("deprecation")
    @Override
    public void onStart(Intent intent, int startId) {
        try {
            ipAddress = intent.getStringExtra("IP_ADDRESS");
            Thread clientThread = new Thread(new ClientThread(ipAddress));
            clientThread.start();
            Toast.makeText(this, "Order Receiver Service Started", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, String.format("Order Receiver Service Error : %s", e.getMessage()), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Order Receiver Service Ended", Toast.LENGTH_LONG).show();
    }

    public class ClientThread implements Runnable {
        private String ipAddress;

        public ClientThread() {
        }

        public ClientThread(String ipAddress) {
            this.ipAddress = ipAddress;
        }


        @Override
        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName(ipAddress);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getBaseContext(),
                                "Connecting to the server",
                                Toast.LENGTH_LONG).show();

                    }
                });

                socket = new Socket(serverAddr, SERVER_PORT);
                try {
                    printWriter = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream())),
                            true);

                    //---get an InputStream object to read from the server---
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));

                    try {
                        //---read all incoming data terminated with a \n
                        // char---
                        String line = null;
                        while ((line = br.readLine()) != null) {
                            final String strReceived = line;

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (strReceived.startsWith("NOTIFY")) {
                                        String[] split = strReceived.split("#");
                                        createNotify(split[1].toString(), split[2].toString(), split[3].toString(), split[4].toString());
                                    }
                                }
                            });
                        }

                        //---disconnected from the server---
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getBaseContext(),
                                        "Client disconnected",
                                        Toast.LENGTH_LONG).show();
                            }
                        });

                    } catch (Exception e) {
                        final String error = e.getLocalizedMessage();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getBaseContext(),
                                        error,
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                } catch (Exception e) {
                    final String error = e.getLocalizedMessage();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getBaseContext(),
                                    error,
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getBaseContext(),
                                "Connection closed.",
                                Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                final String error = e.getLocalizedMessage();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getBaseContext(),
                                error,
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        }

        @SuppressWarnings("deprecation")
        public void createNotify(String noTable, String idMember, String idProduct, String note) {
            try {
                COUNTER++;
                String title = "Order Cooking Done";
                String subject = String.format("Table No. %s", noTable);
                String body = note;
                NM=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                Notification notify=new Notification(R.drawable.
                        ic_stat_name,title,System.currentTimeMillis());
                PendingIntent pending=PendingIntent.getActivity(
                        getApplicationContext(),0, new Intent(),0);
                notify.setLatestEventInfo(getApplicationContext(),subject,body,pending);
                Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        notify.flags = Notification.FLAG_NO_CLEAR;
                notify.sound = alarmSound;
                NM.notify(Integer.valueOf(noTable + idMember + idProduct + COUNTER), notify);
//            NM.notify(0, notify);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    public void onClickSend(String data) {
        printWriter.println(data);
    }


}
