package com.kaching123.tcr.fragment.settings;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.SuperBaseFragment;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment.DialogType;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment.OnDialogClickListener;
import com.kaching123.tcr.fragment.settings.FindDeviceFragment.FindDeviceListener;
import com.kaching123.tcr.fragment.settings.FindDeviceFragment.Mode;
import com.mobeta.android.dslv.DragSortListView;

/**
 * Created by pkabakov on 28.02.14.
 */
@EFragment(R.layout.settings_display_fragment)
@OptionsMenu(R.menu.settings_display_fragment)
public class DisplayFragment extends SuperBaseFragment {

    @ViewById
    protected DragSortListView list;

    @ViewById
    protected View emptyItem;

    private DisplayAdapter adapter;

    public static Fragment instance() {
        return DisplayFragment_.builder().build();
    }

    @AfterViews
    protected void initViews() {
        adapter = new DisplayAdapter(getActivity());
        list.setAdapter(adapter);
        list.setEmptyView(emptyItem);

        setDisplay();
    }

    private void forgetDisplay() {
        getApp().getShopPref().displayAddress().remove();
        getApp().getShopPref().displayName().remove();
    }

    private void setDisplay() {
        String displayAddress = getApp().getShopPref().displayAddress().get();
        String displayName = getApp().getShopPref().displayName().get();
        boolean displayConfigured = !TextUtils.isEmpty(displayAddress);

        adapter.clear();
        if (displayConfigured)
            adapter.add(!TextUtils.isEmpty(displayName) ? displayName : displayAddress);
    }

    @OptionsItem
    protected void actionSearchSelected() {
        FindDeviceFragment.show(getActivity(), findDisplayListener, Mode.DISPLAY);
    }

    private FindDeviceListener findDisplayListener = new FindDeviceListener() {

        @Override
        public void onDeviceSelected() {
            setDisplay();
        }

    };

    private class DisplayAdapter extends ArrayAdapter<String> implements DragSortListView.RemoveListener {

        public DisplayAdapter(Context context) {
            super(context, R.layout.device_list_item, android.R.id.text1);
        }

        @Override
        public void remove(int i) {
            AlertDialogFragment.show(
                    getActivity(),
                    DialogType.CONFIRM_NONE,
                    R.string.display_delete_warning_dialog_title,
                    getString(R.string.display_delete_warning_dialog_message),
                    R.string.btn_confirm,
                    new OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            forgetDisplay();
                            clear();
                            return true;
                        }
                    }, new OnDialogClickListener() {
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
