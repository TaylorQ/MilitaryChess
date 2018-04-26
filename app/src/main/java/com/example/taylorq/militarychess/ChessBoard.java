package com.example.taylorq.militarychess;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;

import java.util.ArrayList;

public class ChessBoard {

    private final int[][] type_map = {
            {Field.TYPE_NORMAL, Field.TYPE_BASECAMP, Field.TYPE_NORMAL, Field.TYPE_BASECAMP, Field.TYPE_NORMAL},
            {Field.TYPE_RAIL,   Field.TYPE_RAIL,     Field.TYPE_RAIL,   Field.TYPE_RAIL,     Field.TYPE_RAIL},
            {Field.TYPE_RAIL,   Field.TYPE_CAMP,     Field.TYPE_NORMAL, Field.TYPE_CAMP,     Field.TYPE_RAIL},
            {Field.TYPE_RAIL,   Field.TYPE_NORMAL,   Field.TYPE_CAMP,   Field.TYPE_NORMAL,   Field.TYPE_RAIL},
            {Field.TYPE_RAIL,   Field.TYPE_CAMP,     Field.TYPE_NORMAL, Field.TYPE_CAMP,     Field.TYPE_RAIL},
            {Field.TYPE_RAIL,   Field.TYPE_RAIL,     Field.TYPE_RAIL,   Field.TYPE_RAIL,     Field.TYPE_RAIL},
            {Field.TYPE_RAIL,   Field.TYPE_RAIL,     Field.TYPE_RAIL,   Field.TYPE_RAIL,     Field.TYPE_RAIL},
            {Field.TYPE_RAIL,   Field.TYPE_CAMP,     Field.TYPE_NORMAL, Field.TYPE_CAMP,     Field.TYPE_RAIL},
            {Field.TYPE_RAIL,   Field.TYPE_NORMAL,   Field.TYPE_CAMP,   Field.TYPE_NORMAL,   Field.TYPE_RAIL},
            {Field.TYPE_RAIL,   Field.TYPE_CAMP,     Field.TYPE_NORMAL, Field.TYPE_CAMP,     Field.TYPE_RAIL},
            {Field.TYPE_RAIL,   Field.TYPE_RAIL,     Field.TYPE_RAIL,   Field.TYPE_RAIL,     Field.TYPE_RAIL},
            {Field.TYPE_NORMAL, Field.TYPE_BASECAMP, Field.TYPE_NORMAL, Field.TYPE_BASECAMP, Field.TYPE_NORMAL}};

    public static final int[][][] map = {
            {{0,0,1,0,1,0,0,0},{0,0,1,0,1,0,1,0},{0,0,1,0,1,0,1,0},{0,0,1,0,1,0,1,0},{0,0,0,0,1,0,1,0}},
            {{1,0,2,1,2,0,0,0},{1,0,2,0,1,0,2,0},{1,0,2,1,1,1,2,0},{1,0,2,0,1,0,2,0},{1,0,0,0,2,1,2,0}},
            {{2,0,1,0,2,0,0,0},{1,1,1,1,1,1,1,1},{1,0,1,0,1,0,1,0},{1,1,1,1,1,1,1,1},{2,0,0,0,2,0,1,0}},
            {{2,1,1,1,2,0,0,0},{1,0,1,0,1,0,1,0},{1,1,1,1,1,1,1,1},{1,0,1,0,1,0,1,0},{2,0,0,0,2,1,1,1}},
            {{2,0,1,0,2,0,0,0},{1,1,1,1,1,1,1,1},{1,0,1,0,1,0,1,0},{1,1,1,1,1,1,1,1},{2,0,0,0,2,0,1,0}},
            {{2,1,2,0,2,0,0,0},{1,0,2,0,0,0,2,0},{1,1,2,0,2,0,2,1},{1,0,2,0,0,0,2,0},{2,0,0,0,2,0,2,1}},
            {{2,0,2,1,2,0,0,0},{0,0,2,0,1,0,2,0},{2,0,2,1,1,1,2,0},{0,0,2,0,1,0,2,0},{2,0,0,0,2,1,2,0}},
            {{2,0,1,0,2,0,0,0},{1,1,1,1,1,1,1,1},{1,0,1,0,1,0,1,0},{1,1,1,1,1,1,1,1},{2,0,0,0,2,0,1,0}},
            {{2,1,1,1,2,0,0,0},{1,0,1,0,1,0,1,0},{1,1,1,1,1,1,1,1},{1,0,1,0,1,0,1,0},{2,0,0,0,2,1,1,1}},
            {{2,0,1,0,2,0,0,0},{1,1,1,1,1,1,1,1},{1,0,1,0,1,0,1,0},{1,1,1,1,1,1,1,1},{2,0,0,0,2,0,1,0}},
            {{2,1,2,0,1,0,0,0},{1,0,2,0,1,0,2,0},{1,1,2,0,1,0,2,1},{1,0,2,0,1,0,2,0},{2,0,0,0,1,0,2,1}},
            {{1,0,1,0,0,0,0,0},{1,0,1,0,0,0,1,0},{1,0,1,0,0,0,1,0},{1,0,1,0,0,0,1,0},{1,0,0,0,0,0,1,0}},
    };

