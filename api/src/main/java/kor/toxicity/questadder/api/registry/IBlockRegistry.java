package kor.toxicity.questadder.api.registry;

import kor.toxicity.questadder.api.block.IQuestBlock;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;

/**
 * @author toxicity
 */
public interface IBlockRegistry {
    /**
     * @since 1.0.10
     * @param key the yaml key of quest block
     * @return the object of quest block or null if not exist
     */
    @Nullable
    IQuestBlock findByKey(@NotNull String key);
    /**
     * @since 1.0.10
     * @param data the block data of quest block
     * @return the object of quest block or null if not exist
     */
    @Nullable
    IQuestBlock findByBlock(@NotNull BlockData data);

    /**
     * @since 1.0.10
     * @return all object of registered block
     */
    @NotNull Collection<@NotNull IQuestBlock> getAllBlock();

    /**
     * @since 1.0.10
     * @return all string key of registered block
     */
    @NotNull
    Set<String> getAllKeys();

    default @Nullable IQuestBlock findByBlock(@NotNull Block block) {
        return findByBlock(block.getBlockData());
    }
    default @Nullable IQuestBlock findByBlock(@NotNull Location location) {
        return findByBlock(location.getBlock());
    }
}
