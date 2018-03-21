package com.ondemandbay.taxianytime.parse;

/**
 * @author Elluminati elluminati.in
 */
public interface AsyncTaskCompleteListener {
    void onTaskCompleted(String response, int serviceCode);
}
