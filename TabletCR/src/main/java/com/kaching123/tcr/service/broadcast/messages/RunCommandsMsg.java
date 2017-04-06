package com.kaching123.tcr.service.broadcast.messages;

import com.google.gson.Gson;

import java.util.Map;
import java.util.UUID;

/**
 * Created by Rodrigo Busata on 7/5/2016.
 */
public class RunCommandsMsg extends BemaSocketMsg<RunCommandsMsg> {

    public String serial;
    public Map<Integer, SqlCommand> commandsSend;

    public RunCommandsMsg(String serial, Map<Integer, SqlCommand> commandsSend) {
        this.serial = serial;
        this.commandsSend = commandsSend;
        this.uuid = UUID.randomUUID().toString();

        this.action = Action.RUN_COMMANDS;
    }

    @Override
    public String uuid() {
        return uuid;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static class SqlCommand {
        public String command;
        public int order;

        public SqlCommand(int order, String command) {
            this.command = command;
            this.order = order;
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof RunCommandsMsg
                && serial.equals(((RunCommandsMsg)o).serial);
    }
}
