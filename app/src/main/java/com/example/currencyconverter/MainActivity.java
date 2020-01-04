package com.example.currencyconverter;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import androidx.annotation.NonNull;

import android.icu.util.Currency;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {


    File path = null;
    File fileToParse = null;
    final String NAME = "fileToParse.xml";
    private final String ECBURL = "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-hist-90d.xml";
    private final int NUM_RATES = 32; // There are 32 exchange rates


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DownloadChecker();
    }

    public class Rate {
        private Currency currency;
        private double exchangeRate;

        private Rate(String ISO, double exchangeRate) {
            this.currency = Currency.getInstance(ISO);
            this.exchangeRate = exchangeRate;
        }

        @NonNull
        @Override
        public String toString() {
            return String.format(Locale.getDefault(),
                    "%s - %.2f", currency.getDisplayName(), exchangeRate);
        }

        // write methods when needed
    }

    public void Parser(File file, String date) throws XmlPullParserException, IOException {

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        StringBuilder sb = new StringBuilder();

        if (fileToParse == null) {
            xpp.setInput(getResources().openRawResource(R.raw.eurofxref), null);
        } else {
            FileInputStream fileInputStream = new FileInputStream(file);
            xpp.setInput(fileInputStream, null);
        }
        ArrayList<Rate> rates = new ArrayList<>();

        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                int attributeCount = xpp.getAttributeCount();
                if (attributeCount > 0) {
                    if (xpp.getAttributeName(0).equals("time")
                            && xpp.getAttributeValue(0).equals(date)) {
                        int i = 0;
                        while (i < NUM_RATES) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.START_TAG) {
                                String ISOCode = xpp.getAttributeValue(0);
                                double exchangeRate = Double.parseDouble(xpp.getAttributeValue(1));
                                rates.add(new Rate(ISOCode, exchangeRate));
                                sb.append(rates.get(i).toString());
                                sb.append("\n");
                                i++;
                            }
                        }
                        break;
                    }
                }
            }
            eventType = xpp.next();
        }
        sb.append("Total number: ");
        sb.append(rates.size());
        TextView myTextView = findViewById(R.id.text_view1);
        myTextView.setText(sb.toString());

    }

    public boolean DownloadChecker() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        path = getFilesDir();
        fileToParse = new File(path, NAME);
        if (connMgr != null) {
            NetworkInfo networkinfo = connMgr.getActiveNetworkInfo();
            if (networkinfo != null && networkinfo.isConnected()) {
                new DownloadFileFromURL().execute(ECBURL);
                Toast.makeText(getApplicationContext(), "Downloaded updated values"
                        , Toast.LENGTH_LONG).show();
                return true;
            } else {
                Toast.makeText(getApplicationContext(), "Failed to download updated values\n Check your connection"
                        , Toast.LENGTH_LONG).show();
            }
        }
        return false;
    }

    public class DownloadFileFromURL extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                if(downloadFile(urls[0])) {
                    return "Downloaded updated values";
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "Failed to download updated values\n Check your connection.";
        }

        @Override
        protected void onPostExecute(String message) {
            try {
                Parser(fileToParse, getIntent().getStringExtra("UserDate"));
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean downloadFile(String myUrl) throws IOException {

        InputStream input;
        FileOutputStream output = new FileOutputStream(fileToParse);
        URL url = new URL(myUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setReadTimeout(10000);
        conn.setConnectTimeout(25000);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();

        // Commented lines are exclusively for debugging

        input = conn.getInputStream();
        int totalSize = conn.getContentLength();
        // int downloadedSize = 0;
        byte[] buffer = new byte[1024];
        int bufferLength;
        while ((bufferLength = input.read(buffer)) > 0) {
            output.write(buffer, 0, bufferLength);
            // downloadedSize += bufferLength;
            // int progress = (int) (downloadedSize * 100 / totalSize);
        }
        input.close();
        output.close();
        return true;
    }
}
