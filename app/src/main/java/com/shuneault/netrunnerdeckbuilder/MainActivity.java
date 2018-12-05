package com.shuneault.netrunnerdeckbuilder;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.shuneault.netrunnerdeckbuilder.db.CardRepository;
import com.shuneault.netrunnerdeckbuilder.db.DatabaseHelper;
import com.shuneault.netrunnerdeckbuilder.fragments.ListDecksFragment;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.game.Format;
import com.shuneault.netrunnerdeckbuilder.helper.AppManager;
import com.shuneault.netrunnerdeckbuilder.interfaces.OnDeckChangedListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;

public class MainActivity extends AppCompatActivity implements OnDeckChangedListener, ListDecksFragment.OnListDecksFragmentListener {

    // Request Codes for activity launch
    public static final int REQUEST_NEW_IDENTITY = 1;
    public static final int REQUEST_SETTINGS = 3;

    // EXTRAS
    public static final String EXTRA_DECK_ID = "com.shuneault.netrunnerdeckbuilder.EXTRA_DECK_ID";

    private FloatingActionButton fabButton;

    // Flags
    private int mScrollDirection = 0;

    // Animations
    private Animation slideDown;
    private Animation slideUp;

    // Database
    DatabaseHelper mDb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_main);

        // show action overflow regardless of hardware menu key
        try {
            ViewConfiguration vConfig = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class
                    .getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(vConfig, false);
            }
        } catch (Exception ex) {
            // Ignore
        }
        // GUI
        fabButton = (FloatingActionButton) findViewById(R.id.fabButton);
        ViewPager mViewPager = (ViewPager) findViewById(R.id.viewPager);
        TabLayout mTabLayout = (TabLayout) findViewById(R.id.tabLayout);

        // Database
        mDb = AppManager.getInstance().getDatabase();

        // Setup the ViewPager
        mViewPager.setAdapter(new DecksFragmentPager(getSupportFragmentManager()));
        mTabLayout.setupWithViewPager(mViewPager);

        // Set up the FloatingActionButton
        fabButton.setOnClickListener(v -> {
            switch (mViewPager.getCurrentItem()) {
                case 0:
                    startChooseIdentityActivity(Card.Side.SIDE_RUNNER);
                    break;
                case 1:
                    startChooseIdentityActivity(Card.Side.SIDE_CORPORATION);
                    break;
            }
        });

        // load show/hide animations for fab
        slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);

        // Action bar
        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setTitle(R.string.title_activity_main);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;

        switch (requestCode) {
            case REQUEST_NEW_IDENTITY:
                // Get the chosen identity
                String identityCardCode = data.getStringExtra(ChooseIdentityActivity.EXTRA_IDENTITY_CODE);
                AppManager appManager = AppManager.getInstance();
                CardRepository repo = appManager.getCardRepository();
                Card card = repo.getCard(identityCardCode);

                // Create a new deck
                //todo: get default format
                Format format = null;
                Deck mDeck = new Deck(card, repo.getGlobalCardPool(), format);
                appManager.getAllDecks().add(mDeck);

                // Save the new deck in the database
                mDb.createDeck(mDeck);

                // Start the new deck activity
                startDeckActivity(mDeck.getRowId());
                break;

            case REQUEST_SETTINGS:

                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                Intent browseIntent = new Intent(this, BrowseActivity.class);
                startActivity(browseIntent);

                break;
//                // Get the set names
//                final ArrayList<String> setNames = new ArrayList<String>();
//                for (Pack pack : AppManager.getInstance().getAllPacks()) {
//                    setNames.add(pack.getName() + " (" + pack.getSize() + ")");
//                }
//                CharSequence[] cs = setNames.toArray(new CharSequence[setNames.size()]);
//                // Display the dialog
//                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//                builder.setTitle(R.string.view_cards);
//                builder.setItems(cs, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        // Launch the full screen image viewer activity
//                        Intent intentFullScreen = new Intent(MainActivity.this, ViewCardsAsGridActivity.class);
//                        intentFullScreen.putExtra(ViewCardsAsGridActivity.EXTRA_SET_NAME, AppManager.getInstance().getAllPacks().get(which).getCode());
//                        startActivity(intentFullScreen);
//                    }
//                });
//                builder.show();
//                break;
            case R.id.mnuOptions:
                startActivityForResult(new Intent(this, SettingsActivity.class), REQUEST_SETTINGS);
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    private void startChooseIdentityActivity(String side) {
        if (!side.equals(Card.Side.SIDE_CORPORATION) && !side.equals(Card.Side.SIDE_RUNNER)) return;

        Intent intent = new Intent(MainActivity.this, ChooseIdentityActivity.class);
        intent.putExtra(ChooseIdentityActivity.EXTRA_SIDE_CODE, side);
        startActivityForResult(intent, REQUEST_NEW_IDENTITY);
    }

    private void startDeckActivity(Long rowId) {
        Intent intent = new Intent(MainActivity.this, DeckActivity.class);
        intent.putExtra(DeckActivity.ARGUMENT_DECK_ID, rowId);
        startActivity(intent);
    }

    @Override
    public void onDeckCardsChanged() {

    }

    @Override
    public void onDeckIdentityChanged(Card newIdentity) {

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

    @Override
    public void OnScrollListener(RecyclerView recyclerView, int dx, int dy) {
        if (dy > 0 && mScrollDirection <= 0) { // Scroll Down
            // hide fab
            fabButton.startAnimation(slideDown);
            fabButton.setVisibility(View.INVISIBLE);
            mScrollDirection = dy;
        } else if (dy < 0 && mScrollDirection >= 0) { // Scroll Up
            // show fab
            fabButton.setVisibility(View.VISIBLE);
            fabButton.startAnimation(slideUp);
            mScrollDirection = dy;
        } else {
            // Same direction
        }
    }

    private class DecksFragmentPager extends FragmentPagerAdapter {

        public DecksFragmentPager(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getResources().getString(R.string.runner);
                case 1:
                    return getResources().getString(R.string.corp);
            }
            return "";
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return ListDecksFragment.newInstance(Card.Side.SIDE_RUNNER);
                case 1:
                    return ListDecksFragment.newInstance(Card.Side.SIDE_CORPORATION);
                default:
                    return ListDecksFragment.newInstance(Card.Side.SIDE_RUNNER);
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }


}
