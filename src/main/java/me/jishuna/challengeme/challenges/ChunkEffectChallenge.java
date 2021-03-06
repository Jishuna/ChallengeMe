package me.jishuna.challengeme.challenges;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Chunk;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent.Action;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.google.common.base.Objects;

import me.jishuna.challengeme.api.challenge.Challenge;
import me.jishuna.challengeme.api.challenge.TickingChallenge;
import me.jishuna.challengeme.api.challenge.ToggleChallenge;
import me.jishuna.challengeme.api.player.ChallengePlayer;

public class ChunkEffectChallenge extends Challenge implements TickingChallenge, ToggleChallenge {

	private List<PotionEffectType> effects;
	private final Map<UUID, ChunkEffectCache> effectCache = new HashMap<>();
	private int maxLevel;

	private static final String KEY = "chunk_effects";

	public ChunkEffectChallenge(Plugin owner) {
		super(owner, KEY);

		addEventHandler(EntityPotionEffectEvent.class, this::onEffect);
	}

	@Override
	protected void loadData(YamlConfiguration upgradeConfig) {
		super.loadData(upgradeConfig);

		this.effects = Arrays.asList(PotionEffectType.values()).stream().collect(Collectors.toList());

		this.maxLevel = upgradeConfig.getInt("max-level", 3);

		for (String effect : upgradeConfig.getStringList("blacklisted-effects")) {
			PotionEffectType type = PotionEffectType.getByName(effect.toUpperCase());

			if (type != null) {
				this.effects.remove(type);
			}
		}
	}

	private void onEffect(EntityPotionEffectEvent event, ChallengePlayer challengePlayer) {
		if (event.getAction() == Action.ADDED)
			return;

		ChunkEffectCache oldCache = this.effectCache.get(event.getEntity().getUniqueId());

		if (oldCache == null || oldCache.getEffectType() != event.getModifiedType()
				|| oldCache.getLevel() != event.getOldEffect().getAmplifier())
			return;

		event.setCancelled(true);
	}

	@Override
	public void onTick(ChallengePlayer challengePlayer, Player player) {
		Chunk chunk = player.getLocation().getChunk();

		ChunkEffectCache oldCache = this.effectCache.get(player.getUniqueId());

		if (oldCache == null || oldCache.getX() != chunk.getX() || oldCache.getZ() != chunk.getZ()) {
			Random random = new Random(hashChunk(chunk));

			PotionEffectType newType = effects.get(random.nextInt(effects.size()));
			int level = random.nextInt(this.maxLevel);

			ChunkEffectCache newCache = new ChunkEffectCache(chunk, newType, level);
			this.effectCache.put(player.getUniqueId(), newCache);

			if (oldCache != null) {
				PotionEffect activeEffect = player.getPotionEffect(oldCache.getEffectType());
				if (activeEffect != null && activeEffect.getAmplifier() == oldCache.getLevel())
					player.removePotionEffect(oldCache.getEffectType());
			}
			player.addPotionEffect(new PotionEffect(newType, Integer.MAX_VALUE, level, true));
		}
	}

	private long hashChunk(Chunk chunk) {
		return Objects.hashCode(chunk.getWorld().getSeed(), chunk.getX(), chunk.getZ());
	}

	@Override
	public void onEnable(ChallengePlayer challengePlayer, Player player) {
		// Nothing to set up on enable
	}

	@Override
	public void onDisable(ChallengePlayer challengePlayer, Player player) {
		Chunk chunk = player.getLocation().getChunk();
		Random random = new Random(hashChunk(chunk));

		PotionEffectType type = effects.get(random.nextInt(effects.size()));
		player.removePotionEffect(type);

		effectCache.remove(player.getUniqueId());
	}

	public static class ChunkEffectCache {

		private final int x;
		private final int z;
		private final int level;
		private final PotionEffectType effectType;

		public ChunkEffectCache(Chunk chunk, PotionEffectType effectType, int level) {
			this.x = chunk.getX();
			this.z = chunk.getZ();
			this.level = level;
			this.effectType = effectType;
		}

		public int getX() {
			return x;
		}

		public int getZ() {
			return z;
		}

		public PotionEffectType getEffectType() {
			return effectType;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final ChunkEffectCache other = (ChunkEffectCache) obj;

			return other.getX() == getX() && other.getZ() == getZ();
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(x, z, level, effectType);
		}

		public int getLevel() {
			return level;
		}

	}
}
