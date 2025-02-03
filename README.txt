NAME
   tvmv - Moves/Copies TV episode files into the correct destination TV folder
          based on the show name, season number and episode number parsed from
          the input files.

SYNOPSIS
   tvmv FILE...|DIR... [-chnrs]

DESCRIPTION
   "tvmv" allows you to move/copy TV episode files into the correct TV folder
   based on the show name, season number and episode number parsed from the
   input file names.
   
   "tvmv" also allows you to replace existing episodes (via the -r flag). This
   may be useful if you need to upgrade SD episodes to HD for example.

   "tvmv" is multi-episode aware when replacing episodes to avoid replacing a
   multi episode with a single episode for example. 

   If the season folder for an input episode does not exist, it will be created
   automatically.
   
   The TV folder destinations that episodes will be moved/copied to, are set
   via the tvmv.conf file. You can set multiple TV destinations for the case
   when your TV collection is not located in the same directory/filesystem.
   Windows Libraries can also be used as the destination if Windows 7/8.
   
   Some TV shows may be released with aliases (e.g. CSI NY = CSI New York). To
   ensure aliased episodes are moved/copied to the right place, aliases can be
   added to aliases.txt to map an alias to your TV show folder.

   See the FILES section for more information.

OPTIONS

   FILE...
      The FILE argument(s) are the file paths for the episode files to be moved

   DIR...
      The DIR argument(s) are the file paths for directories that contain
      episode files to be moved or copied. Can be used with FILE.
      
   --config FILE
      Read the tvmv.conf config file from the path FILE

   -c, --copy
      This flag makes tvmv copy the input episode FILEs instead of moving them.

   -h, --help
      The help message will be output and the program will exit.
      
   -n, --native
      Use Java's NIO API's instead of using Java's IO Streams. Native IO will
      not display a progress bar, but will avoid performing a copy and delete
      when the source and destination are on the same filesystem if moving.

   -r, --replace
      This flag makes tvmv remove the existing episode file (under the season
      directory in one of the TV source folders) and moves/copies the input 
      episode file to the season directory.

   -s, --skip-not-matched
      This flag makes tvmv skip any input files that cannot be matched. The
      default action is to exit when unable to match the episode. 
      
EPISODE IO
   By default, tvmv uses Java IO Streams to copy/move the episode files and
   display file transfer progress. When moving, a copy and delete is performed.
   Moving files this way is inefficient when the source and destination are on
   the same filesystem because a simple rename would suffice. To avoid this
   drawback, you can specify the -n, --native flag to use Java's NIO API's.
   Native IO will no longer display file transfer progress.
   
   Episodes files can contains multiple episode numbers so this is taken into
   account when replacing. Episodes are replaced in sets e.g. [1,2] => [1],[2].
   If any episode in the set fails to be copied/moved, the previous transfers
   are rolled back to the original state of the episode set.

   When replacing episode files, the existing TV destination file to be
   replaced is moved to a temorary file in the same directory. If the copy or
   move operation succeeds, the temporary file will be deleted. If the
   operation fails, the the temporary file will restored and any other episodes
   in the set will be rolled back.
   
FILES
   The TV destination directories should use the following structure:
   
      TV destination/Show Name/Season x/
      TV destination/Show Name/Series x/

   The following files are used by tvmv:
      tvmv.conf (REQUIRED)
         This file stores the configuration data for tvmv. This is where you
         define the TV destination files, and an optional episodes source
         directory. Windows 7/8 users can also define a destination library.
         See sample.tvmv.conf for more information.
      aliases.txt
         This file stores TV show aliases that are mapped to a TV show
         directory. Multiple aliases can point to one show. The line format is:
            <SHOW_ALIAS>=<SHOW_DIRECTORY>
         There is no need to escape or quote characters. The file should be 
         encoded as UTF-8 without the BOM. For example show alias "CSI NY"
         would map to show directory "CSI New York":
            CSI NY=CSI New York

   Default Configuration Directories
      The current directory is first checked for tvmv.conf, and if not found,
      the following directories are checked:

      Windows %USERPROFILE%\tvmv\
      Linux ~/.config/tvmv/
      Mac /Users/.config/tvmv/

EXAMPLES
   Lets define an example directory structure:

   tvmv.conf contains the lines:
   DESTINATION=D:\TV
   DESTINATION=\\archive\TV
   
   D:\TV                                | \\archive\TV
      Scrubs\                           |    CSI New York\
         Season 1                       |       Season 1\
      Modern Family\                    |       Season 2\
         Season 1\                      |    Friends\
         Season 2\                      |       Season 1\
      

   tvmv /path/to/scrubs.s01e01e02.avi "Friends - 1x01 - Pilot.mkv" modern_family_301_blah.mp4

   The example above will move: 
      /path/to/scrubs.s01e01e02.avi to D:\TV\Scrubs\Season 1\scrubs.s01e01e02.avi
      Friends - 1x01 - Pilot.mkv to \\archive\\TV\Friends\Season 1\Friends - 1x01 - Pilot.mkv
      modern_family_301_blah.mp4 to D:\TV\Modern Family\Season 3\modern_family_301_blah.mp4
      
   "D:\TV\Modern Family\Season 3" did not exist before running tvmv. Season
   directories are created as needed.
   
   Now that "D:\TV\Scrubs\Season 1\scrubs.s01e01e02.avi" exists, the following
   example shows how to replace this episode:
   
   tvmv -r /some/path/to/Scrubs_101_720p.mkv "/some/path/to/Scrubs - s01.e02.mkv"
   
   The episodes named Scrubs_101_720p.mkv and "Scrubs - s01.e02.mkv" are moved
   to "D:\TV\Scrubs\Season 1" and "D:\TV\Scrubs\Season 1\scrubs.s01e01e02.avi" will
   be deleted.

   If instead of passing Scrubs episodes 1 and 2 as arguments, only episode 1
   was given, then the replacement would not succeed because episode 2 would
   lost in the replacement.

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