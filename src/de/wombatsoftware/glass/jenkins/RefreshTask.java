package de.wombatsoftware.glass.jenkins;

import java.util.TimerTask;

import android.util.Log;

public class RefreshTask extends TimerTask {
	private JenkinsService jenkinsService;

	public RefreshTask(JenkinsService jenkinsService) {
		this.jenkinsService = jenkinsService;
	}

	@Override
	public void run() {
		Log.d("RefreshTask", "Timer starts");
		jenkinsService.refreshJenkins();
		Log.d("RefreshTask", "Timer finished");		
	}
}