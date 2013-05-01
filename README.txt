NAME
   tvmv - Moves TV episode files into the correct destination TV folder based
          on the show name, season number and episode number parsed from the
          input files.

SYNOPSIS
   tvmv FILE... [-hr]

DESCRIPTION
   "tvmv" allows you to move TV episode files into the correct TV folder based
   based on the show name, season number and episode number parsed from the
   input file names.
   
   "tvmv" also allows you to replace existing episodes (via the -r flag). This
   may be useful if you need to upgrade SD episodes to HD for example.

   If the season folder for an input episode does not exist, it will be created
   automatically.
   
   The TV folder sources that episodes will be moved to, are set via the
   sources.conf file. You can set multiple TV folder sources for the case when
   your TV collection is not located in the same directory/filesystem.
   
   Some TV shows may be released with aliases (e.g. CSI NY = CSI New York). To
   ensure aliased episodes are moved to the right place, aliases can be added
   to aliases.txt to map an alias to your TV show folder.

   See the FILES section for more information.

OPTIONS

   FILE...
      The FILE argument(s) are the file paths for the episode files to be moved

   -h, --help
      The help message will be output and the program will exit.
      
   -r, --replace
      This flag makes tvmv remove the existing episode file (under the season
      directory in one the TV source folders) and moves the input episode file
      to the season directory.
			
Episode IO
   tvmv doesnt perform a direct "move" operation on the input episode files.
	 Instead the input episode files are copied to the season directory as a
	 temporary file and then renamed to the same file name as the input file.
	 If this copy and rename was successful, only then will the original input
	 episode file be deleted.
	 
	 When replacing an episode, the same steps are applied as above except that
	 the existing episode will only be deleted if the input file was successfully
	 copied to the season directory. If the copy wasn't successful the temp file
	 will be deleted and the original input file will remain unchanged.
	 
FILES
   The TV folder sources should use the following folder structure:
   
      TV Source Folder/Show Name/Season x/

   The following files are used by tvmv:
      sources.conf (REQUIRED)
         This file stores the TV folder sources that the episodes should be
         moved to. Each folder should be placed on a new line. Network
         paths can be used e.g. \\192.168.0.10\tv or \\share\mount
      aliases.txt
         This file stores TV show aliases that are mapped to a TV show
         directory. Multiple aliases can point to one show. The line format is:
            <SHOW_ALIAS>=<SHOW_DIRECTORY>
         There is no need to escape or quote characters. The file should be 
         encoded as UTF-8 without the BOM. For example show alias "CSI NY"
         would map to show directory "CSI New York":
            CSI NY=CSI New York

   Default Configuration Directories
      Windows C:\ProgramData\$USER\tvmv\
      Linux ~/.config/tvmv/

EXAMPLES
   Lets define an example directory structure: sources.conf contains the lines
   D:\TV and \\archive\TV.
   
   D:\TV                                | \\archive\TV
      Scrubs\                           |    CSI New York\
         Season 1                       |       Season 1\
      Modern Family\                    |       Season 2\
         Season 1\                      |    Friends\
         Season 2\                      |       Season 1\
      

   tvmv /path/to/scrubs.s01e02.avi "Friends - 1x01 - Pilot.mkv" modern_family_301_blah.mp4

   The example above will move: 
      /path/to/scrubs.s01e02.avi to D:\TV\Scrubs\Season 1\scrubs.s01e02.avi
      Friends - 1x01 - Pilot.mkv to \\archive\\TV\Friends\Season 1\Friends - 1x01 - Pilot.mkv
      modern_family_301_blah.mp4 to D:\TV\Modern Family\Season 3\modern_family_301_blah.mp4
      
   "D:\TV\Modern Family\Season 3" did not exist before running tvmv. Season
   directories are created as needed.
   
   Now that "D:\TV\Scrubs\Season 1\scrubs.s01e02.avi" exists, the following
   example shows how to replace this episode:
   
   tvmv -r /some/path/to/Scrubs_102_720p.mkv
   
   The episode file /some/path/to/Scrubs_102_720p.mkv is moved to
   "D:\TV\Scrubs\Season 1" and "D:\TV\Scrubs\Season 1\scrubs.s01e02.avi" will
   be deleted.

   If there is an alias defined in aliases.txt with the line:
      CSI NY=CSI New York
   Then the following example will move the file csi.ny.s02e01.name.avi to
   "\\archive\TV\CSI New York\Season 2\csi.ny.s02e01.name.avi".
   
   tvmv csi.ny.s02e01.name.avi

COPYRIGHT
   Copyright (c) 2013, Sam Malone. All rights reserved.

LICENSING
   The tvmv source code, binaries and documentation are licensed under a BSD
   License. See LICENSE for details.

AUTHOR
   Sam Malone