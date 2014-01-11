package de.taleCraft.launcher.jobs;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.google.gson.JsonObject;
import com.google.gson.JsonStreamParser;

public class DownloadIndexInfo {
	final boolean isValid;
	final JsonObject docRoot;
	final String versionString;
	
	public DownloadIndexInfo(File file) {
		JsonObject r = null;
		boolean v = false;
		
		try {
			FileReader fileReader = new FileReader(file);
			JsonStreamParser reader = new JsonStreamParser(fileReader);
			r = reader.next().getAsJsonObject();
			fileReader.close();
			v = true;
		} catch (IOException e) {
			e.printStackTrace();
			r = null;
			v = false;
		}
		
		this.docRoot = r;
		this.isValid = v;
		
		if(this.isValid)
		{
			; // Index is valid, so there should be a version!
			this.versionString = this.docRoot.get("tc-version").getAsString();
		}
		else
		{
			; // Index is not valid, so there is no version!
			this.versionString = null;
		}
		
	}

}
