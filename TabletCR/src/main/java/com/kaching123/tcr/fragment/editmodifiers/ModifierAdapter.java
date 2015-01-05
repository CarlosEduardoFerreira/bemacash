package com.kaching123.tcr.fragment.editmodifiers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;
import com.kaching123.tcr.model.ModifierModel;

import static com.kaching123.tcr.fragment.UiHelper.showPrice;

/**
 * Created by vkompaniets on 09.12.13.
 */

public class ModifierAdapter extends ObjectsCursorAdapter<ModifierModel> {

    private IModifierClickListener listener;

    public ModifierAdapter(Context context, IModifierClickListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected View newView(int position, ViewGroup parent) {
        View convertView = LayoutInflater.from(getContext()).inflate(R.layout.editmodifiers_modifier_list_item_view, parent, false);
        assert convertView != null;

        ViewHolder holder = new ViewHolder();
        holder.title = (TextView) convertView.findViewById(R.id.title);
        holder.price = (TextView) convertView.findViewById(R.id.price);

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
        showPrice(holder.price, i.cost);

        holder.deleteBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null){
                    listener.onDeleteClick(i);
                }
            }
        });

        convertView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onModifierClick(i);
                }
            }
        });

        return convertView;
    }

    private static class ViewHolder{
        TextView title;
        TextView price;
        ImageButton deleteBtn;
    }

    public static interface IModifierClickListener{
        public void onDeleteClick(ModifierModel model);
        public void onModifierClick(ModifierModel model);
    }
}
