package com.rpetersen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.ModelCache;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;



public class WorldGenerator {

    public boolean doneCreating = false;
    MyGdxGame game;
    //this is the seeds for our levels, this is used to keep level generation the same
    int[] seeds;// = {12000, 18333, 350, 8632,10251};
    int seed = 10251;//12000, 18333, 350, 8632,10251
    //for random number generation
    static Random rand;// = new Random();
    byte[][] worldTiles;
    Vector3[] directions;
    Vector3[] barricades;
    Array<GameObject>[][] buckets;            //this is for sectioning off our zombies to reduce calculations
    Array<Zombie>[][] zBuckets;            //this is for sectioning off our zombies to reduce calculations
    Array<GameObject>[][] staticModels;       //this is for sectioning off our zombies to reduce calculations
    ModelCache[][] modelBuckets;                        //these are the containers for actually rendering the landscape
    BoundingBox[][] cacheBounds;                        //these are the containers for actually rendering the landscape
    Array<Vector3> toSave;
    List<City> ourCities = new ArrayList<City>();
    //we use the hashmap to pass the name of a tile and return its byte reference
    //both these are used to pass surounding tile data and get back a byte value for the road
    //or the house that should be positioned at this point
    HashMap<String, Integer> roadType;
    HashMap<Integer, String> tileType;
    HashMap<String, Integer> houseType;
    int mapSize = 100;
    int tileSize = 8;
    int bucketRange = 5;
    int minCities = 1;
    int maxCities = 1;

    int distBetweenCitiesX = 10;
    int distBetweenCitiesY = 10;

    float maxCityDistance = 12;
    // float[]  maxCityDistances = new float[]{20, 16, 14, 12, 10};
    float[]  maxCityDistances = new float[]{14, 11,9,7, 6};
    public int[] numberOfCities = new int[]{6,8,10,12,14};

    float maxRoadDistance;

    boolean initialized = false;

    public WorldGenerator(MyGdxGame game){

        this.game  = game;
        seeds = new int[]{12000, 18333, 350, 8632,10251};

        rand = new Random(seeds[game.level]);

        //we only want to create road to cities that are so close to us
        maxRoadDistance = mapSize/2;
        directions = new Vector3[]{new Vector3(1,0,0), new Vector3(-1,0,0) ,  new Vector3(0,0,1), new Vector3(0,0,-1)};
        buckets = new Array[mapSize/bucketRange][mapSize/bucketRange];
        zBuckets = new Array[mapSize/bucketRange][mapSize/bucketRange];
        staticModels = new Array[mapSize/bucketRange][mapSize/bucketRange];
        modelBuckets = new ModelCache[mapSize/bucketRange][mapSize/bucketRange];
        cacheBounds = new BoundingBox[mapSize/bucketRange][mapSize/bucketRange];

        roadType = new HashMap<String, Integer>();
        //this gets our byte representation of our roads, dependant on what roads are around us
        roadType.put("0000", 19); // horizontal
        roadType.put("1100", 0); // horizontal
        roadType.put("0011", 1); // vertical
        roadType.put("1111", 10); // crossroads

        //we should replace this later, with cap pieces, have em
        roadType.put("1000", 13); // left cap
        roadType.put("0100", 14); // right cap
        roadType.put("0010", 12); // down cap
        roadType.put("0001", 11); // up cap

        roadType.put("1001", 9); // right down - CORRECT
        roadType.put("0110", 6); // left up - CORRECT
        roadType.put("0101", 8); // left down
        roadType.put("1010",7); // right up

        roadType.put("0111", 5); // left tee
        roadType.put("1011", 4); // right tee
        roadType.put("1110", 2); // up tee
        roadType.put("1101", 3); // down tee

        houseType = new HashMap<String, Integer>();

        //this gets our byte representation of our roads, dependant on what roads are around us
        houseType.put("1100", 16); // left or right facing house 1
        houseType.put("0011", 15); // up or down facing home 1
        houseType.put("1111", 15); // not possible

        houseType.put("1000", 16); // right facing house
        houseType.put("0100", 17); // left facing house
        houseType.put("0010", 15); // up facing house
        houseType.put("0001", 18); // down facing house

        houseType.put("1001", 16); // right facing house
        houseType.put("0110", 17); // left facing house
        houseType.put("0101", 17); // left facing house
        houseType.put("1010", 16); // right facing house

        houseType.put("0111", 15); // up facing house
        houseType.put("1011", 15); // up facing house
        houseType.put("1110", 16); // right facing house
        houseType.put("1101", 16); // right facing house

        //so we use this array to map to our models, the int is the array value, the string
        //is the name of the model that should installed at this point
        tileType = new HashMap<Integer, String>();

        tileType.put(0, "hor");
        tileType.put(1, "vert");
        tileType.put(2, "upTee");
        tileType.put(3, "downTee");
        tileType.put(4, "rightTee");
        tileType.put(5, "leftTee");
        tileType.put(6, "turn1");
        tileType.put(7, "turn2");
        tileType.put(8, "turn3");
        tileType.put(9, "turn4");
        tileType.put(10, "cross");
        tileType.put(11, "endUp");
        tileType.put(12, "endDown");
        tileType.put(13, "endLeft");
        tileType.put(14, "endRight");
        tileType.put(15, "upHouse");
        tileType.put(16, "rightHouse");
        tileType.put(17, "leftHouse");
        tileType.put(18, "downHouse");
        tileType.put(19, "grass");

        //initialize all our arrays
        for(int y = 0; y < (mapSize/bucketRange); y++){

            for(int x = 0; x < (mapSize/bucketRange); x++){

                buckets[x][y] = new Array<GameObject>();
                zBuckets[x][y] = new Array<Zombie>();
                staticModels[x][y] = new Array<GameObject>();
                modelBuckets[x][y] = new ModelCache();

            }}

    }

