package com.shuneault.netrunnerdeckbuilder.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import kotlin.Lazy;

import com.shuneault.netrunnerdeckbuilder.R;
import com.shuneault.netrunnerdeckbuilder.db.CardRepository;
import com.shuneault.netrunnerdeckbuilder.game.Format;
import com.shuneault.netrunnerdeckbuilder.game.Pack;

import java.util.ArrayList;

import static org.koin.java.standalone.KoinJavaComponent.inject;

public class ChoosePacksDialogFragment extends DialogFragment {


    private Format format;
    private ArrayList<String> mPackCodes = new ArrayList<>();
    private Lazy<CardRepository> repo = inject(CardRepository.class);

    public interface ChoosePacksDialogListener {
        void onChoosePacksDialogPositiveClick(DialogFragment dialog);
    }

    private ChoosePacksDialogListener mListener;

    private ArrayList<String> mPackNames = new ArrayList<>();
    private boolean[] arrChecks = new boolean[0];
    private ArrayList<String> selectedValues = new ArrayList<>();


    public ArrayList<String> getSelectedValues() {
        ArrayList<String> values = new ArrayList<>();
        for (int i = 0; i < arrChecks.length; i++) {
            if (arrChecks[i])
                values.add(mPackCodes.get(i));
        }
        return values;
    }

    public void setData(ArrayList<String> packFilter, Format format){
        this.selectedValues = packFilter;
        this.format = format;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        CardRepository cardRepo = repo.getValue();
        ArrayList<Pack> packs = cardRepo.getPacks(format);
        mPackCodes.clear();
        for (Pack p: packs) {
            mPackCodes.add(p.getCode());
            mPackNames.add(p.getName());
        }

        arrChecks = new boolean[mPackCodes.size()];
        for (int i = 0; i < mPackNames.size(); i++) {
            if (selectedValues.contains(mPackCodes.get(i))){
                arrChecks[i] = true;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set the dialog title
        builder.setTitle(R.string.choose_packs)
            // Specify the list array, the items to be selected by default (null for none),
            // and the listener through which to receive callbacks when items are selected
            .setMultiChoiceItems(mPackNames.toArray(new String[0]), arrChecks,
                    (dialog, which, isChecked) -> arrChecks[which] = isChecked)

            // Set the action buttons
            .setPositiveButton(R.string.ok, (dialog, id) -> mListener.onChoosePacksDialogPositiveClick(ChoosePacksDialogFragment.this))
            .setNeutralButton(R.string.reset, (dialog, id) -> {
                for (int i = 0; i < arrChecks.length; i++) {
                    arrChecks[i] = false;
                }
                mListener.onChoosePacksDialogPositiveClick(ChoosePacksDialogFragment.this);
            });

        return builder.create();
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (ChoosePacksDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement NoticeDialogListener");
        }
    }
}
