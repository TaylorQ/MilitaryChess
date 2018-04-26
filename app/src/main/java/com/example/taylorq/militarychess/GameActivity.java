package com.example.taylorq.militarychess;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class GameActivity extends AppCompatActivity {

    //是否是新玩法
    boolean newrule;
    //棋盘实例
    private ChessBoard chessBoard;
    //蓝牙服务类
    private BluetoothService bluetoothService;
    //右侧控件
    Button plan_finish;
    TextView missile_cd;
    TextView enemy_move;
    TextView title;

    //状态标识
    private int stage;
    private static final int STAGE_PLANNING     = 0;
    private static final int STAGE_PLAN_FINISH  = 1;
    private static final int STAGE_ENEMY_FINISH = 2;
    private static final int STAGE_MY_ROUND     = 3;
    private static final int STAGE_ENEMYROUND   = 4;

    //已选择Field
    private Field selected = null;
    private Field clickedField = null;
    //导弹cd
    private int missile_cd_count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
    }

    @Override
    protected void onStart(){
        super.onStart();

        setup();
    }

    private void setup(){

        bluetoothService = (BluetoothService)getApplication();
        bluetoothService.setmHandler(mHandler);
        newrule = getIntent().getBooleanExtra("newrule", false);

        chessBoard = new ChessBoard(this, fieldClickListener);

        plan_finish = findViewById(R.id.plan_finish);
        plan_finish.setOnClickListener(pfClickListener);

        title = findViewById(R.id.title);
        missile_cd = findViewById(R.id.Missile_CD);
        enemy_move = findViewById(R.id.enemy_move);

        if (newrule){
            chessBoard.place_my_newpiece();
            missile_cd.setText("√");
        }else{
            chessBoard.place_my_classicpiece();
            missile_cd.setText("X");
        }

        stage = STAGE_PLANNING;
        updateTitle();
    }

    private View.OnClickListener pfClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Button pf = (Button)v;
            if (stage == STAGE_PLANNING){
                pf.setText("更改");
                stage = STAGE_PLAN_FINISH;
                updateTitle();
                String Msg = Constants.ready + "|";
                sendMessage(Msg);
            }else if (stage == STAGE_PLAN_FINISH){
                pf.setText("完成");
                stage = STAGE_PLANNING;
                String Msg = Constants.cancel_ready + "|";
                updateTitle();
            }else if (stage == STAGE_ENEMY_FINISH){
                pf.setText("投降");
                String Msg = Constants.my_plan + "|";
                Msg += chessBoard.generate_my_plan();
                sendMessage(Msg);
            }else if (stage == STAGE_MY_ROUND || stage == STAGE_ENEMYROUND){
                surrender();
            }
        }
    };

    private View.OnClickListener fieldClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (stage == STAGE_PLANNING||stage == STAGE_ENEMY_FINISH){
                clickedField = (Field)v;
                fieldClick_planning();
            }else if (stage == STAGE_MY_ROUND){
                clickedField = (Field)v;
                fieldClick_myround();
            }
        }
    };

    private void fieldClick_planning(){
        if (clickedField.getOwner() == null) //布局阶段只能点击有棋子的Field
            return;

        if (newrule){//新玩法的布局规则
            if (selected == null){//选中
                selected = clickedField;
                selected.setSelected(true);
            }else{
                if (selected.equals(clickedField)){//取消选中
                    selected.setSelected(false);
                    selected = null;
                    return;
                }
                if (selected.getOwner().getLevel() == Constants.commander){//司令部只能和导弹互换位置
                    if (clickedField.getOwner().getLevel() != Constants.missile){
                        return;
                    }
                }
                if (selected.getOwner().getLevel() == Constants.missile){
                    if (clickedField.getOwner().getLevel() != Constants.commander){
                        return;
                    }
                }
                Piece temp = selected.getOwner();
                selected.setOwner(clickedField.getOwner());
                clickedField.setOwner(temp);
                selected.setSelected(false);
                selected = null;
            }
        }else{//经典玩法的布局规则
            if (selected == null){//选中
                selected = clickedField;
                selected.setSelected(true);
            }else{
                if (selected.equals(clickedField)){//取消选中
                    selected.setSelected(false);
                    selected = null;
                    return;
                }

                if (selected.getOwner().getLevel() == Constants.junqi){//军旗只能放在大本营
                    if (clickedField.getRow() != 11)
                        return;
                    if (clickedField.getColumn() != 1&&clickedField.getColumn() != 3)
                        return;
                }else if(clickedField.getOwner().getLevel() == Constants.junqi){
                    if (selected.getRow() != 11)
                        return;
                    if (selected.getColumn() != 1&&selected.getColumn() != 3)
                        return;
                }

                else if (selected.getOwner().getLevel() == Constants.zhadan){//炸弹不能放第一排
                    if (clickedField.getRow() == 6)
                        return;
                }else if (clickedField.getOwner().getLevel() == Constants.zhadan){
                    if (selected.getRow() == 6)
                        return;
                }

                else if (selected.getOwner().getLevel() == Constants.dilei){//地雷只能放后两排
                    if (clickedField.getRow() < 10)
                        return;
                }else if (clickedField.getOwner().getLevel() == Constants.dilei){
                    if (selected.getRow() < 10)
                        return;
                }
                Piece temp = selected.getOwner();
                selected.setOwner(clickedField.getOwner());
                clickedField.setOwner(temp);
                selected.setSelected(false);
                selected = null;
            }
        }
    }

    private void fieldClick_myround(){
        if (selected == null){//选中
            if (clickedField.getOwner() != null&&clickedField.getOwner().isMine()){//需选中自己的棋子
                if (clickedField.getType() == Field.TYPE_BASECAMP//不能选大本营
                        &&clickedField.getOwner().getLevel() != Constants.missile)return;//除非是导弹
                if (clickedField.getOwner().getLevel() == Constants.dilei)return;//不能选地雷
                if (clickedField.getOwner().getLevel() == Constants.junqi)return;//不能选军旗
                if (clickedField.getOwner().getLevel() == Constants.commander)return;//不能选司令部
                if (clickedField.getOwner().getLevel() == Constants.antiaircraft)return;//不能选防空装置
                if (clickedField.getOwner().getLevel() == Constants.spy){//选了自家间谍
                    if (clickedField.getRow() < 6&&clickedField.getType() == Field.TYPE_CAMP){//正在对面的行营中
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("间谍")
                                .setMessage("这个间谍可以发动，要发动吗？")
                                .setPositiveButton("去吧！", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        chessBoard.chessboard[clickedField.getRow()-1][clickedField.getColumn()+1].expose();
                                        chessBoard.chessboard[clickedField.getRow()-1][clickedField.getColumn()-1].expose();
                                        chessBoard.chessboard[clickedField.getRow()-1][clickedField.getColumn()].expose();
                                        chessBoard.chessboard[clickedField.getRow()+1][clickedField.getColumn()+1].expose();
                                        chessBoard.chessboard[clickedField.getRow()+1][clickedField.getColumn()-1].expose();
                                        chessBoard.chessboard[clickedField.getRow()+1][clickedField.getColumn()].expose();
                                        chessBoard.chessboard[clickedField.getRow()][clickedField.getColumn()+1].expose();
                                        chessBoard.chessboard[clickedField.getRow()][clickedField.getColumn()-1].expose();
                                        chessBoard.chessboard[clickedField.getRow()][clickedField.getColumn()].expose();
                                        String Msg = Constants.expose + "|";
                                        Msg += (char)('a' + clickedField.getRow());
                                        Msg += (char)('a' + clickedField.getColumn());
                                        sendMessage(Msg);
                                        stage = STAGE_ENEMYROUND;
                                        updateTitle();
                                        chessBoard.clear_foreground();
                                    }
                                })
                                .setNegativeButton("再等等", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        selected = clickedField;
                                        selected.setSelected(true);
                                    }
                                });
                        builder.create().show();
                        return;
                    }
                }
                selected = clickedField;
                selected.setSelected(true);
                return;
            }
        }else{
            if (selected.equals(clickedField)){//取消选中
                selected.setSelected(false);
                selected = null;
                return;
            }else if (clickedField.getOwner() != null){//点中棋子
                if (clickedField.getOwner().isMine()){//点中自己棋子，更改选中
                    if (clickedField.getType() == Field.TYPE_BASECAMP//不能选大本营
                            &&clickedField.getOwner().getLevel() != Constants.missile)return;//除非是导弹
                    if (clickedField.getOwner().getLevel() == Constants.dilei)return;//不能选地雷
                    if (clickedField.getOwner().getLevel() == Constants.junqi)return;//不能选军旗
                    if (clickedField.getOwner().getLevel() == Constants.commander)return;//不能选司令部
                    if (clickedField.getOwner().getLevel() == Constants.antiaircraft)return;//不能选防空装置
                    selected.setSelected(false);
                    selected = clickedField;
                    selected.setSelected(true);
                }else{//选中敌方棋子
                    if (selected.getOwner().getLevel() == Constants.missile){//发射导弹
                        if (missile_cd_count != 0){//导弹还在冷却
                            Toast.makeText(getApplicationContext(), "我方导弹还未装填完毕。", Toast.LENGTH_LONG).show();
                        }else{//导弹可以发射
                            if (clickedField.getRow() < 6){//只能打在对岸
                                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                                builder.setTitle("发射导弹")
                                        .setMessage("确定要发射导弹攻击("+clickedField.getRow()+","+clickedField.getColumn()+")吗？")
                                        .setPositiveButton("射！", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                String Msg = Constants.shoot+"|";
                                                Msg += (char)('a' + clickedField.getRow());
                                                Msg += (char)('a' + clickedField.getColumn());
                                                if (shoot(clickedField)){
                                                    if (clickedField.getOwner().getLevel() == Constants.commander){
                                                        Msg = Constants.win + "|";
                                                        sendMessage(Msg);
                                                        endGame(true);
                                                        return;
                                                    }
                                                    Msg += 'b';
                                                    clickedField.setOwner(null);
                                                }else{
                                                    Msg += 'a';
                                                }
                                                sendMessage(Msg);
                                                missile_cd_count = 5;//冷却五回合
                                                missile_cd.setText(":5");

                                                clickedField = null;
                                                selected.setSelected(false);
                                                selected = null;
                                                stage = STAGE_ENEMYROUND;
                                                updateTitle();
                                                chessBoard.clear_foreground();
                                            }
                                        })
                                        .setNegativeButton("不了", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        });
                                builder.create().show();
                                return;
                            }
                        }
                    }
                    if (clickedField.getType() == Field.TYPE_CAMP){//敌方行营无敌
                        if (!(clickedField.getOwner().getLevel() == Constants.spy
                                &&clickedField.getOwner().isVisible()))return;//除非里面是个暴露了的间谍
                    }
                    if (piece_move(selected, clickedField)){//移动成功，进行战斗
                        int result = selected.getOwner().fight(clickedField.getOwner());
                        if (result == 0){//战斗失败，牺牲
                        }else if (result == 1){//击杀敌人
                            clickedField.setOwner(selected.getOwner());
                        }else if (result == 2){//同归于尽
                            clickedField.setOwner(null);
                        }else if (result == 3){//赢了
                            String Msg = Constants.win+"|";
                            sendMessage(Msg);
                            endGame(true);
                            return;
                        }
                        String Msg = Constants.move+"|";
                        Msg += (char)(selected.getRow() + 'a');
                        Msg += (char)(selected.getColumn() + 'a');
                        Msg += (char)(clickedField.getRow() + 'a');
                        Msg += (char)(clickedField.getColumn() + 'a');
                        Msg += (char)(result + 'a');
                        sendMessage(Msg);
                        selected.setOwner(null);
                        selected.setSelected(false);
                        selected = null;
                        stage = STAGE_ENEMYROUND;
                        updateTitle();
                        chessBoard.clear_foreground();
                    }else{//无法完成移动
                        return;
                    }
                }
            }else{//点中空地
                if (piece_move(selected, clickedField)){//
                    String Msg = Constants.move+"|";
                    Msg += (char)(selected.getRow() + 'a');
                    Msg += (char)(selected.getColumn() + 'a');
                    Msg += (char)(clickedField.getRow() + 'a');
                    Msg += (char)(clickedField.getColumn() + 'a');
                    Msg += 'b';
                    sendMessage(Msg);
                    clickedField.setOwner(selected.getOwner());
                    selected.setOwner(null);
                    selected.setSelected(false);
                    selected = null;
                    stage = STAGE_ENEMYROUND;
                    updateTitle();
                    chessBoard.clear_foreground();
                }else{//无法完成移动
                    return;
                }
            }
        }
    }

    private boolean shoot(Field target){
        int tx = target.getRow();
        int ty = target.getColumn();
        if (tx != 11&&ty != 4
                &&chessBoard.chessboard[tx+1][ty+1].getOwner() != null
                &&chessBoard.chessboard[tx+1][ty+1].getOwner().getLevel() == Constants.antiaircraft)
            return false;
        if (tx != 11&&ty != 0
                &&chessBoard.chessboard[tx+1][ty-1].getOwner() != null
                &&chessBoard.chessboard[tx+1][ty-1].getOwner().getLevel() == Constants.antiaircraft)
            return false;
        if (tx != 11
                &&chessBoard.chessboard[tx+1][ty].getOwner() != null
                &&chessBoard.chessboard[tx+1][ty].getOwner().getLevel() == Constants.antiaircraft)
            return false;
        if (ty != 4
                &&chessBoard.chessboard[tx][ty+1].getOwner() != null
                &&chessBoard.chessboard[tx][ty+1].getOwner().getLevel() == Constants.antiaircraft)
            return false;
        if (ty != 0
                &&chessBoard.chessboard[tx][ty-1].getOwner() != null
                &&chessBoard.chessboard[tx][ty-1].getOwner().getLevel() == Constants.antiaircraft)
            return false;
        if (tx != 0&&ty != 4
                &&chessBoard.chessboard[tx-1][ty+1].getOwner() != null
                &&chessBoard.chessboard[tx-1][ty+1].getOwner().getLevel() == Constants.antiaircraft)
            return false;
        if (tx != 0&&ty != 0
                &&chessBoard.chessboard[tx-1][ty-1].getOwner() != null
                &&chessBoard.chessboard[tx-1][ty-1].getOwner().getLevel() == Constants.antiaircraft)
            return false;
        if (tx != 0
                &&chessBoard.chessboard[tx-1][ty].getOwner() != null
                &&chessBoard.chessboard[tx-1][ty].getOwner().getLevel() == Constants.antiaircraft)
            return false;

        return true;
    }

    private boolean piece_move(Field from, Field to){
        int xmove = to.getRow() - from.getRow();
        int ymove = to.getColumn() - from .getColumn();
        if (xmove < 2&&xmove > -2&&ymove < 2&&ymove > -2){//单步移动
            if (xmove == -1&&ymove == 0){
                return ChessBoard.map[from.getRow()][from.getColumn()][0] != 0;
            }
            if (xmove == -1&&ymove == 1){
                return ChessBoard.map[from.getRow()][from.getColumn()][1] != 0;
            }
            if (xmove == 0&&ymove == 1){
                return ChessBoard.map[from.getRow()][from.getColumn()][2] != 0;
            }
            if (xmove == 1&&ymove == 1){
                return ChessBoard.map[from.getRow()][from.getColumn()][3] != 0;
            }
            if (xmove == 1&&ymove == 0){
                return ChessBoard.map[from.getRow()][from.getColumn()][4] != 0;
            }
            if (xmove == 1&&ymove == -1){
                return ChessBoard.map[from.getRow()][from.getColumn()][5] != 0;
            }
            if (xmove == 0&&ymove == -1){
                return ChessBoard.map[from.getRow()][from.getColumn()][6] != 0;
            }
            if (xmove == -1&&ymove == -1){
                return ChessBoard.map[from.getRow()][from.getColumn()][7] != 0;
            }
        }else if (from.getType() == Field.TYPE_RAIL&&to.getType() == Field.TYPE_RAIL){//铁轨移动
            switch (from.getOwner().getLevel()){
                case Constants.gongbing:
                case Constants.engineer:
                    return fast_rail_move(from, to, new ArrayList<Field>());
                default:
                    return rail_move(from, to);
            }
        }else{
            return false;
        }
        return false;
    }

    private boolean fast_rail_move(Field from, Field to, ArrayList<Field> route){
        route.add(from);
        int fx = from.getRow();
        int fy = from.getColumn();
        int xmove = -1;int ymove = 0;//向上
        if (fx != 0//防止越界
                &&!route.contains(chessBoard.chessboard[fx+xmove][fy+ymove])){//还没走过
            if (ChessBoard.map[fx][fy][0] == 2){//铁轨通向
                if (chessBoard.chessboard[fx+xmove][fy+ymove].equals(to)){//就是目的地，返回成功
                    return true;
                }else if (chessBoard.chessboard[fx+xmove][fy+ymove].getOwner() == null){//无人，可以走
                    if (fast_rail_move(chessBoard.chessboard[fx+xmove][fy+ymove], to, route)){//递归走
                        return true;
                    }
                }
            }
        }
        xmove = -1;ymove = 1;//向右上
        if (fx != 0&&fy != 4
                &&!route.contains(chessBoard.chessboard[fx+xmove][fy+ymove])){
            if (ChessBoard.map[fx][fy][1] == 2){
                if (chessBoard.chessboard[fx+xmove][fy+ymove].equals(to)){
                    return true;
                }else if (chessBoard.chessboard[fx+xmove][fy+ymove].getOwner() == null){
                    if (fast_rail_move(chessBoard.chessboard[fx+xmove][fy+ymove], to, route)){
                        return true;
                    }
                }
            }
        }
        xmove = 0;ymove = 1;//向右
        if (fy != 4
                &&!route.contains(chessBoard.chessboard[fx+xmove][fy+ymove])){
            if (ChessBoard.map[fx][fy][2] == 2){
                if (chessBoard.chessboard[fx+xmove][fy+ymove].equals(to)){
                    return true;
                }else if (chessBoard.chessboard[fx+xmove][fy+ymove].getOwner() == null){
                    if (fast_rail_move(chessBoard.chessboard[fx+xmove][fy+ymove], to, route)){
                        return true;
                    }
                }
            }
        }
        xmove = 1;ymove = 1;//向右下
        if (fx != 11&&fy != 4
                &&!route.contains(chessBoard.chessboard[fx+xmove][fy+ymove])){
            if (ChessBoard.map[fx][fy][3] == 2){
                if (chessBoard.chessboard[fx+xmove][fy+ymove].equals(to)){
                    return true;
                }else if (chessBoard.chessboard[fx+xmove][fy+ymove].getOwner() == null){
                    if (fast_rail_move(chessBoard.chessboard[fx+xmove][fy+ymove], to, route)){
                        return true;
                    }
                }
            }
        }
        xmove = 1;ymove = 0;//向下
        if (fx != 11
                &&!route.contains(chessBoard.chessboard[fx+xmove][fy+ymove])){
            if (ChessBoard.map[fx][fy][4] == 2){
                if (chessBoard.chessboard[fx+xmove][fy+ymove].equals(to)){
                    return true;
                }else if (chessBoard.chessboard[fx+xmove][fy+ymove].getOwner() == null){
                    if (fast_rail_move(chessBoard.chessboard[fx+xmove][fy+ymove], to, route)){
                        return true;
                    }
                }
            }
        }
        xmove = 1;ymove = -1;//向左下
        if (fx != 11&&fy != 0
                &&!route.contains(chessBoard.chessboard[fx+xmove][fy+ymove])){
            if (ChessBoard.map[fx][fy][5] == 2){
                if (chessBoard.chessboard[fx+xmove][fy+ymove].equals(to)){
                    return true;
                }else if (chessBoard.chessboard[fx+xmove][fy+ymove].getOwner() == null){
                    if (fast_rail_move(chessBoard.chessboard[fx+xmove][fy+ymove], to, route)){
                        return true;
                    }
                }
            }
        }
        xmove = 0;ymove = -1;//向左
        if (fy != 0
                &&!route.contains(chessBoard.chessboard[fx+xmove][fy+ymove])){
            if (ChessBoard.map[fx][fy][6] == 2){
                if (chessBoard.chessboard[fx+xmove][fy+ymove].equals(to)){
                    return true;
                }else if (chessBoard.chessboard[fx+xmove][fy+ymove].getOwner() == null){
                    if (fast_rail_move(chessBoard.chessboard[fx+xmove][fy+ymove], to, route)){
                        return true;
                    }
                }
            }
        }
        xmove = -1;ymove = -1;//向左上
        if (fx != 0&&fy != 0
                &&!route.contains(chessBoard.chessboard[fx+xmove][fy+ymove])){
            if (ChessBoard.map[fx][fy][7] == 2){
                if (chessBoard.chessboard[fx+xmove][fy+ymove].equals(to)){
                    return true;
                }else if (chessBoard.chessboard[fx+xmove][fy+ymove].getOwner() == null){
                    if (fast_rail_move(chessBoard.chessboard[fx+xmove][fy+ymove], to, route)){
                        return true;
                    }
                }
            }
        }
        route.remove(from);
        return false;
    }

    private boolean rail_move(Field from, Field to){
        int fx = from.getRow();
        int fy = from.getColumn();
        if (fx == 1||fx == 5||fx == 6||fx == 10){//横轨
            if (fx == to.getRow()){//在同一条铁轨
                int i = (fy>to.getColumn()?to.getColumn():fy)+1;
                int j = (fy<to.getColumn()?to.getColumn():fy);
                for(;i < j;i++){
                    if (chessBoard.chessboard[fx][i].getOwner() != null)
                        return false;
                }
                return true;
            }
        }
        if (fy == 0||fy == 4){//竖轨
            if (fy == to.getColumn()){//在同一条铁轨
                int i = (fx>to.getRow()?to.getRow():fx)+1;
                int j = (fx<to.getRow()?to.getRow():fx);
                for(;i < j;i++){
                    if (chessBoard.chessboard[i][fy].getOwner() != null)
                        return false;
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 处理来自BluetoothChatService消息的handler
     */
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            endGame(true);
                            break;
                    }
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    recieve(readMessage);
                    break;
                case Constants.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString("toast"),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private void recieve(String read){
        String[] msg = read.split("\\|");
        switch (msg[0]){
            case Constants.ready:
                stage = STAGE_ENEMY_FINISH;
                updateTitle();
                plan_finish.setText("开始");
                break;
            case Constants.cancel_ready:
                stage = STAGE_PLANNING;
                updateTitle();
                break;
            case Constants.my_plan:
                chessBoard.place_enemy(msg[1]);
                if (stage == STAGE_PLAN_FINISH){
                    String Msg = Constants.my_plan+"|"+chessBoard.generate_my_plan();
                    sendMessage(Msg);
                    stage = STAGE_MY_ROUND;
                    plan_finish.setText("投降");
                    updateTitle();
                }else if (stage == STAGE_ENEMY_FINISH){
                    stage = STAGE_ENEMYROUND;
                    updateTitle();
                    chessBoard.clear_foreground();
                }
                break;
            case Constants.move:
                String move_str = chessBoard.enemy_move(msg[1]);
                enemy_move.setText(move_str);
                stage = STAGE_MY_ROUND;
                updateTitle();
                if (missile_cd_count != 0){
                    missile_cd_count--;
                    if (missile_cd_count == 0){
                        missile_cd.setText("√");
                    }else{
                        missile_cd.setText(":"+missile_cd_count);
                    }
                }

                break;
            case Constants.shoot:
                enemy_move.setText(chessBoard.boming(msg[1]));
                stage = STAGE_MY_ROUND;
                updateTitle();
                if (missile_cd_count != 0){
                    missile_cd_count--;
                    if (missile_cd_count == 0){
                        missile_cd.setText("√");
                    }else{
                        missile_cd.setText(":"+missile_cd_count);
                    }
                }
                break;
            case Constants.expose:
                enemy_move.setText(chessBoard.spy_activate(msg[1]));
                stage = STAGE_MY_ROUND;
                updateTitle();
                if (missile_cd_count != 0){
                    missile_cd_count--;
                    if (missile_cd_count == 0){
                        missile_cd.setText("√");
                    }else{
                        missile_cd.setText(":"+missile_cd_count);
                    }
                }
                break;
            case Constants.win:
                endGame(false);
                break;
            case Constants.surrender:
                endGame(true);
                break;
        }
    }

    /**
     * 发送消息
     *
     * @param message 需要发送的消息
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (bluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(getApplicationContext(), "尚未和任何人连接", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            bluetoothService.write(send);
        }
    }

    private void surrender(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("投降")
                .setMessage("真的不再反抗一下？")
                .setPositiveButton("投了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String Msg = Constants.surrender + "|";
                        sendMessage(Msg);
                        endGame(false);
                    }
                })
                .setNegativeButton("再试试", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "加油！", Toast.LENGTH_SHORT).show();
                    }
                });
        builder.create().show();
    }

    private void endGame(boolean win){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("游戏结束")
                .setMessage(win?"你赢了！":"你输了……")
                .setPositiveButton("好的吧", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                finish();
            }
        });
        builder.create().show();
    }

    private void updateTitle(){
        String title = "";
        switch (stage){
            case STAGE_PLANNING:
                title = "请调整你的阵型。";
                break;
            case STAGE_PLAN_FINISH:
                title = "等待对方开始";
                break;
            case STAGE_ENEMY_FINISH:
                title = "对方已准备就绪";
                break;
            case STAGE_ENEMYROUND:
                title = "对方回合";
                break;
            case STAGE_MY_ROUND:
                title = "我的回合";
                break;
        }
        this.title.setText(title);
    }
}
