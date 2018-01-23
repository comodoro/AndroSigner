package com.draabek.androsigner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;

import com.draabek.androsigner.com.draabek.androsigner.pastaction.GeneratedAddress;
import com.draabek.androsigner.com.draabek.androsigner.pastaction.GlobalActionsList;
import com.draabek.androsigner.com.draabek.androsigner.pastaction.TransactionAction;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class UserActionActivity extends AppCompatActivity {

    static final String LOG_KEY = UserActionActivity.class.getName();
    private Web3j web3j;
    private Button yesButton;
    private Button noButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_action);

        yesButton = findViewById(R.id.user_action_yes);
        noButton = findViewById(R.id.user_action_no);

        web3j = Web3jFactory.build(
                new HttpService( "https://ropsten.infura.io/tmbhNp6pHaBMdPKYsP7A")
        );


        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                String extra = intent.getStringExtra("command");
                String appName = intent.getPackage();
                switch (extra) {
                    case "generate": {
                        String pwd = intent.getStringExtra("password");
                        handleAddressGeneration(appName, pwd);
                        break;
                    }
                    case "transact": {
                        String pwd = intent.getStringExtra("password");
                        String from = intent.getStringExtra("from");
                        String to = intent.getStringExtra("to");
                        BigInteger value = new BigInteger(intent.getStringExtra("value"));
                        //TODO pass unencoded function
                        String data = intent.getStringExtra("data");
                        BigInteger gasPrice = BigInteger.valueOf(intent.getLongExtra("gasPrice", 0));
                        BigInteger gasLimit = BigInteger.valueOf(intent.getLongExtra("gasPrice", 0));
                        handleTransaction(appName, from, pwd, to, value, data, gasPrice, gasLimit);
                        break;
                    }
                    case "sign":
                        handleBadInput(appName);
                        break;
                    default:
                        handleBadInput(appName);
                        break;
                }
            }
        }
    }

    private String encodeMethod(String method,
                                List<Type> inputParameters,
                                List<TypeReference<?>> outputParameters) {
        Function function = new Function(
                method,  // function we're calling
                inputParameters,  // Parameters to pass as Solidity Types
                outputParameters);

        return FunctionEncoder.encode(function);

    }

    private void handleTransaction(String appName, String from, String pwd, String to, BigInteger value, String data,
                                   BigInteger gasPrice, BigInteger gasLimit) {

        EthGetTransactionCount ethGetTransactionCount = null;
        try {
            ethGetTransactionCount = web3j.ethGetTransactionCount(
                    from, DefaultBlockParameterName.LATEST).sendAsync().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        BigInteger nonce;
        if (ethGetTransactionCount == null) {
            nonce = new BigInteger("0");
        } else {
            nonce = ethGetTransactionCount.getTransactionCount();
        }
        org.web3j.protocol.core.methods.request.Transaction transaction =
                org.web3j.protocol.core.methods.request.Transaction.createFunctionCallTransaction(
                        from, nonce, gasPrice, gasLimit, to, value, data
                );
        TransactionAction transactionAction = new TransactionAction(appName, new Date(), transaction);
        confirmOrRejectAction(new ConfirmAction() {
            @Override
            public void confirm(){
                String txHash;
                try {
                    txHash = signAndSend(from, pwd, to, value, data, gasPrice, gasLimit);
                    Intent result = new Intent("com.draabek.androsigner.RESULT_ACTION");
                    GlobalActionsList.instance().append(transactionAction);
                    result.putExtra("txhash", txHash);
                    setResult(Activity.RESULT_OK, result);
                } catch (IOException e) {
                    e.printStackTrace();
                    Intent result = new Intent("com.draabek.androsigner.RESULT_ACTION");
                    setResult(Activity.RESULT_CANCELED, result);
                    result.putExtra("reason", e.toString());
                 }
                finish();
            }

            @Override
            public void reject() {
                Intent result = new Intent("com.draabek.androsigner.RESULT_ACTION");
                setResult(Activity.RESULT_CANCELED, result);
                result.putExtra("reason", "cancelled");
                finish();
            }
        });
    }

    private String signAndSend(String from, String pwd, String to, BigInteger value, String data,
                                            BigInteger gasPrice, BigInteger gasLimit) throws IOException {
        EthSendTransaction ethSendTransaction = new RawTransactionManager(web3j, GlobalAccountManager.instance()
                .getCredentials(from, pwd))
                .sendTransaction(
                gasPrice,
                gasLimit,
                to,
                data,
                value
               );
        return ethSendTransaction.getTransactionHash();
    }

    @Nullable
    private String generateAddress(String pwd) {
        String fileName = null;
        try {
            //FIXME Use SCrypt when no OOMError happens on android
            fileName = WalletUtils.generateNewWalletFile(
                    pwd, GlobalAccountManager.instance().getRootDir(), false);
        } catch (CipherException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        if (fileName == null) return null;
        fileName = GlobalAccountManager.instance().getRootDir().getAbsolutePath() + "/" + fileName;
        Credentials credentials = null;
        try {
            credentials = WalletUtils.loadCredentials(
                    pwd,
                    fileName);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CipherException e) {
            e.printStackTrace();
        }
        if (credentials == null) return null;
        return credentials.getAddress();
    }

    private void confirmOrRejectAction(ConfirmAction confirmAction) {
        yesButton.setOnClickListener(view -> confirmAction.confirm());
        noButton.setOnClickListener(view -> confirmAction.reject());
    }

    private void handleAddressGeneration(String app, String pwd) {
        String generatedAddressString = generateAddress(pwd);
        GeneratedAddress generatedAddress = new GeneratedAddress(new Date(), app, generatedAddressString);
        confirmOrRejectAction(new ConfirmAction() {
            @Override
            public void confirm() {
                Intent result = new Intent("com.draabek.androsigner.RESULT_ACTION");
                GlobalActionsList.instance().append(generatedAddress);
                // GlobalAccountManager only needs to reload
                GlobalAccountManager.instance().reloadAll();
                result.putExtra("generated_address", generatedAddressString);
                setResult(Activity.RESULT_OK, result);
                finish();
            }

            @Override
            public void reject() {
                Intent result = new Intent("com.draabek.androsigner.RESULT_ACTION");
                setResult(Activity.RESULT_CANCELED, result);
                finish();
            }
        });
    }

    private void handleBadInput(String app) {
        Log.w(LOG_KEY, "Bad input from " + app);
        Intent result = new Intent("com.draabek.androsigner.RESULT_ACTION");
        setResult(Activity.RESULT_CANCELED, result);
        finish();
    }
}