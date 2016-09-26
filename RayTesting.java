package com.rpetersen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;


/**
 * Created by lastr on 5/3/2016.
 */
public class RayTesting {

    private static final Vector3 rayFrom = new Vector3();
    private static final Vector3 rayTo = new Vector3();
    private static final ClosestRayResultCallback callback = new ClosestRayResultCallback(rayFrom, rayTo);
    //public static final game;

    public static btCollisionObject rayTest(btDynamicsWorld collisionWorld, Ray ray, MyGdxGame game) {

        rayFrom.set(ray.origin);
        //rayFrom.set(game.cam.position);

        //find direction between position clicked and teh camera
        // Vector3 direction1 = new Vector3((game.cam.position.x - ray.origin.x) , (game.cam.position.y - ray.origin.y), (game.cam.position.z - camLocation.z) );

        // 50 meters max from the origin
        rayTo.set(ray.direction).scl(20f).add(rayFrom);

        // we reuse the ClosestRayResultCallback, thus we need to reset its
        // values
        callback.setCollisionObject(null);
        callback.setClosestHitFraction(1f);

        Vector3 rayFromWorld = new Vector3();
        rayFromWorld.set(rayFrom.x, 2, rayFrom.z);
        Vector3 rayToWorld = new Vector3();
        rayToWorld.set(rayTo.x, 1, rayTo.z);
        //callback.getRayFromWorld().setValue(rayFrom.x, rayFrom.y, rayFrom.z);
        //callback.getRayToWorld().setValue(rayTo.x, rayTo.y, rayTo.z);
        // Gdx.app.log("Warn", "Shooting ray " + rayFromWorld + " " + rayToWorld + " directiono " + ray.direction);

        collisionWorld.rayTest(rayFrom, rayTo, callback);

        if (callback.hasHit()) {

            return null;
            // game.CreateCube(new Vector3(rayTo.x, 10, rayTo.z), ray.direction);
            //return callback.getCollisionObject();
        }
        else{
            Gdx.app.log("Warn", "Didnt hit anything");

        }

        return null;
    }

}
