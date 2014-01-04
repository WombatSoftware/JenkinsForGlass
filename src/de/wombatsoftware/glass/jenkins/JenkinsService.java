package de.wombatsoftware.glass.jenkins;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
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

	public class JenkinsBinder extends Binder {
        public Jenkins getJenkins() {
        	return jenkins;
        }

        public void republish() {
        	mLiveCard.unpublish();
        	initRemoteViews();
        	initLiveCard();
        }
    }

	private static final String JENKINS_API_PATH = "/api/json";
	private static final String LIVE_CARD_ID = "Jenkins";
	public static final String PREFS_NAME = "JenkinsPreferences";

	private static final String TAG = "JenkinsService";
	private Jenkins jenkins;
	private final JenkinsBinder mBinder = new JenkinsBinder();
	private Intent menuIntent;
	private LiveCard mLiveCard;
	
	private TimelineManager mTimelineManager;

	private RemoteViews remoteViews;

	private String formatStatusMessage(int amount, int totalJobs, String name) {
		return " " + amount + "/" + totalJobs + " " + name;
	}

	private void initJenkins(String url) {
		if(url == null) {
			//throw new Exception
		}

		if(!url.endsWith(JENKINS_API_PATH) && !url.endsWith(JENKINS_API_PATH + "/")) {
			url = url + JENKINS_API_PATH + "/";
		}

		jenkins = Jenkins.createJenkins(url);
	}

	private void initLiveCard() {
		mLiveCard = mTimelineManager.createLiveCard(LIVE_CARD_ID);
		mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));
		mLiveCard.setViews(remoteViews);
		mLiveCard.publish(PublishMode.REVEAL);
	}
	
	private void initRemoteViews() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	    String url = settings.getString("jenkinsUrl", null);

	    if(url == null) {
	    	remoteViews = new RemoteViews(getPackageName(), R.layout.card_setup_needed);	
	    } else {
	    	initJenkins(url);
			remoteViews = new RemoteViews(getPackageName(), R.layout.card_jenkins);
			StatusSummary summary = jenkins.getSummary();

			remoteViews.setTextViewText(R.id.success, formatStatusMessage(summary.getStableJobs(), summary.getTotalJobs(), "Stable"));
			remoteViews.setTextViewText(R.id.unstable, formatStatusMessage(summary.getUnstableJobs(), summary.getTotalJobs(), "Unstable"));
			remoteViews.setTextViewText(R.id.failed, formatStatusMessage(summary.getFailedJobs(), summary.getTotalJobs(), "Failed"));
	    }
	}
	
	@Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

	@Override
	public void onCreate() {
		super.onCreate();

		mTimelineManager = TimelineManager.from(this);
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

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (mLiveCard == null) {
			Log.d(TAG, "Publishing LiveCard");

			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);

			// TODO: Externalize the url

			menuIntent = new Intent(this, MenuActivity.class);
			initRemoteViews();
			initLiveCard();

			Log.d(TAG, "Done publishing LiveCard");
		} else {
			// TODO(w0mbat): Jump to the LiveCard when API is available.
		}

		return START_STICKY;
	}
	
	protected void setJenkins(Jenkins jenkins) {
		this.jenkins = jenkins;
	}
}