    public void InitializeWorld(){

        initialized = true;
        //this is not called on initial world load, this is called if we finish the stage and are reseting the world for new level

        //use a different seed for a different stage
        rand = new Random(seeds[game.level]);
        toSave = new Array<Vector3>();
        ourCities.clear();
        toSave.clear();

        barricades = new Vector3[]{new Vector3(0, 0, GetRandomInt(20, 80)),new Vector3(99,0,GetRandomInt(20, 80)), new Vector3(GetRandomInt(20, 80),0,99), new Vector3(GetRandomInt(20, 80),0,0)};
        //create a new world
        worldTiles = new byte[mapSize][mapSize];

        //this arragements is important need to draw in this order visually
        for(int y = mapSize -1; y > -1; y--){
            for(int x = 0; x < mapSize; x++){
                worldTiles[x][y] = 19;
            }}

        //initialize all our arrays
        for(int y = 0; y < (mapSize/bucketRange); y++){
            for(int x = 0; x < (mapSize/bucketRange); x++){

                Vector3 min = new Vector3((x * bucketRange) * tileSize - tileSize, -1, (-y * bucketRange) * tileSize + tileSize);
                Vector3 max = new Vector3(((x + 1) * bucketRange) * tileSize + tileSize, -1, ((-(y + 1)) * bucketRange) * tileSize - tileSize);
                cacheBounds[x][y] = new BoundingBox(min,max);
                buckets[x][y] = new Array<GameObject>();
                zBuckets[x][y] = new Array<Zombie>();

                //need to destroy each of our old objects or their colliders will still be there
                for(int e = 0; e < staticModels[x][y].size; e++){
                    //sestroy clears all data, reference and calls dispose on go data
                    staticModels[x][y].get(e).Destroy(game.dynamicsWorld);
                }

                staticModels[x][y].clear();
                staticModels[x][y] = new Array<GameObject>();
                modelBuckets[x][y].dispose();
                modelBuckets[x][y] = new ModelCache();
            }}

        for(int x = 0; x < 4; x++) {
            worldTiles[(int)barricades[x].x][(int)barricades[x].z] = 1;
            //Gdx.app.log("WARN", "barricade value " + worldTiles[(int)barricades[x].x][(int)barricades[x].z] + " at " + x + " x,y " + barricades[x].x + " " + barricades[x].z );
        }

        CreateWorld();

        game.doneLoading();
     }


