package de.taleCraft.launcher.jobs;

public class STJ_UpdateTalecraft extends Job<Object> {
	Job<?> preThread;
	
	public STJ_UpdateTalecraft(Job<?> job) {
		super("TaleCratUpdateJob-main");
		this.preThread = job;
	}
	
	@Override
	public Object execute() {
		
		// If there is a pre-Job, wait until it finishes!
		if(this.preThread != null)
			while(!this.preThread.isFinished()) Thread.yield();
		
		// Now do stuff!
		
		
		
		return null;
	}
	
}
