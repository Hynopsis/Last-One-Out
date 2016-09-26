package com.rpetersen.game;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;

/**
 * Created by lastr on 9/13/2016.
 */
public class Zombie {
    //our main zombie class
    GameObject zModel;
    int xBucket;        //our x position in the world array (buckets)
    int zBucket;        //z position...
    float colTimer;     //time since last collision
    int arrayIndex;     //this is our position within the zombies array

    public Zombie(Model model){
        zModel = new GameObject(model, 1.5f, 10, true);
        //paras (random zombie model, collider, mass, friction, animate(not used)
        //zModel = new GameObject(zombieArray.get(world.GetRandomInt(0,3)), zombieShape, 1.5f, 10, true);
        //create our actual model
    }

    public void Initialize(int x, int y, int arrayIndex){
        //model gets initialized inside of MyGdxGame so I dont need to pass a tons of params
        xBucket = x;
        zBucket = y;
        //zModel = new GameObject(zombieArray.get(world.GetRandomInt(0,3)), zombieShape, 1.5f, 10, true);
        this.arrayIndex = arrayIndex;
    }

    public void Update(int x, int y, int colTimer){
        xBucket = x;
        zBucket = y;
        this.colTimer = colTimer;
    }

    public float GetTimer(){
        return colTimer;
    }
    public void SetTimer(float value){
        colTimer = value;
    }



}
