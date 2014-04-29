package com.shuneault.netrunnerdeckbuilder.game;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.helper.AppManager;

public class Card {
	
	public static final String NAME_LAST_MODIFIED = "last-modified";
	public static final String NAME_CODE = "code";
	public static final String NAME_TITLE = "title";
	public static final String NAME_TYPE = "type";
	public static final String NAME_TYPE_CODE = "type_code";
	public static final String NAME_SUBTYPE = "subtype";
	public static final String NAME_SYBTYPE_CODE = "subtype_code";
	public static final String NAME_TEXT = "text";
	public static final String NAME_BASELINK = "baselink";
	public static final String NAME_FACTION = "faction";
	public static final String NAME_FACTION_CODE = "faction_code";
	public static final String NAME_FACTION_COST = "factioncost";
	public static final String NAME_FLAVOR = "flavor";
	public static final String NAME_ILLUSTRATOR = "illustrator";
	public static final String NAME_INFLUENCE_LIMIT = "influencelimit";
	public static final String NAME_MINIMUM_DECK_SIZE = "minimumdecksize";
	public static final String NAME_NUMBER = "number";
	public static final String NAME_QUANTITY = "quantity";
	public static final String NAME_SET_NAME = "setname";
	public static final String NAME_SET_CODE = "set_code";
	public static final String NAME_SIDE = "side";
	public static final String NAME_SIDE_CODE = "side_code";
	public static final String NAME_UNIQUENESS = "uniqueness";
	public static final String NAME_URL = "url";
	public static final String NAME_IMAGE_SRC = "imagesrc";
	public static final String NAME_AGENDA_POINTS = "agendapoints";
	
	private Date lastModified;
	private String code;
	private String title;
	private String type;
	private String typeCode;
	private String subtype;
	private String text;
	private String baselink;
	private String faction;
	private String factionCode;
	private String factionCost;
	private String flavor;
	private String illustrator;
	private String influenceLimit;
	private String minimumDeckSize;
	private String number;
	private String quantity;
	private String setName;
	private String setCode;
	private String side;
	private String sideCode;
	private int agendaPoints;
	private boolean uniqueness;
	private URL url;
	private URL imagesrc;
	
	public Card(JSONObject json) {

		try {
			//this.lastModified = DateFormat.getDateTimeInstance().parse(json.getString(NAME_LAST_MODIFIED));
			this.lastModified = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(json.getString(NAME_LAST_MODIFIED));
			//this.lastModified.setTime(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(json.getString(NAME_LAST_MODIFIED)));
			this.code = json.optString(NAME_CODE);
			this.title = json.optString(NAME_TITLE);
			this.type = json.optString(NAME_TYPE);
			this.typeCode = json.optString(NAME_TYPE_CODE);
			this.subtype = json.optString(NAME_SUBTYPE);
			this.text = json.optString(NAME_TEXT);
			this.baselink = json.optString(NAME_BASELINK);
			this.faction = json.optString(NAME_FACTION);
			this.factionCode = json.optString(NAME_FACTION_CODE);
			this.factionCost = json.optString(NAME_FACTION_COST);
			this.flavor = json.optString(NAME_FLAVOR);
			this.illustrator = json.optString(NAME_ILLUSTRATOR);
			this.influenceLimit = json.optString(NAME_INFLUENCE_LIMIT);
			this.minimumDeckSize = json.optString(NAME_MINIMUM_DECK_SIZE);
			this.number = json.optString(NAME_NUMBER);
			this.quantity = json.optString(NAME_QUANTITY);
			this.setName = json.optString(NAME_SET_NAME);
			this.setCode = json.optString(NAME_SET_CODE);
			this.uniqueness = Boolean.getBoolean(json.optString(NAME_UNIQUENESS));
			this.url = new URL(json.optString(NAME_URL));
			this.imagesrc = new URL(NetRunnerBD.BASE_URL + json.optString(NAME_IMAGE_SRC));
			this.side = json.optString(NAME_SIDE);
			this.sideCode = json.optString(NAME_SIDE_CODE);
			this.agendaPoints = json.optInt(NAME_AGENDA_POINTS, 0);
		} catch (ParseException e) {
			// 
			e.printStackTrace();
		} catch (JSONException e) {
			// 
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// 
			e.printStackTrace();
		}
	}
	

	public Date getLastModified() {
		return lastModified;
	}
	
	public String getCode() {
		return code;
	}
	public String getTitle() {
		return title;
	}
	public String getType() {
		return type;
	}
	public String getTypeCode() {
		return typeCode;
	}
	public String getSubtype() {
		return subtype;
	}
	public String getText() {
		return text;
	}
	public String getBaselink() {
		return baselink;
	}
	public String getFaction() {
		return faction;
	}
	public String getFactionCode() {
		return factionCode;
	}
	public int getFactionCost() {
		try {
			return Integer.parseInt(factionCost);
		} catch (Exception e) {
			return 0;
		}
	}
	public String getFlavor() {
		return flavor;
	}
	public String getIllustrator() {
		return illustrator;
	}
	public int getInfluenceLimit() {
		if (influenceLimit.equals(""))
			return 0;
		else
			return Integer.parseInt(influenceLimit);
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
	public String getSetName() {
		return setName;
	}
	public String getSetCode() {
		return setCode;
	}
	public String getSide() {
		return side;
	}
	public String getSideCode() {
		return sideCode;
	}
	public boolean isUniqueness() {
		return uniqueness;
	}
	public URL getUrl() {
		return url;
	}
	public URL getImagesrc() {
		return imagesrc;
	}
	
	public Bitmap getImage(Context context) {
//		try {
			//return BitmapFactory.decodeStream(context.openFileInput(this.getImageFileName()));
			return BitmapFactory.decodeFile(new File(context.getCacheDir(), this.getImageFileName()).getAbsolutePath());
//		} catch (FileNotFoundException e) {
//			Log.i(AppManager.LOGCAT, "Card " + this.getTitle() + " image: File Not Found");
//		}
//		return null;
	}
	
	public boolean isImageFileExists(Context context) {
		if (context == null) return false;
		//File f = new File(context.getFilesDir(), this.getImageFileName());
		File f = new File(context.getCacheDir(), this.getImageFileName());
		return f.exists();
	}
	
	public int getAgendaPoints() {
		return agendaPoints;
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
		String lowCaseFaction = this.getFaction().toLowerCase();
		if (lowCaseFaction.equals(Faction.FACTION_NEUTRAL)) {
			lowCaseFaction = this.getSideCode() + "_" + lowCaseFaction;
		}
		lowCaseFaction = lowCaseFaction.replace("-", "_");
		lowCaseFaction = lowCaseFaction.replace(" ", "_");
		return lowCaseFaction;
	}
	
	public int getFactionImageRes(Context context) {
		if (getFactionCode().equals(Faction.FACTION_NEUTRAL)) return R.drawable.neutral;
		return context.getResources().getIdentifier(getFactionImageResName(), "drawable", context.getPackageName());
	}
	
	@Override
	public boolean equals(Object o) {
		return ((Card) o).getCode().equals(this.getCode());
	}
	
	public static class Faction {
		public static final String FACTION_NEUTRAL = "neutral";
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
		public static final String IDENTITY = "identity";
	}
	
	public static class SetName {
		public static final String ALTERNATES = "Alternates";
		public static final String CORE_SET = "Core Set";
		public static final String SPECIAL = "Special";
	}
	
	

}