    void CreateWorld(){
        //generate 3-5 random cities for our starting map
        int tCities = numberOfCities[game.level];//GetRandomInt(minCities + game.level ,maxCities + game.level);
        List<City> remainingCities = new ArrayList<City>();
        List<Vector3> totalCities = new ArrayList<Vector3>();
        Vector3 temp = new Vector3(0,0,0);
        boolean tooClose = true;
        boolean retry = false;

        Gdx.app.log("WARN", "Random cities " + tCities);

        //generate random points around the map to place cities, if they are too close then try again to get proper spacing
        for(int x = 0; x < tCities; x++){

            if(x == 0) {
                //get value for random positioning of city
                totalCities.add(GetRandomV3(0, mapSize - 1));
            }
            else {
                temp = GetRandomV3(0, mapSize - 1);
                tooClose = true;

                while(tooClose){

                    retry = false;

                    for (int y = 0; y < totalCities.size(); y++) {
                        //if this returns true then this other point is far enough away from all other points
                        if (!DistanceXY(temp, totalCities.get(y))) {
                            retry = true;
                        }
                    }
                    if(!retry){
                        //so if none of our cities are close than 20 units on each axis, then we can add this city
                        tooClose = false;
                        totalCities.add(temp);
                    }
                    else{
                        //get a new random number
                        temp = GetRandomV3(0, mapSize - 1);
                    }
                }
            }
        }

        //at this point we have created variable positions in our world that are far enough away from each other
        //so actually create our cities and make a copy array for use in our next step
        for(int x = 0; x < totalCities.size(); x++) {
            ourCities.add(new City(totalCities.get(x)));
            remainingCities.add(new City(totalCities.get(x)));
            Gdx.app.log("WARN", "Cities " + totalCities.size() + " made position " + x + " " + totalCities.get(x));
        }

        //now we will go through our cities and create roads between them
        for(int x = 0; x < ourCities.size() - 1; x++) {
            //Gdx.app.log("WARN", "Index " + ourCities.get(x).center + " " + index);
            int removeIndex = -1;
            for(int y = 0; y < remainingCities.size(); y++) {
                if(remainingCities.get(y) == ourCities.get(x)) {
                    removeIndex = y;
                }
            }
            if(removeIndex != -1) {
                remainingCities.remove(removeIndex);
            }

            int totalConnected = 0;
            float originalRoadDistance = maxRoadDistance;

            for(int y = 0; y < remainingCities.size(); y++) {
                //only create roads if we are within our maxDistance
                if(Distance(ourCities.get(x).center,remainingCities.get(y).center) > maxRoadDistance){
                    continue;
                }
                else{//for each city we connect to reduce our chance to connect to another
                    totalConnected +=1;
                    maxRoadDistance -= totalConnected * 5;
                }
                //Gdx.app.log("WARN", "Connecting cities " + ourCities.get(x).center + " " + remainingCities.get(y).center);
                ConnectRoads(ourCities.get(x), remainingCities.get(y), false, 0);
            }
            maxRoadDistance = originalRoadDistance;
        }

        //for temperary debugging, lets mark our cites, this will override assignments from connecting roads
        //in reality we want this to be a crossroads tile
        for(int x = 0; x < totalCities.size(); x++) {
            worldTiles[(int)totalCities.get(x).x][(int)(int)totalCities.get(x).z] = 10;
        }

        //this fleshes out the cities and places houses, and could expand cities with crossroads
        for(int x = 0; x < ourCities.size(); x++) {
            CreateCity(ourCities.get(x));
        }

        //now determine a secton of each edge of the map for dropping of passengers
        //this creates road leading from this edge point to the nearest city

        for(int x = 0; x < 4; x++) {
            GetBarricades(barricades[x]);
        }

        //this determines the type of road tile
        GetRoads();
        //this determines the facing direction of our houses relative to the road
        GetHouses();

        //so now we will actually create the cities and place building and crossroads
        Gdx.app.log("WARN", "Starting to draw map");
        //for debugging we are just drawing strings to the console, so can make sure
        //that world builds correctly
        String line= new String();

        for(int y = mapSize -1; y > -1; y--){
            Gdx.app.log("WARN", line);
            line = "" + y;
            //for(int y = 0; y < mapSize; y++){
            for(int x = 0; x < mapSize; x++) {
                //worldTiles[x][y] = 15;
                if(worldTiles[x][y] == 19){
                    line += " ";
                }
                else {
                    line += worldTiles[x][y];
                }
            }
        }

        Gdx.app.log("WARN", "Map drawn");
        doneCreating = true;
    }

