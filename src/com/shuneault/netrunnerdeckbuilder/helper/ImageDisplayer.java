package com.shuneault.netrunnerdeckbuilder.helper;

import java.io.File;
import java.io.FileOutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.shuneault.netrunnerdeckbuilder.game.Card;

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
			singleCardDownloader = new SingleCardDownloader();
			singleCardDownloader.execute(card);
		}
	}
	
	public static void fill(ImageView imageView, Card card, Context context) {
		ImageDisplayer im = new ImageDisplayer();
		im.fillImageWithCard(imageView, card, context, false);
	}
	public static void fillSmall(ImageView imageView, Card card, Context context) {
		ImageDisplayer im = new ImageDisplayer();
		im.fillImageWithCard(imageView, card, context, true);
	}
	
	public class SingleCardDownloader extends AsyncTask<Card, Void, Bitmap> {

		@Override
		protected Bitmap doInBackground(Card... params) {
			Card card = params[0];
			
			try {
				Bitmap theImage = BitmapFactory.decodeStream(card.getImagesrc().openConnection().getInputStream());
				//FileOutputStream out = mContext.openFileOutput(card.getImageFileName(), Context.MODE_PRIVATE);
				FileOutputStream out = new FileOutputStream(new File(mContext.getCacheDir(), card.getImageFileName()));
				theImage.compress(Bitmap.CompressFormat.PNG, 90, out);
				out.close();
				return theImage;
			} catch (Exception e) { }
			return null;
		}
		
		@Override
		protected void onPostExecute(Bitmap result) {
			if (!this.isCancelled())
				mImageView.setImageBitmap(result);
			//mImageView.setVisibility(View.VISIBLE);
			
		}
		
	}
	
}
