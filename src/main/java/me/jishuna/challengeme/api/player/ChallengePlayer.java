package me.jishuna.challengeme.api.player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;

import me.jishuna.challengeme.api.challenge.Challenge;
import me.jishuna.challengeme.api.challenge.ToggleChallenge;

public class ChallengePlayer {

	private final UUID id;
	private final Set<Challenge> activeChallenges = new HashSet<>();
	private final Map<String, Long> cooldowns = new HashMap<>();

	public ChallengePlayer(Player player) {
		this(player.getUniqueId());
	}

	public ChallengePlayer(UUID uuid) {
		this.id = uuid;
	}

	public UUID getId() {
		return id;
	}

	public Set<Challenge> getActiveChallenges() {
		return activeChallenges;
	}

	public void addChallenge(Challenge challenge) {
		boolean add = this.activeChallenges.add(challenge);

		if (add) {
			if (challenge instanceof ToggleChallenge) {
				((ToggleChallenge) challenge).onEnable(Bukkit.getPlayer(this.id));
			}
		}
	}

	public boolean removeChallenge(Challenge challenge) {
		boolean remove = this.activeChallenges.remove(challenge);

		if (remove) {
			if (challenge instanceof ToggleChallenge) {
				((ToggleChallenge) challenge).onDisable(Bukkit.getPlayer(this.id));
			}
		}
		return remove;
	}

	public boolean hasChallenge(Challenge challenge) {
		return this.activeChallenges.contains(challenge);
	}

	public boolean isOnCooldown(Challenge challenge) {
		Long time = this.cooldowns.get(challenge.getKey());
		return time != null && time > System.currentTimeMillis();
	}

	public void setCooldown(Challenge challenge, int time) {
		this.cooldowns.put(challenge.getKey(), System.currentTimeMillis() + time * 1000);
	}

	public void savePlayer(File file) {

		Gson gson = new Gson();
		try (FileWriter writer = new FileWriter(file)) {
			List<String> challenge_keys = this.activeChallenges.stream().map(Challenge::getKey)
					.collect(Collectors.toList());
			gson.toJson(challenge_keys, writer);
		} catch (JsonIOException | IOException e) {
			// this.plugin.getLogger().severe("Encountered " + e.getClass().getSimpleName()
			// + " while saving player data for UUID " + this.id);
		}
	}
}