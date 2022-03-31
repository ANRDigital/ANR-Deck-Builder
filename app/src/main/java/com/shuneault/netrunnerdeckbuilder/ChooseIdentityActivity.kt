package com.shuneault.netrunnerdeckbuilder

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.shuneault.netrunnerdeckbuilder.adapters.IdentitySpinnerAdapter
import com.shuneault.netrunnerdeckbuilder.db.CardRepository
import com.shuneault.netrunnerdeckbuilder.fragments.NewDeckChooseIdentityFragment
import com.shuneault.netrunnerdeckbuilder.game.Card
import com.shuneault.netrunnerdeckbuilder.game.Format
import com.shuneault.netrunnerdeckbuilder.helper.AppManager
import com.shuneault.netrunnerdeckbuilder.helper.Sorter.IdentitySorter
import org.koin.android.ext.android.inject
import java.util.*

class ChooseIdentityActivity : AppCompatActivity() {
    private var mIdentities: ArrayList<Card> = ArrayList<Card>()
    private var btnOK: Button? = null
    private var btnCancel: Button? = null
    private var spinIdentities: Spinner? = null
    private var pagerIdentity: ViewPager? = null
    private var mIdentity: Card? = null
    private var mSideCode: String? = null
    private var mInitialIdentity: String? = null
    private var mFormat: Format? = null

    val cardRepo: CardRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.fragment_pick_identity)

        // GUI
        spinIdentities = findViewById<View>(R.id.spinIdentities) as Spinner
        btnOK = findViewById<View>(R.id.btnOK) as Button
        btnCancel = findViewById<View>(R.id.btnCancel) as Button
        pagerIdentity = findViewById<View>(R.id.pagerIdentity) as ViewPager
        val repo = cardRepo

        // Arguments
        val intent = intent
        mSideCode = intent.getStringExtra(EXTRA_SIDE_CODE)
        mInitialIdentity = intent.getStringExtra(EXTRA_INITIAL_IDENTITY_CODE)
        val formatID = intent.getIntExtra(EXTRA_FORMAT, 0)
        mFormat = if (formatID > 0) repo.getFormat(formatID) else repo.defaultFormat

        // The identities
        mIdentities = repo.getCardPool(mFormat).getIdentities(mSideCode)
        // Quit if no identities
        if (mIdentities.size == 0) {
            Toast.makeText(this, "No identities in packs", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        Collections.sort(mIdentities, IdentitySorter())
        val adapterCard: ArrayAdapter<Card> = IdentitySpinnerAdapter(this, mIdentities)
        spinIdentities!!.adapter = adapterCard

        // Set the view pager adapter
        pagerIdentity!!.adapter = ImagePagerAdapter(supportFragmentManager)
        mIdentity = mIdentities.get(pagerIdentity!!.currentItem)

        // Events
        btnOK!!.setOnClickListener {
            val data = Intent()
            data.putExtra(EXTRA_IDENTITY_CODE, mIdentity!!.getCode())
            setResult(RESULT_OK, data)
            finish()
        }
        btnCancel!!.setOnClickListener { finish() }
        spinIdentities!!.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                arg0: AdapterView<*>?, arg1: View,
                arg2: Int, arg3: Long
            ) {
                pagerIdentity!!.setCurrentItem(arg2, false)
            }

            override fun onNothingSelected(arg0: AdapterView<*>?) {}
        }
        pagerIdentity!!.setOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageSelected(arg0: Int) {
                mIdentity = mIdentities.get(arg0)
                spinIdentities!!.setSelection(arg0)
            }

            override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {
                //
            }

            override fun onPageScrollStateChanged(arg0: Int) {
                //
            }
        })

        // Display the initial identity if any
        if (mInitialIdentity != null) {
            for (i in mIdentities.indices) {
                if (mIdentities[i].code == mInitialIdentity) {
                    pagerIdentity!!.currentItem = i
                    break
                }
            }
        }
    }

    private inner class ImagePagerAdapter(fm: FragmentManager?) : FragmentStatePagerAdapter(
        fm!!
    ) {
        override fun getCount(): Int {
            return mIdentities.size
        }

        override fun getItem(arg0: Int): Fragment {
            // Change the label
            val x = NewDeckChooseIdentityFragment()
            val bundle = Bundle()
            bundle.putString(
                NewDeckChooseIdentityFragment.ARGUMENT_IDENTITY_CODE,
                mIdentities[arg0].code
            )
            x.arguments = bundle
            return x
        }
    }

    companion object {
        const val EXTRA_SIDE_CODE = "com.example.netrunnerdeckbuilder.EXTRA_SIDE_CODE"
        const val EXTRA_FORMAT = "EXTRA_FORMAT"
        const val EXTRA_INITIAL_IDENTITY_CODE =
            "com.example.netrunnerdeckbuilder.EXTRA_INITIAL_IDENTITY_CODE"
        const val EXTRA_IDENTITY_CODE = "com.example.netrunnerdeckbuilder.EXTRA_IDENTITY_CODE"
    }
}