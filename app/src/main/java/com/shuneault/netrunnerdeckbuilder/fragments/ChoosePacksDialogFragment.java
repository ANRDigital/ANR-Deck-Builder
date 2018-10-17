package com.shuneault.netrunnerdeckbuilder.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.helper.AppManager;

import java.util.ArrayList;

public class ChoosePacksDialogFragment extends DialogFragment {


    public interface ChoosePacksDialogListener {
        void onChoosePacksDialogPositiveClick(DialogFragment dialog);

        void onChoosePacksDialogNegativeClick(DialogFragment dialog);
    }

    private ChoosePacksDialogListener mListener;

    private ArrayList<String> mItems;
    private boolean[] arrChecks = new boolean[0];
    private ArrayList<String> selectedValues = new ArrayList<>();


    public ArrayList<String> getSelectedValues() {
        ArrayList<String> values = new ArrayList<>();
        for (int i = 0; i < arrChecks.length; i++) {
            if (arrChecks[i])
                values.add(mItems.get(i));
        }
        return values;
    }

    public void setPackFilter(ArrayList<String> packFilter){
        this.selectedValues = packFilter;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        mItems = AppManager.getInstance().getSetNames();

        arrChecks = new boolean[mItems.size()];
        for (int i = 0; i < mItems.size(); i++) {
            if (selectedValues.contains(mItems.get(i))){
                arrChecks[i] = true;
            }
        }

        // Set the dialog title
        builder.setTitle(R.string.choose_packs)
            // Specify the list array, the items to be selected by default (null for none),
            // and the listener through which to receive callbacks when items are selected
            .setMultiChoiceItems(mItems.toArray(new String[0]), arrChecks,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        arrChecks[which] = isChecked;
                    }
                })
            // Set the action buttons
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                // Send the positive button event back to the host activity
                mListener.onChoosePacksDialogPositiveClick(ChoosePacksDialogFragment.this);
                }
            })
            .setNeutralButton(R.string.reset, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    for (int i = 0; i < arrChecks.length; i++) {
                        arrChecks[i] = false;
                    }
                    // Send the positive button event back to the host activity

                    mListener.onChoosePacksDialogPositiveClick(ChoosePacksDialogFragment.this);
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                // Send the negative button event back to the host activity
                mListener.onChoosePacksDialogNegativeClick(ChoosePacksDialogFragment.this);
                }
            });

        return builder.create();
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (ChoosePacksDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(getActivity().toString()
                    + " must implement NoticeDialogListener");
        }
    }
}
