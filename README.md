
# BleachHack DarkReaper edition

My own skid of BleachHack for 1.19
I made this to make Bleach compatible with meteor and even gone so far as changing prefix to + and ClickGui bind to right control  
I also gave it own visual flavour  

## Showcase

![](https://i.imgur.com/yktU6CE.png)

## Installation
### For normal people

Download [Fabric for Minecraft 1.19](https://fabricmc.net/use/).
Download the latest version of DarkHack for your Minecraft version from releases.


On Windows: Type %appdata% into the Windows search bar; open the folder that comes up and then open '.minecraft'.

On Mac: Click on the desktop, press Command+Shift+G, type ~/Library and press enter; then open 'Application Support' and finally '.minecraft'.

On Linux: in a terminal window, type 'xdg-open ~/.minecraft'... or if you like working at the commandline, 'cd ~/.minecraft'.

Then inside that folder, you should see a folder named 'mods'. (If you don't see one, make sure you've installed Fabric already and started Minecraft again once).
That's where you'll put any mods you want to install.

### For (200 IQ) developers

Download the branch with the version you want to work on.  
Start A Command Prompt/Terminal in the main folder.  
Generate the needed files for your preferred IDE.  

***Eclipse***

  On Windows:
  > gradlew genSources eclipse
  
  On Linux:
  > chmod +x ./gradlew  
  >./gradlew genSources eclipse

  Start a new workspace in eclipse.
  Click File > Import... > Gradle > Gradle Project.
  Select the Main folder.
  
***IntelliJ***

  On Windows:
  > gradlew genIdeaWorkspace
  
  On Linux:
  > chmod +x ./gradlew  
  >./gradlew genIdeaWorkspace

  In idea click File > Open.
  Select build.gradle in the main folder.
  Select Open as Project.

***Other IDE's***

  Use [this link](https://fabricmc.net/wiki/tutorial:setup) for more information.
  It should be pretty similar to the eclipse and idea setup.
  
###### *Note: Java 17 is required for 1.18+*

## Recommended Mods

Here are some nice to have mods that are compatible with BleachHack, none of these require Fabric API.

### [Multiconnect](https://github.com/Earthcomputer/multiconnect) or [ViaFabric](https://github.com/ViaVersion/ViaFabric)
Mods that allows you to connect to any 1.12-1.19 server from a 1.19 client.

### [Baritone](https://github.com/cabaletta/baritone)
Baritone allows you to automate tasks such as walking, mining or building.

### [Sodium](https://www.curseforge.com/minecraft/mc-mods/sodium), [Lithium](https://www.curseforge.com/minecraft/mc-mods/lithium) and [Phosphor](https://www.curseforge.com/minecraft/mc-mods/phosphor)
Fixes Mojang's spaghetti.

## License

If you are distributing a custom version of BleachHack or a mod with ported features of BleachHack, you are **required** to disclose the source code, state changes, use a compatible license, and follow the [license terms](https://github.com/BleachDrinker420/BleachHack/blob/master/LICENSE).
