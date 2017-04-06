package com.kaching123.tcr.service.broadcast.messages;

import com.google.gson.Gson;

import java.util.List;
import java.util.UUID;

/**
 * Created by Rodrigo Busata on 7/11/2016.
 */
public class RunCallBack extends BemaSocketMsg<RunCallBack> {

    public List<Integer> commandWithSuccess;
    public String serial;

    public RunCallBack(String uuid, String serial, List<Integer> commandWithSuccess) {
        this.uuid = uuid;
        this.serial = serial;
        this.commandWithSuccess = commandWithSuccess;

        this.action = Action.RUN_CALLBACK;
    }

    public RunCallBack(String uuid) {
        this.uuid = uuid;
        this.action = Action.RUN_CALLBACK;
    }

    public RunCallBack() {
        this.uuid = UUID.randomUUID().toString();
        this.action = Action.RUN_CALLBACK;
    }

    @Override
    public String uuid() {
        return null;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }
}
