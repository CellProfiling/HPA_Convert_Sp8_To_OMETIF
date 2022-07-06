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
* For any questions please feel free to contact me (jan.hansen@uni-bonn.de).
* =============================================================================== */

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.text.*;

import javax.swing.UIManager;

import ij.*;
import ij.gui.*;
import ij.io.*;
import ij.measure.*;
import ij.plugin.*;
import ij.text.*;
import loci.formats.FormatException;
import loci.plugins.BF;
import loci.plugins.in.ImportProcess;
import loci.plugins.in.ImporterOptions;

//For XML support
import javax.xml.parsers.DocumentBuilder; 
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

//For exceptions that can be thrown when XML document parsed
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException; 
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.*;

//W3C definitions for a DOM, DOM exceptions, entities, nodes
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Entity;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class ConvertSp8ToOMETif_Main implements PlugIn, Measurements {
	//Name variables
	static final String PLUGINNAME = "Convert Sp8 Tif to OME Tif";
	static final String PLUGINVERSION = "0.0.1";
	
	//Fix fonts
	static final Font SuperHeadingFont = new Font("Sansserif", Font.BOLD, 16);
	static final Font HeadingFont = new Font("Sansserif", Font.BOLD, 14);
	static final Font SubHeadingFont = new Font("Sansserif", Font.BOLD, 12);
	static final Font TextFont = new Font("Sansserif", Font.PLAIN, 12);
	static final Font InstructionsFont = new Font("Sansserif", 2, 12);
	static final Font RoiFont = new Font("Sansserif", Font.PLAIN, 20);
	
	//Fix formats
	DecimalFormat dformat6 = new DecimalFormat("#0.000000");
	DecimalFormat dformat3 = new DecimalFormat("#0.000");
	DecimalFormat dformat0 = new DecimalFormat("#0");
	DecimalFormat dformatDialog = new DecimalFormat("#0.000000");	
		
	static final String[] nrFormats = {"US (0.00...)", "Germany (0,00...)"};
	
	static SimpleDateFormat NameDateFormatter = new SimpleDateFormat("yyMMdd_HHmmss");
	static SimpleDateFormat FullDateFormatter = new SimpleDateFormat("yyyy-MM-dd	HH:mm:ss");
	static SimpleDateFormat FullDateFormatter2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	//Progress Dialog
	ProgressDialog progress;	
	boolean processingDone = false;	
	boolean continueProcessing = true;
	
	//-----------------define params for Dialog-----------------
	int tasks = 1;
	//-----------------define params for Dialog-----------------
	
	public void run(String arg) {	
		//Initialize
		dformat6.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
		dformat3.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
		dformat0.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
		dformatDialog.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
	
		String series [] = {"",""};
		String name [] = {"",""};
		String dir [] = {"",""};
		{
			//Improved file selector
			try{UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}catch(Exception e){}
			
			OpenFilesDialog od = new OpenFilesDialog (true);
			od.setLocation(0,0);
			od.setVisible(true);
			
			od.addWindowListener(new java.awt.event.WindowAdapter() {
		        public void windowClosing(WindowEvent winEvt) {
		        	return;
		        }
		    });
		
			//Waiting for od to be done
			while(od.done==false){
				try{
					Thread.currentThread().sleep(50);
			    }catch(Exception e){
			    }
			}
			
			tasks = od.filesToOpen.size();
			String tempFile;
			boolean withMetaData = false;
			LinkedList<String> allFiles = new LinkedList<String>();
			for(int task = 0; task < tasks; task++){
				//Get all files in the folder
				String [] fileList = od.filesToOpen.get(task).list();
				ArrayList<String> folderFiles = new ArrayList<String>(fileList.length);
				
				//Check for metadata folder
				withMetaData = false;
				for(int f = 0; f < fileList.length; f++) {
					if(fileList [f].equals("MetaData")) {
						withMetaData = true;
					}
				}
				if(withMetaData == false) {
					IJ.log(od.filesToOpen.get(task).getName() + " was skipped since missing MetaData folder");
				}
				
				//TODO Function to expand the directory > enter directories not called MetaData and explore if there are tifs and MetaData folders
				
				//Extract the relevant filenames and avoid duplicates
				scanningFilenames: for(int f = 0; f < fileList.length; f++) {
					tempFile = fileList [f];				
					/** 
					 * Now, the script scans through all file names in the folder and verifies if they are tif files and if so it checks whether they are named as the Sp8 usually does
					 * A standard image output by the tif export function from the Sp8 microscope looks like
					 * <Custom File Name>_z<##>_ch<##>, where z refers to the z plane (e.g., z00 = first z plane) and c refers to the channel number (e.g., "c00" = first channel).
					 * */
					if(fileList [f].endsWith(".tif") || fileList [f].endsWith(".TIF") || fileList [f].endsWith(".tiff") || fileList [f].endsWith(".TIFF")){
						tempFile = tempFile.substring(0,tempFile.toLowerCase().lastIndexOf(".tif"));
						if(tempFile.contains("_z")){
							tempFile = tempFile.substring(0,tempFile.toLowerCase().lastIndexOf("_z"));							
						}else if(tempFile.contains("_ch")){
							tempFile = tempFile.substring(0,tempFile.toLowerCase().lastIndexOf("_ch"));							
						}else {
							IJ.log("Wrong tif formats in folder! Some files were skipped");
						}
						
						/**
						 * Here it is checked whether an identically named file is already in the list (otherwise would load each file again and again...
						 * */
						for(int ff = 0; ff < folderFiles.size(); ff++) {
							if(folderFiles.get(ff).equals(od.filesToOpen.get(task).getName() + System.getProperty("file.separator") + tempFile)) {
								continue scanningFilenames;
							}
						}						
						folderFiles.add(od.filesToOpen.get(task).getName() + System.getProperty("file.separator") + tempFile);
						IJ.log("ACCEPTED: " + folderFiles.get(folderFiles.size()-1));
					}
				}
				
				//Copy new files to all files list
				for(int ff = 0; ff < folderFiles.size(); ff++) {
					allFiles.add(folderFiles.get(ff));					
				}
				
				folderFiles.trimToSize();
				folderFiles = null;
			}
			
			//Generate arrays based on unique names
			tasks = allFiles.size();
			series = new String [tasks];
			name = new String [tasks];
			dir = new String [tasks];
			for(int task = 0; task < allFiles.size(); task++) {
				tempFile = allFiles.get(task);
				series [task] = tempFile.substring(tempFile.lastIndexOf(System.getProperty("file.separator")));
				tempFile = tempFile.substring(0,tempFile.lastIndexOf(System.getProperty("file.separator")));
				name [task] = tempFile.substring(tempFile.lastIndexOf(System.getProperty("file.separator")));
				tempFile = tempFile.substring(0,tempFile.lastIndexOf(System.getProperty("file.separator")));
				dir[task] = tempFile;
				
				IJ.log("ORIGINAL: " + allFiles.get(task));
				IJ.log("series:" + series [task]);
				IJ.log("name:" + name [task]);
				IJ.log("dir:" + dir [task]);
			}
			allFiles.clear();
			allFiles = null;
		}
		 	
		if(tasks == 0) {
			new WaitForUserDialog("No folders selected!").show();
			return;
		}
	
		//add progressDialog
		progress = new ProgressDialog(name, series);
		progress.setLocation(0,0);
		progress.setVisible(true);
		progress.addWindowListener(new java.awt.event.WindowAdapter() {
	        public void windowClosing(WindowEvent winEvt) {
	        	if(processingDone==false){
	        		IJ.error("Script stopped...");
	        	}
	        	continueProcessing = false;	        	
	        	return;
	        }
		});
			
	//&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
	//---------------------------------RUN TASKS----------------------------------
	//&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
	
		for(int task = 0; task < tasks; task++){
			running: while(continueProcessing){
				Date startDate = new Date();
				progress.updateBarText("in progress...");
				
				/**
				* Check for problems
				*/
				//Verify that a folder named "MetaData" exists for the respective series
				if(! new File(dir[task] + System.getProperty("file.separator")
						+ name [task] + System.getProperty("file.separator")
						+ "MetaData").isDirectory()){
					progress.notifyMessage("Task " + (task+1) + "/" + tasks + ": Could not be processed - MetaData folder missing for " + series [task] +  "!", ProgressDialog.ERROR);
					break running;				
				}
				
				//Verify that the "MetaData" folder contains a file called as the series and having the ending ".xml"
				File metaDataFile = new File(dir[task] + System.getProperty("file.separator")
					+ name [task] + System.getProperty("file.separator")
					+ "MetaData" + System.getProperty("file.separator")
					+ series [task] + ".xml");
				if(! metaDataFile.exists()){
					progress.notifyMessage("Task " + (task+1) + "/" + tasks + ": Could not be processed - MetaData .xml file missing for " + series [task] + "!", ProgressDialog.ERROR);
					break running;
				}			
		
				/**
				* Import the XML - generate document to read from it
				*/		
				Document metaDoc = null;
				try {
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();   
					DocumentBuilder db = dbf.newDocumentBuilder();  
					metaDoc = db.parse(metaDataFile);
					metaDoc.getDocumentElement().normalize();  
				} catch (SAXException | IOException | ParserConfigurationException e) {
					String out = "";
					for(int err = 0; err < e.getStackTrace().length; err++){
						out += " \n " + e.getStackTrace()[err].toString();
					}			
					progress.notifyMessage("Task " + (task+1) + "/" + tasks + ": Could not process " + series [task] + " - Error " + e.getCause() + " - Detailed message:\n" + out,  ProgressDialog.ERROR);
					break running;
				}		
				
				NodeList nodeList = metaDoc.getElementsByTagName("Attachment");  
				for(int n = 0; n < nodeList.getLength(); n++) {
					progress.notifyMessage("Task " + (task+1) + "/" + tasks + ": Node Type: " + nodeList.item(n).getNodeType(), ProgressDialog.LOG);
					progress.notifyMessage("Task " + (task+1) + "/" + tasks + ": Node Name: " + nodeList.item(n).getNodeName(), ProgressDialog.LOG);
					progress.notifyMessage("Task " + (task+1) + "/" + tasks + ": Node Value: " + nodeList.item(n).getNodeValue(), ProgressDialog.LOG);
				}
				
				processingDone = true;
				progress.updateBarText("finished!");
				progress.setBar(1.0);
				break running;
			}	
		progress.moveTask(task);
		}
	}
}//end main class