package com.shuneault.netrunnerdeckbuilder.helper;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;

import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.game.Card;

public class NrdbHelper {
    /*
    Shows a card's page on netrunnerdb.com
     */
    public static void ShowNrdbWebPage(Context context, Card card) {
        String url = String.format(context.getString(R.string.nrdb_card_url), card.getCode());
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(context, Uri.parse(url));
    }
}
