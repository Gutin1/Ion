package net.horizonsend.ion.server.features.progression.achievements

import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.server.LegacySettings
import net.horizonsend.ion.server.features.custom.items.CustomItems.CHETHERITE
import net.horizonsend.ion.server.features.progression.SLXP
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.get
import net.horizonsend.ion.server.miscellaneous.utils.vaultEconomy
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.litote.kmongo.addToSet

enum class Achievement(
	val title: String?, //nullable for multi-condition advancements
	val description: String?, //nullable for multi-condition advancements
	val creditReward: Int,
	val experienceReward: Int,
	val chetheriteReward: Int,
	val key: String,
	val criteria: String,
	val resultAdvancement: Boolean // true if it's a multi-criterion advancement, see AdvancementListeners.kt
) {
	/**
	 Process for making a new advancement:

 	 ----- if single-criterion: -----	example file: pilot_ship.json
		 * 1. create a new advancement's {name}.json file in the horizons end datapack......pilot_ship.json
		 * 2. create new enum in this file..................................................PILOT_SHIP(...)
		 * key = folder/json_file_name......................................................"exploration/pilot_ship"
		 * criteria = good practice to match to file name..................................."pilot_ship"
		 * 3. set criteria to "minecraft:impossible"........................................"pilot_ship": { "minecraft:impossible" }
	 	 * 4. grant advancement in its respective area within the code......................Achievements.PILOT_SHIP.rewardAdvancement(player)

	 ----- if multi-criterion: -----	example file: visit_all_systems.json
		 * 1. the advancement's .json holds all criteria....................................visit_all_systems.json
		 * 2. each criterion has its own associated Achievement enum........................visit_asteri -> VISIT_ASTERI(...)
	 	 * 3. repeat step 4 for all Achievements............................................Achievements.VISIT_ASTERI.rewardAdvancement(player)
		 * 4. one extra Achievement enum is made to display when all criteria are met,......VISIT_ALL_SYSTEMS(...)
			  its listener goes in AchievementListeners.kt,.................................onPlayerAdvancementDoneEvent(...)
			  and resultAdvancement is set to TRUE..........................................VISIT_ALL_SYSTEMS(...,true)

	 ===== single criteria format =====
	 "criteria":
	 {
	 	"pilot_ship":    <----- criteria
		 {
		 	"trigger": "minecraft:impossible"
		 }
	 }

	 ===== multiple criteria format =====
	 "criteria":
	 {
		 "visit_asteri":   				<----- criteria
		 {
		 	"trigger": "minecraft:impossible"
		 },
		 "visit_sirius":   				<----- criteria
		 {
		 	"trigger": "minecraft:impossible"
		 },
		 "visit_regulus": 				<----- criteria
		 {
		 	"trigger": "minecraft:impossible"
		 }
	 }
	 */

//	Template:
//	TEMPLATE("title", "description", 0, 0, 0, "key", "criteria", false),
	/** -------------------- Exploration -------------------- **/
	EXPLORATION_ROOT(null, null, 0, 0, 0, "exploration/root", "root", false), // s1ice
//	COMPLETE_TUTORIAL("Space Cadet", "Complete the Tutorial", 1000, 250, 0, "exploration/complete_tutorial", "complete_tutorial", false), // Wither
	BUY_SHIP("Fresh Off the Lot", "Buy a ship from the ship dealer", 250, 100, 0, "exploration/buy_ship", "buy_ship", false), // Sciath + Astral
	DETECT_SHIP("All in Working Order", "Detect a starship", 100, 50, 0, "exploration/detect_ship", "detect_ship", false), // Vandrayk
	PILOT_SHIP("When Players Fly", "Pilot a starship", 100, 50, 0, "exploration/pilot_ship", "pilot_ship", false), // s1ice
	SINK_SHIP("Tango Down", "Shoot down a ship", 1000, 250, 0, "exploration/sink_ship", "sink_ship", false), // Vandrayk
	GET_SUNK("This is Horizon's End", "Get sunk", 500, 100, 0, "exploration/get_sunk", "get_sunk", false), // s1ice
	ENTER_SPACE("Outer Space!", "Enter Space", 250, 100, 0, "exploration/enter_space", "enter_space", false), // s1ice
	CREATE_FLEET("Squad Up", "Create a Fleet", 100, 100, 0, "exploration/create_fleet", "create_fleet", false), // s1ice
	ENTER_HYPERSPACE("Ludicrous Speed!", "Use hyperspace", 250, 75, 8, "exploration/enter_hyperspace", "enter_hyperspace", false), // Liluzivert
//	COMPLETE_COMMISSION("Easy Money", "Complete a commission", 250, 100, 0, "exploration/complete_commission", "complete_commission", false), // s1ice
//	THREE_DAY_STREAK("On a Roll", "Have a 3-day commission streak", 500, 250, 0, "exploration/3_day_streak", "3_day_streak", false), // s1ice
//	MAX_COMMISSION_STREAK("Starstruck", "Max out your commission streak bonus", 1000, 500, 0, "exploration/max_commission_streak", "max_commission_streak", false), // s1ice
	BUY_CRATE("Order Up!", "Buy crates from an Exporter", 500, 125, 0, "exploration/buy_crate", "buy_crate", false), // s1ice
	SELL_CRATE("Space Trucking", "Sell crates to an Importer", 500, 125, 0, "exploration/sell_crate", "sell_crate", false), // Astral

	PILOT_SPEEDER("Beats Walkin'", "Pilot a Speeder", 100, 100, 0, "exploration/pilot_speeder", "pilot_speeder", false), // s1ice
	PILOT_SHUTTLE("Baby Steps", "Pilot a Shuttle", 100, 100, 0, "exploration/pilot_shuttle", "pilot_shuttle", false), // s1ice
	PILOT_TRANSPORT("Moving Forward", "Pilot a Transport", 100, 100, 0, "exploration/pilot_transport", "pilot_transport", false), // s1ice
	PILOT_LIGHT_FREIGHTER("Freightful!", "Pilot a Light Freighter", 100, 100, 0, "exploration/pilot_light_freighter", "pilot_light_freighter", false), // s1ice
	PILOT_MEDIUM_FREIGHTER("Middle Class Lifestyle", "Pilot a Medium Freighter", 100, 100, 0, "exploration/pilot_medium_freighter", "pilot_medium_freighter", false), // s1ice
	PILOT_HEAVY_FREIGHTER("The Bigger the Better", "Pilot a Heavy Freighter", 100, 100, 0, "exploration/pilot_heavy_freighter", "pilot_heavy_freighter", false), // s1ice
	PILOT_BARGE("Corporate Greed", "Pilot a Barge", 100, 100, 0, "exploration/pilot_barge", "pilot_barge", false), // s1ice
	PILOT_STARFIGHTER("Small but Powerful", "Pilot a Starfighter", 100, 100, 0, "exploration/pilot_starfighter", "pilot_starfighter", false), // s1ice
	PILOT_GUNSHIP("Bringing a Gun to a Dagger Fight", "Pilot a Gunship", 100, 100, 0, "exploration/pilot_gunship", "pilot_gunship", false), // s1ice
	PILOT_CORVETTE("The Inbetweener", "Pilot a Corvette", 100, 100, 0, "exploration/pilot_corvette", "pilot_corvette", false), // s1ice
	PILOT_FRIGATE("With Great Power...", "Pilot a Frigate", 100, 100, 0, "exploration/pilot_frigate", "pilot_frigate", false), // s1ice
	PILOT_DESTROYER("...Comes Great Destruction", "Pilot a Destroyer", 100, 100, 0, "exploration/pilot_destroyer", "pilot_destroyer", false), // s1ice
	PILOT_CRUISER("Cruisin' For A Bruisin'", "Pilot a Cruiser", 100, 100, 0, "exploration/pilot_cruiser", "pilot_cruiser", false), // s1ice
	PILOT_BATTLECRUISER("Power of the Sun", "Pilot a Battlecruiser", 100, 100, 0, "exploration/pilot_battlecruiser", "pilot_battlecruiser", false), // s1ice

	ADD_PILOT("Take the Keys", "Add a player as a pilot on a starship", 100, 100, 0, "exploration/add_pilot", "add_pilot", false), // s1ice
	ADD_PILOT_BATTLECRUISER("Don't Scratch the Paint", "Add a player as a pilot on a Battlecruiser", 100, 100, 0, "exploration/add_pilot_battlecruiser", "add_pilot_battlecruiser", false), // s1ice
	BUY_BAZAAR("Ooh, Shiny!", "Buy something from a bazaar", 0, 0, 0, "exploration/buy_bazaar", "buy_bazaar", false), // s1ice
	REMOTE_BUY_BAZAAR("Same-Day Delivery", "Remotely buy something from a Bazaar", 100, 100, 0, "exploration/remote_buy_bazaar", "remote_buy_bazaar", false), // s1ice
	SELL_BAZAAR("Community Market", "Put up an item up on the bazaar", 0, 0, 0, "exploration/sell_bazaar", "sell_bazaar", false), // s1ice
	MILLION_BAZAAR_PROFIT("Capitalist", "Profit 1 million credits from bazaar listings", 1000, 500, 0, "exploration/1_mil_bazaar_profit", "1_mil_bazaar_profit", false), // s1ice
	KILL_PLAYER("Carried Away", "Kill a player", 250, 100, 0, "exploration/kill_player", "kill_player", false), // Astral
	KILL_CAPTAIN("Mutiny", "Kill a player piloting a ship, as their passenger", 500, 250, 0, "exploration/kill_captain", "kill_captain", false), // s1ice
	KILL_SETTLEMENT_LEADER("Caesar", "Kill a settlement leader, in their settlement", 500, 250, 0, "exploration/kill_settlement_leader", "kill_settlement_leader", false), // s1ice
	OBTAIN_PLAYER_HEAD("Beheaded!", "Obtain a Player Head", 250, 100, 0, "exploration/obtain_player_head", "obtain_player_head", false), // s1ice
	SINK_AI_SHIP("Enemy Down!", "Sink an AI ship", 250, 100, 0, "exploration/sink_ai", "sink_ai", false), // s1ice
	SINK_ALIEN_SHIP("Next-Level Threat", "Sink an Alien AI ship", 500, 250, 0, "exploration/sink_alien_ship", "sink_alien_ship", false), // s1ice
	SINK_1K_AI_SHIPS("Terminator", "Kill 1000 AI ships", 10000, 5000, 0, "exploration/kill_1k_ai_ships", "kill_1k_ai_ships", false), // s1ice

	/** ---------------------------------------- EXPLORATION ---------------------------------------- **/
	LEVELING_ROOT(null, null, 0, 0, 0, "leveling/root", "root", false), // s1ice
	LEVEL_10("What do we do now?", "Reach level 10", 1000, 250, 0, "leveling/level_10", "level_10", false), // Vandrayk
	LEVEL_20("Where it begins", "Reach level 20", 2000, 350, 0, "leveling/level_20", "level_20", false), // Kwazedilla + Astral
	LEVEL_40("Sorry for the pain", "Reach level 40", 4000, 500, 0, "leveling/level_40", "level_40", false), // Sciath
	LEVEL_60("The Grind Never Ends","Reach level 60", 6000, 750, 0, "leveling/level_60", "level_60", false), // s1ice
	LEVEL_80("Overwhelming power", "Reach level 80", 8000, 1000, 0, "leveling/level_80", "level_80", false), // Astral
	LEVEL_100("Triple Digits!", "Reach level 100", 10000, 0, 0, "leveling/level_100", "level_100", false), // s1ice
	BAL_10K("Pocket Change", "Have a 10k balance", 0, 100, 0, "leveling/balance_10k", "balance_10k", false), // s1ice
	BAL_50K("Getting Your Bearings", "Have a 50k balance", 0, 500, 0, "leveling/balance_50k", "balance_50k", false),
	BAL_100K("The Grind Has Only Begun", "Have a 100k balance", 0, 1000, 0, "leveling/balance_100k", "balance_100k", false), // s1ice
	BAL_500K("Money, Money, Money", "Have a 500k balance", 0, 2000, 0, "leveling/balance_500k", "balance_500k", false), // s1ice
	BAL_1M("Millionaire", "Have a 1 mil balance", 0, 5000, 0, "leveling/balance_1mil", "balance_1mil", false), // s1ice
	BAL_10M("Monopoly", "Have a 10 mil balance", 0, 7500, 0, "leveling/balance_10mil", "balance_10mil", false), // s1ice

	/** ---------------------------------------- MATERIALS ---------------------------------------- **/
	MATERIALS_ROOT(null, null, 0, 0, 0, "materials/root", "root", false), // s1ice
	OBTAIN_CHETHERITE("Unleaded", "Obtain Chetherite", 0, 0, 0, "materials/obtain_chetherite","obtain_chetherite", false), // Gutin
	OBTAIN_URANIUM("Split the atom", "Obtain Uranium", 0, 0, 0, "materials/obtain_titanium","obtain_aluminum", false), // Astral
	OBTAIN_ALUMINUM_INGOT("Pronounced Aluminum", "Obtain Aluminum", 0, 0, 0, "materials/obtain_aluminum","obtain_aluminum", false), // Gutin
	OBTAIN_TITANIUM_INGOT("Future's Material", "Obtain Titanium", 0, 0, 0, "materials/obtain_titanium","obtain_titanium", false), // Gutin + Astral

	//circuitry line
	OBTAIN_CIRCUITRY(			null, null, 0, 0, 0, "materials/c_obtain_circuitry",			"c_obtain_circuitry",			false), // s1ice
	OBTAIN_CIRCUIT_BOARD(		null, null, 0, 0, 0, "materials/c_obtain_circuit_board",		"c_obtain_circuit_board",		false), // s1ice
	OBTAIN_MOTHERBOARD(			null, null, 0, 0, 0, "materials/c_obtain_motherboard",			"c_obtain_motherboard",			false), // s1ice
	OBTAIN_SUPERCONDUCTOR(		null, null, 0, 0, 0, "materials/c_obtain_superconductor",		"c_obtain_superconductor",		false), // s1ice
	OBTAIN_SUPERCONDUCTOR_BLOCK(null, null, 0, 0, 0, "materials/c_obtain_superconductor_block",	"c_obtain_superconductor_block",false), // s1ice
	OBTAIN_SUPERCONDUCTOR_CORE(	null, null, 0, 0, 0, "materials/c_obtain_superconductor_core",	"c_obtain_superconductor_core",	false), // s1ice

	//reactive line
	OBTAIN_REACTIVE_HOUSING(   null, null, 0, 0, 0, "materials/r_obtain_reactive_housing",	 "r_obtain_reactive_housing", 	false), // s1ice
	OBTAIN_REACTIVE_COMPONENT( null, null, 0, 0, 0, "materials/r_obtain_reactive_component", "r_obtain_reactive_component", false), // s1ice
	OBTAIN_REACTIVE_PLATING(   null, null, 0, 0, 0, "materials/r_obtain_reactive_plating",	 "r_obtain_reactive_plating", 	false), // s1ice
	OBTAIN_REACTIVE_CHASSIS(   null, null, 0, 0, 0, "materials/r_obtain_reactive_chassis",	 "r_obtain_reactive_chassis", 	false), // s1ice
	OBTAIN_REACTIVE_MEMBRANE(  null, null, 0, 0, 0, "materials/r_obtain_reactive_membrane",	 "r_obtain_reactive_membrane", 	false), // s1ice
	OBTAIN_REACTIVE_ASSEMBLY(  null, null, 0, 0, 0, "materials/r_obtain_reactive_assembly",	 "r_obtain_reactive_assembly",	false), // s1ice
	OBTAIN_FABRICATED_ASSEMBLY(null, null, 0, 0, 0, "materials/r_obtain_fabricated_assembly","r_obtain_fabricated_assembly",false), // s1ice
	OBTAIN_REACTOR_CONTROL(    null, null, 0, 0, 0, "materials/r_obtain_reactor_control",	 "r_obtain_reactor_control", 	false), // s1ice

	//steel line
	OBTAIN_STEEL_INGOT(		null, null, 0, 0, 0, "materials/s_obtain_steel",			"s_obtain_steel", 				false), // s1ice
	OBTAIN_STEEL_BLOCK(		null, null, 0, 0, 0, "materials/s_obtain_steel_block",		"s_obtain_steel_block", 		false), // s1ice
	OBTAIN_STEEL_PLATE(		null, null, 0, 0, 0, "materials/s_obtain_steel_plate",		"s_obtain_steel_plate", 		false), // s1ice
	OBTAIN_STEEL_CHASSIS(	null, null, 0, 0, 0, "materials/s_obtain_steel_chassis",	"s_obtain_steel_chassis", 		false), // s1ice
	OBTAIN_STEEL_MODULE(	null, null, 0, 0, 0, "materials/s_obtain_steel_module",		"s_obtain_steel_module", 		false), // s1ice
	OBTAIN_STEEL_ASSEMBLY(	null, null, 0, 0, 0, "materials/s_obtain_steel_assembly",	"s_obtain_steel_assembly", 		false), // s1ice
	OBTAIN_REINFORCED_FRAME(null, null, 0, 0, 0, "materials/s_obtain_reinforced_frame", "s_obtain_reinforced_frame", 	false), // s1ice
	OBTAIN_REACTOR_FRAME(	null, null, 0, 0, 0, "materials/s_obtain_reactor_frame",	"s_obtain_reactor_frame", 		false), // s1ice

	//uranium line
	OBTAIN_ENRICHED_URANIUM(	  null, null, 0, 0, 0, "materials/u_obtain_enriched_uranium",		"u_obtain_enriched_uranium", 		false), // s1ice
	OBTAIN_ENRICHED_URANIUM_BLOCK(null, null, 0, 0, 0, "materials/u_obtain_enriched_uranium_block", "u_obtain_enriched_uranium_block",	false), // s1ice
	OBTAIN_URANIUM_CORE(		  null, null, 0, 0, 0, "materials/u_obtain_uranium_core",			"u_obtain_uranium_core", 			false), // s1ice
	OBTAIN_URANIUM_ROD(			  null, null, 0, 0, 0, "materials/u_obtain_uranium_ro",				"u_obtain_uranium_rod", 			false), // s1ice
	OBTAIN_FUEL_ROD_CORE(		  null, null, 0, 0, 0, "materials/u_obtain_fuel_rod_core",			"u_obtain_fuel_rod_core", 			false), // s1ice
	OBTAIN_FUEL_CELL(			  null, null, 0, 0, 0, "materials/u_obtain_fuel_cell",				"u_obtain_fuel_cell", 				false), // s1ice
	OBTAIN_FUEL_CONTROL(		  null, null, 0, 0, 0, "materials/u_obtain_fuel_control",			"u_obtain_fuel_control", 			false), // s1ice

	/** ---------------------------------------- TECHNOLOGY ---------------------------------------- **/
	TECHNOLOGY_ROOT(null, null, 0, 0, 0, "technology/root", "root", false), // s1ice
	DETECT_MULTIBLOCK("Industrial Revolution", "Detect a multiblock", 500, 125, 0, "technology/detect_multiblock", "detect_multiblock", false), // Wither + Astral
	USE_ROCKET_BOOTS("Rocket Man", "Take flight with rocket boots", 100, 100, 0, "technology/use_rocket_boots", "use_rocket_boots", false), // s1ice
	OBTAIN_SPONGE("Wiring Time!", "Obtain a Sponge", 0, 0, 0, "technology/obtain_sponge","obtain_sponge", false), // s1ice
	OBTAIN_END_ROD("Straight Pathing", "Obtain an End Rod", 0, 0, 0, "technology/obtain_end_rod","obtain_end_rod", false), // s1ice

	/** ---------------------------------------- NATIONS ---------------------------------------- **/
	NATIONS_ROOT(null, null, 0, 0, 0, "nations/root", "root", false), // s1ice
	JOIN_SETTLEMENT("Forming Bonds", "Join a settlement", 100, 100, 0, "nations/join_settlement", "join_settlement", false), // s1ice
	JOIN_NATION("Strength in Numbers", "Join a Nation", 250, 100, 0, "nations/join_nation", "join_nation", false), // s1ice
	SETTLEMENT_MOTD("Community Notice Board", "Set your settlement MOTD", 100, 100, 0, "nations/settlement_motd", "settlement_motd", false), // s1ice

	RELATION_ALLY("Brothers in Arms", "Set a relation to Ally", 100, 100, 0, "nations/relation_ally", "relation_ally", false), // s1ice
	RELATION_FRIENDLY("A Friendly Gesture", "Set a relation to Friendly", 100, 100, 0, "nations/relation_friendly", "relation_friendly", false), // s1ice
	RELATION_NEUTRAL("Switzerland", "Set a relation to Neutral", 100, 100, 0, "nations/relation_neutral", "relation_neutral", false), // s1ice
	RELATION_UNFRIENDLY("Walking on Eggshells", "Set a relation to Unfriendly", 100, 100, 0, "nations/relation_unfriendly", "relation_unfriendly", false), // s1ice
	RELATION_ENEMY("WAR!", "Set a relation to Enemy", 100, 100, 0, "nations/relation_enemy", "relation_enemy", false), // s1ice
	RELATION_NATION("Nation in our Time", "Set a relation to Nation", 100, 100, 0, "nations/relation_nation", "relation_nation", false), // s1ice

	SIEGE_STATION("Poking the bear", "Participate in a Siege", 500, 125, 0, "nations/siege_station", "siege_station", false), // Gutin
	CAPTURE_STATION("Bear eliminated", "Capture a Siege Station", 5000, 750, 0, "nations/capture_station", "capture_station", false), // Liluzivirt
	CREATE_SETTLEMENT("Breaking ground", "Found a settlement", 1000, 250, 0, "nations/create_settlement", "create_settlement", false), // Astral
	CREATE_NATION("Galactic Power", "Found a nation", 5000, 500, 0, "nations/create_nation", "create_nation", false), // Vandrayk
	CREATE_OUTPOST("Manifest Destiny", "Create a nation claim", 2500, 250, 0, "nations/create_settlement", "create_settlement", false), // Vandrayk
	CREATE_STATION(null, null, 0, 0, 0, "nations/create_station", "create_station", false), // s1ice
	CREATE_PERSONAL_STATION("Home Away From Home", "Create a Personal Space Station", 500, 250, 0, "nations/create_personal_station", "create_personal_station", false), // s1ice
	CREATE_SETTLEMENT_STATION("Zero-G Community", "Create a Settlement Space Station", 500, 250, 0, "nations/create_settlement_station", "create_settlement_station", false), // s1ice
	CREATE_NATION_STATION("Asteroids for All!", "Create a Nation Space Station", 500, 250, 0, "nations/create_nation_station", "create_nation_station", false), // s1ice


	/** ---------------------------------------- ANY-CRITERION ADVANCEMENTS ---------------------------------------- **/

	OBTAIN_POWER_DRILL_BASIC("Tools of the Future", "Obtain a Basic Power Tool", 250, 100, 0, "technology/obtain_basic_power_tool","obtain_basic_power_tool", false), // s1ice
	OBTAIN_POWER_CHAINSAW_BASIC("Tools of the Future", "Obtain a Basic Power Tool", 250, 100, 0, "technology/obtain_basic_power_tool","obtain_basic_power_tool", false), // s1ice
	OBTAIN_POWER_HOE_BASIC("Tools of the Future", "Obtain a Basic Power Tool", 250, 100, 0, "technology/obtain_basic_power_tool","obtain_basic_power_tool", false), // s1ice

	OBTAIN_POWER_DRILL_ENHANCED("Tools of the Future", "Obtain a Enhanced Power Tool", 500, 250, 0, "technology/obtain_enhanced_power_tool","obtain_enhanced_power_tool", false), // s1ice
	OBTAIN_POWER_CHAINSAW_ENHANCED("Tools of the Future", "Obtain a Enhanced Power Tool", 500, 250, 0, "technology/obtain_enhanced_power_tool","obtain_enhanced_power_tool", false), // s1ice
	OBTAIN_POWER_HOE_ENHANCED("Tools of the Future", "Obtain a Enhanced Power Tool", 500, 250, 0, "technology/obtain_enhanced_power_tool","obtain_enhanced_power_tool", false), // s1ice

	OBTAIN_POWER_DRILL_ADVANCED("Tools of the Future", "Obtain a Advanced Power Tool", 750, 500, 0, "technology/obtain_advanced_power_tool","obtain_advanced_power_tool", false), // s1ice
	OBTAIN_POWER_CHAINSAW_ADVANCED("Tools of the Future", "Obtain a Advanced Power Tool", 750, 500, 0, "technology/obtain_advanced_power_tool","obtain_advanced_power_tool", false), // s1ice
	OBTAIN_POWER_HOE_ADVANCED("Tools of the Future", "Obtain a Advanced Power Tool", 750, 500, 0, "technology/obtain_advanced_power_tool","obtain_advanced_power_tool", false), // s1ice

	/** ---------------------------------------- MULTI-CRITERION ADVANCEMENTS ---------------------------------------- **/

	OBTAIN_ALL_CORES("Warship Cores!?", "Obtain all 3 Supercapital Cores", 5000, 1000, 0, "", "", true), // s1ice
	OBTAIN_BATTLECRUISER_REACTOR_CORE(null, null, 500, 750, 0, "exploration/obtain_all_cores", "obtain_battlecruiser_core", false), // s1ice
	OBTAIN_CRUISER_REACTOR_CORE(	  null, null, 500, 750, 0, "exploration/obtain_all_cores", "obtain_cruiser_core", 		false), // s1ice
	OBTAIN_BARGE_REACTOR_CORE(		  null, null, 500, 750, 0, "exploration/obtain_all_cores", "obtain_barge_core", 		false), // s1ice

	OBTAIN_ALL_POWER_ARMOR("Honey, Where's My Super-suit?", "Obtain all 4 pieces of Power Armor", 500, 250, 0, "", "", 	true), // s1ice
	OBTAIN_POWER_ARMOR_HELMET(		null, null, 0, 0, 0, "technology/obtain_all_power_armor", "obtain_power_helmet", 	false), // s1ice
	OBTAIN_POWER_ARMOR_CHESTPLATE(	null, null, 0, 0, 0, "technology/obtain_all_power_armor", "obtain_power_chestplate",false), // s1ice
	OBTAIN_POWER_ARMOR_LEGGINGS(	null, null, 0, 0, 0, "technology/obtain_all_power_armor", "obtain_power_leggings", 	false), // s1ice
	OBTAIN_POWER_ARMOR_BOOTS(		null, null, 0, 0, 0, "technology/obtain_all_power_armor", "obtain_power_boots", 	false), // s1ice
/* soon(tm)
	SINK_EACH_AI_SHIP("Man Ms. Machine", "Sink at least one AI ship from every faction", 5000, 2500, 0, "", "", true), // s1ice
	SINK_吃饭人(					null, null, 0, 0, 0, "exploration/sink_each_ai_ship", "sink_吃饭人", 				false), // s1ice
	SINK_WATCHERS(				null, null, 0, 0, 0, "exploration/sink_each_ai_ship", "sink_watchers", 				false), // s1ice
	SINK_MINING_GUILD(			null, null, 0, 0, 0, "exploration/sink_each_ai_ship", "sink_mining_guild", 			false), // s1ice
	SINK_PERSEUS_EXPLORERS(		null, null, 0, 0, 0, "exploration/sink_each_ai_ship", "sink_perseus_explorers", 	false), // s1ice
	SINK_SYSTEM_DEFENSE_FORCES( null, null, 0, 0, 0, "exploration/sink_each_ai_ship", "sink_system_defense_forces", false), // s1ice
	SINK_TSAII_RAIDERS(			null, null, 0, 0, 0, "exploration/sink_each_ai_ship", "sink_tsaii_raiders", 		false), // s1ice
	SINK_PIRATES(				null, null, 0, 0, 0, "exploration/sink_each_ai_ship", "sink_pirates", 				false), // s1ice
*/
	VISIT_ALL_PLANETS("Mr. Worldwide", "Visit Every Planet", 7500, 2500, 0, "", "", true), // s1ice
	VISIT_AERACH(	null, null, 100, 125, 0, "exploration/visit_all_planets", "visit_aerach", 	false), // s1ice
	VISIT_ARET(		null, null, 100, 125, 0, "exploration/visit_all_planets", "visit_aret", 	false), // s1ice
	VISIT_CHANDRA(	null, null, 100, 125, 0, "exploration/visit_all_planets", "visit_chandra", 	false), // s1ice
	VISIT_CHIMGARA(	null, null, 100, 125, 0, "exploration/visit_all_planets", "visit_chimgara", false), // s1ice
	VISIT_DAMKOTH(	null, null, 100, 125, 0, "exploration/visit_all_planets", "visit_damkoth", 	false), // s1ice
	VISIT_GAHARA(	null, null, 100, 125, 0, "exploration/visit_all_planets", "visit_gahara", 	false), // s1ice
	VISIT_HERDOLI(	null, null, 100, 125, 0, "exploration/visit_all_planets", "visit_herdoli", 	false), // s1ice
	VISIT_ILIUS(	null, null, 100, 125, 0, "exploration/visit_all_planets", "visit_ilius", 	false), // s1ice
	VISIT_ISIK(		null, null, 100, 125, 0, "exploration/visit_all_planets", "visit_isik", 	false), // s1ice
	VISIT_KOVFEFE(	null, null, 100, 125, 0, "exploration/visit_all_planets", "visit_kovfefe", 	false), // s1ice
	VISIT_KRIO(		null, null, 100, 125, 0, "exploration/visit_all_planets", "visit_krio", 	false), // s1ice
	VISIT_LIODA(	null, null, 100, 125, 0, "exploration/visit_all_planets", "visit_lioda", 	false), // s1ice
	VISIT_LUXITERNA(null, null, 100, 125, 0, "exploration/visit_all_planets", "visit_luxiterna",false), // s1ice
	VISIT_QATRA(	null, null, 100, 125, 0, "exploration/visit_all_planets", "visit_qatra", 	false), // s1ice
	VISIT_RUBACIEA(	null, null, 100, 125, 0, "exploration/visit_all_planets", "visit_rubaciea", false), // s1ice
	VISIT_TURMS(	null, null, 100, 125, 0, "exploration/visit_all_planets", "visit_turms", 	false), // s1ice
	VISIT_VASK(		null, null, 100, 125, 0, "exploration/visit_all_planets", "visit_vask", 	false), // s1ice

	VISIT_ALL_SYSTEMS("Galactic Explorer", "Visit every system", 2500, 1000, 0, "", "", true), // s1ice
 	VISIT_ASTERI( null, null, 200, 250, 0, "exploration/visit_all_systems", "visit_asteri",  false), // s1ice
	VISIT_ILIOS(  null, null, 200, 250, 0, "exploration/visit_all_systems", "visit_ilios",   false), // s1ice
	VISIT_REGULUS(null, null, 200, 250, 0, "exploration/visit_all_systems", "visit_regulus", false), // s1ice
	VISIT_SIRIUS( null, null, 200, 250, 0, "exploration/visit_all_systems", "visit_sirius",  false), // s1ice
	VISIT_HORIZON(null, null, 200, 250, 0, "exploration/visit_all_systems", "visit_horizon", false), // s1ice
	VISIT_TRENCH( null, null, 200, 250, 0, "exploration/visit_all_systems", "visit_trench",  false), // s1ice
	VISIT_AU_0821(null, null, 200, 400, 0, "exploration/visit_all_systems", "visit_au_0821", false); // s1ice

	/** ---------------------------------------- END OF ADVANCEMENT LIST ---------------------------------------- **/

	fun rewardAdvancement(player: Player) = Tasks.async {
		if (!LegacySettings.master) return@async

		val playerData = SLPlayer[player]
		if (playerData.achievements.map { Achievement.valueOf(it) }.find { it == this } != null) return@async

		if(!resultAdvancement) {
			val advancement = Bukkit.getAdvancement(NamespacedKey("horizonsend", key))

			if(advancement?.let { player.getAdvancementProgress(it).isDone } == false) // if advancement's not done
				Tasks.sync {player.getAdvancementProgress(advancement).awardCriteria(criteria)}
			else return@async

		}

		SLPlayer.updateById(playerData._id, addToSet(SLPlayer::achievements, name))

		vaultEconomy?.depositPlayer(player, creditReward.toDouble())

		SLXP.addAsync(player, experienceReward, false)

		if (chetheriteReward > 0) {
			player.inventory.addItem(CHETHERITE.constructItemStack().asQuantity(chetheriteReward))
		}
		val message: String =
			(
				(if (title != null)
					"\n<gold>${title}\n" else "")
				+ (if(description != null)
					"<gray>Achievement Granted: ${description}<reset>\n" else "")
				+ (if (creditReward > 0)
					"Credits: $creditReward\n" else "")
				+ (if (experienceReward > 0)
					"Experience: $experienceReward\n" else "")
				+ if (chetheriteReward > 0)
					"Chetherite: $chetheriteReward\n" else ""
			)

		if(message != "") player.sendRichMessage(message)
	}
}
