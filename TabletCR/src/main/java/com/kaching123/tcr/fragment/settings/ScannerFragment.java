package com.kaching123.tcr.fragment.settings;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.fragment.SuperBaseFragment;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment.DialogType;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment.OnDialogClickListener;
import com.kaching123.tcr.fragment.settings.FindDeviceFragment.FindDeviceListener;
import com.kaching123.tcr.fragment.settings.FindDeviceFragment.Mode;
import com.mobeta.android.dslv.DragSortListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pkabakov on 28.02.14.
 */
@EFragment(R.layout.settings_scanner_fragment)
@OptionsMenu(R.menu.settings_scanner_fragment)
public class ScannerFragment extends SuperBaseFragment {

    private static final String TAG = "ScannerFragment";
    @ViewById
    protected DragSortListView list;

    @ViewById
    protected View emptyItem;

    private ScannerAdapter adapter;
    private UsbSerialPort sPort;
    private UsbManager mUsbManager;
    private final static String USB_SCANNER_NAME = "USB SCANNER";

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

        Log.d("BemaCarl4","ScannerFragment.setScanner.scannerAddress: " + scannerAddress);
        Log.d("BemaCarl4","ScannerFragment.setScanner.scannerName: " + scannerName);
        Log.d("BemaCarl4","ScannerFragment.setScanner.scannerConfigured: " + scannerConfigured);

        adapter.clear();
        if(scannerName.equalsIgnoreCase(USB_SCANNER_NAME)) {
            if(sPort == null) {
                sPort = getPort();
                Log.d("BemaCarl4","ScannerFragment.setScanner.sPort: " + sPort);
                if(sPort == null){
                    Logger.d("Port = "+sPort);
                    AlertDialogFragment.showAlert(
                            getActivity(),
                            R.string.error_dialog_title,
                            getString(R.string.error_message_scanner_not_attached),
                            R.string.btn_try_again,
                            new OnDialogClickListener() {

                                @Override
                                public boolean onClick() {
                                    setScanner();
                                    return true;
                                }

                            }
                    );
                    return;
                }
            }
            final UsbDevice device = sPort.getDriver().getDevice();
            if (!mUsbManager.hasPermission(device)) {
                PendingIntent mPermissionIntent = PendingIntent.getBroadcast(getActivity(), 0, new Intent("com.android.example.USB_PERMISSION"), 0);
                mUsbManager.requestPermission(device, mPermissionIntent);
                AlertDialogFragment.showAlert(
                        getActivity(),
                        R.string.error_dialog_title,
                        getString(R.string.error_message_scanner_no_permission),
                        R.string.btn_try_again,
                        new OnDialogClickListener() {

                            @Override
                            public boolean onClick() {
                                setScanner();
                                return true;
                            }

                        }
                );
                return;
            }
        }
        if (scannerConfigured)
            adapter.add(!TextUtils.isEmpty(scannerName) ? scannerName : scannerAddress);
    }

    public UsbSerialPort getPort(){
        Log.d("BemaCarl4","ScannerService.ScannerFragment.getPort");
        mUsbManager = (UsbManager) getActivity().getSystemService(Context.USB_SERVICE);
        final List<UsbSerialDriver> drivers =
                UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);
        Log.d("BemaCarl4","ScannerService.ScannerFragment.getPort.drivers: " + drivers.size());
        final List<UsbSerialPort> result = new ArrayList<UsbSerialPort>();
        for (final UsbSerialDriver driver : drivers) {
            final List<UsbSerialPort> ports = driver.getPorts();
            Log.d(TAG, String.format("+ %s: %s port%s",
                    driver, Integer.valueOf(ports.size()), ports.size() == 1 ? "" : "s"));
            result.addAll(ports);
        }
        for(UsbSerialPort port: result){
            final UsbSerialDriver driver = port.getDriver();
            final UsbDevice device = driver.getDevice();
            
            Log.d("BemaCarl4","ScannerService.ScannerFragment.getPort.device.getInterfaceCount(): " + device.getInterfaceCount());
            Log.d("BemaCarl4","ScannerService.ScannerFragment.getPort.device.getInterface(0).getInterfaceClass(): " + device.getInterface(0).getInterfaceClass());
            if(device.getInterface(0).getInterfaceClass() == 2 || device.getInterface(0).getInterfaceClass() == 3){
                return port;
            }
        }
        return null;
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
