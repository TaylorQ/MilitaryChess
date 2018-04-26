package com.example.taylorq.militarychess;

public class Piece {

    private int level;
    private boolean mine;
    private boolean visible;

    public Piece(int l, boolean m, boolean v){
        level = l;
        mine = m;
        visible = v;
    }

    public int getLevel(){
        return level;
    }

    public boolean isMine(){
        return mine;
    }

    public void turnover(){
        visible = !visible;
    }

    public boolean isVisible(){
        return visible;
    }

    public int fight(Piece enemy){
        if(level == enemy.getLevel())return 2;//和自己同军衔的敌人同归于尽
        if(enemy.getLevel() == Constants.zhadan)return 2;//任何人和炸弹都同归于尽
        if(enemy.getLevel() == Constants.bomb)return 2;//任何人和自爆卡车都同归于尽
        if(enemy.getLevel() == Constants.junqi)return 3;//拔军旗赢了
        if(enemy.getLevel() == Constants.commander)return 3;//拆司令部赢了
        if(enemy.getLevel() == Constants.spy)return 1;//任何人都可以杀间谍
        switch(level){
            case Constants.zhadan:
            case Constants.bomb:
                return 2;//炸弹和自爆卡车和任何人都同归于尽
            case Constants.gongbing:
                if(enemy.getLevel() == Constants.dilei)return 1;//工兵拆地雷
                return 0;//不然就是死
            case Constants.engineer:
                if(enemy.getLevel() == Constants.antiaircraft)return 1;//工程车拆防空装置
                if(enemy.getLevel() == Constants.missile)return 1;//工程车拆导弹
                return 0;//不然就是死
            default:
                if(enemy.getLevel() == Constants.dilei)return 0;//普通人踩地雷都是死
                if(enemy.getLevel() == Constants.antiaircraft)return 0;//普通人撞到防空装置都是死
                if(enemy.getLevel() == Constants.missile)return 2;//普通人和导弹发射器都同归于尽
                return level>enemy.getLevel()?1:0;//不然就比大小
        }
    }

}
