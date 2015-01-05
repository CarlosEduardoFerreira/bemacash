package com.kaching123.tcr.adapter;

import java.util.List;

/**
 * Created by gdubina on 03.02.14.
 */
public interface IObjectsAdapter<T> {
    void changeCursor(List<T> list);
}
