package de.wombatsoftware.glass.jenkins;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.TimelineManager;
import com.google.android.glass.timeline.LiveCard.PublishMode;
import com.google.gson.Gson;

import de.wombatsoftware.glass.jenkins.model.JenkinsResponse;
import de.wombatsoftware.glass.jenkins.model.Job;
import de.wombatsoftware.glass.jenkins.model.StatusSummary;

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

			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);

			mLiveCard = mTimelineManager.createLiveCard(LIVE_CARD_ID);

			RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.card_jenkins);

			StatusSummary summary = parseJenkinsFeed(readJenkinsFeed());

			remoteViews.setTextViewText(R.id.success, formatStatusMessage(summary.getStableJobs(), summary.getTotalJobs(), "Stable"));
			remoteViews.setTextViewText(R.id.unstable, formatStatusMessage(summary.getUnstableJobs(), summary.getTotalJobs(), "Unstable"));
			remoteViews.setTextViewText(R.id.failed, formatStatusMessage(summary.getFailedJobs(), summary.getTotalJobs(), "Failed"));

			Intent menuIntent = new Intent(this, MenuActivity.class);
			mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent,
					0));
			mLiveCard.setViews(remoteViews);
			mLiveCard.publish(PublishMode.REVEAL);

			Log.d(TAG, "Done publishing LiveCard");
		} else {
			// TODO(w0mbat): Jump to the LiveCard when API is available.
		}

		return START_STICKY;
	}

	private String formatStatusMessage(int amount, int totalJobs, String name) {
		return " " + amount + "/" + totalJobs + " " + name;
	}

	private String readJenkinsFeed() {
		StringBuilder builder = new StringBuilder();

		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet("http://ci.wombatsoftware.de/api/json");

		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();

			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(content));
				String line;

				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
			} else {
				Log.e(JenkinsService.class.toString(), "Failed to download file");
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return builder.toString();
	}

	private StatusSummary parseJenkinsFeed(String json) {
		Gson gson = new Gson();
		JenkinsResponse jenkinsResponse = gson.fromJson(json, JenkinsResponse.class);

		StatusSummary summary = new StatusSummary();

		for (Job j : jenkinsResponse.getJobs()) {
			switch (j.getColor()) {
				case blue:
					summary.addStableJob();
					break;
	
				case yellow:
					summary.addUnstableJob();
					break;
	
				case red:
					summary.addFailedJob();
					break;
			}
		}

		return summary;
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