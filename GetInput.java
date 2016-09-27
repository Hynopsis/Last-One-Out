package com.rpetersen.game;

/**
 * Created by lastr on 4/11/2016.
 */

import java.util.HashMap;
import java.util.Map;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;

/*
 So this class detects up to five touches at the same time.  When we find one, we compare to
 our screen buttons to determine if they are being pressed.  This manages turning, gas, and the brakes
*/

public class GetInput implements InputProcessor {

    private boolean turningLeft = false;
    private boolean turningRight = false;
    private boolean accelerating = false;
    private boolean braking = false;

    private int leftIndex = -1;
    private int rightIndex = -1;
    private int accIndex = -1;
    private int brakeIndex = -1;

    private int minY;
    private int maxY;
    private int maxY1;

    MyGdxGame game;

    //integer is our finger, we support up to 5 finger touches
    private Map<Integer,TouchInfo> touches = new HashMap<Integer,TouchInfo>();

    //constuctor used just for setting reference to main script
    public GetInput(MyGdxGame game){

        this.game = game;
        //Gdx.app.log("Warn", "Initializing input ");
        minY = (Gdx.graphics.getHeight() - game.uiPaddingY);
        maxY = (Gdx.graphics.getHeight() - game.uiPaddingY) - game.uiMoveSize;
        maxY1 = (Gdx.graphics.getHeight() - game.uiPaddingY) - game.uiPedalSize;

        //initialed the touch classes
        for(int i = 0; i < 5; i++){
            touches.put(i, new TouchInfo());
        }
    }

    protected boolean isTurningLeft() {
        return turningLeft;
    }

    protected boolean isTurningRight() {
        return turningRight;
    }

    protected boolean isAccelerating() {
        return accelerating;
    }

    protected boolean isBraking() {
        return braking;
    }

    //simple classs for holding the position of the touched down
    class TouchInfo {
        public float touchX = 0;
        public float touchY = 0;
        public boolean touched = false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {

        if(pointer < 5){
            touches.get(pointer).touchX = screenX;
            touches.get(pointer).touchY = screenY;
            touches.get(pointer).touched = true;
        }

        //so finding if input is withing our buttons is kinda tricky, they used different
        //layout schemes...this input detect is from upper left corner being zero
        //drawing then has 0,0 being teh middle of the screen
        //so when user taps down we will grab world coordinates and compare to our ui items to see if they fall within it

        if( ((screenY < minY) && (screenY > maxY )) &&  ((screenX < (game.uiPaddingX + game.uiMoveSize)) && (screenX > game.uiPaddingX )) ) {
            // Gdx.app.log("Warn", "Touch is in LEFT button " + pointer);
            turningLeft = true;
            leftIndex = pointer;
        }

        if( ((screenY < minY) && (screenY > maxY )) &&  ((screenX < ((game.uiPaddingX + game.uiMoveSize) * 2)  ) && (screenX > (game.uiPaddingX + (game.uiPaddingX * 2)) )) ) {
            //Gdx.app.log("Warn", "Touch is in RIGHT button " + pointer);
            turningRight = true;
            rightIndex = pointer;
        }

        if( ((screenY < minY) && (screenY > maxY1 )) &&  ((screenX < (Gdx.graphics.getWidth() - game.uiPaddingX) ) && (screenX > (Gdx.graphics.getWidth() - game.uiPaddingX - game.uiPedalSize) )) ) {
            //Gdx.app.log("Warn", "Touch is in GAS button");
            accelerating = true;
            accIndex = pointer;
        }

        if( ((screenY < minY) && (screenY > maxY1 )) &&  ((screenX < (Gdx.graphics.getWidth() - (game.uiPaddingX * 2) - game.uiPedalSize ) && (screenX > (Gdx.graphics.getWidth() - game.uiPaddingX * 2 - game.uiPedalSize * 2  ))) )) {
            //Gdx.app.log("Warn", "Touch is in BRAKE button");
            braking = true;
            brakeIndex = pointer;
        }

        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if(pointer < 5){
            touches.get(pointer).touchX = 0;
            touches.get(pointer).touchY = 0;
            touches.get(pointer).touched = false;
        }

        if(isTurningLeft() && (pointer == leftIndex) ) {
            // Gdx.app.log("Warn", "Done turning left");
            turningLeft = false;
            leftIndex = -1;
        }

        if(isTurningRight() && (pointer == rightIndex) ) {
            //Gdx.app.log("Warn", "Done turning right");
            turningRight = false;
            rightIndex = -1;
        }

        if( isAccelerating() && (pointer == accIndex) ) {
            //Gdx.app.log("Warn", "Done accelerating");
            accelerating = false;
            accIndex = -1;
        }

        if( isBraking() && (pointer == brakeIndex)) {
            //Gdx.app.log("Warn", "Done braking");
            braking = false;
            brakeIndex = -1;
        }
        //Gdx.app.log("Warn", "Finding touch up " + screenX + " " + screenY + " " + pointer );
        return true;
    }

    @Override
    public boolean keyDown(int keycode) {
        //Gdx.app.log("Warn", "Finding key down " + keycode );
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }
    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        // TODO Auto-generated method stub
        return false;
    }



}

