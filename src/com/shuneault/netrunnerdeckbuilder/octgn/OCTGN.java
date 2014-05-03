package com.shuneault.netrunnerdeckbuilder.octgn;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.text.Html;

import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.helper.AppManager;

public class OCTGN {
	
	private static final String KEY_QTY = "qty";
	private static final String KEY_ID = "id";
	private static final String XPATH_IDENTITY_ID = "/deck/section[@name='Identity']/card/@id";
	private static final String XPATH_DECK_NAME = "/deck/section[@name='Identity']/card";
	private static final String XPATH_DECK_NOTES = "/deck/notes";
	private static final String XPATH_CARD_LIST = "/deck/section[@name='R&D / Stack']/card";
	
	public static Deck toDeck(String xmlString) {
		try {
			// Build the XML document
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new InputSource(new StringReader(xmlString)));
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
			Deck deck = new Deck(strDeckName, getCardCodeFromUUID(strIdentityID));
			deck.setNotes(Html.fromHtml( deckNotes ).toString());
			for (int i = 0; i < nodeCards.getLength(); i++) {
				Node node = nodeCards.item(i);
				Card card = AppManager.getInstance().getCard(getCardCodeFromUUID(node.getAttributes().getNamedItem(KEY_ID).getTextContent()));
				int count = Integer.parseInt( node.getAttributes().getNamedItem(KEY_QTY).getTextContent() );
				deck.setCardCount(card, count);
			}
			deck.clearCardsToAddAndRemove();
			
			// Return the deck
			return deck;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private static String getCardCodeFromUUID(String uuid) {
		return uuid.substring(uuid.length()-5);
	}
}
