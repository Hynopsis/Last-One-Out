package com.rpetersen.game;

/**
 * Created by lastr on 4/11/2016.
 */

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
//import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
//import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
//import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.profiling.GLProfiler;

//ApplicationListener,
public class GetInput implements InputProcessor {

    public boolean turningLeft = false;
    public boolean turningRight = false;
    public boolean accelerating = false;
    public boolean braking = false;

    public int leftIndex = -1;
    public int rightIndex = -1;
    public int accIndex = -1;
    public int brakeIndex = -1;
    MyGdxGame game;

    //constuctor used djust for setting refence to main script
    public GetInput(MyGdxGame game){

        this.game = game;
        Gdx.app.log("Warn", "Initializing input ");

        //Gdx.input.setInputProcessor(this);
        // GLProfiler.enable();				//enable profiling
        //initialed the touch classes
        for(int i = 0; i < 5; i++){
            touches.put(i, new TouchInfo());
        }

        Gdx.app.log("Warn", "Called create ");
    }
    //states of different actions


    //simple classs for holding the position of the touched down
    class TouchInfo {
        public float touchX = 0;
        public float touchY = 0;
        public boolean touched = false;
    }

    //integer is our finger, we support up to 5 finger touches
    private Map<Integer,TouchInfo> touches = new HashMap<Integer,TouchInfo>();
    /*
        @Override
        public void create () {
            //assing this as an genereal input processor to start collecting info
            //if we want several methods then there is a method to mix input and stack input for priority
            //should we be calling this here? Does it matter?
            Gdx.input.setInputProcessor(this);
           // GLProfiler.enable();				//enable profiling
            //initialed the touch classes
            for(int i = 0; i < 5; i++){
                touches.put(i, new TouchInfo());
            }

            Gdx.app.log("Warn", "Called create ");
        }
        */
    /*
    @Override
    public void render() {
        //for normal mouse click , use right for right click
        /*
        if(Gdx.input.isKeyPressed(Input.Keys.LEFT)){
            camera.unproject(touchpoint.set(Gdx.input.getX(),Gdx.input.getY(),0);
            if(balloon.getBoundingRectangles().contains(touchPoint.x,touchPoint.y))
            {
                // will be here when balloon will be touched
            }
        }



    }
    */
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

        //so for example our left turn button, which is teh first button on the lower left
        // (Gdx.graphics.getHeight() - game.uiPaddingY - 100 ,game.uiPaddingX)..upper left corner of 1st image
        /*
		so when user taps down we will grab world coordinates and compare to our ui items to see if they fall within it
        if( ((Gdx.graphics.getHeight() - game.uiPaddingY) > screenY) {
            Gdx.app.log("Warn", "Touch is right y area");
            }
		if( ((wc.x < gPox.x + 100) && (wc.x > gPos.x)) && ((wc.z < gPox.z + 100) && (wc.z > gPos.z))){
			//then our click is within the green button image and we should give gas

		}
		*/
        int minY = (Gdx.graphics.getHeight() - game.uiPaddingY);
        int maxY = (Gdx.graphics.getHeight() - game.uiPaddingY) - game.uiMoveSize;
        int maxY1 = (Gdx.graphics.getHeight() - game.uiPaddingY) - game.uiPedalSize;
        //int minX = (Gdx.graphics.getHeight() - game.uiPaddingX);
        //int maxX = (Gdx.graphics.getHeight() - game.uiPaddingX) - game.uiMoveSize;

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


        // Gdx.app.log("Warn", "Finding touch down " + screenX + " " + screenY + " " + pointer );
        //Gdx.app.log("Warn", "Want between  " + minY + " and " + maxY);
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if(pointer < 5){
            touches.get(pointer).touchX = 0;
            touches.get(pointer).touchY = 0;
            touches.get(pointer).touched = false;
        }

        int minY = (Gdx.graphics.getHeight() - game.uiPaddingY);
        int maxY = (Gdx.graphics.getHeight() - game.uiPaddingY) - game.uiMoveSize;
        int maxY1 = (Gdx.graphics.getHeight() - game.uiPaddingY) - game.uiPedalSize;
        //int minX = (Gdx.graphics.getHeight() - game.uiPaddingX);
        //int maxX = (Gdx.graphics.getHeight() - game.uiPaddingX) - game.uiMoveSize;
        //Gdx.app.log("Warn", "Touch up " + pointer);

        if(turningLeft && (pointer == leftIndex) ) {
            // Gdx.app.log("Warn", "Done turning left");
            turningLeft = false;
            leftIndex = -1;
        }

        if(turningRight && (pointer == rightIndex) ) {
            //Gdx.app.log("Warn", "Done turning right");
            turningRight = false;
            rightIndex = -1;
        }

        if( accelerating && (pointer == accIndex) ) {
            //Gdx.app.log("Warn", "Done accelerating");
            accelerating = false;
            accIndex = -1;
        }

        if( braking && (pointer == brakeIndex)) {
            //Gdx.app.log("Warn", "Done braking");
            braking = false;
            brakeIndex = -1;
        }
        //Gdx.app.log("Warn", "Finding touch up " + screenX + " " + screenY + " " + pointer );
        return true;
    }

/*
    @Override
    public void resize(int width, int height) {
        Gdx.input.isKeyPressed(Input.Keys.LEFT);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    */

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
    /*
    @Override
    public void dispose(){

    }
*/


}

