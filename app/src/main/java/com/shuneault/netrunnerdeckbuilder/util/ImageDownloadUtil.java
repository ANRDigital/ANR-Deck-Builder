package com.shuneault.netrunnerdeckbuilder.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;

public class ImageDownloadUtil {

    public static Bitmap downloadImageToCache(Context context, URL imageSrc, String localFileName) throws Exception {
        try {
            URLConnection conn = imageSrc.openConnection();
            Bitmap theImage = BitmapFactory.decodeStream(conn.getInputStream());
            //FileOutputStream out = context.openFileOutput(card.getImageFileName(), Context.MODE_PRIVATE);
            FileOutputStream out = new FileOutputStream(new File(context.getCacheDir(), localFileName));
            theImage.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            return theImage;
        } catch (Exception e) {
            Log.e("LOG", "Could not download image: " + imageSrc.toString());
            throw e;
        }
    }
}
