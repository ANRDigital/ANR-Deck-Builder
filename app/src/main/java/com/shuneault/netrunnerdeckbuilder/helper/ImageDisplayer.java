package com.shuneault.netrunnerdeckbuilder.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.util.CardImageDownloadUtil;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

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
                return CardImageDownloadUtil.downloadCardImage(card, mContext);
            } catch(Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            // Only display if still in the queue
            if (!this.isCancelled())
                mImageView.setImageBitmap(result);
            //mImageView.setVisibility(View.VISIBLE);

        }

    }

}
