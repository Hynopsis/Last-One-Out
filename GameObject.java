package com.rpetersen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.utils.Disposable;

/**
 * Created by lastr on 9/14/2016.
 */
public class GameObject extends ModelInstance implements Disposable {

    //this object has quite a few overrode constructors to handle different things
    public btRigidBody body;						//what we are using for rigidbody physics
    public btCollisionShape shape;				    //this is the size of our instance collisions shape
    public static btRigidBody.btRigidBodyConstructionInfo constructionInfo;	//this is used for collision with rigidbodies
    public static Vector3 localInertia = new Vector3();//this is used and shared, for when we construct our physics data
    public MotionState motionState;
    public AnimationController animator;

    //new instances of these types are needed to create rigidbody objects to detect physics collisions
    //they are expensive to create so we are saving the most common ones to reduce GC work
    private static btRigidBody.btRigidBodyConstructionInfo houseInfo;
    private static btRigidBody.btRigidBodyConstructionInfo zombieInfo;
    private static btRigidBody.btRigidBodyConstructionInfo treeInfo;
    private static btRigidBody.btRigidBodyConstructionInfo rockInfo;
    private static btRigidBody.btRigidBodyConstructionInfo debrisInfo;

    //every visible model with physics has a collision shape that defines its collision size
    private static btCollisionShape treeShape;
    private static btCollisionShape zombieShape;
    private static btCollisionShape houseShape;
    private static btCollisionShape debrisShape;
    private static btCollisionShape rockShape;

    public static boolean initial = true;

    public final BoundingBox bounds = new BoundingBox();//this used to be static and shared between all instances

    public void InitializeStatics(){
        //to prevent passing of variables and stressing the garbage collector
        treeShape = new btBoxShape(new Vector3(.25f, 3f, .25f));
        debrisShape = new btBoxShape(new Vector3(.1f,.1f, .1f));
        zombieShape = new btBoxShape(new Vector3(.3f, .2f, .3f));
        houseShape = new btBoxShape(new Vector3(3, 2, 3));
        rockShape = new btBoxShape(new Vector3(1.5f, 1.5f, 1.5f));

        treeInfo = new btRigidBody.btRigidBodyConstructionInfo(0, null, treeShape, Vector3.Zero);
        rockInfo = new btRigidBody.btRigidBodyConstructionInfo(0, null, rockShape, Vector3.Zero);
        debrisShape.calculateLocalInertia(.5f, localInertia);
        debrisInfo = new btRigidBody.btRigidBodyConstructionInfo(.5f, null, debrisShape, localInertia);
        houseInfo = new btRigidBody.btRigidBodyConstructionInfo(0, null, new btBoxShape(new Vector3(3, 2, 3)), Vector3.Zero);
        zombieShape.calculateLocalInertia(1.5f, localInertia);
        zombieInfo = new btRigidBody.btRigidBodyConstructionInfo(1.5f, null, zombieShape, localInertia);
        initial = false;
    }
    //this method is for loading objects from a imported scene
    public GameObject(Model model, String rootNode, boolean mergeTransform, btCollisionShape shape, float mass) {//,

        super(model, rootNode, mergeTransform);

        motionState = new MotionState();
        motionState.transform = transform;

        if (mass > 0f)
            shape.calculateLocalInertia(mass, localInertia);
        else
            localInertia.set(0, 0, 0);

        constructionInfo = new btRigidBody.btRigidBodyConstructionInfo(mass, null, shape, localInertia);
        this.shape = shape;

        body = new btRigidBody(constructionInfo);
        body.setMotionState(motionState);
        body.setCollisionShape(shape);
    }

    public GameObject(Model model, float mass, float friction, int type) {
        //instead of having seperate constructors, Im using a case statement to setup their collision shaping and
        //other physics data.  Since we are making so many models, we have static refrences for each type that are initialized
        //the first time they are created.  This prevents a massive amount of gc, and loads levels much faster

        //type is 0 = tree, 1 = rock,3 = debris, 4 = player, 5 = ground, 6 = house,
        super(model);

        motionState = new MotionState();
        motionState.transform = transform;

        if(initial){
            //setup static variables
            InitializeStatics();
        }

        switch (type) {
            case 0:  //tree
                shape = treeShape;
                constructionInfo = treeInfo;
                break;
            case 1: //rock
                shape = rockShape;
                constructionInfo = rockInfo;
                break;
            case 3: //debris
                shape = debrisShape;
                constructionInfo = debrisInfo;
                break;
            case 4://we only make one player so not making static player constructorinfo
                shape = new btBoxShape(new Vector3(.3f,.1f, 1f));
                shape.calculateLocalInertia(mass, localInertia);
                constructionInfo = new btRigidBody.btRigidBodyConstructionInfo(mass, null, this.shape, localInertia);
                break;
            case 5://no saved constructorinfo, only one created
                shape = new btBoxShape(new Vector3(404, .25f, 404));
                localInertia.set(0, 0, 0);
                constructionInfo = new btRigidBody.btRigidBodyConstructionInfo(mass, null, this.shape, localInertia);
                break;
            case 6:
                shape = houseShape;
                constructionInfo = houseInfo;//new btRigidBody.btRigidBodyConstructionInfo(mass, null, shape, localInertia);
                break;
            default:
                Gdx.app.log("Warn", "Shouldnt be possible ");
                break;
        }

        if (mass == 0) {
            constructionInfo.setFriction(friction);
        }
        body = new btRigidBody(constructionInfo);
        body.setMotionState(motionState);
        body.setCollisionShape(this.shape);
    }

