package com.kaching123.tcr.fragment.permissions;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.model.Permission;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by vkompaniets on 26.12.13.
 */
public class PermissionsAdapter extends ObjectsCursorAdapter<Permission> {

    HashSet<Permission> selectedItems = new HashSet<Permission>();

    public PermissionsAdapter(Context context) {
        super(context);
    }

    @Override
    protected View newView(final int position, ViewGroup parent) {
        View convertView = LayoutInflater.from(getContext()).inflate(R.layout.permission_list_item, parent, false);
        assert convertView != null;

        ViewHolder holder = new ViewHolder();

        holder.checkBox = (ImageView) convertView.findViewById(R.id.checkbox);
        holder.name = (TextView) convertView.findViewById(R.id.name);

        convertView.setTag(holder);
        return convertView;
    }

    @Override
    protected View bindView(View convertView, int position, Permission item) {
        ViewHolder holder = (ViewHolder) convertView.getTag();
        Permission i = getItem(position);

        if (i == null)
            return convertView;

        holder.name.setText(getContext().getString(i.getLabelId()));
        holder.checkBox.setActivated(selectedItems.contains(i));

        return convertView;
    }

    public void itemClicked(int position){
        Permission p = getItem(position);
        Logger.d("Clicked: " + p);
        if (!selectedItems.contains(p)){
            selectedItems.add(p);
        }else{
            selectedItems.remove(p);
        }
        notifyDataSetChanged();
    }

    public void setSelectedPermissions(Collection<Permission> list){
        selectedItems.clear();
        selectedItems.addAll(list);
        notifyDataSetChanged();
    }

    public void setSelectAll(boolean selected) {
        selectedItems.clear();
        if(!selected){
            notifyDataSetChanged();
            return;
        }
        for(int i = 0; i < getCount(); i++){
            selectedItems.add(getItem(i));
        }
        notifyDataSetChanged();
    }

    public boolean isAllSelected(){
        return selectedItems.size() == getCount();
    }

    public HashSet<Permission> getSelectedPermissions(){
        return selectedItems;
    }

    public int getSelectedCount() {
        return selectedItems == null ? 0 : selectedItems.size();
    }

    private static class ViewHolder{
        ImageView checkBox;
        TextView name;
    }
}
