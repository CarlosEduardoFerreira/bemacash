package com.kaching123.tcr.fragment.inventory;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;

import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;

/**
 * Created by Vladimir on 24.02.14.
 */
@EFragment
public class ButtonViewSelectDialogFragment  extends StyledDialogFragment {

    private static final String DIALOG_NAME = "button_view_select_dialog";

    @ViewById
    protected GridView grid;

    BaseAdapter gridAdapter;

    private IButtonViewDialogListener listener;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = getResources().getDimensionPixelOffset(R.dimen.button_view_select_dialog_width);
        params.height = getResources().getDimensionPixelOffset(R.dimen.button_view_select_dialog_height);

        gridAdapter = new GridAdapter();
        grid.setAdapter(gridAdapter);
        grid.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (listener != null){
                    listener.onSelect(position);
                }
                dismiss();
            }
        });

    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.button_view_select_dialog_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return R.string.button_view_select_dialog_title;
    }

    @Override
    protected int getNegativeButtonTitle() {
        return R.string.btn_cancel;
    }

    @Override
    protected int getPositiveButtonTitle() {
        return 0;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return null;
    }

    @Override
    protected boolean hasPositiveButton() {
        return false;
    }

    private void setListener(IButtonViewDialogListener listener){
        this.listener = listener;
    }

    public static interface IButtonViewDialogListener{
        void onSelect(int level);
    }

    private class GridAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return getActivity().getResources().getInteger(R.integer.quick_buttons_view_count);
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = newView(parent, position);
            }
            bindView(convertView, parent, position);
            return convertView;
        }

        private View newView(ViewGroup parent, int position) {
            return LayoutInflater.from(getActivity()).inflate(R.layout.quickservice_item_view, parent, false);
        }

        private void bindView(View convertView, ViewGroup parent, int position) {
            convertView.getBackground().setLevel(position);
        }
    }

    public static void show(FragmentActivity activity, IButtonViewDialogListener listener){
        DialogUtil.show(activity, DIALOG_NAME, ButtonViewSelectDialogFragment_.builder().build()).setListener(listener);
    }
}
