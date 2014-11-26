package com.shuneault.netrunnerdeckbuilder;



import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.shuneault.netrunnerdeckbuilder.db.DatabaseHelper;
import com.shuneault.netrunnerdeckbuilder.fragments.DeckBuildFragment;
import com.shuneault.netrunnerdeckbuilder.fragments.DeckCardsFragment;
import com.shuneault.netrunnerdeckbuilder.fragments.DeckHandFragment;
import com.shuneault.netrunnerdeckbuilder.fragments.DeckInfoFragment;
import com.shuneault.netrunnerdeckbuilder.fragments.DeckMyCardsFragment;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.helper.AppManager;
import com.shuneault.netrunnerdeckbuilder.helper.Sorter;
import com.shuneault.netrunnerdeckbuilder.interfaces.OnDeckChangedListener;
import com.shuneault.netrunnerdeckbuilder.octgn.OCTGN;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;

public class DeckActivity extends Activity implements OnDeckChangedListener {
	
	public static final String ARGUMENT_DECK_ID = "com.shuneault.netrunnerdeckbuilder.ARGUMENT_DECK_ID";
	public static final String ARGUMENT_SELECTED_TAB = "com.shuneault.netrunnerdeckbuilder.ARGUMENT_SELECTED_TAB";
	public static final String TAG = "DeckFragmentTag";
	
	private DeckInfoFragment fragDeckInfo;
	private DeckBuildFragment fragDeckBuild;
	private DeckCardsFragment fragDeckCards;
	private DeckMyCardsFragment fragDeckMyCards;
	private DeckHandFragment fragDeckHand;
	
	private OnDeckChangedListener mListener;

	private Deck mDeck;
	private ViewPager mViewPager;
	private LinearLayout layoutAgendas;
	private TextView lblInfoInfluence;
	private TextView lblInfoCards;
	private TextView lblInfoAgenda;
	private ActionBar mActionBar;
	private int mSelectedTab = 0;
	
