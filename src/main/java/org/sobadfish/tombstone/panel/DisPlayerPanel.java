package org.sobadfish.tombstone.panel;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.inventory.InventoryHolder;
import cn.nukkit.item.Item;
import org.sobadfish.tombstone.entity.TombStoneEntity;
import org.sobadfish.tombstone.manager.entity.TombStone;
import org.sobadfish.tombstone.panel.lib.AbstractFakeInventory;
import org.sobadfish.tombstone.panel.lib.DoubleChestFakeInventory;

import java.util.Map;

/**
 * @author Sobadfish
 * @date 2023/2/17
 */
public class DisPlayerPanel implements InventoryHolder {

    private AbstractFakeInventory inventory;


    /**
     * 将箱子菜单展示给用户
     * @param player 玩家对象 {@link Player}
     * @param stone 物品
     * @param name 菜单名称
     *
     */
    public void displayPlayer(Player player, TombStoneEntity stone, String name){
        DoubleChestFakeInventory panel = new DoubleChestFakeInventory(this,name);
        panel.setContents(stone.tombStone.inventoryItem);
        panel.setStone(stone);
        inventory = panel;
        player.addWindow(panel);

    }



    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
