/**
 * 
 */
package com.trianz.watch_dog.watchfile;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * @author Mahendhar.Nallabolu
 *
 */
public class WatchFileServiceRun {

	/**
	 * @param args
	 */
	static Logger logger = Logger.getLogger("devpinoyLogger");
	static final String directory = "user.dir";

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String currdir = System.getProperty(directory);
		System.out.println(currdir);
		FileInputStream reader = new FileInputStream(currdir + "\\config\\configurations.properties");
		Properties config = new Properties();
		config.load(reader);
		currdir = currdir + "\\" + config.getProperty("log.configs");
		logger.debug("current directory: " + currdir);
		PropertyConfigurator.configure(currdir);
		Path dir = Paths.get(config.getProperty("dir"));
		logger.debug("watch file service start");
		WatchFileService watchService = new WatchFileService(dir);
		watchService.processEvents();
	}

}
