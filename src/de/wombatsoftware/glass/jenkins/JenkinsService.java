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
        	initRemoteViews();
        	mLiveCard.setViews(remoteViews);
        }
    }

	private static final String LIVE_CARD_ID = "Jenkins";
	public static final String PREFS_JENKINS_URL = "de.wombatsoftware.glass.jenkins.url";
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

		jenkins = Jenkins.createJenkins(url);
		
		if(jenkins.getSummary() == null) {
			deleteJenkinsUrl();
		}
	}

	private void initLiveCard() {
		mLiveCard = mTimelineManager.createLiveCard(LIVE_CARD_ID);
		mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));
		mLiveCard.setViews(remoteViews);
		mLiveCard.publish(PublishMode.REVEAL);
	}

	private void initRemoteViews() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	    String url = settings.getString(PREFS_JENKINS_URL, null);

	    if(url == null) {
	    	remoteViews = new RemoteViews(getPackageName(), R.layout.card_setup_needed);	
	    } else {
	    	try {
	    		initJenkins(url);
	    	} catch (Exception e) {
	    		deleteJenkinsUrl();
	    	}

	    	if(jenkins == null) {
	    		initRemoteViews();
	    		return;
	    	}

	    	StatusSummary summary = jenkins.getSummary();

			remoteViews = new RemoteViews(getPackageName(), R.layout.card_jenkins);
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
	
	private void deleteJenkinsUrl() {
		SharedPreferences settings = getSharedPreferences(JenkinsService.PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.remove(JenkinsService.PREFS_JENKINS_URL);
	    editor.commit();

	    jenkins = null;
	}
}