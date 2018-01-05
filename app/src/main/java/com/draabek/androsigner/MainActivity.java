package com.draabek.androsigner;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.web3j.abi.datatypes.Address;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    Web3j web3 = null;
    List<PastAction> pastActionList;
    ListView pastActionsView;
    TextView statusBar;
    Handler statusBarHandler;
    //FIXME temporary because of mysterious ClassNotFoundException
    static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        web3 = Web3jFactory.build(new HttpService( "https://ropsten.infura.io/tmbhNp6pHaBMdPKYsP7A"));
        setContentView(R.layout.activity_main);
        pastActionsView = findViewById(R.id.past_actions_view);
        PopulateActionsListTask populateActionsListTask = new PopulateActionsListTask();
        populateActionsListTask.execute(10);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        statusBar = findViewById(R.id.content);

        statusBarHandler = new Handler(Looper.getMainLooper(), message -> {
            String text = (String)message.obj;
            statusBar.setText(text);
            return true;
        });

        new Thread(this::showClientVersion).start();
    }

    private void initActionsList(List<? extends PastAction> pastActions) {
        List<Map<String, String>> data = new ArrayList<>();
        for (PastAction pastAction : pastActions) {
            Map<String, String> row = new HashMap<>();
            row.put("name", pastAction.getShortDescription());
            row.put("action", "");
            data.add(row);
        }
        SimpleAdapter adapter = new SimpleAdapter(
                this, // Context.
                data,
                R.layout.list_view_row,
                new String[]{"name", "action"},
                new int[]{R.id.list_view_action, R.id.list_view_detail}
        );
        adapter.setViewBinder((view, data1, textRepresentation) -> {
            if (view.getId() == R.id.list_view_action) {
                ((TextView)view).setText((String) data1);
            }
            return true;
        });
        // Bind to our new adapter.pter(adapter);

        pastActionsView.post(() -> pastActionsView.setAdapter(adapter));
    }

    private void showClientVersion() {
        Web3ClientVersion web3ClientVersion = null;
        try {
            web3ClientVersion = web3.web3ClientVersion().sendAsync().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        String clientVersion = (web3ClientVersion == null) ?
                "Could not get client version. Are you connected?" : web3ClientVersion.getWeb3ClientVersion();
        Message completeMessage =
                statusBarHandler.obtainMessage(0, clientVersion);
        completeMessage.sendToTarget();
    }

    private class PopulateActionsListTask extends AsyncTask<Integer, Integer, Integer> {
        // Do the long-running work in here
        protected Integer doInBackground(Integer... pastBlocks) {
            List<TransactionAction> transactionActionList = getPastTransactions(pastBlocks[0]);
            initActionsList(transactionActionList);
            return transactionActionList.size();
        }

        // This is called each time you call publishProgress()
        protected void onProgressUpdate(Integer... progress) {
            //FIXME
        }

        // This is called when doInBackground() is finished
        protected void onPostExecute(Long result) {
           //FIXME
        }
    }

    private List<PastAction> getPastActions() {
        ArrayList<PastAction> actions = new ArrayList<>();
        actions.add(new GeneratedAddress(new Date(), "Debug", new Address("0x1234")) );
        actions.addAll(getPastTransactions(10));
        return actions;
    }

    private List<TransactionAction> getPastTransactions(long pastBlocks) {
        List<TransactionAction> transactionActionList = new ArrayList<TransactionAction>((int)pastBlocks*100);
        long lastBlockNumber = 0;
        try {
            lastBlockNumber = web3.ethBlockNumber().send().getBlockNumber().longValue();
        } catch (IOException e) {
            e.printStackTrace();
        }
        EthBlock.Block block = null;
        for (long l = lastBlockNumber - pastBlocks;l < lastBlockNumber;l++) {
            try {
                block = web3.ethGetBlockByNumber(new DefaultBlockParameterNumber(l),
                        true).send().getBlock();
                for (EthBlock.TransactionResult<EthBlock.TransactionObject> t : block.getTransactions()) {
                    transactionActionList.add(new TransactionAction(
                            "",
                            new Date(block.getTimestamp().longValue()),
                            t.get()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return transactionActionList;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
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

    public static Context getContext() {
        return context;
    }
}
