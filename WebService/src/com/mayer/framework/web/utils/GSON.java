package com.mayer.framework.web.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class GSON {

    private static GSON ourInstance = new GSON();
    private Gson instance;

    public static GSON getInstance() {
        return ourInstance;
    }

    private GSON() {
        instance = create();
    }

    private final Gson create() {
        GsonBuilder gson = new GsonBuilder();
        gson.excludeFieldsWithoutExposeAnnotation();
        gson.setPrettyPrinting();
        return gson.create();
    }

    public Gson getGson() {
        return instance;
    }

}
