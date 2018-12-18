package com.shuneault.netrunnerdeckbuilder.importer;

import android.text.Html;

import com.shuneault.netrunnerdeckbuilder.db.CardRepository;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.game.Format;
import com.shuneault.netrunnerdeckbuilder.helper.AppManager;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

/**
 * Created by sebast on 22/06/16.
 */

class XmlImporter implements IDeckImporter {
    private static final String KEY_QTY = "qty";
    private static final String KEY_ID = "id";
    private static final String XPATH_IDENTITY_ID = "/deck/section[@name='Identity']/card/@id";
    private static final String XPATH_DECK_NAME = "/deck/section[@name='Identity']/card";
    private static final String XPATH_DECK_NOTES = "/deck/notes";
    private static final String XPATH_CARD_LIST = "/deck/section[@name='R&D / Stack']/card";
    private final CardRepository repo;

    private ArrayList<Deck> mDecks;

    XmlImporter(String text, CardRepository repo) throws Exception {
        this.repo = repo;
        ArrayList<Deck> decks = new ArrayList<>();

        // Build the XML document
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new InputSource(new StringReader(text)));
        doc.getDocumentElement().normalize();
        // Get the Identity ID
        String strIdentityID = XPathFactory.newInstance().newXPath().evaluate(XPATH_IDENTITY_ID, doc);
        // Get the deck name
        String strDeckName = XPathFactory.newInstance().newXPath().evaluate(XPATH_DECK_NAME, doc);
        // Get the deck notes
        String deckNotes = XPathFactory.newInstance().newXPath().evaluate(XPATH_DECK_NOTES, doc);
        // Get the card list
        NodeList nodeCards = (NodeList) XPathFactory.newInstance().newXPath().evaluate(XPATH_CARD_LIST, doc, XPathConstants.NODESET);

        // Build the new deck
        String identityCode = getCardCodeFromUUID(strIdentityID);
        Card identityCard = this.repo.getCard(identityCode);
        //todo: getdefaultformat
        Format format = repo.getDefaultFormat();
        Deck deck = new Deck(identityCard, format);
        deck.setNotes(Html.fromHtml(deckNotes).toString());
        for (int i = 0; i < nodeCards.getLength(); i++) {
            Node node = nodeCards.item(i);
            String code = getCardCodeFromUUID(node.getAttributes().getNamedItem(KEY_ID).getTextContent());
            Card card = repo.getCard(code);
            if (card != null) {
                // TODO: Tell the user we could not import all cards OR download the cards from the Internet
                int count = Integer.parseInt(node.getAttributes().getNamedItem(KEY_QTY).getTextContent());
                deck.setCardCount(card, count);
            } else {
                return;
            }
        }
        deck.clearCardsToAddAndRemove();
        decks.add(deck);

        // Return the decks
        mDecks = decks;
    }

    @Override
    public ArrayList<Deck> toDecks() {
        return mDecks;
    }

    private static String getCardCodeFromUUID(String uuid) {
        return uuid.substring(uuid.length() - 5);
    }
}
