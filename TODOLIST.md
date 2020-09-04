Modules to add:
1. AutoTrap: <https://github.com/dewysoftwareleaks/Ruhama/blob/master/src/main/java/bleach/a32k/module/modules/ObsidianTrap.java>  
Additions: Need to add toggleable option for netherite and multiple variant trap types and an option for persistent
2. HoleFiller: <https://github.com/dewysoftwareleaks/Ruhama/blob/master/src/main/java/bleach/a32k/module/modules/HoleFiller.java>
3. AutoBreed: <https://raw.githubusercontent.com/seppukudevelopment/seppuku/master/src/main/java/me/rigamortis/seppuku/impl/module/misc/BreedModule.java>  
(Already in the filesystem commented out and partially completed)
4. LiquidInteract: <https://raw.githubusercontent.com/seppukudevelopment/seppuku/master/src/main/java/me/rigamortis/seppuku/impl/module/world/LiquidInteractModule.java>
5. EntityControl: <https://github.com/seppukudevelopment/seppuku/blob/master/src/main/java/me/rigamortis/seppuku/impl/module/movement/EntityControlModule.java>
6. Welcomer: <https://github.com/seppukudevelopment/seppuku/blob/master/src/main/java/me/rigamortis/seppuku/impl/module/misc/GreeterModule.java>  
Additions: Rename from greeter to Welcomer and add file with variables for custom messages
7. MoreInv/XCarry: <https://github.com/seppukudevelopment/seppuku/blob/master/src/main/java/me/rigamortis/seppuku/impl/module/misc/MoreInvModule.java>
8. Coord Logger: <https://raw.githubusercontent.com/seppukudevelopment/seppuku/master/src/main/java/me/rigamortis/seppuku/impl/module/misc/CoordLoggerModule.java>
9. NoBreakAnimations: <https://github.com/seppukudevelopment/seppuku/blob/master/src/main/java/me/rigamortis/seppuku/impl/module/render/NoBreakAnimModule.java>
10. Logout Spots: <https://github.com/seppukudevelopment/seppuku/blob/master/src/main/java/me/rigamortis/seppuku/impl/module/render/LogoutSpotsModule.java>
11. Tablist Color Friends: <https://github.com/zeroeightysix/KAMI/commit/6752db8b83a080b843d5ad3ec3b45f9fed2b8df8>
12. AntiForceLook: <https://github.com/zeroeightysix/KAMI/blob/fabric/src/main/java/me/zeroeightsix/kami/feature/module/player/AntiForceLook.java>
13. AntiFog: <https://github.com/zeroeightysix/KAMI/blob/fabric/src/main/java/me/zeroeightsix/kami/feature/module/render/AntiFog.java>
14. Velocity (bleach's one can be pushed by water and players/entities): <https://github.com/zeroeightysix/KAMI/blob/fabric/src/main/java/me/zeroeightsix/kami/feature/module/movement/Velocity.java>
15. NewChunks (for 2b2t): <https://github.com/zeroeightysix/KAMI/blob/fabric/src/main/java/me/zeroeightsix/kami/feature/module/render/ChunkFinder.java>
16. AutoBuilder: <https://github.com/ionar2/salhack/blob/master/src/main/java/me/ionar/salhack/module/world/AutoBuilderModule.java>
17. EChest Farmer: <https://github.com/ionar2/salhack/blob/master/src/main/java/me/ionar/salhack/module/world/EnderChestFarmer.java>
18. AutoWither: <https://github.com/ionar2/salhack/blob/master/src/main/java/me/ionar/salhack/module/world/AutoWitherModule.java>
19. CityESP: <https://github.com/ionar2/salhack/blob/master/src/main/java/me/ionar/salhack/module/render/CityESPModule.java>
20. AntiHurtcam: <https://github.com/zeroeightysix/KAMI/blob/fabric/src/main/java/me/zeroeightsix/kami/feature/module/render/NoHurtCam.java>
21. CursorBlockHighlight: <https://github.com/fr1kin/ForgeHax/blob/1.16/src/main/java/dev/fiki/forgehax/main/mods/render/BlockHighlightMod.java>

Minor Adjustments:  
22. Make scaffold & surround turn off lock to blocks when nothing to place and surround returns to last item  
23. Add Totem Pop Counter  
24. Add block highlight/search module  
25. Vanilla Speed option  
26. Donkey Detect module  
27. In armor hud add vertical alignment, text mode, and percentage mode  
28. add panel for toggling modules into the popout gui  
29. fix popout gui viafabric compatibility  
30. fix tps counter in popout gui  
31. fix crash when selecting text in the UI hud  
32. improve autoopen on elytras to do via block hight not ticks  
33. liquid remover module  
34. Long jump module  
35. Reach module  
36. Auto cobweb (places cobweb in your block to stop player movement)  
37. Make colors work right in wireframe esp  
38. make drawn ignore syntax and allow you to send .drawn [module] without true/false to toggle true/false while still allowing true/false argument  
39. Chat logger & file logger when detecting cactus placed in the nether  
40. properly center the module description left of the module  
41. map position esp (wireframe box of the chunks a map will render)  
42. Auto Bed Bomb module  
43. Fucker module / auto right click beds in nether/end module and auto charge and right click respawn anchors in overworld/end module and make it pull glowstone from inventory  
44. Fix nametags not rendering armor & hand item properly and add player ping to nametags  
45. Option to save/switch configurations  
46. Implement a good Crystal Aura  
47. Make auto-anvil and surround and autotrap ignore adjacent block checks  
48. Make hovering over modules sync the color with the rainbow/colorselector but with 25% opacity (its currently static blue)  
49. Fix auto-eat not stopping eating when triggering  
50. Make elytrafly sync up/down speed with the world your in  
51. Add alpha slider to HoleESP and make this not highlight per block and highlight entire hole either red or blue instead depending on surround type  
52. Add nearest player hud card like in impact+  
53. Fix flight not turning off until relog  
54. Make 2x3 tunnel nuker option  
55. Fix zoom module messing up if held down  
56. Implement rendering chunks and logging into stash finder  
57. Make a check on game crash to disable Xray and Stashfinder (them being enabled during start up causes crash.)  
58. VisualRange (notifies you in chat whenever someone appears or leaves in your render distance  
59. add no entity collision and water push to velocity  
60. make strength esp render over body not under and have toggle to alert in chat when someone drinks strength (it currently spams)  
61. auto tnt ignite module  
62. Add way to slow down swimming speed for 2b2t  
63. ncp bypass noslow mode for eating  
64. add thing to auto turn off freecam when command #come is ran  
65. finish Logout Spots  
66. Add announcer  
67. Add no render for beacon beams
68. Add health to autoeat
69. fix ambience being scuffed
70. disable freecam block collision