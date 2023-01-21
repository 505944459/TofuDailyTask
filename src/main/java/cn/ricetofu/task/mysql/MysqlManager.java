package cn.ricetofu.task.mysql;

import cn.ricetofu.task.TofuDailyTask;
import cn.ricetofu.task.core.ConfigManager;
import cn.ricetofu.task.core.PlayerDataManager;
import cn.ricetofu.task.core.TaskManager;
import cn.ricetofu.task.pojo.PlayerTask;
import cn.ricetofu.task.pojo.SavedPlayerData;
import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.security.PublicKey;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @Author: RiceTofu123
 * @Date: 2023-01-13
 * @Discription: 与数据库操作相关的类
 * */
public class MysqlManager {

    //是否启用了数据库,如果连接失败的话也会为false
    public static boolean enable = false;
    //数据库连接对象，如果没有配置或者连接失败会为null
    public static Connection connection = null;
    //日志记录对象
    private static Logger logger = Bukkit.getLogger();
    //Json解析对象
    private static Gson json = new Gson();
    //玩家数据对象,直接引用
    public static HashMap<String,SavedPlayerData> playerDataMap = PlayerDataManager.playerDataMap;
    //时间格式化对象
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 连接数据库的方法
     * @return 连接是否成功
     * */
    public static boolean connect(){
        //驱动加载
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE,"加载mysql驱动时出现异常？找不到驱动类？");
            return false;
        }

        //连接获取
        String url = "jdbc:mysql://"+ ConfigManager.mysql_host+"/"+ConfigManager.mysql_database+"?useSSL=false";
        String username = ConfigManager.mysql_username;
        String password = ConfigManager.mysql_password;
        try{
            connection = DriverManager.getConnection(url,username,password);
        }catch (SQLException e){
            logger.log(Level.SEVERE,"获取数据库连接时出现了异常？配置的数据库信息有误？？");
            return false;
        }

        //表创建(如果不存在)
        try {
            Statement statement = connection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS `playerdailytaskdata`(\n" +
                    "  `player_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '玩家id',\n" +
                    "  `last_receive_date` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '上次收到任务的时间',\n" +
                    "  `last_finished_date` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '上次完成任务的时间',\n" +
                    "  `last_reward_date` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '上次收到完成奖励的时间',\n" +
                    "  `finished_times` int(11) NULL DEFAULT NULL COMMENT '累计完成的次数',\n" +
                    "  `json` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '对象的json格式，包含完整数据',\n" +
                    "  PRIMARY KEY (`player_id`) USING BTREE\n" +
                    ") ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;");
            //检查表中是否有元素
            ResultSet resultSet = statement.executeQuery("SELECT player_id from playerdailytaskdata");
            if(!resultSet.next()){
                //TODO 表中无元素，则做一次本地数据的同步（考虑到可能是要本地转用Mysql存储）
                //
                //
                //
                //
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        //保持Mysql连接的定时任务
        new BukkitRunnable(){
            @Override
            public void run() {
                try {
                    connection.createStatement().execute("SELECT 1");
                } catch (SQLException e) {
                    cancel();//取消这个任务
                    logger.log(Level.WARNING,"数据库连接貌似断掉了捏~~正在尝试重连……");
                    if(connect()){
                        logger.log(Level.INFO,"数据库重连成功!");
                    }else {
                        logger.log(Level.SEVERE,"数据库重连失败!!!请检查，为确保运作，已经卸载本插件!!");
                        //插件卸载
                        Bukkit.getPluginManager().disablePlugin(TofuDailyTask.getProvidingPlugin(TofuDailyTask.class));
                    }
                }
            }
        }.runTaskTimerAsynchronously(TofuDailyTask.getProvidingPlugin(TofuDailyTask.class),60*20,60*20);
        //连接成功
        enable = true;
        return true;
    }

    /**
     * 从数据库中加载一个玩家的每日任务信息
     * @param player_id 玩家Id
     * @return 是否成功
     * */
    public static boolean loadOne(String player_id){
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("SELECT `json` from `playerdailytaskdata` where `player_id` = ?");
            statement.setString(1,player_id);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()){
                SavedPlayerData savedPlayerData = json.fromJson(resultSet.getString(1), SavedPlayerData.class);
                playerDataMap.put(player_id,savedPlayerData);
                if(savedPlayerData.last_receive_date.equals(sdf.format(new Date()))){
                    //如果保存的日期与今日相同，则证明保存的任务进度是今天的,否则不是,不能放到玩家任务表中
                    TaskManager.player_tasks.put(player_id,savedPlayerData.list);
                }
                return true;
            }
            //新建记录
            connection.createStatement().execute("INSERT INTO `playerdailytaskdata`(`player_id`) values ('"+player_id+"')");
            return false;
        } catch (SQLException e) {
            logger.severe(TofuDailyTask.console_header+"从数据库加载玩家:"+player_id+"数据出现了异常");
            return false;
        }
    }

    /**
     * 保存内存中所有的玩家数据到数据库中
     * @return 是否成功
     * */
    public static boolean save(){
        TaskManager.player_tasks.forEach((k,v)->{
            SavedPlayerData savedPlayerData = playerDataMap.get(k);
            savedPlayerData.list = v;
            PreparedStatement statement = null;
            try {
                connection.prepareStatement(
                        "UPDATE `playerdailytaskdata` set `last_receive_date` = ?,`last_finished_date` = ?,`last_reward_date` = ?,`finished_times` = ?,`json` = ? where player_id = ?"
                );
                statement.setString(1,savedPlayerData.last_receive_date);
                statement.setString(2, savedPlayerData.last_finished_date);
                statement.setString(3,savedPlayerData.last_reward_date);
                statement.setInt(4,savedPlayerData.finished_times);
                statement.setString(5, json.toJson(savedPlayerData));
                statement.setString(6,k);
                statement.executeUpdate();
            } catch (SQLException e) {
                logger.severe(TofuDailyTask.console_header+"在数据库保存玩家:"+k+"数据出现了异常");
            } finally {
                try {
                    if (!statement.isClosed()) {
                        statement.close();
                    }
                } catch (SQLException e) {
                    logger.severe(TofuDailyTask.console_header+"关闭statement对象异常");
                }
            }
        });
        return true;
    }

    /**
     * 保存内存中一个玩家的数据到数据库中
     * @param player_id 玩家的id
     * @return 是否成功
     * */
    public static boolean saveOne(String player_id){
        PreparedStatement statement = null;
        SavedPlayerData savedPlayerData = playerDataMap.get(player_id);
        try {
             statement = connection.prepareStatement(
                    "UPDATE `playerdailytaskdata` set `last_receive_date` = ?,`last_finished_date` = ?,`last_reward_date` = ?,`finished_times` = ?,`json` = ? where player_id = ?"
            );
             statement.setString(1,savedPlayerData.last_receive_date);
             statement.setString(2, savedPlayerData.last_finished_date);
             statement.setString(3,savedPlayerData.last_reward_date);
             statement.setInt(4,savedPlayerData.finished_times);
             statement.setString(5, json.toJson(savedPlayerData));
             statement.setString(6,player_id);
             return statement.executeUpdate()>0;
        } catch (SQLException e) {
            logger.severe(TofuDailyTask.console_header+"在数据库保存玩家:"+player_id+"数据出现了异常");
            return false;
        }
    }


}
