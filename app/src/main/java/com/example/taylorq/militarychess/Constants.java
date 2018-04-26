package com.example.taylorq.militarychess;

public interface Constants {
    // BluetoothService的handler消息标识
    int MESSAGE_STATE_CHANGE = 1;
    int MESSAGE_READ = 2;
    int MESSAGE_TOAST = 5;

    //用户之间蓝牙通讯的消息头
    String new_game = "[NEW GAME]";
    String ready = "[PLAN FINISH]";
    String cancel_ready = "[CANCEL READY]";
    String my_plan = "[MY PLAN]";
    String move = "[MOVE]";
    String shoot = "[SHOOT]";
    String expose = "[EXPOSE]";
    String win = "[WIN]";
    String surrender = "[SURRENDER]";

    //经典玩法的兵种标识
    int dilei      = 0;
    int gongbing   = 1;
    int paizhang   = 2;
    int lianzhang  = 3;
    int yingzhang  = 4;
    int tuanzhang  = 5;
    int lvzhang    = 6;
    int shizhang   = 7;
    int junzhang   = 8;
    int siling     = 9;
    int zhadan    = 10;
    int junqi     = 11;

    //新玩法的兵种标识
    int commander     = 12;
    int missile       = 13;
    int antiaircraft  = 14;
    int spy           = 15;
    int engineer      = 16;
    int bomb          = 17;
    int juniorcombat  = 18;
    int midcombat     = 19;
    int seniorcombat  = 20;
    int ultcombat     = 21;

    String[] p_name = {"地雷", "工兵", "排长", "连长", "营长", "团长",
                        "旅长", "师长", "军长", "司令", "炸弹", "军旗",
                        "司令部", "导弹", "防空装置", "间谍", "工程车",
                        "自爆卡车", "初级兵团", "中级兵团", "高级兵团", "特种兵团"};
}
