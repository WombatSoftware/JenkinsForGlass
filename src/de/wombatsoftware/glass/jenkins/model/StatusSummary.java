package de.wombatsoftware.glass.jenkins.model;

public class StatusSummary {
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
	
	private void addTotalJob() {
		totalJobs++;
	}
}