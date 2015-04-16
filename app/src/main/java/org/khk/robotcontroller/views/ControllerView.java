package org.khk.robotcontroller.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import org.khk.robotcontroller.ControllerActivity;
import org.khk.robotcontroller.MotherFuckingGlobals;

import java.util.Calendar;

/**
 * Created by Joe Dailey on 4/12/2015.
 */
public class ControllerView extends View{

    private ControllerActivity parentActivity;
    private Context context;
    private Paint brush;

    private V_Throttle vKnob;
    private H_Throttle hKnob;

    private Long fireCheck1;
    private Long fireCheck2;
    private final Long MAX_CLICK_TIME = 200L;

    private void init(Context context){
        this.context = context;
        this.brush = new Paint();
        this.vKnob = new V_Throttle(this);
        this.hKnob = new H_Throttle(this);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        brush.setColor(Color.CYAN);

        vKnob.draw(canvas);
        hKnob.draw(canvas);
    }

    public ControllerView(Context context) {
        super(context);
        init(context);
    }
    public ControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public ControllerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch(ev.getAction() & MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_DOWN:
                fireCheck1 = Calendar.getInstance().getTimeInMillis();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                fireCheck2 = Calendar.getInstance().getTimeInMillis();
                break;
            case MotionEvent.ACTION_UP: {
                long clickDuration = Calendar.getInstance().getTimeInMillis() - fireCheck1;
                if (clickDuration < MAX_CLICK_TIME) {
                    Toast.makeText(getContext(), "Fire!", Toast.LENGTH_SHORT).show();
                    fire();
                }
            }
                break;
            case MotionEvent.ACTION_POINTER_UP: {
                long clickDuration = Calendar.getInstance().getTimeInMillis() - fireCheck2;
                if (clickDuration < MAX_CLICK_TIME) {
                    Toast.makeText(getContext(), "Fire!", Toast.LENGTH_SHORT).show();
                    fire();
                }
            }
                break;
        }

