package com.shuneault.netrunnerdeckbuilder.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shuneault.netrunnerdeckbuilder.R
import com.shuneault.netrunnerdeckbuilder.db.CardRepository
import com.shuneault.netrunnerdeckbuilder.fragments.dialog.PackListRecyclerViewAdapter
import com.shuneault.netrunnerdeckbuilder.game.Format
import org.koin.android.ext.android.inject
import java.util.*

class ChoosePacksDialogFragment(val packFilter: ArrayList<String>, startFormat: Format, val allowFormatChange: Boolean) : DialogFragment() {
    interface ChoosePacksDialogListener {
        fun onChoosePacksDialogPositiveClick(dialog: DialogFragment?)
        fun onMyCollectionChosen(dialog: DialogFragment?)
    }

    var format: Format
    private lateinit var rv: RecyclerView
    private lateinit var adapter: PackListRecyclerViewAdapter
    private val repo: CardRepository by inject()

    private var mListener: ChoosePacksDialogListener? = null
    private val mPackCodes = ArrayList<String>()
    private val mPackNames = ArrayList<String>()
    private var selectedValues = ArrayList<String>()

    init {
        selectedValues = packFilter
        this.format = startFormat
    }

    fun getSelectedValues(): ArrayList<String> {
        return adapter.getSelectedValues().map { p -> p.code } as ArrayList<String>
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = layoutInflater.inflate(R.layout.fragment_item_list, null)
        rv = view.findViewById(R.id.pack_list)

        val spnFormat: Spinner = view.findViewById(R.id.spnFormat)
        spnFormat.adapter = ArrayAdapter<Format>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                repo.formats)
        spnFormat.setSelection(repo.formats.indexOf(format))
        spnFormat.isEnabled = allowFormatChange
        spnFormat.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                // update the deck format
                val newFormat = parent?.getItemAtPosition(position) as Format
                if (newFormat != format) {
                    format = newFormat
                    reloadPackList()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        reloadPackList()

        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle(R.string.choose_packs)
                .setView(view)
                // Set the action buttons
                .setPositiveButton(R.string.ok) { dialog: DialogInterface?, id: Int -> mListener!!.onChoosePacksDialogPositiveClick(this) }
                .setNegativeButton(R.string.reset) { dialog: DialogInterface?, id: Int ->
                    adapter.clearSelectedValues()
                    mListener!!.onChoosePacksDialogPositiveClick(this)
                }
                .setNeutralButton("Collection") { dialog: DialogInterface?, which: Int -> mListener!!.onMyCollectionChosen(this) }
        return builder.create()
    }

    private fun reloadPackList() {
        val packs = repo.getPacks(format)
        mPackCodes.clear()
        mPackNames.clear()
        for (p in packs) {
            mPackCodes.add(p.code)
            mPackNames.add(p.name)
        }

        // Set the pack list adapter
        rv.layoutManager = LinearLayoutManager(context)
        adapter = PackListRecyclerViewAdapter(packs, selectedValues)
        rv.adapter = adapter
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = try {
            context as ChoosePacksDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement ChoosePacksDialogListener")
        }
    }
}