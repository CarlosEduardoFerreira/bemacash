package com.kaching123.tcr.activity;

import android.os.Bundle;

import org.androidannotations.annotations.EActivity;
import com.kaching123.tcr.R;
import com.telly.groundy.CallbacksManager;

/**
 * Created by gdubina on 08/11/13.
 */
@EActivity(R.layout.login_activity)
public class LoginActivity extends SuperBaseActivity{

    private CallbacksManager callbacksManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callbacksManager = CallbacksManager.init(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        callbacksManager.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        callbacksManager.onDestroy();
    }

    public CallbacksManager getCallbacksManager() {
        return callbacksManager;
    }
}
