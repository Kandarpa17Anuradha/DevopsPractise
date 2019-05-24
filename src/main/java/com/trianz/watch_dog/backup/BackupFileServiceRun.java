/**
 * 
 */
package com.trianz.watch_dog.backup;

import java.io.File;
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
public class BackupFileServiceRun {

	/**
	 * @param args
	 */
	static Logger logger = Logger.getLogger("devpinoyLogger");

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String currdir = System.getProperty("user.dir");
		FileInputStream reader = new FileInputStream(currdir + "\\configurations.properties");
		Properties config = new Properties();
		config.load(reader);
		currdir = currdir + "\\" + config.getProperty("log.configs");
		PropertyConfigurator.configure(currdir);
		Path dir = Paths.get(config.getProperty("dir"));
		logger.info("file backup service start");
		CreateBackupFile file = new CreateBackupFile();
		file.createBackup(dir + "\\" + config.getProperty("fileName"),
				new File(config.getProperty("storedfile.directory")));
	}

}
