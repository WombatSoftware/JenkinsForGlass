package de.wombatsoftware.glass.jenkins;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.TimelineManager;

/**
 * Service owning the LiveCard living in the timeline.
 */
public class JenkinsService extends Service {

    private static final String TAG = "JenkinsService";
    private static final String LIVE_CARD_ID = "Jenkins";

    private TimelineManager mTimelineManager;
    private LiveCard mLiveCard;

    @Override
    public void onCreate() {
        super.onCreate();
        mTimelineManager = TimelineManager.from(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mLiveCard == null) {
            Log.d(TAG, "Publishing LiveCard");
            
            mLiveCard = mTimelineManager.getLiveCard(LIVE_CARD_ID);
            
            RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.card_jenkins);
            remoteViews.setTextViewText(R.id.success, " 0/5 Success");
            remoteViews.setTextViewText(R.id.unstable, " 0/5 Unstable");
            remoteViews.setTextViewText(R.id.failed, " 0/5 Failed");

            Intent menuIntent = new Intent(this, MenuActivity.class);
            mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));
            mLiveCard.setViews(remoteViews);
            mLiveCard.setNonSilent(true);
            mLiveCard.publish();
            Log.d(TAG, "Done publishing LiveCard");
        } else {
            // TODO(w0mbat): Jump to the LiveCard when API is available.
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mLiveCard != null && mLiveCard.isPublished()) {
            Log.d(TAG, "Unpublishing LiveCard");

            mLiveCard.unpublish();
            mLiveCard = null;
        }

        super.onDestroy();
    }
}