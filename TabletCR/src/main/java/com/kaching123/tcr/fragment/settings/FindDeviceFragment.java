package com.kaching123.tcr.fragment.settings;

import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.bematechus.bemaUtils.PortInfo;
import com.kaching123.display.USBDiplayPrinter;
import com.kaching123.display.scale.BemaScale;
import com.kaching123.pos.USBPrinter;
import com.kaching123.tcr.BuildConfig;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.device.PrinterCommand;
import com.kaching123.tcr.fragment.dialog.DialogUtil;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.model.DeviceModel;
import com.kaching123.usb.SysBusUsbDevice;
import com.kaching123.usb.SysBusUsbManager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ViewById;

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
    public static String USB_HID_SCANNER_NAME = "USB SCANNER (KeyBoard)";
    public static String USB_HID_SCANNER_ADDRESS = "USB SCANNER (KeyBoard)";
    public static String USB_SCANNER_NAME = "USB SCANNER";
    public static String USB_SCANNER_ADDRESS = "USB SCANNER";
    public static String USB_DISPLAY = "LDX1000";
    public static String SEARIL_PORT_SCANNER_ADDRESS = "Integrated Scanner";
    public static String SEARIL_PORT_SCANNER_NAME = "Integrated Scanner";
    public static String USB_MSR_VID = "1667";
    public static String USB_MSR_PID = "0009";
    public static String USB_SCANNER_VID = "0000";
    public static String USB_SCANNER_PID = "5710";
    public static String[] MODEL_NUMBER_ARRAY = {"POSLAB","FREESCALE"};
    public static String MODEL_NUMBER_OLD = "EcolMini";
    public static String MODEL_NUMBER_NEW = "8010";
    @FragmentArg
    protected Mode mode;

    public enum Mode {
        DISPLAY, SCANNER, USBMSR, SCALE
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
        if(mode == Mode.DISPLAY) {
           return  R.string.find_display_title;
        }else if(mode == Mode.SCANNER){
            return R.string.find_scanner_title;
        }else if(mode == Mode.USBMSR){
            return R.string.find_msr_title;
        }else if(mode == Mode.SCALE){
            return R.string.find_scale_title;
        }
        return R.string.find_display_title;
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
        } else if (mode == Mode.SCALE) {
            progressLabel.setText(R.string.find_scale_progress);
            emptyView.setText(R.string.find_scale_empty);
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
        } else if (mode == Mode.SCALE) {
            storeScale(device);
        }

        if (findDeviceListener != null)
            findDeviceListener.onDeviceSelected();
        dismiss();
    }

    private boolean isAIO() {
        return Arrays.asList(MODEL_NUMBER_ARRAY).contains(Build.MANUFACTURER.toUpperCase());
    }

    public String getDeviceName() {
        final String manufacturer = Build.MANUFACTURER;
        final String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        if (manufacturer.equalsIgnoreCase("HTC")) {
            // make sure "HTC" is fully capitalized.
            return "HTC " + model;
        }
        return capitalize(manufacturer) + " " + model;
    }

    private String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        final char[] arr = str.toCharArray();
        boolean capitalizeNext = true;
        String phrase = "";
        for (final char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase += Character.toUpperCase(c);
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase += c;
        }
        return phrase;
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

    private void storeScale(DeviceModel scale) {
        getApp().getShopPref().scaleName().put(scale.getName());
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
                case SCALE:
                    devices = getScaleDevice();
//                    devices = getDevices(bluetoothDevices);
                    break;
                default:
                    break;
            }

            return devices;
        }

        private boolean searchForUsbDisplay()
        {
            UsbManager manager = (UsbManager) getActivity().getSystemService(Context.USB_SERVICE);
            try
            {

                PendingIntent mPermissionIntent;

                mPermissionIntent = PendingIntent.getBroadcast(getActivity(), 0, new Intent(PrinterCommand.ACTION_USB_PERMISSION), 0);

                USBDiplayPrinter display = new USBDiplayPrinter(USBDiplayPrinter.LDX1000_PID,USBDiplayPrinter.LDX1000_VID,manager,null);
                return display.findPrinter(true);
            }
            catch (Exception e) {
                Logger.e("Discovery USB printers ", e);
            }
            return false;
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
            if (isAIO())
                if (mode == Mode.DISPLAY) {
                    devices.add(new DeviceModel(SERIAL_PORT, SERIAL_PORT));
                    devices.add(new DeviceModel("COM2","COM2"));
                    devices.add(new DeviceModel("COM1","COM1"));
                    if(searchForUsbDisplay())
                        devices.add(new DeviceModel(USB_DISPLAY,USB_DISPLAY));
                }
                else if(mode == Mode.SCALE){
                    devices.add(new DeviceModel("COM2","COM2"));
                    devices.add(new DeviceModel("COM1","COM1"));
                }
                else {
                    devices.add(new DeviceModel(SEARIL_PORT_SCANNER_ADDRESS, SEARIL_PORT_SCANNER_NAME));
//                    if (checkUsb(USB_SCANNER_VID, USB_SCANNER_PID))
//                        devices.add(new DeviceModel(USB_SCANNER_NAME, USB_SCANNER_ADDRESS));
                    devices.add(new DeviceModel(USB_SCANNER_NAME, USB_SCANNER_ADDRESS));
//                    devices.add(new DeviceModel(USB_HID_SCANNER_NAME,USB_HID_SCANNER_ADDRESS));
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

        private Set<DeviceModel> getScaleDevice() {
            Set<DeviceModel> devices = new HashSet<DeviceModel>();
            for(int i = 1; i <= 2; i++) {
                String portName = "COM" + i;
                PortInfo info = BemaScale.scalePortInfo();
                info.setPortName(portName);
                BemaScale scale = new BemaScale(info);
                int state = scale.open();
                Logger.d("state = "+state);
                if( state >= 0) {
                    Logger.d("Scale Connected to " + info.getPortName());
                    devices.add(new DeviceModel(info.getPortName(), info.getPortName()));
                }
                scale.close();
            }

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
