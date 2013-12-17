package de.wombatsoftware.glass.jenkins.model;

import java.util.List;

public class JenkinsResponse {
	private List<Job> jobs;

	public List<Job> getJobs() {
		return jobs;
	}

	public void setJobs(List<Job> jobs) {
		this.jobs = jobs;
	}
}