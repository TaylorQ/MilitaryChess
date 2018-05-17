package com.example.taylorq.militarychess;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

public class Field extends AppCompatImageView {

    public static final int TYPE_NORMAL = 1;
    public static final int TYPE_RAIL = 2;
    public static final int TYPE_CAMP = 3;
    public static final int TYPE_BASECAMP = 4;

    private int type;
    private Piece owner = null;
    private int row,column;

    private String owner_name;

    private boolean selected = false;

    public Field(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
   }

   public Field(Context context, AttributeSet attrs) {
       super(context, attrs);
    }

    public Field(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setColor(getResources().getColor(R.color.ally));
        if (owner != null&&!owner.isMine()){
            paint.setColor(getResources().getColor(R.color.enemy));
        }

        float textSize = canvas.getHeight()/5;
        paint.setTextSize(textSize);

        String text;
        if (owner == null){
            text = "";
        }else{
            if (owner.isMine()){
                text = owner_name;
                if (owner.isVisible()){
                    text = "["+text+"]";
                }
            }else{
                if (owner.isVisible()) {
                    text = owner_name;
                }else{
                    text = "敌军";
                }
            }
        }

        canvas.drawText(text, (canvas.getWidth()-paint.measureText(text))/2, (canvas.getHeight()-textSize)*2/3, paint);
    }

    public void refresh(){
        if (owner == null){
            setImageDrawable(null);
        }else{
            setImageResource(R.drawable.piece);
        }

        if (selected){
            setForeground(getResources().getDrawable(R.drawable.field_mark));
        }else{
            setForeground(null);
        }
        invalidate();
    }

    public void red_mark(){
        setForeground(getResources().getDrawable(R.drawable.field_mark_red));
    }

    public void expose(){
        if (owner != null&&!owner.isVisible()){
            owner.turnover();
            refresh();
        }
    }

    public void setSelected(boolean s){
        selected = s;
        refresh();
    }

    public void setType(int t){
        type = t;
    }

    public int getType(){
        return type;
    }

    public void setOwner(Piece new_owner){
        owner = new_owner;
        if (owner != null){
            owner_name = Constants.p_name[owner.getLevel()];
            this.setImageResource(R.drawable.piece);
        }else{
            owner_name = "";
            this.setImageDrawable(null);
        }
        refresh();
    }

    public Piece getOwner(){
        return owner;
    }

    public void setPosition(int r, int c){
        row = r;
        column = c;
    }

    public int getRow(){
        return row;
    }

    public int getColumn(){
        return column;
    }

}
