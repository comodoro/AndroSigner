package com.draabek.androsigner;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import com.example.include.Constants;

import org.bouncycastle.crypto.tls.MACAlgorithm;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.util.Calendar;

import javax.security.auth.x500.X500Principal;

/**
 * An @see IntentService for handling long-running tasks
 */
public class TransactionService extends IntentService {

    public TransactionService() {
        super("TransactionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            //TODO handle
            assert action != null;
            if (action.equals(Constants.COMMAND_GET_PUBKEY)) {

            }
        }
    }

}
