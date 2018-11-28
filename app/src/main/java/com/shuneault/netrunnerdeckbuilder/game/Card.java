package com.shuneault.netrunnerdeckbuilder.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.SpannableString;

import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.SettingsActivity;
import com.shuneault.netrunnerdeckbuilder.helper.AppManager;
import com.shuneault.netrunnerdeckbuilder.helper.TextFormatter;

import java.io.File;
import java.net.URL;

public class Card {
    //    private Date lastModified;
    private String code;
    private String cost;
    private String title;
    //    private String type;
    private String typeCode;
    private String subtype;
    //    private String subtype_code;
    private String text;
    private String baseLink;
    //    private String faction;
    private String factionCode;
    // factionCost is the out-of-faction influence cost to include in a deck
    private int factionCost;

    private String flavor;
    private String illustrator;
    private String influenceLimit;
    private String minimumDeckSize;
    private String number;
    private String quantity;
    private int deckLimit;
    //    private String setName;
    private String setCode;
    //    private String side;
    private String sideCode;
    private int agendaPoints;
    private int advancementCost;
    private int memoryUnits;
    private int trash;
    private int strength;
    private boolean uniqueness;
    //    private URL url;
    private URL imageSrc;
    private int mostWantedInfluence;
    private boolean isUnknown = false;

    public Card() {

    }

    public Card(String code){
        this.code = code;
    }

    public void setCode(String code){
        this.code = code;
    }
    public String getCode() {
        return code;
    }

    public String getCost() {
        return cost.equals("null") ? "X" : cost;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

//    public String getType() {
//        return type;
//    }

    public String getTypeCode() {
        return typeCode;
    }

    public String getSubtype() {
        return subtype;
    }

//    public String getSubtypeCode() { return subtype_code; }

    public String getText() {
        return text;
    }

    public String getBaseLink() {
        return baseLink;
    }

//    public String getFaction() {
//        return faction;
//    }

    public String getFactionCode() {
        return factionCode;
    }

    public int getFactionCost() {
        return factionCost;
    }

    public String getFlavor() {
        return flavor;
    }

    public String getIllustrator() {
        return illustrator;
    }

    public int getInfluenceLimit() {
        try {
            return Integer.parseInt(influenceLimit);
        } catch (Exception e) {
            return Integer.MAX_VALUE;
        }
    }

    public int getMinimumDeckSize() {
        if (minimumDeckSize.equals(""))
            return 0;
        else
            return Integer.parseInt(minimumDeckSize);
    }

    public int getNumber() {
        if (number.equals(""))
            return 0;
        else
            return Integer.parseInt(number);
    }

    public int getQuantity() {
        if (quantity.equals(""))
            return 0;
        else
            return Integer.parseInt(quantity);
    }

    public String getSetCode() {
        return setCode;
    }

    public String getSideCode() {
        return sideCode;
    }

    public boolean isUniqueness() {
        return uniqueness;
    }

    public int getAgendaPoints() {
        return agendaPoints;
    }

    public int getAdvancementCost() {
        return advancementCost;
    }

    public int getMemoryUnits() {
        return memoryUnits;
    }

    public int getTrashCost() {
        return trash;
    }

    public int getStrength() {
        return strength;
    }


//    public URL getUrl() {
//        return url;
//    }

    public URL getImageSrc() {
        return imageSrc;
    }


    /**
     * Splits subtype string by " - " into array of subtypes.
     *
     * @return Array of subtype strings.
     * If subtype string is empty, array will contain a single empty string.
     */
    public String[] getSubtypeArray() {
        return subtype.split(" - ");
    }



    /**
     * @return Formatted text with images
     */
    public SpannableString getFormattedText(Context context) {
        return TextFormatter.getFormattedString(context, getText());
    }

    public Bitmap getImage(Context context) {
        return BitmapFactory.decodeFile(new File(context.getCacheDir(), this.getImageFileName()).getAbsolutePath());
    }

    public Bitmap getSmallImage(Context context) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        return BitmapFactory.decodeFile(new File(context.getCacheDir(), this.getImageFileName()).getAbsolutePath(), options);
    }

    public boolean isImageFileExists(Context context) {
        if (context == null) return false;
        //File f = new File(context.getFilesDir(), this.getImageFileName());
        File f = new File(context.getCacheDir(), this.getImageFileName());
        return f.exists();
    }

    @Override
    public String toString() {
        //
        return this.getTitle();
    }

    public String getImageFileName() {
        return this.getCode() + AppManager.EXT_CARDS_IMAGES;
    }

    public String getFactionImageResName() {
        String lowCaseFaction = this.getFactionCode().toLowerCase();
        if (lowCaseFaction.startsWith(Faction.FACTION_NEUTRAL)) {
            lowCaseFaction = this.getSideCode() + "_" + lowCaseFaction;
        }
        lowCaseFaction = lowCaseFaction.replace("-", "_");
        lowCaseFaction = lowCaseFaction.replace(" ", "_");
        return lowCaseFaction;
    }