    private static final int[][] default_cla_plan = {
            {Constants.shizhang, Constants.gongbing, Constants.paizhang, Constants.lianzhang, Constants.shizhang},
            {Constants.lvzhang, -1, Constants.lianzhang, -1, Constants.zhadan},
            {Constants.zhadan, Constants.yingzhang, -1, Constants.gongbing, Constants.lvzhang},
            {Constants.junzhang, -1, Constants.tuanzhang, -1, Constants.lianzhang},
            {Constants.siling, Constants.gongbing, Constants.yingzhang, Constants.dilei, Constants.paizhang},
            {Constants.tuanzhang, Constants.paizhang, Constants.dilei, Constants.junqi, Constants.dilei}
    };

    private static final int[][] default_new_plan = {
            {Constants.seniorcombat, Constants.engineer, Constants.juniorcombat, Constants.antiaircraft, Constants.seniorcombat},
            {Constants.midcombat, -1, Constants.juniorcombat, -1, Constants.bomb},
            {Constants.bomb, Constants.spy, -1, Constants.engineer, Constants.seniorcombat},
            {Constants.ultcombat, -1, Constants.midcombat, -1, Constants.spy},
            {Constants.ultcombat, Constants.engineer, Constants.antiaircraft, Constants.ultcombat, Constants.juniorcombat},
            {Constants.midcombat, Constants.commander, Constants.midcombat, Constants.missile, Constants.juniorcombat}
    };

    public Field[][] chessboard = new Field[12][5];

