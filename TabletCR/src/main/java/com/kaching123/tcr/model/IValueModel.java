package com.kaching123.tcr.model;

import android.content.ContentValues;

public interface IValueModel {

    String getGuid();
	ContentValues toValues();
}
