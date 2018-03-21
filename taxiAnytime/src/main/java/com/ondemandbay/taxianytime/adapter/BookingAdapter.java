package com.ondemandbay.taxianytime.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidquery.callback.ImageOptions;
import com.ondemandbay.taxianytime.R;
import com.ondemandbay.taxianytime.models.Booking;
import com.ondemandbay.taxianytime.utils.AppLog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class BookingAdapter extends BaseAdapter {
    SimpleDateFormat data = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private LayoutInflater inflater;
    private ArrayList<Booking> list;
    private ImageOptions imageOptions;

    // private AQuery aQuery;
    // private Date newDate = new Date();
    // private String startTime;

    public BookingAdapter(Activity activity, ArrayList<Booking> bookingList) {
        this.list = bookingList;

        inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        imageOptions = new ImageOptions();
        imageOptions.fileCache = true;
        imageOptions.memCache = true;
        imageOptions.fallback = R.drawable.no_items;
    }

    @Override
    public int getCount() {
        AppLog.Log("List size", String.valueOf(list.size()));
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        Date newDate = new Date();
        if (convertView == null) {
            convertView = inflater
                    .inflate(R.layout.booking_item, parent, false);
            holder = new ViewHolder();

            holder.tripStatus = (LinearLayout) convertView
                    .findViewById(R.id.tripStatus);
            holder.ivCalendar = (ImageView) convertView
                    .findViewById(R.id.calendarImg);
            holder.ivSource = (ImageView) convertView
                    .findViewById(R.id.sourceImg);
            holder.tvStatus = (TextView) convertView
                    .findViewById(R.id.tvStatus);
            holder.tvDateTime = (TextView) convertView
                    .findViewById(R.id.tvDateTime);
            holder.tvSource = (TextView) convertView
                    .findViewById(R.id.tvSourceAddr);

            convertView.setTag(holder);
            AppLog.Log("Request_Type", (list.get(position)).getStartTime());
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Booking booking = list.get(position);

        if (booking.isFutureRequest()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

            try {
                newDate = sdf.parse(booking.getStartTime());

            } catch (ParseException e) {
                e.printStackTrace();
            }

            sdf = new SimpleDateFormat("EEE, MMM d hh:mm a");
            String startTime = sdf.format(newDate);
            holder.tripStatus.setBackgroundResource(R.drawable.box_future);
            holder.tvStatus.setText(R.string.text_future_trip);
            holder.ivCalendar
                    .setBackgroundResource(R.drawable.calendar_mybookings);
            holder.tvDateTime.setText(startTime);
            holder.ivSource.setBackgroundResource(R.drawable.ellipse_b);
            holder.tvSource.setText(booking.getSource());
            holder.tvSource.setSingleLine();
        }

       else {
            holder.tripStatus.setBackgroundResource(R.drawable.box_ongoing);
            holder.tvStatus.setText(R.string.text_ongoing_trip);
            holder.ivCalendar.setBackgroundResource(R.drawable.check_ongoing);
            holder.tvDateTime.setText(booking.getRequestCreatedTime());
            holder.ivSource.setBackgroundResource(R.drawable.ellipse_a);
            holder.tvSource.setText(booking.getSource());
            holder.tvSource.setSingleLine();
        }

        return convertView;
    }

    private class ViewHolder {
        TextView tvDateTime, tvSource, tvStatus;
        LinearLayout tripStatus;
        ImageView ivCalendar, ivSource;
    }
}
