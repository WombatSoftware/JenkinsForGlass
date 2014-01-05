package de.wombatsoftware.glass.jenkins.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

import com.google.gson.Gson;

import de.wombatsoftware.glass.jenkins.JenkinsService;

public class Jenkins implements Serializable {
	private static final long serialVersionUID = -4032951634201406937L;

	private static final String TAG = "Jenkins";

	private StatusSummary summary;
	private List<Job> jobs;
	private final String url;
	
	public Jenkins(String url) {
		this.url = url;
		init();
	}

	public StatusSummary getSummary() {
		return summary;
	}
	
	public static Jenkins createJenkins(String url) {
		return new Jenkins(url);
	}

	public void init() {
		Log.d(TAG, "Init Jenkins");

		summary = parseJenkinsFeed(readJenkinsFeed());
	}

	public List<Job> getJobs() {
		return jobs;
	}

	private StatusSummary parseJenkinsFeed(String json) {
		Gson gson = new Gson();
		JenkinsResponse jenkinsResponse = gson.fromJson(json, JenkinsResponse.class);
		
		if(jenkinsResponse == null) {
			return null;
		}

		StatusSummary summary = new StatusSummary();
		jobs = jenkinsResponse.getJobs();

		for (Job j : jobs) {
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

				default:
					summary.addTotalJob();
					break;
			}
		}

		return summary;
	}

	private String readJenkinsFeed() {
		StringBuilder builder = new StringBuilder();

		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);

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
}