; The name that appears in the installer window
Name "Merchant of Venice 0.71b"
Caption "Merchant of Venice 0.71b"
OutFile "venice.exe"
InstallDir $PROGRAMFILES\Venice
BGGradient 6f8389 CCD1CC FFFFFF
InstallColors FF8080 000030
XPStyle on

; Heading text
ComponentText "This will install Venice stock market trading software onto \
your computer."

; Text to prompt user
DirText "Please select an install directory for Venice"

Section "Venice"
  SetOutPath $INSTDIR
  File "venice.jar"
  File "readme.txt"
  File "COPYING.txt"
  File "changelog.txt"
  File /r "doc"
  WriteUninstaller "uninstall.exe"
SectionEnd

Section "Start Menu Shortcuts"
  CreateDirectory "$SMPROGRAMS\Venice"
  CreateShortCut "$SMPROGRAMS\Venice\Manual.lnk" "$INSTDIR\doc\manual.html" "" "$INSTDIR\doc\manual.html" 0
  CreateShortCut "$SMPROGRAMS\Venice\License.lnk" "$INSTDIR\COPYING.txt" "" "$INSTDIR\COPYING.txt" 0
  CreateShortCut "$SMPROGRAMS\Venice\Read Me.lnk" "$INSTDIR\readme.txt" "" "$INSTDIR\readme.txt" 0
  CreateShortCut "$SMPROGRAMS\Venice\Venice.lnk" "$INSTDIR\venice.jar" "" "$INSTDIR\venice.jar" 0
  CreateShortCut "$SMPROGRAMS\Venice\Uninstall Venice.lnk" "$INSTDIR\uninstall.exe" "" "$INSTDIR\uninstall.exe" 0

SectionEnd

UninstallText "This will remove Venice from your computer."

Section "Uninstall"
  Delete $INSTDIR\venice.jar
  Delete $INSTDIR\COPYING.txt
  Delete $INSTDIR\changelog.txt
  Delete $INSTDIR\readme.txt
  Delete $INSTDIR\uninstall.exe
  Delete $INSTDIR\doc\*.*

  ; remove shortcuts, if any
  Delete "$SMPROGRAMS\Venice\*.*"

  ; remove directories used
  RMDir "$SMPROGRAMS\Venice"
  RMDir "$INSTDIR\doc"
  RMDir "$INSTDIR"
SectionEnd
