package gov.noaa.cbrfc;

import java.io.*;

/**
 * Based on inner class GenericExtFilter in 
 * http://www.mkyong.com/java/how-to-find-files-with-certain-extension-only/
 * 
 */
public class FileExtensionFilter implements FilenameFilter {

	private String ext;

	/**
	 * file extension string 
	 * @param ext
	 */
	public FileExtensionFilter(String ext) {
		this.ext = ext;
	}

	/**
	 * 
	 * @param dir directory being scanned 
	 * @param name filename 
	 * @return true if filename ends with extension 
	 */
	public boolean accept(File dir, String name) {
		return (name.endsWith(ext));
	}
	
}
