package me.jishuna.challengeme.challenges;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import me.jishuna.challengeme.api.challenge.Challenge;
import me.jishuna.challengeme.api.challenge.TickingChallenge;

public class AlwaysGlidingChallenge extends Challenge implements TickingChallenge {

	public AlwaysGlidingChallenge(Plugin owner, YamlConfiguration messageConfig) {
		super(owner, "always-gliding", messageConfig);

		addEventHandler(EntityToggleGlideEvent.class, this::onToggleGlide);
	}

	private void onToggleGlide(EntityToggleGlideEvent event, Player player) {
		event.setCancelled(true);
	}

	@Override
	public void onTick(Player player) {
		if (!player.isGliding() && !player.isFlying())
			player.setGliding(true);

		Vector velocity = player.getVelocity();
		if (!player.isFlying() && velocity.length() < 0.3D && !player.getLocation().getBlock().isLiquid())
			player.setVelocity(velocity.multiply(1.5D));

	}
}