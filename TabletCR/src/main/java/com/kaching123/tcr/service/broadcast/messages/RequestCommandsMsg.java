package com.kaching123.tcr.service.broadcast.messages;

import com.google.gson.Gson;

import java.util.UUID;

/**
 * Created by Rodrigo Busata on 7/8/2016.
 */
public class RequestCommandsMsg extends BemaSocketMsg<RequestCommandsMsg> {

    private String serial;

    public RequestCommandsMsg(String serial) {
        this.serial = serial;
        this.uuid = UUID.randomUUID().toString();

        this.action = Action.REQUEST_COMMANDS;
    }

    public String getSerial() {
        return serial;
    }

    @Override
    public String uuid() {
        return uuid;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }
}