    void GetBarricades(Vector3 pos){
        //get first point, then find closest city and connect roads to it

        float smallest = 1000;
        int smallestIndex = -1;

        for(int x = 0; x < ourCities.size(); x++) {

            float distance = (float)Distance(ourCities.get(x).center, pos);

            if(distance < smallest){
                smallest = distance;
                smallestIndex = x;
            }
        }

        ConnectRoads(ourCities.get(smallestIndex).center, pos);

        worldTiles[(int)pos.x][(int)pos.y] = 1;
    }

    void GetRoads(){
        //this detemines what type of roads to place, could be faster, bur works
        //go throuhg whole map and for anything that is a road check around and see what type of road
        for(int x = 0; x < mapSize; x++) {
            for (int y = 0; y < mapSize; y++) {

                if(worldTiles[x][y] == 1){//roads are initialize to one, then determined here
                    worldTiles[x][y] = GetRoadType(new Vector3(x, 0, y));
                }
            }
        }
    }

    void GetHouses(){

        City thisCity;

        for(int x = 0; x < ourCities.size(); x++) {

            //each city has a house that needs someone saved, so do a random check and see if a house has someone to save
            int count = ourCities.get(x).houses.size();
            thisCity = ourCities.get(x);

            int hot = GetRandomInt(0, count - 1); //assumes only one house per city, so just pick one
            toSave.add(new Vector3(thisCity.houses.get(hot).x, 0, thisCity.houses.get(hot).z));

            for(int y = 0; y < count; y++) {

                Vector3 thisHouse = new Vector3(thisCity.houses.get(y).x, 0, thisCity.houses.get(y).z);

                if (!OutOfBounds(thisHouse) && worldTiles[(int) thisHouse.x][(int) thisHouse.z] == 19) {
                    worldTiles[(int) thisHouse.x][(int) thisHouse.z] = GetHouseType(thisHouse);
                }
                else{
                    Gdx.app.log("WARN","House out of bounds " + thisHouse);
                }
            }
        }

    }

    byte GetRoadType(Vector3 position){

        Vector3 combined;
        String temp = "";
        boolean found = false;

        for(int x = 0; x < 4; x++) {
            found = false;
            //so check direct positions around this house...and determine type of road
            combined = new Vector3(position.x + directions[x].x, 0, position.z + directions[x].z);
            //take our position plus our cardinal direction, to get position around us
            if(!OutOfBounds(combined)){
                //so initially on create roads all roads are either one or two, but this catches all roads
                if(worldTiles[(int)combined.x][(int)combined.z] < 19){// && worldTiles[(int)combined.x][(int)combined.z] > 0){//this is a road
                    temp += 1;
                }
                else{
                    temp+=0;
                }
            }
            else{
                //wait , if this position is a barricade then out of bounds are considered roads

                for(int i = 0; i < 4; i++) {
                    //go through our barricades, if this is one, assume this out of bounds point is a road
                    if(position.x == barricades[i].x && position.z == barricades[i].z){
                        Gdx.app.log("WARN","Finding barricade-  " + barricades[x] + " and adding to " + temp);
                        temp +=1;
                        found = true;
                    }

                }

                if(!found) {
                    //if out of bounds set this position to zero
                    temp += 0;
                }
            }
        }
        roadType.put("1000", 13); // left cap
        roadType.put("0100", 14); // right cap
        roadType.put("0010", 12); // down cap
        roadType.put("0001", 11); // up cap

        return roadType.get(temp).byteValue();
        //Gdx.app.log("WARN","Gettng value  " + roadType.get(temp).byteValue());
    }