    public ChessBoard(Activity activity, View.OnClickListener onClickListener){

        chessboard[0][0] = activity.findViewById(R.id.field_0_0);
        chessboard[0][1] = activity.findViewById(R.id.field_0_1);
        chessboard[0][2] = activity.findViewById(R.id.field_0_2);
        chessboard[0][3] = activity.findViewById(R.id.field_0_3);
        chessboard[0][4] = activity.findViewById(R.id.field_0_4);
        chessboard[1][0] = activity.findViewById(R.id.field_1_0);
        chessboard[1][1] = activity.findViewById(R.id.field_1_1);
        chessboard[1][2] = activity.findViewById(R.id.field_1_2);
        chessboard[1][3] = activity.findViewById(R.id.field_1_3);
        chessboard[1][4] = activity.findViewById(R.id.field_1_4);
        chessboard[2][0] = activity.findViewById(R.id.field_2_0);
        chessboard[2][1] = activity.findViewById(R.id.field_2_1);
        chessboard[2][2] = activity.findViewById(R.id.field_2_2);
        chessboard[2][3] = activity.findViewById(R.id.field_2_3);
        chessboard[2][4] = activity.findViewById(R.id.field_2_4);
        chessboard[3][0] = activity.findViewById(R.id.field_3_0);
        chessboard[3][1] = activity.findViewById(R.id.field_3_1);
        chessboard[3][2] = activity.findViewById(R.id.field_3_2);
        chessboard[3][3] = activity.findViewById(R.id.field_3_3);
        chessboard[3][4] = activity.findViewById(R.id.field_3_4);
        chessboard[4][0] = activity.findViewById(R.id.field_4_0);
        chessboard[4][1] = activity.findViewById(R.id.field_4_1);
        chessboard[4][2] = activity.findViewById(R.id.field_4_2);
        chessboard[4][3] = activity.findViewById(R.id.field_4_3);
        chessboard[4][4] = activity.findViewById(R.id.field_4_4);
        chessboard[5][0] = activity.findViewById(R.id.field_5_0);
        chessboard[5][1] = activity.findViewById(R.id.field_5_1);
        chessboard[5][2] = activity.findViewById(R.id.field_5_2);
        chessboard[5][3] = activity.findViewById(R.id.field_5_3);
        chessboard[5][4] = activity.findViewById(R.id.field_5_4);
        chessboard[6][0] = activity.findViewById(R.id.field_7_0);
        chessboard[6][1] = activity.findViewById(R.id.field_7_1);
        chessboard[6][2] = activity.findViewById(R.id.field_7_2);
        chessboard[6][3] = activity.findViewById(R.id.field_7_3);
        chessboard[6][4] = activity.findViewById(R.id.field_7_4);
        chessboard[7][0] = activity.findViewById(R.id.field_8_0);
        chessboard[7][1] = activity.findViewById(R.id.field_8_1);
        chessboard[7][2] = activity.findViewById(R.id.field_8_2);
        chessboard[7][3] = activity.findViewById(R.id.field_8_3);
        chessboard[7][4] = activity.findViewById(R.id.field_8_4);
        chessboard[8][0] = activity.findViewById(R.id.field_9_0);
        chessboard[8][1] = activity.findViewById(R.id.field_9_1);
        chessboard[8][2] = activity.findViewById(R.id.field_9_2);
        chessboard[8][3] = activity.findViewById(R.id.field_9_3);
        chessboard[8][4] = activity.findViewById(R.id.field_9_4);
        chessboard[9][0] = activity.findViewById(R.id.field_10_0);
        chessboard[9][1] = activity.findViewById(R.id.field_10_1);
        chessboard[9][2] = activity.findViewById(R.id.field_10_2);
        chessboard[9][3] = activity.findViewById(R.id.field_10_3);
        chessboard[9][4] = activity.findViewById(R.id.field_10_4);
        chessboard[10][0] = activity.findViewById(R.id.field_11_0);
        chessboard[10][1] = activity.findViewById(R.id.field_11_1);
        chessboard[10][2] = activity.findViewById(R.id.field_11_2);
        chessboard[10][3] = activity.findViewById(R.id.field_11_3);
        chessboard[10][4] = activity.findViewById(R.id.field_11_4);
        chessboard[11][0] = activity.findViewById(R.id.field_12_0);
        chessboard[11][1] = activity.findViewById(R.id.field_12_1);
        chessboard[11][2] = activity.findViewById(R.id.field_12_2);
        chessboard[11][3] = activity.findViewById(R.id.field_12_3);
        chessboard[11][4] = activity.findViewById(R.id.field_12_4);

        for (int i = 0;i < 12;i++){
            for (int j = 0;j < 5;j++){
                chessboard[i][j].setOnClickListener(onClickListener);
                chessboard[i][j].setType(type_map[i][j]);
                chessboard[i][j].setPosition(i, j);
            }
        }
    }

