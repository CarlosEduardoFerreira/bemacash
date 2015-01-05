package com.kaching123.tcr.fragment.edit;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.EditText;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.store.saleorder.UpdateNotesSaleOrderItemCommand;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;

/**
 * Created by pkabakov on 21.02.14.
 */
@EFragment
public class NotesEditFragment extends StyledDialogFragment {

    private static final String DIALOG_NAME = NotesEditFragment.class.getSimpleName();

    @ViewById
    protected EditText notesEdit;

    @FragmentArg
    protected String itemNotes;
    @FragmentArg
    protected String itemGuid;


    public static void show(FragmentActivity activity, String itemNotes, String itemGuid) {
        DialogUtil.show(activity, DIALOG_NAME, NotesEditFragment_.builder().itemNotes(itemNotes).itemGuid(itemGuid).build());
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.notes_edit_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.notes_edit_fragment_title;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return android.R.string.cancel;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return R.string.btn_save;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(
                getResources().getDimensionPixelOffset(R.dimen.notes_edit_dialog_width),
                getDialog().getWindow().getAttributes().height);
    }

    @AfterViews
    protected void initViews() {
        notesEdit.setText(itemNotes);
        notesEdit.setSelection(notesEdit.getText().length());
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return new OnDialogClickListener() {
            @Override
            public boolean onClick() {
                callCommand();
                return true;
            }
        };
    }

    private void callCommand() {
        String notes = notesEdit.getText().toString();
        UpdateNotesSaleOrderItemCommand.start(getActivity(), itemGuid, notes);
    }
}
