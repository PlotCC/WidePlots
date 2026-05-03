package games.fatboychummy.wideplots.block.entity.events;

import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.HashMap;
import java.util.Map;

public class WPPlotAccessChangedEvent extends WPEvent {
    public static final String EVENT_ID = "plot_access_changed";

    public WPPlotAccessChangedEvent(String playerUUID, Map<String, Object> argMap) {
        super(EVENT_ID, playerUUID, argMap);
    }

    public static WPPlotAccessChangedEvent createSet(String playerUUID, String name, int priority) {
        return new WPPlotAccessChangedEvent(playerUUID, new HashMap<>(){{
            put("action", "create_rule_set");
            put("setName", name);
            put("priority", priority);
        }});
    }

    public static WPPlotAccessChangedEvent removeSet(String playerUUID, String name) {
        return new WPPlotAccessChangedEvent(playerUUID, new HashMap<>(){{
            put("action", "remove_rule_set");
            put("setName", name);
        }});
    }

    public static WPPlotAccessChangedEvent reorganizeSet(String playerUUID, String name, int oldPriority, int newPriority) {
        return new WPPlotAccessChangedEvent(playerUUID, new HashMap<>(){{
            put("action", "reorganize_rule_sets");
            put("setName", name);
            put("oldPriority", oldPriority);
            put("newPriority", newPriority);
        }});
    }

    public static WPPlotAccessChangedEvent updateRule(String playerUUID, String setName, String ruleName, String oldValue, String newValue) {
        return new WPPlotAccessChangedEvent(playerUUID, new HashMap<>(){{
            put("action", "update_rule");
            put("setName", setName);
            put("ruleName", ruleName);
            put("oldValue", oldValue);
            put("newValue", newValue);
        }});
    }

    public static WPPlotAccessChangedEvent addToPlayerList(String playerUUID, String setName, String newUUID) {
        return new WPPlotAccessChangedEvent(playerUUID, new HashMap<>(){{
            put("action", "add_to_player_list");
            put("setName", setName);
            put("UUID", newUUID);
        }});
    }

    public static WPPlotAccessChangedEvent removeFromPlayerList(String playerUUID, String setName, String removedUUID) {
        return new WPPlotAccessChangedEvent(playerUUID, new HashMap<>(){{
            put("action", "remove_from_player_list");
            put("setName", setName);
            put("UUID", removedUUID);
        }});
    }

    public static WPPlotAccessChangedEvent addToBlockList(String playerUUID, String setName, String newBlockID) {
        return new WPPlotAccessChangedEvent(playerUUID, new HashMap<>(){{
            put("action", "add_to_block_list");
            put("setName", setName);
            put("blockID", newBlockID);
        }});
    }

    public static WPPlotAccessChangedEvent removeFromBlockList(String playerUUID, String setName, String removedBlockID) {
        return new WPPlotAccessChangedEvent(playerUUID, new HashMap<>(){{
            put("action", "remove_from_block_list");
            put("setName", setName);
            put("blockID", removedBlockID);
        }});
    }

    public static WPPlotAccessChangedEvent setActive(String playerUUID, String setName, boolean active) {
        return new WPPlotAccessChangedEvent(playerUUID, new HashMap<>(){{
            put("action", "set_active");
            put("setName", setName);
            put("active", active);
        }});
    }

    public static WPPlotAccessChangedEvent setPlayerBlacklist(String playerUUID, String setName, boolean blacklistEnabled) {
        return new WPPlotAccessChangedEvent(playerUUID, new HashMap<>(){{
            put("action", "set_player_blacklist");
            put("setName", setName);
            put("blacklistEnabled", blacklistEnabled);
        }});
    }

    public static WPPlotAccessChangedEvent setBlockBlacklist(String playerUUID, String setName, boolean blacklistEnabled) {
        return new WPPlotAccessChangedEvent(playerUUID, new HashMap<>(){{
            put("action", "set_block_blacklist");
            put("setName", setName);
            put("blacklistEnabled", blacklistEnabled);
        }});
    }

    public static WPPlotAccessChangedEvent updateBoundingBox(String playerUUID, String setName, BoundingBox boundingBox) {
        return new WPPlotAccessChangedEvent(playerUUID, new HashMap<>(){{
            put("action", "update_bounding_box");
            put("setName", setName);
            put("min", new HashMap<String, Integer>(){{
                put("x", boundingBox.minX());
                put("y", boundingBox.minY());
                put("z", boundingBox.minZ());
            }});
            put("max", new HashMap<String, Integer>(){{
                put("x", boundingBox.maxX());
                put("y", boundingBox.maxY());
                put("z", boundingBox.maxZ());
            }});
        }});
    }

    public static WPPlotAccessChangedEvent removeBoundingBox(String playerUUID, String setName) {
        return new WPPlotAccessChangedEvent(playerUUID, new HashMap<>(){{
            put("action", "remove_bounding_box");
            put("setName", setName);
        }});
    }
}
