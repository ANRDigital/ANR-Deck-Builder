package com.shuneault.netrunnerdeckbuilder;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.AddFloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.shuneault.netrunnerdeckbuilder.adapters.CardDeckAdapter;
import com.shuneault.netrunnerdeckbuilder.db.DatabaseHelper;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.CardList;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.helper.AppManager;
import com.shuneault.netrunnerdeckbuilder.helper.CardDownloader;
import com.shuneault.netrunnerdeckbuilder.helper.CardDownloader.CardDownloaderListener;
import com.shuneault.netrunnerdeckbuilder.helper.CardImagesDownloader;
import com.shuneault.netrunnerdeckbuilder.helper.CardImagesDownloader.CardImagesDownloaderListener;
import com.shuneault.netrunnerdeckbuilder.helper.Sorter;
import com.shuneault.netrunnerdeckbuilder.interfaces.OnDeckChangedListener;

import org.json.JSONArray;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends ActionBarActivity implements OnDeckChangedListener {

    // Request Codes for activity launch
    public static final int REQUEST_NEW_IDENTITY = 1;
    public static final int REQUEST_SETTINGS = 3;

    // EXTRAS
    public static final String EXTRA_DECK_ID = "com.shuneault.netrunnerdeckbuilder.EXTRA_DECK_ID";

    // Database
    private DatabaseHelper mDb;

    private ArrayList<Deck> mDecks;

    // Load the deck on resume
    private Deck mDeck;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private CardDeckAdapter mDeckAdapter;
	private AddFloatingActionButton fabNewCorpDeck;
	private AddFloatingActionButton fabNewRunnerDeck;
	private FloatingActionsMenu fabMenu;

    // Flags
    private boolean bStarOnly = false;
    private int mScrollDirection = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_main);

	    // show action overflow regardless of hardware menu key
	    try
	    {
		    ViewConfiguration vConfig = ViewConfiguration.get(this);
		    Field menuKeyField = ViewConfiguration.class
				    .getDeclaredField("sHasPermanentMenuKey");
		    if (menuKeyField != null)
		    {
			    menuKeyField.setAccessible(true);
			    menuKeyField.setBoolean(vConfig, false);
		    }
	    }
	    catch (Exception ex)
	    {
		    // Ignore
	    }
        // GUI
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
	    fabNewCorpDeck = (AddFloatingActionButton) findViewById(R.id.action_new_corp);
	    fabNewRunnerDeck = (AddFloatingActionButton) findViewById(R.id.action_new_runner);
	    fabMenu = (FloatingActionsMenu) findViewById(R.id.multiple_actions);

	    // Some variables
        AppManager.getInstance().init(this);
        mDb = AppManager.getInstance().getDatabase();
        mDecks = AppManager.getInstance().getAllDecks();

        // Initialize the layout manager and adapter
        mLayoutManager = new LinearLayoutManager(this);
        mDeckAdapter = new CardDeckAdapter(mDecks, new CardDeckAdapter.ViewHolder.IViewHolderClicks() {
            @Override
            public void onClick(int index) {
                Deck deck = mDecks.get(index);
                loadDeckFragment(deck);
            }

            @Override
            public void onDeckStarred(int index, boolean isStarred) {
                Deck deck = mDecks.get(index);
                deck.setStarred(isStarred);
                mDb.updateDeck(deck);
                // Sort
                Collections.sort(mDecks, new Sorter.DeckSorter());
                mRecyclerView.getAdapter().notifyDataSetChanged();
            }
        });

        // Initialize the RecyclerView
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mDeckAdapter);

        // New Deck buttons
	    fabNewCorpDeck.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
			    if (fabMenu.isExpanded())
			    {
				    fabMenu.collapse();
			    }
			    Intent intent = new Intent(MainActivity.this, ChooseIdentityActivity.class);
			    intent.putExtra(ChooseIdentityActivity.EXTRA_SIDE_CODE, Card.Side.SIDE_CORPORATION);
			    startActivityForResult(intent, REQUEST_NEW_IDENTITY);
		    }
	    });
	    fabNewRunnerDeck.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
			    if (fabMenu.isExpanded())
			    {
				    fabMenu.collapse();
			    }
			    Intent intent = new Intent(MainActivity.this, ChooseIdentityActivity.class);
			    intent.putExtra(ChooseIdentityActivity.EXTRA_SIDE_CODE, Card.Side.SIDE_RUNNER);
			    startActivityForResult(intent, REQUEST_NEW_IDENTITY);
		    }
	    });

	    // load show/hide animations for fab
	    final Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
	    final Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);

        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && mScrollDirection <= 0) { // Scroll Down
	                // collapse fab if expanded when hiding
	                if (fabMenu.isExpanded())
	                {
		                fabMenu.collapse();
	                }
	                // hide fab
                    fabMenu.startAnimation(slideDown);
	                fabMenu.setVisibility(View.INVISIBLE);
                    mScrollDirection = dy;
                } else if (dy < 0 && mScrollDirection >= 0) { // Scroll Up
	                // show fab
	                fabMenu.setVisibility(View.VISIBLE);
                    fabMenu.startAnimation(slideUp);
                    mScrollDirection = dy;
                } else {
                    // Same direction
                }
            }
        });

        initActionBar();


        // Load the cards
        if (AppManager.getInstance().getAllCards().size() == 0) {
            File f = new File(getFilesDir(), AppManager.FILE_CARDS_JSON);
            // Use the local provided copy of the card since NetrunnerDB.com got shut down
            if (!f.exists()) {
                InputStream in = getResources().openRawResource(R.raw.cards);
                try {
                    copy(in, f);
                } catch (Exception e) {
                }
            }
            doLoadCards();

        }

        // Sort the list
        Collections.sort(mDecks, new Sorter.DeckSorter());

    }

    private void initActionBar() {
        // Set the action bar
        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setTitle(R.string.title_activity_main);
//            mActionBar.setLogo(R.drawable.ic_launcher);
//            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;

        switch (requestCode) {
            case REQUEST_NEW_IDENTITY:
                // Get the choosen identity
                Card card = AppManager.getInstance().getAllCards().getCard(data.getStringExtra(ChooseIdentityActivity.EXTRA_IDENTITY_CODE));

                // Create a new deck
                mDeck = new Deck("", card);
                AppManager.getInstance().getAllDecks().add(mDeck);

                // Save the new deck in the database
                mDb.createDeck(mDeck);

                // Start the new deck activity
                loadDeckFragment(mDeck);
                break;

            case REQUEST_SETTINGS:

                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Close the database connection
        mDb.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
	        case R.id.mnuViewSet:
		        // Get the set names
		        final ArrayList<String> setNames = new ArrayList<String>();
		        for (String setName : AppManager.getInstance().getSetNames()) {
			        setNames.add(setName + " (" + AppManager.getInstance().getCardsBySetName(setName).size() + ")");
		        }
		        CharSequence[] cs = setNames.toArray(new CharSequence[setNames.size()]);
		        // Display the dialog
		        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		        builder.setTitle(R.string.view_cards);
		        builder.setItems(cs, new DialogInterface.OnClickListener() {
			        @Override
			        public void onClick(DialogInterface dialog, int which) {
				        // Launch the full screen image viewer activity
				        Intent intent = new Intent(MainActivity.this, ViewDeckFullscreenActivity.class);
				        intent.putExtra(ViewDeckFullscreenActivity.EXTRA_SET_NAME, AppManager.getInstance().getSetNames().get(which));
				        startActivity(intent);
			        }
		        });
		        builder.show();
		        break;
            case R.id.mnuRefreshCards:
                doDownloadCards();
                break;
            case R.id.mnuOptions:
                startActivityForResult(new Intent(this, SettingsActivity.class), REQUEST_SETTINGS);
                break;
            case R.id.mnuAbout:
                PackageInfo pInfo;
                TextView txt = new TextView(this);
                try {
                    pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                    txt.setText(getString(R.string.about_text, pInfo.versionName));
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                }
                txt.setMovementMethod(LinkMovementMethod.getInstance());
                txt.setPadding(25, 25, 25, 25);
                AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                builder2.setTitle(R.string.menu_about);
                builder2.setView(txt);
                builder2.setPositiveButton(R.string.ok, null);
                builder2.show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDeckCardsChanged() {

    }

    @Override
    public void onDeckNameChanged(Deck deck, String name) {

    }

    @Override
    public void onDeckDeleted(Deck deck) {
        mDb.deleteDeck(deck);
    }

    @Override
    public void onDeckCloned(Deck deck) {
        // Load the deck on screen
        loadDeckFragment(deck);
    }

    @Override
    public void onSettingsChanged() {
    }

    @Override
    public void onDeckIdentityChanged(Card newIdentity) {


    }

    public void loadDeckFragment(Deck deck, int selectedTab) {
        // Start the DeckActivity activity
        Intent intent = new Intent(MainActivity.this, DeckActivity.class);
        intent.putExtra(DeckActivity.ARGUMENT_DECK_ID, deck.getRowId());
        intent.putExtra(DeckActivity.ARGUMENT_SELECTED_TAB, selectedTab);
        startActivity(intent);
    }

    public void loadDeckFragment(Deck deck) {
        loadDeckFragment(deck, 0);
    }

    public void doLoadCards() {
        // Cards downloaded, load them
        try {
            /* Load the card list
			 *
			 * - Create the card
			 * - Add the card to the array
			 * - Generate the faction list
			 * - Generate the side list
			 * - Generate the card set list
			 *
			 */
            JSONArray jsonFile = AppManager.getInstance().getJSONCardsFile(this);
            CardList arrCards = AppManager.getInstance().getAllCards();
            arrCards.clear();
            for (int i = 0; i < jsonFile.length(); i++) {
                // Create the card and add to the array
                //		Do not load cards from the Alternates set
                Card card = new Card(jsonFile.getJSONObject(i));
                if (!card.getSetName().equals(Card.SetName.ALTERNATES))
                    arrCards.add(card);
            }

            // Load the decks
            doLoadDecks();

        } catch (FileNotFoundException e) {
            doDownloadCards();
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doLoadDecks() {
        // Load the decks from the DB
        mDecks.clear();
        mDecks.addAll(mDb.getAllDecks(true));
    }

    private void doDownloadCards() {
        CardDownloader dl = new CardDownloader(this, new CardDownloaderListener() {

            ProgressDialog mDialog;

            @Override
            public void onTaskCompleted() {
                // Load the cards in the app
                doLoadCards();

                // Close the dialog
                mDialog.dismiss();

                // Ask if we want to download the images on if almost no images are downloaded
                if (AppManager.getInstance().getNumberImagesCached(MainActivity.this) < 20) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage(R.string.download_all_images_question_first_launch);
                    builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Download all images
                            CardImagesDownloader dnl = new CardImagesDownloader(MainActivity.this, new CardImagesDownloaderListener() {

                                @Override
                                public void onTaskCompleted() {

                                }

                                @Override
                                public void onImageDownloaded(Card card, int count, int max) {

                                }

                                @Override
                                public void onBeforeStartTask(Context context, int max) {

                                }
                            });
                            dnl.execute();
                        }
                    });
                    builder.setNegativeButton(android.R.string.no, null);
                    builder.create().show();
                }

            }

            @Override
            public void onBeforeStartTask(Context context) {
                // Display a progress dialog
                mDialog = new ProgressDialog(context);
                mDialog.setTitle(getResources().getString(R.string.downloading_cards));
                mDialog.setIndeterminate(true);
                mDialog.setCancelable(false);
                mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mDialog.setMessage(null);
                mDialog.show();
            }

            @Override
            public void onDownloadError() {
                // Display the error and cancel the ongoing dialog
                mDialog.dismiss();

                // If zero cards are available, exit the application
                if (AppManager.getInstance().getAllCards().size() <= 0) {
                    Toast.makeText(MainActivity.this, R.string.error_downloading_cards_quit, Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, R.string.error_downloading_cards, Toast.LENGTH_LONG).show();
                }
            }
        });
        dl.execute();
    }

    public void copy(InputStream in, File dst) throws IOException {
        //InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }
}
