package com.shuneault.netrunnerdeckbuilder.helper;

import android.content.Context;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;

import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.CardMWL;

import java.util.Arrays;
import java.util.HashMap;

public class TextFormatter {
    public static String FormatCardTitle(Card card) {
            String titleText;
            String strUnique = (card.isUniqueness() ? "• " : "");
            if (!card.getSubtype().isEmpty()) {
                titleText = strUnique + card.getTitle() + " (" + card.getSubtype() + ")";
            } else {
                titleText = strUnique + card.getTitle();
            }
            return titleText;
    }

    public static SpannableString FormatCardIcons(Context context, Card card) {
        SpannableString iconText;
        switch (card.getTypeCode()) {
            case Card.Type.EVENT:
            case Card.Type.HARDWARE:
            case Card.Type.OPERATION:
            case Card.Type.RESOURCE:
                iconText = TextFormatter.getFormattedString(context, card.getCost() + " [credit]");
                break;
            case Card.Type.AGENDA:
                iconText = TextFormatter.getFormattedString(context, card.getAdvancementCost() + " [credit]" + "  " + card.getAgendaPoints() + " [agenda]");
                break;
            case Card.Type.ASSET:
            case Card.Type.UPGRADE:
                iconText = TextFormatter.getFormattedString(context, card.getCost() + " [credit]" + "  " + card.getTrashCost() + " [trash]");
                break;
            case Card.Type.ICE:
                iconText = TextFormatter.getFormattedString(context, card.getCost() + " [credit]" + "  " + card.getStrength() + "[fist]");
                break;
            case Card.Type.PROGRAM:
                iconText = TextFormatter.getFormattedString(context, card.getCost() + " [credit]" + "  " + card.getMemoryUnits() + " [mu]" + "  " + card.getStrength() + "[fist]");
                break;
            default:
                iconText = SpannableString.valueOf("");
                break;
        }
        return iconText;
    }

    public static SpannableString getFormattedString(Context context, String text) {
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        map.put("[agenda]", R.drawable.agenda);
        map.put("[click]", R.drawable.click);
        map.put("[trash]", R.drawable.trash);
        map.put("[credit]", R.drawable.credits);
        map.put("[subroutine]", R.drawable.subroutine);
        map.put("[mu]", R.drawable.memory_unit);
        map.put("[recurring-credit]", R.drawable.credit_recurr);
        map.put("[link]", R.drawable.links);
        map.put("[fist]", R.drawable.fist);

        // replace all occurrences
        SpannableString span = new SpannableString(Html.fromHtml(text.replace("\n", "<br />")));
        for (String txt : map.keySet()) {
            int index = span.toString().toLowerCase().indexOf(txt);
            while (index >= 0) {
                span.setSpan(new ImageSpan(context, map.get(txt), ImageSpan.ALIGN_BOTTOM), index, index + txt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                index = span.toString().indexOf(txt, index + 1);
            }
        }

        return span;
    }

    public static String GetMWLIcon(CardMWL cardMWL) {
        String result = "";
        if (cardMWL.isRestricted()) {
            int unicorn = 0x1F984;
            result = new String(Character.toChars(unicorn));
        }

        if (cardMWL.isRemoved()) {
            int noEntry = 0x1F6AB;
            result = new String(Character.toChars(noEntry));
        }

        return result;
    }

    public static SpannableString GetInfluenceString(Context context, int numInfluence) {
        SpannableString influenceText = SpannableString.valueOf("");
        if (numInfluence > 0) {
            char[] chars = new char[numInfluence];
            Arrays.fill(chars, context.getResources().getString(R.string.influence_char).toCharArray()[0]);
            influenceText = new SpannableString(new String(chars));
        }
        return influenceText;
    }
}
