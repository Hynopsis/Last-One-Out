package com.rpetersen.game;

/**
 * Created by lastr on 5/17/2016.
 */
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rpetersen4 on 4/12/2016.
 */
public class City {

    List<Vector3> houses = new ArrayList<Vector3>();
    //List<Vector3> roads = new ArrayList<Vector3>();
    List<Vector3> crossroads = new ArrayList<Vector3>();

    Vector3 center;

    public City(Vector3 center){

        this.center = center;
    }
}
