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