    byte GetHouseType(Vector3 position){

        Vector3 combined = new Vector3();
        String temp = "";

        for(int x = 0; x < 4; x++) {
            //so check direct positions around this house...and determine type of road
            combined = new Vector3(position.x + directions[x].x, 0, position.z + directions[x].z);
            //take our position plus our cardinal direction, to get position around us
            if(!OutOfBounds(combined)){
                //so initially on create roads all roads are either one or two, but this catches all roads
                if(worldTiles[(int)combined.x][(int)combined.z] < 15 ){//theree is nothing else here
                    temp += 1;
                }
                else{
                    temp+=0;
                }
            }
            else{
                //if out of bounds set this position to zero
                temp += 4;
            }
        }

        if(houseType.get(temp) == null){
            Gdx.app.log("WARN","GETTING THIS AS NULL " + temp + " " + position + " " + combined);
            return 19;
        }
        else {
            return houseType.get(temp).byteValue();
        }
        //Gdx.app.log("WARN","Gettng value  " + roadType.get(temp).byteValue());
    }

    boolean OutOfBounds(Vector3 pos){
        //this makes sure that we are withing teh map for placement
        if((pos.x > mapSize - 1) || (pos.z > mapSize - 1) || pos.x < 0 || pos.z < 0){
            return true;
        }
        return false;
    }

    void CreateCity(City city){

       // Gdx.app.log("WARN","Running create city to make more roads " + city.center);
        //this method places buildings and and crossroad sections
        Vector3[] pos = {new Vector3(1,0,0),new Vector3(-1,0,0), new Vector3(0,0,1),new Vector3(0,0,-1)};
        Vector3 center = city.center;
        int random;
        int houseRandom;
        Vector3 worldPosition;
        int totalChanged = 0;
        int pointsChecked = 0;
        int pointsTotal = 0;
        int outOfBounds = 0;
        int hasValue = 0;

        random = GetRandomInt(3,6);         //we will randomly update road within this distance

        for(int x = 0; x < 4; x++) {

            for(int t = 1; t < random; t++){

                pointsTotal +=1;
                //so the spot we are checking is our center position + our cardinal direction to check, times how far out we plan to check
                worldPosition = new Vector3(pos[x].x * t, 0, pos[x].z * t ).add(center);

                if(!OutOfBounds(worldPosition)){

                    pointsChecked +=1;
                    totalChanged += 1;
                    //all roads are initialized to one,

                    worldTiles[(int) worldPosition.x][(int) worldPosition.z] = 1;
                    //Gdx.app.log("WARN","Found position to change " + worldPosition);

                    //we will add houses on both new streets and existing streets
                    houseRandom = GetRandomInt(0,100);

                    if (houseRandom > 10) {
                        if(x < 2){//then we are horizontal
                            city.houses.add(new Vector3(worldPosition.x,0,worldPosition.z + 1));
                            city.houses.add(new Vector3(worldPosition.x,0,worldPosition.z - 1));
                        }
                        else if(x > 1){//then we are vertical
                            city.houses.add(new Vector3(worldPosition.x + 1,0,worldPosition.z));
                            city.houses.add(new Vector3(worldPosition.x - 1,0,worldPosition.z));
                        }
                    }
                    else if(houseRandom > 3){
                        if(x < 2){//then we are horizontal
                            if(houseRandom % 2 == 0) {
                                city.houses.add(new Vector3(worldPosition.x, 0, worldPosition.z + 1));
                            }
                            else {
                                city.houses.add(new Vector3(worldPosition.x, 0, worldPosition.z - 1));
                            }
                        }
                        else if(x > 1){//then we are vertical
                            if(houseRandom % 2 == 0) {
                                city.houses.add(new Vector3(worldPosition.x + 1, 0, worldPosition.z ));
                            }
                            else {
                                city.houses.add(new Vector3(worldPosition.x - 1, 0, worldPosition.z ));
                            }
                        }
                    }
                    else{
                        if(x < 2){//then we are horizontal
                            if(worldPosition.x % 3 == 0 && houseRandom % 2 == 0) {
                                city.houses.add(new Vector3(worldPosition.x, 0, worldPosition.z + 1));
                            }
                            else if (worldPosition.x % 3 == 0) {
                                city.houses.add(new Vector3(worldPosition.x, 0, worldPosition.z - 1));
                            }
                        }
                        else if(x > 1){//then we are vertical
                            if(worldPosition.z % 3 == 0 && houseRandom % 2 == 0) {
                                city.houses.add(new Vector3(worldPosition.x + 1, 0, worldPosition.z ));
                            }
                            else if(worldPosition.z % 3 == 0){
                                city.houses.add(new Vector3(worldPosition.x - 1, 0, worldPosition.z ));
                            }
                        }
                    }
                }
                else{
                    outOfBounds += 1;
                }


            }
        }
        //Gdx.app.log("WARN","Total changed " + totalChanged + " pointsChecked " + pointsChecked + " pointsTotal " + pointsTotal + " random " + random + " out of bounds " + outOfBounds + " have values " + hasValue);
    }

