package com.example.currencyconverter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    final String NAME = "fileToParse.xml";
    final String ECB_URL = "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-hist-90d.xml";
    final int NUM_RATES = 32; // There are 32 exchange rates in the file
    // Final ArrayList size will be 33 since we added EUR currency manually
    private String dateToUse;
    private Spinner spinIn;
    private Spinner spinOut;
    private EditText edit;
    ArrayList<Rate> ratesList;
    File fileToParse = null;
    boolean connectionAvailable;

    // Commented lines are exclusively for debugging

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fileToParse = new File(getFilesDir(), NAME);
        connectionAvailable = DownloadChecker();
        if (connectionAvailable) {
            dateToUse = getIntent().getStringExtra("UserDate");
            new DownloadFileFromURL().execute(ECB_URL);
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
        spinIn = findViewById(R.id.spinner_in);
        ArrayAdapter<Rate> adapterIn = new ArrayAdapter<Rate>(this, android.R.layout.simple_spinner_item, ratesList);
        adapterIn.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinIn.setAdapter(adapterIn);

        spinOut = findViewById(R.id.spinner_out);
        ArrayAdapter<Rate> adapterOut = new ArrayAdapter<Rate>(this, android.R.layout.simple_spinner_item, ratesList);
        adapterOut.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinOut.setAdapter(adapterOut);

    }


    public void switchRates(View v) {
        Rate rateIn = (Rate) spinIn.getSelectedItem();
        Rate rateOut = (Rate) spinOut.getSelectedItem();
        spinIn.setSelection(ratesList.indexOf(rateOut));
        spinOut.setSelection(ratesList.indexOf(rateIn));
    }

    public void getSelected(View v) {
        Rate rateIn = (Rate) spinIn.getSelectedItem();
        Rate rateOut = (Rate) spinOut.getSelectedItem();
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
}
