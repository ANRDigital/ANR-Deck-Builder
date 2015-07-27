package com.shuneault.netrunnerdeckbuilder.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.PercentFormatter;
import com.github.mikephil.charting.utils.ValueFormatter;
import com.shuneault.netrunnerdeckbuilder.DeckActivity;
import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.helper.AppManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Deck stats viewer fragment.
 */
public class DeckStatsFragment extends Fragment {

    private View mainView;

    private Deck mDeck;

    private BarChart mBarChart;
    private List<String> mBarLabels;
    private BarDataSet mBarCostSet;
    private BarDataSet mBarStrengthSet;
    private List<BarDataSet> mBarSets;

    private PieChart mTypeChart;
    private List<String> mTypeLabels;
    private PieDataSet mTypeDataSet;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // The View
        mainView = inflater.inflate(R.layout.fragment_deck_stats, container, false);

        // Fetch the deck
        mDeck = AppManager.getInstance().getDeck(getArguments().getLong(DeckActivity.ARGUMENT_DECK_ID));

        // Set up data for graphs
        setUpGraphs();
        calculateDataSets();

        return mainView;
    }

    public void onDeckCardsChanged() {

        if (mBarChart != null) {
            calculateDataSets();
        }

    }

    /**
     * Performs graph view and data setup.
     */
    private void setUpGraphs() {

        // Cost/strength distribution bar chart
        mBarChart = (BarChart) mainView.findViewById(R.id.barChart);

        mBarLabels = new ArrayList<>();

        mBarSets = new ArrayList<>(2);

        mBarCostSet = new BarDataSet(new ArrayList<BarEntry>(), getString(R.string.stats_cost));
        mBarCostSet.setColor(getResources().getColor(R.color.stats_graph_bar_cost));
        mBarCostSet.setDrawValues(false);
        mBarCostSet.setHighLightColor(Color.WHITE);
        mBarCostSet.setHighLightAlpha(70);
        mBarSets.add(mBarCostSet);

        mBarStrengthSet = new BarDataSet(new ArrayList<BarEntry>(), getString(R.string.stats_strength));
        mBarStrengthSet.setColor(getResources().getColor(R.color.stats_graph_bar_strength));
        mBarStrengthSet.setDrawValues(false);
        mBarStrengthSet.setHighLightColor(Color.WHITE);
        mBarStrengthSet.setHighLightAlpha(70);
        mBarSets.add(mBarStrengthSet);

        mBarChart.getAxisRight().setEnabled(false);
        mBarChart.getAxisLeft().setValueFormatter(new IntegerValueFormatter());
        mBarChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        mBarChart.setDescription("");
        mBarChart.setMarkerView(new BarMarkerView(getActivity()));


        // Card type pie chart
        mTypeChart = (PieChart) mainView.findViewById(R.id.typeChart);

        mTypeLabels = new ArrayList<>();
        mTypeDataSet = new PieDataSet(new ArrayList<Entry>(), "");
        mTypeDataSet.setValueFormatter(new PercentFormatter());
        mTypeDataSet.setValueTextSize(10);

        mTypeChart.setUsePercentValues(true);
        mTypeChart.setDescription("");
        mTypeChart.setDrawHoleEnabled(false);
        mTypeChart.getLegend().setEnabled(false);
        mTypeChart.setTouchEnabled(false);

    }

    /**
     * (Re-)populates the graph data sources from the deck.
     */
    private void calculateDataSets() {

        ArrayList<Card> cards = mDeck.getCards();
        Integer[] cardCosts = new Integer[cards.size()];
        HashMap<String, TypeDataEntry> typeData = new HashMap<>();

        // Calculate bounds, parse costs
        int maxCost = 0;
        int maxStrength = 0;
        int i = 0;
        int nextTypeIndex = 0;

        mTypeLabels.clear();
        for (Card c : cards) {

            // Check cost
            try {
                int cost = Integer.parseInt(c.getCost());
                if (cost > maxCost)
                    maxCost = cost;
                cardCosts[i] = cost;
            } catch (NumberFormatException e) {
                // ignore non-integer costs
                cardCosts[i] = null;
            }

            // Check strength
            if (c.getStrength() > maxStrength)
                maxStrength = c.getStrength();


            // Check type
            if ( !typeData.keySet().contains(c.getTypeCode()) ) {
                typeData.put(c.getTypeCode(), new TypeDataEntry(nextTypeIndex++, c.getType()));
                mTypeLabels.add(c.getType());
            }

            ++i;
        }


        // Set up containers
        int barEntries = Math.max(maxCost, maxStrength) + 1;
        int[] barCosts = new int[barEntries];
        int[] barStrengths = new int[barEntries];

        mBarLabels.clear();
        for (i = 0; i < barEntries; ++i) {
            mBarLabels.add(i+"");
        }

        // Accumulate values
        i = 0;
        for (Card c : cards) {

            int count = mDeck.getCardCount(c);

            // Increment count for this card's cost, if it has one
            if (cardCosts[i] != null)
                barCosts[cardCosts[i]] += count;

            // Increment count for this card's strength, if applicable
            if (c.getTypeCode().equals(Card.Type.ICE)
                || (c.getTypeCode().equals(Card.Type.PROGRAM) && c.getSubtypeArray()[0].equals("Icebreaker")) )
                barStrengths[c.getStrength()] += count;

            // Increment count for this card's type
            typeData.get(c.getTypeCode()).cardCount += count;

            ++i;
        }

        // Generate graph data
        mBarCostSet.clear();
        mBarStrengthSet.clear();
        for (i = 0; i < barEntries; ++i) {
            mBarCostSet.addEntry(new BarEntry(barCosts[i], i, R.string.stats_cost_marker));
            mBarStrengthSet.addEntry(new BarEntry(barStrengths[i], i, R.string.stats_strength_marker));
        }

        mTypeDataSet.clear();
        int[] typeColors = new int[typeData.size()];
        i = 0;
        for (Map.Entry<String, TypeDataEntry> e : typeData.entrySet())
        {
            mTypeDataSet.addEntry(new Entry(e.getValue().cardCount, e.getValue().xIndex));
            typeColors[i] = getTypeCodeColor(e.getKey());
            ++i;
        }
        mTypeDataSet.setColors(typeColors);

        // Create/remove chart data
        if (cards.size() == 0) {
            mBarChart.setData(null);
            mTypeChart.setData(null);
        } else {
            mBarChart.setData(new BarData(mBarLabels, mBarSets));
            mTypeChart.setData(new PieData(mTypeLabels, mTypeDataSet));
        }

        mBarChart.getAxisLeft().setAxisMaxValue(mBarCostSet.getYMax());
        mBarChart.fitScreen();
        mBarChart.invalidate();
        mTypeChart.invalidate();

    }

    private int getTypeCodeColor(String typeCode) {
        switch (typeCode) {

            case Card.Type.ICE:
            case Card.Type.PROGRAM:
                return getResources().getColor(R.color.stats_graph_type_ice_program);

            case Card.Type.RESOURCE:
            case Card.Type.ASSET:
                return getResources().getColor(R.color.stats_graph_type_resource_asset);

            case Card.Type.EVENT:
            case Card.Type.OPERATION:
                return getResources().getColor(R.color.stats_graph_type_event_operation);

            case Card.Type.HARDWARE:
            case Card.Type.UPGRADE:
                return getResources().getColor(R.color.stats_graph_type_hardware_upgrade);

            case Card.Type.AGENDA:
                return getResources().getColor(R.color.stats_graph_type_agenda);

            default:
                return Color.BLACK;

        }
    }

    private class TypeDataEntry {

        public int cardCount;
        public int xIndex;
        public String name;

        public TypeDataEntry(int index, String typeName)  {
            cardCount = 0;
            xIndex = index;
            name = typeName;
        }
    }

    public class BarMarkerView extends MarkerView {

        TextView mText;

        public BarMarkerView(Context context)
        {
            super(context, R.layout.stats_marker_text);
            mText = (TextView) findViewById(R.id.statsBarChartMarkerText);
        }

        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            mText.setText(String.format(getString((int)e.getData()), (int)e.getVal(), e.getXIndex()));
        }

        @Override
        public int getXOffset() {
            return 0;
        }

        @Override
        public int getYOffset() {
            return 0;
        }
    }

    public class IntegerValueFormatter implements ValueFormatter {

        @Override
        public String getFormattedValue(float value) {
            int floored = (int) value;
            if (value != floored)
                return "";
            else
                return floored + "";
        }

    }

}
