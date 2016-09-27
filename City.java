package com.rpetersen.game;

/**
 * Created by lastresortname on 5/17/2016.
 */
import com.badlogic.gdx.math.Vector3;
import java.util.ArrayList;
import java.util.List;

public class City {

    private List<Vector3> houses = new ArrayList<Vector3>();        //keeps a list of positions for all houses
    private List<Vector3> crossroads = new ArrayList<Vector3>();    //for a planned feature of expanding size of cities
    private Vector3 center;

    public City(Vector3 center){
        this.setCenter(center);
    }

    protected List<Vector3> getHouses() {
        return houses;
    }

    protected void setHouses(List<Vector3> houses) {
        this.houses = houses;
    }

    protected List<Vector3> getCrossroads() {
        return crossroads;
    }

    protected void setCrossroads(List<Vector3> crossroads) {
        this.crossroads = crossroads;
    }

    protected Vector3 getCenter() {
        return center;
    }

    protected void setCenter(Vector3 center) {
        this.center = center;
    }
}
