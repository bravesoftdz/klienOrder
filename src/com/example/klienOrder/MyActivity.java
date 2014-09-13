package com.example.klienOrder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;



import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.widget.*;


public class MyActivity extends Activity {
    TableLayout tbLayout;
    EditText editID, editName, editPrice, editQty, editIP1, editIP2, editIDw, editWn, editIDm, editMn;
    TextView textView1;
    RadioButton radW, radM, radP;
    int count;
    static final int SERVER_PORT = 5000;

    Handler handler = new Handler();
    static Socket socket;

    StringBuffer dataWillBeTransferred;

    PrintWriter printWriter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        editID = (EditText) findViewById(R.id.etIDProd);
        editName = (EditText) findViewById(R.id.etProdName);
        editPrice = (EditText) findViewById(R.id.etPrice);
        editQty = (EditText) findViewById(R.id.etQty);
        editIP1 = (EditText) findViewById(R.id.editIP1);
        editIP2 = (EditText) findViewById(R.id.editIP2);

        editIDm = (EditText) findViewById(R.id.editMID);
        editIDw = (EditText) findViewById(R.id.editWID);

        editWn = (EditText) findViewById(R.id.editWName);
        editMn = (EditText) findViewById(R.id.editMName);

        tbLayout = (TableLayout) findViewById(R.id.tbLayout);
        textView1 = (TextView) findViewById(R.id.textViewLog);
        count = 1;
        radM = (RadioButton) findViewById(R.id.rdbMember);
        radP = (RadioButton) findViewById(R.id.rdbProd);
        radW = (RadioButton) findViewById(R.id.rdbWaiter);
        dataWillBeTransferred = new StringBuffer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    public void onClickScan(View v) {
        try {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (Exception e) {
            Toast.makeText(getBaseContext(),
                    e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickAdd(View v) {
        TextView tvNo = new TextView(getApplicationContext());
        TextView tvID = new TextView(getApplicationContext());
        TextView tvName = new TextView(getApplicationContext());
        TextView tvPrice = new TextView(getApplicationContext());
        TextView tvQty = new TextView(getApplicationContext());
        TableRow tr = new TableRow(getApplicationContext());
        tvNo.setText(String.valueOf(count));
        tvID.setText(editID.getText());
        tvName.setText(editName.getText());
        tvPrice.setText(editPrice.getText());
        tvQty.setText(editQty.getText());
        tr.addView(tvNo);
        tr.addView(tvID);
        tr.addView(tvName);
        tr.addView(tvPrice);
        tr.addView(tvQty);
        tbLayout.addView(tr);

        dataWillBeTransferred.append("/" + tvID.getText().toString());
        dataWillBeTransferred.append("#" + tvName.getText().toString());
        dataWillBeTransferred.append("#" + tvPrice.getText().toString());
        dataWillBeTransferred.append("#" + tvQty.getText().toString());

        count++;

    }

    public void onClickRem(View v){
        tbLayout.removeAllViews();
        count = 1;
        TextView tvNo = new TextView(getApplicationContext());
        TextView tvID = new TextView(getApplicationContext());
        TextView tvName = new TextView(getApplicationContext());
        TextView tvPrice = new TextView(getApplicationContext());
        TextView tvQty = new TextView(getApplicationContext());
        TableRow tr = new TableRow(getApplicationContext());
        tvNo.setText("No.");
        tvID.setText("Prod.ID");
        tvName.setText("Name");
        tvPrice.setText("Price");
        tvQty.setText("Qty");
        tr.addView(tvNo);
        tr.addView(tvID);
        tr.addView(tvName);
        tr.addView(tvPrice);
        tr.addView(tvQty);
        tbLayout.addView(tr);
    }

    public void onClickSend(View view) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                printWriter.println(dataWillBeTransferred.toString());
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        try {
            if (requestCode == 0) {
                //---handle scan result---
                if (resultCode == RESULT_OK) {
                    String contents = intent.getStringExtra("SCAN_RESULT");
                    String[] split = contents.split("#");
                    if(radP.isChecked()){
                        editID.setText(split[0].toString());
                        editName.setText(split[1].toString());
                        editPrice.setText(split[2].toString());
                    }else if(radM.isChecked()){
                        editIDm.setText(split[0].toString());
                        editMn.setText(split[1].toString());
                    }else if(radW.isChecked()){
                        editIDw.setText(split[0].toString());
                        editWn.setText(split[1].toString());
                    }
                }

            }
        } catch (Exception e) {
            Toast.makeText(getBaseContext(),
                    e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickCon1(View v){
        try {
            String ip1 = editIP1.getText().toString();
            Thread clientThread = new Thread(new ClientThread(ip1));
            clientThread.start();
        } catch (Exception e) {
            Toast.makeText(getBaseContext(),
                    e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    public void onClickCon2(View v){

        try {
            String ip2 = editIP2.getText().toString();
            Thread clientThread = new Thread(new ClientThread(ip2));
            clientThread.start();
        } catch (Exception e) {
            Toast.makeText(getBaseContext(),
                    e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }


    }

    public class ClientThread implements Runnable {

        private String ipAddress;

        public ClientThread(String ipAddress) {
            this.ipAddress = ipAddress;
        }

        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName(ipAddress);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        textView1.setText(textView1.getText()
                                + "Connecting to the server");
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
                                    textView1.setText(textView1.getText()
                                            + "\n" + strReceived);
                                }
                            });
                        }

                        //---disconnected from the server---
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                textView1.setText(textView1.getText()
                                        + "\n" + "Client disconnected");
                            }
                        });

                    } catch (Exception e) {
                        final String error = e.getLocalizedMessage();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                textView1.setText(textView1.getText() + "\n" + error);
                            }
                        });
                    }

                } catch (Exception e) {
                    final String error = e.getLocalizedMessage();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            textView1.setText(textView1.getText() + "\n" + error);
                        }
                    });
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        textView1.setText(textView1.getText()
                                + "\n" + "Connection closed.");
                    }
                });

            } catch (Exception e) {
                final String error = e.getLocalizedMessage();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        textView1.setText(textView1.getText() + "\n" + error);
                    }
                });
            }
        }
    }

//	@Override
//	protected void onStart() {
//		super.onStart();
//		Thread clientThread = new Thread(new ClientThread());
//		clientThread.start();
//	}

    @Override
    protected void onStop() {
        super.onStop();
        try {
            if(socket != null){
                socket.shutdownInput();
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

