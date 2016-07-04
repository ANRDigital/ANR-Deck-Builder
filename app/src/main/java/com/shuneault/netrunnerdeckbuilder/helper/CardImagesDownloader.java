package com.shuneault.netrunnerdeckbuilder.helper;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.shuneault.netrunnerdeckbuilder.MainActivity;
import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.CardList;

import java.io.File;
import java.io.FileOutputStream;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class CardImagesDownloader extends AsyncTask<Void, Integer, Bitmap> {

    private Context mContext;
    private CardImagesDownloaderListener mListener;
    private Card mCardDownloaded;

    // Notification
    NotificationManager mNotifManager;
    NotificationCompat.Builder mNotifBuilder;
    private static final int NOTIFICATION_DOWNLOAD_IMAGES = 1;

    public interface CardImagesDownloaderListener {
        void onBeforeStartTask(Context context, int max);

        void onTaskCompleted();

        void onImageDownloaded(Card card, int count, int max);
    }

    public CardImagesDownloader(Context context, CardImagesDownloaderListener listener) {
        mListener = listener;
        this.mContext = context;

        PendingIntent contentIntent = PendingIntent.getActivity(mContext, NOTIFICATION_DOWNLOAD_IMAGES, new Intent(mContext, MainActivity.class), PendingIntent.FLAG_CANCEL_CURRENT);

        mNotifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifBuilder = new NotificationCompat.Builder(context);
        mNotifBuilder.setContentIntent(contentIntent);
        mNotifBuilder.setSmallIcon(R.drawable.ic_launcher);
        mNotifBuilder.setContentTitle(context.getResources().getString(R.string.downloading_images));
        mNotifBuilder.setOngoing(true);
    }

    @Override
    protected Bitmap doInBackground(Void... voids) {
        CardList cardList = (CardList) AppManager.getInstance().getAllCards().clone();

        // Call the listener
        mListener.onBeforeStartTask(mContext, cardList.size());

        // Percentage counter
        int iCount = 0;

        // Download all images
        for (Card theCard : cardList) {
            // Increment the counter
            iCount++;
            // Download if the file does not exists
            //File f = new File(mContext.getFilesDir(), theCard.getImageFileName());
            File f = new File(mContext.getCacheDir(), theCard.getImageFileName());
            if (f.exists()) {
                // Image is downloaded
                mCardDownloaded = theCard;
                //publishProgress(iCount);
                continue;
            }

            try {
                HttpsURLConnection conn = (HttpsURLConnection) theCard.getImagesrc().openConnection();
                conn.setSSLSocketFactory(getTrustAllSocketFactory().getSocketFactory()); // Trust all SSL certificates
                conn.connect();
                Bitmap theImage = BitmapFactory.decodeStream(conn.getInputStream());
                //FileOutputStream out = mContext.openFileOutput(theCard.getImageFileName(), Context.MODE_PRIVATE);
                FileOutputStream out = new FileOutputStream(f);
                theImage.compress(Bitmap.CompressFormat.PNG, 90, out);
                out.close();
                // Image is downloaded
                mCardDownloaded = theCard;
                publishProgress(iCount);
            } catch (Exception e) {
                //
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        mListener.onImageDownloaded(mCardDownloaded, values[0], AppManager.getInstance().getAllCards().size());
        mNotifBuilder.setContentText(String.format(mContext.getResources().getString(R.string.downloading_images_amount), values[0], AppManager.getInstance().getAllCards().size()));
        mNotifBuilder.setProgress(AppManager.getInstance().getAllCards().size(), values[0], false);
        mNotifManager.notify(NOTIFICATION_DOWNLOAD_IMAGES, mNotifBuilder.build());
    }

    @Override
    protected void onPostExecute(Bitmap result) {

        // Call the listener
        mListener.onTaskCompleted();
        mNotifManager.cancel(NOTIFICATION_DOWNLOAD_IMAGES);

    }

    private SSLContext getTrustAllSocketFactory() {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                }
        };
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            return sc;
        } catch (Exception ignored) {}
        return null;
    }

}
