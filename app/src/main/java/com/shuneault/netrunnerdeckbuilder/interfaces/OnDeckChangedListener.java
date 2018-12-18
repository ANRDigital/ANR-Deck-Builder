package com.shuneault.netrunnerdeckbuilder.interfaces;

import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.game.Format;

public interface OnDeckChangedListener {
    void onDeckCardsChanged();

    void onFormatChanged(Format format);
}
