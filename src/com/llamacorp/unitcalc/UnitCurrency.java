package com.llamacorp.unitcalc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

public class UnitCurrency extends Unit {
	private static String JSON_URL_RATE_TAG = "rate";
	
	private static String mURLPrefix = "http://rate-exchange.appspot.com/currency?from=USD&to=";
	private static String mURLSuffix = "";
	
	
	public UnitCurrency(String name, String longName, double value){
		super(name, longName, value);
	}	
	
	public UnitCurrency(String name, double value){
		super(name, name, value);
	}	
		
	//TODO do we need this?
	public UnitCurrency(){
		super();
	}

	@Override
	public String convertTo(Unit toUnit, String expressionToConv) {
		return expressionToConv + "*" + toUnit.getValue() + "/" + getValue();
	}
		
	/**
	 * Asynchronously try to update the currency rate by fetching the
	 * value via an HTTP JSON API call.  Note that this call will take 
	 * some time to update the value, since it runs in the background.
	 * Also note that the value may or may not even be updated, dependent
	 * on internet connection.  
	 */
	public void asyncRefresh(){
		new HttpAsyncTask().execute(getURL());
	}
	
	private String getURL(){
		return mURLPrefix + toString() + mURLSuffix;
	}

	/** Set value (presumably after HTTP API call)*/
	private void setValue(double val) {
		mValue = val;
	}
	
	
	/**
	 * This class is used to create a background task that handles 
	 * the actual HTTP getting and JSON parsing
	 */
    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            return GET(urls[0]);
        }
		
        // This method is called after doInBackground completes
        @Override
        protected void onPostExecute(String result) {
        	//Toast.makeText(appContext, "Received!", Toast.LENGTH_LONG).show();
			parseRateFromJSONString(result);
 		}
	   	
		/**
		 * Attempt to take a JSON string, parse it, and set the value
		 * of this current Currency Unit
		 */		
		private void parseRateFromJSONString(String result){
            try {
				JSONObject json = new JSONObject(result);
				double rate = json.getDouble(JSON_URL_RATE_TAG);
				setValue(rate);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		/** Helper function for above method*/
		private String GET(String url){
			InputStream inputStream = null;
			String result = "";
			try {
				// create HttpClient
				HttpClient httpclient = new DefaultHttpClient();

				// make GET request to the given URL
				HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

				// receive response as inputStream
				inputStream = httpResponse.getEntity().getContent();

				// convert inputstream to string
				if(inputStream != null)
					result = convertInputStreamToString(inputStream);
				else
					result = "Did not work!";

			} catch (Exception e) {
				Log.d("InputStream", e.getLocalizedMessage());
			}

			return result;
		}

		/** Helper function for above method*/
		private String convertInputStreamToString(InputStream inputStream) throws IOException{
			BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
			String line = "";
			String result = "";
			while((line = bufferedReader.readLine()) != null)
				result += line;

			inputStream.close();
			return result;

		}
    }
}
