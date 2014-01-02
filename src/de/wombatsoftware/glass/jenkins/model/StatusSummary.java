package de.wombatsoftware.glass.jenkins.model;

import java.io.Serializable;

public class StatusSummary implements Serializable {
	private static final long serialVersionUID = 5441353590038015315L;

	private int totalJobs;
	private int stableJobs;
	private int unstableJobs;
	private int failedJobs;
	
	public int getTotalJobs() {
		return totalJobs;
	}

	public int getStableJobs() {
		return stableJobs;
	}

	public int getUnstableJobs() {
		return unstableJobs;
	}

	public int getFailedJobs() {
		return failedJobs;
	}

	public void addStableJob() {
		stableJobs++;
		addTotalJob();
	}
	
	public void addUnstableJob() {
		unstableJobs++;
		addTotalJob();
	}
	
	public void addFailedJob() {
		failedJobs++;
		addTotalJob();
	}
	
	public void addTotalJob() {
		totalJobs++;
	}
}