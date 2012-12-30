WorkerCommand
=============
A Minecraft Forge mod based on Universal Electricity.

Work-in-progress specification:

## Worker Block
 
*   Receives energy and carries out the given commands
  *   If multiple commands are given, rotates through the ones thathave valid actions
  *   More energy means faster work rate
    *   Up to 5 actions per second
*   Tiers:
  *   Stone Worker: 3x3 area, 3 high, level above the work area MUST be clear
  *   Iron Worker: 5x5 area, 3 high, level above the work area MUST be clear
  *   Diamond Worker: 7x7 area, 5 high, level above the work area MUST be clear 
*   Recipe: Shape to be defined
  *   1x Chest,
  *   1x Circuit,
  *   1x Motor,
  *   1x Tier material,
  *   Plating?
*   GUI:
  *   3x3 storage space for inputs
  *   3x3 storage space for outputs
  *   3x storage space for tools
  *   3x storage space for command circuits 
 
## Basic circuits
 
Planter Circuit
*   Plants saplings, seeds, sugar cane, or cacti 
*   Recipe: Shapeless, 1x Circuit, 1x Seeds
*   Requires: Seeds in the input area, farmland/dirt/sand in the workarea
 
Harvester Circuit
 
*   Removes wheat, potatoes and carrots
*   Recipe: Shapeless, 1x Circuit, 1x Wheat
*   Requires: space in the output area
*   Bonus: Hoe tool increases the speed
 
Woodcutter Circuit
 
*   Removes grown trees, melons and pumpkins
*   Recipe: Shapeless, 1x Circuit, 1x Trunk
*   Requires: Axe in the tool area, space in the output area
*   Bonus: Shears allow it to also collect leaves
 
## Advanced Circuits
 
Fertilizer Circuit
 
*   Applies Bone meal or a suitable substitute to planted blocks
*   Recipe: Shapeless, 1x Circuit, 1x Bone
*   Requires: Bone meal in the input area
 
Tiller Circuit
 
*   Turns dirt into farmland
*   Recipe: Shapeless, 1x Circuit, 1x Dirt
*   Requires: hoe
 
## Elite Circuits
 
Miner Circuit
 
*   Removes blocks from the work area
*   Requires: Pickaxe and/or Shovel
 
Filler Circuit
 
*   Places the given blocks into the work area, rotates through theinput slots when placing, so some patterns are possible

Future Work (maybe)
=============
## Worker Bot
 
*   Crafting material
*   Tiers:
  *   Stone Worker: 3x3 area, 3 high, level above the work area MUST beclear
  *   Iron Worker: 5x5 area, 3 high, level above the work area MUST beclear
  *   Diamond Worker: 7x7 area, 5 high, level above the work area MUSTbe clear 
  *   Recipe: Should require 1x Chest, 1x Circuit, 1x Motor, 1x Tiermaterial, Plating?
 
## Worker Station
 
*   Receives energy and carries out the given commands
  *   If multiple commands are given, rotates through the ones thathave valid actions
  *   More energy means faster work rate
    *   Up to 5 actions per second
*   GUI:
  *   3x3 storage space for inputs
  *   3x3 storage space for outputs
  *   3x storage space for tools
  *   3x storage space for command circuits 
*   Recipe: 1x Chip, 6x Iron, 1x Worker Bot
 
## Short-range Wireless Transceiver
 
*   Communicates a Station with a Command Block
 
## Worker Command
 
*   Controls and Synchronizes up to 6 Worker Stations  