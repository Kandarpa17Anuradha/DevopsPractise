/**
 * 
 */
package com.trianz.watch_dog.watchfile;

import static java.nio.file.StandardWatchEventKinds.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.trianz.watch_dog.backup.CreateBackupFile;

/**
 * @author Mahendhar.Nallabolu
 *
 */
public class WatchFileService {
	private final WatchService watcher;
	private final Map<WatchKey, Path> keys;
	private final String directory = "user.dir";
	String currdir = System.getProperty(directory);
	FileInputStream reader;
	Properties config = new Properties();
	Logger logger = Logger.getLogger("devpinoyLogger");

	/**
	 * Creates a WatchService and registers the given directory
	 */
	WatchFileService(Path dir) throws IOException {
		this.watcher = FileSystems.getDefault().newWatchService();
		this.keys = new HashMap<WatchKey, Path>();
		reader = new FileInputStream(currdir + "//" + "config//configurations.properties");
		callRegisterDirectories(dir);
	}

	/**
	 * Register the given directory with the WatchService; This function will be
	 * called by FileVisitor
	 */
	private void registerDirectory(Path dir) throws IOException {
		WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
		keys.put(key, dir);
	}

	/**
	 * Register the given directory, and all its sub-directories, with the
	 * WatchService.
	 */
	private void callRegisterDirectories(final Path start) throws IOException {

		// register directory and sub-directories
		Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				registerDirectory(dir);
				logger.warn("call register directory service");
				return FileVisitResult.CONTINUE;
			}
		});
	}

	/**
	 * Process all events for keys queued to the watcher
	 * 
	 * @throws IOException
	 */
	void processEvents() throws IOException {
		config.load(reader);
		String currdir = System.getProperty("user.dir");
		currdir = currdir + "\\" + config.getProperty("log.configs");
		PropertyConfigurator.configure(currdir);
		for (;;) {

			// wait for key to be signalled
			WatchKey key;
			try {
				key = watcher.take();
			} catch (InterruptedException x) {
				return;
			}

			Path dir = keys.get(key);
			logger.info("event triggered for directory" + dir);
			if (dir == null) {
				logger.error("WatchKey not recognized!!");
				continue;
			}
			for (WatchEvent<?> event : key.pollEvents()) {
				@SuppressWarnings("rawtypes")
				WatchEvent.Kind kind = event.kind();
				// Context for directory entry event is the file name of entry
				@SuppressWarnings("unchecked")
				Path name = ((WatchEvent<Path>) event).context();
				Path child = dir.resolve(name);
				logger.info(event.kind() + "event triggered" + event.context() + "is occured");
				// checking for the modify event type
				if (event.kind().name().equals("ENTRY_MODIFY")) {
					logger.debug("child file is: " + child + "comparing file is: "
							+ config.getProperty("comparison.filename"));
					String[] s1 = config.getProperty("comparison.filename").split(",");
					logger.debug("debug values: " + s1);
					// checking the specified file changed condition
					// for creating backup file
					for (int i = 0; i < s1.length; i++) {
						if (s1[i].equalsIgnoreCase(child.toString())) {
							logger.info("child file comparison matches");
							CreateBackupFile file = new CreateBackupFile();
							file.createBackup(dir + "\\" + child.getFileName().toString(),
									new File(config.getProperty("storedfile.directory")));
						} else {
							logger.info("child file comparison not matched");
						}
					}

				}
				// if directory is created, and watching recursively, then register it and its
				// sub-directories
				if (kind == ENTRY_CREATE) {
					try {
						if (Files.isDirectory(child)) {
							callRegisterDirectories(child);
						}
					} catch (IOException x) {
						// exception message captured
						logger.error("exception occured" + x.getMessage());

					}
				}
			}

			// reset key and remove from set if directory no longer accessible
			boolean valid = key.reset();
			if (!valid) {
				keys.remove(key);
				// all directories are inaccessible
				if (keys.isEmpty()) {
					break;
				}
			}
		}
	}

}
