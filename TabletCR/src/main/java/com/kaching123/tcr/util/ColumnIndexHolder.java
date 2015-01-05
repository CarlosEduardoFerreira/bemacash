package com.kaching123.tcr.util;

import android.database.Cursor;

import java.util.HashMap;
import java.util.Locale;

public class ColumnIndexHolder {

	private HashMap<String, Integer> indexeds = new HashMap<String, Integer>();

    public void updateLazy(Cursor c){
        if(!indexeds.isEmpty())
            return;
        update(c);
    }

	public void update(Cursor c){
		indexeds.clear();
		if(c == null)
			return;
		for(String name : c.getColumnNames()){
			indexeds.put(name, c.getColumnIndex(name));
		}
	}
	
	public int get(String column){
        if(!indexeds.containsKey(column)){
            throw new IllegalArgumentException(String.format(Locale.US, "Column %s in not present in the map", column));
        }
		return indexeds.get(column);
	}
}
