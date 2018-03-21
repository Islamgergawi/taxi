/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ondemandbay.taxianytime;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;
import com.ondemandbay.taxianytime.gcmlib.GCMBaseIntentService;
import com.ondemandbay.taxianytime.utils.AppLog;
import com.ondemandbay.taxianytime.utils.Const;
import com.ondemandbay.taxianytime.utils.PreferenceHelper;

/**
 * IntentService responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService {

    // @SuppressWarnings("hiding")
    // private static final String TAG = "GCMIntentService";

    public GCMIntentService() {
        super(CommonUtilities.SENDER_ID);
    }

    @Override
    protected void onRegistered(Context context, String registrationId) {
        // Log.i(TAG, "Device registered: regId = " + registrationId);
        CommonUtilities.displayMessage(context,
                getResources().getString(R.string.text_device_registered));
        // SSConstanants.DEVICE_TOKEN=registrationId;
        // Create object of SharedPreferences.
        new PreferenceHelper(context).putDeviceToken(registrationId);
        // System.out.println(registrationId + "========>>>>>>");
        AppLog.Log(Const.TAG, registrationId);
        publishResults(registrationId, Activity.RESULT_OK);
        // GCMRegisterHendler.onRegComplete(registrationId);

        /*************************
         * ParseObject pObj = new ParseObject("PushNoti");
         * pObj.put("DeviceToken",registrationId); pObj.put("InRange",true);
         * pObj.put("DeviceType","android"); //pObj.put("ACL","");
         * pObj.saveInBackground(); displayMessage(context,
         * getString(R.string.gcm_registered)); //
         * ServerUtilities.register(context, registrationId);
         *
         */
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        // Log.i(TAG, "Device unregistered");
        CommonUtilities.displayMessage(context,
                getResources().getString(R.string.text_device_unregistered));
        if (GCMRegistrar.isRegisteredOnServer(context)) {

        } else {
            // This callback results from the call to unregister made on
            // ServerUtilities when the registration to the server failed.
            // Log.i(TAG, "Ignoring unregister callback");
        }
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        System.err.println("!@#$%^&*()*&^%$#@#$%^&*(*&^%$#$%^&*&^%$#$%^&*(*&^%$#$%^&*&^%$#"
                +"*******************************************************************************************************");

        // Log.i(TAG, "Received message");
        // String message = getString(R.string.gcm_message);
        AppLog.Log(Const.TAG, intent.getExtras() + "");
        String message = intent.getExtras().getString("message");
        String team = intent.getExtras().getString("team");
        AppLog.Log("Notificaton", message);
        AppLog.Log("Team", team);
        // String title = intent.getExtras().getString("title");
        Intent pushIntent = new Intent(Const.INTENT_WALKER_STATUS);
        pushIntent.putExtra(Const.EXTRA_WALKER_STATUS, team);
        CommonUtilities.displayMessage(context, message);

        // notifies user
        generateNotificationNew(context, message);
        LocalBroadcastManager.getInstance(context).sendBroadcast(pushIntent);
    }

    @Override
    protected void onDeletedMessages(Context context, int total) {
        String message = getResources().getString(R.string.text_delete_msg)
                + total;
        CommonUtilities.displayMessage(context, message);
        // notifies user
        generateNotificationNew(context, message);
    }

    @Override
    public void onError(Context context, String errorId) {
        // Log.i(TAG, "Received error: " + errorId);
        // displayMessage(context, getString(R.string.gcm_error, errorId));
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        // log message
        // Log.i(TAG, "Received recoverable error: " + errorId);
        // displayMessage(context,
        // getString(R.string.gcm_recoverable_error, errorId));
        return super.onRecoverableError(context, errorId);
    }

    private void generateNotificationNew(Context context, String message) {
        try {
            System.err.println("**************************************************************************************" +
                    message
                    + "*******************************************************************************************************");
            //    Toast.makeText(context, "crashh", Toast.LENGTH_SHORT).show();
            final Notification.Builder builder = new Notification.Builder(this);
            builder.setDefaults(Notification.DEFAULT_SOUND
                    | Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE);
            builder.setStyle(
                    new Notification.BigTextStyle(builder).bigText(message)
                            .setBigContentTitle(
                                    context.getString(R.string.app_name)))
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(message).setSmallIcon(R.drawable.ic_launcher);
            builder.setAutoCancel(true);
            Intent notificationIntent = new Intent(context,
                    MainDrawerActivity.class);
            notificationIntent.putExtra("fromNotification", "notification");
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent intent = PendingIntent.getActivity(context, 0,
                    notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(intent);
            final NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(0, builder.build());

            System.err.println("#################################################################################" +
                    message
                    + "###########################################################################");
        }catch(Exception e)
        {

        }
    }

    private void publishResults(String regid, int result) {
        Intent intent = new Intent(CommonUtilities.DISPLAY_REGISTER_GCM);
        intent.putExtra(CommonUtilities.RESULT, result);
        intent.putExtra(CommonUtilities.REGID, regid);

        sendBroadcast(intent);
    }

    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    //@SuppressWarnings("deprecation")
    /* private void generateNotification(Context context, String message,int x) {
     // System.out.println("this is message " + message);
     // System.out.println("NOTIFICATION RECEIVED!!!!!!!!!!!!!!" + message);
     int icon = R.drawable.ic_launcher;
     long when = System.currentTimeMillis();
     NotificationManager notificationManager = (NotificationManager) context
     .getSystemService(Context.NOTIFICATION_SERVICE);
         NotificationCompat.Builder notification = new NotificationCompat.Builder(context)
                 .setSmallIcon(icon)
                 .setContentTitle(context.getString(R.string.app_name))
         .setContentText(message)
                 .setWhen(when);

     /*String title = context.getString(R.string.app_name);
     Intent notificationIntent = new Intent(context,
     MainDrawerActivity.class);
     notificationIntent.putExtra("fromNotification", "notification");
     // set intent so it does not start a new activity
     notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
             | Intent.FLAG_ACTIVITY_SINGLE_TOP);
     PendingIntent intent = PendingIntent.getActivity(context, 0,
     notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

     notification.setLatestEventInfo(context, title, message, intent);
     notification.flags |= Notification.FLAG_AUTO_CANCEL;
     System.out.println("notification====>" + message);
     notification.defaults |= Notification.DEFAULT_SOUND;
     notification.defaults |= Notification.DEFAULT_VIBRATE;
     // notification.defaults |= Notification.DEFAULT_LIGHTS;
     notification.flags |= Notification.FLAG_SHOW_LIGHTS;
     notification.ledARGB = 0x00000000;
     notification.ledOnMS = 0;
     notification.ledOffMS = 0;
     notificationManager.notify(0, notification);
     PowerManager pm = (PowerManager) context
     .getSystemService(Context.POWER_SERVICE);
     PowerManager.WakeLock wakeLock = pm.newWakeLock(
     PowerManager.FULL_WAKE_LOCK
     | PowerManager.ACQUIRE_CAUSES_WAKEUP
     | PowerManager.ON_AFTER_RELEASE, "WakeLock");
    wakeLock.acquire();
     wakeLock.release();
     }*/
}