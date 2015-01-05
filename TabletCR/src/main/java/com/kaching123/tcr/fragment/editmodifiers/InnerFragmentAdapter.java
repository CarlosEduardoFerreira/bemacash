package com.kaching123.tcr.fragment.editmodifiers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.model.ModifierModel;

import java.util.HashSet;

import static com.kaching123.tcr.fragment.UiHelper.showPrice;

/**
 * Created by vkompaniets on 12.12.13.
 */

public class InnerFragmentAdapter  extends ObjectsCursorAdapter<ModifierModel> {

    private HashSet<String> selectedItems = new HashSet<String>();

    public InnerFragmentAdapter(Context context) {
        super(context);
    }

    @Override
    protected View newView(int position, ViewGroup parent) {
        View convertView = LayoutInflater.from(getContext()).inflate(R.layout.editmodifiers_copymodifier_inner_item_view, parent, false);
        assert convertView != null;

        ViewHolder holder = new ViewHolder();
        holder.title = (TextView) convertView.findViewById(R.id.title);
        holder.cost = (TextView) convertView.findViewById(R.id.cost);
        holder.checkbox = (ImageView) convertView.findViewById(R.id.checkbox);

        convertView.setTag(holder);
        return convertView;
    }

    @Override
    protected View bindView(View convertView, int position, ModifierModel item) {
        ViewHolder holder = (ViewHolder) convertView.getTag();
        final ModifierModel i = getItem(position);

        if (i == null) {
            return convertView;
        }

        holder.title.setText(i.title);
        showPrice(holder.cost, i.cost);
        holder.checkbox.setActivated(selectedItems.contains(item.modifierGuid));

        return convertView;
    }

    public void itemClicked (int position){
        ModifierModel model = getItem(position);
        if (!selectedItems.contains(model.modifierGuid)){
            selectedItems.add(model.modifierGuid);
        }else{
            selectedItems.remove(model.modifierGuid);
        }
        notifyDataSetChanged();
    }

    public HashSet<String> getSelectedItems(){
        return selectedItems;
    }

    public boolean isAllSelected(){
        return selectedItems.size() == getCount();
    }

    public void setSelectAll(boolean selected) {
        selectedItems.clear();
        if(!selected){
            notifyDataSetChanged();
            return;
        }
        for(int i = 0; i < getCount(); i++){
            selectedItems.add(getItem(i).modifierGuid);
        }
        notifyDataSetChanged();
    }

    private class ViewHolder{
        ImageView checkbox;
        TextView title;
        TextView cost;
    }
}
