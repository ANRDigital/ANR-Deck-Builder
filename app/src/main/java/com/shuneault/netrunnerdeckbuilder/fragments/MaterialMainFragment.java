package com.shuneault.netrunnerdeckbuilder.fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.shuneault.netrunnerdeckbuilder.ChooseIdentityActivity;
import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.ViewDeckFullscreenActivity;
import com.shuneault.netrunnerdeckbuilder.adapters.CardDeckAdapter;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.helper.AppManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class MaterialMainFragment extends Fragment {

    // Request for new activity
    public static final int REQUEST_NEW_IDENTITY = 1;

    // Shortcuts variables
    private ArrayList<Deck> mDecks;
    private OnDeckSelected mListener;

    private View theView;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private CardDeckAdapter mDeckAdapter;
    private FloatingActionsMenu fabAdd;
    private FloatingActionButton fabRunner;
    private FloatingActionButton fabCorp;
    private FloatingActionButton fabBrowseSets;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // GUI
        theView = inflater.inflate(R.layout.activity_material_main, container, false);
        mRecyclerView = (RecyclerView) theView.findViewById(R.id.recyclerView);
        fabAdd = (FloatingActionsMenu) theView.findViewById(R.id.fabAdd);
        fabRunner = (FloatingActionButton) theView.findViewById(R.id.fabCreateRunner);
        fabCorp = (FloatingActionButton) theView.findViewById(R.id.fabCreateCorp);
        fabBrowseSets = (FloatingActionButton) theView.findViewById(R.id.fabBrowseSets);

        // Some variables
        mDecks = AppManager.getInstance().getAllDecks();

        // Initialize the layout manager and adapter
        mLayoutManager = new LinearLayoutManager(getActivity());
        mDeckAdapter = new CardDeckAdapter(mDecks, new CardDeckAdapter.ViewHolder.IViewHolderClicks() {
            @Override
            public void onClick(int index) {
                Deck deck = mDecks.get(index);
                mListener.onDeckSelected(deck);
            }

            @Override
            public void onDeckStarred(int index, boolean isStarred) {

            }
        });

        // Initialize the RecyclerView
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mDeckAdapter);

        // OnClick
        fabCorp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ChooseIdentityActivity.class);
                intent.putExtra(ChooseIdentityActivity.EXTRA_SIDE_CODE, Card.Side.SIDE_CORPORATION);
                getActivity().startActivityForResult(intent, REQUEST_NEW_IDENTITY);
                fabAdd.collapse();
            }
        });

        // OnClick
        fabRunner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ChooseIdentityActivity.class);
                intent.putExtra(ChooseIdentityActivity.EXTRA_SIDE_CODE, Card.Side.SIDE_RUNNER);
                getActivity().startActivityForResult(intent, REQUEST_NEW_IDENTITY);
                fabAdd.collapse();
            }
        });

        fabBrowseSets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the set names
                final ArrayList<String> setNames = new ArrayList<String>();
                for ( String setName : AppManager.getInstance().getSetNames() ) {
                    setNames.add(setName + " (" + AppManager.getInstance().getCardsBySetName(setName).size() + ")");
                }
                CharSequence[] cs = setNames.toArray(new CharSequence[setNames.size()]);
                // Display the dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.view_cards);
                builder.setItems(cs, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Launch the full screen image viewer activity
                        Intent intent = new Intent(getActivity(), ViewDeckFullscreenActivity.class);
                        intent.putExtra(ViewDeckFullscreenActivity.EXTRA_SET_NAME, AppManager.getInstance().getSetNames().get(which));
                        startActivity(intent);
                    }
                });
                builder.show();
            }
        });

        initActionBar();

        // Return the view
        return theView;
    }

    private void initActionBar() {
        // Option menu
        setHasOptionsMenu(true);

        // Set the action bar
        ActionBar mActionBar = getActivity().getActionBar();
        mActionBar.setTitle(R.string.title_activity_main);
        //mActionBar.setCustomView(R.layout.action_bar_main_activity);
        //mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME);
        mActionBar.setDisplayHomeAsUpEnabled(false);
        mActionBar.setHomeButtonEnabled(false);
        mActionBar.setIcon(R.drawable.ic_launcher);
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_fragment, menu);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnDeckSelected) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnDeckSelected");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static interface OnDeckSelected {
        void onDeckSelected(Deck deck);
    }
}
