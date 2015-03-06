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
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.mobeta.android.dslv.DragSortListView;

/**
 * Created by teli.yin on 2/4/2015.
 */
@EFragment(R.layout.settings_usb_msr_fragment)
@OptionsMenu(R.menu.settings_usb_msr_fragment)
public class USBMsrFragment extends SuperBaseFragment {

    @ViewById
    protected DragSortListView list;

    @ViewById
    protected View emptyItem;

    private UsbMsrAdapter adapter;

    public static Fragment instance() {
        return USBMsrFragment_.builder().build();
    }

    @AfterViews
    protected void initViews() {
        adapter = new UsbMsrAdapter(getActivity());
        list.setAdapter(adapter);
        list.setEmptyView(emptyItem);

        setUsbMsr();
    }

    private void forgetUsbMsr() {
        getApp().getShopPref().usbMSRName().remove();
    }

    private void setUsbMsr() {
        String usbMsrName = getApp().getShopPref().usbMSRName().get();
        boolean usbMsrConfigured = !TextUtils.isEmpty(usbMsrName);

        adapter.clear();
        if (usbMsrConfigured)
            adapter.add(usbMsrName);
    }

    @OptionsItem
    protected void actionSearchSelected() {
        FindDeviceFragment.show(getActivity(), findDisplayListener, FindDeviceFragment.Mode.USBMSR);
    }

    private FindDeviceFragment.FindDeviceListener findDisplayListener = new FindDeviceFragment.FindDeviceListener() {

        @Override
        public void onDeviceSelected() {
            setUsbMsr();
        }

    };

    private class UsbMsrAdapter extends ArrayAdapter<String> implements DragSortListView.RemoveListener {

        public UsbMsrAdapter(Context context) {
            super(context, R.layout.device_list_item, android.R.id.text1);
        }

        @Override
        public void remove(int i) {
            AlertDialogFragment.show(
                    getActivity(),
                    AlertDialogFragment.DialogType.CONFIRM_NONE,
                    R.string.usb_msr_delete_warning_dialog_title,
                    getString(R.string.usb_msr_delete_warning_dialog_message),
                    R.string.btn_confirm,
                    new StyledDialogFragment.OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            forgetUsbMsr();
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
