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
		
		try
		{
			this.execute();
		}
		catch(Exception e)
		{
			System.out.println("Job Failed -> " + this.jobName);
			System.out.println("Job Fail Reason -> " + e.getLocalizedMessage());
			e.printStackTrace();
			
			return;
		}
		
		System.out.println("Finished Job -> " + this.jobName);
		return;
	}

	public abstract void execute();
	
	
	
}
