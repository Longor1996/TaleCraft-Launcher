package de.taleCraft.launcher.jobs;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;

public class DownloadJob extends Job<File>{
	String source;
	File destination;
	
	public DownloadJob(String name, String source, File file) {
		super("download-thread:"+name);
		this.source = source;
		this.destination = file;
	}
	
	@Override
	public File execute() throws Throwable {
		this.setResult(this.destination);
		
		try {
			FileUtils.copyURLToFile(new URL(this.source), this.destination, 5000, 2000);
		} catch (IOException e) {
			// Oh, the download failed... the job failed.
			throw e;
		}
		
		// We did it! We successfully downloaded the File!
		
		return null;
	}
	
	
	
}
