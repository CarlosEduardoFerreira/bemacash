package com.kaching123.tcr;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by teli on 6/4/2015.
 */
public class UpdateObserver implements Observer {

    private UpdateObserverListener listener;

    public UpdateObserver(AutoUpdateApk autoUpdateApk) {
        autoUpdateApk.addObserver(this);
    }

    public void setListener(UpdateObserverListener listener) {
        this.listener = listener;
    }

    @Override
    public void update(Observable observable, Object o) {
        listener.onUpdate(observable, o);
    }

    public interface UpdateObserverListener {
        void onUpdate(Observable observable, Object o);
    }
}
