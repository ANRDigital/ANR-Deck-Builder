package com.shuneault.netrunnerdeckbuilder;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;

import com.shuneault.netrunnerdeckbuilder.ViewModel.FullScreenViewModel;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.helper.ImageDisplayer;
import com.shuneault.netrunnerdeckbuilder.helper.NrdbHelper;

import java.util.ArrayList;

import static org.koin.java.standalone.KoinJavaComponent.get;

public class ViewDeckFullscreenActivity extends AppCompatActivity {
    // Arguments
    public static final String EXTRA_DECK_ID = "com.example.netrunnerdeckbuilder.EXTRA_DECK_ID";
    public static final String EXTRA_CARD_CODE = "com.example.netrunnerdeckbuilder.EXTRA_CARD_CODE";
    public static final String EXTRA_POSITION = "com.example.netrunnerdeckbuilder.EXTRA_POSITION";
    public static final String EXTRA_CARDS = "com.example.netrunnerdeckbuilder.EXTRA_CARDS";

    private ArrayList<Card> mCards = new ArrayList<>();

    private FullScreenViewModel vm = get(FullScreenViewModel.class);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        }

        // load data
        vm.setCardCode(getIntent().getStringExtra(EXTRA_CARD_CODE));
        vm.setPosition(getIntent().getIntExtra(EXTRA_POSITION, 0));
        ArrayList<String> cardList = (ArrayList<String>) getIntent().getSerializableExtra(EXTRA_CARDS);
        if (cardList != null){
            vm.setCardCodes(cardList);
        }
        long deckId = getIntent().getLongExtra(EXTRA_DECK_ID, 0);
        if (deckId > 0) {
            vm.loadDeck(deckId);
        }

        // set theme to identity's faction colors
        String factionCode = vm.getFactionCode();
        if (factionCode != null){
            setTheme(getResources().getIdentifier("Theme.Netrunner_" + factionCode.replace("-", ""), "style", this.getPackageName()));
        }

        setContentView(R.layout.activity_fullscreen_view);

        // GUI Elements
        ViewPager mPager = (ViewPager) findViewById(R.id.pager);

        // Build the image array
        mCards = vm.getCards();

        // Quit if deck is empty
        if (mCards.size() == 0) {
            exitIfDeckEmpty();
            return;
        }

        // Change the icon and title
        updateTitle(mCards.get(vm.getPosition()));

        // Set the adapter for the view pager
        mPager.setAdapter(new ImageViewPager());
        mPager.setCurrentItem(vm.getPosition());
        mPager.addOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                vm.setPosition(arg0);
                Card card = mCards.get(arg0);
                updateTitle(card);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.fullscreen_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.mnuOnline:
                // show nrdb page!
                Card currentCard = mCards.get(vm.getPosition());
                NrdbHelper.ShowNrdbWebPage(this, currentCard);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void exitIfDeckEmpty() {
        if (mCards.size() == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.view_deck);
            builder.setMessage(R.string.deck_is_empty);
            builder.setCancelable(false);
            builder.setPositiveButton(R.string.ok, new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.show();
        }
    }

    private void updateTitle(Card card) {
        Deck deck = vm.getDeck();
        if (deck != null) {
            setTitle("[" + deck.getCardCount(card) + "] - " + deck.getName());
        } else {
            setTitle(card.getTitle());
        }
    }


    private class ImageViewPager extends PagerAdapter {

        @Override
        public int getCount() {
            return mCards.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == (LinearLayout) arg1;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Card card = mCards.get(position);
            LinearLayout v = new LinearLayout(ViewDeckFullscreenActivity.this);
            v.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            v.setBackgroundColor(Color.BLACK);
            ImageView imgCard = new ImageView(ViewDeckFullscreenActivity.this);
            imgCard.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            //imgCard.setImageBitmap(card.getImage(ViewDeckFullscreenActivity.this));
            ImageDisplayer.fill(imgCard, card, ViewDeckFullscreenActivity.this);
            imgCard.setVisibility(View.VISIBLE);
            // Close the activity if the image is clicked
            imgCard.setOnClickListener(arg0 -> ViewDeckFullscreenActivity.this.finish());
            v.addView(imgCard);
            ((ViewPager) container).addView(v, 0);
            return v;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mCards.get(position).getTitle();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();

        return false;
    }
}
