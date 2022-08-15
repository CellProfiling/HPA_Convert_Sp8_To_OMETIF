package hpaConvertSp8ToOMETif_jnh;

/** ===============================================================================
* HPA_Convert_Sp8_To_OMETIF_JNH.java Version 0.0.1
* 
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation (http://www.gnu.org/licenses/gpl.txt )
* 
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*  
* See the GNU General Public License for more details.
*  
* Copyright (C) Jan Niklas Hansen
* Date: June 23, 2022 (This Version: June 23, 2022)
*   
* For any questions please feel free to contact me (jan.hansen@scilifelab.se).
* =============================================================================== */

import java.awt.Font;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

import javax.swing.UIManager;
//For XML support
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

//W3C definitions for a DOM, DOM exceptions, entities, nodes
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.WaitForUserDialog;
import ij.plugin.PlugIn;
import loci.common.RandomAccessInputStream;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.FormatException;
import loci.formats.ImageReader;
import loci.formats.meta.IMetadata;
import loci.formats.out.OMETiffWriter;
import loci.formats.services.OMEXMLService;
import loci.formats.tiff.TiffParser;
//import loci.formats.FormatException;
import loci.formats.tiff.TiffSaver;

public class ConvertSp8ToOMETif_Main implements PlugIn {
	// Name variables
	static final String PLUGINNAME = "Convert Sp8 Tif to OME Tif";
	static final String PLUGINVERSION = "0.0.1";

	// Fix fonts
	static final Font SuperHeadingFont = new Font("Sansserif", Font.BOLD, 16);
	static final Font HeadingFont = new Font("Sansserif", Font.BOLD, 14);
	static final Font SubHeadingFont = new Font("Sansserif", Font.BOLD, 12);
	static final Font TextFont = new Font("Sansserif", Font.PLAIN, 12);
	static final Font InstructionsFont = new Font("Sansserif", 2, 12);
	static final Font RoiFont = new Font("Sansserif", Font.PLAIN, 20);

	// Fix formats
	DecimalFormat dformat6 = new DecimalFormat("#0.000000");
	DecimalFormat dformat3 = new DecimalFormat("#0.000");
	DecimalFormat dformat0 = new DecimalFormat("#0");
	DecimalFormat dformatDialog = new DecimalFormat("#0.000000");

	static final String[] nrFormats = { "US (0.00...)", "Germany (0,00...)" };

	static SimpleDateFormat NameDateFormatter = new SimpleDateFormat("yyMMdd_HHmmss");
	static SimpleDateFormat FullDateFormatter = new SimpleDateFormat("yyyy-MM-dd	HH:mm:ss");
	static SimpleDateFormat FullDateFormatter2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	// Progress Dialog
	ProgressDialog progress;
	boolean processingDone = false;
	boolean continueProcessing = true;

	// -----------------define params for Dialog-----------------
	int tasks = 1;

	String outPath = "E:" + System.getProperty("file.separator") + System.getProperty("file.separator") + "OME Out"
			+ System.getProperty("file.separator");
	// -----------------define params for Dialog-----------------

