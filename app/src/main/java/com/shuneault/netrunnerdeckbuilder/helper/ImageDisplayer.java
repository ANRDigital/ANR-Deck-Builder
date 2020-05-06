package com.shuneault.netrunnerdeckbuilder.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.util.ImageDownloadUtil;

import java.io.File;

public class ImageDisplayer {

    private Context mContext;
    private ImageView mImageView;
    private static SingleCardDownloader singleCardDownloader;

    public void fillImageWithCard(ImageView imageView, Card card, Context context, boolean small) {
        this.mContext = context;
        this.mImageView = imageView;

        // Get the image in a thread and display in the ImageView
        Bitmap theImage = small ? getSmallImage(context, card.getImageFileName()) : getImage(context, card.getImageFileName());
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
                return ImageDownloadUtil.downloadImageToCache(mContext, card.getImageSrc(), card.getImageFileName());
            } catch (Exception e) {
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

    public static Bitmap getImage(Context context, String imageFileName) {
        return BitmapFactory.decodeFile(new File(context.getCacheDir(), imageFileName).getAbsolutePath());
    }

    public static Bitmap getSmallImage(Context context, String imageFileName) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        return BitmapFactory.decodeFile(new File(context.getCacheDir(), imageFileName).getAbsolutePath(), options);
    }

}
