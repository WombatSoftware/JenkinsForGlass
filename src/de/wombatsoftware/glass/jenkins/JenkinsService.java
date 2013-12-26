package de.wombatsoftware.glass.jenkins;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.TimelineManager;
import com.google.android.glass.timeline.LiveCard.PublishMode;

import de.wombatsoftware.glass.jenkins.model.Jenkins;
import de.wombatsoftware.glass.jenkins.model.StatusSummary;

/**
 * Service owning the LiveCard living in the timeline.
 */
public class JenkinsService extends Service {

	private static final String TAG = "JenkinsService";
	private static final String LIVE_CARD_ID = "Jenkins";

	private TimelineManager mTimelineManager;
	private LiveCard mLiveCard;
	private Jenkins jenkins;
	private final JenkinsBinder mBinder = new JenkinsBinder();
	
	private RemoteViews remoteViews;

	@Override
	public void onCreate() {
		super.onCreate();

		mTimelineManager = TimelineManager.from(this);
	}

	public class JenkinsBinder extends Binder {
        public Jenkins getJenkins() {
        	return jenkins;
        }

        public void republish() {
        	mLiveCard.unpublish();
        	initRemoteViews();
        	mLiveCard.setViews(remoteViews);
			mLiveCard.publish(PublishMode.REVEAL);
        }
    }

	@Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (mLiveCard == null) {
			Log.d(TAG, "Publishing LiveCard");

			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);

			mLiveCard = mTimelineManager.createLiveCard(LIVE_CARD_ID);

			remoteViews = new RemoteViews(getPackageName(), R.layout.card_jenkins);
			
			// TODO: Externalize the url
			jenkins = Jenkins.createJenkins("http://ci.wombatsoftware.de/api/json");
			
			initRemoteViews();

			Intent menuIntent = new Intent(this, MenuActivity.class);
			mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));
			mLiveCard.setViews(remoteViews);
			mLiveCard.publish(PublishMode.REVEAL);

			Log.d(TAG, "Done publishing LiveCard");
		} else {
			// TODO(w0mbat): Jump to the LiveCard when API is available.
		}

		return START_STICKY;
	}
	
	private void initRemoteViews() {
		StatusSummary summary = jenkins.getSummary();

		remoteViews.setTextViewText(R.id.success, formatStatusMessage(summary.getStableJobs(), summary.getTotalJobs(), "Stable"));
		remoteViews.setTextViewText(R.id.unstable, formatStatusMessage(summary.getUnstableJobs(), summary.getTotalJobs(), "Unstable"));
		remoteViews.setTextViewText(R.id.failed, formatStatusMessage(summary.getFailedJobs(), summary.getTotalJobs(), "Failed"));
	}

	private String formatStatusMessage(int amount, int totalJobs, String name) {
		return " " + amount + "/" + totalJobs + " " + name;
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
	
	protected void setJenkins(Jenkins jenkins) {
		this.jenkins = jenkins;
	}
}