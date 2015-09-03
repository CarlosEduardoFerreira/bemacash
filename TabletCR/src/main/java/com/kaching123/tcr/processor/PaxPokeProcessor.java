package com.kaching123.tcr.processor;

import android.content.Context;

import com.kaching123.tcr.commands.payment.pax.blackstone.PaxBlackstoneHelloCommand;
import com.kaching123.tcr.model.PaxModel;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by irikhmayer on 09.07.2014.
 */
public class PaxPokeProcessor {

    public static final int INTERVAL_DEBUG = 60000;
    public static final int INTERVAL = Integer.MAX_VALUE;

    private TimerTask executor;
    private Timer timer = new Timer();


    public static PaxPokeProcessor get() {
        return Holder.instance;
    }

    private static final class Holder {
        private static PaxPokeProcessor instance = new PaxPokeProcessor();
    }

    public synchronized void start(final Context context) {
        if (executor != null) {
            return;
        }
        executor = new TimerTask() {
            @Override
            public void run() {
                new PaxBlackstoneHelloCommand().sync(context, PaxModel.get());
            }
        };
        timer.schedule(executor, INTERVAL_DEBUG, INTERVAL);
    }
}
