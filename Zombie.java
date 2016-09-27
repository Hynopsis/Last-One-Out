package com.rpetersen.game;

import com.badlogic.gdx.graphics.g3d.Model;

/**
 * Created by lastresortname on 9/13/2016.
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
    }

    public void Initialize(int x, int y, int arrayIndex){
        //model gets initialized inside of MyGdxGame so I dont need to pass a tons of params
        xBucket = x;
        zBucket = y;
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
