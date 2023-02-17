package org.sobadfish.tombstone;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.player.PlayerDeathEvent;
import cn.nukkit.item.Item;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.SerializedImage;
import cn.nukkit.utils.Utils;
import org.sobadfish.tombstone.entity.TombStoneEntity;
import org.sobadfish.tombstone.manager.TombStoneSpawnManager;
import org.sobadfish.tombstone.panel.DisPlayerPanel;
import org.sobadfish.tombstone.panel.lib.AbstractFakeInventory;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * @author Sobadfish
 * @date 2023/2/17
 */
public class MainClass extends PluginBase implements Listener {

    public static MainClass mainClass;

    public static Skin skin;

    @Override
    public void onEnable() {
        mainClass = this;
        checkServer();
        this.getLogger().info("初始化配置文件中...");
        saveDefaultConfig();
        reloadConfig();
        this.getLogger().info("加载墓碑皮肤中...");
        initDataFile();
        this.getLogger().info("墓碑插件成功加载");
        this.getServer().getPluginManager().registerEvents(this,this);
    }

    private void initDataFile() {
        File skinDir = new File(this.getDataFolder()+"/skin");
        if(!skinDir.exists()){
            if(!skinDir.mkdirs()){
                this.getLogger().info("皮肤文件夹创建失败");
            }
            File skinPng = new File(this.getDataFolder()+"/skin/skin.png");
            if(!skinPng.exists()){
                saveResource("skin/skin.png","/skin/skin.png",false);
            }
            File skinJson = new File(this.getDataFolder()+"/skin/skin.json");
            if(!skinJson.exists()){
                saveResource("skin/skin.json","/skin/skin.json",false);
            }
        }
        skin = new Skin();
        BufferedImage skindata;
        try {
            skindata = ImageIO.read(new File(this.getDataFolder() + "/skin/skin.png"));
            if (skindata != null) {
                skin.setSkinData(skindata);
                skin.setSkinId("tombstone");
            }
            Map<String, Object> skinJson = (new Config(this.getDataFolder() + "/skin/skin.json", Config.JSON)).getAll();
            String geometryName = null;

            if (skinJson.containsKey("format_version")) {
                skin.generateSkinId("tombstone");
                for (Map.Entry<String, Object> entry1 : skinJson.entrySet()) {
                    if (geometryName == null) {
                        if (entry1.getKey().startsWith("geometry")) {
                            geometryName = entry1.getKey();
                        }
                    }
                }
                skin.setSkinResourcePatch("{\"geometry\":{\"default\":\"" + geometryName + "\"}}");
                skin.setGeometryData(Utils.readFile(new File(this.getDataFolder() + "/skin/skin.json")));
                skin.setTrusted(true);
            } else {
                for (Map.Entry<String, Object> entry1 : skinJson.entrySet()) {
                    if (geometryName == null) {
                        geometryName = entry1.getKey();
                    }
                }
                skin.setGeometryName(geometryName);
                skin.setGeometryData(Utils.readFile(new File(this.getDataFolder() + "/skin/skin.json")));
            }
        } catch (IOException var19) {
            this.getLogger().error("皮肤载入失败！");
        }

    }

    @EventHandler
    public void onDamage(EntityDamageEvent event){
        Entity entity = event.getEntity();
        if(entity instanceof TombStoneEntity){
            event.setCancelled();
            if(event instanceof EntityDamageByEntityEvent){
                Entity damager = ((EntityDamageByEntityEvent) event).getDamager();

                if(damager instanceof Player) {
                    //必须离得近
                    if (entity.distance(damager) <= 3) {
                        if(((TombStoneEntity) entity).isPrivate){
                            if(((TombStoneEntity) entity).tombStone != null){
                                if(!((TombStoneEntity) entity).tombStone.target.equalsIgnoreCase(damager.getName())){
                                    return;
                                }
                            }
                        }

                        new DisPlayerPanel().displayPlayer((Player) damager, ((TombStoneEntity) entity),
                                MainClass.mainClass.getConfig().getString("title").replace("#{player}", ((TombStoneEntity) entity).tombStone.target));
                    }
                }
            }

        }


    }


    private static void checkServer(){
        boolean ver = false;
        //双核心兼容
        try {
            Class<?> c = Class.forName("cn.nukkit.Nukkit");
            c.getField("NUKKIT_PM1E");
            ver = true;

        } catch (ClassNotFoundException | NoSuchFieldException ignore) { }
        try {
            Class<?> c = Class.forName("cn.nukkit.Nukkit");
            c.getField("NUKKIT").get(c).toString().equalsIgnoreCase("Nukkit PetteriM1 Edition");
            ver = true;
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException ignore) {
        }

        AbstractFakeInventory.IS_PM1E = ver;

    }


    @EventHandler
    public void onDeath(PlayerDeathEvent event){
        if(getConfig().getStringList("enable-world").contains(event.getEntity().getLevel().getFolderName())) {
            if(!event.getEntity().getInventory().isEmpty()) {
                TombStoneSpawnManager.spawnTomStone(event.getEntity());
            }
            event.setDrops(new Item[0]);
        }
    }

    /**
     * 将秒转换为时间显示格式 00:00
     * @param s 秒
     * @return 时间显示格式
     * */
    public static String formatTime(int s){
        int min = s / 60;
        int ss = s % 60;
        String mi = min+"";
        String sss = ss+"";
        if(min < 10){
            mi = "0"+mi;
        }
        if(ss < 10){
            sss = "0"+ss;
        }
        if(min > 0){

            return mi+":"+sss;
        }else{
            return "00:"+sss+"";
        }

    }
}
