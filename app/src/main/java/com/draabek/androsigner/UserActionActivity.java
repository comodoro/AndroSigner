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
import com.draabek.androsigner.com.draabek.androsigner.pastaction.PastAction;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.core.methods.request.Transaction;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Date;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class UserActionActivity extends AppCompatActivity {

    static final String LOG_KEY = UserActionActivity.class.getName();
    private PastAction currentAction;
    private Button yesButton;
    private Button noButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_action);

        yesButton = findViewById(R.id.user_action_yes);
        noButton = findViewById(R.id.user_action_no);

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
                        String value = intent.getStringExtra("value");
                        String data = intent.getStringExtra("data");
                        handleBadInput(appName);
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

    private String encodeMethod(String method, String from, String to, BigInteger gasPrice,
                                BigInteger gasLimit, List<Type> inputParameters,
                                List<TypeReference<?>> outputParameters) {
        Function function = new Function(
                "functionName",  // function we're calling
                inputParameters,  // Parameters to pass as Solidity Types
                outputParameters);

        String encodedFunction = FunctionEncoder.encode(function);
//        RawTransaction rawTransaction = RawTransaction.createTransaction()createFunctionCallTransaction(
//                from,
//                nonce,
//                BigInteger gasPrice,
//                BigInteger gasLimit,
//                to,
//                encodedFunction);
        throw new UnsupportedOperationException();
        //new RawTransactionManager(web3j, credentials, TransactionReceiptProcessor)
    }
    private Transaction generateTransaction(String from, String to, String value, String data,
                                            BigInteger gasPrice, BigInteger gasLimit) {
        throw new UnsupportedOperationException();
    }

    private void signAndSend(Transaction transaction) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    private String generateAddress(String pwd) {
        String path = "";
        String fileName = null;
        try {
            fileName = WalletUtils.generateNewWalletFile(
                    "your password",
                    new File(path), true);
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

    private void confirmOrRejectAction(PastAction pastAction, ConfirmAction confirmAction) {
        yesButton.setOnClickListener(view -> confirmAction.confirm(pastAction));
        noButton.setOnClickListener(view -> confirmAction.reject(pastAction));
    }

    private void handleAddressGeneration(String app, String pwd) {
        String generatedAddressString = generateAddress(pwd);
        GeneratedAddress generatedAddress = new GeneratedAddress(new Date(), app, generatedAddressString);
        confirmOrRejectAction(generatedAddress, new ConfirmAction() {
            @Override
            public void confirm(PastAction pastAction) {
                Intent result = new Intent("com.draabek.androsigner.RESULT_ACTION");
                GlobalActionsList.instance().append(generatedAddress);
                result.putExtra("generated_address", generatedAddressString);
                setResult(Activity.RESULT_OK, result);
                finish();
            }

            @Override
            public void reject(PastAction pastAction) {
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