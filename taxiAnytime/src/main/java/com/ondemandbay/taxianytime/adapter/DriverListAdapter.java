/**
 *
 */
package com.ondemandbay.taxianytime.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.ondemandbay.taxianytime.R;
import com.ondemandbay.taxianytime.models.Driver;

import java.util.ArrayList;

/**
 * @author Elluminati elluminati.in
 */
public class DriverListAdapter extends BaseAdapter {

    ViewHolder holder;
    /**
     *
     */

    private LayoutInflater inflater;
    private ArrayList<Driver> listDriver;

    public DriverListAdapter(Context context, ArrayList<Driver> listDriver) {
        this.listDriver = listDriver;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    /*
     * (non-Javadoc)
     *
     * @see android.widget.Adapter#getCount()
     */
    @Override
    public int getCount() {
        return listDriver.size();
    }

    /*
     * (non-Javadoc)
     *
     * @see android.widget.Adapter#getItem(int)
     */
    @Override
    public Object getItem(int position) {
        return listDriver.get(position);
    }

    /*
     * (non-Javadoc)
     *
     * @see android.widget.Adapter#getItemId(int)
     */
    @Override
    public long getItemId(int position) {
        return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.widget.Adapter#getView(int, android.view.View,
     * android.view.ViewGroup)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item, parent, false);
            holder = new ViewHolder();

        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        return convertView;
    }

    private class ViewHolder {

    }

}
