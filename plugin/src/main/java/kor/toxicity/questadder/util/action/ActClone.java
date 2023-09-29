package kor.toxicity.questadder.util.action;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import kor.toxicity.questadder.QuestAdderBukkit;
import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.event.QuestAdderEvent;
import kor.toxicity.questadder.api.mechanic.AbstractAction;
import kor.toxicity.questadder.api.mechanic.ActionResult;
import kor.toxicity.questadder.api.util.DataField;
import kor.toxicity.questadder.manager.EntityManager;
import kor.toxicity.questadder.manager.LocationManager;
import kor.toxicity.questadder.manager.SkinManager;
import kor.toxicity.questadder.util.NamedLocation;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ActClone extends AbstractAction {
    @DataField(aliases = "k", throwIfNull = true)
    public String key;
    @DataField(aliases = "s")
    public String skin;
    @DataField(aliases = {"loc","l"})
    public String location;

    private GameProfile profile;
    private NamedLocation loc;

    public ActClone(@NotNull QuestAdder adder) {
        super(adder);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (skin != null) {
            profile = SkinManager.INSTANCE.getProfile(skin);
            if (profile == null) throw new RuntimeException("the skin named \"" + skin + " doesn't exist.");
        }
        if (location != null) {
            loc = LocationManager.INSTANCE.getLocation(location);
            if (loc == null) throw new RuntimeException("the location named \"" + location + " doesn't exist.");
        }
        skin = null;
        location = null;
    }

    @NotNull
    @Override
    public ActionResult invoke(@NotNull Player player, @NotNull QuestAdderEvent event) {
        var nms = QuestAdderBukkit.Companion.getNms();
        GameProfile prof;
        if (profile != null) {
            prof = profile;
        } else {
            prof = new GameProfile(UUID.randomUUID(),"");
            nms.getProperties(prof).putAll("textures", nms.getProperties(nms.getGameProfile(player)).get("textures"));
        }
        EntityManager.INSTANCE.register(player,key, nms.createFakePlayer(player, loc != null ? loc.getLocation() : player.getLocation(), prof));
        return ActionResult.SUCCESS;
    }
}
