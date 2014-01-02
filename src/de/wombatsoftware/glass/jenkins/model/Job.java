package de.wombatsoftware.glass.jenkins.model;

import java.io.Serializable;

public class Job implements Serializable {
	public enum Color {
		aborted, aborted_anime, blue, blue_anime, disabled, notbuilt, notbuilt_anime, red, red_anime, yellow, yellow_anime
	}

	private static final long serialVersionUID = -8897635463581572109L;

	private Color color;
	private String name;
	private String url;

	public Color getColor() {
		return color;
	}

	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}