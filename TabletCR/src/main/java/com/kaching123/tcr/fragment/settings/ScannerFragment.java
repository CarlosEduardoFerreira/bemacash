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
@EFragment(R.layout.settings_scanner_fragment)
@OptionsMenu(R.menu.settings_scanner_fragment)
public class ScannerFragment extends SuperBaseFragment {

    @ViewById
    protected DragSortListView list;

    @ViewById
    protected View emptyItem;

    private ScannerAdapter adapter;

    public static Fragment instance() {
        return ScannerFragment_.builder().build();
    }

    @AfterViews
    protected void initViews() {
        adapter = new ScannerAdapter(getActivity());
        list.setAdapter(adapter);
        list.setEmptyView(emptyItem);

        setScanner();
    }

    private void forgetScanner() {
        getApp().getShopPref().scannerAddress().remove();
        getApp().getShopPref().scannerName().remove();
    }

    private void setScanner() {
        String scannerAddress = getApp().getShopPref().scannerAddress().get();
        String scannerName = getApp().getShopPref().scannerName().get();
        boolean scannerConfigured = !TextUtils.isEmpty(scannerAddress);

        adapter.clear();
        if (scannerConfigured)
            adapter.add(!TextUtils.isEmpty(scannerName) ? scannerName : scannerAddress);
    }

    @OptionsItem
    protected void actionSearchSelected() {
        FindDeviceFragment.show(getActivity(), findDisplayListener, Mode.SCANNER);
    }

    private FindDeviceListener findDisplayListener = new FindDeviceListener() {

        @Override
        public void onDeviceSelected() {
            setScanner();
        }

    };

    private class ScannerAdapter extends ArrayAdapter<String> implements DragSortListView.RemoveListener {

        public ScannerAdapter(Context context) {
            super(context, R.layout.device_list_item, android.R.id.text1);
        }

        @Override
        public void remove(int i) {
            AlertDialogFragment.show(
                    getActivity(),
                    DialogType.CONFIRM_NONE,
                    R.string.scanner_delete_warning_dialog_title,
                    getString(R.string.scanner_delete_warning_dialog_message),
                    R.string.btn_confirm,
                    new OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            forgetScanner();
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
