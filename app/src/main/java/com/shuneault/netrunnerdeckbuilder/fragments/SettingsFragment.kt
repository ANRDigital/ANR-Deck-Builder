package com.shuneault.netrunnerdeckbuilder.fragments

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.preference.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shuneault.netrunnerdeckbuilder.BuildConfig
import com.shuneault.netrunnerdeckbuilder.MainActivity
import com.shuneault.netrunnerdeckbuilder.R
import com.shuneault.netrunnerdeckbuilder.db.CardRepository
import com.shuneault.netrunnerdeckbuilder.db.IDeckRepository
import com.shuneault.netrunnerdeckbuilder.game.Card
import com.shuneault.netrunnerdeckbuilder.game.Format
import com.shuneault.netrunnerdeckbuilder.game.NetRunnerBD
import com.shuneault.netrunnerdeckbuilder.helper.CardImagesDownloader
import com.shuneault.netrunnerdeckbuilder.helper.CardImagesDownloader.CardImagesDownloaderListener
import com.shuneault.netrunnerdeckbuilder.helper.LocalFileHelper
import com.shuneault.netrunnerdeckbuilder.helper.StringDownloader
import com.shuneault.netrunnerdeckbuilder.helper.StringDownloader.FileDownloaderListener
import org.json.JSONArray
import org.koin.android.ext.android.inject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class SettingsFragment() : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {
    // Preferences
    var prefCollection: MultiSelectListPreference? = null
    var prefClearCache: Preference? = null
    var prefDownloadAllImages: Preference? = null
    var prefDownloadCardData: Preference? = null
    var prefExportDecks: Preference? = null
    var prefAbout: Preference? = null
    var prefDefFormat: ListPreference? = null
    val repo: CardRepository by inject()
    val mDeckRepo: IDeckRepository by inject()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        requireActivity().setTitle(R.string.action_settings)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        prefAbout = findPreference(KEY_PREF_ABOUT)
        prefAbout!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference: Preference? ->
            showAboutDialog()
            false
        }
        prefClearCache = findPreference(KEY_PREF_CLEAR_CACHE)
        prefClearCache!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference: Preference? ->
            doClearCache()
            false
        }
        prefDownloadAllImages = findPreference(KEY_PREF_DOWNLOAD_ALL_IMAGES)
        prefDownloadAllImages!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference: Preference? ->
            doDownloadAllImages()
            false
        }
        prefDownloadCardData = findPreference(KEY_PREF_DOWNLOAD_CARD_DATA)
        prefDownloadCardData!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference: Preference? ->
            repo.doDownloadAllData()
            // update last download date
            sharedPreferences.edit()
                    .putLong(SHARED_PREF_LAST_UPDATE_DATE, Calendar.getInstance().timeInMillis)
                    .apply()
            refreshPrefsSummaries()
            false
        }
        prefExportDecks = findPreference(KEY_PREF_EXPORT_ALL_DECKS)
        prefExportDecks!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference: Preference? ->
            doExportAllDecks()
            false
        }

        // Collection
        prefCollection = findPreference<Preference>(KEY_PREF_COLLECTION) as MultiSelectListPreference?
        val packs = repo.getPacks(true)
        val packNames = arrayOfNulls<CharSequence>(packs.size)
        val packCodes = arrayOfNulls<CharSequence>(packs.size)
        for (i in packs.indices) {
            packNames[i] = packs[i].name
            packCodes[i] = packs[i].code
        }
        prefCollection!!.entries = packNames
        prefCollection!!.entryValues = packCodes

        // Format
        prefDefFormat = findPreference<Preference>(KEY_PREF_DEFAULT_FORMAT) as ListPreference?
        val formats = repo.formats
        val formatNames = arrayOfNulls<CharSequence>(formats.size)
        val formatIds = arrayOfNulls<CharSequence>(formats.size)
        for (i in formats.indices) {
            formatNames[i] = formats[i].name
            formatIds[i] = formats[i].id.toString()
        }
        prefDefFormat!!.entries = formatNames
        prefDefFormat!!.entryValues = formatIds

        // Display the summary for data packs to display
        refreshPrefsSummaries()
    }

    private fun showAboutDialog() {
        val pInfo: PackageInfo
        val txt = TextView(context)
        try {
            pInfo = requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0)
            txt.text = getString(R.string.about_text, pInfo.versionName)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        txt.movementMethod = LinkMovementMethod.getInstance()
        txt.setPadding(25, 25, 25, 25)
        val builder2 = MaterialAlertDialogBuilder(requireContext())
        builder2.setTitle(R.string.menu_about)
        builder2.setView(txt)
        builder2.setPositiveButton(R.string.ok, null)
        builder2.show()
    }

    private fun doClearCache() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle(R.string.clear_cache)
        builder.setMessage(getString(R.string.message_clear_cache))
        builder.setNegativeButton(android.R.string.no) { dialog, which -> }
        builder.setPositiveButton(android.R.string.yes) { dialog, which -> deleteCache(context) }
        builder.show()
    }

    private fun doDownloadAllImages() {
        val cardDownloader = CardImagesDownloader(context, object : CardImagesDownloaderListener {
            override fun onBeforeStartTask(context: Context, max: Int) {}
            override fun onTaskCompleted() {}
            override fun onImageDownloaded(card: Card, count: Int, max: Int) {}
        })
        cardDownloader.execute()
    }

    private fun doExportAllDecks() {
        val jsonArray = JSONArray()
        for (deck in mDeckRepo.allDecks) {
            val jsonDeck = deck.toJSON()
            jsonArray.put(jsonDeck)
        }
        val filename = "netrunner_decks.anrdecks"
        // Save the file as OCTGN format
        try {
            val fileOut = requireContext().openFileOutput(filename, Context.MODE_PRIVATE)
            fileOut.write(jsonArray.toString().toByteArray())
            fileOut.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Log.i("LOGCAT", requireContext().getFileStreamPath(filename).toString())

        // Create the send intent
        val intentEmail = Intent(Intent.ACTION_SEND)
        intentEmail.type = "text/plain"
        intentEmail.putExtra(Intent.EXTRA_SUBJECT, "NetRunner Deck - All decks")
        intentEmail.putExtra(Intent.EXTRA_TEXT, "\r\n\r\nDownload Android Netrunner DeckBuilder for free at https://play.google.com/store/apps/details?id=com.shuneault.netrunnerdeckbuilder")
        val fileStreamPath = requireContext().getFileStreamPath(filename)
        val fileUri = FileProvider.getUriForFile(requireContext(),
                BuildConfig.APPLICATION_ID,
                fileStreamPath)
        intentEmail.putExtra(Intent.EXTRA_STREAM, fileUri)
        startActivityForResult(Intent.createChooser(intentEmail, getText(R.string.menu_share)), 0)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences,
                                           key: String) {
        when (key) {
            KEY_PREF_COLLECTION, KEY_PREF_DEFAULT_FORMAT -> refreshPrefsSummaries()
            KEY_PREF_LANGUAGE -> doLanguageChange()
        }
    }

    private fun doLanguageChange() {
        // Set the adapter
        //todo: can we get this from a viewModel?
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(requireActivity())
        val locale = sharedPrefs.getString(KEY_PREF_LANGUAGE, "en")
        val sd = StringDownloader(context, String.format(NetRunnerBD.getAllCardsUrl(locale)), LocalFileHelper.FILE_CARDS_JSON, object : FileDownloaderListener {
            var mDialog: ProgressDialog? = null
            override fun onBeforeTask() {
                // Display a progress dialog
                mDialog = ProgressDialog(context)
                mDialog!!.setTitle(getString(R.string.downloading_cards))
                mDialog!!.isIndeterminate = true
                mDialog!!.setCancelable(false)
                mDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
                mDialog!!.setMessage(getString(R.string.downloading_cards_restart))
                mDialog!!.show()
            }

            override fun onTaskComplete(s: String) {
                // Close the dialog
                mDialog!!.dismiss()
                val context = context
                val mStartActivity = Intent(context, MainActivity::class.java)
                val mPendingIntentId = 123456
                val mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT)
                val mgr = context!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                mgr[AlarmManager.RTC, System.currentTimeMillis() + 100] = mPendingIntent
                System.exit(0)
            }

            override fun onError(e: Exception) {
                // Display the error and cancel the ongoing dialog
                mDialog!!.dismiss()

                // If zero cards are available, exit the application
                if (repo.hasCards()) {
                    Toast.makeText(context, R.string.error_downloading_cards_quit, Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, R.string.error_downloading_cards, Toast.LENGTH_LONG).show()
                }

                // Log
                Log.e("LOG", e.message!!)
            }
        })
        sd.execute()
    }

    private fun refreshPrefsSummaries() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val stringSet = sharedPreferences.getStringSet(KEY_PREF_COLLECTION, HashSet())
        prefCollection!!.summary = stringSet?.size.toString() + " packs"
        val defaultFormat: String = sharedPreferences.getString(KEY_PREF_DEFAULT_FORMAT, Format.FORMAT_STANDARD.toString())!!
        val formatId = defaultFormat.toInt()
        val formatSummary = repo.getFormat(formatId).name
        prefDefFormat!!.summary = formatSummary
        val lastUpdate = Calendar.getInstance()
        val timeInMillis = sharedPreferences.getLong(SHARED_PREF_LAST_UPDATE_DATE, 0)
        if (timeInMillis > 0) {
            lastUpdate.timeInMillis = timeInMillis
            val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
            prefDownloadCardData!!.summary = "Last updated: " + formatter.format(lastUpdate.time)
        }
    }

    companion object {
        const val KEY_PREF_DISPLAY_SET_NAMES_WITH_CARDS = "pref_ShowSetNames"
        const val KEY_PREF_CLEAR_CACHE = "pref_ClearCache"
        const val KEY_PREF_DOWNLOAD_ALL_IMAGES = "pref_DownloadAllImages"
        const val KEY_PREF_DOWNLOAD_CARD_DATA = "pref_DownloadCardData"
        const val KEY_PREF_LANGUAGE = "pref_Language"
        const val KEY_PREF_EXPORT_ALL_DECKS = "pref_ExportAllDecks"
        const val KEY_PREF_ABOUT = "pref_About"
        const val KEY_PREF_DEFAULT_FORMAT = "pref_Format"
        const val KEY_PREF_COLLECTION = "pref_collection"
        const val SHARED_PREF_LAST_UPDATE_DATE = "SHARED_PREF_LAST_UPDATE_DATE"
        fun deleteCache(context: Context?) {
            try {
                val dir = context!!.cacheDir
                if (dir != null && dir.isDirectory) {
                    deleteDir(dir)
                }
            } catch (e: Exception) {
            }
        }

        fun deleteDir(dir: File?): Boolean {
            if (dir != null && dir.isDirectory) {
                val children = dir.list()
                for (i in children.indices) {
                    val success = deleteDir(File(dir, children[i]))
                    if (!success) {
                        return false
                    }
                }
            }
            return dir!!.delete()
        }
    }
}