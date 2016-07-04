package com.shuneault.netrunnerdeckbuilder.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.HttpsConnection;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.shuneault.netrunnerdeckbuilder.game.Card;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class ImageDisplayer {

    private Context mContext;
    private ImageView mImageView;
    private static SingleCardDownloader singleCardDownloader;

    public void fillImageWithCard(ImageView imageView, Card card, Context context, boolean small) {
        this.mContext = context;
        this.mImageView = imageView;

        // Get the image in a thread and display in the ImageView
        Bitmap theImage = small ? card.getSmallImage(context) : card.getImage(context);
        if (theImage != null) {
            imageView.setImageBitmap(theImage);
        } else {
            // Remove the image prior to download
            imageView.setImageResource(context.getResources().getIdentifier("card_back_" + card.getSideCode(), "drawable", context.getPackageName()));

            // Download
            singleCardDownloader = new SingleCardDownloader();
            singleCardDownloader.execute(card);
        }
    }

    public static void fill(ImageView imageView, Card card, Context context) {
        if (card == null) return;
        ImageDisplayer im = new ImageDisplayer();
        im.fillImageWithCard(imageView, card, context, false);
    }

    public static void fillSmall(ImageView imageView, Card card, Context context) {
        if (card == null) return;
        ImageDisplayer im = new ImageDisplayer();
        im.fillImageWithCard(imageView, card, context, true);
    }

    public class SingleCardDownloader extends AsyncTask<Card, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Card... params) {
            Card card = params[0];

            try {
                HttpsURLConnection conn = (HttpsURLConnection) card.getImagesrc().openConnection();
                conn.setSSLSocketFactory(getTrustAllSocketFactory().getSocketFactory());
                Bitmap theImage = BitmapFactory.decodeStream(conn.getInputStream());
                //FileOutputStream out = mContext.openFileOutput(card.getImageFileName(), Context.MODE_PRIVATE);
                FileOutputStream out = new FileOutputStream(new File(mContext.getCacheDir(), card.getImageFileName()));
                theImage.compress(Bitmap.CompressFormat.PNG, 90, out);
                out.close();
                return theImage;
            } catch (Exception e) {
                Log.e("LOG", "Could not download image: " + card.getImagesrc().toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            // Only display if still in the queue
            if (!this.isCancelled())
                mImageView.setImageBitmap(result);
            //mImageView.setVisibility(View.VISIBLE);

        }

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
