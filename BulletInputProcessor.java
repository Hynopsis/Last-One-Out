package com.rpetersen.game;

/**
 * Created by lastresortname on 5/17/2016.
 */
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.utils.viewport.Viewport;

public class BulletInputProcessor extends InputAdapter {

    private Viewport pickingViewport;
    private btDynamicsWorld collisionWorld;
    private MyGdxGame game;

    // a constructor which takes a Viewport and the collision world and stores them
    public BulletInputProcessor(Viewport viewport, btDynamicsWorld dynamicWorld, MyGdxGame game) {
        this.pickingViewport = viewport;
        this.collisionWorld = dynamicWorld;
        this.game = game;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        //Gdx.app.log("Warn", "We are getting touch");
        if(game.roundOver && game.playerHealth == 0 && game.gas == 0 && game.finalTime == 0){
            //load new stage...
            game.roundOver = false;
            if(game.level == game.maxLevel){
                //then restart the game and return to the main menu, this is where we will change levels in future
                Gdx.app.log("Warn", "Move to main menu here");
                game.playerHealth = 100;
                game.initialized = false;
                game.level =  -1;
                game.mainMenu = true;
            }
            else {
                //call level creation function
                game.Initialize();
            }
        }
        else if(game.gameOver && game.gameOverTimer == game.gameOverWait){
            //once game is over, we wait a bit before allowing player to restart, otherwise happens instantly
            game.gameOverTimer = 0;
            game.playerHealth = 10;
            game.gameOver = false;
            game.level--;
            //Initialize will increment level, so decrement it here...
            game.Initialize();
        }

        if (button == Input.Buttons.LEFT) {
            //the beginning of raycasting, for a weapon shooting mechanic I will later add
            Ray pickRay = pickingViewport.getPickRay(screenX, screenY);
            //Vector3 direction1 = new Vector3(( game.worldLookPoint.x - game.cam.position.x) , (game.worldLookPoint.y - game.cam.position.y), (game.worldLookPoint.z - game.cam.position.z) ).nor();
            //pickRay.set(pickRay.origin, new Vector3(pickRay.direction.x, 0 , pickRay.direction.z));
            //System.out.println("Are these the same? " + pickRay.origin + " " + screenX + " " + screenY);

            btCollisionObject body = RayTesting.rayTest(collisionWorld, pickRay, game);

            if (body != null) {
                //game.CreateCube();
                //System.out.println("Hitting with ray " + body.getUserValue());
                return false;
            }
        }

        return false;
    }
}
