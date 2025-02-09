package org.sobadfish.tombstone.manager.entity;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.level.Position;

import java.util.*;

/**
 * @author Sobadfish
 * @date 2023/2/17
 */
public class TombStone {

    public String target;

    public Player player;

    public Map<Integer,Item> inventoryItem;

    public Position spawnPosition;

    public long createTime;

    public TombStone(Player player){
        this.target = player.getName();
        this.player = player;
        this.inventoryItem = player.getInventory().getContents();
        this.spawnPosition = player.getPosition();
        this.createTime = System.currentTimeMillis();

    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TombStone)) {
            return false;
        }

        TombStone tombStone = (TombStone) o;

        if(tombStone.createTime != createTime){
            return false;
        }

        if (!target.equalsIgnoreCase(tombStone.target)) {
            return false;
        }
        return spawnPosition.equals(tombStone.spawnPosition);
    }

}
