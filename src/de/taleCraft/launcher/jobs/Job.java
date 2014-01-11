package de.taleCraft.launcher.jobs;

public abstract class Job<T> implements Runnable {
	public final String jobName;
	private boolean finished;
	private boolean success;
	private T returnObject;
	
	public Job(String name)
	{
		this.jobName = name;
		this.finished = false;
	}
	
	@Override
	public final void run() {
		// Reset, Job Object Data
		this.returnObject = null;
		this.finished = false;
		this.success = false;
		
		// ---------------------- EXECUTION ---------------------- //
		System.out.println("[JOB] Execution of Job -> " + this.jobName);
		
		Object jobFailureReturnObject = null;
		
		try
		{
			jobFailureReturnObject = this.execute();
		}
		catch(Throwable e)
		{
			jobFailureReturnObject = e;
		}
		
		if(jobFailureReturnObject == null)
		{
			// ---------------------- SUCCESS ---------------------- //
			System.out.println("[JOB] Finished Job -> " + this.jobName);
			
			if(this.returnObject != null)
			{
				System.out.println("[JOB] Job Result: " + this.returnObject);;
			}
			
			this.success = true;;
		}
		else
		{
			// ---------------------- FAILURE ---------------------- //
			if(jobFailureReturnObject instanceof Exception)
			{
				System.out.println("[JOB] The Job '"+this.jobName+"' failed because of an Exception.");
				System.out.println("[JOB] Job Failure Exception -> " + ((Exception) jobFailureReturnObject).getLocalizedMessage());
				((Exception) jobFailureReturnObject).printStackTrace();
			}
			else
			{
				System.out.println("[JOB] The Job '"+this.jobName+"' failed because it reported a error object.");
				System.out.println("[JOB] Job Error Object Class -> " + jobFailureReturnObject.getClass().getName());
				System.out.println("[JOB] Job Error Object -> " + jobFailureReturnObject);
			}
			
			this.success = false;;
		}
		
		this.finished = true;
		return;
	}
	
	public abstract Object execute();
	
	public boolean isFinished()
	{
		return this.finished;
	}
	
	public boolean isSuccess()
	{
		if(!this.finished)
			throw new IllegalStateException("Execution is not yet finished!");
		
		return this.success;
	}
	
	public void setResult(T returnObject)
	{
		this.returnObject = returnObject;
	}
	
	public T getResult()
	{
		return this.returnObject;
	}
	
}
