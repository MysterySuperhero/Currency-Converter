package com.mysterysuperhero.converter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mysterysuperhero.converter.network.APIService;
import com.mysterysuperhero.converter.network.CurrencyService;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private ListView fromListView;
    private ListView toListView;
    private EditText fromView;
    private EditText toView;
    private String from = null;
    private String to = null;


    private Receiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fromValue = fromView.getText().toString();
                if (!fromValue.isEmpty()) {
                    progressBar.setVisibility(View.VISIBLE);
                    try {
                        double from_amount = Double.parseDouble(fromValue);
                        if (from != null && to != null) {
                            convertValue(from_amount);
                        }
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Invalid value", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Put value to initial", Toast.LENGTH_SHORT).show();
                }

            }
        });

        this.progressBar = (ProgressBar) findViewById(R.id.progressBarMain);
        this.fromListView = (ListView) findViewById(R.id.fromListView);
        this.toListView = (ListView) findViewById(R.id.toListView);
        this.fromView = (EditText) findViewById(R.id.currFromEdit);
        this.toView = (EditText) findViewById(R.id.currToEdit);

        setListViewOnCLickListeners(fromListView, "from");
        setListViewOnCLickListeners(toListView, "to");

        this.downloadCurrencyInfo();

        IntentFilter filter = new IntentFilter(Receiver.ACTION_DOWNLOADED);
        filter.addAction(Receiver.ACTION_CONVERTED);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new Receiver();
        registerReceiver(receiver, filter);

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

    private void downloadCurrencyInfo() {
        progressBar.setVisibility(View.VISIBLE);
        Intent intent = new Intent(MainActivity.this, CurrencyService.class);
        intent.setAction(CurrencyService.ACTION_DOWNLOAD_CURRENCY);
        startService(intent);
    }

    private void convertValue(double from_amount) {
        Intent intent = new Intent(MainActivity.this, CurrencyService.class);
        intent.setAction(CurrencyService.ACTION_CONVERT);
        intent.putExtra("from_amount", from_amount);
        intent.putExtra("from", from);
        intent.putExtra("to", to);
        startService(intent);
    }

    private void setListViewOnCLickListeners(final ListView listView, final String direction) {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                String s = (String) listView.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                if (direction.equals("from")) {
                    from = s.split(":")[0];
                } else {
                    to = s.split(":")[0];
                }
            }
        });
    }

    public class Receiver extends BroadcastReceiver {

        public static final String ACTION_DOWNLOADED = "currency.downloaded";
        public static final String ACTION_CONVERTED = "currency.converted";


        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_DOWNLOADED)) {
                handleActionDownloaded();
            } else {
                if (intent.getAction().equals(ACTION_CONVERTED)) {
                    handleActionConverted(intent);
                }
            }

        }

        private void handleActionDownloaded() {
            ArrayList<APIService.Currency> currencyList =
                    (ArrayList<APIService.Currency>) CurrencyService.getCurrencyService().currencies.clone();

            ArrayAdapter<String> adapterFrom = new ArrayAdapter<>(MainActivity.this,
                    R.layout.currency_list_item, castCurrencyListToStrignList(currencyList));
            MainActivity.this.fromListView.setAdapter(adapterFrom);

            ArrayAdapter<String> adapterTo = new ArrayAdapter<>(MainActivity.this,
                    R.layout.currency_list_item, castCurrencyListToStrignList(currencyList));
            MainActivity.this.toListView.setAdapter(adapterTo);
            progressBar.setVisibility(View.GONE);
        }

        private void handleActionConverted(Intent intent) {
            double to_amount = intent.getDoubleExtra("to_amount", 0.0);
            toView.setText(String.valueOf(to_amount));
            progressBar.setVisibility(View.GONE);
        }

        private ArrayList<String> castCurrencyListToStrignList(ArrayList<APIService.Currency> currList) {
            ArrayList<String> stringList = new ArrayList<>();

            for (APIService.Currency el : currList) {
                stringList.add(el.getId() + ": " + el.getDescription());
            }

            return stringList;
        }
    }
}
