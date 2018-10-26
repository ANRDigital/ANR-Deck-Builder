package com.shuneault.netrunnerdeckbuilder.game;

import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

public class CardBuilder {
    //    public static final String NAME_LAST_MODIFIED = "last-modified";
    private static final String NAME_CODE = "code";
    private static final String NAME_COST = "cost";
    private static final String NAME_TITLE = "title";
    //    public static final String NAME_TYPE = "type";
    private static final String NAME_TYPE_CODE = "type_code";
    private static final String NAME_SUBTYPE = "keywords";
    //    public static final String NAME_SUBTYPE_CODE = "subtype_code";
    private static final String NAME_TEXT = "text";
    private static final String NAME_BASELINK = "base_link";
    //    public static final String NAME_FACTION = "faction";
    private static final String NAME_FACTION_CODE = "faction_code";
    private static final String NAME_FACTION_COST = "faction_cost";
    private static final String NAME_FLAVOR = "flavor";
    private static final String NAME_ILLUSTRATOR = "illustrator";
    private static final String NAME_INFLUENCE_LIMIT = "influence_limit";
    private static final String NAME_MINIMUM_DECK_SIZE = "minimum_deck_size";
    private static final String NAME_NUMBER = "position";
    private static final String NAME_QUANTITY = "quantity";
    //    public static final String NAME_SET_NAME = "setname";
    private static final String NAME_SET_CODE = "pack_code";
    //    public static final String NAME_SIDE = "side";
    private static final String NAME_SIDE_CODE = "side_code";
    private static final String NAME_UNIQUENESS = "uniqueness";
    //    public static final String NAME_URL = "url";
    private static final String NAME_IMAGE_URL_OVERRIDE = "image_url";
    //    public static final String NAME_IMAGE_SRC = "imagesrc";
    private static final String NAME_AGENDA_POINTS = "agenda_points";
    private static final String NAME_ADVANCEMENT_COST = "advancement_cost";
    private static final String NAME_MEMORY_UNITS = "memory_cost";
    private static final String NAME_TRASH = "trash_cost";
    private static final String NAME_STRENGTH = "strength";

    private String imageUrlTemplate;

    public CardBuilder(String imageUrlTemplate) {

        this.imageUrlTemplate = imageUrlTemplate;
    }

    public Card BuildFromJson(JSONObject json) {
        Card card = null;
        try {
            card = new Card();
            card.setCode(json.optString(NAME_CODE));
            card.setCost( json.optString(NAME_COST));
            card.setTitle(json.optString(NAME_TITLE));
            card.setTypeCode(json.optString(NAME_TYPE_CODE));
            card.setSubtype(json.optString(NAME_SUBTYPE));
            card.setText(json.optString(NAME_TEXT));
            card.setBaseLink(json.optString(NAME_BASELINK));
            card.setFactionCode(json.optString(NAME_FACTION_CODE));
            card.setFactionCost(json.optInt(NAME_FACTION_COST, 0));
            card.setFlavor(json.optString(NAME_FLAVOR));
            card.setIllustrator(json.optString(NAME_ILLUSTRATOR));
            card.setInfluenceLimit(json.optString(NAME_INFLUENCE_LIMIT));
            card.setMinimumDeckSize(json.optString(NAME_MINIMUM_DECK_SIZE));
            card.setNumber(json.optString(NAME_NUMBER));
            card.setQuantity(json.optString(NAME_QUANTITY));
            card.setSetCode(json.optString(NAME_SET_CODE));
            card.setUniqueness(json.optBoolean(NAME_UNIQUENESS));
            // does it have an unusual image_src? If not follow standard pattern
            String imageUrl = json.optString(NAME_IMAGE_URL_OVERRIDE);
            if (imageUrl == null || imageUrl.length() < 1) {
                imageUrl = imageUrlTemplate.replace("{code}", card.getCode());
            }
            card.setImageSrc(new URL(imageUrl));
            card.setSideCode(json.optString(NAME_SIDE_CODE));
            card.setAgendaPoints(json.optInt(NAME_AGENDA_POINTS, 0));
            card.setAdvancementCost(json.optInt(NAME_ADVANCEMENT_COST, 0));
            card.setMemoryUnits(json.optInt(NAME_MEMORY_UNITS, 0));
            card.setTrash(json.optInt(NAME_TRASH, 0));
            card.setStrength(json.optInt(NAME_STRENGTH, 0));
        } catch (MalformedURLException e) {
            //
            e.printStackTrace();
        }
        
        return card;
    }
}