    void ConnectRoads(City city1, City city2, boolean checkHouses, int direction){

        //so this method connects a road between our first and second city
        //Gdx.app.log("WARN", "Pickle calling ConnectRoads  " + city1.center.toString() + " " + city2.center.toString());

        Vector3 diff = new Vector3((city2.center.x - city1.center.x), 0,(city2.center.z - city1.center.z));// = city1.center.sub(city2.center);        \
        int dx = (int)Math.abs(diff.x);
        int dz = (int)Math.abs(diff.z);

        int counterX = 1;
        int counterZ = 1;

        Vector3 cityC = city1.center;
        Vector3 cityC1 = city2.center;

        float lastX = 0;

        int localDirection;

        while(counterX < dx + 1){
            //so find our horizontal distance to this other town
            if(diff.x < 0){
                localDirection = counterX * -1;
            }
            else{
                localDirection = counterX;
            }

            if(counterX == dx){      //this is our last road tile before going up and down
                worldTiles[(int)(cityC.x + localDirection)][(int)(cityC.z)] = 1;//map.get("horizontal").byteValue();
                lastX = (cityC.x + localDirection);
            }
            else{
                lastX = (cityC.x + localDirection);
                worldTiles[(int)(cityC.x + localDirection)][(int)(cityC.z)] = 1;// map.get("horizontal").byteValue();
            }
            //Gdx.app.log("WARN", "Pickle TEST Iterate x  " + dx + " " + counterX + " " + (cityC.x + localDirection));
            counterX +=1;
        }
        //so this should have placed roads from our position all the way horizontally, so now move down
        while(counterZ < dz){
            //so find our horizontal distance to this other town
            if(diff.z < 0){
                localDirection = counterZ * -1;
            }
            else{
                localDirection = counterZ;
            }

            if(counterZ == dz -2){      //this is our last road tile before going up and down
                //later will change this to horizontal end block?
                worldTiles[(int)(lastX)][(int)(cityC.z + localDirection)] = 1;//map.get("vertical").byteValue();
            }
            else{
                worldTiles[(int)(lastX)][(int)(cityC.z + localDirection)] = 1;//map.get("vertical").byteValue();
            }

            counterZ += 1;
            //Gdx.app.log("WARN", "Pickle TEST Iterate x  " + dz + " " + counterZ + " " + (cityC.z + localDirection)););
        }
        //Gdx.app.log("WARN", "Pickle Found horizontal " + counterX + " and vertical " + counterZ);
    }

