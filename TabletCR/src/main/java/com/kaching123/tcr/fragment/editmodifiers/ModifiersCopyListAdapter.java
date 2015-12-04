package com.kaching123.tcr.fragment.editmodifiers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.fragment.editmodifiers.SearchFragment.ModifierCountItemModel;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created by vkompaniets on 12.12.13.
 */
public class ModifiersCopyListAdapter extends ObjectsCursorAdapter<ModifierCountItemModel> implements StickyListHeadersAdapter {

    public ModifiersCopyListAdapter(Context context) {
        super(context);
    }

    @Override
    protected View newView(int position, ViewGroup parent) {
        View convertView = LayoutInflater.from(getContext()).inflate(R.layout.editmodifiers_copymodifiers_list_item_view, parent, false);
        assert convertView != null;

        ViewHolder holder = new ViewHolder();
        holder.description = (TextView) convertView.findViewById(R.id.description);
        holder.modifiersCnt = (TextView) convertView.findViewById(R.id.modifiers_cnt);
        holder.addonsCnt = (TextView) convertView.findViewById(R.id.addons_cnt);
        holder.optionsCnt = (TextView) convertView.findViewById(R.id.options_cnt);

        convertView.setTag(holder);
        return convertView;
    }

    @Override
    protected View bindView(View convertView, int position, SearchFragment.ModifierCountItemModel item) {
        ViewHolder holder = (ViewHolder) convertView.getTag();
        ModifierCountItemModel i = getItem(position);

        if (i == null) {
            return convertView;
        }

        holder.description.setText(i.description);
        holder.modifiersCnt.setText("" + i.numModifiers);
        holder.addonsCnt.setText("" + i.numAddons);
        holder.optionsCnt.setText("" + i.numOptionals);

        return convertView;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = newHeaderView(parent);
        }

        TextView textView = (TextView) convertView;
        ModifierCountItemModel item = getItem(position);
        textView.setText(item.categoryTitle);

        return convertView;
    }

    @Override
    public long getHeaderId(int i) {
        return getItem(i).categoryId.hashCode();
    }

    private View newHeaderView(ViewGroup parent) {
        return LayoutInflater.from(getContext()).inflate(R.layout.search_item_category_header, parent, false);
    }

    private static class ViewHolder{
        TextView description;
        TextView modifiersCnt;
        TextView addonsCnt;
        TextView optionsCnt;

    }
}
