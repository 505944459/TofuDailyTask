package cn.ricetofu.task.core;

import cn.ricetofu.task.pojo.SavedPlayerData;
import com.google.gson.Gson;
import org.bukkit.Bukkit;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * @Author: RiceTofu123
 * @Date: 2023-01-18
 * @Discription: 玩家数据操作的类
 * */
public class PlayerDataManager {

    private static Gson json = new Gson();//json操作对象
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    public static HashMap<String,SavedPlayerData> playerDataMap = new HashMap<>();

    /**
     * 加载所有玩家的数据，从本地的json文件中，不建议使用，建议每次只加载一个
     * @param file json文件的所处文件夹
     * */
    public static void loadLocal(File file){

        for (File listFile : file.listFiles()) {
            if(!listFile.isDirectory()){
                String name = listFile.getName();
                int i = name.lastIndexOf(".");
                String player_name = name.substring(0,i);
                String last_name = name.substring(i);
                if(last_name.equals(".json")){
                    try {
                        SavedPlayerData savedPlayerData = json.fromJson(new FileReader(listFile), SavedPlayerData.class);
                        playerDataMap.put(player_name,savedPlayerData);//保存到表中
                        if(savedPlayerData.last_receive_date.equals(sdf.format(new Date()))){
                            //如果保存的日期与今日相同，则证明保存的任务进度是今天的,否则不是,不能放到玩家任务表中
                            TaskManager.player_tasks.put(player_name,savedPlayerData.list);
                        }
                    } catch (FileNotFoundException e) {
                        Bukkit.getLogger().severe(MessageManager.header+"加载玩家数据文件时出现了一个错误!玩家id:"+player_name);
                    }
                }
            }
        }

    }

    /**
     * 加载一个玩家的玩家数据，从本地的json文件中
     * @param file 玩家数据所在的文件夹
     * @param player_id 玩家名字
     * */
    public static void loadOneLocal(File file,String player_id){
        file = new File(file,player_id+".json");
        if(file.exists()) { //新玩家无数据，会在第一次领取每日任务的时候新建对象
            try {
                SavedPlayerData savedPlayerData = json.fromJson(new FileReader(file), SavedPlayerData.class);
                playerDataMap.put(player_id,savedPlayerData);//保存到表中
                if(savedPlayerData.last_receive_date.equals(sdf.format(new Date()))){
                    //如果保存的日期与今日相同，则证明保存的任务进度是今天的,否则不是,不能放到玩家任务表中
                    TaskManager.player_tasks.put(player_id,savedPlayerData.list);
                }
            } catch (FileNotFoundException e) {
                Bukkit.getLogger().severe(MessageManager.header+"加载玩家数据文件时出现了一个错误!玩家id:"+player_id);
            }
        }


    }

    /**
     * 保存内存中所有的玩家数据到本地的json文件中，不建议使用，建议单个单个保存
     * @param file 保存到的文件目录对象
     * */
    public static void saveLocal(File file){

        TaskManager.player_tasks.forEach((k,v)->{
            try {
                File data = new File(file, k + ".json");
                if(!data.exists())data.createNewFile();
                BufferedWriter writer = new BufferedWriter(new FileWriter(data));
                SavedPlayerData savedPlayerData = playerDataMap.get(k);
                savedPlayerData.list = v;
                String s = json.toJson(savedPlayerData, SavedPlayerData.class);
                writer.write(s);
                writer.flush();
                writer.close();
            } catch (IOException e) {
                Bukkit.getLogger().severe(MessageManager.header+"保存玩家数据文件时出现了一个错误!玩家id:"+k);
            }
        });

    }

    public static void saveOneLocal(File file,String player_id){
        //保存一个玩家的数据
        try {
            File data = new File(file, player_id + ".json");
            if(!data.exists())data.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(data));
            SavedPlayerData savedPlayerData = playerDataMap.get(player_id);
            savedPlayerData.list = TaskManager.player_tasks.get(player_id);
            savedPlayerData.last_receive_date = sdf.format(new Date());
            String s = json.toJson(savedPlayerData, SavedPlayerData.class);
            writer.write(s);
            writer.flush();
            writer.close();
        }catch (IOException e){
            Bukkit.getLogger().severe(MessageManager.header+"保存玩家数据文件时出现了一个错误!玩家id:"+player_id);
        }
    }
}
