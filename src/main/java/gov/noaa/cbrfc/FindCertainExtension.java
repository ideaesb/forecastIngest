package gov.noaa.cbrfc;

import java.io.*;

/**
 * test class for listing files ending in SIM24.SQME.24.CS.xml.gz
 * commented out to avoid another ambiguous point of entry to Application 
 * @author udaykari
 *
 */
public class FindCertainExtension {
	/*
	private static final String xml_directory="/Users/udaykari/Documents/workspace/ens2csv/ensts/UnReg";
	private static final String filename_extension_filter = "SIM24.SQME.24.CS.xml.gz";
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		new FindCertainExtension().listFile(xml_directory, filename_extension_filter);
		
	}
	public void listFile(String folder, String ext) {

		GenericExtFilter filter = new GenericExtFilter(ext);

		File dir = new File(folder);
		
		if(dir.isDirectory()==false){
			System.out.println("Directory does not exists : " + xml_directory);
			return;
		}
		
		// list out all the file name and filter by the extension
		String[] list = dir.list(filter);

		if (list.length == 0) {
			System.out.println("no files end with : " + ext);
			return;
		}

		int count=0;
		for (String file : list) {
			count++;
			//String temp = new StringBuffer(filename_extension_filter).append(File.separator).append(file).toString();
			System.out.println(count + " :" + file.toString());
		}
	}

	// inner class, generic extension filter
	public class GenericExtFilter implements FilenameFilter {

		private String ext;

		public GenericExtFilter(String ext) {
			this.ext = ext;
		}

		public boolean accept(File dir, String name) {
			return (name.endsWith(ext));
		}
	}
	*/
}
