package org.sobadfish.tombstone.entity;



import cn.nukkit.entity.EntityHuman;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.particle.FlameParticle;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.TextFormat;
import org.sobadfish.tombstone.MainClass;
import org.sobadfish.tombstone.manager.entity.TombStone;

/**
 * @author Sobadfish
 * @date 2023/2/17
 */
public class TombStoneEntity extends EntityHuman {

    public TombStone tombStone;

    public int privateTime = 0;

    public int pTime = -1;

    public boolean isPrivate = true;

    public int liveTime;

    private int s = 1;

    @Override
    public float getWidth() {
        return 0.7f;
    }

    @Override
    public float getHeight() {
        return 0.7f;
    }

    public TombStoneEntity(TombStone stone, FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
        this.tombStone = stone;
        this.setNameTagVisible(true);
        this.setNameTagAlwaysVisible(true);
        setImmobile();
        if(pTime < 0){
            pTime = MainClass.mainClass.getConfig().getInt("protect-time");
        }
        liveTime = MainClass.mainClass.getConfig().getInt("live-time",300);
        setScale(2.5f);

        setNameTag("...");

    }


    @Override
    public boolean attack(float damage) {
        return false;
    }



    @Override
    public boolean onUpdate(int currentTick) {
        int var2 = currentTick - this.lastUpdate;

        String pri = MainClass.mainClass.getConfig().getString("protected-msg").replace("#{protected-time}",
                MainClass.formatTime(pTime - (privateTime / 20)));
        if(privateTime / 20 < pTime){
            privateTime+= var2;
        }else{
            pri = MainClass.mainClass.getConfig().getString("unprotected-msg");
            isPrivate = false;
        }

        if(tombStone != null){
            long afterTime = System.currentTimeMillis() - tombStone.createTime;
//            String msg = tombStone.target+" 的遗产 "+pri+" \n";
            int s = (int) (afterTime / 1000);
            String name = tombStone.target;
            if(MainClass.mainClass.getServer().getPluginManager().getPlugin("CustomName") != null){
                try {
                    name = TiXYA2357.Command.Lib.getPlayerStrName(tombStone.player);
                }catch (Exception ignore){}
            }
            String msg = MainClass.mainClass.getConfig().getString("display-title")
                    .replace("#{title}",MainClass.mainClass.getConfig().getString("title").replace("#{player}",name))
                    .replace("#{chunk-protected}",pri)
                    .replace("#{live-time}",MainClass.formatTime(liveTime - s));
            setNameTag(TextFormat.colorize('&',msg));
            if(liveTime - s <= 0){
                this.close();
            }
        }

        if(s >= 360){
            s = 1;
        }

        rotateY(new Position(getPosition().x + 1.3,getPosition().y + 0.6,getPosition().z,getLevel()),s);
        rotateY(new Position(getPosition().x - 1.3,getPosition().y + 0.6,getPosition().z,getLevel()),s);
        s++;

        return super.onUpdate(currentTick);
    }

    public TombStone getTombStone() {
        return tombStone;
    }

    //旋转粒子
    public void rotateY(Position point, double angle) {
        double cosAngle = Math.cos(angle);
        double sinAngle = Math.sin(angle);
        double x = point.x * cosAngle + point.z * sinAngle;
        double z = -point.x * sinAngle + point.z * cosAngle;

        getLevel().addParticle(new FlameParticle(new Position(point.x +x,point.y,point.z + z,point.level)));

    }

}
