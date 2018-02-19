package com.draabek.androsigner;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.draabek.androsigner.com.draabek.androsigner.pastaction.GlobalActionsList;
import com.draabek.androsigner.com.draabek.androsigner.pastaction.PastAction;
import com.draabek.androsigner.com.draabek.androsigner.pastaction.TransactionAction;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;

import java.io.File;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        pastActionsView = findViewById(R.id.past_actions_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        createGlobalLists();
        PopulateActionsListTask populateActionsListTask = new PopulateActionsListTask();
        populateActionsListTask.execute(10);

        statusBar = findViewById(R.id.activity_main_status_bar);
        new Thread(this::showClientVersion).start();
    }

    private void createGlobalLists() {
        File rootDir = this.getApplicationContext().getFilesDir();
        File actionsDir = new File(rootDir.getAbsolutePath() + "/actions");
        if (!(actionsDir.exists())) {
            if (!actionsDir.mkdir()) throw new RuntimeException("Actions directory could not be created");
        }
        GlobalActionsList.create(actionsDir);
        GlobalActionsList.instance().reloadAll();
        File accountsDir = new File(rootDir.getAbsolutePath() + "/accounts");
        if (!(accountsDir.exists())) {
            if (!accountsDir.mkdir()) throw new RuntimeException("Accounts directory could not be created");
        }
        GlobalAccountManager.create(accountsDir);
        GlobalAccountManager.instance().reloadAll();
    }

    private void ensureInitWeb3() {
        if (web3 == null) {
            web3 = Web3jFactory.build(new HttpService( SignerApplication.getConfig().getEndpoint()));
        }
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

        pastActionsView.post(() -> pastActionsView.setAdapter(adapter));
    }

    private void showClientVersion() {
        ensureInitWeb3();
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
        statusBar.post(() -> statusBar.setText(clientVersion));
    }

    @SuppressLint("StaticFieldLeak")
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

    private List<TransactionAction> getPastTransactions(long pastBlocks) {
        ensureInitWeb3();
        List<TransactionAction> transactionActionList = new ArrayList<>((int) pastBlocks * 100);
        long lastBlockNumber = 0;
        try {
            lastBlockNumber = web3.ethBlockNumber().send().getBlockNumber().longValue();
        } catch (IOException e) {
            e.printStackTrace();
        }
        EthBlock.Block block;
        for (long l = lastBlockNumber - pastBlocks;l < lastBlockNumber;l++) {
            for (int tries = 0;tries < 3;tries++)
                try {
                    block = web3.ethGetBlockByNumber(new DefaultBlockParameterNumber(l),
                            true).send().getBlock();
                    if (block == null) continue;
                    for (EthBlock.TransactionResult t : block.getTransactions()) {
                        EthBlock.TransactionObject o = (EthBlock.TransactionObject) t.get();
                        transactionActionList.add(new TransactionAction(
                                "",
                                new Date(block.getTimestamp().longValue()),
                                new Transaction(
                                        o.getFrom(),
                                        o.getNonce(),
                                        o.getGasPrice(),
                                        o.getGas(),
                                        o.getTo(),
                                        o.getValue(),
                                        o.getInput()
                                )));
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
}
