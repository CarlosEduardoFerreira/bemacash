package com.mayer.framework.web.model;

import java.net.URLEncoder;

/**
 * @author Ivan v. Rikhmayer
 *          Utility class to hold a web parameter.
 */
public class WebParameter {
	
	private String key;
	private String value;
	
	/**
	 * Builds a webparameter.
	 * @param key
	 * @param value
	 */
	public WebParameter(String key, String value) {
		this.key = key;
		this.value = value;
	}
	
	public String getValue(){
		return value;
	}
	
	public String getKey(){
		return key;
	}
	
	/**
	 * Returns a string of the form
	 * key value.
	 */
	public String toString(){
		StringBuilder keyvalue;
		keyvalue = new StringBuilder();
		try {
			String encodedValue = URLEncoder.encode(value, "UTF-8");
			keyvalue.append(key);
			keyvalue.append("=");
			keyvalue.append(encodedValue);
		} catch (Exception ex) {
			throw new RuntimeException("Broken VM: no UTF-8", ex);
		}
		return keyvalue.toString();
	}
}