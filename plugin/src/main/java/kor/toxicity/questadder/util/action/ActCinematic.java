package kor.toxicity.questadder.util.action;

import kor.toxicity.questadder.QuestAdderBukkit;
import kor.toxicity.questadder.api.QuestAdder;
import kor.toxicity.questadder.api.mechanic.CancellableAction;
import kor.toxicity.questadder.api.event.QuestAdderEvent;
import kor.toxicity.questadder.manager.LocationManager;
import kor.toxicity.questadder.api.util.DataField;
import kotlin.Unit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

public class ActCinematic extends CancellableAction {

    private static final Map<UUID,CinematicTask> TASK_MAP = new ConcurrentHashMap<>();

    @DataField(aliases = "f", throwIfNull = true)
    public String from;
    @DataField(aliases = "t",throwIfNull = true)
    public String to;
    @DataField(aliases = "i")
    public int iterate;
    @DataField(aliases = "p")
    public long period;
    @DataField
    public boolean packet = false;

    private BiConsumer<Integer,Player> consumer;

    public ActCinematic(@NotNull QuestAdder adder) {
        super(adder);
    }

    @Override
    public void cancel(@NotNull Player player) {
        var task = TASK_MAP.remove(player.getUniqueId());
        if (task != null) task.cancel();
    }

    @Override
    public void initialize() {
        super.initialize();
        if (period < 1) period = 1;
        if (iterate < 1) iterate = 1;
        var loc1 = LocationManager.INSTANCE.getLocation(from);
        if (loc1 == null) throw new RuntimeException("The location named \"" + from + "\" does not exist.");
        var loc2 = LocationManager.INSTANCE.getLocation(to);
        if (loc2 == null) throw new RuntimeException("The location named \"" + to + "\" does not exist.");

        var reference1 = loc1.getLocation();
        var reference2 = loc2.getLocation();

        var yaw = getYaw(reference1);


        var pitch = reference1.getPitch();
        var z = reference1.getZ();
        var y = reference1.getY();
        var x = reference1.getX();

        var addPitch = reference2.getPitch() - pitch;

        var yaw2 = getYaw(reference2);
        var addYaw1 = yaw2 - yaw;
        var addYaw2 = -yaw - (360 - yaw2);

        var addYaw = Math.abs(addYaw1) < Math.abs(addYaw2) ? addYaw1 : addYaw2;


        var addZ = reference2.getZ() - z;
        var addY = reference2.getY() - y;
        var addX = reference2.getX() - x;

        var multiply = 1 / (double) iterate;

        var loc = IntStream.range(0,iterate).mapToObj(i -> {
            var dI = (double) i;
            return new Location(
                    reference1.getWorld(),
                    x + addX * multiply * dI,
                    y + addY * multiply * dI,
                    z + addZ * multiply * dI,
                    (float) (yaw + addYaw * multiply * dI),
                    (float) (pitch + addPitch * multiply * dI)
            );
        }).toList();

        if (packet) consumer = (i,p) -> QuestAdderBukkit.Companion.getNms().changePosition(p,loc.get(i));
        else consumer = (i,p) -> p.teleport(loc.get(i));
    }

    @Override
    public void invoke(@NotNull Player player, @NotNull QuestAdderEvent event) {
        var task = TASK_MAP.put(player.getUniqueId(),new CinematicTask(player));
        if (task != null) task.cancel();
    }


    private static float getYaw(Location location) {
        var yaw = location.getYaw();
        return yaw < 0 ? 360 + yaw : yaw;
    }

    private class CinematicTask {
        private int i = 0;
        private final BukkitTask task;

        public CinematicTask(Player player) {
            task = QuestAdderBukkit.Companion.taskTimer(period,period,() -> {
                if (i == iterate) {
                    TASK_MAP.remove(player.getUniqueId());
                    cancel();
                } else consumer.accept(i++,player);
                return Unit.INSTANCE;
            });
        }
        private void cancel() {
            task.cancel();
        }
    }
}
