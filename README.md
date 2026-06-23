# HPA: Convert Sp8-Tif to OME-Tif
FIJI plugin that allows to convert the ome.tif files exported from the 3D viewer in Leica's LASX (Confocal Microscope DMi8-Sp8 or Stellaris 8) to OME-Tif files suitable for the HPA LIMS.
See release notes for more information.

## Copyright
(c) 2022-2026, Jan N. Hansen

Contact: jan.hansen (at) scilifelab.se

## Licenses
The plugin and the source code are published under the [GNU General Public License v3.0](https://github.com/hansenjn/ExtractSharpestPlane_JNH/blob/master/LICENSE).

## Download
Download the latest plugin version [here](https://github.com/CellProfiling/HPA_Convert_Sp8_To_OMETIF/releases/).

## How to install?
#### Installing FIJI / ImageJ to your computer
FIJI is open-source, freely available software that can be downloaded [here](https://imagej.net/software/fiji/downloads). To install ImageJ / FIJI, you only need to download the distribution of your choice and fitting to your Operating system and extract the software directory from the downloaded archive. The resulting folder can be placed anywhere on your computer (where you have read and write permissions). 

On Mac OS, do not place the ImageJ / FIJI application into the *Applications* folder. Instead place it somewhere else than in the *Applications* folder, i.e. to the *desktop* or the *documents* directory. Thereby you can avoid a collision with a security feature by Mac OS, that might otherwise trigger software failures. If Mac OS does not allow you to launch FIJI by clicking the FIJI.app directory due to security concerns, run FIJI by right click (or holding option while clicking) to open more options for the file and click "open".

#### Installing the *HPA_Convert_OPERA_To_LIMS-OMETIF* FIJI plugin
- Download the .jar file from the latest software release listed on the [releases page in this repository](https://github.com/CellProfiling/HPA_Convert_Sp8_To_OMETIF/releases/).

<p align="center">
   <img src="https://github.com/user-attachments/assets/f7b671b6-9a9c-46db-8e3a-a044ff9b21bb" width=700>
</p>


- Launch ImageJ and install the plugin by drag and drop of the downloaded .jar file into the ImageJ window (red marked region in the screenshot below):
<p align="center">
   <img src="https://user-images.githubusercontent.com/27991883/201358020-c3685947-b5d8-4127-88ec-ce9b4ddf0e56.png" width=500>
</p>

- Confirm the installation by pressing save in the upcoming dialog.

- Next, FIJI requires to be restarted (close it and start it again).

- You should now be able to start the plugin via the menu entry <i>Plugins > Cell Profiling > Extend ome.tif from Sp8 to LIMS-like ome.tif (v...)</i>


<p align="center">
   <img src="https://github.com/user-attachments/assets/96a882c0-a76d-437f-b000-e0c48d8365bb" width=500>
</p>

## How to update the plugin to a newer version?
- Download the .jar file from the latest software release (or the version release you want to change to) from the [releases page in this repository](https://github.com/CellProfiling/HPA_Convert_Sp8_To_OMETIF/releases/).
- Make sure ImageJ/FIJI is closed.
- Find your ImageJ/FIJI repository and enter the plugins folder.

<p align="center">
   <img width="170" src="https://github.com/user-attachments/assets/6ed99ebe-c960-4811-a532-c3c9f3da1ded" />
   <img width="500" src="https://github.com/user-attachments/assets/5386b219-21c7-49e9-b75a-136cec50ffa1" />
</p>

- Delete the old version.
- Paste the newly downloaded .jar file.
- Start ImageJ/FIJI.
- You should now be able to see that the menu entry <i>Plugins > Cell Profiling > Extend ome.tif from Sp8 to LIMS-like ome.tif (v...)</i> shows the new plugin version within the parantheses.

## How to use?

### Step by Step

0. Make sure the plugin was installed
1. Start the plugin through the FIJI menu: <i>Plugins > Cell Profiling > Extend ome.tif from Sp8 to LIMS-like ome.tif (v...)</i>
2. Enter a file path for the output folder - Notes:
   1. All files for LIMS will be saved into this folder.
   2. This folder can also be selected on a mounted drive (e.g., the confocal server, from where LIMS imports images).
   3. IMPORTANT: Make sure to either (A) not name the folder already in a way that LIMS will start importing while you are still exporting files with this plugin (DO NOT name it "if<the number>") or (B) not have started acquisition yet (in this case it is ok to name it "if<the number>". This is important since otherwise you will likely get lots of LIMS errors or export errors.

<p align="center">
   <img width="600" src="https://github.com/user-attachments/assets/71509dba-e587-4dba-8fdb-146633aba42d" />
</p>

3. Click OK!
4. A dialog pops up that asks you to list the folders you want to process.

<p align="center">
   <img width="500" src="https://github.com/user-attachments/assets/3875f018-1cab-4f0d-af41-6c662010639f" />
</p>

5. Select the folders you want to process

<p align="center">
   <img width="680" alt="image" src="https://github.com/user-attachments/assets/e58d5289-f548-45b8-8439-09c4ebb5244d" />
</p>

6. Click 'Start processing'

<p align="center">
   <img width="500" alt="image" src="https://github.com/user-attachments/assets/2637f498-3802-4ac9-9b9f-9511fca59792" />
</p>

6. Now let the plugin run. It is normal that some messages are logged on the bottom.
   1. It is normal that there might be an additional window popping up with some cryptic messages. This is normal and comes from OME-TIF loading through BioFormats plugins.

<p align="center">
   <img width="400" src="https://github.com/user-attachments/assets/35c69c78-f465-4950-9362-959c2f5bcdcf" />
</p>

   2. It is normal that there may be logging information on corrections in the OME XML data, such as the following messages for processing single-plane images:
  
<p align="center">
   <img width="800" src="https://github.com/user-attachments/assets/af98472c-94d8-44c8-8097-19360b59cf87" />
</p>

   3. The only problem is if ERRORS appear, which will let the bar turn red at the end of the processing and the message says: "Could not process" - see under point 7. 

7. When the processing is done, the bar should turn green. If it is not green but red, you might have had processing errors.
   1. Example for successful processing: GREEN or ORANGE bar. The orange bar just indicates that there are some notifications. You can review those - they might be as specified in 6.i. and unless they are concering, all is good.

<p align="center">
   <img width="650" alt="image" src="https://github.com/user-attachments/assets/2994957f-184b-46bb-a34b-7962f5a2675e" />
</p>



   3. Example for not-successfully processed images: The bar has turned red at the end of the processing and the message says: "Could not process" for certain files. If you cannot source the problem back to a wrong setting or export from Leica LASX, please submit an issue as follows: Click on the messages, press Control+A or Cmd+A to mark all messages, press Control+C or Cmd+C to copy all of the messages, submit with a notification explaining the issue to be submitted [here](https://github.com/CellProfiling/HPA_Convert_Sp8_To_OMETIF/issues) to ask for feedback from the developer. Make sure to answer follow up questions from the developer.

<p align="center">
   <img width="650" alt="image" src="https://github.com/user-attachments/assets/6f3867e0-4b9e-44ca-bd31-96ac68dc45e7" />
</p>

8. If processing was successful, you can proceed to LIMS upload: Copy the output folder to the folder from where LIMS imports files and rename it to the name that is required for LIMS import (```if<4-digit plate number>```)

---

### Detailed Setup Guide (from v0.2.2) on

When you launch the plugin, you'll see a configuration dialog with four main sections:

#### 1. Output Settings

**Output folder path**: Where processed files will be saved.

**Folder Naming**:
- **DO NOT** name the folder `if####` (where #### is a 4-digit number) while the plugin is running
- LIMS automatically imports from folders matching this pattern
- If you use this naming during processing, LIMS will start importing incomplete/corrupted files
- **Recommended workflow**:
  1. Use a temporary folder name during export (e.g., `temp_export`, `processing_batch1`)
  2. Wait for plugin to finish completely
  3. Rename folder to `if####` format for LIMS import

**Folder Contents**:
- Use an **empty folder** or unique subfolder to avoid overwriting existing files
- The plugin **WILL overwrite** files with identical names without additional warning
- If the output folder contains existing files, the plugin will show a warning with option to change folders

**Where to save**:
- Local disk (fastest) and later copy to confocal drive
- Mounted network drive (convenient for direct LIMS import)
- Make sure you have write permissions

---

#### 2. Channel Assignment (REQUIRED)

This is the most important configuration step. You must map each microscope channel to a LIMS-recognized channel type, which LIMS will detect later.

**Required Channel Types** (all must be assigned):
- `blue (DAPI/DNA)` - Nuclear staining, typically DAPI
- `green (Protein of Interest)` - Your target protein (HPA antibody)
- `red (MT/Cilia)` - Microtubules (standard) or cilia marker (cilia section) or flagellar marker (sperm)
- `yellow (ER/BB)` - Endoplasmic reticulum (standard) or basal body marker (cilia section) or mitochondria marker (sperm)
- `white (TI)` - Transmitted light/brightfield (can be skipped, see below)

**Special Rules**:
1. **All four core colors required**: You cannot skip blue, green, red, or yellow
2. **White is optional**: For 4-channel setups, set Channel 4 to `NA`
3. **Green can be double assigned**: You can assign green two times but ⚠️ **Important**: When using duplicate green assignment (e.g., for sperm atlas), you **MUST** run the [HPA_LIMS_Channel_Selector](https://github.com/CellProfiling/HPA_LIMS_Channel_Selector/) tool on the output files **before** uploading to LIMS. This tool selects which green channel should be used as the primary channel for LIMS processing.

**Example Configurations**:

**Standard 4-channel imaging (Sp8/Stellaris if blue and red are acquired in first sequential):**
Channel 0 -> blue (DAPI/DNA)
Channel 1 -> red (MT/Cilia)
Channel 2 -> green (Protein of Interest)
Channel 3 -> yellow (ER/BB)
Channel 4 -> NA

**5-channel with transmitted light (Sp8/Stellaris if blue, red, white acquired all in first sequential):**
Channel 0 -> blue (DAPI/DNA)
Channel 1 -> red (MT/Cilia)
Channel 2 -> white (TI)
Channel 3 -> green (Protein of Interest)
Channel 4 -> yellow (ER/BB)

**What happens if you get it wrong?**

The plugin will validate your channel assignments and show an error if:
- Any of the four core colors (blue, green, red, yellow) are missing
- You try to proceed without all required assignments

Simply click OK on the error dialog and fix your channel assignments.

---

#### 3. Logging Options

These are for troubleshooting only. Leave all unchecked for normal operation.

Enable these if:
- You encounter processing errors
- A developer asks you to provide detailed logs
- You're debugging metadata transfer issues

**Warning**: Enabling these options generates **very verbose** output in the Log window and may slow down processing.

---

#### 4. Input File Selection

After clicking OK in the main dialog, you'll select folders to process.

**What to select**:
- The top-level folder(s) containing your exported data
- Each folder must contain:
  - `.ome.tif` files (exported from LASX)
  - A `MetaData` or `Metadata` subfolder
  - `.ome.xml` files in the MetaData folder (one per image position)

**The plugin will**:
- Recursively search through all subfolders
- Find all `.ome.tif` files with matching `.ome.xml` metadata
- Process each valid file pair

---

### Common Questions & Troubleshooting

**Q: Can I process multiple experiments at once?**  
A: Yes! Select multiple top-level folders in the file selection dialog. But note: They end up in the same output folder, so only do it for multiple files from the same plate.

**Q: What if I have both TileScan and manual acquisitions?**  
A: Both are supported. Manual acquisitions should be restructured in LASX as Collections before export (Nested format: `TileScan 1 > Row > Column > image stack`).

**Q: The plugin is running slowly. Is this normal?**  
A: Yes. Processing includes:
- Reading large TIFF files
- Parsing XML metadata
- Enriching OME metadata
- Writing complete OME-TIFF files

Large datasets (especially multi-well plates with many positions) can take considerable time.

**Q: Can I cancel processing midway?**  
A: Yes, close the progress window and FIJI. Restart FIJI. Already processed files will remain in the output folder and are valid. You can resume by running the plugin again and selecting only the unprocessed folders. It is recommended though to rather delete and rerun entirely to avoid human mistakes.

**Q: What if some files fail to process?**  
A: 
1. The progress window will show errors (messages in red at bottom, red progress bar)
2. Copy the entire error log: Click on notification area → Ctrl+A (Cmd+A on Mac) → Ctrl+C (Cmd+C on Mac)
3. Submit an issue on [GitHub Issues](https://github.com/CellProfiling/HPA_Convert_Sp8_To_OMETIF/issues) with:
   - The copied error log
   - Description of your setup (Sp8 vs Stellaris, TileScan vs manual, etc.)
   - Any unusual recording settings

**Q: I see orange warnings but processing completed. Is this a problem?**  
A: Orange warnings are usually informational (e.g., position corrections, missing optional metadata). As long as the final message says "processing done" and the bar is green or orange (not red), your files should be fine. Common warnings include:
- XML position corrections (normal for manual recordings)
- Missing optional metadata fields
- Automatic adjustments to ensure LIMS compatibility

**Q: The output folder already has files. What happens?**  
A: The plugin will warn you before processing. Files with identical names will be **overwritten without additional confirmation**. Best practice: always use an empty folder or create a unique subfolder for each processing batch.

