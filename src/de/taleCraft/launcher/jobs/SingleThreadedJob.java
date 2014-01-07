package de.taleCraft.launcher.jobs;

public abstract class SingleThreadedJob implements Runnable {
	public final String jobName;
	
	public SingleThreadedJob(String name)
	{
		this.jobName = name;
	}
	
	@Override
	public final void run() {
		System.out.println("Execution of Job -> " + this.jobName);
		this.execute();
	}

	public abstract void execute();
	
	
	
}
