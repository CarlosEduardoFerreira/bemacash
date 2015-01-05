package com.kaching123.tcr.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EViewGroup;
import com.googlecode.androidannotations.annotations.ViewById;
import com.jess.ui.TwoWayGridView;
import com.kaching123.tcr.R;
import com.kaching123.tcr.adapter.ObjectsCursorAdapter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by vkompaniets on 18.11.13.
 */
@EViewGroup(R.layout.modify_container)
public abstract class BaseAddonContainerView<T> extends FrameLayout {

    @ViewById
    protected TwoWayGridView buttonGrid;

    @ViewById
    protected TextView containerTitle;


    private ButtonsAdapter<T> adapter;

    public BaseAddonContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @AfterViews
    protected void init() {
        adapter = createAdapter();
        buttonGrid.setAdapter(adapter);
    }

    protected abstract ButtonsAdapter<T> createAdapter();

    public void setContainerTitle(String title) {
        this.containerTitle.setText(title);
    }

    public void setList(List<T> modifiers) {
        adapter.changeCursor(modifiers);
    }

    public Set<String> getSelectedItems() {
        return adapter.selectedItems;
    }

    protected void setSelectedItems(List<String> selectedItemGuids) {
        adapter.setSelectedItems(selectedItemGuids);
    }

    public void cleanSelection() {
        adapter.setSelectedItems(null);
    }

    public abstract class ButtonsAdapter<T> extends ObjectsCursorAdapter<T> {

        private HashSet<String> selectedItems = new HashSet<String>();

        private boolean singleMode;

        public ButtonsAdapter(Context context, boolean singleMode) {
            super(context);
            this.singleMode = singleMode;
        }

        @Override
        public void changeCursor(List<T> list) {
            super.changeCursor(list);
            if (singleMode && list != null && !list.isEmpty() && selectedItems.isEmpty()) {
                selectedItems.add(getGuid(list.get(0)));
                onChangeSelections();
            }
        }

        private void onChangeSelections() {
            if (onChangeListener != null) {
                onChangeListener.onChanged();
            }
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            ModifyButton v = ModifyButton_.build(getContext());
            v.setOnClickListener(changeSelectedListener);
            return v;
        }

        @Override
        protected View bindView(View convertView, int position, T item) {
            ModifyButton view = (ModifyButton) convertView;
            BigDecimal price = getCost(item);
            view.bind(getGuid(item), getTitle(item), price, selectedItems.contains(getGuid(item)));
            return view;
        }

        protected abstract String getTitle(T item);

        protected abstract BigDecimal getCost(T item);

        protected abstract String getGuid(T item);

        private void setSelectedItems(List<String> selectedItemGuids) {
            selectedItems.clear();
            if (selectedItemGuids != null) {
                selectedItems.addAll(selectedItemGuids);
            }
            notifyDataSetChanged();
        }

        private OnClickListener changeSelectedListener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                ModifyButton v = (ModifyButton) view;
                String guid = v.getGuid();
                if (singleMode) {
                    handleSingleMode(guid);
                } else {
                    handleMultipleMode(guid);
                }
                notifyDataSetChanged();
                onChangeSelections();
            }

            private void handleMultipleMode(String guid) {
                if (!selectedItems.remove(guid)) {
                    selectedItems.add(guid);
                }
            }

            private void handleSingleMode(String guid) {
                selectedItems.clear();
                selectedItems.add(guid);
            }
        };
    }

    private OnChangeListener onChangeListener;

    public void setOnChangeListener(OnChangeListener onChangeListener) {
        this.onChangeListener = onChangeListener;
    }

    public static interface OnChangeListener {
        void onChanged();
    }

}
