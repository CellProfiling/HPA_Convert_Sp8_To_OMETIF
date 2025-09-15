# HPA: Convert Sp8-Tif to OME-Tif
FIJI plugin that allows to convert the ome.tif files exported from the 3D viewer in Leica's LASX (Confocal Microscope DMi8-Sp8 or Stellaris 8) to OME-Tif files suitable for the HPA LIMS.
See release notes for more information.

## Copyright
(c) 2022-2025, Jan N. Hansen

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

### How to update the plugin to a newer version?
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

### How to use?
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
   1. Example for successful processing: GREEN BAR!
   2. Example for not-successfully processed images: The bar has turned red at the end of the processing and the message says: "Could not process" for certain files. If you cannot source the problem back to a wrong setting or export from Leica LASX, please submit an issue as follows: Click on the messages, press Control+A or Cmd+A to mark all messages, press Control+C or Cmd+C to copy all of the messages, submit with a notification explaining the issue to be submitted [here](https://github.com/CellProfiling/HPA_Convert_Sp8_To_OMETIF/issues) to ask for feedback from the developer. Make sure to answer follow up questions from the developer.

<p align="center">
   <img width="650" alt="image" src="https://github.com/user-attachments/assets/6f3867e0-4b9e-44ca-bd31-96ac68dc45e7" />
</p>

8. Copy the output folder to the folder from where LIMS imports files and rename it to the name that is required for LIMS import (```if<4-digit plate number>```)
