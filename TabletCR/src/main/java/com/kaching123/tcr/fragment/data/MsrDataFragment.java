package com.kaching123.tcr.fragment.data;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.AtomicUpload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import IDTech.MSR.XMLManager.StructConfigParameters;
import IDTech.MSR.uniMag.UniMagTools.uniMagReaderToolsMsg;
import IDTech.MSR.uniMag.UniMagTools.uniMagSDKTools;
import IDTech.MSR.uniMag.uniMagReader;
import IDTech.MSR.uniMag.uniMagReaderMsg;

/**
 * Created by gdubina on 09/12/13.
 */
public class MsrDataFragment extends Fragment {

    public static final String FTAG = "MsrDataFragment";

    public static enum State{CONNECTING, CONNECTED, DISCONNECTED}

    private uniMagReader myUniMagReader = null;
    private uniMagSDKTools firmwareUpdateTool = null;

    private UniMagReaderCallback uniMagReaderCallback;
    private State state = State.DISCONNECTED;

    private SwipeCallback swipeCallback;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logger.d("[MSR] onActivityCreated %s", getActivity());
        uniMagReaderCallback = new UniMagReaderCallback();
    }

    @Override
    public void onStart() {
        Logger.d("[MSR] onStart %s", getActivity());
        super.onStart();
        new AsyncInitTask().execute();
    }

    @Override
    public void onStop() {
        Logger.d("[MSR] onStop %s", getActivity());
        release();
        super.onStop();
    }

    public void releaseNow() {
        Logger.d("[MSR] releaseNow!!! %s", getActivity());
        release();
    }

    private void release(){
        Logger.d("[MSR] release %s", getActivity());
        if (myUniMagReader != null) {
            myUniMagReader.unregisterListen();
            myUniMagReader.release();
            myUniMagReader = null;
        }
    }

    private class AsyncInitTask extends AsyncTask<Void, Void, String>{

        @Override
        protected void onPreExecute() {
            Logger.d("[MSR] onPreExecute %s", getActivity());
            release();
        }

        @Override
        protected String doInBackground(Void... params) {

            if(!new AtomicUpload().hasInternetConnection()){
                state = State.DISCONNECTED;
                return null;
            }

            Logger.d("[MSR] doInBackground %s", getActivity());
            try{
                myUniMagReader = new uniMagReader(uniMagReaderCallback, getActivity());
                myUniMagReader.setVerboseLoggingEnable(true);
                myUniMagReader.registerListen();

                String fileNameWithPath = getConfigurationFileFromRaw();
                if (!isFileExist(fileNameWithPath)) {
                    fileNameWithPath = null;
                }
                /////////////////////////////////////////////////////////////////////////////////
                // Network operation is prohibited in the UI Thread if target API is 11 or above.
                // If target API is 11 or above, please use AsyncTask to avoid errors.
                myUniMagReader.setXMLFileNameWithPath(fileNameWithPath);
                myUniMagReader.loadingConfigurationXMLFile(true);
                /////////////////////////////////////////////////////////////////////////////////

                //Initializing SDKTool for firmware update
                firmwareUpdateTool = new uniMagSDKTools(uniMagReaderCallback, getActivity());
                firmwareUpdateTool.setUniMagReader(myUniMagReader);
                myUniMagReader.setSDKToolProxy(firmwareUpdateTool.getSDKToolProxy());
                if(!myUniMagReader.isReaderConnected()){
                    myUniMagReader.connect();
                }
            }catch (Exception e){
                state = State.DISCONNECTED;
                Logger.e("MSR. can't create msr classes", e);
            }
            return null;
        }
    }

    private String getConfigurationFileFromRaw() {
        return getXMLFileFromRaw("idt_unimagcfg_default.xml");
    }

    private boolean isFileExist(String path) {
        if (TextUtils.isEmpty(path))
            return false;
        File file = new File(path);
        return file.exists();
    }

    private String getXMLFileFromRaw(String fileName) {
        //the target filename in the application path
        String fileNameWithPath = null;
        fileNameWithPath = fileName;

        try {
            InputStream in = getResources().openRawResource(R.raw.idt_unimagcfg_default);
            int length = in.available();
            byte[] buffer = new byte[length];
            in.read(buffer);
            in.close();
            getActivity().deleteFile(fileNameWithPath);
            FileOutputStream fout = getActivity().openFileOutput(fileNameWithPath, Context.MODE_PRIVATE);
            fout.write(buffer);
            fout.close();

            // to refer to the application path
            File fileDir = getActivity().getFilesDir();
            fileNameWithPath = fileDir.getParent() + java.io.File.separator + fileDir.getName();
            fileNameWithPath += java.io.File.separator + "idt_unimagcfg_default.xml";

        } catch (Exception e) {
            e.printStackTrace();
            fileNameWithPath = null;
        }
        return fileNameWithPath;
    }

    public boolean swipeCard(SwipeCallback swipeCallback){
        if(myUniMagReader == null || state != State.CONNECTED)
            return false;

        this.swipeCallback = swipeCallback;
         if(!myUniMagReader.startSwipeCard()){
            this.swipeCallback = null;
            return false;
        }
        return true;
    }

    public void stopSwipeCard(){
        if(myUniMagReader == null || state != State.CONNECTED)
            return;

        swipeCallback = null;
        myUniMagReader.stopSwipeCard();
    };

    public State getState() {
        return state;
    }

    public class UniMagReaderCallback implements uniMagReaderMsg, uniMagReaderToolsMsg {

        // implementing a method onReceiveMsgToConnect, defined in uniMagReaderMsg interface
        // receiving a message when SDK starts powering up the UniMag device
        @Override
        public void onReceiveMsgToConnect() {
            state = State.CONNECTING;
        }

        // implementing a method onReceiveMsgConnected, defined in uniMagReaderMsg interface
        // receiving a message that the uniMag device has been connected
        @Override
        public void onReceiveMsgConnected() {
            state = State.CONNECTED;
        }

        // implementing a method onReceiveMsgDisconnected, defined in uniMagReaderMsg interface
        // receiving a message that the uniMag device has been disconnected
        @Override
        public void onReceiveMsgDisconnected() {
            state = State.DISCONNECTED;
        }

        // implementing a method onReceiveMsgTimeout, defined in uniMagReaderMsg inteface
        // receiving a timeout message for powering up or card swipe
        @Override
        public void onReceiveMsgTimeout(String s) {
            if(state == State.CONNECTING){
                state = State.DISCONNECTED;
            }
        }

        // implementing a method onReceiveMsgToSwipeCard, defined in uniMagReaderMsg interface
        // receiving a message when SDK starts recording, then application should ask user to swipe a card
        @Override
        public void onReceiveMsgToSwipeCard() {
            // ready to swipe
            if(swipeCallback == null)
                return;
            swipeCallback.onSwipeReady();
        }

        // implementing a method onReceiveMsgCommandResult, defined in uniMagReaderMsg interface
        // receiving a message when SDK is able to parse a response for commands from the reader
        @Override
        public void onReceiveMsgCommandResult(int i, byte[] bytes) {

        }
        // implementing a method onReceiveMsgCardData, defined in uniMagReaderMsg interface
        // receiving card data here
        @Override
        public void onReceiveMsgCardData(byte b, byte[] cardDataBytes) {
            //swipe result here
            if(swipeCallback == null)
                return;
            byte[] msrData = new byte[cardDataBytes.length];
            System.arraycopy(cardDataBytes, 0, msrData, 0, cardDataBytes.length);
            CardData data = new CardData(msrData);
            swipeCallback.onSwipeCompleted(data);
        }

        // implementing a method onReceiveMsgProcessingCardData, defined in uniMagReaderMsg interface
        // receiving a message when SDK detects data coming from the UniMag reader
        // The main purpose is to give early notification to user to wait until SDK finishes processing card data.
        @Override
        public void onReceiveMsgProcessingCardData() {
            //swipe progress here
        }

        // implementing a method onReceiveMsgFailureInfo, defined in uniMagReaderMsg interface
        // receiving a message when SDK could not find a profile of the phone
        @Override
        public void onReceiveMsgFailureInfo(int index, String error) {
            //swipe failed
            if(swipeCallback == null)
                return;
            swipeCallback.onSwipeError(index, error);
        }

        @Override
        public boolean getUserGrant(int type, String s) {
            boolean getUserGranted = false;
            switch(type)
            {
                case uniMagReaderMsg.typeToPowerupUniMag:
                    //pop up dialog to get the user grant
                    getUserGranted = true;
                    break;
                case uniMagReaderMsg.typeToUpdateXML:
                    //pop up dialog to get the user grant
                    getUserGranted = true;
                    break;
                case uniMagReaderMsg.typeToOverwriteXML:
                    //pop up dialog to get the user grant
                    getUserGranted = true;
                    break;
                case uniMagReaderMsg.typeToReportToIdtech:
                    //pop up dialog to get the user grant
                    getUserGranted = true;
                    break;
                default:
                    getUserGranted = false;
                    break;
            }
            return getUserGranted;
        }

        // this method has been depricated, and will not be called in this version of SDK.
        @Override
        @Deprecated
        public void onReceiveMsgSDCardDFailed(String s) {/*ignore*/}

        // implementing a method onReceiveMsgAutoConfigProgress, defined in uniMagReaderMsg interface
        // receiving a message of Auto Config progress
        @Override
        public void onReceiveMsgAutoConfigProgress(int i) {/*ignore*/}

        @Override
        public void onReceiveMsgAutoConfigProgress(int i, double v, String s) {/*ignore*/}

        @Override
        public void onReceiveMsgAutoConfigCompleted(StructConfigParameters structConfigParameters) {/*ignore*/}

        @Override
        public void onReceiveMsgUpdateFirmwareProgress(int i) {/*ignore*/}

        @Override
        public void onReceiveMsgUpdateFirmwareResult(int i) {/*ignore*/}

        @Override
        public void onReceiveMsgChallengeResult(int i, byte[] bytes) {/*ignore*/}
    }

    public static interface SwipeCallback{
        void onSwipeReady();
        void onSwipeCompleted(CardData result);
        void onSwipeError(int index, String error);
    }

    public static MsrDataFragment newInstance(){
        return new MsrDataFragment();
    }
}
