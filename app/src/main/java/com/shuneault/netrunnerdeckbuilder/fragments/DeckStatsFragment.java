package com.shuneault.netrunnerdeckbuilder.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.shuneault.netrunnerdeckbuilder.DeckActivity;
import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.helper.AppManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Deck stats viewer fragment.
 */
public class DeckStatsFragment extends Fragment {

    private View mainView;

    private Deck mDeck;

    private List<String> mBarLabels;
    private BarDataSet mBarCostSet;
    private BarDataSet mBarStrengthSet;

    private BarChart mBarChart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // The View
        mainView = inflater.inflate(R.layout.fragment_deck_stats, container, false);

        // Fetch the deck
        mDeck = AppManager.getInstance().getDeck(getArguments().getLong(DeckActivity.ARGUMENT_DECK_ID));

        // Set up data for graphs
        setUpGraph();
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
    private void setUpGraph() {

        mBarLabels = new ArrayList<>();

        mBarChart = (BarChart) mainView.findViewById(R.id.barChart);

        ArrayList<BarDataSet> sets = new ArrayList<>(2);

        mBarCostSet = new BarDataSet(new ArrayList<BarEntry>(), getString(R.string.stats_cost));
        mBarCostSet.setColor(getResources().getColor(R.color.graph_bar_cost));
        mBarCostSet.setDrawValues(false);
        sets.add(mBarCostSet);

        mBarStrengthSet = new BarDataSet(new ArrayList<BarEntry>(), getString(R.string.stats_strength));
        mBarStrengthSet.setColor(getResources().getColor(R.color.graph_bar_strength));
        mBarStrengthSet.setDrawValues(false);
        sets.add(mBarStrengthSet);

        mBarChart.setData(new BarData(mBarLabels, sets));

        mBarChart.getAxisRight().setEnabled(false);
        mBarChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        mBarChart.setDescription("");
        mBarChart.setTouchEnabled(false);

    }

    /**
     * (Re-)populates the graph data sources from the deck.
     */
    private void calculateDataSets() {

        ArrayList<Card> cards = mDeck.getCards();
        Integer[] cardCosts = new Integer[cards.size()];

        // Calculate bounds, parse costs
        int maxCost = 0;
        int maxStrength = 0;
        int i = 0;

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

            // Increment count for this card's cost, if it has one
            if (cardCosts[i] != null)
                barCosts[cardCosts[i]] += mDeck.getCardCount(c);

            // Increment count for this card's strength, if applicable
            if (c.getTypeCode().equals(Card.Type.ICE)
                || c.getTypeCode().equals(Card.Type.PROGRAM))
                barStrengths[c.getStrength()] += mDeck.getCardCount(c);

            ++i;
        }


        // Generate graph data
        mBarCostSet.clear();
        mBarStrengthSet.clear();
        for (i = 0; i < barEntries; ++i) {
            mBarCostSet.addEntry(new BarEntry(barCosts[i], i));
            mBarStrengthSet.addEntry(new BarEntry(barStrengths[i], i));
        }

        mBarChart.getAxisLeft().setAxisMaxValue(mBarCostSet.getYMax());
        mBarChart.fitScreen();
        mBarChart.invalidate();

    }

}
