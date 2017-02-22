package com.kaching123.tcr.service.broadcast.messages;

import com.google.gson.Gson;

import java.util.UUID;

/**
 * Created by Rodrigo Busata on 7/8/2016.
 */
public class NotifyNewCommandMsg extends BemaSocketMsg<NotifyNewCommandMsg> {

    public String serial;

    public NotifyNewCommandMsg(String serial) {
        this.serial = serial;
        this.uuid = UUID.randomUUID().toString();

        this.action = Action.NOTIFY_NEW_COMMANDS;
    }

    @Override
    public String uuid() {
        return null;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }
}
