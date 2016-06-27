package com.shuneault.netrunnerdeckbuilder.importer;

import com.shuneault.netrunnerdeckbuilder.game.Deck;

import java.util.ArrayList;

public interface IDeckImporter {
    ArrayList<Deck> toDecks();
}