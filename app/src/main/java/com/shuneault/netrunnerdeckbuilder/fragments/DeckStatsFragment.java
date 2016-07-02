package com.shuneault.netrunnerdeckbuilder.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.PercentFormatter;
import com.github.mikephil.charting.utils.ValueFormatter;
import com.shuneault.netrunnerdeckbuilder.DeckActivity;
import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.SettingsActivity;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.helper.AppManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Deck stats viewer fragment.
 */
public class DeckStatsFragment extends Fragment {

    private View mainView;

    private Deck mDeck;
    private boolean mTryParseSubtypes;

    private BarChart mBarChart;
    private List<String> mBarLabels;
    private BarDataSet mBarCostSet;
    private BarDataSet mBarStrengthSet;
    private List<BarDataSet> mBarSets;
    private BarMarkerView mBarMarkerView;

    private PieChart mTypeChart;
    private List<String> mTypeLabels;
    private PieDataSet mTypeDataSet;

    private PieChart mIceSubtypeChart;
    private List<String> mIceSubtypeLabels;
    private PieDataSet mIceSubtypeDataSet;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // The View
        mainView = inflater.inflate(R.layout.fragment_deck_stats, container, false);

        // Fetch the deck
        mDeck = AppManager.getInstance().getDeck(getArguments().getLong(DeckActivity.ARGUMENT_DECK_ID));

        // Check the language to see if we should even bother trying to parse subtypes
        String lang = PreferenceManager
                .getDefaultSharedPreferences(getActivity().getApplicationContext())
                .getString(SettingsActivity.KEY_PREF_LANGUAGE, "en");
        if (lang != null) {
            mTryParseSubtypes = lang.equals("en");
        }

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
        mBarCostSet = makeBarDataSet(R.string.stats_cost, R.color.stats_graph_bar_cost);
        mBarSets.add(mBarCostSet);

        mBarStrengthSet = makeBarDataSet(R.string.stats_strength, R.color.stats_graph_bar_strength);
        if (mTryParseSubtypes) {
            mBarSets.add(mBarStrengthSet);
        }
        else {
            // If not showing strength, change bar chart title
            ((TextView)mainView.findViewById(R.id.barChartTitle))
                    .setText(getString(R.string.stats_bar_chart_cost_title));
        }

        mBarChart.getAxisRight().setEnabled(false);
        mBarChart.getAxisLeft().setValueFormatter(new IntegerValueFormatter());
        mBarChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        mBarChart.setDescription("");
        mBarMarkerView = new BarMarkerView(getActivity());
        mBarChart.setMarkerView(mBarMarkerView);
        mBarChart.setPinchZoom(false);
        mBarChart.setDoubleTapToZoomEnabled(false);


        // Card type pie chart
        mTypeChart = (PieChart) mainView.findViewById(R.id.typeChart);

        mTypeLabels = new ArrayList<>();
        mTypeDataSet = new PieDataSet(new ArrayList<Entry>(), "");
        mTypeDataSet.setValueFormatter(new PercentFormatter());
        mTypeDataSet.setValueTextSize(10);

        setUpPieChart(mTypeChart);

        // Ice/breaker subtype pie chart
        mIceSubtypeChart = (PieChart) mainView.findViewById(R.id.iceSubtypeChart);

        if (!mTryParseSubtypes) {
            mainView.findViewById(R.id.iceSubtypeChartSection).setVisibility(View.GONE);
        }

        String iceType = mDeck.getSide().equals(Card.Side.SIDE_CORPORATION) ? "Ice" : "Icebreaker";
        ((TextView)mainView.findViewById(R.id.iceSubtypeChartTitle)).setText(
                String.format(getString(R.string.stats_ice_subtype_chart), iceType));

        mIceSubtypeLabels = new ArrayList<>();
        mIceSubtypeDataSet = new PieDataSet(new ArrayList<Entry>(), "");
        mIceSubtypeDataSet.setValueFormatter(new PercentFormatter());
        mIceSubtypeDataSet.setValueTextSize(10);

