package com.shuneault.netrunnerdeckbuilder.importer;

import com.shuneault.netrunnerdeckbuilder.game.Deck;

import java.util.ArrayList;

/**
 * Created by sebast on 22/06/16.
 */

public class UniversalImporter implements IDeckImporter {

    private ArrayList<Deck> mDecks;

    public UniversalImporter(String text) throws Exception {
        try {
            mDecks = (new JsonImporter(text)).toDecks();
        } catch (Exception ignored) {
            try {
                mDecks = (new XmlImporter(text)).toDecks();
            } catch (Exception ignored2) {}
        }
    }

    @Override
    public ArrayList<Deck> toDecks() {
        return mDecks;
    }


}