	// Database
	private DatabaseHelper mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_deck);

        // GUI
        mViewPager = (ViewPager) findViewById(R.id.pager);
        layoutAgendas = (LinearLayout) findViewById(R.id.layoutAgendas);
        lblInfoInfluence = (TextView) findViewById(R.id.lblInfoInfluence);
        lblInfoCards = (TextView) findViewById(R.id.lblInfoCards);
        lblInfoAgenda = (TextView) findViewById(R.id.lblInfoAgenda);

        // ActionBar
        mActionBar = getActionBar();

        // Database
        mDb = new DatabaseHelper(this);

        // Get the params
        if (savedInstanceState != null) {
            mDeck = AppManager.getInstance().getDeck(savedInstanceState.getLong(ARGUMENT_DECK_ID));
            mSelectedTab = savedInstanceState.getInt(ARGUMENT_SELECTED_TAB);
        } else {
            mDeck = AppManager.getInstance().getDeck(getIntent().getExtras().getLong(ARGUMENT_DECK_ID));
            mSelectedTab = getIntent().getExtras().getInt(ARGUMENT_SELECTED_TAB);
        }

        // Change the title
        mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);
        mActionBar.setTitle(mDeck.getName());
        if (mDeck.getIdentity().getFactionCode().equals(Card.Faction.FACTION_NEUTRAL)) {
            mActionBar.setIcon(getResources().getDrawable(R.drawable.ic_launcher));
        } else {
            mActionBar.setIcon(mDeck.getIdentity().getFactionImageRes(this));
        }

        // Display the agendas (in the infobar) only if it is a CORP deck
        if (mDeck.getSide().equals(Card.Side.SIDE_CORPORATION)) {
            layoutAgendas.setVisibility(View.VISIBLE);
        } else {
            layoutAgendas.setVisibility(View.GONE);
        }

        // Set the page adapter
        mViewPager.setAdapter(new DeckTabsPagerAdapter(getFragmentManager()));
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                mActionBar.setSelectedNavigationItem(arg0);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                //

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
                //

            }
        });

        // Add the tabs
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {

            @Override
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

            }

            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

            }
        };
        // Add the necessary tabs
        if (mActionBar.getTabCount() > 0)
            mActionBar.removeAllTabs();

        mActionBar.addTab(mActionBar.newTab().setText(R.string.tab_info).setTabListener(tabListener), (mSelectedTab == mActionBar.getTabCount()));
        mActionBar.addTab(mActionBar.newTab().setText(R.string.tab_my_cards).setTabListener(tabListener), (mSelectedTab == mActionBar.getTabCount()));
        mActionBar.addTab(mActionBar.newTab().setText(R.string.tab_cards).setTabListener(tabListener), (mSelectedTab == mActionBar.getTabCount()));
        mActionBar.addTab(mActionBar.newTab().setText(R.string.tab_build).setTabListener(tabListener), (mSelectedTab == mActionBar.getTabCount()));
        mActionBar.addTab(mActionBar.newTab().setText(R.string.tab_hand).setTabListener(tabListener), (mSelectedTab == mActionBar.getTabCount()));

        // Update the infobar
        updateInfoBar();
    }
		
	private void updateInfoBar() {
		// Update the influence, card count and agendas
		lblInfoInfluence.setText(mDeck.getDeckInfluence() + "/" + mDeck.getInfluenceLimit());
		lblInfoCards.setText(mDeck.getDeckSize() + "/" + mDeck.getMinimumDeckSize());
		lblInfoAgenda.setText(mDeck.getDeckAgenda() + "/(" + mDeck.getDeckAgendaMinimum() + '-' + (mDeck.getDeckAgendaMinimum()+1) + ')');

		// Update the style Influence
		if (mDeck.isInfluenceOk())
			lblInfoInfluence.setTextAppearance(this, R.style.InfoBarGood);
		else
			lblInfoInfluence.setTextAppearance(this, R.style.InfoBarBad);
		// Update the style Agendas
		if (mDeck.isAgendaOk())
			lblInfoAgenda.setTextAppearance(this, R.style.InfoBarGood);
		else
			lblInfoAgenda.setTextAppearance(this, R.style.InfoBarBad);
		// Update the style Cards
		if (mDeck.isCardCountOk())
			lblInfoCards.setTextAppearance(this, R.style.InfoBarGood);
		else
			lblInfoCards.setTextAppearance(this, R.style.InfoBarBad);
	}
		
		
	public class DeckTabsPagerAdapter extends FragmentStatePagerAdapter {

		public DeckTabsPagerAdapter(FragmentManager fragmentManager) {
			super(fragmentManager);
		}

		@Override
		public Fragment getItem(int arg0) {
			Bundle bundle;
			switch (arg0) {
			case 0:
				fragDeckInfo = new DeckInfoFragment();
				bundle = new Bundle();
				bundle.putLong(ARGUMENT_DECK_ID, mDeck.getRowId());
				fragDeckInfo.setArguments(bundle);
				return fragDeckInfo;
			case 1:
				fragDeckMyCards = new DeckMyCardsFragment();
				bundle = new Bundle();
				bundle.putLong(ARGUMENT_DECK_ID, mDeck.getRowId());
				fragDeckMyCards.setArguments(bundle);
				return fragDeckMyCards;
			case 2:
				fragDeckCards = new DeckCardsFragment();
				bundle = new Bundle();
				bundle.putLong(ARGUMENT_DECK_ID, mDeck.getRowId());
				fragDeckCards.setArguments(bundle);
				return fragDeckCards;
			case 3:
				fragDeckBuild = new DeckBuildFragment();
				bundle = new Bundle();
				bundle.putLong(DeckBuildFragment.ARGUMENT_DECK_ID, mDeck.getRowId());
				fragDeckBuild.setArguments(bundle);
				return fragDeckBuild;
			case 4:
				fragDeckHand = new DeckHandFragment();
				bundle = new Bundle();
				bundle.putLong(DeckHandFragment.ARGUMENT_DECK_ID, mDeck.getRowId());
				fragDeckHand.setArguments(bundle);
				return fragDeckHand;
			default:
				return new DeckInfoFragment();
			}
		}

		@Override
		public int getCount() {
			// 
			return 5;
		}
		
		@Override
		public CharSequence getPageTitle(int position) {
			// 
			return "Tab " + position;
		}
		
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.deck, menu);
        return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.mnuDeleteDeck:
			// Alert
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.delete_deck);
			builder.setMessage(R.string.message_delete_deck);
			builder.setPositiveButton(R.string.ok, new AlertDialog.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					AppManager.getInstance().deleteDeck(mDeck);
					mListener.onDeckDeleted(mDeck);
					Toast.makeText(DeckActivity.this, R.string.message_deck_deleted, Toast.LENGTH_SHORT).show();
				}
			});
			builder.setNegativeButton(R.string.cancel, new AlertDialog.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
				}
			});
			builder.show();
			
			return true;
		case R.id.mnuCloneDeck:
			// Create a clone of the deck
			Deck newDeck = mDeck.clone(this);
			AppManager.getInstance().addDeck(newDeck);
			mListener.onDeckCloned(newDeck);
			Toast.makeText(this, R.string.toast_deck_cloned_successfuly, Toast.LENGTH_LONG).show();
			return true;
			
		case R.id.mnuViewFullScreen:
			Intent intent = new Intent(this, ViewDeckFullscreenActivity.class);
			intent.putExtra(ViewDeckFullscreenActivity.EXTRA_DECK_ID, mDeck.getRowId());
			startActivity(intent);
			return true;
			
		case R.id.mnuChangeIdentity:
			// Change the identity
			Intent intentChooseIdentity = new Intent(this, ChooseIdentityActivity.class);
			intentChooseIdentity.putExtra(ChooseIdentityActivity.EXTRA_SIDE_CODE, mDeck.getSide());
			intentChooseIdentity.putExtra(ChooseIdentityActivity.EXTRA_INITIAL_IDENTITY_CODE, mDeck.getIdentity().getCode());
			startActivityForResult(intentChooseIdentity, MainActivity.REQUEST_CHANGE_IDENTITY);
			return true;
			
		case R.id.mnuOCTGN:
			String filename = mDeck.getName() + ".o8d";
			// Save the file as OCTGN format
			try {
				FileOutputStream fileOut = this.openFileOutput(filename, Context.MODE_WORLD_READABLE);
				fileOut.write(OCTGN.fromDeck(mDeck).getBytes());
				fileOut.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			// Create the send intent
			Intent intentEmail = new Intent(Intent.ACTION_SEND);
			intentEmail.setType("text/plain");
			intentEmail.putExtra(Intent.EXTRA_SUBJECT, "NetRunner Deck - " + mDeck.getName());
			intentEmail.putExtra(Intent.EXTRA_TEXT, "\r\n\r\nDownload Android Netrunner DeckBuilder for free at https://play.google.com/store/apps/details?id=com.shuneault.netrunnerdeckbuilder");
			intentEmail.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(getFileStreamPath(filename)));
			startActivity(Intent.createChooser(intentEmail, getText(R.string.menu_share)));
			
			return true;

            case R.id.mnuPlainText:
                // Generate the text to send
                //      TITLE (Card Count)
                //      IDENTITY
                //      -- Card Category (Card Count)
                //      <Count> <Card>...
                //      <Count> <Card>...
                //      -- Card Category (Card Count)
                //      <Count> <Card>...
                //      <Count> <Card>...

                // Sort the cards
                ArrayList<Card> theCards = mDeck.getCards();
                Collections.sort(theCards, new Sorter.CardSorterByCardType());

                StringBuilder sb = new StringBuilder();
                // Title
                sb.append(String.format("%s (%s %s)\n", mDeck.getName(), mDeck.getDeckSize(), getResources().getString(R.string.cards)));
                // Identity
                sb.append(String.format("%s\n", mDeck.getIdentity().getTitle()));
                // Cards
                String lastType = "";
                for (Card card : theCards) {
                    if (!card.getType().equals(lastType)) {
                        lastType = card.getType();
                        sb.append(String.format("-- %s (%s %s)\n", lastType, mDeck.getCardCountByType(card.getType()), getResources().getString(R.string.cards)));
                    }
                    sb.append(String.format("%s %s\n", mDeck.getCardCount(card), card.getTitle()));
                }

                // Create the send intent
                Intent intentEmailPlain = new Intent(Intent.ACTION_SEND);
                intentEmailPlain.setType("text/plain");
                intentEmailPlain.putExtra(Intent.EXTRA_SUBJECT, "NetRunner Deck - " + mDeck.getName());
                intentEmailPlain.putExtra(Intent.EXTRA_TEXT, sb.toString() + "\n\nDownload Android Netrunner DeckBuilder for free at https://play.google.com/store/apps/details?id=com.shuneault.netrunnerdeckbuilder");
                startActivity(Intent.createChooser(intentEmailPlain, getText(R.string.menu_share)));

			
		default:
			return super.onOptionsItemSelected(item);
		}
		
	}

	// Allow the Fragment Deck Build to be updated
	@Override
	public void onDeckNameChanged(Deck deck, String name) {

	}

	@Override
	public void onDeckDeleted(Deck deck) {
		// 
		
	}

	@Override
	public void onDeckCardsChanged() {
		// Call the DeckBuildFragment update
		if (fragDeckBuild != null)
			fragDeckBuild.onDeckCardsChanged();
		if (fragDeckMyCards != null)
			fragDeckMyCards.onDeckCardsChanged();
		if (fragDeckCards != null)
			fragDeckCards.onDeckCardsChanged();
		
		// Update the infobar
		updateInfoBar();
	}

	@Override
	public void onDeckCloned(Deck deck) {
		// 
		
	}

	@Override
	public void onDeckIdentityChanged(Card newIdentity) {
	// Forward the event to sub fragments
		if (fragDeckInfo != null)
			fragDeckInfo.onDeckIdentityChanged(newIdentity);
		if (fragDeckBuild != null)
			fragDeckBuild.onDeckIdentityChanged(newIdentity);
		if (fragDeckCards != null)
			fragDeckCards.onDeckIdentityChanged(newIdentity);
		
		// Change the actionbar icon
		getActionBar().setIcon(newIdentity.getFactionImageRes(this));
		
		// Update the infobar
		updateInfoBar();
	}
 
	@Override
	public void onSettingsChanged() {
		// Forward the event to sub fragments
		if (fragDeckInfo != null)
			fragDeckInfo.onSettingsChanged();
		if (fragDeckBuild != null)
			fragDeckBuild.onSettingsChanged();
		if (fragDeckCards != null)
			fragDeckCards.onSettingsChanged();
		
	}

	@Override
	public void onPause() {
		super.onPause();
		
		// Save the deck
		mDb.saveDeck(mDeck);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(ARGUMENT_DECK_ID, mDeck.getRowId());
		outState.putInt(ARGUMENT_SELECTED_TAB, mSelectedTab);
	}
		
}
