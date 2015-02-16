# SettingsDeployer for Kodi
A tool to automate download and deployment of settings/addons/repos for XBMC/Kodi on Android.

This is a simple android application that, when run, fetches a ZIP archive from a predefined URL (or set of URLs),
downloads it and extracts it to the proper location of the master profile of XBMC/Kodi in internal storage.
For this, XBMC/Kodi is first detected by the code and the profile directory detected.

Permissions:

Read/Write external storage - Used to save the zip package to internal storage, to detect Kodi/XBMC profile dirs and extract the package.
Full internet access - To download the package set in the urls.xml.
Prevent the device from sleeping - During download/extract the device shouldn't turn off the CPU otherwise we're toast.


Version 1.2:

* Now using a wakelock to keep the device awake during the download and extraction work. The wakelock is release when
  the work finishes, an error occurs, the user cancels or 4 minutes have passed. If the device can't download and extract the package
  in 4 minutes of time something is awry.

Initial version 1.1:

* Detect XBMC/Kodi via internal storage created dirs or by querying the PackageManager.
* Download predefined ZIP file. Can be overriden for each flavor (see the build.gradle file for the module to see what flavors exist or create new ones).
  URLs are defined in urls.xml files.
* Automatically extract.
* Log all operations and cleanup stuff when done.
* Do stuff in the background and report progress. When exiting via the Back key, all operations are cancelled, currently.
* No wakelocks - if the screen turns off it might ruin everything. Wakelocks will be present in future versions.

Currently tailored to be used by TheWiz to deploy his Kodi package but can be forked and used by anyone.
MIT licence - just add the licence to whatever you do with this code.
