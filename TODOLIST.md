# BleachHack 1.16.2 epearl edition
![](https://img.shields.io/github/last-commit/22s/bleachhack-1.16-epearl-edition.svg)
![](https://img.shields.io/github/languages/code-size/22s/bleachhack-1.16-epearl-edition.svg)
##Todo List
Modules to add:
- AutoTrap: <https://github.com/dewysoftwareleaks/Ruhama/blob/master/src/main/java/bleach/a32k/module/modules/ObsidianTrap.java>
Additions: Need to add toggleable option for netherite and multiple variant trap types and an option for persistent
- HoleFiller: <https://github.com/dewysoftwareleaks/Ruhama/blob/master/src/main/java/bleach/a32k/module/modules/HoleFiller.java>
- AutoBreed: <https://raw.githubusercontent.com/seppukudevelopment/seppuku/master/src/main/java/me/rigamortis/seppuku/impl/module/misc/BreedModule.java> (Already in the filesystem commented out and partially completed)
- LiquidInteract: <https://raw.githubusercontent.com/seppukudevelopment/seppuku/master/src/main/java/me/rigamortis/seppuku/impl/module/world/LiquidInteractModule.java>
- EntityControl: <https://github.com/seppukudevelopment/seppuku/blob/master/src/main/java/me/rigamortis/seppuku/impl/module/movement/EntityControlModule.java>
- Welcomer: <https://github.com/seppukudevelopment/seppuku/blob/master/src/main/java/me/rigamortis/seppuku/impl/module/misc/GreeterModule.java>
Additions: Rename from greeter to Welcomer and add file with variables for custom messages
- MoreInv/XCarry: <https://github.com/seppukudevelopment/seppuku/blob/master/src/main/java/me/rigamortis/seppuku/impl/module/misc/MoreInvModule.java>
- Coord Logger: <https://raw.githubusercontent.com/seppukudevelopment/seppuku/master/src/main/java/me/rigamortis/seppuku/impl/module/misc/CoordLoggerModule.java>
- NoBreakAnimations: <https://github.com/seppukudevelopment/seppuku/blob/master/src/main/java/me/rigamortis/seppuku/impl/module/render/NoBreakAnimModule.java>
- Logout Spots: <https://github.com/seppukudevelopment/seppuku/blob/master/src/main/java/me/rigamortis/seppuku/impl/module/render/LogoutSpotsModule.java>
- Tablist Color Friends: <https://github.com/zeroeightysix/KAMI/commit/6752db8b83a080b843d5ad3ec3b45f9fed2b8df8>
- AntiForceLook: <https://github.com/zeroeightysix/KAMI/blob/fabric/src/main/java/me/zeroeightsix/kami/feature/module/player/AntiForceLook.java>
- AntiFog: <https://github.com/zeroeightysix/KAMI/blob/fabric/src/main/java/me/zeroeightsix/kami/feature/module/render/AntiFog.java>
- Velocity (bleach's one can be pushed by water and players/entities): <https://github.com/zeroeightysix/KAMI/blob/fabric/src/main/java/me/zeroeightsix/kami/feature/module/movement/Velocity.java>
- NewChunks: <https://github.com/zeroeightysix/KAMI/blob/fabric/src/main/java/me/zeroeightsix/kami/feature/module/render/ChunkFinder.java>
- AutoBuilder: <https://github.com/ionar2/salhack/blob/master/src/main/java/me/ionar/salhack/module/world/AutoBuilderModule.java>
- EChest Farmer: <https://github.com/ionar2/salhack/blob/master/src/main/java/me/ionar/salhack/module/world/EnderChestFarmer.java>
- AutoWither: <https://github.com/ionar2/salhack/blob/master/src/main/java/me/ionar/salhack/module/world/AutoWitherModule.java>
- CityESP: <https://github.com/ionar2/salhack/blob/master/src/main/java/me/ionar/salhack/module/render/CityESPModule.java>
- AntiHurtcam: <https://github.com/zeroeightysix/KAMI/blob/fabric/src/main/java/me/zeroeightsix/kami/feature/module/render/NoHurtCam.java>
- CursorBlockHighlight: <https://github.com/fr1kin/ForgeHax/blob/1.16/src/main/java/dev/fiki/forgehax/main/mods/render/BlockHighlightMod.java>

Minor Adjustments:
- Make scaffold & surround turn off lock to blocks when nothing to place and surround returns to last item
- Add Totem Pop Counter
- Add Inventory Totem Counter
- Fix .drawn command isn’t mapped in the filemanager so it doesn’t save anything when turning off
- Add block highlight/search module
- Vanilla Speed option
- Donkey Detect module
- In armor hud add vertical alignment, text mode, and percentage mode
- implement 2b2t flight mode
- add panel for toggling modules into the popout gui
- fix popout gui viafabric compatibility
- fix tps counter in popout gui
- fix crash when selecting text in the UI hud
- improve autoopen on elytras
- liquid remover module
- Long jump module
- Reach module
- Auto cobweb (places cobweb in your block to stop player movement)  Idk if no slow works against cobweb
- Make colors work right in wireframe esp
- make drawn ignore syntax and allow you to send .drawn [module] without true/false to toggle true/false while still allowing true/false argument
- add module for toggle msgs in chat
- Chat logger & file logger when detecting cactus placed in the nether
- properly center the module description left of the module
- map position esp (wireframe box of the chunks a map will render)
- Auto Bed Bomb module
- Fucker module / auto right click beds in nether/end module and auto charge and right click respawn anchors in overworld/end module and make it pull glowstone from inventory
- Change BoxESP into a much better model
- Fix nametags not rendering armor & hand item properly and add player ping to nametags
- Option to save/switch configurations
- Implement a good Crystal Aura
- Make auto-anvil and surround and autotrap ignore adjacent block checks
- Make hovering over modules sync the color with the rainbow/colorselector but with 25% opacity (its static blue)
- Fix auto-eat not stopping eating when triggering
- Make elytrafly sync up/down speed with the world your in
- Add alpha slider to HoleESP and make this not highlight per block and highlight entire hole either red or blue instead depending on surround type
- Add background to inventory preview on UI
- Add nearest player hud card like in impact+
- Fix flight not turning off until relog
- Make 2x3 tunnel nuker option
- Fix zoom module messing up if held down
- Implement rendering chunks into stash finder
- Make a check on game crash to disable Xray and Stashfinder (them being enabled during start up causes crash.)
- VisualRange (notifies you whenever someone appears or leaves in your render distance
- add no entity collision and water push to velocity
- make strength esp render over body not under and have toggle to alert in chat when someone drinks strength (it currently spams)
- fix wireframe esp being bad and gross and fucked up
- auto tnt ignite module
- add 2b2t flight mode to elytra
- Add way to slow down swimming speed for 2b2t
- ncp bypass noslow for eating