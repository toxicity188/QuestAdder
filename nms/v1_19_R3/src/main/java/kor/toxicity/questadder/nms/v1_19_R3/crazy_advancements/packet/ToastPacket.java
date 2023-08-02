package kor.toxicity.questadder.nms.v1_19_R3.crazy_advancements.packet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import kor.toxicity.questadder.nms.v1_19_R3.crazy_advancements.advancement.ToastNotification;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.protocol.game.PacketPlayOutAdvancements;
import net.minecraft.resources.MinecraftKey;

/**
 * Represents an Advancements Packet for Toast Notifications
 * 
 * @author Axel
 *
 */
public class ToastPacket {
	
	private final Player player;
	private final boolean add;
	private final ToastNotification notification;
	
	/**
	 * Constructor for creating Toast Packets
	 * 
	 * @param player The target Player
	 * @param add Whether to add or remove the Advancement
	 * @param notification The Notification
	 */
	public ToastPacket(Player player, boolean add, ToastNotification notification) {
		this.player = player;
		this.add = add;
		this.notification = notification;
	}
	
	/**
	 * Gets the target Player
	 * 
	 * @return The target Player
	 */
	public Player getPlayer() {
		return player;
	}
	
	/**
	 * Gets whether the Advancement is added or removed
	 * 
	 * @return Whether the Advancement is added or removed
	 */
	public boolean isAdd() {
		return add;
	}
	
	/**
	 * Gets the Notification
	 * 
	 * @return The Notification
	 */
	public ToastNotification getNotification() {
		return notification;
	}
	
	/**
	 * Builds a packet that can be sent to a Player
	 * 
	 * @return The Packet
	 */
	public PacketPlayOutAdvancements build() {
		//Create Lists
		List<net.minecraft.advancements.Advancement> advancements = new ArrayList<>();
		Set<MinecraftKey> removedAdvancements = new HashSet<>();
		Map<MinecraftKey, AdvancementProgress> progress = new HashMap<>();
		
		//Populate Lists
		if(add) {
			advancements.add(PacketConverter.toNmsToastAdvancement(getNotification()));
			progress.put(ToastNotification.NOTIFICATION_NAME.getMinecraftKey(), ToastNotification.NOTIFICATION_PROGRESS.getNmsProgress());
		} else {
			removedAdvancements.add(ToastNotification.NOTIFICATION_NAME.getMinecraftKey());
		}
		
		//Create Packet
		PacketPlayOutAdvancements packet = new PacketPlayOutAdvancements(false, advancements, removedAdvancements, progress);
		return packet;
	}
	
	/**
	 * Sends the Packet to the target Player
	 * 
	 */
	public void send() {
		PacketPlayOutAdvancements packet = build();
		((CraftPlayer) getPlayer()).getHandle().b.a(packet);
	}
	
	
	
	
}