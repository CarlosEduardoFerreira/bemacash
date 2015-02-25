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

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.ItemClick;
import com.googlecode.androidannotations.annotations.ViewById;
import com.kaching123.tcr.BuildConfig;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.model.DeviceModel;
import com.kaching123.usb.SysBusUsbDevice;
import com.kaching123.usb.SysBusUsbManager;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
    public static final String INTEGRATED_DISPLAYER = "Integrated Customer Display";
    public static final String SERIAL_PORT = "Integrated Customer Display";
    public static String USB_MSR_NAME = "Integrated MSR";
    public static String USB_SCANNER_NAME = "USB SCANNER";
    public static String USB_SCANNER_ADDRESS = "USB SCANNER";
    public static String SEARIL_PORT_SCANNER_ADDRESS = "Integrated Scanner";
    public static String SEARIL_PORT_SCANNER_NAME = "Integrated Scanner";
    public static String USB_MSR_VID = "1667";
    public static String USB_MSR_PID = "0009";
    public static String USB_SCANNER_VID = "0000";
    public static String USB_SCANNER_PID = "5710";
    @FragmentArg
    protected Mode mode;

    public enum Mode {
        DISPLAY, SCANNER, USBMSR
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
        } else if (mode == Mode.SCANNER) {
            progressLabel.setText(R.string.find_scanner_progress);
            emptyView.setText(R.string.find_scanner_empty);
        } else if (mode == Mode.USBMSR) {
            progressLabel.setText(R.string.find_msr_progress);
            emptyView.setText(R.string.find_msr_empty);
        }

        adapter = new DeviceAdapter(getActivity());
        listView.setAdapter(adapter);
        new GetDevicesTask().execute();
    }

    @ItemClick
    protected void listViewItemClicked(DeviceModel device) {
        if (mode == Mode.DISPLAY) {
            storeDisplay(device);
        } else if (mode == Mode.SCANNER) {
            storeScanner(device);
        } else if (mode == Mode.USBMSR) {
            storeUsbMsr(device);
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

    private void storeUsbMsr(DeviceModel usbMsr) {
        getApp().getShopPref().usbMSRName().put(usbMsr.getAddress());
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
//            if (adapter == null || !adapter.isEnabled()) {
//                return null;
//            }

            Set<BluetoothDevice> bluetoothDevices = adapter.getBondedDevices();
            Set<DeviceModel> devices = null;
            switch (mode) {
                case DISPLAY:
                case SCANNER:
                    devices = getDevices(bluetoothDevices);
                    break;
                case USBMSR:
                    devices = getUsbMsrDevices();
                    break;
                default:
                    break;
            }

            return devices;
        }

        private Collection<DeviceModel> getEmulatedDevice() {
            DeviceModel emulatedDevice;
            if (mode == Mode.DISPLAY) {
                emulatedDevice = new DeviceModel(EMULATED_DISPLAY_NAME, EMULATED_DISPLAY_ADDRESS);
            } else {
                emulatedDevice = new DeviceModel(EMULATED_SCANNER_NAME, EMULATED_SCANNER_ADDRESS);
            }
            return Arrays.asList(emulatedDevice);
        }

        private Set<DeviceModel> getDevices(Set<BluetoothDevice> bluetoothDevices) {
            Set<DeviceModel> devices = new HashSet<DeviceModel>();
            boolean useConstraint = mode == Mode.DISPLAY;
            if (mode == Mode.DISPLAY)
                devices.add(new DeviceModel(SERIAL_PORT, SERIAL_PORT));
            else {
                devices.add(new DeviceModel(SEARIL_PORT_SCANNER_ADDRESS, SEARIL_PORT_SCANNER_NAME));
                if (checkUsb(USB_SCANNER_VID, USB_SCANNER_PID))
                    devices.add(new DeviceModel(USB_SCANNER_NAME, USB_SCANNER_ADDRESS));
            }
            for (BluetoothDevice device : bluetoothDevices) {
                if (useConstraint && !checkConstraint(device))
                    continue;

                devices.add(new DeviceModel(device.getName(), device.getAddress()));
            }
            return devices;
        }

        private Set<DeviceModel> getUsbMsrDevices() {
            Set<DeviceModel> devices = new HashSet<DeviceModel>();
            if (checkUsb(USB_MSR_VID, USB_MSR_PID))
                devices.add(new DeviceModel(USB_MSR_NAME, USB_MSR_NAME));
            return devices;
        }

        private boolean checkUsb(String VID, String PID) {
            SysBusUsbManager mUsbManagerLinux = new SysBusUsbManager();
            HashMap<String, SysBusUsbDevice> mLinuxUsbDeviceList = mUsbManagerLinux.getUsbDevices();
            Iterator<SysBusUsbDevice> deviceIterator = mLinuxUsbDeviceList.values().iterator();
            while (deviceIterator.hasNext()) {
                SysBusUsbDevice device = deviceIterator.next();
                if (device.getVID().equalsIgnoreCase(VID) && device.getPID().equalsIgnoreCase(PID)) {
                    return true;
                }
            }
            return false;
        }

        private boolean checkConstraint(BluetoothDevice device) {
            if (mode != Mode.DISPLAY)
                return true;

            if (device == null || device.getName() == null)
                return false;
            return device.getName().contains(DISPLAY_DEVICE_NAME_CONSTRAINT);
        }

        @Override
        protected void onPostExecute(Collection<DeviceModel> devices) {
            if (getActivity() == null)
                return;

            progressBlock.setVisibility(View.GONE);
            listView.setEmptyView(emptyView);

            adapter.clear();
            if (devices != null && !devices.isEmpty()) {
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
            TextView textView = (TextView) super.getView(position, convertView, parent);
            String name = getItem(position).getName();
            textView.setText(TextUtils.isEmpty(name) ? getItem(position).getAddress() : name);
            return textView;
        }
    }

    public interface FindDeviceListener {

        public void onDeviceSelected();

    }

}
