package com.draabek.sample;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    TextView output;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        output = findViewById(R.id.output);
        Button generate = findViewById(R.id.generate);
        generate.setOnClickListener(view -> {
            if (!checkSignerMissing()) return;
            Intent signerIntent = new Intent("com.draabek.androsigner.CONFIRM_REQUEST_ACTION");
            signerIntent.setClassName("com.draabek.androsigner", "com.draabek.androsigner.UserActionActivity");
            signerIntent.putExtra("INTENT_PASSWORD", "1234");
            signerIntent.putExtra("INTENT_COMMAND", "COMMAND_GENERATE_ADDRESS");
            signerIntent.setType("text/plain");
            startActivityForResult(signerIntent, 0);
        });

        Button transact = findViewById(R.id.transact);
        transact.setOnClickListener(view -> {
            if (!checkSignerMissing()) return;
            Intent signerIntent = getPackageManager().getLaunchIntentForPackage("com.draabek.androsigner");
            signerIntent.putExtra("command", "COMMAND_SEND_TRANSACTION");
            //TODO put parameters
            //TODO obtain gas
            startActivityForResult(signerIntent, 1);
        });

        Button sign = findViewById(R.id.sign);
        sign.setOnClickListener(view -> {
            if (!checkSignerMissing()) return;
            Intent signerIntent = getPackageManager().getLaunchIntentForPackage("com.draabek.androsigner");
            signerIntent.putExtra("command", "COMMAND_SIGN_MESSAGE");
            //TODO put parameters
            startActivityForResult(signerIntent, 2);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String address = data.getStringExtra("RETURN_GENERATED_ADDRESS");
                String mnemonic = data.getStringExtra("RETURN_ADDRESS_MNEMONIC");
                output.setText("Got address " + address + " with mnemonic " + mnemonic);
            }
        }
    }

    private boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private boolean checkSignerMissing() {
        if (!(isPackageInstalled("com.draabek.androsigner", getPackageManager()))) {
            //todo prompt to install
            output.setText("Signer not found.");
            return false;
        }
        return true;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
