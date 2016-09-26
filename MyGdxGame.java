package com.rpetersen.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.DebugDrawer;
import com.badlogic.gdx.physics.bullet.collision.CollisionObjectWrapper;
import com.badlogic.gdx.physics.bullet.collision.ContactListener;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseInterface;
import com.badlogic.gdx.physics.bullet.collision.btCollisionAlgorithm;
import com.badlogic.gdx.physics.bullet.collision.btCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btDispatcherInfo;
import com.badlogic.gdx.physics.bullet.collision.btManifoldResult;
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

//
public class MyGdxGame extends ApplicationAdapter {

    private Preferences prefs;// = Gdx.app.getPreferences("My Preferences");
    boolean initial = true;                 //this is a flag for the first time the game is run

    int level = 0;                          //this is the current level we are on, 0-based index
    int maxLevel = 4;                       //total number of rounds in a level
    int[] levelScores = new int[maxLevel + 1];  //keeps totals of our score between rounds
    public boolean initialized = false;     //whether world has been created on this script

    long baseTime;							//used to measure time between collisions
    float timer;                            //used for timing collision between zombies and character
    float collisionTimer = -1;              //for collisiosn between house, rocks trees
    float finalTime = -1;                   //used for scoring time left
    float closeCounter = 0;                 //counter for how long we are close to a passenger
    float closeDropCounter = 0;             //" " to a drop off location(barricade)
    float checkTime = 200;					//time between registering collisions, in secodnds
    int passengers = 0;

    //not sure if any of these need to be public or not...?
    public PerspectiveCamera cam;			//used for rendering 3d world
    public OrthographicCamera oCam;			//used for rendering ui

    FPSLogger fps;                   //for debugging
    GetInput input;					//this captures our input information in other class
    Environment environment;			//used for rendering teh scene

    ModelBatch modelBatch;			//collection of objects sent for rendering

    AssetManager assets;				                           //object for loading resources
    Array<GameObject> instances = new Array<GameObject>();         //this is a collection of models to be rendered each frame
    Array<GameObject> renderZombies = new Array<GameObject>();              //these are the zombies visible by the player
    Array<Zombie> zombiePool = new Array<Zombie>();                    //these are all the zombies from our pooler
    Array<GameObject> debris = new Array<GameObject>();            //these are zombie pieces from killing them

    Array<Model> houseArray = new Array<Model>();                  //collection of all our house modesl
    Array<Model> zombieArray = new Array<Model>();                 //collection of all our zombie models
    Array<Model> treeArray = new Array<Model>();                   //tree models
    Array<Model> rockArray = new Array<Model>();                   //rock modesl
    Array<Model> bushArray = new Array<Model>();                   //bush models

    //these two array are for random positioning of details and zombies on a tile
    Vector3[] positions = new Vector3[]{new Vector3(2.25f, 0, -2.25f), new Vector3(5.25f, 0, -2.25f), new Vector3(2.5f, 0,  -5.25f), new Vector3(5.25f, 0, - 5.25f)};
    Vector3[] housePositions = new Vector3[]{new Vector3(1f, 0, 1f), new Vector3(7f, 0, -1), new Vector3(1, 0,  -7), new Vector3(7, 0, -7)};

    GameObject playerGo;				//our player gameobject
    GameObject playerGo1;			//for testing models
    GameObject groundGo;             //this is a huge plane representing the ground
    GameObject waterGo;              //this is a another huge plane representing edge of the world water
    //public GameObject instance;             //general holder used throughout the game to stop creating new objects

    boolean loading;					//used while loading resources

    SpriteBatch spriteBatch;                //this is a containing for all our textures/sprite that get rendered

    //we shoudl change these to be relative to screen size, may be too much sometimes
    //these values are used to detect input in GetInput Class,
    public int uiPaddingX = 50;     //*
    public int uiPaddingY = 50;     //*
    public int uiMoveSize = 256;    //*
    public int uiPedalSize = 256;   //*
    //used for positioning ui elements, public since getInput class uses these too
    public Vector2 gPos;        //*
    public Vector2 bPos;        //*
    public Vector2 lPos;        //*
    public Vector2 rPos;        //*

    //minimap image, which is created at runtime based on randomly generated terrain
    Texture miniMapImg;

    //these are textures for our menus
    Texture menu;
    Texture continuePic;
    Texture roundOverPic;
    Texture gameOverPic;
    Texture retryPic;
    Texture congratsPic;
    Texture returnPic;
    Texture logo;
    Texture logoBackground;

    //cache variables for translations during movement and rotations, to avoid creating new ones every frame
    Vector3 position = new Vector3();
    Vector3 position1 = new Vector3();
    Vector3 position2 = new Vector3();
    Quaternion tempRotation = new Quaternion();
    Matrix4 instanceRotation;
    Quaternion quaternion = new Quaternion();
    Quaternion oldRotation = new Quaternion();
    Matrix4 mat = new Matrix4();
    Vector3 playerPosition;
    Vector3 direction;
    Vector3[] directions = new Vector3[]{new Vector3(1,0,0), new Vector3(-1,0,0) ,  new Vector3(0,0,1), new Vector3(0,0,-1),
            new Vector3(1,0,1), new Vector3(-1,0,1) ,  new Vector3(-1,0,-1), new Vector3(1,0,-1), new Vector3(0,0,0)};

    Vector3 playerBucket = new Vector3();
    int total = 0;
    Vector3 around = new Vector3();
    int count = 0;
    double zRange = 0.00;//Math.pow((float)(world.tileSize * zombieRange),2);
    int index = 0;
    Vector2 newBucket = new Vector2();
    float distance = 0.0f;
    float currentSpeed;
    Vector3 random = new Vector3();
    Vector3 playerIndex = new Vector3();
    GameObject thisGo;

    Vector3 camPosition;// = new Vector3(0,2.5f, 2.1f);	//where we are positioning the camera relative to our player
    Vector3 lookPosition;// = new Vector3(0,0,-3f);	    //instead of looking at the player, we look at a position in front of our player
    Vector3 currentLookPoint = new Vector3();
    Vector3 currentBackPoint = new Vector3();

    //player vehicle movement
    float speed = 0;                        //this is current player speed
    int maxSpeed = 40;                      //max player speed
    int maxRSpeed = -maxSpeed/2;             //max reverse speed
    float turnSpeed = 90;                   //how fast we can turn

    float zombieSpeed = 2;                  //zombie speed
    float zombieRange = 3.5f;               //how close zombies are before heading to player
    int zombieCounter = 10;                 //used to tally zombies placed and set their positional indexes
    int zombiePoolCounter = 0;              //on later levels we resued zombies, and use this counter to grab from zombiepool array

    float frameTimer = 0;                   //used to counter number of frames since start/used for a coule of things

    int playerHealth = 100;                 //starting player health
    int maxPlayerHealth = 100;              //max player health
    float gas = 100;                        //starting gas amount
    float maxGas = 100;                     //max gas amount
    float gasUse = .01f;                   //how much gas we use per frame relative to our current speed

    //these are used for displaying totals at the end of the round
    int pHealth = 0;
    float pGas = 0;
    float pTime = 0;

    WorldGenerator world;                   //this is responsible for creating the data for our world

    //all values used to display player HUD and UI elements
    Stage stage;
    Label label;
    Label distanceLabel;
    Label passLabel;
    Label timeLabel;
    Label menuTitleLabel;
    Label timeLeftLabel;
    Label gasLeftLabel;
    Label dmgLabel;
    Label totalLabel;
    Label finalLabel;
    Label lineLabel;
    TextButton startGame;
    TextButton settings;

    BitmapFont font;
    StringBuilder stringBuilder;

    int treeChance = 5;                 //chance for us to place a tree on a grass tile
    int bushChance = 6;                 //chance for us to place a tree on a grass tile
    int rockChance = 6;                 //chance for us to place a tree on a grass tile
    int zombieChance = 6;                //chance to spawn a zombie

    //helper classes
    btCollisionConfiguration collisionConfig;
    btDispatcher dispatcher;

    //for detecting physics events
    MyContactListener contactListener;
    //for setting up a physics world for broad phase collisions
    btBroadphaseInterface broadphase;
    public static btDynamicsWorld dynamicsWorld;
    btConstraintSolver constraintSolver;

    //temp house model for testing collisions
    //Model downHouse;

    DebugDrawer debugDrawer;                        //can be used to draw collisions shapes, super laggy though

    //so these are bitwise flags for collisions detection,
    final static short PLAYER_FLAG = 1<<2;
    final static short HOUSE_FLAG = 1<<3;
    final static short GROUND_FLAG = 1<<8;
    final static short ENEMY_FLAG = 1<<10;
    final static short DEBRIS_FLAG = 1<<4;
    final static short ALL_FLAG = -1;
    final static short COMBINED_FLAG1 = ENEMY_FLAG | GROUND_FLAG | HOUSE_FLAG;
    final static short COMBINED_FLAG = ENEMY_FLAG | GROUND_FLAG | HOUSE_FLAG | PLAYER_FLAG;

    static Texture guiTexture;// = new Texture("greenButton.png");//new Texture("GuiPack.png");

    static TextureRegion landRegion;// = new TextureRegion(guiTexture, 1, 443, 512, 512);
    static TextureRegion leftArrowRegion;// = new TextureRegion(guiTexture, 1, 185, 256, 256);
    static TextureRegion collectBarRegion;// = new TextureRegion(guiTexture, 515, 930, 256, 25);
    static TextureRegion gasBarRegion;// = new TextureRegion(guiTexture, 1, 158, 256, 25);
    static TextureRegion healthbarRegion;// = new TextureRegion(guiTexture, 515, 903, 256, 25);
    static TextureRegion healthbar1Region;// = new TextureRegion(guiTexture, 1, 131, 256, 25);
    static TextureRegion greenButtonRegion;// = new TextureRegion(guiTexture, 259, 313, 128, 128);
    static TextureRegion redButtonRegion;// = new TextureRegion(guiTexture, 515, 773, 128, 128);
    static TextureRegion rightArrowRegion;// = new TextureRegion(guiTexture, 1, 1, 128, 128);
    static TextureRegion redRegion;// = new TextureRegion(guiTexture, 773, 905, 50, 50);
    static TextureRegion targetRegion;// = new TextureRegion(guiTexture, 259, 279, 32, 32);
    static TextureRegion toSaveRegion;// = new TextureRegion(guiTexture, 389, 409, 32, 32);

    //for our minimap
    Pixmap pixmap;

    //this is for detecting raycasts using physics, dont thing we ever ended up using this
    Viewport viewport = null;

    //this signals the stage is finished and all passngers have been collected, display round over menu, and go to next stage
    //these are basically current game states and are used all over the place
    boolean roundOver = false;
    boolean gameOver = false;
    boolean mainMenu = true;

    //this adds a waiter when gameover or round over before collectin touch events
    int gameOverTimer = 0;
    int gameOverWait =45;

    Node roadNode = new Node();
    GameObject roadInstance;
    GameObject treeInstance;
    GameObject bushInstance;
    GameObject rockInstance;
    GameObject zombieInstance;
    Model otherCube;

    @Override
    public void create () {
        //create all our texture information from our texture atlas
        guiTexture = new Texture("GuiPack.png");

        //landRegion = new TextureRegion(guiTexture, 1, 443, 512, 512);
        leftArrowRegion = new TextureRegion(guiTexture, 1, 277, 256, 256);
        collectBarRegion = new TextureRegion(guiTexture, 1, 733, 512, 64);
        gasBarRegion = new TextureRegion(guiTexture, 1, 667, 512, 64);
        healthbarRegion = new TextureRegion(guiTexture, 1, 601, 512, 64);
        healthbar1Region = new TextureRegion(guiTexture, 1, 535, 512, 64);
        greenButtonRegion = new TextureRegion(guiTexture, 515, 541, 256, 256);
        redButtonRegion = new TextureRegion(guiTexture, 1, 19, 256, 256);
        rightArrowRegion = new TextureRegion(guiTexture, 259, 277, 256, 256);
        redRegion = new TextureRegion(guiTexture, 1, 1, 16, 16);
        targetRegion = new TextureRegion(guiTexture, 773, 765, 32, 32);
        toSaveRegion = new TextureRegion(guiTexture, 259, 259, 32, 32);

        //initialize physics library, then create helper objects,
        Bullet.init();
        collisionConfig = new btDefaultCollisionConfiguration();
        dispatcher = new btCollisionDispatcher(collisionConfig);
        broadphase = new btDbvtBroadphase();
        constraintSolver = new btSequentialImpulseConstraintSolver();
        dynamicsWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase, constraintSolver, collisionConfig);
        dynamicsWorld.setGravity(new Vector3(0, -9, 0)); //was - 10 for gravity

