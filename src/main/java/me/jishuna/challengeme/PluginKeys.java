package me.jishuna.challengeme;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

public enum PluginKeys {
	CHALLENGE_KEY("challenge_key"), CATEGORY_KEY("categoery_key");

	private final String name;
	private NamespacedKey key;

	private PluginKeys(String name) {
		this.name = name;
	}

	public static void initialize(Plugin plugin) {
		for (PluginKeys plguinKey : PluginKeys.values()) {
			plguinKey.key = new NamespacedKey(plugin, plguinKey.name);
		}
	}

	public NamespacedKey getKey() {
		return this.key;
	}

}
