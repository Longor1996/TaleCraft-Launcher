package de.taleCraft.launcher.jobs;

enum EnumUpdateCheckResult
{
	// This will do nothing, user can instantly play.
	CURRENT_IS_NEWER(true, true), // This will do nothing, user can instantly play.
	SAME_VERSION(true, true), // This will do nothing, user can instantly play.
	LOCAL_AVAIBLE_ONLINE_NOT(true, false), // This will do nothing, user can instantly play.
	
	// This will prompt the user to update.
	DOWNLOAD_IS_NEWER(true, true), // This will prompt the user to update.
	NO_LOCAL_ONLINE_AVAIBLE(false, true), // This will prompt the user to update.
	
	// This will cause a error message!
	NO_VERSION_AVAIBLE_LOCAL_NOR_ONLINE(false, false);
	
	public final boolean canPlay;
	public final boolean canUpdate;
	
	EnumUpdateCheckResult(boolean arg0, boolean arg1)
	{
		this.canPlay = arg0;
		this.canUpdate = arg1;
	}
}