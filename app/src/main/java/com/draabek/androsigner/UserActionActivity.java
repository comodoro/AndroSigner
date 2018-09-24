package com.draabek.androsigner;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.draabek.androsigner.pastaction.AccountListAction;
import com.draabek.androsigner.pastaction.GeneratedAddress;
import com.draabek.androsigner.pastaction.GlobalActionsList;
import com.draabek.androsigner.pastaction.PastAction;
import com.draabek.androsigner.pastaction.SignMessageAction;
import com.draabek.androsigner.pastaction.TransactionAction;
import com.example.include.Constants;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Bip39Wallet;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.Sign;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.http.HttpService;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import go.error;
import jnr.posix.WString;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class UserActionActivity extends AppCompatActivity {

    static final String LOG_KEY = UserActionActivity.class.getName();
    private Web3j web3j;
    private Button yesButton;
    private Button noButton;
    private TextView askView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_action);

        yesButton = findViewById(R.id.user_action_yes);
        noButton = findViewById(R.id.user_action_no);
        askView = findViewById(R.id.user_action_text);
        web3j = Web3jFactory.build(
                new HttpService( SignerApplication.getConfig().getEndpoint())
        );

        createGlobalLists();

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Constants.CONFIRM_REQUEST_ACTION.equals(action)) {
            if ("text/plain".equals(type)) {
                String extra = intent.getStringExtra(Constants.INTENT_COMMAND);
                ComponentName activityComponent = getCallingActivity();
                String activity = (activityComponent != null)? activityComponent.getClassName() : null;
                Log.v(LOG_KEY, String.format("Received command %s from activity %s", extra, activity));
                switch (extra) {
                    case Constants.COMMAND_GENERATE_ADDRESS: {
                        String pwd = intent.getStringExtra(Constants.INTENT_PASSWORD);
                        handleAddressGeneration(activity, pwd);
                        break;
                    }
                    case Constants.COMMAND_CONFIRM_TRANSACTION: {
                        boolean waitForResult = intent.hasExtra(Constants.INTENT_WAIT_FOR_RESULT);
//TODO
//                        if (waitForResult) {
//                            intent.setClass(this, TransactionService.class);
//                            intent.putExtra(Constants.INTENT_CALLING_ACTIVITY, activity);
//                            startService(intent);
//                        } else {
                            handleTransaction(activity, intent);
//                        }
                    }
                    break;
                    case Constants.COMMAND_SIGN_MESSAGE:
                        String message = intent.getStringExtra(Constants.INTENT_MESSAGE);
                        String account = intent.getStringExtra(Constants.INTENT_ACCOUNT);
                        String password = intent.getStringExtra(Constants.INTENT_PASSWORD);
                        handleSignMessage(activity, message, account, password);
                        break;

                    case Constants.COMMAND_LIST_ADDRESSES:
                        handleListAccounts(activity);
                        break;
                
                    case Constants.COMMAND_GET_BALANCE:
                        String accountToView = intent.getStringExtra(Constants.INTENT_ACCOUNT);
                        handleGetBalance(activity, accountToView);
                        break;
                        
                    default:
                        handleError(activity, Constants.INTENT_FAILURE_REASON_UNKNOWN_ERROR);
                        break;
                }
            }
        }
    }

    /**
     *
     * @param activity
     * @param account
     */
    private void handleGetBalance(String activity, String account) {
        confirmOrRejectAction(new ConfirmAction() {
            @Override
            public void confirm() {
                Intent result = new Intent(Constants.INTENT_RESULT_DESCRIPTION);
                BigInteger balanceInWei = getBalanceWei(account);
                result.putExtra(Constants.INTENT_ACCOUNT_BALANCE, balanceInWei.toString());
                setResult(Activity.RESULT_OK, result);
                finish();
            }

            @Override
            public void reject() {
                Intent result = new Intent(Constants.INTENT_RESULT_DESCRIPTION);
                setResult(Activity.RESULT_CANCELED, result);
                result.putExtra(Constants.INTENT_FAILURE_REASON, Constants.INTENT_FAILURE_REASON_CANCELLED);
                finish();
            }
        });
    }

    private void handleSignMessage(String activity, String message, String account, String password) {
        confirmOrRejectAction(new ConfirmAction() {
            @Override
            public void confirm() {
                ECKeyPair ecKeyPair = GlobalAccountManager.instance().getCredentials(account, password).getEcKeyPair();
                Sign.SignatureData signatureData = Sign.signMessage(message.getBytes(Charset.forName("utf16")), ecKeyPair, false);
                Intent result = new Intent(Constants.INTENT_RESULT_DESCRIPTION);
                result.putExtra(Constants.INTENT_MESSAGE_SIGNATURE, signatureData.hashCode());
                setResult(Activity.RESULT_OK, result);
                GlobalActionsList.instance().append(new SignMessageAction(activity, new Date(),
                        PastAction.State.CONFIRMED, message, account));
                finish();
            }

            @Override
            public void reject() {
                Intent result = new Intent(Constants.INTENT_RESULT_DESCRIPTION);
                setResult(Activity.RESULT_CANCELED, result);
                result.putExtra(Constants.INTENT_FAILURE_REASON, Constants.INTENT_FAILURE_REASON_CANCELLED);
                finish();
            }
        });
    }

    private void handleListAccounts(String activity) {
        confirmOrRejectAction(new ConfirmAction() {
            @Override
            public void confirm() {
                Intent result = new Intent(Constants.INTENT_RESULT_DESCRIPTION);
                AccountListAction accountListAction = new AccountListAction(activity, new Date(), PastAction.State.CONFIRMED);
                GlobalActionsList.instance().append(accountListAction);
                String[] accounts = GlobalAccountManager.instance().getAddresses().toArray(new String[0]);
                result.putExtra(Constants.RETURN_ACCOUNT_LIST, accounts);
                setResult(Activity.RESULT_OK, result);
                finish();
            }

            @Override
            public void reject() {
                Intent result = new Intent(Constants.INTENT_RESULT_DESCRIPTION);
                setResult(Activity.RESULT_CANCELED, result);
                result.putExtra(Constants.INTENT_FAILURE_REASON, Constants.INTENT_FAILURE_REASON_CANCELLED);
                finish();
            }
        });
    }

    //todo out of this class
    private BigInteger getBalanceWei(String address) {
        try {
            return web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send().getBalance();
        } catch (IOException e) {
            e.printStackTrace();
            return new BigInteger("");
        }
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

    private String encodeMethod(String method,
                                List<Type> inputParameters,
                                List<TypeReference<?>> outputParameters) {
        Function function = new Function(
                method,  // function we're calling
                inputParameters,  // Parameters to pass as Solidity Types
                outputParameters);

        return FunctionEncoder.encode(function);
    }

    private void handlePubkey() {
        Intent result = new Intent(Constants.INTENT_RESULT_DESCRIPTION);
        result.putExtra(Constants.RETURN_TRANSACTION_HASH, "mock hash");
        setResult(Activity.RESULT_OK, result);
    }

    private void handleTransaction(String activity, Intent intent) {
        String password = intent.getStringExtra(Constants.INTENT_PASSWORD);
        String from = intent.getStringExtra(Constants.INTENT_FROM);
        String to = intent.getStringExtra(Constants.INTENT_TO);
        BigInteger value = new BigInteger(intent.getStringExtra(Constants.INTENT_VALUE));
        //TODO pass unencoded function ABI (to be transparent)
        String data = intent.getStringExtra(Constants.INTENT_DATA);
        BigInteger gasPrice = new BigInteger(intent.getStringExtra(Constants.INTENT_GAS_PRICE));
        BigInteger gasLimit = new BigInteger(intent.getStringExtra(Constants.INTENT_GAS_LIMIT));
        askView.setText(String.format(getString(R.string.user_action_question), getString(R.string.user_action_ethereum_transaction)));
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
                        from, nonce, gasPrice, gasLimit, to,
                        value, data
                );
        confirmOrRejectAction(new ConfirmAction() {
            @Override
            public void confirm(){
                signAndSend(nonce, from, password, to, value, data, gasPrice, gasLimit);
                Intent result = new Intent(Constants.INTENT_RESULT_DESCRIPTION);
                TransactionAction transactionAction = new TransactionAction(activity, new Date(), PastAction.State.CONFIRMED, transaction);
                GlobalActionsList.instance().append(transactionAction);
                //todo txhash is bound to sending the transaction in web3j, get it another way
                //result.putExtra(Constants.RETURN_TRANSACTION_HASH, txHash);
                result.putExtra(Constants.RETURN_TRANSACTION_HASH, "mock hash");
                setResult(Activity.RESULT_OK, result);
                finish();
            }

            @Override
            public void reject() {
                TransactionAction transactionAction = new TransactionAction(activity, new Date(), PastAction.State.REJECTED, transaction);
                GlobalActionsList.instance().append(transactionAction);
                Intent result = new Intent(Constants.INTENT_RESULT_DESCRIPTION);
                setResult(Activity.RESULT_CANCELED, result);
                result.putExtra(Constants.INTENT_FAILURE_REASON, Constants.INTENT_FAILURE_REASON_CANCELLED);
                finish();
            }
        });
    }

    private void signAndSend(BigInteger nonce, String from, String pwd, String to, BigInteger value, String data,
                                            BigInteger gasPrice, BigInteger gasLimit) {
        Credentials credentials = GlobalAccountManager.instance()
                .getCredentials(from, pwd);

        RawTransaction rawTransaction = RawTransaction.createTransaction(
                nonce,
                gasPrice,
                gasLimit,
                to,
                value,
                data);

        byte [] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        //fixme
        new Thread(() -> {
                    try {
                        String transactionHash = web3j.ethSendRawTransaction(
                                Arrays.toString(signedMessage)).send().getTransactionHash();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        ).start();
    }

    @Nullable
    private String[] generateAddress(String pwd) {
        Bip39Wallet bip39Wallet = null;
        String fileName = null;
        try {
            bip39Wallet = WalletUtils.generateBip39Wallet(
                    pwd, GlobalAccountManager.instance().getRootDir());
        } catch (CipherException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (bip39Wallet == null) return null;
        fileName = bip39Wallet.getFilename();
        Credentials credentials = null;
        try {
            credentials = WalletUtils.loadCredentials(pwd,
                    GlobalAccountManager.instance().getRootDir().getAbsolutePath() + "/" + fileName);
        } catch (IOException e) {
            Log.e(this.getClass().getName(), fileName);
            e.printStackTrace();
        } catch (CipherException e) {
            e.printStackTrace();
        }
        if (credentials == null) return null;
        return new String[]{credentials.getAddress(), bip39Wallet.getMnemonic()};
    }

    private void confirmOrRejectAction(ConfirmAction confirmAction) {
        yesButton.setOnClickListener(view -> confirmAction.confirm());
        noButton.setOnClickListener(view -> confirmAction.reject());
    }

    private void handleAddressGeneration(String app, String pwd) {
        askView.setText(String.format(getString(R.string.user_action_question), getString(R.string.user_action_address_generation)));
        confirmOrRejectAction(new ConfirmAction() {
            @Override
            public void confirm() {
                Log.v(LOG_KEY, "Generate action confirmed");
                String[] addressInfo = generateAddress(pwd);
                GeneratedAddress generatedAddress = new GeneratedAddress(new Date(), app, PastAction.State.CONFIRMED, addressInfo[0]);
                GlobalActionsList.instance().append(generatedAddress);
                Intent result = new Intent(Constants.INTENT_RESULT_DESCRIPTION);
                // GlobalAccountManager only needs to reloaded
                GlobalAccountManager.instance().reloadAll();
                result.putExtra(Constants.RETURN_GENERATED_ADDRESS, addressInfo[0]);
                result.putExtra(Constants.RETURN_ADDRESS_MNEMONIC, addressInfo[1]);
                setResult(Activity.RESULT_OK, result);
                finish();
            }

            @Override
            public void reject() {
                Log.v(LOG_KEY, "Generate action rejected");
                GeneratedAddress generatedAddress = new GeneratedAddress(new Date(), app, PastAction.State.REJECTED, null);
                GlobalActionsList.instance().append(generatedAddress);
                Intent result = new Intent(Constants.INTENT_RESULT_DESCRIPTION);
                result.putExtra(Constants.INTENT_FAILURE_REASON, Constants.INTENT_FAILURE_REASON_CANCELLED);
                setResult(Activity.RESULT_CANCELED, result);
                finish();
            }
        });
    }

    private void handleError(String app, String error) {
        Log.w(LOG_KEY, String.format("Error processing request from %s: %s", app, error));
        Intent result = new Intent(Constants.INTENT_RESULT_DESCRIPTION);
        result.putExtra(Constants.INTENT_FAILURE_REASON, error);
        setResult(Activity.RESULT_CANCELED, result);
        finish();
    }}