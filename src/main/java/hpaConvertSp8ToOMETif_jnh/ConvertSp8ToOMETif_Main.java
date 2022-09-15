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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
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
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
//W3C definitions for a DOM, DOM exceptions, entities, nodes
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
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
import loci.formats.MetadataTools;
import loci.formats.in.MetadataOptions;
import loci.formats.meta.IMetadata;
import loci.formats.meta.MetadataRetrieve;
import loci.formats.meta.MetadataStore;
import loci.formats.ome.OMEXMLMetadata;
import loci.formats.out.OMETiffWriter;
import loci.formats.services.OMEXMLService;
import loci.formats.tiff.TiffParser;
//import loci.formats.FormatException;
import loci.formats.tiff.TiffSaver;
import ome.xml.meta.MetadataConverter;
import ome.xml.model.OME;
import ome.xml.model.OMEModel;

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

		boolean loadLif = false;	//TODO make optional
		boolean extendOnly = true;
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

//				IJ.log("ORIGINAL: " + fullPath[task]);
//				IJ.log("series:" + series[task]);
//				IJ.log("name:" + name[task]);
//				IJ.log("dir:" + dir[task]);
			}
		} else if(extendOnly){
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
						break;
					}else if (fileList[f].equals("Metadata")) {
						withMetaData = true;
						break;
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
//						tempFile = tempFile.substring(0, tempFile.toLowerCase().lastIndexOf(".tif"));
						if (tempFile.contains("_z")) {	//raw tif output: _z, xlef data: --Z
//							tempFile = tempFile.substring(0, tempFile.toLowerCase().lastIndexOf("_z"));
						} else if (tempFile.contains("--Z")) {
//							tempFile = tempFile.substring(0, tempFile.toLowerCase().lastIndexOf("--z"));
						} else if (tempFile.contains("_ch")) {	//raw tif output: _ch, xlef data: --C
//							tempFile = tempFile.substring(0, tempFile.toLowerCase().lastIndexOf("_ch"));
						} else if (tempFile.contains("--C")) {
//							tempFile = tempFile.substring(0, tempFile.toLowerCase().lastIndexOf("--c"));
						}else {
							IJ.log("Wrong tif formats in folder! Some files were skipped");
							continue scanningFilenames;
						}

						/**
						 * Here it is checked whether an identically named file is already in the list
						 * (otherwise would load each file again and again...
						 */
//						for (int ff = 0; ff < folderFiles.size(); ff++) {
//							if (folderFiles.get(ff).equals(od.filesToOpen.get(task).getAbsolutePath()
//									+ System.getProperty("file.separator") + tempFile)) {
//								continue scanningFilenames;
//							}
//						}
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
				
				//Getting series name
				series[task] = tempFile.substring(tempFile.lastIndexOf(System.getProperty("file.separator")) + 1);
				if (tempFile.contains("_z")) {	//raw tif output: _z, xlef data: --Z
					series[task] = series[task].substring(0, series[task].toLowerCase().lastIndexOf("_z"));
				} else if (tempFile.contains("--Z")) {
					series[task] = series[task].substring(0, series[task].toLowerCase().lastIndexOf("--z"));
				}
				
				tempFile = tempFile.substring(0, tempFile.lastIndexOf(System.getProperty("file.separator")));
				name[task] = tempFile.substring(tempFile.lastIndexOf(System.getProperty("file.separator")) + 1);
				tempFile = tempFile.substring(0, tempFile.lastIndexOf(System.getProperty("file.separator")));
				name[task] = tempFile.substring(tempFile.lastIndexOf(System.getProperty("file.separator")) + 1) + name[task];
				tempFile = tempFile.substring(0, tempFile.lastIndexOf(System.getProperty("file.separator")) + 1);
				dir[task] = tempFile;

				IJ.log("FULL PATH: " + fullPath[task]);
				IJ.log("series:" + series[task]);
				IJ.log("name:" + name[task]);
				IJ.log("dir:" + dir[task]);
			}
			allFiles.clear();
			allFiles = null;
		}else{
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
					}else if (fileList[f].equals("Metadata")) {
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
						if (tempFile.contains("_z")) {	//raw tif output: _z, xlef data: --Z
							tempFile = tempFile.substring(0, tempFile.toLowerCase().lastIndexOf("_z"));
						} else if (tempFile.contains("--Z")) {
							tempFile = tempFile.substring(0, tempFile.toLowerCase().lastIndexOf("--z"));
						} else if (tempFile.contains("_ch")) {	//raw tif output: _ch, xlef data: --C
							tempFile = tempFile.substring(0, tempFile.toLowerCase().lastIndexOf("_ch"));
						} else if (tempFile.contains("--C")) {
							tempFile = tempFile.substring(0, tempFile.toLowerCase().lastIndexOf("--c"));
						}else {
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
						convertToOMETif(dir[task] + "" + System.getProperty("file.separator") + name[task]);
						
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
					/**
					 * Convert folder structure into OME
					 * */
					try {
//						if(!importingFromFolderStructureXLEF(dir[task],name[task], series[task], task, true)) {
//							break running;
//						}	
						
						this.extendOMETiffCommentWithMetadataXML(fullPath[task], task, true, true);
						
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
				}

				processingDone = true;
				progress.updateBarText("finished!");
				progress.setBar(1.0);
				break running;
			}
			progress.moveTask(task);
		}
	}

	void replaceXMLInTif(String file, String xmlToInsert, boolean extendedLogging) throws IOException, FormatException {
		// read comment
		if(extendedLogging) progress.notifyMessage("Start replacmenet for " + file,ProgressDialog.LOG);
		if(extendedLogging) progress.notifyMessage("	Reading...",ProgressDialog.LOG);

		String omeTifComment = new TiffParser(file).getComment();
		// or if you already have the file open for random access, you can use:
		// RandomAccessInputStream fin = new RandomAccessInputStream(f);
		// TiffParser tiffParser = new TiffParser(fin);
		// String comment = tiffParser.getComment();
		// fin.close();

		// display comment, and prompt for changes
		if(extendedLogging) progress.notifyMessage("	Read content: " + omeTifComment,ProgressDialog.LOG);
		
		// save results back to the TIFF file
		if(extendedLogging) progress.notifyMessage("	Replace with: " + xmlToInsert,ProgressDialog.LOG);
		TiffSaver saver = new TiffSaver(file);
		RandomAccessInputStream in = new RandomAccessInputStream(file);
		saver.overwriteComment(in, xmlToInsert);
		in.close();
		
		omeTifComment = new TiffParser(file).getComment();
		if(extendedLogging) progress.notifyMessage("	Verifying new content: " + omeTifComment,ProgressDialog.LOG);
		
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
		String line = "", collectedString = "";
		copyPaste: while (true) {
			try {
				line = br.readLine();
				if (line.equals(null))
					break copyPaste;
				collectedString += line;

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
		return collectedString;
	}

	void convertToOMETif(String id) throws Exception {
		ImageReader reader = new ImageReader();
		OMETiffWriter writer = new OMETiffWriter();

		int dot = id.lastIndexOf(".");
		String outId = (dot >= 0 ? id.substring(0, dot) : id) + "";
		progress.updateBarText("Converting " + id.substring(id.lastIndexOf(System.getProperty("file.separator"))+1) + "...");

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
		int planeCounts [] = new int [seriesCount];
		
		LinkedList <String> savedFilePaths = new LinkedList <String>();
		String tempTxt, ZTxt, CTxt;
		String outFilePath;
		
		File fileDir;
		for (int s = 0; s < seriesCount; s++) {
			progress.setBar(s/seriesCount*0.4);
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
			
			planeCounts [s] = reader.getImageCount();
			for (int p = 0; p < planeCounts [s]; p++) {
				progress.setBar((s/(double)seriesCount+p/(double)planeCounts [s]/(double)seriesCount)*0.4);
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
				
				//Saving file
				outFilePath = outId + tempTxt + ZTxt + System.getProperty("file.separator") 
					+ outId.substring(outId.lastIndexOf(System.getProperty("file.separator"))+1) + tempTxt + ZTxt + CTxt + ".ome.tif";				
				savedFilePaths.add(outFilePath+"");				
				writer.setId(outFilePath);
				// write plane to output file
				writer.saveBytes(p, plane);
			}
			progress.updateBarText("Converting " + id.substring(id.lastIndexOf(System.getProperty("file.separator"))+1) + "... (" + (s+1) + "/" + seriesCount+" done)");
		}
		progress.updateBarText("Converted - closing metadata writer...");
		writer.close();
		progress.updateBarText("Converted - closing metadata reader...");
		reader.close();
		
		for(int f = 0; f < savedFilePaths.size(); f++) {
			progress.setBar(0.4+(0.4*f/(double)savedFilePaths.size()));
			progress.updateBarText("Cleaning XML " + savedFilePaths.get(f).substring(savedFilePaths.get(f).lastIndexOf(System.getProperty("file.separator"))+1) + "...");
			if(id.substring(id.lastIndexOf(".")).equals(".lif")) {
				progress.notifyMessage("Clean lif based xml", ProgressDialog.ERROR);				
				cleanUpOmeTifFromLifXmlString(savedFilePaths.get(f), false, true);
			}else if(id.substring(id.lastIndexOf(".")).equals(".xlef")) {
				progress.notifyMessage("Clean xlef based xml", ProgressDialog.ERROR);
				cleanUpOmeTifFromXlefXmlString(savedFilePaths.get(f), false, true);
			}
			
		}
		
		progress.notifyMessage("Converted " + id + " to " + outId + "...", ProgressDialog.LOG);
	}
	
	/**
	 * Cleanup XML in OME TIF single image files generated from a Lif file
	 * This code should remove all information related to series that are not in this image.
	 * */
	void cleanUpOmeTifFromLifXmlString(String file, boolean logWholeComments, boolean extendedLogging) throws IOException, FormatException {
		// read comment
		progress.notifyMessage("Reading " + file + " ", ProgressDialog.LOG);
		progress.updateBarText("Reading " + file + " ");
		
		String comment = new TiffParser(file).getComment();
		// or if you already have the file open for random access, you can use:
		// RandomAccessInputStream fin = new RandomAccessInputStream(f);
		// TiffParser tiffParser = new TiffParser(fin);
		// String comment = tiffParser.getComment();
		// fin.close();
		progress.updateBarText("Reading " + file + " done!");
		// display comment, and prompt for changes
		if(logWholeComments) {
			progress.notifyMessage("Original comment:", ProgressDialog.LOG);
			progress.notifyMessage(comment, ProgressDialog.LOG);
			
		}
		
		
		//Cleaning up the XML
		{

			/**
			 * Import the XML - generate document to read from it
			 */
			Document metaDoc = null;
			String metaDataXMLString = comment+"";
//			InputStream xmlIs = org.apache.commons.io.IOUtils.toInputStream(metaDataXMLString);	
			InputStream xmlIs = new java.io.ByteArrayInputStream(metaDataXMLString.getBytes(StandardCharsets.UTF_8));
			//Note usually it is UTF-8 but it is also specified in the OME-String.
			
			{			
				try {
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					DocumentBuilder db = dbf.newDocumentBuilder();
															
					metaDoc = db.parse(xmlIs);
					metaDoc.getDocumentElement().normalize();
				} catch (SAXException | IOException | ParserConfigurationException e) {
					String out = "";
					for (int err = 0; err < e.getStackTrace().length; err++) {
						out += " \n " + e.getStackTrace()[err].toString();
					}
					progress.notifyMessage("Could not process "
							+ " - Error " + e.getCause() + " - Detailed message:\n" + out,
							ProgressDialog.ERROR);
					return;
				}

				
				/*
				 * Editing starts - Let's first find out the uuid of the saved image and the image that it belongs to in the metadata so we know what to keep!
				 * In the XML the uuid of the respective image is stored in the parent node (<OME ...>) as an argument called UUID = "". We need to retrieve that first.
				 * Then, in the XML file each image is saved as an <Image> node, and under each Image 
				 * node there is an item called <Pixels>, under which each image plane is stored as a <TiffData> 
				 * node containing a <UUID> node, whose content is the UUID containing also the argument FileName.
				 * E.g., thge TiffData node can look like this:
				 * 	<TiffData FirstC="1" FirstT="0" FirstZ="0" IFD="0" PlaneCount="1">
               	 *		<UUID FileName="20220513_JNH008_E5_PFATrit0p1_GT335_1k_S0_Z00_C01.ome.tif">urn:uuid:615a14da-fcf8-4184-be60-df092f26fa1c</UUID>
            	 *	</TiffData>
            	 *	We need to scan through all image nodes and there screen these TiffData nodes to find out the corresponding image in which the UUID is contained. 
            	 *	Then we want to store the image ID and image Name in which the uuid is stored. Both of these parameters are attributes of an <Image> (Image objects look e.g. like: <Image ID="Image:0" Name="Series001"> ...)
            	 * 	Then from that image we can find out which Instrument ID belongs to it, since under the image node there is an <InstrumentRef> node. We only need to keep instrument settings for this InstrumentID
				 * */
				
				//Supportive variables
				NodeList nodeList, childNodes, grandChildNodes;
				
				
				//Retrieve UUID
				String uuid = "none";
				{
					uuid = metaDoc.getElementsByTagName("OME").item(0).getAttributes().getNamedItem("UUID").getNodeValue();
					if(extendedLogging) progress.notifyMessage("Detected uuid:	" + uuid, ProgressDialog.ERROR);
				}				
				if(uuid.equals("none")) {
					progress.notifyMessage("Failed to read uuid",ProgressDialog.ERROR);
					return;					
				}
				
				//Retrieve ImageID
				String imageID = null;
				String imageName = "NoImageNameDetected";
				int imageIndexInAllImageNodes = -1;
				{
					nodeList = metaDoc.getElementsByTagName("Image");
					findingUuidInImages: for (int n = 0; n < nodeList.getLength(); n++) {
						childNodes = nodeList.item(n).getChildNodes();
						int pixelC = -1;
						for (int c = 0; c < childNodes.getLength(); c++) {
							if(childNodes.item(c).getNodeName().equals("Pixels")) {
								pixelC = c;
								if(extendedLogging) progress.notifyMessage("Accepted childNode " + c + " - Name: " + childNodes.item(c).getNodeName(),ProgressDialog.LOG);
								break;
							}else {
								if(extendedLogging) progress.notifyMessage("Discarded childNode " + c + " - Name: " + childNodes.item(c).getNodeName(),ProgressDialog.LOG);
							}
						}
						if(extendedLogging) progress.notifyMessage("Decided item nr " + pixelC + "",ProgressDialog.LOG);
						
						//Now go into the subnodes of Pixel, search for <TiffData> nodes and retrieve the UUID element in there. 
						//When the UUID matches to the uuid we look for, verify that the filename matches the attribute FileName. If it does, save that ImageID.
						childNodes = childNodes.item(pixelC).getChildNodes();
						 for (int c = 0; c < childNodes.getLength(); c++) {							
							if(childNodes.item(c).getNodeName().equals("TiffData")) {
								if(extendedLogging) progress.notifyMessage("Found TiffChildNode at " + c + "",ProgressDialog.LOG);
								grandChildNodes = childNodes.item(c).getChildNodes();
								for (int cc = 0; cc < grandChildNodes.getLength(); cc++) {
									if(grandChildNodes.item(cc).getTextContent().equals(uuid)) {										
										if(extendedLogging) progress.notifyMessage("Found uuidNode " + cc + " - UUID: " + grandChildNodes.item(cc).getTextContent(),ProgressDialog.LOG);
										if(extendedLogging) progress.notifyMessage("Noted FileName for UUID: " + grandChildNodes.item(cc).getAttributes().getNamedItem("FileName").getNodeValue(),ProgressDialog.LOG);	
										
										if(extendedLogging) progress.notifyMessage("Does it match to " + file.substring(file.lastIndexOf(System.getProperty("file.separator"))+1) + "?",ProgressDialog.LOG);	
										if(grandChildNodes.item(cc).getAttributes().getNamedItem("FileName").getNodeValue().equals(file.substring(file.lastIndexOf(System.getProperty("file.separator"))+1))) {
											if(extendedLogging) progress.notifyMessage("YES!",ProgressDialog.LOG);
											imageID = nodeList.item(n).getAttributes().getNamedItem("ID").getNodeValue();
											imageName = nodeList.item(n).getAttributes().getNamedItem("Name").getNodeValue();
											imageIndexInAllImageNodes = n;
											if(extendedLogging) progress.notifyMessage("ImageID: " + imageID,ProgressDialog.LOG);	
											if(extendedLogging) progress.notifyMessage("ImageName: " + imageName,ProgressDialog.LOG);	
											if(extendedLogging) progress.notifyMessage("ImageIndex: " + imageIndexInAllImageNodes,ProgressDialog.LOG);	
											break findingUuidInImages;
										}
									}
										
								}
							}else {
								if(extendedLogging) progress.notifyMessage("Refused childnode " + c + ", which is " + childNodes.item(c).getNodeName(),ProgressDialog.LOG);								
							}
						}
						
						
					}
				}
				nodeList = null;
				childNodes = null;
				grandChildNodes = null;
				if(imageName.equals( "NoImageNameDetected")) {
					progress.notifyMessage("Failed to detect image name",ProgressDialog.ERROR);
					return;
				}
				
				//Retrieve Instrument ID
				String instrumentID = "missing";				
				nodeList = metaDoc.getElementsByTagName("Image").item(imageIndexInAllImageNodes).getChildNodes();
				for (int n = 0; n < nodeList.getLength(); n++) {
					if(nodeList.item(n).getNodeName().equals("InstrumentRef")) {
						instrumentID = nodeList.item(n).getAttributes().getNamedItem("ID").getNodeValue();
						if(extendedLogging) progress.notifyMessage("Instrument ID: " + instrumentID,ProgressDialog.LOG);	
						break;
					}
					if(n == nodeList.getLength()-1) {
						progress.notifyMessage("Could not find instrumentID",ProgressDialog.ERROR);	
						return;
					}
				}
				nodeList = null;
				
				if(extendedLogging) progress.notifyMessage("Retrieved all parameters :)",ProgressDialog.LOG);			
				
				/*
				 * Now we start the cleaning. We know which image and instrument object to keep and can clear everything else.
				 * */
				
				//Scan through images and remove obsolete image nodes
				{
					nodeList = metaDoc.getElementsByTagName("Image");
					for (int n = nodeList.getLength()-1; n >=0; n--) {						
						if(n == imageIndexInAllImageNodes) {							
							continue;
						}
						if(extendedLogging) progress.notifyMessage("Deleting image node " + n 
								+ " (" + nodeList.item(n).getAttributes().item(0) 
								+ " " + nodeList.item(n).getAttributes().item(1) + ")",ProgressDialog.LOG);
						
						//not relevant image node > delete it
						nodeList.item(n).getParentNode().removeChild(nodeList.item(n));				
					}
				}
				
				//Scan through instruments and remove obsolete instruments nodes
				{
					nodeList = metaDoc.getElementsByTagName("Instrument");
					for (int n = nodeList.getLength()-1; n >=0; n--) {
						if(nodeList.item(n).getAttributes().getNamedItem("ID").getNodeValue().equals(instrumentID)) {
							if(extendedLogging) progress.notifyMessage("Keeping instrument node " + n 
									+ " (ID = " + nodeList.item(n).getAttributes().getNamedItem("ID").getNodeValue() + ")",ProgressDialog.LOG);
							continue;
						}
						
						if(extendedLogging) progress.notifyMessage("Deleting instrument node " + n 
								+ " (ID = " + nodeList.item(n).getAttributes().getNamedItem("ID").getNodeValue() + ")",ProgressDialog.LOG);
						
						//not relevant image node > delete it
						nodeList.item(n).getParentNode().removeChild(nodeList.item(n));				
					}
				}
				
				/**
				 * Scan through extended metadata and remove obsolete original metadata nodes
				 * The structure of the original metadata nodes is as follows (example, "<" removed to avoid xml detection):
				 * <XMLAnnotation ID="Annotation:0" Namespace="openmicroscopy.org/OriginalMetadata">
				 *     Value>
				 *         OriginalMetadata>
				 *             Key>
				 *             		Series002 Image #0|ATLConfocalSettingDefinition #0|DetectorList #0|Detector #0|IsEnabled
				 *             /Key>
				 *             Value>
				 *             		1
				 *             /Value>
				 *         /OriginalMetadata>
				 *     /Value>
				 * /XMLAnnotation>
				 */
				{
					nodeList = metaDoc.getElementsByTagName("XMLAnnotation");
					for (int n = nodeList.getLength()-1; n >=0; n--) {
						childNodes = nodeList.item(n).getChildNodes();
						for(int c1 = childNodes.getLength()-1; c1 >= 0; c1--) {
							if(!childNodes.item(c1).getNodeName().equals("Value")) {
								if(extendedLogging) progress.notifyMessage("Node of type " + childNodes.item(c1).getNodeName() + " in XML Annotation " + n,ProgressDialog.NOTIFICATION);
								continue;
							}
							grandChildNodes = childNodes.item(c1).getChildNodes();
							for(int c2 = grandChildNodes.getLength()-1; c2 >= 0; c2--) {
								if(!grandChildNodes.item(c2).getNodeName().equals("OriginalMetadata")) {
									if(extendedLogging) progress.notifyMessage("Node of type " + grandChildNodes.item(c2).getNodeName() + " in XML Annotation " + n + " Value " + c1,ProgressDialog.NOTIFICATION);
									continue;
								}
								
								//Verify that key is given as first element
								if(!grandChildNodes.item(c2).getChildNodes().item(0).getNodeName().equals("Key")) {
									if(extendedLogging) progress.notifyMessage("Key missing in Original Metadata - XML Annotation " + n + " Value " + c1 + " Or.Metad. " + c2,ProgressDialog.NOTIFICATION);
									continue;
								}

								//Verify that key starts with ImageName		
								if(grandChildNodes.item(c2).getChildNodes().item(0).getTextContent().startsWith(imageName)) {
									//keep the XML Annotation
//									if(extendedLogging) progress.notifyMessage("         KEEP node " + n 
//											+ " (ID " + nodeList.item(n).getAttributes().getNamedItem("ID") 
//											+ " Content " + grandChildNodes.item(c2).getChildNodes().item(0).getTextContent() + ")",ProgressDialog.LOG);									
								}else {
									//delete the XML Annotation node since it belongs to a different image
//									if(extendedLogging) progress.notifyMessage("Deleting image node " + n 
//											+ " (ID " + nodeList.item(n).getAttributes().getNamedItem("ID") 
//											+ " Content " + grandChildNodes.item(c2).getChildNodes().item(0).getTextContent() + ")",ProgressDialog.LOG);
									nodeList.item(n).getParentNode().removeChild(nodeList.item(n));	
								}
							}
						}	
					}

					/*
					 * Since the XML annotation IDs are named with consecutive numbers (ID="Annotation:0" etc.), we need to adjust these numbers for the remaining nodes
					 * */
					nodeList = metaDoc.getElementsByTagName("XMLAnnotation");
					org.w3c.dom.Element attrib;
					for (int n = 0; n < nodeList.getLength(); n++) {
//					    if(extendedLogging) progress.notifyMessage("Adjust " + nodeList.item(n).getAttributes().getNamedItem("ID"),ProgressDialog.LOG);
						attrib = (org.w3c.dom.Element) nodeList.item(n);
					    attrib.setAttribute("ID", "Annotation:" + n);
//					    if(extendedLogging) progress.notifyMessage("   Adjusted " + nodeList.item(n).getAttributes().getNamedItem("ID"),ProgressDialog.LOG);
					}
					
					
					/**
					 * LIMS always reads only the Image:0 notes > change Image name to Image:0
					 * Change instrument also to Instrument:0 ?
					 * */
					{
						nodeList = metaDoc.getElementsByTagName("Image");
						for (int n = nodeList.getLength()-1; n >=0; n--) {						
							if(nodeList.item(n).getAttributes().getNamedItem("ID").getNodeValue().equals(imageID)) {
								if(extendedLogging) progress.notifyMessage("Changing image ID from " + nodeList.item(n).getAttributes().getNamedItem("ID").getNodeValue()
										+ " to Image:0",ProgressDialog.LOG);
								nodeList.item(n).getAttributes().getNamedItem("ID").setNodeValue("Image:0");
							}
							
							
							//not relevant image node > delete it
							nodeList.item(n).getParentNode().removeChild(nodeList.item(n));				
						}
					}
					
					
					/**
					 * TODO
					 * Should we change the IFDs to completely remove the Z stack? Instead use a z position?
					 * Fix Channel information based on OriginalMetadata?
					 * */
				}
				
				//Write document back to string
				if(extendedLogging)	IJ.log("A: " + metaDataXMLString);
				try {
				    Transformer tf = TransformerFactory.newInstance().newTransformer();
				    StreamResult strRes = new StreamResult(new StringWriter());
				    DOMSource metaDocSource = new DOMSource(metaDoc);				    
				    tf.transform(metaDocSource, strRes);
				    metaDataXMLString = strRes.getWriter().toString();
				} catch(TransformerException tfe) {
				    tfe.printStackTrace();
				    String out = "";
					for (int err = 0; err < tfe.getStackTrace().length; err++) {
						out += " \n " + tfe.getStackTrace()[err].toString();
					}
					progress.notifyMessage("Could not convert document to String"
							+ " - Error " + tfe.getCause() + " - Detailed message:\n" + out,
							ProgressDialog.ERROR);
				}
				if(extendedLogging)	IJ.log("B: " + metaDataXMLString);
				
			}
			// hand back the edited comment
			comment = metaDataXMLString;
		}

		if(logWholeComments) {
			progress.notifyMessage("New comment:", ProgressDialog.LOG);
			progress.notifyMessage(comment, ProgressDialog.LOG);			
		}
		
		// save results back to the TIFF file
		TiffSaver saver = new TiffSaver(file);
		RandomAccessInputStream in = new RandomAccessInputStream(file);
		saver.overwriteComment(in, comment);
		in.close();

		comment = new TiffParser(file).getComment();

		if(logWholeComments) {
			progress.notifyMessage("Saved comment:", ProgressDialog.LOG);
			progress.notifyMessage(comment, ProgressDialog.LOG);
		}
	}
	
	/**
	 * Cleanup XML in OME TIF single image files generated from a Leica XLEF file
	 * This code should remove all information related to series that are not in this image.
	 * */
	void cleanUpOmeTifFromXlefXmlString(String file, boolean logWholeComments, boolean extendedLogging) throws IOException, FormatException {
		// read comment
		progress.notifyMessage("Reading " + file + " ", ProgressDialog.LOG);
		progress.updateBarText("Reading " + file + " ");
		
		String comment = new TiffParser(file).getComment();
		// or if you already have the file open for random access, you can use:
		// RandomAccessInputStream fin = new RandomAccessInputStream(f);
		// TiffParser tiffParser = new TiffParser(fin);
		// String comment = tiffParser.getComment();
		// fin.close();
		progress.updateBarText("Reading " + file + " done!");
		// display comment, and prompt for changes
		if(logWholeComments) {
			progress.notifyMessage("Original comment:", ProgressDialog.LOG);
			progress.notifyMessage(comment, ProgressDialog.LOG);
			
		}
		
		//Cleaning up the XML
		{

			/**
			 * Import the XML - generate document to read from it
			 */
			Document metaDoc = null;
			String metaDataXMLString = comment+"";
//			InputStream xmlIs = org.apache.commons.io.IOUtils.toInputStream(metaDataXMLString);	
			InputStream xmlIs = new java.io.ByteArrayInputStream(metaDataXMLString.getBytes(StandardCharsets.UTF_8));
			//Note usually it is UTF-8 but it is also specified in the OME-String.
			
			{			
				try {
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					DocumentBuilder db = dbf.newDocumentBuilder();
															
					metaDoc = db.parse(xmlIs);
					metaDoc.getDocumentElement().normalize();
				} catch (SAXException | IOException | ParserConfigurationException e) {
					String out = "";
					for (int err = 0; err < e.getStackTrace().length; err++) {
						out += " \n " + e.getStackTrace()[err].toString();
					}
					progress.notifyMessage("Could not process "
							+ " - Error " + e.getCause() + " - Detailed message:\n" + out,
							ProgressDialog.ERROR);
					return;
				}

				
				/*
				 * Editing starts - Let's first find out the uuid of the saved image and the image that it belongs to in the metadata so we know what to keep!
				 * In the XML the uuid of the respective image is stored in the parent node (<OME ...>) as an argument called UUID = "". We need to retrieve that first.
				 * Then, in the XML file each image is saved as an <Image> node, and under each Image 
				 * node there is an item called <Pixels>, under which each image plane is stored as a <TiffData> 
				 * node containing a <UUID> node, whose content is the UUID containing also the argument FileName.
				 * E.g., thge TiffData node can look like this:
				 * 	<TiffData FirstC="1" FirstT="0" FirstZ="0" IFD="0" PlaneCount="1">
               	 *		<UUID FileName="20220513_JNH008_E5_PFATrit0p1_GT335_1k_S0_Z00_C01.ome.tif">urn:uuid:615a14da-fcf8-4184-be60-df092f26fa1c</UUID>
            	 *	</TiffData>
            	 *	We need to scan through all image nodes and there screen these TiffData nodes to find out the corresponding image in which the UUID is contained. 
            	 *	Then we want to store the image ID and image Name in which the uuid is stored. Both of these parameters are attributes of an <Image> (Image objects look e.g. like: <Image ID="Image:0" Name="Series001"> ...)
            	 * 	Then from that image we can find out which Instrument ID belongs to it, since under the image node there is an <InstrumentRef> node. We only need to keep instrument settings for this InstrumentID
				 * */
				
				//Supportive variables
				NodeList nodeList, childNodes, grandChildNodes;
				
				
				//Retrieve UUID
				String uuid = "none";
				{
					uuid = metaDoc.getElementsByTagName("OME").item(0).getAttributes().getNamedItem("UUID").getNodeValue();
					if(extendedLogging) progress.notifyMessage("Detected uuid:	" + uuid, ProgressDialog.ERROR);
				}				
				if(uuid.equals("none")) {
					progress.notifyMessage("Failed to read uuid",ProgressDialog.ERROR);
					return;					
				}
				
				//Retrieve ImageID
				String imageID = null;
				String imageName = "NoImageNameDetected";
				int imageIndexInAllImageNodes = -1;
				{
					nodeList = metaDoc.getElementsByTagName("Image");
					findingUuidInImages: for (int n = 0; n < nodeList.getLength(); n++) {
						childNodes = nodeList.item(n).getChildNodes();
						int pixelC = -1;
						for (int c = 0; c < childNodes.getLength(); c++) {
							if(childNodes.item(c).getNodeName().equals("Pixels")) {
								pixelC = c;
								if(extendedLogging) progress.notifyMessage("Accepted childNode " + c + " - Name: " + childNodes.item(c).getNodeName(),ProgressDialog.LOG);
								break;
							}else {
								if(extendedLogging) progress.notifyMessage("Discarded childNode " + c + " - Name: " + childNodes.item(c).getNodeName(),ProgressDialog.LOG);
							}
						}
						if(extendedLogging) progress.notifyMessage("Decided item nr " + pixelC + "",ProgressDialog.LOG);
						
						//Now go into the subnodes of Pixel, search for <TiffData> nodes and retrieve the UUID element in there. 
						//When the UUID matches to the uuid we look for, verify that the filename matches the attribute FileName. If it does, save that ImageID.
						childNodes = childNodes.item(pixelC).getChildNodes();
						 for (int c = 0; c < childNodes.getLength(); c++) {							
							if(childNodes.item(c).getNodeName().equals("TiffData")) {
								if(extendedLogging) progress.notifyMessage("Found TiffChildNode at " + c + "",ProgressDialog.LOG);
								grandChildNodes = childNodes.item(c).getChildNodes();
								for (int cc = 0; cc < grandChildNodes.getLength(); cc++) {
									if(grandChildNodes.item(cc).getTextContent().equals(uuid)) {										
										if(extendedLogging) progress.notifyMessage("Found uuidNode " + cc + " - UUID: " + grandChildNodes.item(cc).getTextContent(),ProgressDialog.LOG);
										if(extendedLogging) progress.notifyMessage("Noted FileName for UUID: " + grandChildNodes.item(cc).getAttributes().getNamedItem("FileName").getNodeValue(),ProgressDialog.LOG);	
										
										if(extendedLogging) progress.notifyMessage("Does it match to " + file.substring(file.lastIndexOf(System.getProperty("file.separator"))+1) + "?",ProgressDialog.LOG);	
										if(grandChildNodes.item(cc).getAttributes().getNamedItem("FileName").getNodeValue().equals(file.substring(file.lastIndexOf(System.getProperty("file.separator"))+1))) {
											if(extendedLogging) progress.notifyMessage("YES!",ProgressDialog.LOG);
											imageID = nodeList.item(n).getAttributes().getNamedItem("ID").getNodeValue();
											imageName = nodeList.item(n).getAttributes().getNamedItem("Name").getNodeValue();
											imageIndexInAllImageNodes = n;
											if(extendedLogging) progress.notifyMessage("ImageID: " + imageID,ProgressDialog.LOG);	
											if(extendedLogging) progress.notifyMessage("ImageName: " + imageName,ProgressDialog.LOG);	
											if(extendedLogging) progress.notifyMessage("ImageIndex: " + imageIndexInAllImageNodes,ProgressDialog.LOG);	
											break findingUuidInImages;
										}
									}
										
								}
							}else {
								if(extendedLogging) progress.notifyMessage("Refused childnode " + c + ", which is " + childNodes.item(c).getNodeName(),ProgressDialog.LOG);								
							}
						}
						
						
					}
				}
				nodeList = null;
				childNodes = null;
				grandChildNodes = null;
				if(imageName.equals( "NoImageNameDetected")) {
					progress.notifyMessage("Failed to detect image name",ProgressDialog.ERROR);
					return;
				}
				
				//Retrieve Instrument ID
				String instrumentID = "missing";				
				nodeList = metaDoc.getElementsByTagName("Image").item(imageIndexInAllImageNodes).getChildNodes();
				for (int n = 0; n < nodeList.getLength(); n++) {
					if(nodeList.item(n).getNodeName().equals("InstrumentRef")) {
						instrumentID = nodeList.item(n).getAttributes().getNamedItem("ID").getNodeValue();
						if(extendedLogging) progress.notifyMessage("Instrument ID: " + instrumentID,ProgressDialog.LOG);	
						break;
					}
					if(n == nodeList.getLength()-1) {
						progress.notifyMessage("Could not find instrumentID",ProgressDialog.ERROR);	
						return;
					}
				}
				nodeList = null;
				
				if(extendedLogging) progress.notifyMessage("Retrieved all parameters :)",ProgressDialog.LOG);			
				
				/*
				 * Now we start the cleaning. We know which image and instrument object to keep and can clear everything else.
				 * */
				
				//Scan through images and remove obsolete image nodes
				{
					nodeList = metaDoc.getElementsByTagName("Image");
					for (int n = nodeList.getLength()-1; n >=0; n--) {						
						if(n == imageIndexInAllImageNodes) {							
							continue;
						}
						if(extendedLogging) progress.notifyMessage("Deleting image node " + n 
								+ " (" + nodeList.item(n).getAttributes().item(0) 
								+ " " + nodeList.item(n).getAttributes().item(1) + ")",ProgressDialog.LOG);
						
						//not relevant image node > delete it
						nodeList.item(n).getParentNode().removeChild(nodeList.item(n));				
					}
				}
				
				//Scan through instruments and remove obsolete instruments nodes
				{
					nodeList = metaDoc.getElementsByTagName("Instrument");
					for (int n = nodeList.getLength()-1; n >=0; n--) {
						if(nodeList.item(n).getAttributes().getNamedItem("ID").getNodeValue().equals(instrumentID)) {
							if(extendedLogging) progress.notifyMessage("Keeping instrument node " + n 
									+ " (ID = " + nodeList.item(n).getAttributes().getNamedItem("ID").getNodeValue() + ")",ProgressDialog.LOG);
							continue;
						}
						
						if(extendedLogging) progress.notifyMessage("Deleting instrument node " + n 
								+ " (ID = " + nodeList.item(n).getAttributes().getNamedItem("ID").getNodeValue() + ")",ProgressDialog.LOG);
						
						//not relevant image node > delete it
						nodeList.item(n).getParentNode().removeChild(nodeList.item(n));				
					}
				}
				
				/**
				 * Scan through extended metadata and remove obsolete original metadata nodes
				 * The structure of the original metadata nodes is as follows (example, "<" removed to avoid xml detection):
				 * <XMLAnnotation ID="Annotation:0" Namespace="openmicroscopy.org/OriginalMetadata">
				 *     Value>
				 *         OriginalMetadata>
				 *             Key>
				 *             		Series002 Image #0|ATLConfocalSettingDefinition #0|DetectorList #0|Detector #0|IsEnabled
				 *             /Key>
				 *             Value>
				 *             		1
				 *             /Value>
				 *         /OriginalMetadata>
				 *     /Value>
				 * /XMLAnnotation>
				 */
				{
					nodeList = metaDoc.getElementsByTagName("XMLAnnotation");
					for (int n = nodeList.getLength()-1; n >=0; n--) {
						childNodes = nodeList.item(n).getChildNodes();
						for(int c1 = childNodes.getLength()-1; c1 >= 0; c1--) {
							if(!childNodes.item(c1).getNodeName().equals("Value")) {
								if(extendedLogging) progress.notifyMessage("Node of type " + childNodes.item(c1).getNodeName() + " in XML Annotation " + n,ProgressDialog.NOTIFICATION);
								continue;
							}
							grandChildNodes = childNodes.item(c1).getChildNodes();
							for(int c2 = grandChildNodes.getLength()-1; c2 >= 0; c2--) {
								if(!grandChildNodes.item(c2).getNodeName().equals("OriginalMetadata")) {
									if(extendedLogging) progress.notifyMessage("Node of type " + grandChildNodes.item(c2).getNodeName() + " in XML Annotation " + n + " Value " + c1,ProgressDialog.NOTIFICATION);
									continue;
								}
								
								//Verify that key is given as first element
								if(!grandChildNodes.item(c2).getChildNodes().item(0).getNodeName().equals("Key")) {
									if(extendedLogging) progress.notifyMessage("Key missing in Original Metadata - XML Annotation " + n + " Value " + c1 + " Or.Metad. " + c2,ProgressDialog.NOTIFICATION);
									continue;
								}

								//Verify that key starts with ImageName		
								if(grandChildNodes.item(c2).getChildNodes().item(0).getTextContent().startsWith(imageName)) {
									//keep the XML Annotation
//									if(extendedLogging) progress.notifyMessage("         KEEP node " + n 
//											+ " (ID " + nodeList.item(n).getAttributes().getNamedItem("ID") 
//											+ " Content " + grandChildNodes.item(c2).getChildNodes().item(0).getTextContent() + ")",ProgressDialog.LOG);									
								}else {
									//delete the XML Annotation node since it belongs to a different image
//									if(extendedLogging) progress.notifyMessage("Deleting image node " + n 
//											+ " (ID " + nodeList.item(n).getAttributes().getNamedItem("ID") 
//											+ " Content " + grandChildNodes.item(c2).getChildNodes().item(0).getTextContent() + ")",ProgressDialog.LOG);
									nodeList.item(n).getParentNode().removeChild(nodeList.item(n));	
								}
							}
						}	
					}

					/*
					 * Since the XML annotation IDs are named with consecutive numbers (ID="Annotation:0" etc.), we need to adjust these numbers for the remaining nodes
					 * */
					nodeList = metaDoc.getElementsByTagName("XMLAnnotation");
					org.w3c.dom.Element attrib;
					for (int n = 0; n < nodeList.getLength(); n++) {
//					    if(extendedLogging) progress.notifyMessage("Adjust " + nodeList.item(n).getAttributes().getNamedItem("ID"),ProgressDialog.LOG);
						attrib = (org.w3c.dom.Element) nodeList.item(n);
					    attrib.setAttribute("ID", "Annotation:" + n);
//					    if(extendedLogging) progress.notifyMessage("   Adjusted " + nodeList.item(n).getAttributes().getNamedItem("ID"),ProgressDialog.LOG);
					}
					
					
					/**
					 * LIMS always reads only the Image:0 notes > change Image name to Image:0
					 * Change instrument also to Instrument:0 ?
					 * */
					{
						nodeList = metaDoc.getElementsByTagName("Image");
						for (int n = nodeList.getLength()-1; n >=0; n--) {						
							if(nodeList.item(n).getAttributes().getNamedItem("ID").getNodeValue().equals(imageID)) {
								if(extendedLogging) progress.notifyMessage("Changing image ID from " + nodeList.item(n).getAttributes().getNamedItem("ID").getNodeValue()
										+ " to Image:0",ProgressDialog.LOG);
								nodeList.item(n).getAttributes().getNamedItem("ID").setNodeValue("Image:0");
							}
							
							
							//not relevant image node > delete it
							nodeList.item(n).getParentNode().removeChild(nodeList.item(n));				
						}
					}
					
					
					/**
					 * TODO
					 * Should we change the IFDs to completely remove the Z stack? Instead use a z position?
					 * Fix Channel information based on OriginalMetadata?
					 * */
				}
				
				//Write document back to string
				if(extendedLogging)	IJ.log("A: " + metaDataXMLString);
				try {
				    Transformer tf = TransformerFactory.newInstance().newTransformer();
				    StreamResult strRes = new StreamResult(new StringWriter());
				    DOMSource metaDocSource = new DOMSource(metaDoc);				    
				    tf.transform(metaDocSource, strRes);
				    metaDataXMLString = strRes.getWriter().toString();
				} catch(TransformerException tfe) {
				    tfe.printStackTrace();
				    String out = "";
					for (int err = 0; err < tfe.getStackTrace().length; err++) {
						out += " \n " + tfe.getStackTrace()[err].toString();
					}
					progress.notifyMessage("Could not convert document to String"
							+ " - Error " + tfe.getCause() + " - Detailed message:\n" + out,
							ProgressDialog.ERROR);
				}
				if(extendedLogging)	IJ.log("B: " + metaDataXMLString);
				
			}
			// hand back the edited comment
			comment = metaDataXMLString;
		}

		if(logWholeComments) {
			progress.notifyMessage("New comment:", ProgressDialog.LOG);
			progress.notifyMessage(comment, ProgressDialog.LOG);			
		}
		
		// save results back to the TIFF file
		TiffSaver saver = new TiffSaver(file);
		RandomAccessInputStream in = new RandomAccessInputStream(file);
		saver.overwriteComment(in, comment);
		in.close();

		comment = new TiffParser(file).getComment();

		if(logWholeComments) {
			progress.notifyMessage("Saved comment:", ProgressDialog.LOG);
			progress.notifyMessage(comment, ProgressDialog.LOG);
		}
	}
		
	/**
	 * This function is still a draft and requires more work
	 * */
	boolean importingFromFolderStructureXLEF(String directory, String filename, String series, int task, boolean extendedLogging) {
		/**
		 * Finding the metadata
		 */
		File metaDataFile = new File("");
		String metadataFolderName = "Not Known";
		String metadataFileEnding = "Not Known";
		{
			// Verify that a folder named "MetaData" exists and contains a file called as the series and having the ending ".xml" (raw data export) or ".xlif" (xlef data)
			searchMetadata: while(true) {
				metadataFolderName = "Metadata";
				
				metaDataFile = new File(directory + System.getProperty("file.separator") + filename
						+ System.getProperty("file.separator") + metadataFolderName + System.getProperty("file.separator")
						+ series + ".xml");
				
				if (metaDataFile.exists()) {
					metadataFileEnding = ".xml";
					break searchMetadata;
				}
				
				metaDataFile = new File(directory + System.getProperty("file.separator") + filename
						+ System.getProperty("file.separator") + metadataFolderName + System.getProperty("file.separator")
						+ series + ".xlif");
					
				if (metaDataFile.exists()) {
					metadataFileEnding = ".xlif";
					break searchMetadata;
				}
				
				metadataFolderName = "MetaData";
				
				metaDataFile = new File(directory + System.getProperty("file.separator") + filename
						+ System.getProperty("file.separator") + metadataFolderName + System.getProperty("file.separator")
						+ series + ".xml");
				
				if (metaDataFile.exists()) {
					metadataFileEnding = ".xml";
					break searchMetadata;
				}
				
				metaDataFile = new File(directory + System.getProperty("file.separator") + filename
						+ System.getProperty("file.separator") + metadataFolderName + System.getProperty("file.separator")
						+ series + ".xlif");
					
				if (metaDataFile.exists()) {
					metadataFileEnding = ".xlif";
					break searchMetadata;
				}
						
				metaDataFile = null;				
				progress.notifyMessage("Task " + (task + 1) + "/" + tasks
						+ ": Could not be processed - metadata folder or .xml or .xlef file missing for " + series + "!",
						ProgressDialog.ERROR);
				return false;
			}
			if(extendedLogging) progress.notifyMessage("Metadata folder is called <" + metadataFolderName + ">",ProgressDialog.LOG);
			if(extendedLogging) progress.notifyMessage("Metadata file ending is " + metadataFileEnding + "",ProgressDialog.LOG);
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
				String out = "";
				for (int err = 0; err < e1.getStackTrace().length; err++) {
					out += " \n " + e1.getStackTrace()[err].toString();
				}
				progress.notifyMessage("Task " + (task + 1) + "/" + tasks + ": Failed reading metadata file for "
						+ series + " - Error " + e1.getCause() + " - Detailed message:\n" + out,
						ProgressDialog.ERROR);
				return false;
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

//			NodeList nodeList = metaDoc.getElementsByTagName("ATLConfocalSettingDefinition");
//			for (int n = 0; n < nodeList.getLength(); n++) {
//				progress.notifyMessage("Task " + (task + 1) + "/" + tasks + ": Node " + (n + 1) + " Type: "
//						+ nodeList.item(n).getNodeType(), ProgressDialog.LOG);
//				progress.notifyMessage("Task " + (task + 1) + "/" + tasks + ": Node " + (n + 1) + " Name: "
//						+ nodeList.item(n).getNodeName(), ProgressDialog.LOG);
//				progress.notifyMessage("Task " + (task + 1) + "/" + tasks + ": Node " + (n + 1) + " Value: "
//						+ nodeList.item(n).getNodeValue(), ProgressDialog.LOG);
//				progress.notifyMessage("Task " + (task + 1) + "/" + tasks + ": Node " + (n + 1) + " Content: "
//						+ nodeList.item(n).getTextContent(), ProgressDialog.LOG);
//				progress.notifyMessage("Task " + (task + 1) + "/" + tasks + ": Node " + (n + 1) + " NodeMap:",
//						ProgressDialog.LOG);
//				for (int i = 0; i < nodeList.item(n).getAttributes().getLength(); i++) {
//					progress.notifyMessage("" + nodeList.item(n).getAttributes().item(i), ProgressDialog.LOG);
//				}
////				progress.notifyMessage("Task " + (task+1) + "/" + tasks + ": Node " + (n+1) + " Content: " + nodeList.item(n).getFeature("Name", ""), ProgressDialog.LOG);
//			}
		}
		
		
		/**
		 * Open each tif file and save it as OME-tif. *
		 */
		{
			// Get all files in the folder
			String[] fileList = new File(directory + "" + System.getProperty("file.separator") + filename)
					.list();
			String tempFile, slice, channel;
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

				if(extendedLogging) progress.notifyMessage("Verify " + fileList[f],ProgressDialog.LOG);

				if (!fileList[f].contains(series))
					continue scanningFilenames;
				if(metadataFileEnding.equals("xml")) {
					if (!(fileList[f].contains("_z")))
						continue scanningFilenames; // TODO make optional
					if (!(fileList[f].contains("_c")))
						continue scanningFilenames; // TODO make optional
				}else if(metadataFileEnding.equals("xlif")){
					if (!(fileList[f].contains("--Z")))
						continue scanningFilenames; // TODO make optional
					if (!(fileList[f].contains("--C")))
						continue scanningFilenames; // TODO make optional
				}else {
					//TODO
				}
				
				if(extendedLogging) progress.notifyMessage("Accepted " + fileList[f],ProgressDialog.LOG);

				if (fileList[f].endsWith(".tif") || fileList[f].endsWith(".TIF")
						|| fileList[f].endsWith(".tiff") || fileList[f].endsWith(".TIFF")) {
					tempFile = fileList[f];
					
					try {
						ImageReader reader = new ImageReader();
						OMETiffWriter writer = new OMETiffWriter();
	
						String inId = directory + "" + System.getProperty("file.separator") + filename
										+ System.getProperty("file.separator") + tempFile;
																	
						if(extendedLogging) progress.updateBarText("Converting " + tempFile.substring(tempFile.lastIndexOf(System.getProperty("file.separator"))+1) + "...");
						if(extendedLogging) progress.notifyMessage("Filepath " + inId,ProgressDialog.LOG);
	
						// record metadata to OME-XML format
						ServiceFactory factory = new ServiceFactory();
						OMEXMLService service = factory.getInstance(OMEXMLService.class);
						IMetadata omexmlMeta = service.createOMEXMLMetadata();
						reader.setOriginalMetadataPopulated(true);
						reader.setMetadataStore(omexmlMeta);
						reader.setId(inId);
	
						// configure OME-TIFF writer
						writer.setMetadataRetrieve(omexmlMeta);
						
						// writer.setCompression("J2K");
	
						// write out image planes
						int seriesCount = reader.getSeriesCount();
						int planeCounts [] = new int [seriesCount];
						
						LinkedList <String> savedFilePaths = new LinkedList <String>();
						String tempTxt, ZTxt, CTxt;
						String outFilePath;
						
						File fileDir;	
						for (int s = 0; s < seriesCount; s++) {
							progress.setBar(s/seriesCount*0.4);
							reader.setSeries(s);
							writer.setSeries(s);						
							
							planeCounts [s] = reader.getImageCount();
							for (int p = 0; p < planeCounts [s]; p++) {
								progress.setBar((s/(double)seriesCount+p/(double)planeCounts [s]/(double)seriesCount)*0.4);
								byte[] plane = reader.openBytes(p);
								int [] coords = reader.getZCTCoords(p);
								if(extendedLogging) progress.notifyMessage("Reader Z " + coords [0] + " - C " + coords [1] + " - T " + coords [2]  ,ProgressDialog.LOG);
										
								slice = tempFile.substring(tempFile.toLowerCase().lastIndexOf("_z") + 2,
										tempFile.toLowerCase().lastIndexOf("_z") + 4);
								channel = tempFile.substring(tempFile.toLowerCase().lastIndexOf("_ch") + 3,
										tempFile.toLowerCase().lastIndexOf("_ch") + 5);
								if(extendedLogging) progress.notifyMessage("Z " + slice + " - C " + channel,ProgressDialog.LOG);

								new File(outPath + filename + "_" + series + "_Z" + slice + System.getProperty("file.separator"))
										.mkdir();
								outFilePath = outPath + filename + "_" + series + "_Z" + slice + System.getProperty("file.separator") 
									+ series + "_Z" + slice + "_C" + channel + ".ome.tif";
								
								//Saving file
								writer.setId(outFilePath);
								// write plane to output file
								writer.saveBytes(p, plane);
							}
							progress.updateBarText("Converting " + tempFile.substring(tempFile.lastIndexOf(System.getProperty("file.separator"))+1) + "... (" + (s+1) + "/" + seriesCount+" done)");
						}
						progress.updateBarText("Converted - closing metadata writer...");
						writer.close();
						progress.updateBarText("Converted - closing metadata reader...");
						reader.close();
					} catch (FormatException | IOException | DependencyException | ServiceException e) {
						String out = "";
						for (int err = 0; err < e.getStackTrace().length; err++) {
							out += " \n " + e.getStackTrace()[err].toString();
						}
						progress.notifyMessage("Task " + (task + 1) + "/" + tasks + ": Could not process " + series + " because of error:" 
								+ "\\nCause: " + e.getCause() 
								+ "\nMessage: " + e.getMessage() + "\nLocalizedMessage: " + e.getLocalizedMessage() + "\nDetailed message:\n" + out,
								ProgressDialog.ERROR);
						return false;
					}
					
					
					
					
					
					
					// Now open the file and save it as an OME-Tiff
					boolean goon = false;
					if(goon) {
						if(extendedLogging) progress.notifyMessage("Open " + directory + "" + System.getProperty("file.separator") + filename
								+ System.getProperty("file.separator") + tempFile,ProgressDialog.LOG);
						imp = IJ.openImage(directory + "" + System.getProperty("file.separator") + filename
								+ System.getProperty("file.separator") + tempFile);
											
						slice = tempFile.substring(tempFile.toLowerCase().lastIndexOf("_z") + 2,
								tempFile.toLowerCase().lastIndexOf("_z") + 4);
						channel = tempFile.substring(tempFile.toLowerCase().lastIndexOf("_ch") + 3,
								tempFile.toLowerCase().lastIndexOf("_ch") + 5);
						if(extendedLogging) progress.notifyMessage("Z " + slice + " - C " + channel,ProgressDialog.LOG);

						new File(outPath + filename + "_" + series + "_Z" + slice + System.getProperty("file.separator"))
								.mkdir();
						String outfilepath = outPath + filename + "_" + series + "_Z" + slice + System.getProperty("file.separator") 
							+ series + "_Z" + slice + "_C" + channel + ".ome.tif";
										
						IJ.saveAs(imp, "Tiff", outfilepath);
						imp.close();
						
						
						try {
							this.insertBioFormatsXML(outfilepath, metaDataXMLString, true, true);
						} catch (IOException | FormatException | ServiceException | DependencyException e) {
							String out = "";
							for (int err = 0; err < e.getStackTrace().length; err++) {
								out += " \n " + e.getStackTrace()[err].toString();
							}
							progress.notifyMessage("Task " + (task + 1) + "/" + tasks + ": Could not process " + series + " because of error:" 
									+ "\\nCause: " + e.getCause() 
									+ "\nMessage: " + e.getMessage() + "\nLocalizedMessage: " + e.getLocalizedMessage() + "\nDetailed message:\n" + out,
									ProgressDialog.ERROR);
							return false;
						}
						
						//TODO Generate OME XML FROM METADATA
						try {
							replaceXMLInTif(outfilepath, "<This is the added description>", true);
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
		}
		
		//Returns true only when this code is reached
		return true;
	}
	
	void insertBioFormatsXML(String file, String xmlInput, boolean extendedLogging, boolean wholeLogging) throws IOException, FormatException, ServiceException, DependencyException {
		// read comment
		progress.notifyMessage("Generating XML for " + file + " ", ProgressDialog.LOG);
		progress.updateBarText("Reading " + file + " ");
		
		String comment = new TiffParser(file).getComment();
		// or if you already have the file open for random access, you can use:
		// RandomAccessInputStream fin = new RandomAccessInputStream(f);
		// TiffParser tiffParser = new TiffParser(fin);
		// String comment = tiffParser.getComment();
		// fin.close();
		progress.updateBarText("Reading " + file + " done!");
		// display comment, and prompt for changes
		if(wholeLogging) {
			progress.notifyMessage("Original comment in file:", ProgressDialog.LOG);
			progress.notifyMessage(comment, ProgressDialog.LOG);
		}
		
		//Converting XML to OME
		//TODO REMOVE FROM HERE and start developing
		ServiceFactory factory = new ServiceFactory();
		OMEXMLService service = factory.getInstance(OMEXMLService.class);		
		IMetadata omexmlMeta = service.createOMEXMLMetadata();
		service.convertMetadata(xmlInput, omexmlMeta);
		service.createOMEXMLRoot(xmlInput);
		
		
//		xmlMeta = omeXmlService.createOMEXMLMetadata(xml);
//        log.info("Converting to OMERO metadata");
//        MetadataConverter.convertMetadata(xmlMeta, target);
		
//		IMetadata meta = 

//		comment = service.getOMEXML(omexmlMeta);
		
		if(wholeLogging) {
			progress.notifyMessage("New comment:", ProgressDialog.LOG);
			progress.notifyMessage(comment, ProgressDialog.LOG);			
		}
		
		// save results back to the TIFF file
		TiffSaver saver = new TiffSaver(file);
		RandomAccessInputStream in = new RandomAccessInputStream(file);
		saver.overwriteComment(in, comment);
		in.close();

		comment = new TiffParser(file).getComment();

		if(wholeLogging) {
			progress.notifyMessage("Saved comment:", ProgressDialog.LOG);
			progress.notifyMessage(comment, ProgressDialog.LOG);
		}
	}

	void extendOMETiffCommentWithMetadataXML(String file, int task, boolean logWholeComments, boolean extendedLogging) throws IOException, FormatException, ServiceException, DependencyException {
			// read comment
			if(extendedLogging)	progress.notifyMessage("Reading " + file + " ", ProgressDialog.LOG);
			
			progress.updateBarText("Reading " + file + " ");
			
			/**
			 * In the OME Tiff output from the 3D viewer it will generate a folder for each well and then put the multiple positions recorded there (e.g., R1, R2, R3, R4) 
			 * These positions are also in the filenames (Example file name "R2_z00_ch0.ome.tif")
			 * In the folder for each well there is also a "MetaData" folder with xml files. For each position (e.g., R2), there is:
			 * 		R2.ome.xml
			 * 		R2.ome_properties.xml
			 * 		R2.ome_properties.xsl
			 * We need to read R2.ome.xml and transfer the metadata from there
			 * */
			String positionName = file.substring(file.lastIndexOf(System.getProperty("file.separator"))+1);
			positionName = positionName.substring(0,positionName.indexOf("_z"));
			String metadataFilePath = file.substring(0,file.lastIndexOf(System.getProperty("file.separator"))+1) 
					+ "MetaData" + System.getProperty("file.separator") 
					+ positionName + ".ome.xml";
			
			if(extendedLogging)	progress.notifyMessage("Series name: " + positionName + "", ProgressDialog.LOG);
			if(extendedLogging)	progress.notifyMessage("Metadata file path: " + metadataFilePath + "", ProgressDialog.LOG);
			
			/**
			 * Import the XML file and generate document to read from it
			 */
			Document metaDoc = null;
			String metaDataXMLString = "";
			File metaDataFile = new File(metadataFilePath);
			{
				try {
					metaDataXMLString = this.readFileAsOneString(metaDataFile);
				} catch (FileNotFoundException e1) {
					String out = "";
					for (int err = 0; err < e1.getStackTrace().length; err++) {
						out += " \n " + e1.getStackTrace()[err].toString();
					}
					progress.notifyMessage("Task " + (task + 1) + "/" + tasks + ": Failed reading metadata file for " + file 
							+ "\nError message: " + e1.getMessage()
							+ "\nError localized message: " + e1.getLocalizedMessage()
							+ "\nError cause: " + e1.getCause() 
							+ "\nDetailed message:"
							+ "\n" + out,
							ProgressDialog.ERROR);
					return;
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
					progress.notifyMessage("Task " + (task + 1) + "/" + tasks + ": Could not process metadata file " + metadataFilePath 
							+ "\nError message: " + e.getMessage()
							+ "\nError localized message: " + e.getLocalizedMessage()
							+ "\nError cause: " + e.getCause() 
							+ "\nDetailed message:"
							+ "\n" + out,
							ProgressDialog.ERROR);
					return;
				}

//				NodeList nodeList = metaDoc.getElementsByTagName("ATLConfocalSettingDefinition");
//				for (int n = 0; n < nodeList.getLength(); n++) {
//					progress.notifyMessage("Task " + (task + 1) + "/" + tasks + ": Node " + (n + 1) + " Type: "
//							+ nodeList.item(n).getNodeType(), ProgressDialog.LOG);
//					progress.notifyMessage("Task " + (task + 1) + "/" + tasks + ": Node " + (n + 1) + " Name: "
//							+ nodeList.item(n).getNodeName(), ProgressDialog.LOG);
//					progress.notifyMessage("Task " + (task + 1) + "/" + tasks + ": Node " + (n + 1) + " Value: "
//							+ nodeList.item(n).getNodeValue(), ProgressDialog.LOG);
//					progress.notifyMessage("Task " + (task + 1) + "/" + tasks + ": Node " + (n + 1) + " Content: "
//							+ nodeList.item(n).getTextContent(), ProgressDialog.LOG);
//					progress.notifyMessage("Task " + (task + 1) + "/" + tasks + ": Node " + (n + 1) + " NodeMap:",
//							ProgressDialog.LOG);
//					for (int i = 0; i < nodeList.item(n).getAttributes().getLength(); i++) {
//						progress.notifyMessage("" + nodeList.item(n).getAttributes().item(i), ProgressDialog.LOG);
//					}
////					progress.notifyMessage("Task " + (task+1) + "/" + tasks + ": Node " + (n+1) + " Content: " + nodeList.item(n).getFeature("Name", ""), ProgressDialog.LOG);
//				}
			}
			
			/**
			 * Open the tif file, extract the tif comment (= OME XML String) to modify it.
			 * */
			
			String comment = new TiffParser(file).getComment();
			// or if you already have the file open for random access, you can use:
			// RandomAccessInputStream fin = new RandomAccessInputStream(f);
			// TiffParser tiffParser = new TiffParser(fin);
			// String comment = tiffParser.getComment();
			// fin.close();
			progress.updateBarText("Reading " + file + " done!");
			// display comment, and prompt for changes
			if(logWholeComments) {
				progress.notifyMessage("Original comment:", ProgressDialog.LOG);
				progress.notifyMessage(comment, ProgressDialog.LOG);
				
			}
			
			
			/**
			 * Generate a MetadatStore
			 * */
			ServiceFactory factory = new ServiceFactory();
			OMEXMLService service = factory.getInstance(OMEXMLService.class);
			OMEXMLMetadata meta = service.createOMEXMLMetadata(comment);
			
			/**
			 * Verify that x and y position match
			 * */
			if(extendedLogging)	progress.notifyMessage("XML PosX: " + metaDoc.getElementsByTagName("Tile").item(0).getAttributes().getNamedItem("PosX").getNodeValue(), ProgressDialog.LOG);
			if(extendedLogging)	progress.notifyMessage("XML PosY: " + metaDoc.getElementsByTagName("Tile").item(0).getAttributes().getNamedItem("PosY").getNodeValue(), ProgressDialog.LOG);
			
			Double xmlPosX = Double.parseDouble(metaDoc.getElementsByTagName("Tile").item(0).getAttributes().getNamedItem("PosX").getNodeValue()); 
			Double xmlPosY = Double.parseDouble(metaDoc.getElementsByTagName("Tile").item(0).getAttributes().getNamedItem("PosY").getNodeValue()); 
			
			for(int i = 0; i < meta.getPlaneCount(0); i++){
				if(xmlPosX != meta.getPlanePositionX(0, i).value().doubleValue()) {
					if(extendedLogging)	progress.notifyMessage("OME PosX: " + meta.getPlanePositionX(0, i).value(), ProgressDialog.LOG);
					if(extendedLogging)	progress.notifyMessage("OME PosY: " + meta.getPlanePositionY(0, i).value(), ProgressDialog.LOG);
					progress.notifyMessage("XML did not match OME metadata x position! Skipped task " + task + "!", ProgressDialog.ERROR);
					return;
				}
				if(xmlPosY != meta.getPlanePositionY(0, i).value().doubleValue()) {
					if(extendedLogging)	progress.notifyMessage("OME PosX: " + meta.getPlanePositionX(0, i).value(), ProgressDialog.LOG);
					if(extendedLogging)	progress.notifyMessage("OME PosY: " + meta.getPlanePositionY(0, i).value(), ProgressDialog.LOG);
					progress.notifyMessage("XML did not match OME metadata y position! Skipped task " + task + "!", ProgressDialog.ERROR);
					return;
				}				
			}
			
			/**
			 * Add original metadata
			 * */
			NodeList tempNodes = metaDoc.getElementsByTagName("Attachment");
			Node attachmentHardwareSettings = null;
			NamedNodeMap attributes;
			for(int n = 0; n < tempNodes.getLength(); n++){
				if(tempNodes.item(n).getAttributes().getNamedItem("Name").getNodeValue().equals("HardwareSetting")) {
					attachmentHardwareSettings = tempNodes.item(n);
					attributes = tempNodes.item(n).getAttributes();
					for(int a = 0; a < attributes.getLength(); a++) {
						service.populateOriginalMetadata(meta, attributes.item(a).getNodeName(), attributes.item(a).getNodeValue());
					}
					
					NodeList tempNodes2 = tempNodes.item(n).getChildNodes();
					for(int cn = 0; cn < tempNodes2.getLength(); cn++) {
						if(tempNodes2.item(cn).getNodeName().equals("ATLConfocalSettingDefinition")){
							attributes = tempNodes2.item(cn).getAttributes();
							for(int a = 0; a < attributes.getLength(); a++) {
								service.populateOriginalMetadata(meta, "ATLConfocalSettingDefinition|" + attributes.item(a).getNodeName(), attributes.item(a).getNodeValue());
							}
						}
						if(tempNodes2.item(cn).getNodeName().equals("LDM_Block_Sequential")){
							attributes = tempNodes2.item(cn).getAttributes();
							for(int a = 0; a < attributes.getLength(); a++) {
								service.populateOriginalMetadata(meta, "LDM_Block_Sequential|" + attributes.item(a).getNodeName(), attributes.item(a).getNodeValue());
							}
						}
					}
					break;
				}
			}
			
			tempNodes = metaDoc.getElementsByTagName("*");
			int sibblings;
			int id;
			Node sibbling;
			if(extendedLogging)	progress.notifyMessage("Number of detected xml elements: " + tempNodes.getLength(), ProgressDialog.LOG);
			screeningNodes: for(int n = 0; n < tempNodes.getLength(); n++){
//				if(extendedLogging)	progress.notifyMessage(n + ": NameSpaceURI: " + tempNodes.item(n).getNamespaceURI(), ProgressDialog.LOG);
//				if(extendedLogging)	progress.notifyMessage(n + ": Parent name: " + tempNodes.item(n).getParentNode().getNodeName(), ProgressDialog.LOG);
//				if(extendedLogging)	progress.notifyMessage(n + ": Parent type: " + tempNodes.item(n).getParentNode().getNodeType(), ProgressDialog.LOG);
//				if(extendedLogging)	progress.notifyMessage(n + ": Type: " + tempNodes.item(n).getNodeType(), ProgressDialog.LOG);
//				if(extendedLogging)	progress.notifyMessage(n + ": Name: " + tempNodes.item(n).getNodeName(), ProgressDialog.LOG);
//				if(extendedLogging)	progress.notifyMessage(n + ": Value: " + tempNodes.item(n).getNodeValue(), ProgressDialog.LOG);

				Node tempNode = tempNodes.item(n);
				String tempXPath = "";
				
				//TODO number the nodes!
				sibblings = 0;
				for(int cn = 0; cn < tempNode.getParentNode().getChildNodes().getLength(); cn++) {
					if(tempNode.getParentNode().getChildNodes().item(cn).getNodeName().equals(tempNode.getNodeName())) {
						sibblings ++;
					}
				}
				if(sibblings != 0) {
					sibbling = tempNode;
					id = 0;
					for(int cn = 0; cn < sibblings; cn++) {
						sibbling = sibbling.getNextSibling();
						if(sibbling == null) {
							break;
						}
						if(sibbling.getNodeName().equals(tempNode.getNodeName())) {
							id++;
						}
					}
					id = sibblings - id - 1;
					tempXPath += tempNode.getNodeName() + id;
				}else {
					tempXPath += tempNode.getNodeName();					
				}
								
				boolean valid = false;
				while(!tempNode.equals(null)) {
					tempNode = tempNode.getParentNode();
					if(tempNode.getNodeName().equals("#document") || tempNode.equals(null)) {
						break;
					}else if(tempNode.getNodeName().equals("Image")){
						if(tempXPath.startsWith("ImageDescription")) {
							valid = true;							
						}
						break;
					}else if(tempNode.getNodeName().equals("Attachment")) {
						if(tempNode.getAttributes().getNamedItem("Name").getNodeValue().equals("HardwareSetting")) {
							valid = true;							
						}
						break;
					}					
					tempXPath = tempNode.getNodeName() + "|" + tempXPath;
				}
				if(valid) {
					if(extendedLogging)	progress.notifyMessage(n + ": XPATH: " + tempXPath, ProgressDialog.LOG);
					if(extendedLogging)	progress.notifyMessage(n + ": Value: " + tempNodes.item(n).getNodeValue(), ProgressDialog.LOG);
					if(tempNodes.item(n).getNodeValue() != null) {
						if(extendedLogging) progress.notifyMessage("ACCEPTED BECAUSE VALUE NOT NULL", ProgressDialog.LOG);						
						service.populateOriginalMetadata(meta, tempXPath, tempNodes.item(n).getNodeValue());
					}
					if(tempNodes.item(n).hasAttributes()) {
						attributes = tempNodes.item(n).getAttributes();
						for(int a = 0; a < attributes.getLength(); a++) {
							if(extendedLogging)	progress.notifyMessage(n + ": Found attribute: " + tempXPath + "|" + attributes.item(a).getNodeName() + ": " + attributes.item(a).getNodeValue(), ProgressDialog.LOG);
							service.populateOriginalMetadata(meta, tempXPath + "|" + attributes.item(a).getNodeName(), attributes.item(a).getNodeValue());
						}						
					}					
				}else {
					if(extendedLogging)	progress.notifyMessage(n + ": Declined XPATH: " + tempXPath, ProgressDialog.LOG);					
				}
			}
			
			/**
			 * Translate metadata
			 * */
			{
				
			}
						
			/**
			 * Retrieve new comment
			 * */
			comment = service.getOMEXML(meta);
			
			if(logWholeComments) {
				progress.notifyMessage("Comment after adjustments:", ProgressDialog.LOG);
				progress.notifyMessage(comment, ProgressDialog.LOG);
				
			}
			
			/**
			 * Saving modified tiff comment
			 * */
//		    TiffSaver saver = new TiffSaver(file);
//		    RandomAccessInputStream in = new RandomAccessInputStream(file);
//		    saver.overwriteComment(in, comment);
//		    in.close();
//			progress.updateBarText("Saving " + file + " done!");
	}
	
}// end main class