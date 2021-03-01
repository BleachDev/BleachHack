# BleachHack-1.14
[![Github All Releases](https://img.shields.io/github/downloads/bleachdrinker420/bleachhack-1.14/total.svg)]()
![](https://img.shields.io/github/last-commit/bleachdrinker420/bleachhack-1.14.svg)
![](https://img.shields.io/github/languages/code-size/bleachdrinker420/bleachhack-1.14.svg)
![](https://img.shields.io/badge/daily%20commit-yes-blue)

Very cool client.  

Works on fabric 1.16 & 1.17 Snapshots (1.14-1.15 is discontinued)

> Join The Discord: https://discord.gg/b5Wc4nQ

## Installation:
**For normal people:**

Download [fabric for minecraft 1.16/1.17](https://fabricmc.net/use/)  
Download the lastest compiled version of bleachhack for your Minecraft version [from the releases section](https://github.com/BleachDrinker420/bleachhack-1.14/releases)

On Windows: type %appdata% into the location field of the Windows Explorer; then open '.minecraft'.

On Mac: click on the desktop, press Command+Shift+G, type ~/Library and press enter; then open 'Application Support' and finally '.minecraft'.

On Linux: in a terminal window, type 'xdg-open ~/.minecraft'... or if you like working at the commandline, 'cd ~/.minecraft'.

Then inside that folder, you should see a folder named 'mods'. (If you don't see one, make sure you've installed Fabric already and started Minecraft again once).
That's where you'll put any mods you want to install. 

--------------

**For (200 IQ) developers:**

Download the project  
Start A Command Prompt in the BleachHack-Fabric-(*Version*) folder. 
Generate the needed files for your preferred IDE.

***Eclipse:***

  On Windows:
  > gradlew genSources eclipse
  
  On Linux:
  > chmod +x ./gradlew  
  >./gradlew genSources eclipse

  Start a new workspace in eclipse  
  Click File > Import... > Gradle > Gradle Project  
  Select the BleachHack-Fabric-(*Version*) folder  
  
***Other IDE´s:***

  Use [this link](https://fabricmc.net/wiki/tutorial:setup) for more information.
  It should be pretty similar to the eclipse setup.
  
--------------

### For skidders:

If you are distributing a custom version of BleachHack or a mod with ported features of BleachHack, you are **required** to disclose the source code, state changes, use a compatible license, and follow the [licence terms](https://github.com/BleachDrinker420/bleachhack-1.14/blob/master/LICENSE)
