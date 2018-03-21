/**
 *
 */
package com.ondemandbay.taxianytime.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ondemandbay.taxianytime.R;
import com.ondemandbay.taxianytime.component.MyFontTextView;
import com.ondemandbay.taxianytime.models.Card;
import com.ondemandbay.taxianytime.parse.AsyncTaskCompleteListener;
import com.ondemandbay.taxianytime.parse.HttpRequester;
import com.ondemandbay.taxianytime.parse.ParseContent;
import com.ondemandbay.taxianytime.utils.AndyUtils;
import com.ondemandbay.taxianytime.utils.AppLog;
import com.ondemandbay.taxianytime.utils.Const;
import com.ondemandbay.taxianytime.utils.PreferenceHelper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Elluminati elluminati.in
 */
public class PaymentListAdapter extends BaseAdapter implements
        AsyncTaskCompleteListener, OnClickListener {
    private LayoutInflater inflater;
    private ViewHolder holder;
    private ArrayList<Card> listCard;
    private int selectedPosition;
    private Activity context;
    private PreferenceHelper pHelper;
    // private int positionKey = 21;
    private Dialog deleteCardDialog;
    private MyFontTextView tvDeleteCardOk, tvDeleteCardCancel;
    private Card card;

    // private int index;

    public PaymentListAdapter(Context context, ArrayList<Card> listCard,
                              int defaultCard) {
        this.listCard = listCard;
        this.context = (Activity) context;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        selectedPosition = defaultCard;
        pHelper = new PreferenceHelper(context);
    }

    @Override
    public int getCount() {
        return listCard.size();
    }

    @Override
    public Object getItem(int position) {
        return listCard.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.view_payment_list_item,
                    parent, false);
            holder = new ViewHolder();
            holder.ivCard = (ImageView) convertView.findViewById(R.id.ivCard);
            holder.tvNo = (TextView) convertView.findViewById(R.id.tvNo);
            holder.ivCardDelete = (ImageView) convertView
                    .findViewById(R.id.ivCardDelete);
            // holder.rdCardSelection = (RadioButton) convertView
            // .findViewById(R.id.rdCardSelection);
            convertView.setTag(holder);
            // holder.rdCardSelection.setTag(position);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        card = listCard.get(position);
        final int cardId = card.getId();
        // index = position;

        holder.tvNo.setText("*****" + card.getLastFour());

        holder.ivCardDelete.setOnClickListener(this);
        // if (type.equalsIgnoreCase(Const.VISA)) {
        // holder.ivCard.setImageResource(R.drawable.ub__creditcard_visa);
        // } else if (type.equalsIgnoreCase(Const.MASTERCARD)) {
        // holder.ivCard
        // .setImageResource(R.drawable.ub__creditcard_mastercard);
        // } else if (type.equalsIgnoreCase(Const.AMERICAN_EXPRESS)) {
        // holder.ivCard.setImageResource(R.drawable.ub__creditcard_amex);
        // } else if (type.equalsIgnoreCase(Const.DISCOVER)) {
        // holder.ivCard.setImageResource(R.drawable.ub__creditcard_discover);
        // } else if (type.equalsIgnoreCase(Const.DINERS_CLUB)) {
        // holder.ivCard.setImageResource(R.drawable.ub__creditcard_discover);
        // } else {
        // holder.ivCard.setImageResource(R.drawable.ub__nav_payment);
        // }

        if (selectedPosition == cardId) {
            holder.ivCard.setSelected(true);
            // holder.rdCardSelection.setChecked(true);
        } else {
            holder.ivCard.setSelected(false);
            // holder.rdCardSelection.setChecked(false);
        }
        if (card.isDefault()) {
            // holder.rdCardSelection.setChecked(true);
            holder.ivCard.setSelected(true);
            PreferenceHelper pref = new PreferenceHelper(context);
            pref.putDefaultCard(cardId);
            pref.putDefaultCardNo(card.getLastFour());
            pref.putDefaultCardType(card.getCardType());
        } else {
            holder.ivCard.setSelected(false);
            // holder.rdCardSelection.setChecked(false);
        }
        holder.ivCard.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView view = (ImageView) v;
                if (!view.isSelected()) {
                    AppLog.Log("PaymentAdapater", "checked Id " + cardId);
                    selectedPosition = cardId;
                    PreferenceHelper pref = new PreferenceHelper(context);
                    pref.putDefaultCard(cardId);
                    pref.putDefaultCardNo(card.getLastFour());
                    pref.putDefaultCardType(card.getCardType());
                    notifyDataSetChanged();
                    setDefaultCard(cardId);
                } else {
                    AppLog.Log("PaymentAdapater", "unchecked Id " + cardId);
                }
                Intent i = new Intent("card_change_receiver");
                context.sendBroadcast(i);
            }
        });

        holder.tvNo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView view = (TextView) v;
                if (!view.isSelected()) {
                    AppLog.Log("PaymentAdapater", "checked Id " + cardId);
                    selectedPosition = cardId;
                    PreferenceHelper pref = new PreferenceHelper(context);
                    pref.putDefaultCard(cardId);
                    pref.putDefaultCardNo(card.getLastFour());
                    pref.putDefaultCardType(card.getCardType());
                    notifyDataSetChanged();
                    setDefaultCard(cardId);
                } else {
                    AppLog.Log("PaymentAdapater", "unchecked Id " + cardId);
                }
                Intent i = new Intent("card_change_receiver");
                context.sendBroadcast(i);
            }
        });
        return convertView;
    }

    private void setDefaultCard(int cardId) {
        AndyUtils.showCustomProgressDialog(context,
                context.getString(R.string.text_changing_default_card), true,
                null);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL, Const.ServiceType.DEFAULT_CARD);
        map.put(Const.Params.ID, String.valueOf(pHelper.getUserId()));
        map.put(Const.Params.TOKEN, String.valueOf(pHelper.getSessionToken()));
        map.put(Const.Params.DEFAULT_CARD_ID, String.valueOf(cardId));

        new HttpRequester((Activity) context, map,
                Const.ServiceCode.DEFAULT_CARD, this);
        // requestQueue.add(new VolleyHttpRequest(Method.POST, map,
        // Const.ServiceCode.DEFAULT_CARD, this, this));
    }

    @Override
    public void onTaskCompleted(String response, int serviceCode) {

        switch (serviceCode) {

            case Const.ServiceCode.DEFAULT_CARD:
                listCard.clear();
                AppLog.Log("PaymentAdapter", "CardSelection reponse : " + response);
                new ParseContent((Activity) context).parseCards(response, listCard);
                notifyDataSetChanged();
                AndyUtils.removeCustomProgressDialog();
                holder.tvNo.setTextColor(context.getColor(R.color.theme_color));
                break;

            case Const.ServiceCode.DELETE_CARD:
                if (new ParseContent(context).isSuccess(response)) {
                    new ParseContent(context).getNextDefaultCard(response);
                    listCard.remove(card);
                    notifyDataSetChanged();
                    AndyUtils.removeCustomProgressDialog();
                    AndyUtils.showToast(
                            context.getString(R.string.text_success_delete_card),
                            context);
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivCardDelete:
                showDeleteCardDialog();
                break;

            case R.id.tvDeleteCardOk:
                deleteCardDialog.dismiss();
                deleteCard();
                break;

            case R.id.tvDeleteCardCancel:
                deleteCardDialog.dismiss();
                break;

            default:
                break;
        }
    }

    private void deleteCard() {
        AndyUtils.showCustomProgressDialog(context,
                context.getString(R.string.text_deleting_card), true, null);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Const.URL, Const.ServiceType.DELETE_CARD);
        map.put(Const.Params.ID, String.valueOf(pHelper.getUserId()));
        map.put(Const.Params.TOKEN, String.valueOf(pHelper.getSessionToken()));
        map.put(Const.Params.CARD_ID, String.valueOf(card.getId()));

        new HttpRequester((Activity) context, map,
                Const.ServiceCode.DELETE_CARD, this);
        // requestQueue.add(new VolleyHttpRequest(Method.POST, map,
        // Const.ServiceCode.DELETE_CARD, this, this));
    }

    public void showDeleteCardDialog() {
        deleteCardDialog = new Dialog(context);
        deleteCardDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        deleteCardDialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        deleteCardDialog.setContentView(R.layout.dialog_delete_card);
        tvDeleteCardOk = (MyFontTextView) deleteCardDialog
                .findViewById(R.id.tvDeleteCardOk);
        tvDeleteCardCancel = (MyFontTextView) deleteCardDialog
                .findViewById(R.id.tvDeleteCardCancel);

        tvDeleteCardOk.setOnClickListener(this);
        tvDeleteCardCancel.setOnClickListener(this);
        deleteCardDialog.show();
    }

    private class ViewHolder {
        public ImageView ivCard;
        public TextView tvNo;
        public ImageView ivCardDelete;
        // public RadioButton rdCardSelection;
    }
}
