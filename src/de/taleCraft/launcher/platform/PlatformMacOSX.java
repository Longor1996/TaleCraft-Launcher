package de.taleCraft.launcher.platform;

public class PlatformMacOSX extends Platform {

	@Override
	public String getLWJGLLibraryName() {
		return "macosx";
	}

	@Override
	public String getSimpleName() {
		return "osx";
	}

}
