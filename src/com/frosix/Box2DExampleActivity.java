package com.frosix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnAreaTouchListener;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.Scene.ITouchArea;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.shape.Shape;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.ui.activity.BaseGameActivity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import android.hardware.SensorManager;
import android.util.Log;
import android.view.Display;

public class Box2DExampleActivity extends BaseGameActivity implements IOnSceneTouchListener  {

	private static int CAMERA_HEIGHT;
	private static float CAMERA_WIDTH ;
	private Camera mCamera;
	private BitmapTextureAtlas mBitmapTextureAtlas;
	private TiledTextureRegion mCircleFaceTextureRegion;
	private Scene mScene;
	private PhysicsWorld mPhysicsWorld;
	private Map<Integer ,Body> bodyMap = new HashMap<Integer,Body>(); 

	

	private Body rectBody ;
	private Body globBody ;
	
	Rectangle rect;
	private boolean moveFlag =false;
	private boolean isRight =false;
	//Const 
	private static final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0.7f, 0.5f);
	//
	
	@Override
	public Engine onLoadEngine() {
		Display display = getWindowManager().getDefaultDisplay(); 
		  CAMERA_WIDTH  = display.getWidth();
		  CAMERA_HEIGHT = display.getHeight();
		  this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		  final EngineOptions engineOptions = new EngineOptions(true,
					ScreenOrientation.PORTRAIT, new RatioResolutionPolicy(
							CAMERA_WIDTH, CAMERA_HEIGHT), mCamera);
			
		    engineOptions.getTouchOptions().setRunOnUpdateThread(true);
		    
			final Engine mEngine = new Engine(engineOptions);
		
			return mEngine;
	}

	@Override
	public void onLoadResources() {
		this.mBitmapTextureAtlas = new BitmapTextureAtlas(64, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		
		this.mCircleFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "face_circle_tiled.png", 0, 32, 2, 1);
		this.mEngine.getTextureManager().loadTexture(this.mBitmapTextureAtlas);
	}

	@Override
	public Scene onLoadScene() {
		this.mScene = new Scene();
		
		//the gravity vector is passed to constructor
		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, 0), false);
		
		
		final Shape ground = new Rectangle(0, CAMERA_HEIGHT - 2, CAMERA_WIDTH, 2);
		final Shape roof = new Rectangle(0, 0, CAMERA_WIDTH, 2);
	//	final Shape left = new Rectangle(0, 0, 2, CAMERA_HEIGHT);
		final Shape left = new Rectangle(- CAMERA_HEIGHT/2, CAMERA_HEIGHT/2, CAMERA_HEIGHT, 2);
		left.setRotation(90);
		final Shape right = new Rectangle(CAMERA_WIDTH - 2, 0, 2, CAMERA_HEIGHT);
		
		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.3f, 0.5f);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, left, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, right, BodyType.StaticBody, wallFixtureDef);

		this.mScene.attachChild(ground);
		this.mScene.attachChild(roof);
		this.mScene.attachChild(left);
		this.mScene.attachChild(right);

		this.mScene.registerUpdateHandler(this.mPhysicsWorld);
		
		
		mScene.setTouchAreaBindingEnabled(true);
		
		
		mScene.registerUpdateHandler(new TimerHandler (2, false ,  new ITimerCallback() {
			
			@Override
			public void onTimePassed(TimerHandler pTimerHandler) {
				
				makeEffect(globBody, new Vector2(0, 20));
			}
		}));
		
		mScene.setOnSceneTouchListener(this);	
				
		return mScene;
	}

	
	@Override
	public void onLoadComplete() {	
		globBody = addFace(1);
		
		addControl();
		mScene.registerUpdateHandler(new IUpdateHandler() {
			
			@Override
			public void reset() {						
			}
			
			@Override
			public void onUpdate(float pSecondsElapsed) {
				movePlatform(moveFlag ,isRight )		;		
			}
		});
		
		mPhysicsWorld.setContactListener(new ContactListener() {
			
			@Override
			public void preSolve(Contact contact, Manifold oldManifold) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void postSolve(Contact contact, ContactImpulse impulse) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void endContact(Contact contact) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beginContact(Contact contact) {
				Body ball ;
				Body otherBody ;
				
				ball = contact.getFixtureA().getBody().getType().equals(BodyType.DynamicBody) ? contact.getFixtureA().getBody() : contact.getFixtureB().getBody();
				otherBody =  ball==contact.getFixtureA().getBody() ? contact.getFixtureB().getBody() : contact.getFixtureA().getBody();
			}
		});
		
	}
	
	public void makeEffect(Body pBody , Vector2 pVector ){
		pBody.setLinearVelocity(pVector);
		writeVectorToLog(pVector ," make effect");
	    };
	
    public void writeVectorToLog( Vector2 pVector , String pStr){
    	Log.i("flag", pStr + "  pVector=" + pVector.x  +" y:" +pVector.y );
    }
    
	private Body addFace( int count){
		
		final AnimatedSprite face = new AnimatedSprite(0, 0, this.mCircleFaceTextureRegion);
		face.setScale(1.5f);
		final Body body = PhysicsFactory.createCircleBody(this.mPhysicsWorld, face, BodyType.DynamicBody, PhysicsFactory.createFixtureDef(1, 1f, 0.5f));
		
		//Physicworld coordinates smaller then screen coord.
		body.setTransform(240 /32 , 400/ 32, 0);
		face.setUserData(count);
		
		if(!bodyMap.containsValue(count)){
			bodyMap.put(count, body);
		}
		
		this.mScene.attachChild(face);
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(face, body, true, true));
		return body;
		
		
	}

	public void addControl(){
		float rectWidth = 150;
		float rectHeight = 20;
		
		rect = new Rectangle(0, CAMERA_HEIGHT - rectHeight , rectWidth, rectHeight);
		rectBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, rect, BodyType.KinematicBody, PhysicsFactory.createFixtureDef(1, 1.2f, 0.5f));
		rectBody.setTransform(0, (CAMERA_HEIGHT - rectHeight) /32, 0);
		rect.setColor(255, 255, 255);
		
		this.mScene.attachChild(rect);
		mScene.registerTouchArea(rect);
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(rect, rectBody, true, true));
	}
	
	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		if(!pSceneTouchEvent.isActionUp()){
			moveFlag = true;
			if(pSceneTouchEvent.getX() > CAMERA_WIDTH/2){
				isRight = true;
				if((rect.getX() + rect.getWidth()) > CAMERA_WIDTH){
					moveFlag = false;
				}
			}
			else if (rect.getX() > 0){
				isRight = false;
				}
				else{
					moveFlag = false;
				}
		}
		else{
			moveFlag = false;
		}
		return true;
	}
	

	public void movePlatform( boolean pMoveFlag , boolean pIsRight){
		if(pMoveFlag){
			if(pIsRight){
				rectBody.setLinearVelocity(10, 0);
			}
			else{
				rectBody.setLinearVelocity(-10 , 0);
			}
		}
		else{
			rectBody.setLinearVelocity(0, 0);
		}
	}
	
	

	

}