    public GameObject(Model model, float mass, float friction, boolean animate) {
        //this model constructor iis used for animated models, which for now is only zombies
        super(model);

        if(animate) {
            animator = new AnimationController(this);
            animator.setAnimation("walk", -1);                  //animation only gets called if you we simulating on render through: controller.update(Gdx.graphics.getDeltaTime());
        }

        motionState = new MotionState();
        motionState.transform = transform;

        if(initial){
            //setup static variables
            InitializeStatics();
        }

        shape = zombieShape;
        constructionInfo = zombieInfo;

        body = new btRigidBody(constructionInfo);
        body.setMotionState(motionState);
        body.setCollisionShape(shape);
    }

    public GameObject(Model model) {

        super(model);
    }
    //seperate constructor for objects that dont need collisions
    public GameObject(Model model, String rootNode, boolean mergeTransform) {//,

        super(model, rootNode, mergeTransform);
    }

    public void dispose(){
        if(body != null) {
            body.dispose();
        }
        if(shape != null) {
            shape.dispose();
        }
        if(motionState != null) {
            motionState.dispose();
        }
        if(model != null) {
            //this is a problem, models need to be disposed, but always errors with Fatal signal 11 exception
            //model.dispose();
        }
    }

    public void Destroy(btDynamicsWorld dynamicsWorld){
        //remove our rigidboyd
        if(body != null) {
            //Gdx.app.log("Warn", "Number " + this.body.getUserValue());
            dynamicsWorld.removeRigidBody(this.body);
        }
        body = null;
        shape = null;
        motionState = null;
        dispose();
    }

    //TON OF SAMPLE CODE FOR LOADING ALL KINDS OF MODELS, no real good place to put it

        /*  //sample code for getting all models from a loaded scene
		for (int p = 0; p < model.nodes.size; p++) {//model.nodes.size
			for (int i = 0; i < model.nodes.size; i++) {//model.nodes.size

				String id = model.nodes.get(i).id;
				ModelInstance instance = new ModelInstance(model, id);
				Node node = instance.getNode(id);

				instance.transform.set(node.globalTransform);
				node.translation.set(i * 8, p * 8, 0);//i * 10
				node.scale.set(1, 1, 1);
				node.rotation.idt();
				instance.calculateTransforms();

				instances.add(instance);

				Gdx.app.log("Warn", "Position " + instance.transform.getTranslation(new Vector3(0, 0, 0)) + " id " + id);
			}
		}

		*/


		/*
		Model land = assets.get("grass.g3db", Model.class);
		player = new ModelInstance(land);
		player.transform.setToTranslation(0, 0, 0);
		//Gdx.app.log("Warn", "Our car position at initial " + player.transform.getTranslation(new Vector3(0, 0, 0)));
		instances.add(player);

		*/


    //Model car = assets.get("car.g3db", Model.class);
    //playerGo = new GameObject(car, "car", true);
    ///player.transform.setToTranslation(0, 0, 0);
    //Gdx.app.log("Warn", "Our car position at initial " + player.transform.getTranslation(new Vector3(0, 0, 0)));
    //instances.add(player);


