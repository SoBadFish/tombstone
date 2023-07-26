package org.sobadfish.tombstone;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.player.PlayerDeathEvent;
import cn.nukkit.event.player.PlayerInteractEntityEvent;
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
import java.util.List;
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
        this.getLogger().info("初始化配置文件中...");
        saveDefaultConfig();
        reloadConfig();
        this.getLogger().info("加载墓碑皮肤中...");
        initDataFile();
        this.getLogger().info("墓碑插件成功加载");
        this.getServer().getPluginManager().registerEvents(this,this);
    }

    /**
     * 来自RsNPC的模型加载代码
     * */
    private void initDataFile() {
        //如果是4D皮肤
        if(!new File(this.getDataFolder()+"/skin").exists()){
            new File(this.getDataFolder()+"/skin").mkdirs();
        }
        if(!new File(this.getDataFolder()+"/skin/skin.png").exists()){
            saveResource("skin/skin.png","/skin/skin.png",true);
        }
        if(!new File(this.getDataFolder()+"/skin/skin.json").exists()){
            saveResource("skin/skin.json","/skin/skin.json",true);
        }
        skin = new Skin();
        BufferedImage skindata = null;
        try {
            skindata = ImageIO.read(new File(this.getDataFolder()+"/skin/skin.png"));
        } catch (IOException var19) {
            System.out.println("不存在皮肤");
        }

        if (skindata != null) {
            skin.setSkinData(skindata);
            skin.setSkinId("tombstone");
        }
        File skinJsonFile = new File(this.getDataFolder() + "/skin/skin.json");
        if(skinJsonFile.exists()){
            Map<String, Object> skinJson = (new Config(this.getDataFolder()+"/skin/skin.json", Config.JSON)).getAll();
            String geometryName = null;

            String formatVersion = (String) skinJson.getOrDefault("format_version", "1.10.0");
            skin.setGeometryDataEngineVersion(formatVersion); //设置皮肤版本，主流格式有1.16.0,1.12.0(Blockbench新模型),1.10.0(Blockbench Legacy模型),1.8.0
            switch (formatVersion){
                case "1.16.0":
                case "1.12.0":
                    geometryName = getGeometryName(skinJsonFile);
                    if("nullvalue".equals(geometryName)){
                        getLogger().info("暂不支持皮肤所用格式！请等待更新！");
                    }else{
                        skin.generateSkinId("tombstone");
                        skin.setSkinResourcePatch("{\"geometry\":{\"default\":\"" + geometryName + "\"}}");
                        skin.setGeometryName(geometryName);
                        try {
                            skin.setGeometryData(Utils.readFile(skinJsonFile));
                        }catch (IOException e){
                            return;
                        }

                        getLogger().info("加载皮肤中！");
                    }
                    break;
                default:
                    getLogger().info("["+skinJsonFile.getName()+"] 的版本格式为："+formatVersion + "，正在尝试加载！");
                case "1.10.0":
                case "1.8.0":
                    for (Map.Entry<String, Object> entry : skinJson.entrySet()) {
                        if (geometryName == null) {
                            if (entry.getKey().startsWith("geometry")) {
                                geometryName = entry.getKey();
                            }
                        }else {
                            break;
                        }
                    }
                    skin.generateSkinId("tombstone");
                    skin.setSkinResourcePatch("{\"geometry\":{\"default\":\"" + geometryName + "\"}}");
                    skin.setGeometryName(geometryName);
                    try {
                        skin.setGeometryData(Utils.readFile(skinJsonFile));
                    }catch (IOException e){
                        return;
                    }
                    break;
            }
        }
    }

    private static String getGeometryName(File file) {
        Config originGeometry = new Config(file, Config.JSON);
        if (!originGeometry.getString("format_version").equals("1.12.0") && !originGeometry.getString("format_version").equals("1.16.0")) {
            return "nullvalue";
        }
        //先读取minecraft:geometry下面的项目
        List<Map<String, Object>> geometryList = (List<Map<String, Object>>) originGeometry.get("minecraft:geometry");
        //不知道为何这里改成了数组，所以按照示例文件读取第一项
        Map<String, Object> geometryMain = geometryList.get(0);
        //获取description内的所有
        Map<String, Object> descriptions = (Map<String, Object>) geometryMain.get("description");
        return (String) descriptions.getOrDefault("identifier", "geometry.unknown"); //获取identifier
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
