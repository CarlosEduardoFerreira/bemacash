package com.kaching123.tcr.fragment.editmodifiers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.fragment.search.CategoryItemViewModel;

import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created by vkompaniets on 12.12.13.
 */
public class ModifiersCopyListAdapter extends ObjectsCursorAdapter<CategoryItemViewModel> implements StickyListHeadersAdapter {

    public ModifiersCopyListAdapter(Context context, List<CategoryItemViewModel> objects) {
        super(context);
        changeCursor(objects);
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
    protected View bindView(View convertView, int position, CategoryItemViewModel item) {
        ViewHolder holder = (ViewHolder) convertView.getTag();
        CategoryItemViewModel i = getItem(position);

        if (i == null) {
            return convertView;
        }

        holder.description.setText(i.description);
        holder.modifiersCnt.setText("" + i.modifiersCount);
        holder.addonsCnt.setText("" + i.addonsCount);
        holder.optionsCnt.setText("" + i.optionalCount);

        return convertView;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = newHeaderView(parent);
        }

        TextView textView = (TextView) convertView;
        CategoryItemViewModel item = getItem(position);
        textView.setText(item.categoryName);

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
