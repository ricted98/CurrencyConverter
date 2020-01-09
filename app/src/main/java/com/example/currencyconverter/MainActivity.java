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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
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

    File fileToParse = null;
    boolean connectionAvailable;
    final String NAME = "fileToParse.xml";
    private String dateToUse;
    private final String ECBURL = "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-hist-90d.xml";
    private final int NUM_RATES = 32; // There are 32 exchange rates in the file
    // Final ArrayList size will be 33 since we added EUR currency manually
    private Spinner spin;
    private Spinner spinn;
    private EditText edit;
    ArrayList<Rate> ratesList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fileToParse = new File(getFilesDir(), NAME);
        connectionAvailable = DownloadChecker();
        if (connectionAvailable) {
            dateToUse = getIntent().getStringExtra("UserDate");
            new DownloadFileFromURL().execute(ECBURL);
        } else {
            try {
                ratesList = Parser(fileToParse, dateToUse);
                Spinners(ratesList);
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }
        }
    }


    public ArrayList<Rate> Parser(File file, String date) throws XmlPullParserException, IOException {

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        // StringBuilder can be used to debug
        //StringBuilder sb = new StringBuilder();
        boolean useDefaultDate = !connectionAvailable;

        if (!(file.exists() && file.length() != 0)) {
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
                            && (xpp.getAttributeValue(0).equals(date) || useDefaultDate)) {
                        int i = 0;
                        while (i < NUM_RATES) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.START_TAG) {
                                String ISOCode = xpp.getAttributeValue(0);
                                double exchangeRate = Double.parseDouble(xpp.getAttributeValue(1));
                                rates.add(new Rate(ISOCode, exchangeRate));
                                //sb.append(rates.get(i).toString());
                                //sb.append("\n");
                                i++;
                            }
                        }
                        break;
                    }
                }
            }
            eventType = xpp.next();
        }
        //sb.append("Total number: ");
        //sb.append(rates.size());
        //TextView myTextView = findViewById(R.id.text_view1);
        //myTextView.setText(sb.toString());
        rates.add(0, new Rate("EUR", 1.0d));
        return rates;

    }

    public boolean DownloadChecker() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        // path = getFilesDir();
        // fileToParse = new File(path, NAME);
        if (connMgr != null) {
            NetworkInfo networkinfo = connMgr.getActiveNetworkInfo();
            if (networkinfo != null && networkinfo.isConnected()) {
                //new DownloadFileFromURL().execute(ECBURL);
                Toast.makeText(getApplicationContext(), "Downloaded updated values"
                        , Toast.LENGTH_LONG).show();
                return true;
            } else {
                Toast.makeText(getApplicationContext(), "Failed to download updated values\nCheck your connection"
                        , Toast.LENGTH_LONG).show();
            }
        }
        return false;
    }

    public class DownloadFileFromURL extends AsyncTask<String, String, Boolean> {

        @Override
        protected Boolean doInBackground(String... urls) {
            try {
                if (downloadFile(urls[0])) {
                    return new Boolean(true);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new Boolean(false);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            try {
                ratesList = Parser(fileToParse, dateToUse);
                //lets call the spinner
                Spinners(ratesList);
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
        //int totalSize = conn.getContentLength();
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

    public void Spinners(ArrayList ratesList) {
        edit = findViewById(R.id.edit_in);
        spin = findViewById(R.id.spinner_in);
        ArrayAdapter<Rate> adapterin = new ArrayAdapter<Rate>(this, android.R.layout.simple_spinner_item, ratesList);
        adapterin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spin.setAdapter(adapterin);

        spinn = findViewById(R.id.spinner_out);
        ArrayAdapter<Rate> adapterout = new ArrayAdapter<Rate>(this, android.R.layout.simple_spinner_item, ratesList);
        adapterout.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinn.setAdapter(adapterout);

    }


    public void getSelected(View v) {
        Rate rateIn = (Rate) spin.getSelectedItem();
        Rate rateOut = (Rate) spinn.getSelectedItem();
        String userInput = edit.getText().toString();
        double money = userInput.equals("") ? 0 : Double.parseDouble(userInput);
        convert(rateIn, rateOut, money);
    }

    public void convert(Rate rateIn, Rate rateOut, double inputValue) {

        double outputValue = rateOut.getExchangeRate() / rateIn.getExchangeRate() * inputValue;
        TextView editOut = findViewById(R.id.edit_out);
        editOut.setText(String.format("%.2f", outputValue));


        /*String choiceIn = rateIn.toString();
        String choiceOut = rateOut.toString();
        Toast.makeText(this, "you have " + choiceIn + "\nto " + choiceOut, Toast.LENGTH_LONG).show();
         */
    }

    public void switchRates(View v) {
        Rate rateIn = (Rate) spin.getSelectedItem();
        Rate rateOut = (Rate) spinn.getSelectedItem();
        spin.setSelection(ratesList.indexOf(rateOut));
        spinn.setSelection(ratesList.indexOf(rateIn));
    }
}