        contactListener = new MyContactListener();

        //creates elements for our HUD and player GUI elements
        stage = new Stage();
        font = new BitmapFont();
        font.getData().setScale(2,2);

        //these are buttons for our main menu, then we add listeners to detect input
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.up = new SpriteDrawable(new Sprite(new Texture("button.png")));
        style.down = new SpriteDrawable(new Sprite(new Texture("button.png")));
        //style.over = new SpriteDrawable(new Sprite(new Texture("greenButton.png")));
        style.font = font;
        style.fontColor = Color.WHITE;

        startGame = new TextButton("START GAME", style);
        startGame.setPosition(Gdx.graphics.getWidth() / 2 - 300, Gdx.graphics.getHeight() / 2 - 200);
        startGame.setHeight(200);
        startGame.setWidth(600);

        startGame.addListener(
                new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        Gdx.app.log("Warn", "Getting touch up on startGame Button");
                        mainMenu = false;
                    }
                }
        );

        settings = new TextButton("SETTINGS", style);
        settings.setPosition(Gdx.graphics.getWidth() / 2 - 300, Gdx.graphics.getHeight() / 2 - 450);
        settings.setHeight(200);
        settings.setWidth(600);

        settings.addListener(

                new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        Gdx.app.log("Warn", "Getting touch up on settings  click listener Button");
                    }
                    //public void onTouch()
                }

        );


        //create all the labels we will use throughout the game
        label = new Label(" ", new Label.LabelStyle(font, Color.WHITE));
        distanceLabel = new Label(" ", new Label.LabelStyle(font, Color.WHITE));
        passLabel = new Label(" ", new Label.LabelStyle(font, Color.WHITE));
        timeLabel = new Label(" ", new Label.LabelStyle(font, Color.WHITE));
        menuTitleLabel = new Label(" ", new Label.LabelStyle(font, Color.BLACK));
        timeLeftLabel = new Label(" ", new Label.LabelStyle(font, Color.BLACK));
        gasLeftLabel = new Label(" ", new Label.LabelStyle(font, Color.BLACK));
        dmgLabel = new Label(" ", new Label.LabelStyle(font, Color.BLACK));
        totalLabel = new Label(" ", new Label.LabelStyle(font, Color.BLACK));
        finalLabel = new Label(" ", new Label.LabelStyle(font, Color.BLACK));
        lineLabel = new Label(" ", new Label.LabelStyle(font, Color.BLACK));

        stage.addActor(startGame);
        stage.addActor(settings);
        stage.addActor(label);
        stage.addActor(lineLabel);
        stage.addActor(totalLabel);
        stage.addActor(finalLabel);
        stage.addActor((timeLabel));
        stage.addActor(distanceLabel);
        stage.addActor(passLabel);
        stage.addActor(menuTitleLabel);
        stage.addActor(timeLeftLabel);
        stage.addActor(gasLeftLabel);
        stage.addActor(dmgLabel);

        stringBuilder = new StringBuilder();

        //this creates all the data for our world when we call InitializeWorld, which calls doneLoading on this class
        world = new WorldGenerator(this);

        fps = new FPSLogger();

        input = new GetInput(this);                 //this creates our input class which detect and responds to touche events

        camPosition = new Vector3(0,0f, 2.5f);      //this is where the camera is positioned relative to our player
        lookPosition = new Vector3(0,0,-1.5f);      //this is where the camera looks to    -z if forward

        //load our textures for our menus
        menu = new Texture("menu.png");
        continuePic = new Texture("continue.png");
        roundOverPic = new Texture("roundOver.png");
        gameOverPic = new Texture("gameOver.png");
        congratsPic = new Texture("congrats.png");
        returnPic = new Texture("return.png");
        retryPic = new Texture("retry.png");
        logoBackground = new Texture("highway.jpg");
        logo = new Texture("logo.png");

        //objects to group things for rendering
        modelBatch = new ModelBatch();
        spriteBatch = new SpriteBatch();

        //the environment object hold basic world data
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.6f, 0.6f, 0.6f, 1f));
        environment.add(new DirectionalLight().set(0.6f, 0.4f, 0.6f, .4f, -.4f, 0.4f));

        //set up static ui positioning variables
        gPos = new Vector2((Gdx.graphics.getWidth()/2 - uiPaddingX - uiPedalSize), (-Gdx.graphics.getHeight()/2 + uiPaddingY));
        bPos = new Vector2((Gdx.graphics.getWidth()/2 - (2 * uiPaddingX) -  (2 * uiPedalSize)),(-Gdx.graphics.getHeight()/2 + uiPaddingY));
        lPos = new Vector2((-Gdx.graphics.getWidth()/2 + uiPaddingX), (-Gdx.graphics.getHeight()/2 + uiPaddingY));// + uiMoveSize +
        rPos = new Vector2((-Gdx.graphics.getWidth()/2 + (2 * uiMoveSize)), (-Gdx.graphics.getHeight()/2 + uiPaddingY ));

        //first value is field of view, the others get the width and height of the current device
        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        //this camera is for rendering the UI components in their static positions
        oCam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        cam.near = 1f;                      //closest we draw anything in this camera
        cam.far = 1000f;					//visibly cuts off camera view but techincally draws everything
        cam.update();						//apply changes to camera

        assets = new AssetManager();		//this loads from the /assets folder directly
        //Terrain tiles
        assets.load("roads.g3db", Model.class);
        assets.load("cube.g3db", Model.class);
        assets.load("chunk.g3db", Model.class);
        //player model, originally starting in a car
        assets.load("car.g3db", Model.class);
        //assets.load("skydome.g3db", Model.class);
        assets.load("grass.g3db", Model.class);
        assets.load("Zombie.g3db", Model.class);
        assets.load("zombieLow.g3db", Model.class);
        assets.load("zombieLow1.g3db", Model.class);
        assets.load("zombieLow2.g3db", Model.class);
        assets.load("zombieLow3.g3db", Model.class);
        assets.load("tree.g3db", Model.class);
        assets.load("rock.g3db", Model.class);
        assets.load("rock1.g3db", Model.class);
        assets.load("rock2.g3db", Model.class);
        assets.load("bush.g3db", Model.class);
        assets.load("bush1.g3db", Model.class);
        assets.load("bush2.g3db", Model.class);
        assets.load("tree.g3db", Model.class);
        assets.load("tree1.g3db", Model.class);
        assets.load("tree2.g3db", Model.class);
        assets.load("water.g3db", Model.class);
        assets.load("house1.g3db", Model.class);
        assets.load("house2.g3db", Model.class);
        assets.load("house3.g3db", Model.class);
        assets.load("house4.g3db", Model.class);
        assets.load("house5.g3db", Model.class);
        assets.load("hor.g3db", Model.class);
        assets.load("vert.g3db", Model.class);

        loading = true;                     //bool saying we havent finished loading our models yet

        viewport = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), cam);

        //we have three classes that collect user input, getInput which detects button clicks and holds
        //then bulletInput which raycasts where the user clicks, this stacks them
        //then our stage is for our menus, it doesnt detect holds, otherwise would replace input class

        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(stage);
        inputMultiplexer.addProcessor(new BulletInputProcessor(viewport, dynamicsWorld, this));
        inputMultiplexer.addProcessor(input);

        Gdx.input.setInputProcessor(inputMultiplexer);

        GLProfiler.enable();				//enable profiling

        //enable these to draw lines around collision models, pretty much useless, suuper laggy
        //debugDrawer = new DebugDrawer();
        //debugDrawer.setDebugMode(btIDebugDraw.DebugDrawModes.DBG_MAX_DEBUG_DRAW_MODE);
        //dynamicsWorld.setDebugMode();
        //dynamicsWorld.setDebugDrawer(debugDrawer);
    }

    void Initialize() {
        //this resets our world for when loading new stage
        //bulletInputProcessor detects clicks on retry or next stage, and calls this function to reset game
        Gdx.app.log("Warn", "RUNNING INITIALIZE");

        playerHealth = 100;
        gas = 100;

        loading = false;

        if (!initial) {
            level += 1;          //this changes the seed used to create the world
        } else {
            initial = false;    //initially we dont increment this counter, whenever we reload we increment it
        }

        //see if we have save data, if we dont, save our data - if we do then update our current level
        //the only data we really need to save it is our level and our levels scores(lateR)

        //we will also save our options data on game start, so player can adjust teh seed etc...

        //see if we have level data
        if(prefs == null){
            prefs = Gdx.app.getPreferences("My Preferences");
        }

        if(prefs.getInteger("Level", -1) == -1){//dont save level 0, only higher levels
            Gdx.app.log("Warn", "No save data found");
            prefs.putInteger("Level", level);
            prefs.flush();                              //needs to be called to update
        }
        else{//otherwsie we have save data and need to overwrite it
            //Gdx.app.log("Warn", "Found level save data " + level);
            int saveLevel = prefs.getInteger("Level");

            Gdx.app.log("Warn", "Found level save data " + level + " " + saveLevel);

            if(saveLevel > level){//then assign this level info
                level = saveLevel;
            }
            else{//otherwise keep current level and assign new one to save data
                prefs.putInteger("Level", level);
                prefs.flush();                              //needs to be called to update
            }
        }

        //reset our counter
        pHealth = 0;
        pGas = 0;
        pTime = 0;

        finalTime = -1;

        //reset all data for tracking passengers on minimap
        closeCounter = 0;
        closeDropCounter = 0;
        checkTime = 200;                    //time between registering collisions
        passengers = 0;

        frameTimer = 0;
        speed = 0;

        //player, the ground, and the water are drawn individually and are not part of instances, so just remove everything else
        //this clears our world from memory before we load a new one, or we will have problems
        for (int x = 0; x < instances.size; x++) {        //clears all our trees and houses
            instances.get(x).Destroy(dynamicsWorld);
        }

        instances.clear();
        renderZombies.clear();                            //this array collect zombies close enough for rendering, so just clear it

        //zHere - this is where we would reposition them and set their physics off -.setAwake(false), set position below ground
        //so on recreation dont delete our old zombies, just set them invisible and not working with physics then just move their position...
        //stems from errors in destroyig objects, can't figure it out, so lets just pool our zombies and not destroy any
        if(!initial){//first time through we dont have to do any of this

            Vector3 zp = new Vector3();
            for (int x = 0; x < zombiePool.size; x++) {

                if (zombiePool.get(x) != null) {
                    //zombiePool.get(x).zModel.body.forceActivationState(2);

                    zp = zombiePool.get(x).zModel.transform.getTranslation(zp);
                    //set their position below the ground so not visible
                    zombiePool.get(x).zModel.transform.setTranslation(zp.x, 20, zp.z);
                    //playerGo.bounds.set(new Vector3(0, 0, 0), new Vector3(1, 2, 1));
                    //so probably need to set the visibility and change the bounds, but can do later
                }
            }

            for (int x = 0; x < debris.size; x++) {           //this destroys any chunks from killing zomibes, shouldnt be any really
                debris.get(x).Destroy(dynamicsWorld);
            }

            zombiePoolCounter = 0;                              //reset our counter to pull needed zombie from our pool
        }

        instances = new Array<GameObject>();
        renderZombies = new Array<GameObject>();
        debris = new Array<GameObject>();

        world.InitializeWorld();  ///  remember this calls doneLoading when it is done, so we dont have to wait for it


    }

    public void doneLoading() {
        //this is called from the WorldGenerator class when it is done generating data for the world
        MakeWorld();

        baseTime = TimeUtils.millis();
        loading = false;
    }

    public void MakeWorld(){
        //our WorldGenerator creates all the arrays holding city data, this actually placed the models and add physics objects
        //this also adds trees and zombies randomly throughout the world
        // Gdx.app.log("Warn", "DONE LOADING toSave " + world.toSave.size);
        int random;

        //the pixmap is a pixel map, which is gets pixels assigned based on the world, then gets turned into a texture for a minimap
        pixmap = new Pixmap(100,100, Pixmap.Format.RGBA8888);
        //Fill it blue for water
        pixmap.setColor(Color.BLUE);
        pixmap.fill();

        //so our world class has the data about what tiles should be loaded at what spots in the world
        //so just iterate through its array and position the tiles accordingly
        /*  BLENDER MODELS
             Scale set to 1,1,1
             Apply rotations, positions and scales
             Make sure they have the right texture on the model, and in the uv editing window
             Rotations are different, so use the, just have to keep exporting till looks right

          */
        //this is a merged model that contains all our road tiles in one asset
        Model mainModel = assets.get("roads.g3db", Model.class);

        Model zombie = assets.get("zombieLow.g3db", Model.class);
        zombieArray.add(zombie);
        zombie = assets.get("zombieLow1.g3db", Model.class);
        zombieArray.add(zombie);
        zombie = assets.get("zombieLow2.g3db", Model.class);
        zombieArray.add(zombie);
        zombie = assets.get("zombieLow3.g3db", Model.class);
        zombieArray.add(zombie);

        Model car = assets.get("car.g3db", Model.class);
        Model ground = assets.get("grass.g3db", Model.class);

        Model tree = assets.get("tree.g3db", Model.class);
        treeArray.add(tree);
        Model tree1 = assets.get("tree1.g3db", Model.class);
        treeArray.add(tree1);
        Model tree2 = assets.get("tree2.g3db", Model.class);
        treeArray.add(tree2);

        //treeArray.add(tree);
        Model rock = assets.get("rock.g3db", Model.class);
        rockArray.add(rock);       ;
        Model rock1 = assets.get("rock1.g3db", Model.class);
        rockArray.add(rock1);
        Model rock2 = assets.get("rock2.g3db", Model.class);
        rockArray.add(rock2);

        Model bush = assets.get("bush.g3db", Model.class);
        bushArray.add(bush);
        Model bush1 = assets.get("bush1.g3db", Model.class);
        bushArray.add(bush1);
        Model bush2 = assets.get("bush2.g3db", Model.class);
        bushArray.add(bush2);


        Model cube = assets.get("cube.g3db", Model.class);
        otherCube = assets.get("chunk.g3db", Model.class);
        Model water = assets.get("water.g3db", Model.class);
        Model hor = assets.get("hor.g3db", Model.class);
        Model vert = assets.get("vert.g3db", Model.class);
        //downHouse = assets.get("house5.g3db", Model.class);
        Model house1 = assets.get("house1.g3db", Model.class);
        houseArray.add(house1);
        house1 = assets.get("house2.g3db", Model.class);
        houseArray.add(house1);
        house1 = assets.get("house3.g3db", Model.class);
        houseArray.add(house1);
        house1 = assets.get("house4.g3db", Model.class);
        houseArray.add(house1);
        house1 = assets.get("house5.g3db", Model.class);
        houseArray.add(house1);

        String id;
        GameObject instance;
        Node node;

        Gdx.app.log("Warn", "Going to make car");
        playerGo = new GameObject(car, 1f, 0, 4);
        playerGo.transform.setTranslation(new Vector3(new Vector3((world.mapSize / 2) * 8 + 5, 5, (world.mapSize / 2f) * -8 - 5)));//(new Vector3(30, 10, 30));//(new Vector3(new Vector3((world.mapSize / 2) * 8, 0, (world.mapSize / 2f) * -8)));
        playerGo.body.proceedToTransform(playerGo.transform);
        playerGo.bounds.set(new Vector3(0, 0, 0), new Vector3(1, 2, 1));
        playerGo.body.setUserValue(3);
        playerGo.body.setCollisionFlags(playerGo.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
        playerGo.body.setAngularFactor(new Vector3(0, 1, 0));
        //so we only want to be notified of when our car hits an enemy for now
        playerGo.body.setContactCallbackFlag(PLAYER_FLAG);
        playerGo.body.setContactCallbackFilter(ENEMY_FLAG);
        dynamicsWorld.addRigidBody(playerGo.body, PLAYER_FLAG, COMBINED_FLAG1);

        //this make it so our player doenst miss collisions even at high speed, continuous collision detection
        playerGo.body.setCcdMotionThreshold(.0000001f);
        playerGo.body.setCcdSweptSphereRadius(0.50f);

        //can use this to test a model, it will load next to player
        //playerGo1 = new GameObject(bushArray.get(0));
        //playerGo1.transform.setToTranslation(new Vector3(new Vector3((world.mapSize / 2) * 8 + 10, 5, (world.mapSize / 2f) * -8 - 10)));

        //example of adding events to our animator, currently not used, but good code
        // Pick the current animation by name
        /*
        controller.setAnimation("Walk", 1, new AnimationController.AnimationListener() {

            @Override
            public void onEnd(AnimationController.AnimationDesc animation) {
                // this will be called when the current animation is done.
                // queue up another animation called "balloon".
                // Passing a negative to loop count loops forever.  1f for speed is normal speed.
                controller.queue("Walk", -1, 1f, null, 0f);
            }

            @Override
            public void onLoop(AnimationController.AnimationDesc animation) {
                // TODO Auto-generated method stub

            }

        });
        */



        Gdx.app.log("Warn", "Going to terrain");

        int cacheX = 0;
        int cacheZ = 0;
        GameObject horInstance;

        Gdx.app.log("Warn", "Going to make ground");
        groundGo = new GameObject(ground, 0, .25f, 5);
        groundGo.transform.setTranslation(new Vector3(400, -.1f, -400));
        groundGo.body.proceedToTransform(groundGo.transform);
        groundGo.body.setUserValue(8);
        groundGo.body.setCollisionFlags(groundGo.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
        groundGo.body.setContactCallbackFlag(GROUND_FLAG);
        groundGo.body.setContactCallbackFilter(0);
        dynamicsWorld.addRigidBody(groundGo.body, GROUND_FLAG, ALL_FLAG);

        //make our simple water model with no collisions
        waterGo = new GameObject(water);
        waterGo.transform.setTranslation(new Vector3(0, -2, 0));
        waterGo.transform.scale(120, 1, 120);

        //so basically go through our worldTiles array on WorldGenerator and places the appropriate models
        for (int x = 0; x < world.mapSize; x++) {
            for (int y = 0; y < world.mapSize; y++) {

                id = world.tileType.get((int) world.worldTiles[x][y]);      //get the string representation of this tile
                int index = (int)world.worldTiles[x][y];                    //int representation

                if (index < 15) {
                    //then this is a road
                    pixmap.setColor(Color.GRAY);    //add a gray spot to the minimap
                    pixmap.drawPixel(x,99 - y);     //draw the pixel

                    GetRoad(x,y, mainModel, id);

                    random = world.GetRandomInt(0,9);

                    if(random > zombieChance){
                        GetZombiePool(x,y,false);
                    }

                }
                else if(index == 19){
                    //this is grass, grass tile dont get drawn, instead we draw one big one and draw everything else on top of it
                    pixmap.setColor(Color.valueOf("#145214"));
                    pixmap.drawPixel(x, 99 - y);
                    //if this is a grass tile, then we might place a tree, or a zombie
                    int thisIndex = -1;
                    int thisIndex1 = -1;

                    random = WorldGenerator.GetRandomInt(1,9);

                    if(random > treeChance) {

                        thisIndex = world.GetRandomInt(0, 3);
                        GetTree(x,y,false,thisIndex);
                    }

                    random = WorldGenerator.GetRandomInt(1,9);

                    if(random > rockChance) {

                        thisIndex1 = thisIndex;

                        while(thisIndex1 == thisIndex){
                            //this assure we get a number different than what we got for tree placement
                            //if we didnt place a tree, then they will both be -1, and then will use first random number
                            thisIndex1 = world.GetRandomInt(0, 3);
                        }

                        GetRock(x,y,false,thisIndex1);

                    }

                    random = WorldGenerator.GetRandomInt(1,9);

                    if(random > bushChance) {
                        //bushes are easy and have no collider...
                        int thisIndex2 = thisIndex1;
                        //this basically keeps rerolling a random number till it is not one already selected
                        while(thisIndex2 == thisIndex1 || thisIndex2 == thisIndex){
                            thisIndex2 = world.GetRandomInt(0, 3);
                        }

                        GetBush(x,y,false,thisIndex2);

                    }

                    random = world.GetRandomInt(0,9);

                    if(random > zombieChance + 2){
                        //GetZombie(x, y, false);
                        GetZombiePool(x,y,false);
                    }
                }
                else{
                    //create a house with a collider
                    pixmap.setColor(Color.WHITE);
                    pixmap.drawPixel(x, 99 - y);

                    instance = new GameObject(houseArray.get(world.GetRandomInt(0,4)), 0, 0, 6);
                    instance.transform.setTranslation(new Vector3(x * 8 + 4, 0, y * -8 - 4));

                    if(index == 15){//up facing house
                        instance.transform.rotate(new Vector3(0,1,0), 180);
                    }
                    else if(index == 16){
                        instance.transform.rotate(new Vector3(0, 1, 0), 90); // facing the right
                    }
                    else if(index == 17){
                        instance.transform.rotate(new Vector3(0,1,0), 270); // facing the left
                    }
                    else{
                        //no rotation needed
                    }

                    instance.body.proceedToTransform(instance.transform);
                    instance.bounds.set(new Vector3(x * 8 - 6, 0, y * -8 + 6), new Vector3(x * 8 + 6, 0, y * -8 - 6));
                    instance.body.setUserValue(5);										//this is used to tag us for collisions
                    instance.body.setCollisionFlags(instance.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);

                    dynamicsWorld.addRigidBody(instance.body, HOUSE_FLAG, ALL_FLAG);//, OBJECT_FLAG, GROUND_FLAG
                    instance.body.setContactCallbackFlag(HOUSE_FLAG);
                    instance.body.setContactCallbackFilter(PLAYER_FLAG);

                    cacheX = (int)(Math.floor(x / world.bucketRange));
                    cacheZ = (int)(Math.floor(y / world.bucketRange));

                    world.staticModels[cacheX][cacheZ].add(instance);

                    //check to see if we spawn a tree or a zombie at this location
                    random = WorldGenerator.GetRandomInt(1,9);

                    if(random > treeChance) {
                        GetTree(x,y,true, -1);
                    }

                    random = world.GetRandomInt(0,9);

                    if(random > zombieChance){
                        //GetZombie(x, y, true);
                        GetZombiePool(x,y,false);
                    }
                }


            }
        }
        //this finds our "barricades" and draws roads out over the water to whereever, player drops of passengers here
        //barricades are positions at the edge of the map where the player drops off passengers they pick up
        Vector3 pos = world.barricades[0];
        for(int p = 1; p < 10; p++) {
            horInstance = new GameObject(hor);
            horInstance.transform.setToTranslation((pos.x - p) * 8, 0, pos.z * -8);
            //Gdx.app.log("Warn", "Model position " + new Vector3((pos.x - p) * 8, 0, pos.z * -8) + " " + pos + " bounds min " + new Vector3(((pos.x - p) * 8), 0, pos.z * 8) + " max " + new Vector3(((pos.x - 1) * 8), 0, pos.z * 8));
            horInstance.bounds.set(new Vector3(((pos.x - p) * 8), 0, pos.z * -8), new Vector3(((pos.x - p) * 8 + 8), 0, pos.z * -8 - 8));
            instances.add(horInstance);
        }

        pos = world.barricades[1];

        for(int p = 1; p < 10; p++) {
            horInstance = new GameObject(hor);
            horInstance.transform.setToTranslation((pos.x + p) * 8, 0, pos.z * -8);
            //Gdx.app.log("Warn", "Model position " + new Vector3((pos.x + p) * 8, 0, pos.z * -8) + " " + pos);
            horInstance.bounds.set(new Vector3((pos.x + p) * 8, 0, pos.z * -8), new Vector3((pos.x - p) * 8 + 8, 1, pos.z * -8 - 8));
            instances.add(horInstance);
        }

        pos = world.barricades[2];

        for(int p = 1; p < 10; p++) {
            horInstance = new GameObject(vert);
            horInstance.transform.setToTranslation((pos.x) * 8, 0, (pos.z + p) * -8);
            // Gdx.app.log("Warn", "Model position " + new Vector3((pos.x) * 8, 0, (pos.z + p) * -8) + " " + pos);
            horInstance.bounds.set(new Vector3((pos.x) * 8, 0, (pos.z + p) * -8), new Vector3((pos.x) * 8 + 8, 1, (pos.z + p) * -8 - 8));
            instances.add(horInstance);
        }

        pos = world.barricades[3];

        for(int p = 1; p < 10; p++) {
            horInstance = new GameObject(vert);
            horInstance.transform.setToTranslation((pos.x) * 8, 2, (pos.z - p) * -8);
            //Gdx.app.log("Warn", "Model position " + new Vector3((pos.x) * 8, 0, (pos.z - p) * -8) + " " + pos);
            horInstance.bounds.set(new Vector3((pos.x) * 8, 0, (pos.z - p) * -8), new Vector3((pos.x) * 8 + 8, 1, (pos.z - p) * -8 - 8));
            instances.add(horInstance);
        }

        //now we need to genrate our model caches, which will wrap all our grouped models to reduce draw calls

        for(int x = 0; x < world.mapSize/world.bucketRange; x++) {
            for(int y = 0; y < world.mapSize/world.bucketRange; y++){
                //so everything in the world has been partitioned into groups (static models array), so here we combine
                //each model in the group into one to reduce draw calls dramatically...
                world.modelBuckets[x][y].begin();
                world.modelBuckets[x][y].add(world.staticModels[x][y]);
                world.modelBuckets[x][y].end();
                }
        }


        camPosition = new Vector3(0,9.5f, 3.5f);
        lookPosition = new Vector3(0,1.0f,-4.5f);

        //finish creating our minimap
        miniMapImg = new Texture(pixmap);
        pixmap.dispose();
    }

    void GetRoad(int x, int y, Model mainModel, String id){

        //our mainModel is a larger model that contains all our road tiles,
        roadInstance = new GameObject(mainModel, id, true);         //get from our main model the road type we need
        roadNode = roadInstance.getNode(id);
        roadInstance.transform.set(roadNode.globalTransform);
        roadNode.translation.set(x * 8, 0, y * -8);
        roadInstance.bounds.set(new Vector3(x * 8, 0, y * -8), new Vector3(x * 8 + 8, 5, y * -8 - 8));
        roadNode.rotation.idt();
        roadInstance.calculateTransforms();
        //so all models are passed to a list of nearby objects, then later they are combined to reduce draw calls
        int cacheX = (int)(Math.floor(x / world.bucketRange));
        int cacheZ = (int)(Math.floor(y / world.bucketRange));

        world.staticModels[cacheX][cacheZ].add(roadInstance);

    }

    void GetBush(int x, int y, boolean atHouse, int positionIndex){

        int bushIndex = world.GetRandomInt(0,2);
        bushInstance = new GameObject(bushArray.get(bushIndex));
        bushInstance.transform.setTranslation(new Vector3(x * 8, 0, y * -8).add(positions[positionIndex]));
        bushInstance.bounds.set((new Vector3(x * 8 - 6, 0, y * -8 + 6)), (new Vector3(x * 8 + 6, 1, y * -8 - 6)));

        int cacheX = (int)(Math.floor(x / world.bucketRange));
        int cacheZ = (int)(Math.floor(y / world.bucketRange));

        world.staticModels[cacheX][cacheZ].add(bushInstance);
    }

    void GetTree(int x, int y, boolean atHouse, int positionIndex){

        treeInstance = new GameObject(treeArray.get(world.GetRandomInt(0,2)), 0, 0, 0);

        if(atHouse) {
            treeInstance.transform.setTranslation(new Vector3(x * 8, 0, y * -8).add(housePositions[world.GetRandomInt(0, 3)]));//(new Vector3(x * 8 + (9-random), 0, y * -8 - (9-random)))
        }
        else{//this is to avoid place trees, rocks, and bushes at teh same spot
            if(positionIndex == -1){
                positionIndex = world.GetRandomInt(0, 3);
            }
            treeInstance.transform.setTranslation(new Vector3(x * 8, 0, y * -8).add(positions[positionIndex]));//(new Vector3(x * 8 + (9-random), 0, y * -8 - (9-random)))
        }

        treeInstance.body.proceedToTransform(treeInstance.transform);
        treeInstance.bounds.set((new Vector3(x * 8 - 6, 0, y * -8 + 6)), (new Vector3(x * 8 + 6, 1, y * -8 - 6)));
        treeInstance.body.setUserValue(7);                                        //this is used to tag us for collisions
        treeInstance.body.setCollisionFlags(treeInstance.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
        dynamicsWorld.addRigidBody(treeInstance.body, GROUND_FLAG, ALL_FLAG);//, OBJECT_FLAG, GROUND_FLAG
        treeInstance.body.setContactCallbackFlag(GROUND_FLAG);
        treeInstance.body.setContactCallbackFilter(PLAYER_FLAG);

        int cacheX = (int)(Math.floor(x / world.bucketRange));
        int cacheZ = (int)(Math.floor(y / world.bucketRange));

        world.staticModels[cacheX][cacheZ].add(treeInstance);
    }

    void GetRock(int x, int y, boolean atHouse, int positionIndex){

        int rockIndex = world.GetRandomInt(0,2);
        rockInstance = new GameObject(rockArray.get(rockIndex), 0, 0, 1);

        if(positionIndex != -1){
            rockInstance.transform.setTranslation(new Vector3(x * 8, 0, y * -8).add(positions[positionIndex]));//(new Vector3(x * 8 + (9-random), 0, y * -8 - (9-random)))
        }

        rockInstance.body.proceedToTransform(rockInstance.transform);
        rockInstance.bounds.set((new Vector3(x * 8 - 6, 0, y * -8 + 6)), (new Vector3(x * 8 + 6, 1, y * -8 - 6)));
        rockInstance.body.setUserValue(7);                                        //this is used to tag us for collisions
        rockInstance.body.setCollisionFlags(rockInstance.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
        dynamicsWorld.addRigidBody(rockInstance.body, GROUND_FLAG, ALL_FLAG);//, OBJECT_FLAG, GROUND_FLAG
        rockInstance.body.setContactCallbackFlag(GROUND_FLAG);
        rockInstance.body.setContactCallbackFilter(PLAYER_FLAG);

        int cacheX = (int)(Math.floor(x / world.bucketRange));
        int cacheZ = (int)(Math.floor(y / world.bucketRange));

        world.staticModels[cacheX][cacheZ].add(rockInstance);
    }
/*old method using normal gameobject instead of zombie class
    public void GetZombie(int x, int y, boolean atHouse){

        zombieInstance = new GameObject(zombieArray.get(world.GetRandomInt(0,3)), 1.5f, 10, true);
        Vector3 zombPos;

        if(!atHouse) {
            //int randX =
            zombPos = new Vector3(x * 8 + world.GetRandomInt(0, 8), 0, y * -8 - world.GetRandomInt(0, 8));
            zombieInstance.transform.setTranslation(zombPos);//(new Vector3(x * 8 + (9-random), 0, y * -8 - (9-random)))
        }
        else{
            zombPos = new Vector3(x * 8 + housePositions[world.GetRandomInt(0,3)].x, 0, y * -8 - housePositions[world.GetRandomInt(0,3)].z);
            zombieInstance.transform.setTranslation(zombPos);//(new Vector3(x * 8 + (9-random), 0, y * -8 - (9-random)))
        }


        zombieInstance.body.proceedToTransform(zombieInstance.transform);
        zombieInstance.bounds.set((new Vector3(x * 8 - 3, 0, y * -8 + 3)), (new Vector3(x * 8 + 3f, 0, y * -8 - 3f)));
        //now when we want to find our particular zombie, we will us zombies.get(zombie.body.getUserValue - 10)
        zombieInstance.body.setCollisionFlags(zombieInstance.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);

        dynamicsWorld.addRigidBody(zombieInstance.body, ENEMY_FLAG, COMBINED_FLAG);//, OBJECT_FLAG, GROUND_FLAG
        zombieInstance.body.setContactCallbackFlag(ENEMY_FLAG);
        zombieInstance.body.setContactCallbackFilter(0);
        zombieInstance.body.forceActivationState(0);

        //for reducing number of checks we group zombies in to spatial buckets for checking only if withing certain range
        //so this is weird since we have weird coordinates...
        zombPos.x = (int)(Math.floor(zombPos.x / world.tileSize / world.bucketRange));
        zombPos.z = (int)(Math.floor(Math.abs(zombPos.z) / world.tileSize / world.bucketRange));

        try{
            //Gdx.app.log("Warn", "Adding to buckets " + zombPos);
            //so if out of range for a bucket, then dont even make the zombie

            zombieInstance.body.setUserValue(zombieCounter);
            world.buckets[(int)zombPos.x][(int)zombPos.z].add(zombieInstance);
            //so this is a parallet array that holds which bucket our zombie belongs tp
            //when we move zombies around we may change their bucket
            //so this is a little weird, but the x,y of V3 are the bucket index, but the last position is a timer since the
            //last time we had a collision, this reduces collisions to happen every so often for damamge

            zombieBucket.add(new Vector3(zombPos.x, zombPos.z, 0));
            zombies.add(zombieInstance);
            zombieCounter++;
        }
        catch(ArrayIndexOutOfBoundsException e){
            Gdx.app.log("Warn", "Out of range " + zombPos);
        }


    }
*/
    private Zombie GetNextZombie(){
        //so this just grabs a zombie from our zombiepool based on our counter
        if(zombiePoolCounter < zombiePool.size){
            zombiePoolCounter++;
            return zombiePool.get(zombiePoolCounter - 1);
        }
        else{//then we need more zombies, create a new one and add to the pool

           zombiePoolCounter++;
           return new Zombie(zombieArray.get(world.GetRandomInt(0, 3)));   //this creates the model in the constructor
        }

    }
    private void GetZombiePool(int x, int y, boolean atHouse){

        //so initially there are no zombies, so create them, on later stages we reuse them, so just grab a random one...
        Zombie zombieInstance = GetNextZombie();

        //if(initial) {
            //zombieInstance = new Zombie(zombieArray.get(world.GetRandomInt(0, 3)));   //this creates the model in the constructor
        //}
       // else{
           // zombieInstance = GetNextZombie();
        //}

        Vector3 zombPos;

        //we apply a random position, which is different if tile contains a house
        if(!atHouse) {
            zombPos = new Vector3(x * 8 + world.GetRandomInt(0, 8), 0, y * -8 - world.GetRandomInt(0, 8));
            zombieInstance.zModel.transform.setTranslation(zombPos);
        }
        else{
            zombPos = new Vector3(x * 8 + housePositions[world.GetRandomInt(0,3)].x, 0, y * -8 - housePositions[world.GetRandomInt(0,3)].z);
            zombieInstance.zModel.transform.setTranslation(zombPos);
        }

        zombieInstance.zModel.body.proceedToTransform(zombieInstance.zModel.transform);
        zombieInstance.zModel.bounds.set((new Vector3(x * 8 - 3, 0, y * -8 + 3)), (new Vector3(x * 8 + 3f, 0, y * -8 - 3f)));
        //now when we want to find our particular zombie, we will us zombies.get(zombie.body.getUserValue - 10)
        zombieInstance.zModel.body.setCollisionFlags(zombieInstance.zModel.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);

        dynamicsWorld.addRigidBody(zombieInstance.zModel.body, ENEMY_FLAG, COMBINED_FLAG);//, OBJECT_FLAG, GROUND_FLAG
        zombieInstance.zModel.body.setContactCallbackFlag(ENEMY_FLAG);
        zombieInstance.zModel.body.setContactCallbackFilter(0);
        zombieInstance.zModel.body.forceActivationState(0); //forces all to sleep immediately, otherwise lags on load till they sleep naturally
        //zombieInstance.zModel.body.activate();

        //for reducing number of checks we group zombies in to spatial buckets for checking only if withing certain range
        //so this is weird since we have weird coordinates...
        zombPos.x = (int)(Math.floor(zombPos.x / world.tileSize / world.bucketRange));
        zombPos.z = (int)(Math.floor(Math.abs(zombPos.z) / world.tileSize / world.bucketRange));

        try{
            zombieInstance.zModel.body.setUserValue(zombieCounter);
            world.zBuckets[(int)zombPos.x][(int)zombPos.z].add(zombieInstance);
            zombieInstance.Initialize((int) zombPos.x, (int) zombPos.z, zombieCounter);//last parm us collision counter

            zombiePool.add(zombieInstance);
            zombieCounter++;
        }
        catch(ArrayIndexOutOfBoundsException e){//so if out of range for a bucket, then dont even make the zombie
            Gdx.app.log("Warn", "Out of range " + zombPos);
        }
    }

    @Override
    public void render() {
        ///this gets called every frame so keep that in mind
        stage.act();//for rendering our ui components

        //so if our player is outside of our map, end the game
        if (playerGo != null) {
            if (position.y < -2) {
                Gdx.app.log("Warn", "PLAYER OUTSIDE OF MAP");
                playerHealth = 0;
            }
        }

        //if player has run out of health, end the round
        if (playerHealth <= 0 && initialized && !mainMenu && !roundOver) {
            Gdx.app.log("Warn", "GETTING PLAYER DEAD");
            playerGo.body.setLinearVelocity(Vector3.Zero);
            playerGo.body.setAngularVelocity(Vector3.Zero);
            speed = 0;
            gameOver = true;
        }

        frameTimer += 1; //we can use this with modulous to do every other frame, every third frame ect.
        int visibleCount = 0;
        int totalCount = 0;

        //so now to start world we have to call world.InitializeWorld() -> which then calls doneLoading
        if (assets.update()) {
            //then we can access our models for world loading
            if (!mainMenu) {
                if (!initialized) {
                    initialized = true;
                    Initialize();
                }
            }
        }

        if(playerGo != null){
            //update the player speed relative to detected input
            //speed is reverse, so smaller number speeds up, larger slows down
            if (input.accelerating) {

                if (speed > -maxSpeed) {
                    if (speed >= 0) {
                        speed -= .2f;
                    } else if (speed < 0) {
                        speed -= .3f;
                    }
                } else {
                    speed = -maxSpeed;
                }
                //Gdx.app.log("Warn", "Aceelerating speed " + speed + " versus velocity " + playerGo.body.getLinearVelocity().z);
            }
            else if (input.braking) {

                if (speed < -maxRSpeed) {

                    if (speed <= 0) {
                        speed += .5f;
                    } else {
                        speed += .1f;
                    }
                    //Gdx.app.log("Warn", "Braking speed " + speed);
                } else {
                    speed = -maxRSpeed;
                }

            }
            else
            {
                if (speed < .1 && speed > -.1) {
                    speed = 0;
                } else {

                    if (speed < 0) {
                        //speed += .05f;
                        speed += .25f;
                    } else {
                        //speed -= .05f;
                        speed -= .15f;
                    }
                }
                //Gdx.app.log("Warn", "Doing nothing " + speed);
            }

            //this is a mess, we translate camera relative to a specific position behind the player, need to recompute every frame player moves
            //set our rotation and position to variables
            playerGo.transform.getRotation(tempRotation); //mTempRotation is a member Quaternion variable to avoid unnecessary instantiations every frame.
            playerGo.transform.getTranslation(position);
            //skydome.transform.setTranslation(player.transform.getTranslation(new Vector3(0,0,0)));

            mat = playerGo.transform.cpy();                    //get a copy of our player transform for calculating translations for camera
            mat.translate(camPosition);                        //move player copy camPosition relative to player
            mat.getTranslation(position1);                    //coverts to world position and assign to position1

            cam.position.set(position1);                        //set camera position to this positive realtive to player (+camPosition)
            //cam.position.lerp(position1, .5f);
            currentBackPoint = new Vector3(position1.x, 0, position1.z);

            mat.translate(new Vector3(lookPosition.x - camPosition.x, lookPosition.y - camPosition.y, lookPosition.z - camPosition.z));//lookPosition.sub(camPosition)	//so watn to camera to look at lookPosition, but subtract our our camPosition
            mat.getTranslation(position1);
            currentLookPoint = position1;

            playerPosition = playerGo.transform.getTranslation(new Vector3(0, 0, 0));
            direction = new Vector3(playerPosition.x - position1.x, 0, playerPosition.z - position1.z).nor();
            //Gdx.app.log("Warn", "Direction " + direction.nor());
            playerGo.body.setAngularVelocity(new Vector3(0, 0, 0));

            if (speed != 0 && !roundOver && !gameOver) {
                //Gdx.app.log("Warn", "Calllnig velocity code, direction " + direction + " speed " + speed);
                //annoying, was getting weird movement, but it was because rigidbody was going to sleep, this wakes it up
                playerGo.body.activate();
                playerGo.body.setLinearVelocity(new Vector3(direction.x * speed, playerGo.body.getAngularVelocity().y, direction.z * speed));
                playerGo.body.setAngularVelocity(new Vector3(0, 0, 0));
                //now update our gas
                gas += speed / maxSpeed * gasUse;
            }
            //really important, must use the physics library methods for model movement to coordinate with physics, Unity and here
            if (input.turningLeft) {
                playerGo.transform.rotate(0, 1, 0, turnSpeed * Gdx.graphics.getDeltaTime());//.translate(0, 0, 1 * Gdx.graphics.getDeltaTime()
            }
            if (input.turningRight) {
                playerGo.transform.rotate(0, 1, 0, -turnSpeed * Gdx.graphics.getDeltaTime());//.translate(0, 0, 1 * Gdx.graphics.getDeltaTime()
            }
            //shouldnt need to do this anymore, but have to for rotating till switch to angularvelocity
            playerGo.body.setWorldTransform(playerGo.transform);

            cam.lookAt(position1);                            //look to our camPosition relative to player
            cam.up.set(Vector3.Y);                            ///camera was slanted on the z, but this fixed it
        }//end player movement and rotation

        //deal with moving and determining rendering for all our zombies/enemies
        if(playerGo != null && !roundOver && !gameOver && !mainMenu) {

            playerGo.transform.getTranslation(position2);
            //so player bucket is their locations, divided by tileSize
            playerBucket.x = (float)Math.floor(position2.x/ world.tileSize / world.bucketRange);
            playerBucket.y = 0;
            playerBucket.z = (float)Math.floor(Math.abs(position2.z/ world.tileSize / world.bucketRange));

            count = 0;
            zRange = Math.pow((float)(world.tileSize * zombieRange),2);
            index = 0;
            renderZombies.clear();						//clear our render zombies array

            //the whole world is already loaded and filled with zombies.  They have been partitioned into groups so we can
            //check only the zombies close to the player. SO here we check the ones that share our tile and the other 8 surrounding us
            for (int c = 0; c < 9; c++) {

                around.x = playerBucket.x + directions[c].x;
                around.z = playerBucket.z + directions[c].z;

                if(around.x > 19 || around.x < 0 || around.z > 19 || around.z < 0 ){
                    continue;
                }
                //cache the count of the bucket we are in
                count = world.zBuckets[(int) around.x][(int) around.z].size;

                thisGo = null;
                //go through all the zombies attached to this bucket
                //want to go backwards through the array, since we may remove an element and wont mess up our index
                for (int v = count - 1; v > -1; v--) {

                    try {
                        thisGo = world.zBuckets[(int) around.x][(int) around.z].get(v).zModel; //zHere
                    } catch (Exception ex) {
                        Gdx.app.log("Warn", "Zombie not there ");
                        continue;
                    }

                    if (thisGo == null) {
                        Gdx.app.log("Warn", "Zombie null ");
                        continue;
                    }
                    renderZombies.add(thisGo);                      //add this zombie to get rendered this frame


                    //if(frameTimer%2 == 0){                          //only update basic stuff every other frame, half processing power
                    //get world position of this zombie
                    thisGo.transform.getTranslation(position1);
                    distance = position2.dst2(position1);
                    AnimationController.AnimationDesc desc = thisGo.animator.current;
                    boolean wander = false;

                    if (distance > zRange) {
                        //so for now, if they are too far away, just have them stand there
                        //otherwise we shoudl proabably just move in a random direction since probably visible
                        continue;
                    }
                    //check zombie distance, if close then run, otherwise walk
                    if (distance < zRange / 10) {
                        if (desc.animation.id.equals("Walk")) {
                            //if pretty close to player then start to run towards them
                            thisGo.animator.animate("run", -1, 2, null, .25f);
                        }
                        currentSpeed = zombieSpeed * 2;
                    } else {
                        if (desc.animation.id.equals("Run")) {
                            thisGo.animator.animate("walk", -1, 1, null, .25f);
                        }
                        currentSpeed = zombieSpeed;
                    }

                    random = new Vector3(position1.x + world.GetRandomInt(-10, 10), position1.y, position1.z + world.GetRandomInt(-10, 10));
                    thisGo.body.activate();
                    thisGo.animator.update(Gdx.graphics.getDeltaTime());

                    direction = (position2).sub(position1).nor();                        //get the normalized direction between player and zombie
                    direction.set(-direction.x, 0, -direction.z);                        //set direction opposite, not really sure why
                    instanceRotation = thisGo.transform.cpy();//.mul(thisGo.transform);	//why are we multiplying by ourself, cahnged and still seems to work
                    instanceRotation.setToLookAt(direction, new Vector3(0, -1, 0));
                    instanceRotation.rotate(0, 0, 1, 180);
                    instanceRotation.getRotation(quaternion);

                    thisGo.transform.getTranslation(position1);
                    playerGo.transform.getTranslation(position2);

                    if (position2.dst(position1) < 2) {
                        currentSpeed = 1;
                    }

                    //the offset by 1000 keeps thing in the positive realm
                    if (!wander) {
                        direction = new Vector3(position2.x - position1.x + 1000, position2.y - position1.y + 1000, position2.z - position1.z + 1000);//position2).sub(position1).nor();
                        direction = new Vector3((direction.x - 1000), (direction.y - 1000), (direction.z - 1000)).nor();//position2).sub(position1).nor();
                        direction = new Vector3((direction.x * currentSpeed), (direction.y * currentSpeed), (direction.z * currentSpeed));
                    } else {
                        direction = new Vector3(random.x - position1.x + 1000, random.y - position1.y + 1000, random.z - position1.z + 1000);//position2).sub(position1).nor();
                        direction = new Vector3((direction.x - 1000), (direction.y - 1000), (direction.z - 1000)).nor();//position2).sub(position1).nor();
                        direction = new Vector3((direction.x * currentSpeed), (direction.y * currentSpeed), (direction.z * currentSpeed));
                    }

                    thisGo.transform.getRotation(oldRotation);

                    thisGo.transform.set(position1, oldRotation.slerp(quaternion, .08f * currentSpeed / zombieSpeed));
                    //thisGo.body.activate();                                                            //make sure physics are enabled
                     //direction = direction.add(thisGo.body.getLinearVelocity());
                    thisGo.body.setLinearVelocity(thisGo.body.getLinearVelocity().lerp(direction, .25f * currentSpeed/zombieSpeed));                                        ///set our velocity
                    //new Vector3(direction.x/2, direction.y/2, direction.z/2)
                    thisGo.body.setWorldTransform(thisGo.transform);
                    //if we dont set the bounds then culling doesnt work properly, and zombies will flicker
                    thisGo.bounds.set(new Vector3(position1.x - 1, 0, position1.z + 1), new Vector3(position1.x + 1, .1f, position1.z - 1));
                    //thisGo.animator.update(Gdx.graphics.getDeltaTime());
                    index = thisGo.body.getUserValue() - 10;            //this is our position in the zombies array and zombieBucket array

                    newBucket.x = (int) (Math.floor(position1.x / world.tileSize / world.bucketRange));
                    newBucket.y = (int) (Math.floor(Math.abs(position1.z) / world.tileSize / world.bucketRange));

                    if (newBucket.x != around.x || newBucket.y != around.z) {
                        //so if the proper bucket for this zombie is not its current one, need to change it
                        try {
                            world.zBuckets[(int) newBucket.x][(int) newBucket.y].add(world.zBuckets[(int) around.x][(int) around.z].get(v));//zHere
                            world.zBuckets[(int) around.x][(int) around.z].get(v).Update((int) newBucket.x, (int) newBucket.y, 0);
                            //finally remove us from our original bucket
                            world.zBuckets[(int) around.x][(int) around.z].removeValue(world.zBuckets[(int) around.x][(int) around.z].get(v), true);//zHere
                            //Gdx.app.log("Warn", "Changing zombie bucket from " + around.x + ", " + around.z + " to " + newBucket.x + ", " + newBucket.y + " player bucket " + playerBucket);
                        } catch (Exception ex) {
                            Gdx.app.log("Warn", "Cant move out of bounds, keeping current bucket ");
                        }
                    }
                //}
                }
                total += count;
            }
        }

        cam.update();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        int notVisible = 0;


        if(initialized && !loading) {
            //draw our world models
            modelBatch.begin(cam);

            visibleCount = 0;

            totalCount = 0;
            notVisible = 0;
            int zombieCount = 0;
            //so we go through each item around us and determine if it is visible or not

            for (int x = 0; x < instances.size; x++) {
                //this still handles our barricades
                if (cam.frustum.boundsInFrustum(instances.get(x).bounds)) {
                    visibleCount += 1;
                    modelBatch.render(instances.get(x), environment);
                }
                else {
                    notVisible++;
                }
                totalCount++;
            }

            for (int x = 0; x < world.mapSize / world.bucketRange; x++) {
                for (int y = 0; y < world.mapSize / world.bucketRange; y++) {

                    if (cam.frustum.boundsInFrustum(world.cacheBounds[x][y])) {
                        visibleCount++;
                        modelBatch.render(world.modelBuckets[x][y], environment);
                    } else {
                        notVisible++;
                    }
                    totalCount++;
                }
            }

            for (int x = 0; x < renderZombies.size; x++) {

                if (cam.frustum.boundsInFrustum(renderZombies.get(x).bounds)) {
                    visibleCount += 1;
                    zombieCount++;
                    modelBatch.render(renderZombies.get(x), environment);
                } else {
                    //Gdx.app.log("Warn","Zombie out of bounds " + renderZombies.get(x).bounds + " pos " + renderZombies.get(x).transform.getTranslation(Vector3.Zero));
                    notVisible++;
                }
                totalCount++;
            }
            //go through and render all our world strips

            if (waterGo != null) {
                modelBatch.render(waterGo, environment);
                modelBatch.render(debris, environment);
                modelBatch.render(groundGo, environment);
                modelBatch.render(playerGo, environment);
            }
            if (playerGo1 != null) {
                modelBatch.render(playerGo1, environment);
            }
            //Gdx.app.log("Warn", "Total zombies " + zombieCount + " total could render " + renderZombies.size);
                    //Gdx.app.log("Warn", "Environment " + envirCount + " zombies " + zombCount + " debris " + debris.size + " total possible zombs " + renderZombies.size);
            modelBatch.end();

            //go through backwards and remove the debris that has fallen under the ground
            try {
                int d = debris.size - 1;

                for (int x = d; x > -1; x--) {

                    if (debris.get(x).transform.getTranslation(new Vector3(0, 0, 0)).y < -10) {
                        debris.get(x).Destroy(dynamicsWorld);
                        debris.removeIndex(x);
                        //Gdx.app.log("Warn", "Removing debris");
                    }
                }
            }
            catch(Exception ex){
                Gdx.app.log("Warn", ex.getMessage()  + " Error ");
            }

        }
        //perform physics step
        final float delta = Math.min(1f / 30f, Gdx.graphics.getDeltaTime());
        if(!roundOver && !gameOver) {
            dynamicsWorld.stepSimulation(delta, 10, 1f / 30f);
        }
        //this needs to be done for all moving bodies
        //might not need to do this anymore since using motion states...
        if(playerGo != null){
            playerGo.body.getWorldTransform(playerGo.transform);
            //Gdx.app.log("Warn", "Player world y is : " + playerGo.transform.getTranslation(new Vector3(0,0,0)).y);
        }

        //debugDrawer.begin(cam);
        //dynamicsWorld.debugDrawWorld();
        //debugDrawer.end();

        float finalDist = 1000;
        //draw our gui images
        spriteBatch.setProjectionMatrix(oCam.combined);		//this makes the ui use the orthographic camera
        spriteBatch.begin();

        if(mainMenu){
            //these two below will only happen if they have finished all stages and playing again
            //if end of round stuff showing turn it off
            if(totalLabel.isVisible() == true){
                gasLeftLabel.setVisible(false);
                dmgLabel.setVisible(false);
                timeLeftLabel.setVisible(false);
                lineLabel.setVisible(false);
                totalLabel.setVisible(false);
                finalLabel.setVisible(false);
            }
            //if map stuff is visible, disable it
            if(label.isVisible()){
                label.setVisible(false);
                passLabel.setVisible(false);
                timeLabel.setVisible(false);
                distanceLabel.setVisible(false);
            }
            //if our maini menu is up, draw it and return no need for anyting else
            startGame.setVisible(true);
            settings.setVisible(true);
            //then draw our main menu and return
            //Gdx.app.log("Warn", "CACHE NULL " + Gdx.graphics.getWidth() + " " + Gdx.graphics.getHeight());
            spriteBatch.draw(logoBackground, -Gdx.graphics.getWidth() / 2, -Gdx.graphics.getHeight() / 2, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            spriteBatch.draw(logo, -Gdx.graphics.getWidth() / 2 / 2, 100, Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 5);
            spriteBatch.end();
            stage.draw();
            return;
        }
        else{
            if(startGame.isVisible()){
                startGame.setVisible(false);
                settings.setVisible(false);
            }
            if(!label.isVisible()){
                label.setVisible(true);
                passLabel.setVisible(true);
                timeLabel.setVisible(true);
                distanceLabel.setVisible(true);
            }
        }


        float dist = 0;
        drawTextureRegion(greenButtonRegion,(int)gPos.x,(int)gPos.y);

        if(!loading) {
            //draw our debugging information
            stringBuilder.setLength(0);
            stringBuilder.append(" FPS: ").append(Gdx.graphics.getFramesPerSecond());
            stringBuilder.append(" Total: ").append(totalCount);
            stringBuilder.append(" Visible: ").append(visibleCount);
            stringBuilder.append(" Culled: ").append(notVisible);
            stringBuilder.append(" Calls: ").append(GLProfiler.drawCalls);
            stringBuilder.append(" Verts: ").append(GLProfiler.vertexCount.total);
            label.setSize(200, 400);
            label.setText(stringBuilder);
            //draw our minimap
            spriteBatch.draw(miniMapImg, (Gdx.graphics.getWidth() / 2 - 410), (Gdx.graphics.getHeight() / 2 - 410), 400f, 400f); // width then height

            playerGo.transform.getTranslation(playerPosition);

            playerIndex.x = (float)((playerPosition.x / world.tileSize / world.bucketRange));
            playerIndex.z = (float)((Math.abs(playerPosition.z) / world.tileSize / world.bucketRange));

            playerPosition = new Vector3(playerPosition.x/2,0, playerPosition.z/2);

            int toRemove = -1;
            boolean somethingClose = false;
            //now here we draw icons over the minimap to show the player where to go
            if(passengers > 0){
                //we are full and have all the passengers we can carry
                //then we need to draw targets at our barricade positions to tell player where to go
                //only do this once
                for(int u = 0; u < 4; u++){
                    //if we have full passengers highlight the dropoff locations to make obvious what to do
                    if(passengers == 3) {
                        Vector3 pos = new Vector3(world.barricades[u].x * 4, 0, (world.barricades[u].z * 4));
                        drawTextureRegion(targetRegion, (int) (Gdx.graphics.getWidth() / 2 - 410 + pos.x - 16), (int) (Gdx.graphics.getHeight() / 2 - 410 + pos.z - 16));
                    }
                    dist = (float) world.Distance(playerIndex, new Vector3(world.barricades[u].x / 5, 0, (world.barricades[u].z / 5)));

                    if (dist < finalDist) {
                        finalDist = dist;
                        //so if we are close enough to a house that have a survivor, we start a counter
                        //once a player waits long enough we will pick them up and they are a passenger
                        if (finalDist < .3f) {
                            somethingClose = true;
                            //Gdx.app.log("Warn", "Close enough " + finalDist + " closeCounter " + closeCounter);
                            closeDropCounter += 1;

                            if (closeDropCounter > (passengers * 100)) {
                                passengers = 0;
                                closeDropCounter = 0;
                            }
                        }
                    }
                }
            }
            //if we werent close to something stop our counter
            if(!somethingClose){
                closeDropCounter = 0;
            }
            //reset this to false since used in other method
            somethingClose = false;

            for(int x = 0; x < world.toSave.size; x++){
                //for each person to save still draw an indicator on the minimap
                if(passengers == 3){
                    //then dont draw our pasenger locations, and dont check for collections
                }
                else {

                    Vector3 pos = new Vector3(world.toSave.get(x).x * 4, 0, (world.toSave.get(x).z * 4));//new Vector3(400,0,400);//
                    dist = (float) world.Distance(playerIndex, new Vector3(world.toSave.get(x).x / 5, 0, (world.toSave.get(x).z / 5)));

                    if (dist < finalDist) {
                        //somethingClose = true;
                        finalDist = dist;
                        //so if we are close enough to a house that have a survivor, we start a counter
                        //once a player waits long enough we will pick them up and they are a passenger
                        if (finalDist < .3f) {
                            somethingClose = true;
                            //Gdx.app.log("Warn", "Close enough " + finalDist + " closeCounter " + closeCounter);
                            closeCounter += 1;

                            if (closeCounter > 300) {
                                passengers++;
                                toRemove = x;
                                closeCounter = 0;
                                //Gdx.app.log("Warn", "Close enough to remove " + finalDist + " closeCounter " + closeCounter);
                            }
                        }
                    }
                    drawTextureRegion(targetRegion, (int) (Gdx.graphics.getWidth() / 2 - 410 + pos.x - 8), (int) (Gdx.graphics.getHeight() / 2 - 410 + pos.z - 8));
                }
            }

            if(!somethingClose){
                closeCounter = 0;
            }

            if(world.toSave.size == 0 && passengers == 0){
                //everyone collected, show main menu and total points for stage
                roundOver = true;
            }

            if(!somethingClose){
                closeCounter = 0;
            }

            if(toRemove > -1){
                world.toSave.removeIndex(toRemove);
            }

            //draw our health and gas
            drawTextureRegion(targetRegion, (int) (Gdx.graphics.getWidth() / 2 - 410 + playerPosition.x - 16), (int) (Gdx.graphics.getHeight() / 2 - 410 - playerPosition.z - 16));
            drawTextureRegion(healthbarRegion, (int) -Gdx.graphics.getWidth() / 2 + 20, (int) Gdx.graphics.getHeight() / 2 - 80);
            drawTextureRegion(healthbar1Region, (int) -Gdx.graphics.getWidth() / 2 + 20, (int) Gdx.graphics.getHeight() / 2 - 80, ((float) playerHealth / (float) maxPlayerHealth));
            drawTextureRegion(healthbarRegion, (int) -Gdx.graphics.getWidth() / 2 + 20, (int) Gdx.graphics.getHeight() / 2 - 165);
            drawTextureRegion(gasBarRegion, (int) -Gdx.graphics.getWidth() / 2 + 20, Gdx.graphics.getHeight() / 2 - 165, (float) gas / maxGas);

            if (gameOver) {
                //now if we are on level 5, then they are finished and we should total all their score for the stage
                //then let them return to teh main menu
                spriteBatch.draw(menu, -400, -300, 800, 600);
                spriteBatch.draw(gameOverPic, -280, 50, 536, 150);

                if(gameOverTimer == gameOverWait) {
                    spriteBatch.draw(retryPic, -210, -200, 425, 80);
                }
                else{
                    gameOverTimer++;
                }

            }
            if(roundOver) {

                if(level == maxLevel){
                    spriteBatch.draw(menu, -400, -500, 800, 1000);
                    spriteBatch.draw(congratsPic, -280, 325, 536, 90);

                    if (playerHealth == 0 && gas == 0 && finalTime == 0) {
                        //done adding up our player, total show click to continue
                        spriteBatch.draw(returnPic, -210, -325, 425, 80);
                    }
                }
                else {
                    spriteBatch.draw(menu, -400, -500, 800, 1000);
                    spriteBatch.draw(roundOverPic, -280, 325, 536, 90);

                    if (playerHealth == 0 && gas == 0 && finalTime == 0) {
                        //done adding up our player, total show click to continue
                        spriteBatch.draw(continuePic, -210, -200, 425, 80);
                    }
                }
            }

            if(closeCounter > 0){
                drawTextureRegion(healthbarRegion, (int) -Gdx.graphics.getWidth() / 2 + 20, (int) Gdx.graphics.getHeight() / 2 - 250);
                drawTextureRegion(collectBarRegion, (int) -Gdx.graphics.getWidth() / 2 + 20, Gdx.graphics.getHeight() / 2 - 250, (float) closeCounter/300);
             }
            else if(closeDropCounter > 0){
                drawTextureRegion(healthbarRegion, (int) -Gdx.graphics.getWidth() / 2 + 20, (int) Gdx.graphics.getHeight() / 2 - 250);
               drawTextureRegion(collectBarRegion, (int) -Gdx.graphics.getWidth() / 2 + 20, Gdx.graphics.getHeight() / 2 - 250, (float) closeDropCounter/(passengers * 100));
            }
        }
        drawTextureRegion(redButtonRegion, (int) bPos.x, (int) bPos.y);
        drawTextureRegion(leftArrowRegion, (int)lPos.x,(int) lPos.y);
        drawTextureRegion(rightArrowRegion, (int)rPos.x,(int) rPos.y);

        spriteBatch.end();

        stringBuilder.setLength(0);
        stringBuilder.append(" FPS: ").append(Gdx.graphics.getFramesPerSecond());
        stringBuilder.append("  Visible: ").append(visibleCount);
        stringBuilder.append("  Culled: ").append(notVisible);
        stringBuilder.append("  Possible: ").append(totalCount);
        //Gdx.app.log("Warn", "STATS: Vertices: " + GLProfiler.vertexCount.total + " Drawcalls: " + GLProfiler.drawCalls);
        stringBuilder.append("  DrawCalls: ").append(GLProfiler.drawCalls);
        stringBuilder.append("  Vertices: ").append(GLProfiler.vertexCount.total);
        label.setSize(200, 400);
        label.setText(stringBuilder);
        label.setPosition(Gdx.graphics.getWidth()/2 - 475, Gdx.graphics.getHeight() - 250);

        stringBuilder.setLength(0);
        float conv = Math.round(finalDist * 100);
        stringBuilder.append("Closest:         ").append(conv / 100.00);
        distanceLabel.setSize(200, 100);
        distanceLabel.setText(stringBuilder);
        distanceLabel.setPosition(Gdx.graphics.getWidth() - 300, Gdx.graphics.getHeight() - 500);

        stringBuilder.setLength(0);
        stringBuilder.append("Passengers:  ").append(passengers).append("/3");
        passLabel.setSize(200, 100);
        passLabel.setText(stringBuilder);
        passLabel.setPosition(Gdx.graphics.getWidth() - 300, Gdx.graphics.getHeight() - 540);

        stringBuilder.setLength(0);
        stringBuilder.append("Timer:            ").append((TimeUtils.millis() - baseTime) / 1000);
        timeLabel.setSize(200, 100);
        timeLabel.setText(stringBuilder);
        timeLabel.setPosition(Gdx.graphics.getWidth() - 300, Gdx.graphics.getHeight() - 580);


        if(roundOver){

            if (finalTime == -1) {
                // Gdx.app.log("Warn", "Ending round " +  );
                //this inplicityly only happens once at the end of the round
                gasLeftLabel.setVisible(true);
                dmgLabel.setVisible(true);
                timeLeftLabel.setVisible(true);
                lineLabel.setVisible(true);
                totalLabel.setVisible(true);

                if(baseTime == 0){
                    baseTime = TimeUtils.millis();
                }
                //our tiem allowed per level is 30 seconds base + 30 for each city
                finalTime = (world.numberOfCities[level] * 30 + 30) - (int)(TimeUtils.millis() - baseTime)/1000;

                if(finalTime < 0){
                    finalTime = 0;
                }
                if(playerHealth < 0){
                    playerHealth = 0;
                }
                if(gas < 0){
                    gas = 0;
                }

                pHealth = (playerHealth * 100);
                pGas = (((int)gas) * 25);
                pTime = (finalTime * 10);
                Gdx.app.log("Warn", "Ending round " +  pHealth + " " + playerHealth + " " + gas + " " + pGas + " " + finalTime + " " + pTime);
            }

            //this will make our total round point increment while visible to user
            if(frameTimer % 2 == 0) {

                if (playerHealth != 0) {
                    playerHealth -= 1;
                }
                else if(playerHealth < 0){
                    playerHealth = 0;
                }

                if (gas != 0) {
                    gas = (int)gas - 1;
                }
                else if(gas < 0){
                    gas = 0;
                }

                if (finalTime != 0) {
                    finalTime -= 1;
                }
                else if(finalTime < 0){
                    finalTime = 0;
                }
            }

            stringBuilder.setLength(0);
            stringBuilder.append("HEALTH REMAINING: ").append(playerHealth).append("   X 100  =  ").append((int)(pHealth - playerHealth * 100));
            dmgLabel.setSize(200, 200);
            dmgLabel.setText(stringBuilder);
            dmgLabel.setPosition(Gdx.graphics.getWidth()/2 - 295, Gdx.graphics.getHeight()/2 + 150);

            stringBuilder.setLength(0);
            stringBuilder.append("GAS REMAINING:        ").append((int)gas).append("   X  25    =  ").append((int)(pGas - gas * 25));
            gasLeftLabel.setSize(200, 200);
            gasLeftLabel.setText(stringBuilder);
            gasLeftLabel.setPosition(Gdx.graphics.getWidth()/2 - 295, Gdx.graphics.getHeight()/2 + 90);

            stringBuilder.setLength(0);
            stringBuilder.append("TIME REMAINING:       ").append((int)(finalTime)).append("   X  10    =  ").append((int)((int)(pTime - (finalTime) * 10)));
            timeLeftLabel.setSize(200, 200);
            timeLeftLabel.setText(stringBuilder);
            timeLeftLabel.setPosition(Gdx.graphics.getWidth()/2 - 295, Gdx.graphics.getHeight()/2 + 30);

            stringBuilder.setLength(0);
            stringBuilder.append("--------------------------------------------------------- ");
            lineLabel.setSize(200, 200);
            lineLabel.setText(stringBuilder);
            lineLabel.setPosition(Gdx.graphics.getWidth()/2 - 295, Gdx.graphics.getHeight()/2 + -30);

            if (level == maxLevel) {
                //then this is final level, show screen that total all total from all stages
                //Gdx.app.log("Warn", "DRAWING FINAL NUMBERS");
                finalLabel.setVisible(true);
                int thisTotal = ((int) ((pHealth - playerHealth * 100) + (pGas - (int) gas * 25) + (pTime - finalTime * 10)));
                totalLabel.setVisible(true);
                stringBuilder.setLength(0);
                //float conv = Math.round(finalDist * 100);
                stringBuilder.append("TOTAL:                                               ").append((int) ((pHealth - playerHealth * 100) + (pGas - (int) gas * 25) + (pTime - finalTime * 10)));
                totalLabel.setSize(200, 200);
                totalLabel.setText(stringBuilder);
                totalLabel.setPosition(Gdx.graphics.getWidth() / 2 - 295, Gdx.graphics.getHeight() / 2 - 90);

                levelScores[level] = thisTotal;

                if(playerHealth == 0 && gas == 0 && finalTime == 0){
                    //Gdx.app.log("Warn", "DRAWING TOTALs");

                    int total = 0;

                    for (int x = 0; x < maxLevel + 1; x++) {
                        total += levelScores[x];
                    }

                    stringBuilder.setLength(0);
                    stringBuilder.append("TOTAL ALL ROUNDS:                    ").append(total);
                    finalLabel.setSize(200, 200);
                    finalLabel.setText(stringBuilder);
                    finalLabel.setPosition(Gdx.graphics.getWidth() / 2 - 295, (Gdx.graphics.getHeight() / 2) -200);
                }

            }
            else{
                stringBuilder.setLength(0);
                levelScores[level] = ((int)((pHealth - playerHealth * 100) + (pGas - (int)gas * 25) + (pTime - finalTime * 10 )) );
                stringBuilder.append("TOTAL:                                               ").append((int)((pHealth - playerHealth * 100) + (pGas - (int)gas * 25) + (pTime - finalTime * 10 )) );
                totalLabel.setSize(200, 200);
                totalLabel.setText(stringBuilder);
                totalLabel.setPosition(Gdx.graphics.getWidth()/2 - 295, Gdx.graphics.getHeight()/2 -90);
            }
        }
        else{

            if(totalLabel.isVisible() == true){
                gasLeftLabel.setVisible(false);
                dmgLabel.setVisible(false);
                timeLeftLabel.setVisible(false);
                lineLabel.setVisible(false);
                totalLabel.setVisible(false);
                finalLabel.setVisible(false);
            }

        }
        stage.draw();
        GLProfiler.reset();
    }

    void drawTextureRegion(TextureRegion t, int x, int y){
        spriteBatch.draw(t, x, y, t.getRegionWidth(), t.getRegionHeight());
    }
    void drawTextureRegion(TextureRegion t, int x, int y, float widthScalar){
        spriteBatch.draw(t, x, y, t.getRegionWidth()  * widthScalar, t.getRegionHeight());
    }
/*
    boolean checkCollision(btCollisionObject obj0, btCollisionObject obj1) {
        //wraps our objects to use bullet library
        CollisionObjectWrapper co0 = new CollisionObjectWrapper(obj0);
        CollisionObjectWrapper co1 = new CollisionObjectWrapper(obj1);

        //this takes two shapes and finds the right algorithm to detect collisions
        btCollisionAlgorithm algorithm = dispatcher.findAlgorithm(co0.wrapper, co1.wrapper);

        btDispatcherInfo info = new btDispatcherInfo();
        btManifoldResult result = new btManifoldResult(co0.wrapper, co1.wrapper);

        algorithm.processCollision(co0.wrapper, co1.wrapper, info, result);

        //if we have anything on our hitlist, then we have a collision to set return value
        boolean hitSomething = result.getPersistentManifold().getNumContacts() > 0;

        dispatcher.freeCollisionAlgorithm(algorithm.getCPointer());
        result.dispose();
        info.dispose();
        co1.dispose();
        co0.dispose();

        return hitSomething;
    }

    private Vector3 thisPos = new Vector3();

    protected boolean isVisible(final Camera cam, final GameObject instance) {
        //instance.transform.getTranslation(thisPos);
        //thisPos.add(instance.center);

        return cam.frustum.boundsInFrustum(instance.bounds);
        //return cam.frustum.boundsInFrustum(thisPos, instance.dimensions);
    }

    public void CreateCube(Vector3 pos, Vector3 direction){

        //Model otherCube = assets.get("otherCube.g3db", Model.class);
        //GameObject hitModel = zombies.get(userValue1 - 10);
        //playerGo1.body.proceedToTransform(playerGo1.transform);
        Vector3 worldPosition = new Vector3();
        //hitModel.transform.getTranslation(worldPosition);
        //hitModel.transform.setTranslation(new Vector3(0, 0, 0));
        GameObject cube;

        for(int x = 0; x < 1 ;x++) {
            //playerGo1 = new GameObject()
            cube = new GameObject(otherCube, .5f, 1f, 3);
            //cube.transform.setTranslation(pos);
            Vector3 create = new Vector3();
            playerGo.transform.getTranslation(create);
            cube.transform.setTranslation(create.add(0,2,0));
            cube.body.proceedToTransform(cube.transform);
            cube.bounds.set(new Vector3(pos.x - 1f, 2, pos.z + 1f), new Vector3(pos.x + 1f, 10f, pos.z - 1f));
            //cube.body.setUserValue(-1);										//this is used to tag us for collisions
            //cube.body.setLinearFactor(new Vector3(.5f,0,.5f));
            //cube.body.setAngularFactor(.5f);
            cube.body.setCollisionFlags(cube.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
            //playerGo.body.setWorldTransform(playerGo.transform);
            //playerGo1.body.setWorldTransform(playerGo1.transform);
            //dynamicsWorld.addRigidBody(playerGo1.body);//, OBJECT_FLAG, GROUND_FLAG
            cube.body.setContactCallbackFlag(DEBRIS_FLAG);
            cube.body.setContactCallbackFilter(0);


            dynamicsWorld.addRigidBody(cube.body, ENEMY_FLAG, ALL_FLAG);//, ENEMY_FLAG, ALL_FLAG);
            cube.body.activate();//new Vector3(0,1000,0)
            float force = 10;
            Vector3 forcer = new Vector3(direction.x * force, 0, direction.z * force);
            cube.body.applyImpulse(forcer, new Vector3(0,0,0));


            Vector3 playerPos = new Vector3();
            playerGo.transform.getTranslation(playerPos);
            Gdx.app.log("Warn", "Made a cube " + pos + " player pos " + playerPos);

            debris.add(cube);

        }
    }

    public void DeleteZombie(int index){

        int size = world.buckets[(int) zombieBucket.get(index).x][(int) zombieBucket.get(index).y].size;
        boolean found = false;
        int foundIndex = 0;

        for(int x = 0; x < size; x++){
            if (zombies.get(index) == world.buckets[(int) zombieBucket.get(index).x][(int) zombieBucket.get(index).y].get(x)){
                //found = true;
                foundIndex = x;
            }
        }

        Gdx.app.log("Warn", "Clearing zombie");
        zombies.get(index).dispose();
        zombies.set(index,null);

        //zombieBucket.set(userValue1 - 10,null);		//this isnt a reference to the gameobject so dont need to clear it
        //world.buckets[(int) zombieBucket.get(index).x][(int) zombieBucket.get(userValue1 - 10).y].removeIndex(foundIndex);

    }
    */


    @Override
    public void dispose(){
        //model.dispose();
        instances.clear();
        assets.dispose();

        dynamicsWorld.dispose();
        constraintSolver.dispose();
        //bullet is a C++ wrapper, an in that language memory is not managed, must dispose of ourselves
        dispatcher.dispose();
        collisionConfig.dispose();
        contactListener.dispose();
        broadphase.dispose();
    }

    @Override
    public void resume(){

    }

    @Override
    public void resize(int width, int height){
        stage.getViewport().update(width, height, true);

        if(viewport != null){
            viewport.update(width, height);
        }

    }

    @Override
    public void pause(){

    }

    public float GetHighestPoint(Vector3 velocity){

        if(Math.abs(velocity.z) > Math.abs(velocity.x)){
            return Math.abs(velocity.z);
        }
        else{
            return Math.abs(velocity.x);
        }

    }

    //this is our contact event for collision objects, if something touches something else, this will fire...
    //using nested class since needs so much data from this class, seems unnatural to seperate them
    class MyContactListener extends ContactListener {
        @Override
        public boolean onContactAdded (int userValue0, int partId0, int index0, boolean match0,
                                       int userValue1, int partId1, int index1, boolean match1) {

            //so when player touches a zombie, have it explode with smaller pieces that shoot all over

            if(userValue0 == 3 && (userValue1 == 7 || userValue1 == 5)) {


                if(baseTime == 0){
                    baseTime = TimeUtils.millis();
                }

                //Gdx.app.log("Warn", "Getting collision: " + (TimeUtils.millis() - baseTime)/1000 + " " + ((TimeUtils.millis() - baseTime)/1000 - collisionTimer)  );
                //so we want to measure time between collision and reset speed every so often but not constantly

                if(collisionTimer == -1){
                    //initialize this to current time since world start
                    collisionTimer = (TimeUtils.millis() - baseTime)/1000;
                }

                if( ((TimeUtils.millis() - baseTime)/1000 - collisionTimer) >=  1){
                    //if it has been more than one second since last collisions then set speed to zero
                    //Gdx.app.log("Warn", "Setting speed: " + ((TimeUtils.millis() - baseTime)/1000 - collisionTimer) + " playerVelocity z " + GetHighestPoint(playerGo.body.getLinearVelocity()) );
                    //reset our timer
                    //we do damage to the player regardless of their speed changes
                    int impactSpeed = (int)GetHighestPoint(playerGo.body.getLinearVelocity())/2;

                    if(impactSpeed > 5){
                        //playerHealth -= (int)(GetHighestPoint(playerGo.body.getLinearVelocity())/3f);
                    }

                    Gdx.app.log("Warn", "Getting collision: " + impactSpeed + " velocity " + playerGo.body.getLinearVelocity() + speed);

                    playerGo.body.setLinearVelocity(new Vector3(0, 0, 0));
                    playerGo.body.setAngularVelocity(new Vector3(0, 0, 0));

                    speed = 0;
                    collisionTimer = ( TimeUtils.millis() - baseTime)/1000;
                }

            }


            if(userValue0 == 3 && userValue1 > 9) {

                //for now we will ignore zombie collisions till we get stiched over to zombie class...
                Vector3 worldPosition = new Vector3();

                //so we are currently not using teh zombies array for anything
                //so userValue has our index inthe zombiePool array, which no longer needs to be -10 I believe
                if (zombiePool.get(userValue1 - 10) == null) {
                    Gdx.app.log("Warn", "Instnace is null");
                    return false;
                }

                if (baseTime == 0) {
                    baseTime = TimeUtils.millis();
                }

                //we need to limit how often we apply damage for collsions
                if (zombiePool.get(userValue1 - 10).GetTimer() == 0) {
                    //then we have either never had a collision, or we have changed buckets
                    timer = (float) ((TimeUtils.millis() - baseTime));//time since game start, or current time really

                    //set collision timer on zombie
                    zombiePool.get(userValue1 - 10).SetTimer(timer);
                    Gdx.app.log("Warn", "Colliding with " + (userValue1 - 10) + "for first time");

                } else {
                    //so we are measuring our time since last contact, if larger than our timer threshold then reset it to current time and allow the collision
                    //otherwise return false and get out of this collision
                    timer = (float) ((TimeUtils.millis() - baseTime));

                    if (timer - zombiePool.get(userValue1 - 10).GetTimer() > checkTime) {
                        //set our timer ot this new one
                        zombiePool.get(userValue1 - 10).SetTimer(timer);
                        Gdx.app.log("Warn", "Collidning again " + (timer - zombiePool.get(userValue1 - 10).GetTimer()) + " " + checkTime);
                    } else {
                        return false;
                    }


                }


                //so to destroy a zombie they need to be in front of us
                //so we can use current velocity to determine facing direction, then see if zombie position is pretty close

                //Vector3 velocity = playerGo.body.getLinearVelocity();
                Vector3 playerPosition = new Vector3();
                playerGo.transform.getTranslation(playerPosition);

                Vector3 zombiePosition = new Vector3();
                zombiePool.get(userValue1 - 10).zModel.transform.getTranslation(zombiePosition);

                //the easiest way might be to determine world space of postion in front of vechiel then mesure distance to it
                // Gdx.app.log("Warn", "i Distance front " + zombiePosition.dst(currentLookPoint) + " back " + zombiePosition.dst(currentBackPoint) + " speed " + GetHighestPoint(playerGo.body.getLinearVelocity()));
                //Gdx.app.log("Warn", "i Player pos " + playerPosition + " zomb pos " + zombiePosition + " current look " + currentLookPoint + " back " + currentBackPoint);
                //Gdx.app.log("Warn", "i Velocity " + playerGo.body.getLinearVelocity());
                if ((zombiePosition.dst(currentBackPoint) > 4.25 || zombiePosition.dst(currentBackPoint) < 2.5) && GetHighestPoint(playerGo.body.getLinearVelocity()) > 10f) {
                    //then the zombie is in front of the vehicle on collision or behind it, so can kill it
                } else {
                    return false;
                }
               // Gdx.app.log("Warn", "GEtting zoombie collision in front ");

                try {
                    GameObject hitModel = zombiePool.get(userValue1 - 10).zModel;

                    hitModel.transform.getTranslation(worldPosition);
                    hitModel.transform.setTranslation(worldPosition.x, 20, worldPosition.z);
                    //Gdx.app.log("Warn", "i GEtting zoombie collision in front " + worldPosition);
                    int x = zombiePool.get(userValue1 - 10).xBucket;
                    int z = zombiePool.get(userValue1 - 10).zBucket;

                    //we need to remove the zombie from the array, but have to iterate through and match teh userValue

                    for (int i = 0; i < world.zBuckets[x][z].size; i++) {
                        if (world.zBuckets[x][z].get(i).zModel.body.getUserValue() == userValue1) {
                            //then we remove this one
                            world.zBuckets[x][z].removeIndex(i);
                            break;
                        }
                    }
                    //so for damaging our player, we need to detemine the direction relative to our facing position
                }
                catch (Exception ex) {
                    Gdx.app.log("Warn", "Some kind of error " + ex.getLocalizedMessage() + " " + ex.getStackTrace() + " " + ex.getMessage());
                }

                if (Math.abs(GetHighestPoint(playerGo.body.getLinearVelocity())) > 4.25) {
                    //then destroy the xombie, if not push it and deal dammag to us
                    //playerHealth = playerHealth - 3;
                    speed += 5f;
                    Vector3 vel = new Vector3(playerGo.body.getLinearVelocity().x / 5, playerGo.body.getAngularVelocity().y / 5, playerGo.body.getAngularVelocity().z / 5);
                    playerGo.body.setLinearVelocity(vel);
                    //Gdx.app.log("Warn", "Damange player 2 " + playerHealth);
                } else {
                    //Gdx.app.log("Warn", "Damange player 5 " + playerHealth);
                    //playerHealth = playerHealth - 1;
                    speed += 3f;
                }

                try {
                    GameObject cube;
                    for (int x = 0; x < 6; x++) {
                        Gdx.app.log("Warn", "Loop " + x);
                        cube = new GameObject(otherCube, .5f, 1f, 3);
                        if(cube.body == null){
                            Gdx.app.log("Warn", "Missing body");
                        }
                        if(cube.transform == null){
                            Gdx.app.log("Warn", "Missing transform");
                        }

                        cube.transform.setTranslation(new Vector3(worldPosition.x, .1f, worldPosition.z));
                        cube.body.proceedToTransform(cube.transform);
                        cube.bounds.set(new Vector3(worldPosition.x - .1f, 2, worldPosition.z + .1f), new Vector3(worldPosition.x + .1f, 10f, worldPosition.z - .1f));
                        cube.body.setCollisionFlags(cube.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
                        cube.body.setContactCallbackFlag(DEBRIS_FLAG);
                        cube.body.setContactCallbackFilter(0);

                        dynamicsWorld.addRigidBody(cube.body, DEBRIS_FLAG, DEBRIS_FLAG);
                        cube.body.activate();

                        Vector3 playerLocation = new Vector3();
                        Vector3 camLocation = cam.position;
                        float hitSpeed = playerGo.body.getLinearVelocity().z;
                        //hitSpeed = hitSpeed / 2;
                        playerGo.transform.getTranslation(playerLocation);
                        Vector3 direction1 = new Vector3((playerLocation.x - camLocation.x), .8f, (playerLocation.z - camLocation.z));
                        //Vector3 direction = new Vector3(camLocation.x - playerLocation.x, camLocation.y - playerLocation.y, camLocation.z - playerLocation.z);

                        //Gdx.app.log("Warn", "Going to make zombie debris direction " + direction + " other " + direction1 + " hit Speed " + hitSpeed);
                        cube.body.applyImpulse(direction1, new Vector3(new Random().nextFloat(), -.09f, new Random().nextFloat()));

                        debris.add(cube);

                    }
                }
                catch(Exception ex){
                    Gdx.app.log("Warn", "Some kind of error 1 " + ex.getLocalizedMessage() + " " + ex.getStackTrace() + " " + ex.getMessage());
                }
                return true;
            }
            return true;
        }
    }




}
