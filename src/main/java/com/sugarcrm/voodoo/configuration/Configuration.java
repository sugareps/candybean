package com.sugarcrm.voodoo.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Logger;

import junit.framework.Assert;

import com.sugarcrm.voodoo.utilities.OptionalLogger;
import com.sugarcrm.voodoo.utilities.Utils;

public class Configuration extends Properties {
	private static final long serialVersionUID = 1L;
	private static int defaultName = 0;
	private OptionalLogger log;
	private String configPath;
	private Boolean createdFile = false;

	public OptionalLogger getLogger() {
		return log;
	}

	public void setLogger(OptionalLogger log) {
		this.log = log;
	}

	public String getConfigPath() {
		return configPath;
	}

	public void setConfigPath(String configPath) {
		this.configPath = configPath;
	}

	public Configuration() {
		this(null);
	}

	public Configuration(Logger log) {
		this.setLogger(new OptionalLogger(log));
	}

	/**
	 * @author ylin
	 */
	public void createFile() {
		createFile(Integer.toString(defaultName) + ".properties", true);
		defaultName++;
	}

	/**
	 * @author ylin
	 * 
	 * @param absolutePath
	 */
	public void createFile(String absolutePath) {
		createFile(absolutePath, false);
	}

	/**
	 * 
	 * 
	 * @author ylin
	 * 
	 * @param log
	 * @param fileName
	 * @param temporary
	 */
	private void createFile(String fileName, Boolean temporary) {
		if (temporary) {
			String tempPath = System.getProperty("user.home") + File.separator + "TemporaryConfigurationFiles" + File.separator + fileName;
			log.info("Using temporary path " + tempPath + ".\n");
			setConfigPath(tempPath);
		} else {
			log.info("Using path " + fileName + ".\n");
			setConfigPath(fileName);
		}
		File file = new File(getConfigPath());
		File dir = new File(file.getParent());

		if (!dir.exists()) {
			log.info("Parent directory does not exist for " + file.getName() + ", creating folder(s).\n");
			boolean createdDirs = dir.mkdirs();
			// check and log directory creation
			if (createdDirs) {
				log.info("Created folder(s) " + file.getParent() + ".\n");
			} else 
				log.severe("Unable to create folder(s) " + file.getParent() + ".\n");
		}

		Boolean fileExisted = file.exists();
		if (fileExisted)
			log.info(file.getName() + " exists, proceeding to overwrite file.\n");
		// write the Configuration object to the path specified by file
		try {
			store(file, null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// check and log whether created file exists
		if (!fileExisted) {
			if (file.exists())
				log.info("Created file " + file.getName() + ".\n");
			else
				log.severe("Unable to create file " + file.getName() + ".\n");

			// assert file exists
			Assert.assertTrue(file.getName() + " was not created!", file.exists());
		}

		// load() has logging built in
		this.load(getConfigPath());

		// set createdFile to true, allow deleteFile() to delete file
		createdFile = true;
	}

	/**
	 * 
	 * 
	 * @author ylin
	 */
	public void deleteFile() {
		if (!createdFile) {
			log.severe("No file was created, deleteFile() aborted.");
		} else {
			File file = new File(getConfigPath());
			File dir = file.getParentFile();

			boolean fileDeleted = file.delete();

			// check delete succeeded and file no longer exists
			if (fileDeleted && !file.exists()) {
				getLogger().info("Deleted file " + file.getName() + ".\n");
				configPath = null;
				createdFile = false;
			} else
				getLogger().info("Unable to delete file " + file.getName() + ".\n");

			// delete all empty parent folders
			recursiveFolderDelete(file);

			// assert file does not exist
			Assert.assertTrue(file.getName() + " was not deleted!", !file.exists());
			// assert parent directory does not exist
			Assert.assertTrue(dir.getAbsolutePath() + " was not deleted!", !dir.exists());
		}
	}

	private void recursiveFolderDelete(File file) {
		File dir = file.getParentFile();
		if (dir.list().length == 0) {
			boolean dirDeleted = dir.delete();
			if (dirDeleted && !file.exists()) 
				getLogger().info("Deleted folder " + dir.getAbsolutePath() + ".\n");
			else
				getLogger().info("Unable to delete folder " + dir.getAbsolutePath() + ".\n");
			recursiveFolderDelete(file.getParentFile());
		}
	}

	/**
	 * NOTE: This method takes in a path of type String instead of a FileInputStream object
	 * to add path robustness by calling 'Utils.adjustPath' and then the actual load method
	 * 
	 * @author wli
	 * 
	 * @param filePath
	 * @throws FileNotFoundException 
	 * @throws IOException 
	 * @throws Exception
	 */
	public void load(String filePath) {
		String adjustedPath = Utils.adjustPath(filePath);
		try {
			load(new FileInputStream(new File(adjustedPath)));
		} catch (FileNotFoundException e) {
			// get file name using substring of adjustedPath that starts after the last /
			getLogger().severe(adjustedPath.substring(adjustedPath.lastIndexOf('/') + 1) + " not found.\n");
			e.printStackTrace();
		} catch (IOException e) {
			getLogger().severe("Unable to load " + adjustedPath.substring(adjustedPath.lastIndexOf('/') + 1) + ".\n");
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * 
	 * @author ylin
	 * 
	 * @param file
	 */
	public void load(File file) {
		try {
			load(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			getLogger().severe(file.getName() + " not found.\n");
			e.printStackTrace();
		} catch (IOException e) {
			getLogger().severe("Unable to load " + file.getName() + ".\n");
			e.printStackTrace();
		}
	}
	
	/**
	 * NOTE: This method takes in a path of type String instead of a FileOutputStream object
	 * to add path robustness by calling 'Utils.adjustPath' and then the actual store method
	 * 
	 * @author wli
	 * 
	 * @param filePath
	 * @param comments
	 * @throws FileNotFoundException 
	 * @throws IOException 
	 * @throws Exception
	 */
	public void store(String filePath, String comments) throws FileNotFoundException, IOException {
		String adjustedPath = Utils.adjustPath(filePath);
		store(new FileOutputStream(new File(adjustedPath)), comments);
	}

	/**
	 * 
	 * 
	 * @author ylin
	 * 
	 * @param file
	 * @param comments
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public void store(File file, String comments) throws FileNotFoundException, IOException {
		store(new FileOutputStream(file), comments);
	}

	/**
	 * This method overrides the extended getProperty(key, defaultValue) method 
	 * to support cascading value
	 * 
	 * @author wli
	 * 
	 * @param path
	 * @return a cascaded value
	 */
	public String getProperty(String key, String defaultValue) {
		return getCascadingPropertyValue(this, defaultValue, key);
	}

	/**
	 * This is a newly added method (with defaultValue) to retrieve a path
	 * from the properties file and safely return it after calling Utils.adjustPath
	 * 
	 * @author wli
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public String getPathProperty(String key, String defaultValue) {
		String pathValue = getCascadingPropertyValue(this, defaultValue, key);
		return Utils.adjustPath(pathValue);
	}

	/**
	 * This is a newly added method (without defaultValue) to retrieve a path 
	 * from the properties file and safely return it after calling Utils.adjustPath
	 * 
	 * @author wli
	 * 
	 * @param key
	 * @return
	 */
	public String getPathProperty(String key) {
		String pathValue = getProperty(key);
		return Utils.adjustPath(pathValue);
	}

	/**
	 * Takes the value and split them according to the given
	 * delimiter and return a String[]. 
	 * (Example, "FRUITS = apple pear banana)
	 * delimiter: " "
	 * and returns String[]: apple pear banana
	 * 
	 * @author wli
	 * 
	 * @param key
	 * @param delimiter
	 * @return
	 */
	public String[] getPropertiesArray(String key, String delimiter) {
		String values = getProperty(key);
		String[] result = values.split(delimiter);
		for (int i = 0; i < result.length; i++) {
			result[i] = result[i].trim();
		}
		return result;
	}

	public ArrayList<String> getPropertiesArrayList(String key, String delimiter) {
		String values = getProperty(key);
		String[] arrayOfValues = values.split(delimiter);
		ArrayList<String> result = new ArrayList<String>();
		for (String value : arrayOfValues) {
			result.add(value.trim());
		}
		return result;
	}

	/**
	 * Consume a ArrayList of type String containing properties (ie, "USERNAME=root")
	 * and set all the properties onto a file. Key/value are separated by a equal sign '='
	 * 
	 * @author wli
	 *
	 * @param listOfProperties
	 */
	public void setProperties(ArrayList<String> listOfProperties) {
		for (String property : listOfProperties) {
			String[] keyValueHolder = property.split("=");
			String key = keyValueHolder[0].trim();
			String value = keyValueHolder[1].trim();
			setProperty(key, value);
		}
	}

	public void setProperties(String[] listOfProperties) {
		for (String property : listOfProperties) {
			String[] keyValueHolder = property.split("=");
			String key = keyValueHolder[0].trim();
			String value = keyValueHolder[1].trim();
			setProperty(key, value);
		}
	}

	/**
	 * NOTE: If one of the properties has multiple values (ex. fruits=apple, pear, banana),
	 *       make sure the delimiter used to separate properties is not the same delimiter
	 *       used to separate values (ex. fruit1=apple; fruit2=pear; fruits=apple, pear, banana)
	 *       
	 * @author ylin
	 * 
	 * @param listOfProperties
	 * @param delimiter
	 */
	public void setProperties(String listOfProperties, String delimiter) {
		for (String property : listOfProperties.split(delimiter)) {
			String[] keyValueHolder = property.split("=");
			String key = keyValueHolder[0].trim();
			String value = keyValueHolder[1].trim();
			setProperty(key, value);
		}
	}

	/**
	 * Given a properties file, a default key-value pair value, and a key, this
	 * function returns:\n a) the default value\n b) or, if exists, the
	 * key-value value in the properties file\n c) or, if exists, the system
	 * property key-value value. This function is used to override configuration
	 * files in cascading fashion.
	 * 
	 * @param props
	 * @param defaultValue
	 * @param key
	 * @return
	 */
	private static String getCascadingPropertyValue(Properties props,
			String defaultValue, String key) {
		String value = defaultValue;
		if (props.containsKey(key))
			value = props.getProperty(key);
		if (System.getProperties().containsKey(key))
			value = System.getProperty(key);
		return value;
	}

}