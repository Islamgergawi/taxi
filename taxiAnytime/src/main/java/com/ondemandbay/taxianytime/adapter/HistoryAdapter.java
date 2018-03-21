/**
 *
 */
package com.ondemandbay.taxianytime.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.ImageOptions;
import com.hb.views.PinnedSectionListView.PinnedSectionListAdapter;
import com.ondemandbay.taxianytime.R;
import com.ondemandbay.taxianytime.component.MyFontTextView;
import com.ondemandbay.taxianytime.models.History;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.TreeSet;

/**
 * @author Elluminati elluminati.in
 */
public class HistoryAdapter extends BaseAdapter implements
        PinnedSectionListAdapter {

    // private ImageOptions imageOptions;
    public static final int TYPE_ITEM = 0;
    public static final int TYPE_SEPARATOR = 1;
    private static final int TYPE_MAX_COUNT = TYPE_SEPARATOR + 1;
    // private DecimalFormat df;
    TreeSet<Integer> mSeparatorsSet;
    SimpleDateFormat simpleDateFormat;
    AQuery aQuery;
    private Activity activity;
    private ArrayList<History> list;
    // private AQuery aQuery;
    private LayoutInflater inflater;
    private ViewHolder holder;
    private SectionViewHolder sectionHolder;
    private ImageOptions imageOptions;

    /**
     * @param historyActivity
     * @param historyList
     */
    public HistoryAdapter(Activity activity, ArrayList<History> historyList,
                          TreeSet<Integer> mSeparatorsSet) {
        this.activity = activity;
        this.list = historyList;

        // df = new DecimalFormat("00");
        this.mSeparatorsSet = mSeparatorsSet;

        inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        // notifyDataSetInvalidated();
        // notifyDataSetChanged();

        imageOptions = new ImageOptions();
        imageOptions.fileCache = true;
        imageOptions.memCache = true;
        imageOptions.targetWidth = 200;
        imageOptions.fallback = R.drawable.default_user;
        aQuery = new AQuery(activity);
    }

    /*
     * (non-Javadoc)
     *
     * @see android.widget.Adapter#getCount()
     */
    @Override
    public int getCount() {
        return list.size();
    }

    /*
     * (non-Javadoc)
     *
     * @see android.widget.Adapter#getItem(int)
     */
    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    /*
     * (non-Javadoc)
     *
     * @see android.widget.Adapter#getItemId(int)
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.widget.Adapter#getView(int, android.view.View,
     * android.view.ViewGroup)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        int type = getItemViewType(position);
        if (convertView == null) {
            switch (type) {
                case TYPE_ITEM:
                    convertView = inflater.inflate(R.layout.history_item, parent,
                            false);
                    holder = new ViewHolder();
                    holder.ivmap = (ImageView) convertView
                            .findViewById(R.id.ivHistoryMap);
                    holder.tvName = (MyFontTextView) convertView
                            .findViewById(R.id.tvHistoryName);

                    holder.tvDate = (MyFontTextView) convertView
                            .findViewById(R.id.tvHistoryDate);
                    holder.tvCost = (MyFontTextView) convertView
                            .findViewById(R.id.tvHistoryCost);

                    convertView.setTag(holder);
                    break;
                case TYPE_SEPARATOR:
                    sectionHolder = new SectionViewHolder();
                    convertView = inflater.inflate(R.layout.history_date_layout,
                            parent, false);

                    sectionHolder.tv = (TextView) convertView
                            .findViewById(R.id.tvDate);
                    convertView.setTag(sectionHolder);
                    break;
            }

        } else {
            switch (type) {
                case TYPE_ITEM:
                    holder = (ViewHolder) convertView.getTag();
                    break;
                case TYPE_SEPARATOR:
                    sectionHolder = (SectionViewHolder) convertView.getTag();
                    break;

            }
        }

        switch (type) {
            case TYPE_ITEM:
                History history = list.get(position);
                aQuery.id(holder.ivmap).progress(R.id.pBar)
                        .image(history.getMapImage(), imageOptions);
                holder.tvName.setText(history.getFirstName() + " "
                        + history.getLastName());
                holder.tvDate.setText(formatTime(history.getDate()));
                holder.tvCost.setText(activity.getString(R.string.payment_unit)
                        + history.getTotal());
                break;
            case TYPE_SEPARATOR:
                Date currentDate = new Date();
                Date returnDate = new Date();
                // Log.i("Return date", "" + list.get(position).getDate());
                SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
                String date = df1.format(currentDate);

                if (list.get(position).getDate().equals(date)) {
                    sectionHolder.tv.setText(activity
                            .getString(R.string.text_today));
                } else if (list.get(position).getDate()
                        .equals(getYesterdayDateString())) {
                    sectionHolder.tv.setText(activity
                            .getString(R.string.text_yesterday));
                } else {
                    try {
                        returnDate = df1.parse(list.get(position).getDate());

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    SimpleDateFormat pinnedDate = new SimpleDateFormat(
                            "MMMM dd,yyyy");
                    Log.i("New Date", pinnedDate.format(returnDate));
                    sectionHolder.tv.setText(pinnedDate.format(returnDate));
                }
                break;

        }

        return convertView;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.hb.views.PinnedSectionListView.PinnedSectionListAdapter#
     * isItemViewTypePinned(int)
     */
    @Override
    public boolean isItemViewTypePinned(int viewType) {
        return viewType == TYPE_SEPARATOR;
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_MAX_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        return mSeparatorsSet.contains(position) ? TYPE_SEPARATOR : TYPE_ITEM;
    }

    private String getYesterdayDateString() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return dateFormat.format(cal.getTime());
    }

    private String formatTime(String strDate) {
        try {
            SimpleDateFormat sf = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
            Date time = sf.parse(strDate);
            TimeZone timezone = TimeZone.getDefault();
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
            sdf.setTimeZone(timezone);
            int currentOffsetFromUTC = timezone.getRawOffset() + (timezone.inDaylightTime(time) ? timezone.getDSTSavings() : 0);
            String result = sdf.format(time.getTime() + currentOffsetFromUTC);
            return result;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return strDate;
    }

    private class ViewHolder {
        MyFontTextView tvName, tvCost, tvDate;
        ImageView ivmap;
        // CircleImageView ivIcon;
    }

    class SectionViewHolder {
        TextView tv;
    }
}