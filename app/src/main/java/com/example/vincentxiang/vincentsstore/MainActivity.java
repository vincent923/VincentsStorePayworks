package com.example.vincentxiang.vincentsstore;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import io.mpos.transactions.*;
import io.mpos.ui.shared.*;
import io.mpos.provider.*;
import java.util.*;
import java.math.*;
import io.mpos.ui.shared.model.*;
import io.mpos.accessories.parameters.AccessoryParameters;
import io.mpos.accessories.*;
import io.mpos.transactions.parameters.*;
import android.content.*;
import android.view.*;
import android.widget.*;
import io.mpos.transactionprovider.*;
import android.text.*;

public class MainActivity extends AppCompatActivity {

    public EditText mEditAmount;
    public BigDecimal amount;

    public Transaction lastTransaction;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditAmount = findViewById(R.id.editTextAmount);

    }

    public void paymentButtonClicked(View v) {

        //Button button=(Button) v;
        //((Button) v).setText("clicked");
        MposUi ui = MposUi.initialize(this, ProviderMode.MOCK,
                "merchantIdentifier", "merchantSecretKey");

        ui.getConfiguration().setSummaryFeatures(EnumSet.of(
                // Add this line, if you do want to offer printed receipts
                // MposUiConfiguration.SummaryFeature.PRINT_RECEIPT,
                MposUiConfiguration.SummaryFeature.SEND_RECEIPT_VIA_EMAIL)
        );

        // Start with a mocked card reader:
        AccessoryParameters accessoryParameters = new AccessoryParameters.Builder(AccessoryFamily.MOCK)
                .mocked()
                .build();
        ui.getConfiguration().setTerminalParameters(accessoryParameters);

            // Add this line if you would like to collect the customer signature on the receipt (as opposed to the digital signature)
        ui.getConfiguration().setSignatureCapture(MposUiConfiguration.SignatureCapture.ON_RECEIPT);


            /* When using the Bluetooth Miura, use the following parameters:
            AccessoryParameters accessoryParameters = new AccessoryParameters.Builder(AccessoryFamily.MIURA_MPI)
                                                                             .bluetooth()
                                                                             .build();
            ui.getConfiguration().setTerminalParameters(accessoryParameters);
            */

            /* When using Verifone readers via WiFi or Ethernet, use the following parameters:
            AccessoryParameters accessoryParameters = new AccessoryParameters.Builder(AccessoryFamily.VERIFONE_VIPA)
                                                                             .tcp("192.168.254.123", 16107)
                                                                             .build();
            ui.getConfiguration().setTerminalParameters(accessoryParameters);
            */

        if(!TextUtils.isEmpty(mEditAmount.getText().toString().trim()))
        {


            amount = new BigDecimal(mEditAmount.getText().toString());
            if(amount.compareTo(BigDecimal.ZERO)<=0)
            {
                ((EditText) findViewById(R.id.editTextWarning)).setText("The amount should be more than 0");
                return;
            }

            TransactionParameters transactionParameters = new TransactionParameters.Builder()
                    .charge(amount, io.mpos.transactions.Currency.USD)
                    .subject("Bouquet of Flowers")
                    .customIdentifier("yourReferenceForTheTransaction")
                    .build();

            Intent intent = ui.createTransactionIntent(transactionParameters);
            startActivityForResult(intent, MposUi.REQUEST_CODE_PAYMENT);

            ((EditText) findViewById(R.id.editTextWarning)).setText("");

        }else
        {
            ((EditText) findViewById(R.id.editTextWarning)).setText("Please input an amount");
            return;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == MposUi.REQUEST_CODE_PAYMENT) {
            if (resultCode == MposUi.RESULT_CODE_APPROVED) {
                // Transaction was approved
                Toast.makeText(this, "Transaction approved", Toast.LENGTH_LONG).show();
            } else {
                // Card was declined, or transaction was aborted, or failed
                // (e.g. no internet or accessory not found)
                Toast.makeText(this, "Transaction was declined, aborted, or failed",
                        Toast.LENGTH_LONG).show();
            }
            // Grab the processed transaction in case you need it
            // (e.g. the transaction identifier for a refund).
            // Keep in mind that the returned transaction might be null
            // (e.g. if it could not be registered).
            Transaction transaction = MposUi.getInitializedInstance().getTransaction();
        }


    }

    public void refundButtonClicked(View v)
    {

        //Get the last transaction;
        lastTransaction = MposUi.getInitializedInstance().getTransaction();

        //((Button) v).setText(lastTransaction.getIdentifier().toString());
        MposUi ui = MposUi.initialize(this, ProviderMode.MOCK,
                "merchantIdentifier", "merchantSecretKey");

        if(lastTransaction==null)
        {
            ((EditText) findViewById(R.id.editTextWarning)).setText("Please perform a payment first");
            return;
        }
        else
        {
            //Perform a full refund
            TransactionParameters parameters = new TransactionParameters.Builder()
                    .refund(lastTransaction.getIdentifier()).amountAndCurrency(lastTransaction.getAmount(),

                            io.mpos.transactions.Currency.USD)
                    // For partial refunds, specify the amount to be refunded
                    // and the currency from the original transaction
                    //.amountAndCurrency(new BigDecimal("1.00"), io.mpos.transactions.Currency.EUR)
                    .build();

            ui.getConfiguration().setSummaryFeatures(EnumSet.of(
                    // Add this line, if you do want to offer printed receipts
                    // MposUiConfiguration.SummaryFeature.PRINT_RECEIPT,
                    MposUiConfiguration.SummaryFeature.SEND_RECEIPT_VIA_EMAIL)
            );

            Intent intent = ui.createTransactionIntent(parameters);
            startActivityForResult(intent, MposUi.REQUEST_CODE_PAYMENT);

            ((EditText) findViewById(R.id.editTextWarning)).setText("");
        }

    }
}
