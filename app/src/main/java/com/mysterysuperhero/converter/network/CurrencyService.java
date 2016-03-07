package com.mysterysuperhero.converter.network;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.mysterysuperhero.converter.MainActivity;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class CurrencyService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_DOWNLOAD_CURRENCY = "download";
    public static final String ACTION_CONVERT = "convert";

    // TODO: Rename parameters
    public static final String FROM_AMOUNT_PARAM = "from_amount";
    public static final String FROM_PARAM = "from";
    public static final String TO_PARAM = "to";

    public static CurrencyService self = null;

    public ArrayList<APIService.Currency> currencies = null;

    public String to_amount;

    private APIService service;

    public static CurrencyService getCurrencyService() {
        return self;
    }

    @SuppressWarnings("SpellCheckingInspection")
    public CurrencyService() {
        super("CurrencyService");
        self = this;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://currencyconverter.p.mashape.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(APIService.class);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_DOWNLOAD_CURRENCY.equals(action)) {
                handleActionDownload();
            } else if (ACTION_CONVERT.equals(action)) {
                final double from_amount = intent.getDoubleExtra(FROM_AMOUNT_PARAM, 0.0);
                final String from = intent.getStringExtra(FROM_PARAM);
                final String to = intent.getStringExtra(TO_PARAM);
                handleActionConvert(from_amount, from, to);
            }
        }
    }

    private void handleActionDownload() {
        Call<ArrayList<APIService.Currency>> call = service.getAvailableCurrency();
        try {
            Response<ArrayList<APIService.Currency>> response = call.execute();
            this.currencies = response.body();
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(MainActivity.Receiver.ACTION_DOWNLOADED);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            sendBroadcast(broadcastIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void handleActionConvert(double from_amount, String from, String to) {
        try {
            Call<APIService.Conversion> call = service.convert(from, String.valueOf(from_amount), to);
            Response<APIService.Conversion> res = call.execute();
            this.to_amount = String.valueOf(res.body().to_amount);
            Intent broadcastIntent = new Intent();
            broadcastIntent.putExtra("to_amount", Double.parseDouble(to_amount));
            broadcastIntent.setAction(MainActivity.Receiver.ACTION_CONVERTED);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            sendBroadcast(broadcastIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