        setUpPieChart(mIceSubtypeChart);
        mIceSubtypeDataSet.setColors(ColorTemplate.JOYFUL_COLORS);

    }

    private BarDataSet makeBarDataSet(int nameRes, int colorRes) {
        BarDataSet result = new BarDataSet(new ArrayList<BarEntry>(), getString(nameRes));
        result.setColor(getResources().getColor(colorRes));
        result.setDrawValues(false);
        result.setHighLightColor(Color.WHITE);
        result.setHighLightAlpha(70);
        return result;
    }

    private void setUpPieChart(PieChart chart) {
        chart.setUsePercentValues(true);
        chart.setDescription("");
        chart.getLegend().setEnabled(false);
        chart.setRotationEnabled(false);
        chart.setHoleRadius(40);
        chart.setTransparentCircleRadius(45);
        chart.setOnChartValueSelectedListener(new PieSelectionListener(chart));
    }

    /**
     * (Re-)populates the graph data sources from the deck.
     */
    private void calculateDataSets() {

        ArrayList<Card> cards = mDeck.getCards();
        Integer[] cardCosts = new Integer[cards.size()];

        ArrayList<TypeDataEntry> typeData = new ArrayList<>();
        HashMap<String, TypeDataEntry> typeDataMap = new HashMap<>();

        ArrayList<TypeDataEntry> iceSubtypeData = new ArrayList<>();
        HashMap<String, TypeDataEntry> iceSubtypeDataMap = new HashMap<>();

        // Calculate bounds, parse costs
        int maxCost = 0;
        int maxStrength = 0;
        int i = 0;
        int nextTypeIndex = 0;
        int nextIceSubtypeIndex = 0;

        mTypeLabels.clear();
        mIceSubtypeLabels.clear();
        for (Card c : cards) {

            int count = mDeck.getCardCount(c);

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
            if ( !typeDataMap.keySet().contains(c.getTypeCode()) ) {
                TypeDataEntry type = new TypeDataEntry(nextTypeIndex++, c.getTypeCode());
                typeData.add(type);
                typeDataMap.put(c.getTypeCode(), type);
                mTypeLabels.add(c.getTypeCode());
            }

            // Increment count for this card's type
            typeDataMap.get(c.getTypeCode()).cardCount += count;

            // Check ice/breaker subtype
            String iceSubtype = c.getIceOrIcebreakerSubtype();
            if ( !iceSubtype.equals("")) {
                if (!iceSubtypeDataMap.keySet().contains(iceSubtype)) {
                    TypeDataEntry type = new TypeDataEntry(nextIceSubtypeIndex++, iceSubtype);
                    iceSubtypeData.add(type);
                    iceSubtypeDataMap.put(iceSubtype, type);
                    mIceSubtypeLabels.add(iceSubtype);
                }

                // Increment count for this card's subtype
                iceSubtypeDataMap.get(iceSubtype).cardCount += count;
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

            ++i;
        }

        // Generate graph data
        mBarCostSet.clear();
        mBarStrengthSet.clear();
        for (i = 0; i < barEntries; ++i) {
            mBarCostSet.addEntry(new BarEntry(barCosts[i], i, R.string.stats_cost_marker));
            mBarStrengthSet.addEntry(new BarEntry(barStrengths[i], i, R.string.stats_strength_marker));
        }
        mBarMarkerView.setDataSetSize(barEntries);

        mTypeDataSet.clear();
        int[] typeColors = new int[typeData.size()];
        i = 0;
        for (TypeDataEntry t : typeData)
        {
            mTypeDataSet.addEntryOrdered(new Entry(t.cardCount, t.xIndex));
            typeColors[t.xIndex] = getTypeCodeColor(t.code);
            ++i;
        }
        mTypeDataSet.setColors(typeColors);

        mIceSubtypeDataSet.clear();
        i = 0;
        for (TypeDataEntry t : iceSubtypeData)
        {
            mIceSubtypeDataSet.addEntryOrdered(new Entry(t.cardCount, t.xIndex));
            ++i;
        }

        // Set/clear chart data
        if (cards.size() == 0) {
            mBarChart.clear();
            mTypeChart.clear();
        } else {
            mBarChart.setData(new BarData(mBarLabels, mBarSets));
            mTypeChart.setData(new PieData(mTypeLabels, mTypeDataSet));
        }

        if (iceSubtypeData.size() == 0) {
            mIceSubtypeChart.clear();
        } else {
            mIceSubtypeChart.setData(new PieData(mIceSubtypeLabels, mIceSubtypeDataSet));
        }

        mBarChart.getAxisLeft().setAxisMaxValue(mBarCostSet.getYMax());
        mBarChart.fitScreen();
        mBarChart.invalidate();
        mTypeChart.invalidate();
        mIceSubtypeChart.invalidate();

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

    private class PieSelectionListener implements OnChartValueSelectedListener {

        PieChart mChart;

        public PieSelectionListener(PieChart chart) {
            mChart = chart;
        }

        @Override
        public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
            mChart.setCenterText((int) e.getVal() + " cards.");
        }

        @Override
        public void onNothingSelected() {
            mChart.setCenterText("");
        }

    }

    private class TypeDataEntry {

        public int cardCount;
        public int xIndex;
        public String code;

        public TypeDataEntry(int index, String typeCode)  {
            cardCount = 0;
            xIndex = index;
            code = typeCode;
        }
    }

    public class BarMarkerView extends MarkerView {

        int mDataSetSize = 1;
        int mDataIndex;
        TextView mText;

        public BarMarkerView(Context context)
        {
            super(context, R.layout.stats_marker_text);
            mText = (TextView) findViewById(R.id.statsBarChartMarkerText);
        }

        public void setDataSetSize(int size) {
            mDataSetSize = Math.max(1, size - 1);
        }

        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            mText.setText(String.format(getString((int)e.getData()), (int)e.getVal(), e.getXIndex()));
            mDataIndex = e.getXIndex();
        }

        @Override
        public int getXOffset() {
            return Math.round(-getWidth() * (mDataIndex / (float)mDataSetSize));
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