    public int getFactionImageRes(Context context) {
        if (getFactionCode().startsWith(Faction.FACTION_NEUTRAL)) return R.drawable.neutral;
        return context.getResources().getIdentifier(getFactionImageResName(), "drawable", context.getPackageName());
    }

    public int getMWLInfluence() {
        return this.mostWantedInfluence;
    }

    public boolean isMostWanted() {
        return this.mostWantedInfluence > 0;
    }

    /**
     * Gets ice/icebreaker main subtype.
     *
     * @return The main subtype of an ice or icebreaker (if detected) card, otherwise an empty string.
     */
    public String getIceOrIcebreakerSubtype() {
        switch (typeCode) {
            case Type.ICE:
                return getSubtypeArray()[0];
            case Type.PROGRAM:
                String[] subtypes = getSubtypeArray();
                // FIXME: Can't detect icebreakers on non-english cards.
                return (subtypes.length > 1 && subtypes[0].equals("Icebreaker")) ? subtypes[1] : "";
            default:
                return "";
        }
    }

    @Override
    public boolean equals(Object o) {
        return ((Card) o).getCode().equals(this.getCode());
    }

    public boolean isResource() {
        return getTypeCode().contains(Type.RESOURCE);
    }

    public boolean isVirtual() {
        return getSubtype().contains("Virtual");
    }

    public boolean isNeutral() {
        return getFactionCode().startsWith(Faction.FACTION_NEUTRAL);
    }

    public boolean isAgenda() {
        return getTypeCode().equals(Type.AGENDA);
    }

    public boolean isIdentity() {
        return getTypeCode().equals(Type.IDENTITY);
    }

    public boolean isJinteki() {
        return getFactionCode().equals(Faction.FACTION_JINTEKI);
    }

    public void setIsUnknown() {
        isUnknown = true;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setBaseLink(String baseLink) {
        this.baseLink = baseLink;
    }

    public void setFactionCode(String factionCode) {
        this.factionCode = factionCode;
    }

    public void setFactionCost(int factionCost) {
        this.factionCost = factionCost;
    }

    public void setFlavor(String flavor) {
        this.flavor = flavor;
    }

    public void setIllustrator(String illustrator) {
        this.illustrator = illustrator;
    }

    public void setInfluenceLimit(String influenceLimit) {
        this.influenceLimit = influenceLimit;
    }

    public void setMinimumDeckSize(String minimumDeckSize) {
        this.minimumDeckSize = minimumDeckSize;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public void setDeckLimit(int limit) {
        this.deckLimit = limit;
    }

    public void setSetCode(String setCode) {
        this.setCode = setCode;
    }

    public void setSideCode(String sideCode) {
        this.sideCode = sideCode;
    }

    public void setAdvancementCost(int advancementCost) {
        this.advancementCost = advancementCost;
    }

    public void setAgendaPoints(int agendaPoints) {
        this.agendaPoints = agendaPoints;
    }

    public void setMemoryUnits(int memoryUnits) {
        this.memoryUnits = memoryUnits;
    }

    public void setTrash(int trash) {
        this.trash = trash;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public void setImageSrc(URL imageSrc) {
        this.imageSrc = imageSrc;
    }

    public void setMostWantedInfluence(int mostWantedInfluence) {
        this.mostWantedInfluence = mostWantedInfluence;
    }

    public boolean isUnknown() {
        return isUnknown;
    }

    public void setUniqueness(boolean uniqueness) {
        this.uniqueness = uniqueness;
    }

    public int getDeckLimit() {
        return deckLimit;
    }

    public static class Faction {
        public static final String FACTION_NEUTRAL = "neutral-";
        public static final String FACTION_SHAPER = "shaper";
        public static final String FACTION_CRIMINAL = "criminal";
        public static final String FACTION_WEYLAND_CONSORTIUM = "weyland-consortium";
        public static final String FACTION_ANARCH = "anarch";
        public static final String FACTION_HAAS_BIOROID = "haas-bioroid";
        public static final String FACTION_JINTEKI = "jinteki";
        public static final String FACTION_NBN = "nbn";
    }

    public static class Side {
        public static final String SIDE_RUNNER = "runner";
        public static final String SIDE_CORPORATION = "corp";
    }

    public static class Type {
        public static final String AGENDA = "agenda";
        public static final String ASSET = "asset";
        public static final String EVENT = "event";
        public static final String ICE = "ice";
        public static final String IDENTITY = "identity";
        public static final String HARDWARE = "hardware";
        public static final String OPERATION = "operation";
        public static final String PROGRAM = "program";
        public static final String RESOURCE = "resource";
        public static final String UPGRADE = "upgrade";
    }

    public static class SubTypeCode {
        public static final String ALLIANCE = "alliance";
    }

    public static class SpecialCards {
        public static final String CARD_THE_PROCESSOR = "03029";
        public static final String CARD_ANDROMEDA = "02083";
        public static final String CARD_CUSTOM_BIOTICS_ENGINEERED_FOR_SUCCESS = "03002";
        public static final String APEX = "09029";
    }


}
