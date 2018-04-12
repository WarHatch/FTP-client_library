# FTP-client_library
An FTP client created without using preexisting ftp libraries

Made to be used in passive (PASV) mode.
Interaction with non windows file systems hasn't been tested.

## Command recognition
**Done:**
* USER
* PASS
* QUIT
* PASV
* RETR
* STOR
* LIST
* NOOP
* SYST
* PWD
* CWD
* RMD
* MKD

**TODO:**
* RENAME FROM/TO (RNFR/RNTO)
* Implement STAT usage in long transfers
* Add server connection args on launch

**TODO maybe:**
* Transfer parameter commands(TYPE, MODE, STRU)
* STOU
* APPEND 