    public String enemy_move(String move){
        int fromx = move.charAt(0) - 'a';fromx = 11 - fromx;
        int fromy = move.charAt(1) - 'a';fromy = 4 - fromy;
        int tox = move.charAt(2) - 'a';tox = 11 - tox;
        int toy = move.charAt(3) - 'a';toy = 4 - toy;
        int result = move.charAt(4) - 'a';

        if(chessboard[tox][toy].getOwner() == null){
                chessboard[tox][toy].setOwner(chessboard[fromx][fromy].getOwner());
                chessboard[fromx][fromy].setOwner(null);
                chessboard[fromx][fromy].red_mark();
                chessboard[tox][toy].red_mark();
                return "敌方("+(fromx+1)+","+(fromy+1)+")移动至("+(tox+1)+","+(toy+1)+")。";
            }

            String str_move = "敌方("+(fromx+1)+","+(fromy+1)+")攻击我方"
                    +Constants.p_name[chessboard[tox][toy].getOwner().getLevel()];

            if (result == 0){
                str_move += "失败，被击杀。";
            }else if (result == 1){
                str_move += "成功。";
            chessboard[tox][toy].setOwner(chessboard[fromx][fromy].getOwner());
        }else if (result == 2){
            str_move += "，双方同归于尽。";
            chessboard[tox][toy].setOwner(null);
        }
        chessboard[fromx][fromy].setOwner(null);
        chessboard[fromx][fromy].red_mark();
        chessboard[tox][toy].red_mark();


        return str_move;
    }
    public String boming(String missile){
        int bx = missile.charAt(0) - 'a';bx = 11 - bx;
        int by = missile.charAt(1) - 'a';by = 4 - by;
        int result = missile.charAt(2) - 'a';
        String str_bomb = "敌方发射导弹打击我方("+(bx+1)+","+(by+1)+")，";
        if (result == 1){
            str_bomb += "击杀我方"+Constants.p_name[chessboard[bx][by].getOwner().getLevel()]+"。";
            chessboard[bx][by].setOwner(null);
        }else{
            str_bomb += "被我方防空装置拦截。";
        }
        chessboard[bx][by].red_mark();
        return str_bomb;
    }

    public String spy_activate(String spy){
        int spyx = spy.charAt(0) - 'a';spyx = 11 - spyx;
        int spyy = spy.charAt(1) - 'a';spyy = 4 - spyy;

        chessboard[spyx+1][spyy+1].expose();
        chessboard[spyx+1][spyy].expose();
        chessboard[spyx+1][spyy-1].expose();
        chessboard[spyx][spyy+1].expose();
        chessboard[spyx][spyy].expose();
        chessboard[spyx][spyy-1].expose();
        chessboard[spyx-1][spyy+1].expose();
        chessboard[spyx-1][spyy].expose();
        chessboard[spyx-1][spyy-1].expose();

        chessboard[spyx][spyy].red_mark();

        return "在("+(spyx+1)+","+(spyy+1)+")出现了敌方间谍,四周的友军都暴露了。";
    }

    public void place_my_classicpiece(){
        for (int i = 0;i < 6;i++){
            for (int j =  0;j < 5;j++){
                if (default_cla_plan[i][j] != -1)
                    chessboard[i+6][j].setOwner(new Piece(default_cla_plan[i][j], true, false));
            }
        }
    }

    public void place_my_newpiece(){
        for (int i = 0;i < 6;i++){
            for (int j =  0;j < 5;j++){
                if (default_new_plan[i][j] != -1)
                    chessboard[i+6][j].setOwner(new Piece(default_new_plan[i][j], true, false));
            }
        }
    }

    public String generate_my_plan(){
        String plan = "";
        for (int i = 0;i < 6;i++){
            for (int j =  0;j < 5;j++){
                Piece p = chessboard[i+6][j].getOwner();
                if (p == null){
                    plan += 'a';
                }else{
                    plan += (char)('b'+p.getLevel());
                }
            }
        }
        return plan;
    }

    public void place_enemy(String plan){
        for (int i = 0;i < 6;i++){
            for (int j =  0;j < 5;j++){
                int level = plan.charAt(i*5+j) - 'b';
                if (level != -1){
                    chessboard[5 - i][4 - j].setOwner(new Piece(level, false, false));
                }
            }
        }
    }

    public void clear_foreground(){
        for (int i = 0;i < 12;i++){
            for (int j = 0;j < 5;j++){
                chessboard[i][j].setForeground(null);
            }
        }
    }

}
