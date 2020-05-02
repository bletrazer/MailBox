package fr.bletrazer.mailbox.listeners.utils;

import java.time.Duration;

public class AbstractDuration {
	
	private Duration duration = Duration.ofSeconds(0);
	
	public AbstractDuration() {
		
	}
	
	public Duration getDuration() {
		return duration;
	}

	public void setDuration(Duration duration) {
		this.duration = duration;
	}
	
}
