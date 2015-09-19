package com.shuneault.netrunnerdeckbuilder.helper;

import android.content.Context;
import android.os.AsyncTask;

import com.shuneault.netrunnerdeckbuilder.SettingsActivity;
import com.shuneault.netrunnerdeckbuilder.game.NetRunnerBD;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CardDownloader extends AsyncTask<Void, Integer, JSONArray> {

    private Context mContext;
    private CardDownloaderListener mListener;

    public interface CardDownloaderListener {
        void onBeforeStartTask(Context context);

        void onTaskCompleted();

        void onDownloadError();
    }

    public CardDownloader(Context context, CardDownloaderListener listener) {
        mContext = context;
        mListener = listener;
    }

    @Override
    protected void onPreExecute() {
        // Call the listener
        mListener.onBeforeStartTask(mContext);
    }

    @Override
    protected JSONArray doInBackground(Void... params) {

        // Download the JSON file
        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(String.format(NetRunnerBD.URL_GET_ALL_CARDS, AppManager.getInstance().getSharedPrefs().getString(SettingsActivity.KEY_PREF_LANGUAGE, "en")));
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

            return new JSONArray(strResult);

        } catch (ClientProtocolException e1) {
        } catch (IOException e1) {
        } catch (JSONException e) {
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONArray result) {
        // No file - throw error
        if (result == null) {
            mListener.onDownloadError();
            return;
        }

        // Save the files
        try {

            // Delete the old file
            File f = new File(mContext.getFilesDir(), AppManager.FILE_CARDS_JSON);
            if (f.exists())
                f.delete();

            // Save the file
            String fileName = AppManager.FILE_CARDS_JSON;
            FileOutputStream outputStream = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStream.write(result.toString().getBytes());
            outputStream.close();

            // Call the listener
            mListener.onTaskCompleted();

        } catch (Exception e) {
            mListener.onDownloadError();
            //Toast.makeText(MainActivity.this, R.string.error_downloading_cards, Toast.LENGTH_LONG).show();
        }
    }


}
