In general, the usersOtherClasses folder is a location for classes that the user might 
wish to create but that do not belong in the userRules or userAnalyses folder.  User
rules and analyses might reference classes in this folder, for example.

Note that this folder also contains an empty class called ExternalResourceLoader.
The ExternalResourceLoader class serves as a place holder for loading external
resources. The URLResource class uses this class to locate files outside of the jar. 
Removing this file will prevent the user from adding some features (like icons).