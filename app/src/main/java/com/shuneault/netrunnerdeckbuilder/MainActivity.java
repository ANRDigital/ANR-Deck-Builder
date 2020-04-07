package com.shuneault.netrunnerdeckbuilder;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.shuneault.netrunnerdeckbuilder.ViewModel.MainActivityViewModel;
import com.shuneault.netrunnerdeckbuilder.fragments.ListDecksFragment;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.Deck;

import java.lang.reflect.Field;

import static org.koin.java.standalone.KoinJavaComponent.get;

public class MainActivity extends AppCompatActivity implements ListDecksFragment.OnListDecksFragmentListener {

    private MainActivityViewModel viewModel = get(MainActivityViewModel.class);

    // Request Codes for activity launch
    public static final int REQUEST_NEW_IDENTITY = 1;
    public static final int REQUEST_SETTINGS = 3;

    private FloatingActionButton fabButton;

    // Flags
    private int mScrollDirection = 0;

    // Animations
    private Animation slideDown;
    private Animation slideUp;

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
                // Create a new deck
                Deck mDeck = viewModel.createDeck(identityCardCode);

                // Start the new deck activity
                startDeckActivity(mDeck.getRowId());
                break;

            case REQUEST_SETTINGS:

                break;
        }
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
    public void OnScrollListener(RecyclerView recyclerView, int dx, int dy) {
        if (dy > 0 && mScrollDirection <= 0) { // Scroll Down
            // hide fab
            fabButton.startAnimation(slideDown);
            fabButton.hide();
            mScrollDirection = dy;
        } else if (dy < 0 && mScrollDirection >= 0) { // Scroll Up
            // show fab
            fabButton.show();
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