	@Override
	public void run(String arg) {

		// &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
		// ---------------------------------INIT JOBS----------------------------------
		// &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
		
		dformat6.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
		dformat3.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
		dformat0.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
		dformatDialog.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));

		String series[] = { "", "" };
		String name[] = { "", "" };
		String dir[] = { "", "" };
		String fullPath[] = { "", "" };

		boolean loadLif = true;	//TODO make optional
		if (loadLif) {
			OpenFilesDialog od = new OpenFilesDialog(false);
			od.setLocation(0, 0);
			od.setVisible(true);

			od.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(WindowEvent winEvt) {
					return;
				}
			});

			// Waiting for od to be done
			while (od.done == false) {
				try {
					Thread.currentThread().sleep(50);
				} catch (Exception e) {
				}
			}

			tasks = od.filesToOpen.size();
			series = new String[tasks];
			name = new String[tasks];
			dir = new String[tasks];
			fullPath = new String[tasks];
			for (int task = 0; task < tasks; task++) {
				fullPath[task] = od.filesToOpen.get(task).toString();
				series[task] = "";
				name[task] = od.filesToOpen.get(task).getName();
				dir[task] = od.filesToOpen.get(task).getParent();

				IJ.log("ORIGINAL: " + fullPath[task]);
				IJ.log("series:" + series[task]);
				IJ.log("name:" + name[task]);
				IJ.log("dir:" + dir[task]);
			}
		} else {
			// Loading a folder structure
			
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception e) {
			}

			// Launch improved file selector for manually selecting directories
			OpenFilesDialog od = new OpenFilesDialog(true);
			od.setLocation(0, 0);
			od.setVisible(true);

			od.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(WindowEvent winEvt) {
					return;
				}
			});

			// Waiting for od to be done
			while (od.done == false) {
				try {
					Thread.currentThread().sleep(50);
				} catch (Exception e) {
				}
			}

			tasks = od.filesToOpen.size();
			String tempFile;
			boolean withMetaData = false;
			LinkedList<String> allFiles = new LinkedList<String>();
			for (int task = 0; task < tasks; task++) {
				// Get all files in the folder
				String[] fileList = od.filesToOpen.get(task).list();
				ArrayList<String> folderFiles = new ArrayList<String>(fileList.length);

				// Check for metadata folder
				withMetaData = false;
				for (int f = 0; f < fileList.length; f++) {
					if (fileList[f].equals("MetaData")) {
						withMetaData = true;
					}
				}
				if (withMetaData == false) {
					IJ.log(od.filesToOpen.get(task).getName() + " was skipped since missing MetaData folder");
				}

				// TODO Function to expand the directory > enter directories not called MetaData
				// and explore if there are tifs and MetaData folders

				// Extract the relevant filenames and avoid duplicates
				scanningFilenames: for (int f = 0; f < fileList.length; f++) {
					tempFile = fileList[f];
					/**
					 * Now, the script scans through all file names in the folder and verifies if
					 * they are tif files and if so it checks whether they are named as the Sp8
					 * usually does A standard image output by the tif export function from the Sp8
					 * microscope looks like <Custom File Name>_z<##>_ch<##>, where z refers to the
					 * z plane (e.g., z00 = first z plane) and c refers to the channel number (e.g.,
					 * "c00" = first channel).
					 */
					if (fileList[f].endsWith(".tif") || fileList[f].endsWith(".TIF") || fileList[f].endsWith(".tiff")
							|| fileList[f].endsWith(".TIFF")) {
						tempFile = tempFile.substring(0, tempFile.toLowerCase().lastIndexOf(".tif"));
						if (tempFile.contains("_z")) {
							tempFile = tempFile.substring(0, tempFile.toLowerCase().lastIndexOf("_z"));
						} else if (tempFile.contains("_ch")) {
							tempFile = tempFile.substring(0, tempFile.toLowerCase().lastIndexOf("_ch"));
						} else {
							IJ.log("Wrong tif formats in folder! Some files were skipped");
						}

						/**
						 * Here it is checked whether an identically named file is already in the list
						 * (otherwise would load each file again and again...
						 */
						for (int ff = 0; ff < folderFiles.size(); ff++) {
							if (folderFiles.get(ff).equals(od.filesToOpen.get(task).getAbsolutePath()
									+ System.getProperty("file.separator") + tempFile)) {
								continue scanningFilenames;
							}
						}
						folderFiles.add(od.filesToOpen.get(task).getAbsolutePath()
								+ System.getProperty("file.separator") + tempFile);
						IJ.log("ACCEPTED: " + folderFiles.get(folderFiles.size() - 1));
					}
				}

				// Copy new files to all files list
				for (int ff = 0; ff < folderFiles.size(); ff++) {
					allFiles.add(folderFiles.get(ff));
				}

				folderFiles.trimToSize();
				folderFiles = null;
			}

			// Generate arrays based on unique names
			tasks = allFiles.size();
			series = new String[tasks];
			name = new String[tasks];
			dir = new String[tasks];
			fullPath = new String[tasks];
			for (int task = 0; task < allFiles.size(); task++) {
				tempFile = allFiles.get(task);
				fullPath[task] = tempFile;
				series[task] = tempFile.substring(tempFile.lastIndexOf(System.getProperty("file.separator")) + 1);
				tempFile = tempFile.substring(0, tempFile.lastIndexOf(System.getProperty("file.separator")));
				name[task] = tempFile.substring(tempFile.lastIndexOf(System.getProperty("file.separator")) + 1);
				tempFile = tempFile.substring(0, tempFile.lastIndexOf(System.getProperty("file.separator")) + 1);
				dir[task] = tempFile;

				IJ.log("ORIGINAL: " + allFiles.get(task));
				IJ.log("series:" + series[task]);
				IJ.log("name:" + name[task]);
				IJ.log("dir:" + dir[task]);
			}
			allFiles.clear();
			allFiles = null;
		}

		if (tasks == 0) {
			new WaitForUserDialog("No folders selected!").show();
			return;
		}

		// add progressDialog
		progress = new ProgressDialog(name, series);
		progress.setLocation(0, 0);
		progress.setVisible(true);
		progress.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(WindowEvent winEvt) {
				if (processingDone == false) {
					IJ.error("Script stopped...");
				}
				continueProcessing = false;
				return;
			}
		});

		// &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
		// ---------------------------------RUN TASKS----------------------------------
		// &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&

		for (int task = 0; task < tasks; task++) {
			running: while (continueProcessing) {
				Date startDate = new Date();
				progress.updateBarText("in progress...");

				if(loadLif){
					// Conversion via OME
					try {
//						this.convertToOMETif(dir[task] + "" + System.getProperty("file.separator") + name[task]);
						this.convertToOMETif(dir[task] + "" + System.getProperty("file.separator") + name[task]);
					} catch (Exception e) {
						String out = "";
						for (int err = 0; err < e.getStackTrace().length; err++) {
							out += " \n " + e.getStackTrace()[err].toString();
						}
						progress.notifyMessage("Task " + (task + 1) + "/" + tasks + ": Could not process "
								+ series[task] + " - Error " + e.getCause() + " - Detailed message:\n" + out,
								ProgressDialog.ERROR);
						break running;
					}

				}else {
					if(!importingFromFolderStructureXLEF(dir[task],name[task], series[task], task)) {
						break running;
					}
				}

				processingDone = true;
				progress.updateBarText("finished!");
				progress.setBar(1.0);
				break running;
			}
			progress.moveTask(task);
		}
	}

	void editTif(String file, String xmlToInsert) throws IOException, FormatException {
		// read comment
		System.out.println("Reading " + file + " ");
		String comment = new TiffParser(file).getComment();
		// or if you already have the file open for random access, you can use:
		// RandomAccessInputStream fin = new RandomAccessInputStream(f);
		// TiffParser tiffParser = new TiffParser(fin);
		// String comment = tiffParser.getComment();
		// fin.close();
		IJ.log("[done]");
		// display comment, and prompt for changes
		IJ.log("Comment =");
		IJ.log(comment);
		IJ.log("Enter new comment (no line breaks):");
//		System.out.print("Saving " + file);
		// save results back to the TIFF file
		TiffSaver saver = new TiffSaver(file);
		RandomAccessInputStream in = new RandomAccessInputStream(file);
		saver.overwriteComment(in, xmlToInsert);
		in.close();
		IJ.log(" [done]");

		comment = new TiffParser(file).getComment();
		IJ.log("New comment =");
		IJ.log(comment);
	}

	IMetadata generateOMEXML(String xmlToInsert) throws DependencyException, ServiceException {
		ServiceFactory factory = new ServiceFactory();
		OMEXMLService service = factory.getInstance(OMEXMLService.class);
		IMetadata omexmlMeta = service.createOMEXMLMetadata(xmlToInsert);
		return omexmlMeta;
	}

	void addOMEXMLtoTif(String file, String xmlToInsert)
			throws IOException, FormatException, DependencyException, ServiceException {
		IJ.log(xmlToInsert);
		IMetadata omeXML = generateOMEXML(xmlToInsert);

		// save to the TIFF file
		TiffSaver saver = new TiffSaver(file);
		RandomAccessInputStream in = new RandomAccessInputStream(file);
		saver.overwriteComment(in, omeXML);
		in.close();

		String comment = new TiffParser(file).getComment();
		IJ.log("New comment =");
		IJ.log(comment);
	}

	String readFileAsOneString(File path) throws FileNotFoundException {
		FileReader fr = new FileReader(path);
		BufferedReader br = new BufferedReader(fr);
		String line = "", collectedXML = "";
		copyPaste: while (true) {
			try {
				line = br.readLine();
				if (line.equals(null))
					break copyPaste;
				collectedXML += line;

			} catch (Exception e) {
				break copyPaste;
			}
		}
		try {
			br.close();
			fr.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
		return collectedXML;
	}

	void convertToOMETif(String id) throws Exception {
		ImageReader reader = new ImageReader();
		OMETiffWriter writer = new OMETiffWriter();

		int dot = id.lastIndexOf(".");
		String outId = (dot >= 0 ? id.substring(0, dot) : id) + "";
		System.out.print("Converting " + id + " to " + outId + " ");

		// record metadata to OME-XML format
		ServiceFactory factory = new ServiceFactory();
		OMEXMLService service = factory.getInstance(OMEXMLService.class);
		IMetadata omexmlMeta = service.createOMEXMLMetadata();
		reader.setOriginalMetadataPopulated(true);
		reader.setMetadataStore(omexmlMeta);
		reader.setId(id);

		// configure OME-TIFF writer
		writer.setMetadataRetrieve(omexmlMeta);
		// writer.setCompression("J2K");

		// write out image planes
		int seriesCount = reader.getSeriesCount();
		String tempTxt, ZTxt, CTxt;
		File fileDir;
		for (int s = 0; s < seriesCount; s++) {
			reader.setSeries(s);
			writer.setSeries(s);
			
			tempTxt = "_S";
			if(seriesCount > 100000) {
				tempTxt += "" + s;
			}else if(seriesCount > 10000) {
				if(s < 10) {
					tempTxt += "0000" + s;
				}else if(s < 100) {
					tempTxt += "000" + s;
				}else if(s < 1000) {
					tempTxt += "00" + s;
				}else if(s < 10000) {
					tempTxt += "0" + s;
				}else {
					tempTxt += "" + s;
				}
			}else if(seriesCount > 1000) {
				if(s < 10) {
					tempTxt += "000" + s;
				}else if(s < 100) {
					tempTxt += "00" + s;
				}else if(s < 1000) {
					tempTxt += "0" + s;
				}else {
					tempTxt += "" + s;
				}
			}else if(seriesCount > 100) {
				if(s < 10) {
					tempTxt += "00" + s;
				}else if(s < 100) {
					tempTxt += "0" + s;
				}else {
					tempTxt += "" + s;
				}
			}else if(seriesCount > 10) {
				if(s < 10) {
					tempTxt += "0" + s;
				}else {
					tempTxt += "" + s;
				}
			}else {
				tempTxt += "" + s;
			}
			
			int planeCount = reader.getImageCount();
			for (int p = 0; p < planeCount; p++) {
				byte[] plane = reader.openBytes(p);
				int [] coords = reader.getZCTCoords(p);
					
				ZTxt = "";
				if(coords[0] < 10) {
					ZTxt += "_Z0" + coords[0];
				}else {
					ZTxt += "_Z" + coords[0];
				}		
				
				fileDir = new File(outId + tempTxt + ZTxt);
				if(!fileDir.exists()) fileDir.mkdir();
								
				CTxt = "";
				if(coords[1] < 10) {
					CTxt += "_C0" + coords[1];
				}else {
					CTxt += "_C" + coords[1];
				}			
								
				writer.setId(outId + tempTxt + ZTxt + System.getProperty("file.separator") 
					+ outId.substring(outId.lastIndexOf(System.getProperty("file.separator"))+1) + tempTxt + ZTxt + CTxt + ".ome.tif");
				// write plane to output file
				writer.saveBytes(p, plane);
				System.out.print(".");
			}
		}
		writer.close();
		reader.close();
		System.out.println(" [done]");
	}
	
	void convertToOMETif(String id, String omeXML) throws Exception {
		ImageReader reader = new ImageReader();
		OMETiffWriter writer = new OMETiffWriter();

		int dot = id.lastIndexOf(".");
		String outId = (dot >= 0 ? id.substring(0, dot) : id) + "";
		System.out.print("Converting " + id + " to " + outId + " ");

		// record metadata to OME-XML format
		ServiceFactory factory = new ServiceFactory();
//		OMEXMLService service = factory.getInstance(OMEXMLService.class);
		
//		IMetadata omexmlMeta = service.createOMEXMLMetadata();
//		reader.setMetadataStore(omexmlMeta);
//		reader.setId(id);
		
		IMetadata omexmlMeta = this.generateOMEXML(omeXML);

		// configure OME-TIFF writer
		writer.setMetadataRetrieve(omexmlMeta);
		// writer.setCompression("J2K");

		// write out image planes
		int seriesCount = reader.getSeriesCount();
		String tempTxt, ZTxt, CTxt;
		File fileDir;
		for (int s = 0; s < seriesCount; s++) {
			reader.setSeries(s);
			writer.setSeries(s);
			
			tempTxt = "_S";
			if(seriesCount > 100000) {
				tempTxt += "" + s;
			}else if(seriesCount > 10000) {
				if(s < 10) {
					tempTxt += "0000" + s;
				}else if(s < 100) {
					tempTxt += "000" + s;
				}else if(s < 1000) {
					tempTxt += "00" + s;
				}else if(s < 10000) {
					tempTxt += "0" + s;
				}else {
					tempTxt += "" + s;
				}
			}else if(seriesCount > 1000) {
				if(s < 10) {
					tempTxt += "000" + s;
				}else if(s < 100) {
					tempTxt += "00" + s;
				}else if(s < 1000) {
					tempTxt += "0" + s;
				}else {
					tempTxt += "" + s;
				}
			}else if(seriesCount > 100) {
				if(s < 10) {
					tempTxt += "00" + s;
				}else if(s < 100) {
					tempTxt += "0" + s;
				}else {
					tempTxt += "" + s;
				}
			}else if(seriesCount > 10) {
				if(s < 10) {
					tempTxt += "0" + s;
				}else {
					tempTxt += "" + s;
				}
			}else {
				tempTxt += "" + s;
			}
			
			int planeCount = reader.getImageCount();
			for (int p = 0; p < planeCount; p++) {				
				byte[] plane = reader.openBytes(p);
				int [] coords = reader.getZCTCoords(p);
					
				ZTxt = "";
				if(coords[0] < 10) {
					ZTxt += "_Z0" + coords[0];
				}else {
					ZTxt += "_Z" + coords[0];
				}			
				fileDir = new File(outId + tempTxt);
				if(!fileDir.exists()) fileDir.mkdir();
				
				CTxt = "";
				if(coords[1] < 10) {
					CTxt += "_C0" + coords[1];
				}else {
					CTxt += "_C" + coords[1];
				}			
				fileDir = new File(outId + tempTxt + ZTxt);
				if(!fileDir.exists()) fileDir.mkdir();
								
				writer.setId(outId + tempTxt + ZTxt + System.getProperty("file.separator") 
					+ outId.substring(outId.lastIndexOf(System.getProperty("file.separator"))+1) + tempTxt + ZTxt + CTxt + ".ome.tif");
				// write plane to output file
				writer.saveBytes(p, plane);
				System.out.print(".");
			}
		}
		writer.close();
		reader.close();
		System.out.println(" [done]");
	}
	
	/**
	 * This function is still a draft and requires more work
	 * */
	boolean importingFromFolderStructureXLEF(String directory, String filename, String series, int task) {
		/**
		 * Check for problems
		 */
		File metaDataFile = new File("");
		{
			// Verify that a folder named "MetaData" exists for the respective series
			if (!new File(directory + System.getProperty("file.separator") + filename
					+ System.getProperty("file.separator") + "MetaData").isDirectory()) {
				progress.notifyMessage("Task " + (task + 1) + "/" + tasks
						+ ": Could not be processed - MetaData folder missing for " + series + "!",
						ProgressDialog.ERROR);
				return false;
			}

			// Verify that the "MetaData" folder contains a file called as the series and
			// having the ending ".xml"
			metaDataFile = new File(directory + System.getProperty("file.separator") + filename
					+ System.getProperty("file.separator") + "MetaData" + System.getProperty("file.separator")
					+ series + ".xml");
			
			if (!metaDataFile.exists()) {
				progress.notifyMessage("Task " + (task + 1) + "/" + tasks
						+ ": Could not be processed - MetaData .xml file missing for " + series + "!",
						ProgressDialog.ERROR);
				return false;
			}
		}		
		
		
		/**
		 * Import the XML - generate document to read from it
		 */
		Document metaDoc = null;
		String metaDataXMLString = "";
		{
			try {
				metaDataXMLString = this.readFileAsOneString(metaDataFile);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}

			try {
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				metaDoc = db.parse(metaDataFile);
				metaDoc.getDocumentElement().normalize();
			} catch (SAXException | IOException | ParserConfigurationException e) {
				String out = "";
				for (int err = 0; err < e.getStackTrace().length; err++) {
					out += " \n " + e.getStackTrace()[err].toString();
				}
				progress.notifyMessage("Task " + (task + 1) + "/" + tasks + ": Could not process "
						+ series + " - Error " + e.getCause() + " - Detailed message:\n" + out,
						ProgressDialog.ERROR);
				return false;
			}

			NodeList nodeList = metaDoc.getElementsByTagName("ATLConfocalSettingDefinition");
			for (int n = 0; n < nodeList.getLength(); n++) {
				progress.notifyMessage("Task " + (task + 1) + "/" + tasks + ": Node " + (n + 1) + " Type: "
						+ nodeList.item(n).getNodeType(), ProgressDialog.LOG);
				progress.notifyMessage("Task " + (task + 1) + "/" + tasks + ": Node " + (n + 1) + " Name: "
						+ nodeList.item(n).getNodeName(), ProgressDialog.LOG);
				progress.notifyMessage("Task " + (task + 1) + "/" + tasks + ": Node " + (n + 1) + " Value: "
						+ nodeList.item(n).getNodeValue(), ProgressDialog.LOG);
				progress.notifyMessage("Task " + (task + 1) + "/" + tasks + ": Node " + (n + 1) + " Content: "
						+ nodeList.item(n).getTextContent(), ProgressDialog.LOG);
				progress.notifyMessage("Task " + (task + 1) + "/" + tasks + ": Node " + (n + 1) + " NodeMap:",
						ProgressDialog.LOG);
				for (int i = 0; i < nodeList.item(n).getAttributes().getLength(); i++) {
					progress.notifyMessage("" + nodeList.item(n).getAttributes().item(i), ProgressDialog.LOG);
				}
//				progress.notifyMessage("Task " + (task+1) + "/" + tasks + ": Node " + (n+1) + " Content: " + nodeList.item(n).getFeature("Name", ""), ProgressDialog.LOG);
			}
		}
		
		
		/**
		 * Open each tif file and save it as OME-tif. *
		 */
		{
			// Get all files in the folder
			String[] fileList = new File(directory + "" + System.getProperty("file.separator") + filename)
					.list();
			String tempFile, plane, channel;
			ImagePlus imp;

			// Scanning filenames, extract the relevant filenames and avoid duplicates
			scanningFilenames: for (int f = 0; f < fileList.length; f++) {
				/**
				 * Now, the script scans through all file names in the folder and verifies if
				 * they are tif files and if so it checks whether they are named as the Sp8
				 * usually does A standard image output by the tif export function from the Sp8
				 * microscope looks like <Custom File Name>_z<##>_ch<##>, where z refers to the
				 * z plane (e.g., z00 = first z plane) and c refers to the channel number (e.g.,
				 * "c00" = first channel).
				 */

				IJ.log("Verify " + fileList[f]);

				if (!fileList[f].contains(series))
					continue scanningFilenames;
				if (!fileList[f].contains("_z"))
					continue scanningFilenames; // TODO make optional
				if (!fileList[f].contains("_c"))
					continue scanningFilenames; // TODO make optional

				IJ.log("Accepted " + fileList[f]);

				if (fileList[f].endsWith(".tif") || fileList[f].endsWith(".TIF")
						|| fileList[f].endsWith(".tiff") || fileList[f].endsWith(".TIFF")) {
					tempFile = fileList[f];

					// Now open the file and save it as an OME-Tiff
					IJ.log("Open " + directory + "" + System.getProperty("file.separator") + filename
							+ System.getProperty("file.separator") + tempFile);
					imp = IJ.openImage(directory + "" + System.getProperty("file.separator") + filename
							+ System.getProperty("file.separator") + tempFile);

					plane = tempFile.substring(tempFile.toLowerCase().lastIndexOf("_z") + 2,
							tempFile.toLowerCase().lastIndexOf("_z") + 4);
					channel = tempFile.substring(tempFile.toLowerCase().lastIndexOf("_ch") + 3,
							tempFile.toLowerCase().lastIndexOf("_ch") + 5);
					IJ.log("Z " + plane + " - C " + channel);

					new File(outPath + filename + "_" + series + System.getProperty("file.separator"))
							.mkdir();
					String outfilepath = outPath + filename + "_" + series
							+ System.getProperty("file.separator") + series + "_C" + channel + ".ome.tif";
					IJ.saveAs(imp, "Tiff", outfilepath);
					imp.close();

					try {
						this.editTif(outfilepath, "<This is the added description>");
					} catch (Exception e) {
						String out = "";
						for (int err = 0; err < e.getStackTrace().length; err++) {
							out += " \n " + e.getStackTrace()[err].toString();
						}
						progress.notifyMessage(
								"Task " + (task + 1) + "/" + tasks + ": Could not process " + series
										+ " - Error " + e.getCause() + " - Detailed message:\n" + out,
								ProgressDialog.ERROR);
						return false;
					}

					try {
						this.addOMEXMLtoTif(outfilepath, metaDataXMLString);
					} catch (Exception e) {
						String out = "";
						for (int err = 0; err < e.getStackTrace().length; err++) {
							out += " \n " + e.getStackTrace()[err].toString();
						}
						progress.notifyMessage(
								"Task " + (task + 1) + "/" + tasks + ": Could not process " + series
										+ " - Error " + e.getCause() + " - Detailed message:\n" + out,
								ProgressDialog.ERROR);
						return false;
					}
				}
			}
		}
		
		//Returns true only when this code is reached
		return true;
	}
	

}// end main class