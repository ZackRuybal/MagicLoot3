package me.mrCookieSlime.MagicLoot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.mrCookieSlime.CSCoreLibPlugin.PluginUtils;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.CSCoreLibPlugin.events.ItemUseEvent;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.Item.CustomItem;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.Item.MenuItem;
import me.mrCookieSlime.CSCoreLibSetup.CSCoreLibLoader;
import me.mrCookieSlime.Slimefun.Lists.RecipeType;
import me.mrCookieSlime.Slimefun.Lists.SlimefunItems;
import me.mrCookieSlime.Slimefun.Objects.Category;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunMachine;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.handlers.ItemInteractionHandler;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.Slimefun;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;

public class main extends JavaPlugin {
	
	public static Config config_items;
	public static Config config_names;
	public static Config config_ench;
	public static Config config_potions;
	public static Config config_effects;
	public static Config cfg;
	public static Config tiers;
	public static main instance;
	
	public static List<RuinHandler> handlers = new ArrayList<RuinHandler>();
	
	@Override
	public void onEnable() {
		CSCoreLibLoader loader = new CSCoreLibLoader(this);
		
		if (loader.load()) {
			PluginUtils utils = new PluginUtils(this);
			utils.setupConfig();
			utils.setupMetrics();
			utils.setupUpdater(74010, getFile());
			String path = getDataFolder().getPath();
			config_items = new Config(new File(path+"/Items.yml"));
			config_names = new Config(new File(path+"/Names.yml"));
			config_ench = new Config(new File(path+"/Enchantments.yml"));
			config_potions = new Config(new File(path+"/Potions.yml"));
			config_effects = new Config(new File(path+"/Effects.yml"));
			cfg = new Config(new File(path+"/config.yml"));
			tiers = new Config(new File(path+"/loot_tiers.yml"));
			
			instance = this;
			MagicLoot.setupConfigs();
			
			try {
				RuinBuilder.loadRuins();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			ItemManager.emeraldenchants = Bukkit.getPluginManager().isPluginEnabled("EmeraldEnchants");
			
			if (Bukkit.getPluginManager().isPluginEnabled("Slimefun")) {
				Category category = new Category(new MenuItem(Material.BOOKSHELF, ChatColor.DARK_PURPLE+"MagicLoot", 0, "open"));
				new SlimefunItem(category, new CustomItem(new MaterialData(Material.BOOKSHELF), ChatColor.LIGHT_PURPLE+"Lost Bookshelf", "", ChatColor.RESET+"Scrambled Parts of an", ChatColor.RESET+"ancient Library..."), "LOST_BOOKSHELF", RecipeType.ENHANCED_CRAFTING_TABLE,
				new ItemStack[] {new ItemStack(Material.BOOKSHELF), null, new ItemStack(Material.BOOKSHELF), SlimefunItems.MAGIC_LUMP_3, SlimefunItems.MAGICAL_BOOK_COVER, SlimefunItems.MAGIC_LUMP_3, new ItemStack(Material.BOOKSHELF), null, new ItemStack(Material.BOOKSHELF)}, new CustomItem(new CustomItem(new MaterialData(Material.BOOKSHELF), ChatColor.LIGHT_PURPLE+"Lost Bookshelf", "", ChatColor.RESET+"Scrambled Parts of an", ChatColor.RESET+"ancient Library..."), 2))
				.register();
				
				new SlimefunItem(category, new CustomItem(new MaterialData(Material.WORKBENCH), ChatColor.LIGHT_PURPLE+"Lost Librarian's Desk", "", ChatColor.RESET+"Basically like a Lost Librarian"), "LOST_LIBRARIANS_DESK", RecipeType.ENHANCED_CRAFTING_TABLE,
				new ItemStack[] {SlimefunItem.getItem("LOST_BOOKSHELF"), null, SlimefunItem.getItem("LOST_BOOKSHELF"), null, SlimefunItems.TALISMAN, null, SlimefunItem.getItem("LOST_BOOKSHELF"), null, SlimefunItem.getItem("LOST_BOOKSHELF")})
				.register(new ItemInteractionHandler() {
					
					@Override
					public boolean onRightClick(ItemUseEvent e, Player p, ItemStack stack) {
						if (e.getClickedBlock() == null) return false;
						SlimefunItem item = BlockStorage.check(e.getClickedBlock());
						if (item == null || !item.getName().equals("LOST_LIBRARIANS_DESK")) return false;
						try {
							e.setCancelled(true);
							LostLibrarian.openMenu(p);
						} catch (Exception e1) {
							e1.printStackTrace();
						}
						return true;
					}
				});
			}
			
			getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				@Override
				public void run() {
					if (Bukkit.getPluginManager().isPluginEnabled("Slimefun")) {
						for (String item: Slimefun.listIDs()) {
							if (!(SlimefunItem.getByName(item) instanceof SlimefunMachine)) MagicLoot.getConfig(ConfigType.ITEMS).setDefaultValue("Slimefun-Item." + item, true);
						}
						main.instance.getLogger().info("Slimefun has been found!");
						main.instance.getLogger().info("I will now generate Slimefun Loot as well!");
					}
					MagicLoot.loadSettings();
					
					new MLListener();
				}
			}, 10);
		}
	}
	
	@Override
	public void onDisable() {
		cfg = null;
		config_effects = null;
		config_ench = null;
		config_items = null;
		config_names = null;
		config_potions = null;
		handlers = null;
		
		ItemManager.COLOR = null;
		ItemManager.ENCHANTMENTS = null;
		ItemManager.EFFECTS = null;
		ItemManager.potion = null;
		ItemManager.POTIONEFFECTS = null;
		ItemManager.PREFIX = null;
		ItemManager.SLIMEFUN = null;
		ItemManager.SUFFIX = null;
		ItemManager.TOOLS = null;
		ItemManager.TREASURE = null;
		ItemManager.types = null;
		
		MagicLoot.colors = null;
		MagicLoot.effects = null;
		MagicLoot.prefixes = null;
		MagicLoot.suffixes = null;
		MagicLoot.mobs = null;
		
		RuinBuilder.schematics = null;
	}

}
