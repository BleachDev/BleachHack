
# BleachHack
![](https://img.shields.io/github/downloads/bleachdrinker420/BleachHack/total?style=flat-square)
![](https://img.shields.io/tokei/lines/github/BleachDrinker420/BleachHack?style=flat-square)
![](https://img.shields.io/github/languages/code-size/bleachdrinker420/BleachHack?style=flat-square)
![](https://img.shields.io/github/last-commit/bleachdrinker420/BleachHack?style=flat-square)
![](https://img.shields.io/badge/daily%20commit-yes-blue?style=flat-square)
![](https://img.shields.io/discord/620600892718055434?style=flat-square)

Obama client for Fabric 1.16 & 1.17 (1.14-1.15 is discontinued).

> Website: https://bleachhack.org/  
> Discord: https://bleachhack.org/discord

## Showcase
<details>
 <summary>Images</summary>

 ![](https://res.bleachhack.org/images/ClickguiShowcase.jpg)

 ![](https://res.bleachhack.org/images/RenderShowcase.jpg)

</details>

## Installation
### For normal people

Download [Fabric for Minecraft 1.16/1.17](https://fabricmc.net/use/).
Download the latest version of BleachHack for your Minecraft version [from the website](https://bleachhack.org/).


On Windows: Type %appdata% into the Windows search bar; open the folder that comes up and then open '.minecraft'.

On Mac: Click on the desktop, press Command+Shift+G, type ~/Library and press enter; then open 'Application Support' and finally '.minecraft'.

On Linux: in a terminal window, type 'xdg-open ~/.minecraft'... or if you like working at the commandline, 'cd ~/.minecraft'.

Then inside that folder, you should see a folder named 'mods'. (If you don't see one, make sure you've installed Fabric already and started Minecraft again once).
That's where you'll put any mods you want to install.

### For (200 IQ) developers

Download the project.
Start A Command Prompt/Terminal in the BleachHack-Fabric-(*Version*) folder.
Generate the needed files for your preferred IDE.

***Eclipse***

  On Windows:
  > gradlew genSources eclipse
  
  On Linux:
  > chmod +x ./gradlew  
  >./gradlew genSources eclipse

  Start a new workspace in eclipse.
  Click File > Import... > Gradle > Gradle Project.
  Select the BleachHack-Fabric-(*Version*) folder.

***Other IDE's***

  Use [this link](https://fabricmc.net/wiki/tutorial:setup) for more information.
  It should be pretty similar to the eclipse setup.
  
###### *Note: Java 16 is required for 1.17+*

## Recommended Mods

Here are some nice to have mods that are compatible with BleachHack, none of these require Fabric API.

### [Multiconnect](https://github.com/Earthcomputer/multiconnect)
Multiconnect allows you to connect to any 1.8-1.17 server from a 1.17 client.

### [Baritone](https://github.com/cabaletta/baritone)
Baritone allows you to automate tasks such as walking, mining or building.

## License

If you are distributing a custom version of BleachHack or a mod with ported features of BleachHack, you are **required** to disclose the source code, state changes, use a compatible license, and follow the [license terms](https://github.com/BleachDrinker420/BleachHack/blob/master/LICENSE).
