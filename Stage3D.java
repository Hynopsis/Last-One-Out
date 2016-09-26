package com.rpetersen.game;


/**
 * Created by lastr on 4/8/2016.
 */

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.Ray;

public class Stage3D extends Stage {

    @Override
    public Vector2 screenToStageCoordinates (Vector2 screenCoords) {
        Ray pickRay = getViewport().getPickRay(screenCoords.x, screenCoords.y);
        Vector3 intersection = new Vector3(0, 0, 1);
        if (Intersector.intersectRayPlane(pickRay, new Plane(new Vector3(0, 0, 1), Vector3.Zero), intersection)) {
            screenCoords.x = intersection.x;
            screenCoords.y = intersection.y;
        } else {
            screenCoords.x = Float.MAX_VALUE;
            screenCoords.y = Float.MAX_VALUE;
        }
        return screenCoords;
    }

}

