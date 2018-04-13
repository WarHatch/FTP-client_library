# FTP-client_library
An FTP client created without using preexisting ftp libraries

Made to be used in passive (PASV) mode.
Interaction with non windows file systems hasn't been tested.

## Command recognition
### Done:
**Data type:**
* RETR
* STOR
* STOU
* LIST
* PASV

**COMMAND type:**
* USER
* PASS
* QUIT
* NOOP
* SYST
* PWD, CWD, RMD, MKD
* RENAME FROM/TO (RNFR/RNTO)
* STAT

### TODO in the future:
* Transfer parameter commands(TYPE, MODE, STRU)
* APPEND 
* ABOR
