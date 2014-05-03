package com.shuneault.netrunnerdeckbuilder;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.shuneault.netrunnerdeckbuilder.db.DatabaseHelper;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.helper.AppManager;
import com.shuneault.netrunnerdeckbuilder.octgn.OCTGN;

public class ImportOCTGNActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Get the intent
		Intent intent = getIntent();
		
		// Ask for a new deck name
		ProgressDialog pDialog = new ProgressDialog(this);
		pDialog.setIndeterminate(true);
		pDialog.setMessage(getResources().getText(R.string.please_wait));
		
		pDialog.setCancelable(true);
		pDialog.setOnCancelListener(new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				
			}
		});
		pDialog.show();
		
		// Download the image
		if (intent.getData().getScheme().equals("file")) {
			// File opened through file explorer
			Deck deck = OCTGN.toDeck( getImportData(getIntent()) );
			addImportedDeck(deck);
		} else if (intent.getData().getScheme().equals("http")) {
			// Opened through the browser, download the file
			StringDownloader sd = new StringDownloader(new StringDownloaderListener() {
				
				@Override
				public void onDownloadFinish(String result) {
					Deck deck = OCTGN.toDeck( result );
					addImportedDeck(deck);
				}
			});
			sd.execute(intent.getDataString());
		} else if (intent.getData().getScheme().equals("content")) {
			// Opened through Gmail or another mail application
			Deck deck = OCTGN.toDeck( getImportData(getIntent()) );
			addImportedDeck(deck);
		}
		
	}
	
	private void addImportedDeck(Deck deck) {
		// Import the deck with a new name
		deck.setName("[IMP] " + deck.getName());
		// Add the deck
		AppManager.getInstance().addDeck(deck);
		(new DatabaseHelper(this)).createDeck(deck);
		// Toast
		Toast.makeText(this, "Deck imported successfuly", Toast.LENGTH_SHORT).show();
		// Launch the new deck
		Intent intent = new Intent(this, MainActivity.class);
		intent.putExtra(MainActivity.EXTRA_DECK_ID, deck.getRowId());
		startActivity(intent);
		finish();
	}
	
	/**
	 * 
	 * @param intent
	 * @return String representation of the XML file
	 */
	private String getImportData(Intent intent) {
		try {
			InputStream in = getContentResolver().openInputStream(intent.getData());
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null)
				sb.append(line);
			
			String strResult = sb.toString();
			in.close();

			return strResult;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private interface StringDownloaderListener {
		void onDownloadFinish(String result);
	}
	private class StringDownloader extends AsyncTask<String, Void, String> {
				
		private StringDownloaderListener mListener;
		
		public StringDownloader(StringDownloaderListener listener) {
			this.mListener = listener;
		}

		@Override
		protected String doInBackground(String... params) {
			
			// Download the file
			try {
				URL url = new URL(params[0]);
				HttpClient httpClient = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(url.toURI());
				HttpResponse response = httpClient.execute(httpGet);
				HttpEntity entity = response.getEntity();
				InputStream is = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null)
					sb.append(line);
	
				String strResult = sb.toString();
				is.close();
				
				return strResult;
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			// Nothing
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			mListener.onDownloadFinish(result);
		}
		
	}
	

}
