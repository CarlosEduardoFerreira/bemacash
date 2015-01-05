package com.kaching123.tcr.model;

public enum ItemType {

	ITEM, ADDON;

	public static ItemType valueOf(int id) {
		return values()[id];
	}
}
