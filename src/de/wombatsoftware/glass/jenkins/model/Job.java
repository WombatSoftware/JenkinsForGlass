package de.wombatsoftware.glass.jenkins.model;

import java.io.Serializable;

public class Job implements Serializable {
	private static final long serialVersionUID = -8897635463581572109L;

	public enum Color {
		blue, yellow, red
	};

	private String name;
	private String url;
	private Color color;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}
}