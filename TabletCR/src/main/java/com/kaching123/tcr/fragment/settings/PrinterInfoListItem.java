package com.kaching123.tcr.fragment.settings;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;
import com.kaching123.tcr.R;

@EViewGroup(R.layout.settings_printer_info_item_view)
public class PrinterInfoListItem extends FrameLayout{

	@ViewById
	protected TextView ip;
	
	@ViewById
	protected TextView mac;

    @StringRes(R.string.pref_printer_ip_tmpl)
    protected String ipTmpl;

    @StringRes(R.string.pref_printer_mac_tmpl)
    protected String macTmpl;
	
	public PrinterInfoListItem(Context context) {
		super(context);
	}
	
	public void bind(String ip, String mac){
		this.ip.setText(String.format(ipTmpl, ip));
		this.mac.setText(String.format(macTmpl, mac));
	}

}
