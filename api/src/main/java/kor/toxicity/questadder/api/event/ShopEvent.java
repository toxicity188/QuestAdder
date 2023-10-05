package kor.toxicity.questadder.api.event;

import kor.toxicity.questadder.api.shop.IShop;
import org.jetbrains.annotations.NotNull;

public interface ShopEvent {
    @NotNull
    IShop getShop();
}
