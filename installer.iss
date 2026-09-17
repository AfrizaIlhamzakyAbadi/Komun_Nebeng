[Setup]
; Basic Application Information
AppName=Komun_Nebeng_1.4
AppVersion=1.0.0
AppPublisher=Komun Nebeng
; Default installation folder
DefaultDirName={autopf}\Komun_Nebeng
; Start menu group name
DefaultGroupName=Komun Nebeng
; Output settings for the generated installer
OutputDir=Output
OutputBaseFilename=KomunNebengInstaller
Compression=lzma
SolidCompression=yes
; Architecture
ArchitecturesAllowed=x64
ArchitecturesInstallIn64BitMode=x64

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
; Main executable
Source: "build\windows\x64\runner\Release\komun_nebeng.exe"; DestDir: "{app}"; Flags: ignoreversion
; All other required files (DLLs, data folder, etc.)
Source: "build\windows\x64\runner\Release\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
; Start Menu Icon
Name: "{group}\Komun Nebeng"; Filename: "{app}\komun_nebeng.exe"
; Desktop Icon
Name: "{autodesktop}\Komun Nebeng"; Filename: "{app}\komun_nebeng.exe"; Tasks: desktopicon

[Run]
; Option to launch app after installation
Filename: "{app}\komun_nebeng.exe"; Description: "Launch Komun Nebeng"; Flags: nowait postinstall skipifsilent
