package com.kaching123.tcr.service.broadcast.messages;

/**
 * Created by Rodrigo Busata on 7/8/2016.
 */
public interface BemaSocketProtocol<T> {

    String uuid();
    Action action();

    enum Action {
        UNDEFINED, RUN_CALLBACK, RUN_COMMANDS, REQUEST_COMMANDS, NOTIFY_NEW_COMMANDS
    }
}
