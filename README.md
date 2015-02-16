# SettingsDeployer for Kodi
A tool to automate download and deployment of settings/addons/repos for XBMC/Kodi on Android.

This is a simple android application that, when run, fetches a ZIP archive from a predefined URL (or set of URLs),
downloads it and extracts it to the proper location of the master profile of XBMC/Kodi in internal storage.
For this, XBMC/Kodi is first detected by the code and the profile directory detected.

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
