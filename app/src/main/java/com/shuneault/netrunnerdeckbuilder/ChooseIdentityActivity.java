package com.shuneault.netrunnerdeckbuilder;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.shuneault.netrunnerdeckbuilder.adapters.IdentitySpinnerAdapter;
import com.shuneault.netrunnerdeckbuilder.fragments.NewDeckChooseIdentityFragment;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.helper.AppManager;
import com.shuneault.netrunnerdeckbuilder.helper.Sorter;

import java.util.ArrayList;
import java.util.Collections;

public class ChooseIdentityActivity extends AppCompatActivity {

    public static final String EXTRA_SIDE_CODE = "com.example.netrunnerdeckbuilder.EXTRA_SIDE_CODE";
    public static final String EXTRA_INITIAL_IDENTITY_CODE = "com.example.netrunnerdeckbuilder.EXTRA_INITIAL_IDENTITY_CODE";

    public static final String EXTRA_IDENTITY_CODE = "com.example.netrunnerdeckbuilder.EXTRA_IDENTITY_CODE";

    private ArrayList<Card> mIdentities;
    private Button btnOK;
    private Button btnCancel;
    private Spinner spinIdentities;
    private ViewPager pagerIdentity;
    private Card mIdentity;
    private String mSideCode;
    private String mInitialIdentity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.fragment_pick_identity);

        // GUI
        spinIdentities = (Spinner) findViewById(R.id.spinIdentities);
        btnOK = (Button) findViewById(R.id.btnOK);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        pagerIdentity = (ViewPager) findViewById(R.id.pagerIdentity);

        // Arguments
        mSideCode = getIntent().getStringExtra(EXTRA_SIDE_CODE);
        mInitialIdentity = getIntent().getStringExtra(EXTRA_INITIAL_IDENTITY_CODE);

        // The identities
        mIdentities = AppManager.getInstance().getCardsFromDataPacksToDisplay().getIdentities(mSideCode);
        // Quit if no identities
        if (mIdentities.size() == 0) {
            Toast.makeText(this, "No identities in packs", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Collections.sort(mIdentities, new Sorter.IdentitySorter());
        ArrayAdapter<Card> adapterCard = new IdentitySpinnerAdapter(this, mIdentities);
        spinIdentities.setAdapter(adapterCard);

        // Set the view pager adapter
        pagerIdentity.setAdapter(new ImagePagerAdapter(getSupportFragmentManager()));
        mIdentity = mIdentities.get(pagerIdentity.getCurrentItem());

        // Events
        btnOK.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra(EXTRA_IDENTITY_CODE, mIdentity.getCode());
                setResult(RESULT_OK, data);
                finish();
            }
        });
        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        spinIdentities.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                pagerIdentity.setCurrentItem(arg2, false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
        pagerIdentity.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                mIdentity = mIdentities.get(arg0);
                spinIdentities.setSelection(arg0);
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

        // Display the initial identity if any
        if (mInitialIdentity != null) {
            for (int i = 0; i < mIdentities.size(); i++) {
                if (mIdentities.get(i).getCode().equals(mInitialIdentity)) {
                    pagerIdentity.setCurrentItem(i);
                    break;
                }
            }
        }

    }

    private class ImagePagerAdapter extends FragmentStatePagerAdapter {

        public ImagePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return mIdentities.size();
        }


        @Override
        public Fragment getItem(int arg0) {
            // Change the label
            NewDeckChooseIdentityFragment x = new NewDeckChooseIdentityFragment();
            Bundle bundle = new Bundle();
            bundle.putString(NewDeckChooseIdentityFragment.ARGUMENT_IDENTITY_CODE, mIdentities.get(arg0).getCode());
            x.setArguments(bundle);
            return x;

        }

    }
}
