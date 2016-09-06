package com.kaching123.tcr.adapter;

import android.content.Context;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;

import com.mobeta.android.dslv.DragSortListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pkabakov on 10.04.2014.
 */
    public abstract class ObjectCursorDragAdapter<T> extends ObjectsCursorAdapter<T> implements DragSortListView.DragSortListener {

        public static final int REMOVED = -1;

        private SparseIntArray listMapping = new SparseIntArray();

        private ArrayList<Integer> removedCursorPositions = new ArrayList<Integer>();

        public ObjectCursorDragAdapter(Context context) {
            super(context);
        }


        @Override
        public void changeCursor(List<T> cursor) {
            super.changeCursor(cursor);
            resetMappings();
        }

        public void reset() {
            resetMappings();
            notifyDataSetChanged();
        }

        private void resetMappings() {
            listMapping.clear();
            removedCursorPositions.clear();
        }

        @Override
        public T getItem(int position) {
            return super.getItem(listMapping.get(position, position));
        }

        @Override
        public long getItemId(int position) {
            return super.getItemId(listMapping.get(position, position));
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            int mappedPosition = listMapping.get(position, position);
            if (convertView == null) {
                convertView = newDropDownView(mappedPosition, parent);
            }
            return bindView(convertView, mappedPosition, getItem(position));
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            int mappedPosition = listMapping.get(position, position);
            if (convertView == null) {
                convertView = newView(mappedPosition, parent);
            }
            return bindView(convertView, mappedPosition, getItem(position));
        }

        @Override
        public void drop(int from, int to) {
            if (from != to) {
                int cursorFrom = listMapping.get(from, from);

                if (from > to) {
                    for (int i = from; i > to; --i) {
                        listMapping.put(i, listMapping.get(i - 1, i - 1));
                    }
                } else {
                    for (int i = from; i < to; ++i) {
                        listMapping.put(i, listMapping.get(i + 1, i + 1));
                    }
                }
                listMapping.put(to, cursorFrom);

                cleanMapping();
                notifyDataSetChanged();
            }
        }

        @Override
        public void remove(int which) {
            int cursorPos = listMapping.get(which, which);
            if (!removedCursorPositions.contains(cursorPos)) {
                removedCursorPositions.add(cursorPos);
            }

            int newCount = getCount();
            for (int i = which; i < newCount; ++i) {
                listMapping.put(i, listMapping.get(i + 1, i + 1));
            }

            listMapping.delete(newCount);

            cleanMapping();
            notifyDataSetChanged();
        }

        @Override
        public void drag(int from, int to) {}

        private void cleanMapping() {
            ArrayList<Integer> toRemove = new ArrayList<Integer>();

            int size = listMapping.size();
            for (int i = 0; i < size; ++i) {
                if (listMapping.keyAt(i) == listMapping.valueAt(i)) {
                    toRemove.add(listMapping.keyAt(i));
                }
            }

            size = toRemove.size();
            for (int i = 0; i < size; ++i) {
                listMapping.delete(toRemove.get(i));
            }
        }

        @Override
        public int getCount() {
            return super.getCount() - removedCursorPositions.size();
        }

        public int getCursorPosition(int position) {
            return listMapping.get(position, position);
        }

        public ArrayList<Integer> getCursorPositions() {
            ArrayList<Integer> result = new ArrayList<Integer>();

            for (int i = 0; i < getCount(); ++i) {
                result.add(listMapping.get(i, i));
            }

            return result;
        }

        public int getListPosition(int cursorPosition) {
            if (removedCursorPositions.contains(cursorPosition)) {
                return REMOVED;
            }

            int index = listMapping.indexOfValue(cursorPosition);
            if (index < 0) {
                return cursorPosition;
            } else {
                return listMapping.keyAt(index);
            }
        }

        public List<T> getItems(int from, int to){
            ArrayList<T> items = new ArrayList<>(to - from + 1);
            for (int i = from; i <= to; i++) {
                items.add(getItem(i));
            }
            return items;
        }
    }