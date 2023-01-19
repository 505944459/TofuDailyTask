package cn.ricetofu.task.pojo;

import java.util.List;

/**
 * @Author: RiceTofu123
 * @Date: 2023-01-19
 * @Discription: 玩家数据存储的类
 * */
public class SavedPlayerData {

    //上次收到每日任务的时间
    public String last_receive_date;

    //上次完成每日任务的时间
    public String last_finished_date = "";

    //上次收到完成奖励的时间
    public String last_reward_date = "";

    //累计完成次数
    public Integer finished_times;


    //上次收到的任务表(包含了完成情况等信息)
    public List<PlayerTask> list;
}
