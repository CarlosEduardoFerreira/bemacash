package com.kaching123.tcr.fragment.settings;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.display.DisplayWelcomeMessageCommand;
import com.kaching123.tcr.fragment.SuperBaseFragment;
import com.kaching123.tcr.fragment.dialog.AlertDialogFragment;
import com.kaching123.tcr.fragment.dialog.StyledDialogFragment;
import com.kaching123.tcr.fragment.dialog.WaitDialogFragmentWithCallback;
import com.kaching123.tcr.service.DisplayService;
import com.kaching123.tcr.service.ScaleService;
import com.kaching123.tcr.service.ScannerService;
import com.kaching123.tcr.util.ReceiverWrapper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

/**
 * Created by long.jiao on 8/14/2015.
 */
@EFragment(R.layout.settings_device_diagnose_fragment)
@OptionsMenu(R.menu.settings_device_diagnose_fragment)
public class DiagnoseFragment extends SuperBaseFragment implements DisplayService.IDisplayBinder {
    final static String[] DEVICE_ARRAY = {"Network","Printer","Pax","Drawer","Display","Scanner","Msr","Scale"};
    @ViewById
    protected ListView list;

    @ViewById
    protected View emptyItem;

    @ViewById
    protected EditText usbScannerInput;

    private ArrayAdapter<String> adapter;
    private ScaleService scaleService;
    private boolean scaleServiceBound;
    protected ServiceConnection scaleServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            scaleServiceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            scaleBinder = (ScaleService.ScaleBinder) service;
            scaleService = scaleBinder.getService();
            scaleServiceBound = true;
        }
    };
    private ScaleService.ScaleBinder scaleBinder;
    private boolean isUSBScanner;
    private ScannerService.ScannerBinder scannerBinder;

    public String getScannerRead() {
        return scannerRead;
    }

    public void setScannerRead(String scannerRead) {
        this.scannerRead = scannerRead;
    }

    private String scannerRead;

    public static Fragment instance() {
        return DiagnoseFragment_.builder().build();
    }

    @AfterViews
    protected void initViews() {
        final Context context = DiagnoseFragment.this.getActivity();
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, DEVICE_ARRAY);
        list.setAdapter(adapter);
        list.setEmptyView(emptyItem);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {
                // TODO Auto-generated method stub
                int itemPosition = position;
                switch (itemPosition){
                    case 0:
                        ConnectivityManager cm =
                                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

                        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                        boolean isConnected = activeNetwork != null &&
                                activeNetwork.isConnectedOrConnecting();
                        if(isConnected)
                            Toast.makeText(context,"Internet is connected",Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(context,"Internet is not connected, please check your settings",Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        TestPrinterFragment.show(getActivity());
                        break;
                    case 4:
                        FindDeviceFragment.show(getActivity(), findDisplayListener, FindDeviceFragment.Mode.DISPLAY);
                        task = new DisplayTask(itemPosition);
                        break;
                    case 5:
                        FindDeviceFragment.show(getActivity(), findScannerListener, FindDeviceFragment.Mode.SCANNER);
                        task = new DisplayTask(itemPosition);
                        break;
                    case 7:
                        FindDeviceFragment.show(getActivity(), findScaleListener, FindDeviceFragment.Mode.SCALE);
                        task = new DisplayTask(itemPosition);
                        break;
                }
            }
        });
    }

    private FindDeviceFragment.FindDeviceListener findScannerListener = new FindDeviceFragment.FindDeviceListener() {

        @Override
        public void onDeviceSelected() {
            bindToScannerService();
            task.execute();
        }

    };

    private FindDeviceFragment.FindDeviceListener findScaleListener = new FindDeviceFragment.FindDeviceListener() {

        @Override
        public void onDeviceSelected() {
            bindToScaleService();
            task.execute();
        }

    };

    private DisplayTask task;
    private FindDeviceFragment.FindDeviceListener findDisplayListener = new FindDeviceFragment.FindDeviceListener() {

        @Override
        public void onDeviceSelected() {
            task.execute();

        }

    };

    @Override
    public void startCommand(DisplayService.Command displayCommand) {
        if (displayBinder != null)
            displayBinder.startCommand(displayCommand);
    }

    @Override
    public void setDisplayListener(DisplayService.DisplayListener displayListener) {
        if (displayBinder != null)
            displayBinder.setDisplayListener(displayListener);
    }

    @Override
    public void tryReconnectDisplay() {
        if (displayBinder != null)
            displayBinder.tryReconnectDisplay();
    }

    private DisplayService.DisplayBinder displayBinder;
    private ServiceConnection displayServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            displayBinder = (DisplayService.DisplayBinder) binder;
            setDisplayListener(displayListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            displayBinder = null;
        }
    };

    private DisplayService.DisplayListener displayListener = new DisplayService.DisplayListener() {

        @Override
        public void onDisconnected() {
            if (getActivity().isFinishing() || getActivity().isDestroyed())
                return;

            AlertDialogFragment.showAlert(
                    getActivity(),
                    R.string.error_dialog_title,
                    getString(R.string.error_message_display_disconnected),
                    R.string.btn_try_again,
                    new StyledDialogFragment.OnDialogClickListener() {

                        @Override
                        public boolean onClick() {
                            tryReconnectDisplay();
                            return true;
                        }

                    }
            );
        }

        @Override
        public void onError() {
            if (getActivity().isFinishing() || getActivity().isDestroyed())
                return;

            Toast.makeText(getActivity(), R.string.error_message_display_command_error, Toast.LENGTH_SHORT).show();
        }

    };

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();

        unbindFromScaleService();
    }

    private void bindToDisplayService() {
        boolean displayConfigured = !TextUtils.isEmpty(getApp().getShopPref().displayAddress().get()); //Serial Port?
        if (displayConfigured)
            DisplayService.bind(getActivity(), displayServiceConnection);
    }

    private void unbindFromDisplayService() {
        if (displayBinder != null) {
            displayBinder = null;
            getActivity().unbindService(displayServiceConnection);
        }
    }

    private void bindToScaleService() {
        boolean displayConfigured = !TextUtils.isEmpty(getApp().getShopPref().scaleName().get()); //Serial Port?
        if (displayConfigured) {
            ScaleService.bind(getActivity(), scaleServiceConnection);
        }
    }

    private void unbindFromScaleService() {
        if (scaleBinder != null) {
            scaleBinder = null;
            getActivity().unbindService(scaleServiceConnection);
        }
    }

    private class DisplayTask extends AsyncTask<String, Void, Void> {
        int pos;
        private String scaleRead;

        public DisplayTask(int pos){
            this.pos = pos;
        }

        @Override
        protected Void doInBackground(String... params) {
                if(pos == 4)
                    startCommand(new DisplayWelcomeMessageCommand());
                else if(pos == 7){
//                    scaleRead = scaleService.readScale();
                    while(true){
                        if(scaleService != null && scaleServiceBound){
                            Logger.d(scaleServiceBound + " read = "+scaleService.readScale());
                            scaleRead = scaleService.readScale();
                            break;
                        }
                    }
                }else if(pos == 5){
                    while(true){
                        if(scannerRead != null && scannerRead != ""){
                            Logger.d("scannerRead = "+scannerRead);
                            break;
                        }
                    }
                }
            return null;
        }

        @Override
        protected void onPreExecute() {
            if (pos == 4)
                bindToDisplayService();
            else if(pos == 5){
                if(getUSBScanner()) {
                    DialogFragment fragment = WaitDialogFragmentWithCallback.showWithReturn(getActivity(),getString(R.string.wait_dialog_title));
                    return;
                }

            }
            WaitDialogFragmentWithCallback.show(getActivity(), getString(R.string.wait_dialog_title));
        }

        @Override
        protected void onPostExecute(Void none) {
            String confirmStr = null;
            if (pos == 4) {
                confirmStr = getString(R.string.confirm_display_title);
                unbindFromDisplayService();
            }
            else if(pos == 7){
                confirmStr = String.format(getString(R.string.confirm_scale_title),scaleRead);
            }
            else if(pos == 5){
                confirmStr = String.format(getString(R.string.confirm_scanner_title),scannerRead);
                scannerRead = null;
                unbindFromScannerService();
            }
            AlertDialogFragment.show(getActivity(), AlertDialogFragment.DialogType.CONFIRM,
                    R.string.btn_confirm,
                    confirmStr,
                    R.string.btn_yes,
                    R.string.btn_retry,
                    true,
                    new StyledDialogFragment.OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            return true;
                        }
                    },
                    new StyledDialogFragment.OnDialogClickListener() {
                        @Override
                        public boolean onClick() {
                            task = new DisplayTask(pos);
                            task.execute();
                            return true;
                        }
                    },
                    null
            );
            WaitDialogFragmentWithCallback.hide(getActivity());
        }
    }

    private ScannerService.ScannerListener scannerListener = new ScannerService.ScannerListener() {

        @Override
        public void onDisconnected() {
            Logger.d("ScannerBaseActivity: scannerListener: onDisconnected()");
            if (getActivity().isFinishing() || getActivity().isDestroyed()) {
                Logger.d("ScannerBaseActivity: scannerListener: onDisconnected(): ignore and exit - activity is finishing");
                return;
            }

            AlertDialogFragment.showAlert(
                    getActivity(),
                    R.string.error_dialog_title,
                    getString(R.string.error_message_scanner_disconnected),
                    R.string.btn_try_again,
                    new StyledDialogFragment.OnDialogClickListener() {

                        @Override
                        public boolean onClick() {
                            tryReconnectScanner();
                            return true;
                        }

                    }
            );
        }

        @Override
        public void onBarcodeReceived(String barcode) {
            Logger.d("ScannerBaseActivity: scannerListener: onBarcodeReceived()" + barcode);
            if (getActivity().isFinishing() || getActivity().isDestroyed()) {
                Logger.d("ScannerBaseActivity: scannerListener: onBarcodeReceived(): ignore and exit - activity is finishing");
                return;
            }
        }

    };

    public void tryReconnectScanner() {
        Logger.d("ScannerBaseActivity: tryReconnectScanner()");
        if (scannerBinder != null)
            scannerBinder.tryReconnectScanner();
        else
            Logger.d("ScannerBaseActivity: tryReconnectScanner(): failed - not binded!");
    }

    public void disconnectScanner() {
        Logger.d("ScannerBaseActivity: disconnectScanner()");
        if (scannerBinder != null)
            scannerBinder.disconnectScanner();
        else
            Logger.d("ScannerBaseActivity: disconnectScanner(): failed - not binded!");
    }

    private void bindToScannerService() {
        Logger.d("ScannerBaseActivity: bindToScannerService()"+getApp().getShopPref().scannerAddress().get());
        boolean scannerConfigured = !TextUtils.isEmpty(getApp().getShopPref().scannerAddress().get());

        if (scannerConfigured) {
            if (getApp().getShopPref().scannerAddress().get().equalsIgnoreCase(FindDeviceFragment.USB_SCANNER_ADDRESS))
                setUSBScanner(true);
            else if(!getApp().getShopPref().scannerAddress().get().equalsIgnoreCase(FindDeviceFragment.SEARIL_PORT_SCANNER_ADDRESS))
                ScannerService.bind(getActivity(), scannerServiceConnection);
        } else
            Logger.d("ScannerBaseActivity: bindToScannerService(): failed - scanner is not configured!");
    }

    private void setUSBScanner(boolean flag) {
        isUSBScanner = flag;
    }

    private boolean getUSBScanner()
    {
        return isUSBScanner;
    }

    private void unbindFromScannerService() {
        Logger.d("ScannerBaseActivity: unbindFromScannerService()");
        if (scannerBinder != null) {
            scannerBinder = null;
            getActivity().unbindService(scannerServiceConnection);
        } else {
            Logger.d("ScannerBaseActivity: unbindFromScannerService(): ignore and exit - not binded");
        }
    }

    private ServiceConnection scannerServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            Logger.d("ScannerBaseActivity: scannerServiceConnection: onServiceConnected()");
            scannerBinder = (ScannerService.ScannerBinder) binder;
            setScannerListener(scannerListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            Logger.d("ScannerBaseActivity: scannerServiceConnection: onServiceDisconnected()");
            scannerBinder = null;
        }
    };

    private void setScannerListener(ScannerService.ScannerListener scannerListener) {
        Logger.d("ScannerService: setScannerListener(): scannerListener = " + scannerListener);
        this.scannerListener = scannerListener;
    }

    public void receivedScannerCallback(String barcode){
            scannerRead = barcode;
      }

}
