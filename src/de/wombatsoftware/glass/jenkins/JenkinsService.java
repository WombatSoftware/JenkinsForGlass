package de.wombatsoftware.glass.jenkins;

import java.util.Timer;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;
import com.google.android.glass.timeline.TimelineManager;

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

        public void refreshJenkins() {
        	JenkinsService.this.refreshJenkins();
        }
    }

	public static final String PREFS_JENKINS_URL = "de.wombatsoftware.glass.jenkins.url";
	public static final String PREFS_NAME = "JenkinsPreferences";
	
	private static final String LIVE_CARD_ID = "Jenkins";
	private static final String TAG = "JenkinsService";
	private static final long timerTimeout = 1000 * 60 * 30;

	private Jenkins jenkins;
	private final JenkinsBinder mBinder = new JenkinsBinder();
	private Intent menuIntent;
	private LiveCard mLiveCard;
	private TimelineManager mTimelineManager;
	private RefreshTask refreshTask;
	private RemoteViews remoteViews;
	private Timer timer;
	
	public Jenkins getJenkins() {
		return jenkins;
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

			timer.cancel();
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

			initJenkins();
			initRemoteViews();
			initLiveCard();
			initRefreshTimer();

			Log.d(TAG, "Done publishing LiveCard");
		} else {
			// TODO(w0mbat): Jump to the LiveCard when API is available.
		}

		return START_STICKY;
	}

	public void refreshJenkins() {
		StatusSummary summary = jenkins.getSummary();

		int failedJobsBefore = summary.getFailedJobs();
		int unstableJobsBefore = summary.getUnstableJobs();
		int stableJobsBefore = summary.getStableJobs();

		initJenkins();
    	initRemoteViews();
    	mLiveCard.setViews(remoteViews);

    	summary = jenkins.getSummary();

		int failedJobsAfter = summary.getFailedJobs();
		int unstableJobsAfter = summary.getUnstableJobs();
		int stableJobsAfter = summary.getStableJobs();

		if(failedJobsAfter > failedJobsBefore || unstableJobsAfter > unstableJobsBefore) {
			playSound(Sounds.ERROR);
		} else if (stableJobsAfter > stableJobsBefore) {
			playSound(Sounds.SUCCESS);
		}
    }

	private void deleteJenkinsUrl() {
		SharedPreferences settings = getSharedPreferences(JenkinsService.PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.remove(JenkinsService.PREFS_JENKINS_URL);
	    editor.commit();

	    jenkins = null;
	}
	
	private String formatStatusMessage(int amount, int totalJobs, String name) {
		return " " + amount + "/" + totalJobs + " " + name;
	}

	private void initJenkins() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		String url = settings.getString(PREFS_JENKINS_URL, null);

		if(url == null) {
			jenkins = null;
		} else {
			initJenkins(url);
		}
	}

	private void initJenkins(String qrContent) {
		jenkins = Jenkins.createJenkins(qrContent);

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

	private void initRefreshTimer() {
		refreshTask = new RefreshTask(this);
		timer = new Timer();
		timer.scheduleAtFixedRate(refreshTask, timerTimeout, timerTimeout);
	}

	private void initRemoteViews() {
	    if(jenkins == null) {
	    	remoteViews = new RemoteViews(getPackageName(), R.layout.card_setup_needed);	
	    } else {
	    	StatusSummary summary = jenkins.getSummary();

			remoteViews = new RemoteViews(getPackageName(), R.layout.card_jenkins);
			remoteViews.setTextViewText(R.id.success, formatStatusMessage(summary.getStableJobs(), summary.getTotalJobs(), "Stable"));
			remoteViews.setTextViewText(R.id.unstable, formatStatusMessage(summary.getUnstableJobs(), summary.getTotalJobs(), "Unstable"));
			remoteViews.setTextViewText(R.id.failed, formatStatusMessage(summary.getFailedJobs(), summary.getTotalJobs(), "Failed"));
	    }
	}
	
	private void playSound(int sound) {
		AudioManager audio = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		audio.playSoundEffect(sound);
	}
}