        vKnob.trackTouch(ev);
        hKnob.trackTouch(ev);
        invalidate();
        return true;
    }

    public void fire(){
        MotherFuckingGlobals.fucker.serialSend("z");
        Log.d("Control Key", "z");
    }

    private class H_Throttle{
        private Paint brush;
        public float knubPosition;// -1.0 - 1.0
        public int padding;
        public float trackWidth;
        private int touchID;
        public ControllerView parent;
        private final long WAIT_DURATION = 100L;

        private Long lastSend;

        public H_Throttle(ControllerView parent){
            this.brush = new Paint();
            this.knubPosition = 0.5f;
            this.padding = 10;
            this.touchID = -1;
            this.parent = parent;
            lastSend = Calendar.getInstance().getTimeInMillis();
        }

        public void draw(Canvas canvas){

            int width = getWidth();
            int halfWidth = width/2;
            int height = getHeight();

            brush.setColor(Color.BLACK);
            brush.setStrokeCap(Paint.Cap.ROUND);
            brush.setStrokeWidth(15);

            canvas.drawLine(halfWidth+padding, height/2, halfWidth+width/2-padding, height/2, brush);

            trackWidth = (width/2-2*padding);
            canvas.drawCircle(halfWidth+padding+knubPosition*trackWidth, height/2, 20, brush);

        }

        public void moveKnob(float x){
            if(x < getWidth()/2+padding)
                x = getWidth()/2+padding;
            if(x > getWidth()-padding)
                x = getWidth()-padding;

            float realXonTrack = (x - (getWidth()/2+padding));
            knubPosition = realXonTrack/trackWidth;

            char out;
            double numba = (knubPosition*20);
            out = (char) ('A' + (char)numba);

            if(Calendar.getInstance().getTimeInMillis() - lastSend > WAIT_DURATION) {
                lastSend = Calendar.getInstance().getTimeInMillis();
                MotherFuckingGlobals.fucker.serialSend(""+out);
                Log.d("Control Key", ""+out);
            }

        }

        public void trackTouch(MotionEvent ev) {
            int index = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;

            switch(ev.getAction() & MotionEvent.ACTION_MASK){
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    if( ev.getX(index) > getWidth()/2  && ev.getX(index) < getWidth() && touchID == -1){
                        touchID = ev.getPointerId(index);
                        moveKnob(ev.getX(index));
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if( touchID != -1){
                        moveKnob(ev.getX(ev.findPointerIndex(touchID)));
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    if( ev.getPointerId(index) == touchID ){
                        touchID = -1;
                        this.knubPosition = 0.5f;

                        MotherFuckingGlobals.fucker.serialSend("K");
                        MotherFuckingGlobals.fucker.serialSend("K");
                        MotherFuckingGlobals.fucker.serialSend("K");
                        MotherFuckingGlobals.fucker.serialSend("K");
                        MotherFuckingGlobals.fucker.serialSend("K");
                        MotherFuckingGlobals.fucker.serialSend("K");
                        Log.d("Control Key", "K");

                    }
                    break;
            }
        }
    }

    private class V_Throttle{
        private Paint brush;
        public float knubPosition;// 0.0 - 1.0
        public int padding;
        private float trackWidth;
        private int touchID;
        private Thread myRunner;
        public ControllerView parent;

        public V_Throttle(ControllerView parent){
            this.brush = new Paint();
            this.knubPosition = 0.5f;
            this.padding = 10;
            touchID = -1;
            this.parent = parent;

            myRunner = new Thread(new V_runner(this));
            myRunner.start();
        }

        public void draw(Canvas canvas){

            int width = getWidth();
            int height = getHeight();

            brush.setColor(Color.BLACK);
            brush.setStrokeCap(Paint.Cap.ROUND);
            brush.setStrokeWidth(15);

            canvas.drawLine(width/2/2, padding, width/2/2, height-padding, brush);

            trackWidth = (height-2*padding);
            canvas.drawCircle(width/2/2, padding+knubPosition*trackWidth, 20, brush);

        }

        public void moveKnob(float y){
            if(y < padding)
                y = padding;
            if(y > getHeight()-padding)
                y = getHeight()-padding;

            float realYonTrack = (y - (padding));
            knubPosition = realYonTrack/trackWidth;
        }

        public void trackTouch(MotionEvent ev) {
            int index = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;

            switch(ev.getAction() & MotionEvent.ACTION_MASK){
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    if( ev.getX(index) > 0  && ev.getX(index) < getWidth()/2 && touchID == -1){
                        touchID = ev.getPointerId(index);
                        moveKnob(ev.getY(index));
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if( touchID != -1){
                        moveKnob(ev.getY(ev.findPointerIndex(touchID)));
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    if( ev.getPointerId(index) == touchID ){
                        touchID = -1;
                        this.knubPosition = 0.5f;
                    }
                    break;
            }
        }


    }

    public class V_runner implements Runnable{
        private V_Throttle throttle;
        private final long WAIT_DURATION = 100L;

        private Long lastSend;

        public V_runner(V_Throttle that){
            this.throttle = that;
            lastSend = Calendar.getInstance().getTimeInMillis();
        }

        @Override
        public void run() {
            while(true) {
                if (Calendar.getInstance().getTimeInMillis() - lastSend > WAIT_DURATION) {
                    if(throttle.knubPosition == 0.5f && throttle.parent.hKnob.knubPosition == 0.5f){

                        lastSend = Calendar.getInstance().getTimeInMillis();
                        MotherFuckingGlobals.fucker.serialSend("k");
                        Log.d("Control Key", "k");
                    }else{
                        if(throttle.knubPosition != 0.5f) {
                            char out;
                            double numba = (throttle.knubPosition * 20);
                            out = (char) ('a' + (char) numba);

                            lastSend = Calendar.getInstance().getTimeInMillis();
                            MotherFuckingGlobals.fucker.serialSend("" + out);
                            Log.d("Control Key", "" + out);
                        }
                    }

                }
            }
        }
    }
}

