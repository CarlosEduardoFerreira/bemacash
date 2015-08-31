package com.kaching123.tcr.fragment.settings;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;

import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.SuperBaseFragment;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.mobeta.android.dslv.DragSortListView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

/**
 * Created by long.jiao on 7/21/2015.
 */
@EFragment(R.layout.settings_scale_fragment)
@OptionsMenu(R.menu.settings_scale_fragment)
public class ScaleFragment extends SuperBaseFragment {

    @ViewById
    protected DragSortListView list;

    @ViewById
    protected View emptyItem;

    private ScaleAdapter adapter;

    public static Fragment instance() {
        return ScaleFragment_.builder().build();
    }

    @AfterViews
    protected void initViews() {
        adapter = new ScaleAdapter(getActivity());
        list.setAdapter(adapter);
        list.setEmptyView(emptyItem);

        setScale();
    }

    private void forgetScale() {
        getApp().getShopPref().scaleName().remove();
    }

    private void setScale() {
        String scaleName = getApp().getShopPref().scaleName().get();
        boolean scaleConfigured = !TextUtils.isEmpty(scaleName);

        adapter.clear();
        if (scaleConfigured)
            adapter.add(scaleName);
    }

    @OptionsItem
    protected void actionSearchSelected() {
        FindDeviceFragment.show(getActivity(), findDisplayListener, FindDeviceFragment.Mode.USBMSR);
    }

    private FindDeviceFragment.FindDeviceListener findDisplayListener = new FindDeviceFragment.FindDeviceListener() {

        @Override
        public void onDeviceSelected() {
            setScale();
        }

    };

    private class ScaleAdapter extends ArrayAdapter<String> implements DragSortListView.RemoveListener {

        public ScaleAdapter(Context context) {
            super(context, R.layout.device_list_item, android.R.id.text1);
        }

        @Override
        public void remove(int i) {
            AlertDialogFragment.show(
                    getActivity(),
                    AlertDialogFragment.DialogType.CONFIRM_NONE,
                    R.string.scale_delete_warning_dialog_title,
                    getString(R.string.scale_delete_warning_dialog_message),
                    R.string.btn_confirm,
                    new StyledDialogFragment.OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            forgetScale();
                            clear();
                            return true;
                        }
                    }, new StyledDialogFragment.OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            adapter.notifyDataSetChanged();
                            return true;
                        }
                    }, null
            );
        }

    }

}
