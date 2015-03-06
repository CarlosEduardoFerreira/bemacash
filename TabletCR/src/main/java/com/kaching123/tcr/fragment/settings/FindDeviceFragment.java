package com.kaching123.tcr.fragment.settings;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ViewById;
import com.kaching123.tcr.BuildConfig;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.model.DeviceModel;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by pkabakov on 28.02.14.
 */
@EFragment
public class FindDeviceFragment extends StyledDialogFragment {

    private static final String DIALOG_NAME = FindDeviceFragment.class.getSimpleName();

    @ViewById
    protected ListView listView;

    @ViewById(android.R.id.empty)
    protected TextView emptyView;

    @ViewById
    protected View progressBlock;

    @ViewById
    protected TextView progressLabel;

    private DeviceAdapter adapter;

    protected FindDeviceListener findDeviceListener;

    @FragmentArg
    protected Mode mode;

    public enum Mode {
        DISPLAY, SCANNER
    }

    public static void show(FragmentActivity activity, FindDeviceListener findDeviceListener, Mode mode) {
        FindDeviceFragment fragment = FindDeviceFragment_.builder().mode(mode).build();
        fragment.setFindDeviceListener(findDeviceListener);
        DialogUtil.show(activity, DIALOG_NAME, fragment);
    }

    public void setFindDeviceListener(FindDeviceListener findDeviceListener) {
        this.findDeviceListener = findDeviceListener;
    }

    @Override
    protected int getDialogContentLayout() {
        return R.layout.settings_find_device_fragment;
    }

    @Override
    protected int getDialogTitle() {
        return mode == Mode.DISPLAY ? R.string.find_display_title : R.string.find_scanner_title;
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
    protected boolean hasPositiveButton() {
        return false;
    }

    @Override
    protected OnDialogClickListener getPositiveButtonListener() {
        return null;
    }

    @AfterViews
    protected void initViews() {
        if (mode == Mode.DISPLAY) {
            progressLabel.setText(R.string.find_display_progress);
            emptyView.setText(R.string.find_display_empty);
        } else {
            progressLabel.setText(R.string.find_scanner_progress);
            emptyView.setText(R.string.find_scanner_empty);
        }

        adapter = new DeviceAdapter(getActivity());
        listView.setAdapter(adapter);
        new GetDevicesTask().execute();
    }

    @ItemClick
    protected void listViewItemClicked(DeviceModel device){
        if (mode == Mode.DISPLAY) {
            storeDisplay(device);
        } else {
            storeScanner(device);
        }

        if (findDeviceListener != null)
            findDeviceListener.onDeviceSelected();
        dismiss();
    }

    private void storeDisplay(DeviceModel display) {
        getApp().getShopPref().displayAddress().put(display.getAddress());
        getApp().getShopPref().displayName().put(display.getName());
    }

    private void storeScanner(DeviceModel scanner) {
        getApp().getShopPref().scannerAddress().put(scanner.getAddress());
        getApp().getShopPref().scannerName().put(scanner.getName());
    }

    private class GetDevicesTask extends AsyncTask<Void, Void, Collection<DeviceModel>> {

        private static final String DISPLAY_DEVICE_NAME_CONSTRAINT = "LCI Display";

        private static final String EMULATED_DISPLAY_NAME = "LCI Display EMULATED";
        private static final String EMULATED_DISPLAY_ADDRESS = "EMULATED_DISPLAY_ADDRESS";
        private static final String EMULATED_SCANNER_NAME = "Barcode Scanner EMULATED";
        private static final String EMULATED_SCANNER_ADDRESS = "EMULATED_SCANNER_ADDRESS";

        private boolean isEmulate() {
            return !BuildConfig.SUPPORT_PRINTER;
        }

        @Override
        protected void onPreExecute() {
            progressBlock.setVisibility(View.VISIBLE);
        }

        @Override
        protected Collection<DeviceModel> doInBackground(Void... params) {
            if (isEmulate()) {
                return getEmulatedDevice();
            }

            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter == null || !adapter.isEnabled()) {
                return null;
            }
            Set<BluetoothDevice> bluetoothDevices = adapter.getBondedDevices();
            return getDevices(bluetoothDevices);
        }

        private Collection<DeviceModel> getEmulatedDevice() {
            DeviceModel emulatedDevice;
            if (mode == Mode.DISPLAY ) {
                emulatedDevice = new DeviceModel(EMULATED_DISPLAY_NAME, EMULATED_DISPLAY_ADDRESS);
            } else {
                emulatedDevice = new DeviceModel(EMULATED_SCANNER_NAME, EMULATED_SCANNER_ADDRESS);
            }
            return Arrays.asList(emulatedDevice);
        }

        private Set<DeviceModel> getDevices(Set<BluetoothDevice> bluetoothDevices) {
            Set<DeviceModel> devices = new HashSet<DeviceModel>();
            boolean useConstraint = mode == Mode.DISPLAY;
            for(BluetoothDevice device: bluetoothDevices) {
                if (useConstraint && !checkConstraint(device))
                    continue;

                devices.add(new DeviceModel(device.getName(), device.getAddress()));
            }
            return devices;
        }

        private boolean checkConstraint(BluetoothDevice device) {
            if (mode != Mode.DISPLAY)
                return true;

            return device.getName().contains(DISPLAY_DEVICE_NAME_CONSTRAINT);
        }

        @Override
        protected void onPostExecute(Collection<DeviceModel> devices) {
            if (getActivity() == null)
                return;

            progressBlock.setVisibility(View.GONE);
            listView.setEmptyView(emptyView);

            adapter.clear();
            if(devices != null && !devices.isEmpty()){
                adapter.addAll(devices);
            }
        }
    }

    public static class DeviceAdapter extends ArrayAdapter<DeviceModel> {

        public DeviceAdapter(Context context) {
            super(context, R.layout.device_find_list_item);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView = (TextView)super.getView(position, convertView, parent);
            String name = getItem(position).getName();
            textView.setText(TextUtils.isEmpty(name) ? getItem(position).getAddress() : name);
            return textView;
        }
    }

    public interface FindDeviceListener {

        public void onDeviceSelected();

    }

}
