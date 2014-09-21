package com.example.klienOrder;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;


public class MyActivity extends Activity {
    TableLayout tbLayout;
    NotificationManager NM;
    EditText editID, editName,
            editPrice, editQty,
            editIP1,
            editIDw, editWn, editTableNo,
            editIDm, editMn;
    TextView textView1;
    RadioButton radW, radM, radP;
    int count;


    StringBuffer dataWillBeTransferred;


    boolean mBounded;
    ClientService mServer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        editID = (EditText) findViewById(R.id.etIDProd);
        editName = (EditText) findViewById(R.id.etProdName);
        editPrice = (EditText) findViewById(R.id.etPrice);
        editQty = (EditText) findViewById(R.id.etQty);
        editIP1 = (EditText) findViewById(R.id.editIP1);
//        editIP2 = (EditText) findViewById(R.id.editIP2);

        editIDm = (EditText) findViewById(R.id.editMID);
        editIDw = (EditText) findViewById(R.id.editWID);

        editWn = (EditText) findViewById(R.id.editWName);
        editMn = (EditText) findViewById(R.id.editMName);

        editTableNo = (EditText) findViewById(R.id.editTable);

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

        //data yang akan dikirim ke server
        dataWillBeTransferred.append("/" + editTableNo.getText().toString());//no table
        dataWillBeTransferred.append("#" + editIDw.getText().toString());//id waiter
        dataWillBeTransferred.append("#" + editIDm.getText().toString());//id member
        dataWillBeTransferred.append("#" + tvID.getText().toString());// id produk
        dataWillBeTransferred.append("#" + tvName.getText().toString());// nama produk
        dataWillBeTransferred.append("#" + tvPrice.getText().toString());// harga
        dataWillBeTransferred.append("#" + tvQty.getText().toString());// qty

        count++;

    }

    public void onClickRem(View v) {
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
        dataWillBeTransferred.setLength(0);
    }

    public void onClickSend(View view) {
        try {
            mServer.onClickSend(dataWillBeTransferred.toString());
        } catch (Exception e) {
            Toast.makeText(getBaseContext(),
                    e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        try {
            if (requestCode == 0) {
                //---handle scan result---
                if (resultCode == RESULT_OK) {
                    String contents = intent.getStringExtra("SCAN_RESULT");
                    String[] split = contents.split("#");
                    if (radP.isChecked()) {
                        editID.setText(split[0].toString());
                        editName.setText(split[1].toString());
                        editPrice.setText(split[2].toString());
                    } else if (radM.isChecked()) {
                        editIDm.setText(split[0].toString());
                        editMn.setText(split[1].toString());
                    } else if (radW.isChecked()) {
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

    public void onClickCon(View v) {
        try {
            String ip = editIP1.getText().toString();

            Intent service = new Intent(this, ClientService.class);
            service.putExtra("IP_ADDRESS", ip);
            startService(service);

            bindService(service, mConnection, BIND_AUTO_CREATE);
        } catch (Exception e) {
            Toast.makeText(getBaseContext(),
                    e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(getBaseContext(), "Service is disconnected", Toast.LENGTH_LONG).show();
            mBounded = false;
            mServer = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            Toast.makeText(getBaseContext(), "Service is connected", Toast.LENGTH_LONG).show();
            mBounded = true;
            ClientService.LocalBinder mLocalBinder = (ClientService.LocalBinder) service;
            mServer = mLocalBinder.getClientServiceInstance();
        }
    };


}

