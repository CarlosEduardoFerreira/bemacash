package com.kaching123.tcr.service.broadcast.messages;

import com.google.gson.Gson;

/**
 * Created by Rodrigo Busata on 7/8/2016.
 */
public class BemaSocketMsg<T> implements BemaSocketProtocol<T> {

    protected String uuid;
    protected Action action = Action.UNDEFINED;
    private String data;

    @Override
    public String uuid() {
        return uuid;
    }

    @Override
    public Action action() {
        return action;
    }

    public Object toObject(Class c){
        return new Gson().fromJson(data, c);
    }

    public BemaSocketMsg fromJson(String json){
        BemaSocketMsg bemaSocketMsg = new Gson().fromJson(json, BemaSocketMsg.class);
        if (bemaSocketMsg != null) bemaSocketMsg.data = json;
        return bemaSocketMsg;
    }
}
