package com.diabet.muhendis.diabetex.helpers;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.diabet.muhendis.diabetex.Keys;
import com.diabet.muhendis.diabetex.MainActivity;
import com.diabet.muhendis.diabetex.R;
import com.diabet.muhendis.diabetex.db.DiabetWatchDbHelper;
import com.google.firebase.FirebaseApp;
import java.util.Calendar;
import java.util.Map;

/**
 * Created by muhendis on 1.04.2018.
 */

public class AlarmManagerHelper extends BroadcastReceiver {
    private final String TAG ="MYALARM";
    private LocalDBHelper localDbHelper;
    private DiabetWatchDbHelper diabetWatchDbHelper;
    public AlarmManagerHelper() {

    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        FirebaseApp.initializeApp(context);
        diabetWatchDbHelper = new DiabetWatchDbHelper(context);
        localDbHelper = new LocalDBHelper(diabetWatchDbHelper);

        if (intent.getAction()!= null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            setAlarms(context);
        }
        NotificationCompat.Builder mBuilder;
        String message = intent.getStringExtra(Keys.ALARM_MESSAGE);
        boolean showIfNotFinishedProgram = intent.getBooleanExtra(Keys.ALARM_MESSAGE_SHOW_IF_NOT_FINISHED_PROGRAM,false);
        boolean showIfFinishedProgram = intent.getBooleanExtra(Keys.ALARM_MESSAGE_SHOW_IF_FINISHED_PROGRAM,false);
        boolean showIfFinishedProgramInEvening = intent.getBooleanExtra(Keys.ALARM_MESSAGE_SHOW_IF_NOT_FINISHED_PROGRAM_IN_EVENING,false);
        boolean showStatisticsInFriday = intent.getBooleanExtra(Keys.ALARM_MESSAGE_SHOW_STATISTICS_IN_FRIDAY,false);


        Log.d(TAG,"LAST 5 DAYS FINISHED PROGRAMS: "+localDbHelper.checkFinishedProgramNumberForLastFiveDays());
        if(showIfFinishedProgram && localDbHelper.isAllProgramFinishedToday()){
            message="Tebrikler. Bugün çok sağlıklısın \uD83D\uDC4F\uD83D\uDE0A";
        }

        if(showIfFinishedProgramInEvening && localDbHelper.isAllProgramFinishedToday()){
            message="Tebrikler. Bütün egzersizlerini tamamladın. Bugün çok sağlıklısın \uD83D\uDC4F\uD83D\uDE0A";
        }

        if(showStatisticsInFriday)
        {
            int numberOfDaysProgramsCompleted = localDbHelper.checkFinishedProgramNumberForLastFiveDays();
            message="Bu hafta "+ numberOfDaysProgramsCompleted+" gün programlarının tümünü tamamladın.";
        }

        Log.d(TAG,"MESSAGE: "+message);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        if(message!=null)
        {
            if((showIfNotFinishedProgram && !localDbHelper.isAllProgramFinishedToday())|| (!showIfNotFinishedProgram))
            {
                Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.logo);
                mBuilder = new NotificationCompat.Builder(context)
                        .setLargeIcon(icon)
                        .setBadgeIconType(R.drawable.logo)
                        .setSmallIcon(R.drawable.logo_white)
                        .setContentTitle("Diabetex")
                        .setSound(alarmSound)
                        .setVibrate(new long[] { 1000, 1000})
                        .setContentText(message);

                Intent resultIntent = new Intent(context, MainActivity.class);
                int _id = (int) System.currentTimeMillis();
                // Because clicking the notification opens a new ("special") activity, there's
                // no need to create an artificial back stack.
                PendingIntent resultPendingIntent =
                        PendingIntent.getActivity(
                                context,
                                _id,
                                resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );

                mBuilder.setContentIntent(resultPendingIntent);


                // Gets an instance of the NotificationManager service//

                NotificationManager mNotificationManager =

                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                // When you issue multiple notifications about the same type of event,
                // it’s best practice for your app to try to update an existing notification
                // with this new information, rather than immediately creating a new notification.
                // If you want to update this notification at a later date, you need to assign it an ID.
                // You can then use this ID whenever you issue a subsequent notification.
                // If the previous notification is still visible, the system will update this existing notification,
                // rather than create a new one. In this example, the notification’s ID is 001//

                mNotificationManager.notify(001, mBuilder.build());
            }


        }






    }

    public void setAlarms(final Context context){
        Thread t = new Thread(                        new Runnable() {
            @Override
            public void run() {
                AlarmManagerHelper alarm = new AlarmManagerHelper();

                AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                Intent i = new Intent(context, AlarmManagerHelper.class);
                i.putExtra(Keys.ALARM_MESSAGE,"Günaydın. Egzersiz yapmak için güzel bir gün ☀️☀️");

                PendingIntent pi = PendingIntent.getBroadcast(context, 930, i, 0);
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, 9);
                calendar.set(Calendar.MINUTE, 30);

                am.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY, pi);


                ////////////////////////////////////////////////////////////////////////////////////

                am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                i = new Intent(context, AlarmManagerHelper.class);
                i.putExtra(Keys.ALARM_MESSAGE,"Egzersizlere başladık mı? \uD83C\uDFC3\uD83D\uDC4D️");
                i.putExtra(Keys.ALARM_MESSAGE_SHOW_IF_NOT_FINISHED_PROGRAM,true);
                pi = PendingIntent.getBroadcast(context, 1300, i, 0);
                calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, 13);
                calendar.set(Calendar.MINUTE, 00);

                am.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY, pi);

                ////////////////////////////////////////////////////////////////////////////////////

                am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                i = new Intent(context, AlarmManagerHelper.class);
                i.putExtra(Keys.ALARM_MESSAGE,"Hala bitirilmemiş egzersizlerin var. Yapabilirsin \uD83D\uDCE3\uD83D\uDC4A\uD83D\uDC4A️");
                i.putExtra(Keys.ALARM_MESSAGE_SHOW_IF_FINISHED_PROGRAM,true);
                pi = PendingIntent.getBroadcast(context, 1830, i, 0);
                calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, 18);
                calendar.set(Calendar.MINUTE, 30);

                am.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY, pi);

                ////////////////////////////////////////////////////////////////////////////////////

                am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                i = new Intent(context, AlarmManagerHelper.class);
                i.putExtra(Keys.ALARM_MESSAGE,"Haydi bir gayret daha bitir egzersizlerini \uD83D\uDC4C\uD83D\uDC4C\uD83D\uDCAA️");
                i.putExtra(Keys.ALARM_MESSAGE_SHOW_IF_NOT_FINISHED_PROGRAM_IN_EVENING,true);
                pi = PendingIntent.getBroadcast(context, 2100, i, 0);
                calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, 21);
                calendar.set(Calendar.MINUTE, 00);

                am.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY, pi);

                ////////////////////////////////////////////////////////////////////////////////////

                am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                i = new Intent(context, AlarmManagerHelper.class);
                i.putExtra(Keys.ALARM_MESSAGE,"Haydi bir gayret daha bitir egzersizlerini \uD83D\uDC4C\uD83D\uDC4C\uD83D\uDCAA️");
                i.putExtra(Keys.ALARM_MESSAGE_SHOW_STATISTICS_IN_FRIDAY,true);
                pi = PendingIntent.getBroadcast(context, 2130, i, 0);
                calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.DAY_OF_WEEK,6);
                calendar.set(Calendar.HOUR_OF_DAY, 21);
                calendar.set(Calendar.MINUTE, 30);

                am.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY*7, pi);


            }
        });

        t.start();


    }

}