    //Model car1 = assets.get("car.g3db", Model.class);
    //player = new GameObject(car, "car", true);
    //player1.transform.setToTranslation(0, 0, 2);
    //Gdx.app.log("Warn", "Our car position at initial " + player.transform.getTranslation(new Vector3(0,0,0)));
    //instances.add(player1);
    //Vector3 spot = new Vector3();
    //player1.transform.getTranslation(spot);
/* //example code for creating a models with physics
		Gdx.app.log("Warn", "Going to make zombie");
		playerGo1 = new GameObject(otherCube, debrisShape, 1f, 1f);
		playerGo1.transform.setTranslation(new Vector3(new Vector3((world.mapSize / 2) * 8 + 8, 10, (world.mapSize / 2f) * -8 - 8)));
		playerGo1.body.proceedToTransform(playerGo1.transform);
		playerGo1.bounds.set(new Vector3(0, 0, 0), new Vector3(1, 2, 1));
		//playerGo1.body.setUserValue(1);										//this is used to tag us for collisions
		//playerGo1.body.setLinearFactor(new Vector3(.5f,0,.5f));
		playerGo1.body.setAngularFactor(.5f);
		playerGo1.body.setCollisionFlags(playerGo1.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
		//playerGo.body.setWorldTransform(playerGo.transform);
		//playerGo1.body.setWorldTransform(playerGo1.transform);
		//dynamicsWorld.addRigidBody(playerGo1.body);//, OBJECT_FLAG, GROUND_FLAG
		playerGo1.body.setContactCallbackFlag(ENEMY_FLAG);
		playerGo1.body.setContactCallbackFilter(0);

		dynamicsWorld.addRigidBody(playerGo1.body);

		Gdx.app.log("Warn", "Going to make tree");
		//playerGo1 = new GameObject()
		playerGo2 = new GameObject(tree, treeShape, 0);
		playerGo2.transform.setTranslation(new Vector3(new Vector3((world.mapSize / 2) * 8 + 4, 0, (world.mapSize / 2f) * -8 - 4)));
		playerGo2.body.proceedToTransform(playerGo2.transform);
		playerGo2.bounds.set(new Vector3(0, 0, 0), new Vector3(1, 2, 1));
		playerGo2.body.setUserValue(1);										//this is used to tag us for collisions
		playerGo2.body.setCollisionFlags(playerGo2.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
		//playerGo.body.setWorldTransform(playerGo.transform);
		//playerGo1.body.setWorldTransform(playerGo1.transform);
		dynamicsWorld.addRigidBody(playerGo2.body);//, OBJECT_FLAG, GROUND_FLAG
		playerGo2.body.setContactCallbackFlag(ENEMY_FLAG);
		playerGo2.body.setContactCallbackFilter(0);

		instances.add(playerGo2);

		Gdx.app.log("Warn", "Going to make house");
		//playerGo1 = new GameObject()
		instance = new GameObject(downHouse, houseShape, 0);
		instance.transform.setTranslation(new Vector3((world.mapSize / 2) * 8 + 7,0, (world.mapSize / 2f) * -8 - 7));
		instance.body.proceedToTransform(instance.transform);
		instance.bounds.set(new Vector3((world.mapSize / 2) * 8 + 7, 0, (world.mapSize / 2f) * -8 - 7), new Vector3((world.mapSize / 2) * 8 + 15, 0, (world.mapSize / 2f) * -8 - 15));
		instance.body.setUserValue(2);										//this is used to tag us for collisions
		instance.body.setCollisionFlags(instance.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
		//playerGo.body.setWorldTransform(playerGo.transform);
		//playerGo1.body.setWorldTransform(playerGo1.transform);
		dynamicsWorld.addRigidBody(instance.body);//, OBJECT_FLAG, GROUND_FLAG
		instance.body.setContactCallbackFlag(GROUND_FLAG);
		instance.body.setContactCallbackFilter(0);

		house = instance;
		instances.add(instance);

		//example of getting a model from a loaded scene
		Gdx.app.log("Warn", "Going to make car");
		instance = new GameObject(mainModel, "car", true);

		node = instance.getNode("car");

		instance.transform.set(node.globalTransform);
		node.translation.set(0, 0, 0);//(world.mapSize * 8) / 2, 0, (world.mapSize * 8) / 2
		node.scale.set(1, 1, 1);
		node.rotation.idt();
		instance.calculateTransforms();

		//instances.add(instance);

		player = instance;

		/*
		Gdx.app.log("Warn", "Going to make car");
		instance = new GameObject(mainModel, "car", true);

		node = instance.getNode("car");

		instance.transform.set(node.globalTransform);
		node.translation.set(0, 0, 20);//(world.mapSize * 8) / 2, 0, (world.mapSize * 8) / 2
		instance.bounds.set(new Vector3(-3,0, 3), new Vector3(3,0, -3));
		node.scale.set(1, 1, 1);
		node.rotation.idt();
		instance.calculateTransforms();
		*/

		/*
		Gdx.app.log("Warn", "Going to make tree");
		instance = new GameObject(mainModel, "tree", false);
		Gdx.app.log("Warn", "loading model");

		node = instance.getNode("tree");

		instance.transform.set(node.globalTransform);
		node.translation.set(0, 0, 0);//(world.mapSize * 8) / 2, 0, (world.mapSize * 8) / 2
		node.scale.set(1, 1, 1);
		node.rotation.idt();
		instance.calculateTransforms();

		tree = instance;
        */

        /*
		Gdx.app.log("Warn", "Going to make slydome");

		instance = new GameObject(mainModel, "skydome1", true);

		node = instance.getNode("skydome1");

		instance.transform.set(node.globalTransform);
		node.translation.set(0,0,0);//(world.mapSize * 8) / 2, 0, (world.mapSize * 8) / 2
		node.scale.set(20, 20, 20);
		node.rotation.idt();
		instance.calculateTransforms();

		instances.add(instance);
		skydome = instance;


        //example of loading a simple model with no collisions
        //Model ship = assets.get("terrain.g3db", Model.class);
        //ModelInstance shipInstance = new ModelInstance(ship);
        //shipInstance.transform.setToTranslation(32, 0, 32);
        //instances.add(shipInstance);
		*/
}