    void ConnectRoads(Vector3 point1, Vector3 point2){

        //so this method connects a road between our first and second city
        //Gdx.app.log("WARN", "Pickle calling ConnectRoads  " + city1.center.toString() + " " + city2.center.toString());

        Vector3 diff = new Vector3((point2.x - point1.x), 0,(point2.z - point1.z));// = city1.center.sub(city2.center);        \
        int dx = (int)Math.abs(diff.x);
        int dz = (int)Math.abs(diff.z);

        int counterX = 1;
        int counterZ = 1;
        Vector3 cityC = point1;
        float lastX = 0;

        int localDirection;

        while(counterX < dx + 1){
            //so find our horizontal distance to this other town
            if(diff.x < 0){
                localDirection = counterX * -1;
            }
            else{
                localDirection = counterX;
            }

            if(counterX == dx){      //this is our last road tile before going up and down
                worldTiles[(int)(cityC.x + localDirection)][(int)(cityC.z)] = 1;//map.get("horizontal").byteValue();
                lastX = (cityC.x + localDirection);
            }
            else{
                lastX = (cityC.x + localDirection);
                worldTiles[(int)(cityC.x + localDirection)][(int)(cityC.z)] = 1;// map.get("horizontal").byteValue();
            }
            //Gdx.app.log("WARN", "Pickle TEST Iterate x  " + dx + " " + counterX + " " + (cityC.x + localDirection));
            counterX +=1;
        }
        //so this should have placed roads from our position all the way horizontally, so now move down
        while(counterZ < dz){
            //so find our horizontal distance to this other town
            if(diff.z < 0){
                localDirection = counterZ * -1;
            }
            else{
                localDirection = counterZ;
            }

            if(counterZ == dz - 1){      //this is our last road tile before going up and down
                //later will change this to horizontal end block?
                worldTiles[(int)(lastX)][(int)(cityC.z + localDirection)] = 1;//map.get("vertical").byteValue();
            }
            else{
                worldTiles[(int)(lastX)][(int)(cityC.z + localDirection)] = 1;//map.get("vertical").byteValue();
            }

            counterZ += 1;
            //Gdx.app.log("WARN", "Pickle TEST Iterate x  " + dz + " " + counterZ + " " + (cityC.z + localDirection)););
        }
        //Gdx.app.log("WARN", "Pickle Found horizontal " + counterX + " and vertical " + counterZ);
    }

    public static int GetRandomInt(int min, int max){

        return  rand.nextInt((max - min) + 1) + min;

    }

    Vector3 GetRandomV3(int min, int max){

        return  new Vector3((rand.nextInt((max - min) + 1) + min), 0, rand.nextInt((max - min) + 1) + min) ;

    }

    public double Distance(Vector3 point1, Vector3 point2){
        //       2          2
        // \/ (y2-y1)  + (x2-x1)
        return Math.sqrt(Math.pow((double)(point2.z - point1.z), (double)2) + Math.pow((double)(point2.x - point1.x),(double) 2));

    }

    public boolean DistanceXY(Vector3 point1, Vector3 point2){

        //this is a specialized method that takes two points and returns false if the are too close, see below
        double first =  Math.sqrt(Math.pow((double)(point2.z - point1.z), (double)2) + Math.pow((double)(0),(double) 2));
        double totalDistance =  Math.sqrt(Math.pow((double)(point2.z - point1.z), (double)2) + Math.pow((point2.x - point1.x), (double)2));
        double second =  Math.sqrt(Math.pow((double)(0), (double)2) + Math.pow((double)(point2.x - point1.x),(double) 2));

        if(totalDistance < maxCityDistances[game.level]){
            return false;
        }
        else if(first < 5 || second < 5){
            return false;
        }
        else{
            //Log.d("WARN", "Pickle all good " + point1.toString() + " " + point2.toString());
            return true;
        }
    }

}


