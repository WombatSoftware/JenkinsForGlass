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
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;

public class Jenkins implements Serializable {
	public class Credentials implements Serializable {
		private static final long serialVersionUID = -7625113581627279690L;

		private String token;
		private String user;

		public Credentials(){};

		public Credentials(String user, String token) {
			super();
			this.user = user;
			this.token = token;
		}

		public String getToken() {
			return token;
		}

		public String getUser() {
			return user;
		}

		public void setToken(String token) {
			this.token = token;
		}

		public void setUser(String user) {
			this.user = user;
		}
	}

	private static final String EMPTY_STRING = "";

	private static final String GROUP_DELIMITER = ";";
	private static final String JENKINS_API_PATH = "/api/json";
	private static final String JENKINS_GROUP = "Jenkins:";
	private static final long serialVersionUID = -4032951634201406937L;
	private static final String TAG = "Jenkins";
	private static final String TOKEN_GROUP = "T:";
	private static final String USER_GROUP = "U:";

	public static Jenkins createJenkins(String url) {
		return new Jenkins(url);
	}

	private Credentials credentials = new Credentials();
	private List<Job> jobs;

	private StatusSummary summary;

	private String url;

	private Jenkins(String qrContent) {
		init(qrContent);
	}

	public void createSecurityHeader(HttpRequestBase base) {
		String auth = credentials.getUser() + ":" + credentials.getToken();
		String base64EncodedCredentials = Base64.encodeToString(auth.getBytes(), Base64.NO_WRAP);
		base.addHeader("Authorization", "Basic " + base64EncodedCredentials);
	}

	public Credentials getCredentials() {
		return credentials;
	}

	public List<Job> getJobs() {
		return jobs;
	}

	public StatusSummary getSummary() {
		return summary;
	}

	public void init() {
		Log.d(TAG, "Init Jenkins");
		summary = parseJenkinsFeed(readJenkinsFeed());
	}
	
	public void init(String qrContent) {
		Log.d(TAG, "Init Jenkins with QR-Content");

		extractToken(qrContent);
		init();
	}

	private String convertEntityToString(HttpResponse response) {
		StringBuilder builder = new StringBuilder();
		HttpEntity entity = response.getEntity();
		InputStream content;

		try {
			content = entity.getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(content));
			String line;

			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
		} catch (IllegalStateException e) {
			Log.e(TAG, "IllegalStateException orrured", e);
		} catch (IOException e) {
			Log.e(TAG, "IOException orrured", e);
		}

		return builder.toString();
	}

	private void extractToken(String qrContent) {
		Log.d(TAG, "QR-Content: " + qrContent);
		
		for(String group : qrContent.split(GROUP_DELIMITER)) {
			Log.d(TAG, "Group-Content: " + group);
			
			if(group.startsWith(JENKINS_GROUP)) {
				this.url = group.replaceAll(JENKINS_GROUP, EMPTY_STRING);

				if(!url.contains(JENKINS_API_PATH) && !url.contains(JENKINS_API_PATH + "/")) {
					url = url + JENKINS_API_PATH + "/";
				}
			} else if(group.startsWith(USER_GROUP)) {
				this.credentials.setUser(group.replaceAll(USER_GROUP, EMPTY_STRING));
			} else if(group.startsWith(TOKEN_GROUP)) {
				this.credentials.setToken(group.replaceAll(TOKEN_GROUP, EMPTY_STRING));
			}
		}
	}

	private StatusSummary parseJenkinsFeed(String json) {
		Gson gson = new Gson();
		JenkinsResponse jenkinsResponse = gson.fromJson(json, JenkinsResponse.class);

		if (jenkinsResponse == null) {
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
		Log.d("Jenkins", "Read-URL: " + url);

		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		
		if(credentials.getUser() != null && credentials.getToken() != null) {
			createSecurityHeader(httpGet);
		}

		HttpResponse response = null;

		try {
			response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();

			if(statusCode != 200) {
				Log.e(TAG, "Status:" + statusCode);
				Log.e(TAG, convertEntityToString(response));
				return null;
			}
		} catch (ClientProtocolException e) {
			Log.e(TAG, "ClientProtocolException orrured", e);
		} catch (IOException e) {
			Log.e(TAG, "IOException orrured", e);
		}

		return convertEntityToString(response);
	}
}