package com.rpetersen.game;

import java.util.Map;
import java.util.HashMap;
/**
 * Created by lastr on 4/17/2016.
 */

public enum TileType {
    //so this is our full listing of world tile types
    //it also impliments a hashmap so we can easily get the integer value for matching to other arrays
    //basic roads and grass
    GRASS(0),
    HORIZONTAL(1),
    VERTICAL(2),
    UPTEE(3),
    DOWNTEE(4),
    RIGHTTEE(5),
    LEFTTEE(6),
    TURN1(7),
    TURN2(8),
    TURN3(9),
    TURN4(10),
    CROSS(11),

    //house types, at first we only have one for simplicity
    RIGHTHOUSE(12),
    LEFTHOUSE(13),
    UPHOUSE(14),
    DOWNHOUSE(15);

    private int value;
    //private static Map map = new HashMap<>();
    private static Map<Integer, TileType> map = new HashMap<Integer, TileType>();

    private TileType(int value) {
        this.value = value;
    }

    static {
        for (TileType tileType : TileType.values()) {
            map.put(tileType.value, tileType);
        }
    }

    public static TileType valueOf(int tileType) {
        return map.get(tileType);
    }

    public int getValue() {
        return value;
    }

}
