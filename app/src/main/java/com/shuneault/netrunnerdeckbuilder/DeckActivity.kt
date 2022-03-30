package com.shuneault.netrunnerdeckbuilder

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shuneault.netrunnerdeckbuilder.ViewModel.DeckActivityViewModel
import com.shuneault.netrunnerdeckbuilder.export.JintekiNet
import com.shuneault.netrunnerdeckbuilder.export.OCTGN
import com.shuneault.netrunnerdeckbuilder.export.PlainText
import com.shuneault.netrunnerdeckbuilder.fragments.*
import com.shuneault.netrunnerdeckbuilder.fragments.ChoosePacksDialogFragment.ChoosePacksDialogListener
import com.shuneault.netrunnerdeckbuilder.game.Card
import com.shuneault.netrunnerdeckbuilder.game.Format
import com.shuneault.netrunnerdeckbuilder.helper.ISettingsProvider
import com.shuneault.netrunnerdeckbuilder.interfaces.IDeckViewModelProvider
import com.shuneault.netrunnerdeckbuilder.interfaces.OnDeckChangedListener
import com.shuneault.netrunnerdeckbuilder.ui.ThemeHelper.Companion.getTheme
import com.shuneault.netrunnerdeckbuilder.util.SlidingTabLayout
import org.koin.android.ext.android.inject
import java.util.concurrent.atomic.AtomicInteger
import org.koin.androidx.viewmodel.ext.android.viewModel

