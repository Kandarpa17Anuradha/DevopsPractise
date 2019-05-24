/**
 * 
 */
package com.trianz.watch_dog.backup;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.log4j.Logger;

/**
 * @author Mahendhar.Nallabolu
 *
 */
public class CreateBackupFile {
	Logger logger = Logger.getLogger("devpinoyLogger");

	public void createBackup(String filename, File destfilepath) throws IOException {
		File originalFile = new File(filename);
		Date date = new Date();
		DateFormat formatter = new SimpleDateFormat("ddMMyyyyHHmms");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		String dateFormatted = formatter.format(date);
		File backupFile = new File(destfilepath.getAbsolutePath()
				.concat("\\" + originalFile.getName().concat(dateFormatted).concat(".backup")));
		Files.copy(originalFile.toPath(), backupFile.toPath());
		logger.info("back up file: " + backupFile.getName() + " is created");
	}
}
