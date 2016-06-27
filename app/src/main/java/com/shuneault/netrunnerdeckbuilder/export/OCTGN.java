package com.shuneault.netrunnerdeckbuilder.export;

import android.text.Html;
import android.util.Log;

import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.helper.AppManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

public class OCTGN implements DeckFormatter {

    // OCTGN Specific values
    private static final String OCTGN_GAME_ID = "0f38e453-26df-4c04-9d67-6d43de939c77";
    private static final String OCTGN_CARD_ID_PREFIX = "bc0f047c-01b1-427f-a439-d451eda";

    @Override
    public String fromDeck(Deck deck) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();

            // Root
            Element eleRoot = doc.createElement("deck");
            eleRoot.setAttribute("game", OCTGN_GAME_ID);
            doc.appendChild(eleRoot);
            // Section - Identity
            Element eleSectionIdentity = doc.createElement("section");
            eleSectionIdentity.setAttribute("name", "Identity");
            eleRoot.appendChild(eleSectionIdentity);
            // Card - Identity
            Element eleCardIdentity = doc.createElement("card");
            eleCardIdentity.setAttribute("qty", "1");
            eleCardIdentity.setAttribute("id", OCTGN_CARD_ID_PREFIX + deck.getIdentity().getCode());
            eleCardIdentity.setTextContent(deck.getIdentity().getTitle());
            eleSectionIdentity.appendChild(eleCardIdentity);
            // Section - cards
            Element eleSectionCards = doc.createElement("section");
            eleSectionCards.setAttribute("name", "R&D / Stack");
            eleRoot.appendChild(eleSectionCards);
            // Cards
            for (Card card : deck.getCards()) {
                Element eleCard = doc.createElement("card");
                eleCard.setAttribute("qty", String.valueOf(deck.getCardCount(card)));
                eleCard.setAttribute("id", OCTGN_CARD_ID_PREFIX + card.getCode());
                eleCard.setTextContent(card.getTitle());
                eleSectionCards.appendChild(eleCard);
            }
            // Notes
            Element eleNotes = doc.createElement("notes");
            eleNotes.setTextContent(deck.getNotes());
            eleRoot.appendChild(eleNotes);

            // Return the XML text
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            String output = writer.getBuffer().toString();
            return output;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }


}
