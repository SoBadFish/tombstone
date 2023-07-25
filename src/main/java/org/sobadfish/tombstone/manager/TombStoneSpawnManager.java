package org.sobadfish.tombstone.manager;

import cn.nukkit.Player;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.nbt.tag.CompoundTag;
import org.sobadfish.tombstone.MainClass;
import org.sobadfish.tombstone.entity.TombStoneEntity;
import org.sobadfish.tombstone.manager.entity.TombStone;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Sobadfish
 * @date 2023/2/17
 */
public class TombStoneSpawnManager {


    public static void spawnTomStone(Player player){
        TombStone tombStone = new TombStone(player);

        Skin skin = MainClass.skin;
        if(MainClass.mainClass.getConfig().getBoolean("load-player-skin",true)){
            if(player.getSkin() != null){
                skin.setSkinData(player.getSkin().getSkinData());
            }
        }
        CompoundTag tag = EntityHuman.getDefaultNBT(player.getPosition());
        tag.putCompound("Skin",new CompoundTag()
                .putByteArray("Data", skin.getSkinData().data)
                .putString("ModelId",skin.getSkinId())
        );

        TombStoneEntity tombStoneEntity = new TombStoneEntity(tombStone,player.chunk,
              tag);

        tombStoneEntity.setSkin(skin);
        tombStoneEntity.yaw = player.getYaw();
        tombStoneEntity.spawnToAll();
//        tombStones.add(tombStone);


    }


}