class DeckActivity() : AppCompatActivity(), OnDeckChangedListener, ChoosePacksDialogListener,
    IDeckViewModelProvider {
    private var layoutFiltered: LinearLayout? = null
    private var lblInfoInfluence: TextView? = null
    private var lblInfoCards: TextView? = null
    private var lblInfoAgenda: TextView? = null
    private var lblInfoLegal: TextView? = null
    private var mSelectedTab = 0
    val vm: DeckActivityViewModel by viewModel()
    private val settingsProvider: ISettingsProvider by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        // Set the theme and layout
        val deckId = intent.extras!!.getLong(ARGUMENT_DECK_ID)
        vm.setDeckId(deckId)
        try {
            setTheme(getTheme(vm.deck!!.factionCode, this))
        } catch (e: Exception) {
            // do nothing, will use default blue theme instead
            e.printStackTrace()
        }
        setContentView(R.layout.activity_deck)

        // GUI
        val mViewPager = findViewById<ViewPager>(R.id.pager)
        val layoutAgendas = findViewById<LinearLayout>(R.id.layoutAgendas)
        layoutFiltered = findViewById(R.id.layoutFiltered)
        layoutFiltered!!.setOnClickListener(View.OnClickListener { v: View? -> doChoosePacks() })
        lblInfoInfluence = findViewById(R.id.lblInfoInfluence)
        lblInfoCards = findViewById(R.id.lblInfoCards)
        lblInfoAgenda = findViewById(R.id.lblInfoAgenda)
        lblInfoLegal = findViewById(R.id.lblInfoLegal)


        // ActionBar - set elevation to 0 to remove shadow
        val mActionBar = supportActionBar
        if (mActionBar != null) {
            mActionBar.elevation = 0f
        }

        // Get the params
        mSelectedTab = savedInstanceState?.getInt(ARGUMENT_SELECTED_TAB)
            ?: intent.extras!!.getInt(ARGUMENT_SELECTED_TAB)

        // Change the title
        mActionBar!!.setDisplayHomeAsUpEnabled(true)
        mActionBar.setTitle(vm.deck!!.name)
        // app icon doesn't work with support library - needs implemented differently
//        if (mDeck.getIdentity().getFactionCode().equals(Card.Faction.FACTION_NEUTRAL)) {
//            mActionBar.setLogo(getResources().getDrawable(R.drawable.ic_launcher));
//        } else {
//            mActionBar.setLogo(mDeck.getIdentity().getFactionImageRes(this));
//        }

        // Display the agendas (in the infobar) only if it is a CORP deck
        if (vm.deck!!.side == Card.Side.SIDE_CORPORATION) {
            layoutAgendas.visibility = View.VISIBLE
        } else {
            layoutAgendas.visibility = View.GONE
        }
        setPackFilterIconVisibility()

        // Update the infobar
        updateInfoBar()

        // Set the page adapter
        mViewPager.adapter = DeckTabsPagerAdapter(supportFragmentManager)

        // attach tabs to view pager
        val tabs = findViewById<SlidingTabLayout>(R.id.tabs)
        tabs.setViewPager(mViewPager)
        if (vm.deck!!.factionCode.startsWith(Card.Faction.FACTION_NEUTRAL)) {
            tabs.setBackgroundColor(resources.getColor(R.color.netrunner_blue))
        } else {
            tabs.setBackgroundColor(
                resources.getColor(
                    resources.getIdentifier(
                        "dark_" + vm.deck!!.factionCode.replace("-", ""),
                        "color",
                        this.packageName
                    )
                )
            )
        }
    }

    private fun setPackFilterIconVisibility() {
        val format = vm.deck!!.format
        if (format.canFilter()) {
            layoutFiltered!!.visibility = View.VISIBLE
        } else {
            layoutFiltered!!.visibility = View.GONE
        }
    }

    private fun updateInfoBar() {
        // Update the influence, card count and agendas
        val deck = vm.deck
        if (deck!!.influenceLimit == Int.MAX_VALUE) {
            lblInfoInfluence!!.text =
                deck.deckInfluence.toString() + "/" + resources.getString(R.string.infinite_symbol)
        } else {
            lblInfoInfluence!!.text = deck.deckInfluence.toString() + "/" + deck.influenceLimit
        }
        lblInfoCards!!.text = deck.deckSize.toString() + "/" + deck.minimumDeckSize
        lblInfoAgenda!!.text =
            deck.deckAgenda.toString() + "/(" + deck.deckAgendaMinimum + '-' + (deck.deckAgendaMinimum + 1) + ')'

        // Update the style Influence
        if (deck.isInfluenceOk) lblInfoInfluence!!.setTextAppearance(
            this,
            R.style.InfoBarGood
        ) else lblInfoInfluence!!.setTextAppearance(this, R.style.InfoBarBad)
        // Update the style Agendas
        if (deck.isAgendaOk) lblInfoAgenda!!.setTextAppearance(
            this,
            R.style.InfoBarGood
        ) else lblInfoAgenda!!.setTextAppearance(this, R.style.InfoBarBad)
        // Update the style Cards
        if (deck.isCardCountOk) lblInfoCards!!.setTextAppearance(
            this,
            R.style.InfoBarGood
        ) else lblInfoCards!!.setTextAppearance(this, R.style.InfoBarBad)
        if (vm.isValid) {
            lblInfoLegal!!.setTextAppearance(this, R.style.InfoBarGood)
            lblInfoLegal!!.text = "✓"
        } else {
            lblInfoLegal!!.setTextAppearance(this, R.style.InfoBarBad)
            lblInfoLegal!!.text = "✗"
        }
        setPackFilterIconVisibility()
    }

    override fun getViewModel(): DeckActivityViewModel {
        return vm
    }

    inner class DeckTabsPagerAdapter(fragmentManager: FragmentManager?) : FragmentPagerAdapter(
        fragmentManager!!
    ) {
        override fun getItem(arg0: Int): Fragment {
            return when (arg0) {
                1 -> DeckMyCardsFragment()
                2 -> DeckCardsFragment()
                3 -> DeckBuildFragment()
                4 -> DeckStatsFragment()
                5 -> DeckHandFragment()
                else -> DeckInfoFragment()
            }
        }

        override fun getCount(): Int {
            //
            return 6
        }

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return resources.getString(R.string.tab_info)
                1 -> return resources.getString(R.string.tab_my_cards)
                2 -> return resources.getString(R.string.tab_cards)
                3 -> return resources.getString(R.string.tab_build)
                4 -> return resources.getString(R.string.tab_stats)
                5 -> return resources.getString(R.string.tab_hand)
            }
            return ""
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.deck, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.mnuDeleteDeck -> {
                // Alert
                val builder = MaterialAlertDialogBuilder(this)
                builder.setTitle(R.string.delete_deck)
                builder.setMessage(R.string.message_delete_deck)
                builder.setPositiveButton(R.string.ok) { dialog, which ->
                    vm.deleteDeck(vm.deck)
                    Toast.makeText(
                        this@DeckActivity,
                        R.string.message_deck_deleted,
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
                builder.setNegativeButton(R.string.cancel) { dialog, which -> }
                builder.show()
                true
            }
            R.id.mnuCloneDeck -> {
                // Create a clone of the deck
                val newDeckId = vm.cloneDeck(vm.deck)
                Toast.makeText(this, R.string.toast_deck_cloned_successfuly, Toast.LENGTH_LONG)
                    .show()
                // Start the new deck activity
                val intentClone = Intent(this@DeckActivity, DeckActivity::class.java)
                intentClone.putExtra(ARGUMENT_DECK_ID, newDeckId)
                intentClone.putExtra(ARGUMENT_SELECTED_TAB, 0)
                startActivity(intentClone)
                // Close this activity
                finish()
                true
            }
            R.id.mnuViewFullScreen -> {
                val intentFullScreen = Intent(this, DeckViewActivity::class.java)
                intentFullScreen.putExtra(ARGUMENT_DECK_ID, vm.deck!!.rowId)
                startActivity(intentFullScreen)
                true
            }
            R.id.mnuChangeIdentity -> {
                // Change the identity
                val intentChooseIdentity = Intent(this, ChooseIdentityActivity::class.java)
                intentChooseIdentity.putExtra(
                    ChooseIdentityActivity.EXTRA_SIDE_CODE,
                    vm.deck!!.side
                )
                intentChooseIdentity.putExtra(
                    ChooseIdentityActivity.EXTRA_FORMAT,
                    vm.deck!!.format.id
                )
                intentChooseIdentity.putExtra(
                    ChooseIdentityActivity.EXTRA_INITIAL_IDENTITY_CODE,
                    vm.deck!!.identity.code
                )
                startActivityForResult(intentChooseIdentity, REQUEST_CHANGE_IDENTITY)
                true
            }
            R.id.mnuOCTGN -> {
                val filename = vm.deck!!.fileSafeName + ".o8d"
                // Save the file as OCTGN format
                try {
                    val fileOut = openFileOutput(filename, MODE_PRIVATE)
                    fileOut.write(OCTGN().fromDeck(vm.deck).toByteArray())
                    fileOut.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Create the send intent
                val intentEmail = Intent(Intent.ACTION_SEND)
                intentEmail.type = "text/plain"
                intentEmail.putExtra(
                    Intent.EXTRA_SUBJECT,
                    "NetRunner Deck - " + vm.deck!!.name
                )
                intentEmail.putExtra(
                    Intent.EXTRA_TEXT,
                    "\r\n\r\nDownload Android Netrunner DeckBuilder for free at https://play.google.com/store/apps/details?id=com.shuneault.netrunnerdeckbuilder"
                )
                val fileStreamPath = getFileStreamPath(filename)
                val fileUri = FileProvider.getUriForFile(
                    this,
                    BuildConfig.APPLICATION_ID,
                    fileStreamPath
                )
                intentEmail.putExtra(Intent.EXTRA_STREAM, fileUri)
                startActivity(Intent.createChooser(intentEmail, getText(R.string.menu_share)))
                true
            }
            R.id.mnuPlainText -> {
                val plainText = PlainText(this).fromDeck(vm.deck)

                // Create the send intent
                val intentEmailPlain = Intent(Intent.ACTION_SEND)
                intentEmailPlain.type = "text/plain"
                intentEmailPlain.putExtra(
                    Intent.EXTRA_SUBJECT,
                    "NetRunner Deck - " + vm.deck!!.name
                )
                intentEmailPlain.putExtra(
                    Intent.EXTRA_TEXT,
                    "$plainText\n\nDownload Android Netrunner DeckBuilder for free at https://play.google.com/store/apps/details?id=com.shuneault.netrunnerdeckbuilder"
                )
                startActivity(Intent.createChooser(intentEmailPlain, getText(R.string.menu_share)))
                true
            }
            R.id.mnuJintekiNet -> {
                val jintekiNet = JintekiNet().fromDeck(vm.deck)

                // Create the send intent
                val intentJintekiNetPlain = Intent(Intent.ACTION_SEND)
                intentJintekiNetPlain.type = "text/plain"
                intentJintekiNetPlain.putExtra(
                    Intent.EXTRA_SUBJECT,
                    "NetRunner Deck - " + vm.deck!!.name
                )
                intentJintekiNetPlain.putExtra(Intent.EXTRA_TEXT, jintekiNet)
                startActivity(
                    Intent.createChooser(
                        intentJintekiNetPlain,
                        getText(R.string.menu_share)
                    )
                )
                true
            }
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.mnuSetPacks -> {
                doChoosePacks()
                true
            }
            R.id.mnuCoreCount -> {
                doSetCoreCount()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun doChoosePacks() {
        // display list alert dialog
        val deck = vm.deck
        val dialog = ChoosePacksDialogFragment(deck!!.packFilter, deck.format, false)
        dialog.show(supportFragmentManager, "choosePacks")
    }

    private fun doSetCoreCount() {
        val choice = AtomicInteger()
        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle(R.string.set_core_count)
            .setSingleChoiceItems(
                R.array.arrCoreCountPreference, vm.deck!!.coreCount
            ) { dialog: DialogInterface?, which: Int -> choice.set(which) }
            .setPositiveButton(R.string.ok) { dialog: DialogInterface, which: Int ->
                val count = choice.get()
                vm.setCoreCount(count) // which is zero based array
                doCardPoolChange()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog: DialogInterface, which: Int -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.show()
    }

    override fun onChoosePacksDialogPositiveClick(dialog: DialogFragment?) {
        // save the new setting
        val frag = dialog as ChoosePacksDialogFragment?
        val packFilter: ArrayList<String> = frag!!.getSelectedValues()
        vm.setPackFilter(packFilter)
        doCardPoolChange()
        updateInfoBar()
    }

    override fun onMyCollectionChosen(dialog: DialogFragment?) {
        val myCollection = settingsProvider.myCollection
        vm.setPackFilter(myCollection)
        doCardPoolChange()
        updateInfoBar()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) return
        when (requestCode) {
            REQUEST_CHANGE_IDENTITY -> {
                val idCode = data!!.getStringExtra(ChooseIdentityActivity.EXTRA_IDENTITY_CODE)
                vm.changeDeckIdentity(vm.deck, idCode)

                // Restart the activity
                val intent = Intent(this@DeckActivity, DeckActivity::class.java)
                intent.putExtra(ARGUMENT_DECK_ID, vm.deck!!.rowId)
                intent.putExtra(ARGUMENT_SELECTED_TAB, mSelectedTab)
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onDeckCardsChanged() {
        // Update the infobar
        updateInfoBar()
        val fragments = supportFragmentManager.fragments
        for (f in fragments) {
            if (f is DeckMyCardsFragment) f.onDeckCardsChanged()
            if (f is DeckCardsFragment) f.onDeckCardsChanged()
        }
    }

    override fun onFormatChanged(format: Format) {
        if (vm.changeDeckFormat(format)) {
            doCardPoolChange()
        }
    }

    private fun doCardPoolChange() {
        updateInfoBar()
        val fragments = supportFragmentManager.fragments
        for (f in fragments) {
            if (f is DeckMyCardsFragment) f.onFormatChanged()
            if (f is DeckCardsFragment) f.onFormatChanged()
        }
    }

    public override fun onPause() {
        super.onPause()

        // Save the deck
        val myHandler = Handler(Looper.getMainLooper())
        val myRunnable = Runnable { vm.save() }
        myHandler.post(myRunnable)
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(ARGUMENT_DECK_ID, vm.deck!!.rowId!!)
        outState.putInt(ARGUMENT_SELECTED_TAB, mSelectedTab)
    }

    companion object {
        // Activity Result
        const val REQUEST_CHANGE_IDENTITY = 2
        const val ARGUMENT_DECK_ID = "com.shuneault.netrunnerdeckbuilder.ARGUMENT_DECK_ID"
        const val ARGUMENT_SELECTED_TAB = "com.shuneault.netrunnerdeckbuilder.ARGUMENT_SELECTED_TAB"
    }
}