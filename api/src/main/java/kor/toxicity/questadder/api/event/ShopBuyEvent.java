package kor.toxicity.questadder.api.event;

import kor.toxicity.questadder.api.shop.IShop;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ShopBuyEvent extends QuestAdderPlayerEvent implements ShopEvent {
    private final @NotNull IShop shop;
    private final @NotNull ItemStack item;
    public ShopBuyEvent(@NotNull Player who, @NotNull IShop shop, @NotNull ItemStack item) {
        super(who);
        this.shop = shop;
        this.item = item;
    }

    @NotNull
    @Override
    public IShop getShop() {
        return shop;
    }

    @NotNull
    public ItemStack getItem() {
        return item;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
