package com.frosix;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
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
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.shape.Shape;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.extension.multiplayer.protocol.adt.message.IMessage;
import org.anddev.andengine.extension.multiplayer.protocol.adt.message.Message;
import org.anddev.andengine.extension.multiplayer.protocol.shared.BluetoothSocketConnection;
import org.anddev.andengine.extension.multiplayer.protocol.shared.Connector;
import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;

import android.util.Log;
import android.view.Display;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.frosix.protocol.adt.message.CommonMessage;
import com.frosix.protocol.adt.message.ICommonMessage;
import com.frosix.protocol.adt.message.MoveSpriteCommonMessage;
import com.frosix.protocol.adt.message.TouchControlMessage;

import static java.lang.Math.*;
public class PingPongGameActivity extends BaseMultiplayerGameActivity implements IOnSceneTouchListener {
	
	private static int CAMERA_HEIGHT;
	private static float CAMERA_WIDTH ;
	private Camera mCamera;
	private BitmapTextureAtlas mBitmapTextureAtlas;
	private TiledTextureRegion mCircleFaceTextureRegion;
	private Scene mScene;
	private PhysicsWorld mPhysicsWorld;
		
	Rectangle selfRect;
	private Body selfRectBody ;
	
	Rectangle enemyRect;
	private Body enemyRectBody ;
	
	private boolean moveSelfFlag =false;
	private boolean isSelfRight =false;
	
	private boolean moveEnemyFlag =false;
	private boolean isEnemyRight =false;
	
		
	private boolean isSceneLoaded = false ;
	private boolean isMessageHandled = false ;
	private boolean onStartedflag = false;
	
	Body globBbody;
	
	//Const 
		private static final FixtureDef FIXTURE_WALL = PhysicsFactory.createFixtureDef(1, 1f, 0f);
		private static final FixtureDef FIXTURE_PLATFORM = PhysicsFactory.createFixtureDef(0.5f, 0.5f, 0.5f);
		private static final FixtureDef FIXTURE_BALL = PhysicsFactory.createFixtureDef(1, 1f, 1f);
		//
		
		
	
	@SuppressWarnings("serial")
	private Map<Short, Class<? extends ICommonMessage>> messageMap = new HashMap<Short, Class<? extends ICommonMessage>>() {{
		put(FLAG_MESSAGE_TOUCH_CONTROL, TouchControlMessage.class);
		put(FLAG_MESSAGE_SYNCHRONIZING , SynchronizingMessage.class);
	}};
	
	
	
	@Override
	public Engine onLoadEngine() {
		Display display = getWindowManager().getDefaultDisplay(); 
		  CAMERA_WIDTH  = display.getWidth();
		  CAMERA_HEIGHT = display.getHeight();
		  this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		  final EngineOptions engineOptions = new EngineOptions(true,
					ScreenOrientation.PORTRAIT, new RatioResolutionPolicy(
							CAMERA_WIDTH, CAMERA_HEIGHT), mCamera);
			
		   		    
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
		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, 0), false);
	
		
		final Shape ground = new Rectangle(0, CAMERA_HEIGHT - 2, CAMERA_WIDTH, 2);
		final Shape roof = new Rectangle(0, 0, CAMERA_WIDTH, 2);
		final Shape left = new Rectangle(- CAMERA_HEIGHT/2, CAMERA_HEIGHT/2, CAMERA_HEIGHT, 2);
		left.setRotation(90);
		final Shape right = new Rectangle(CAMERA_WIDTH - 2, 0, 2, CAMERA_HEIGHT);
		
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, ground, BodyType.StaticBody, FIXTURE_WALL);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, roof, BodyType.StaticBody, FIXTURE_WALL);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, left, BodyType.StaticBody, FIXTURE_WALL).setUserData("left");
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, right, BodyType.StaticBody, FIXTURE_WALL).setUserData("right");

		this.mScene.attachChild(ground);
		this.mScene.attachChild(roof);
		this.mScene.attachChild(left);
		this.mScene.attachChild(right);

		this.mScene.registerUpdateHandler(this.mPhysicsWorld);
	
		mScene.setTouchAreaBindingEnabled(true);
		
		mScene.setOnSceneTouchListener(this);	
		return mScene;
	}

	
	@Override
	public void onLoadComplete() {	
			
		addControl();
		mScene.registerUpdateHandler(new IUpdateHandler() {
			
			@Override
			public void reset() {						
			}
			
			@Override
			public void onUpdate(float pSecondsElapsed) {
				moveSelfPlatform(moveSelfFlag ,isSelfRight );	
				//moveEnemyPlatform(moveEnemyFlag , isEnemyRight);
			}
		});
		
		mScene.registerUpdateHandler(new TimerHandler(0.1f,true, new ITimerCallback() {	
			
			@Override
			public void onTimePassed(TimerHandler pTimerHandler) {
				SynchronizingMessage syncMessageToSend = (SynchronizingMessage)getMessage(FLAG_MESSAGE_SYNCHRONIZING);
				syncMessageToSend.set(selfRectBody.getPosition() ,selfRectBody.getLinearVelocity() );
				sendMessage(syncMessageToSend);
				
			}
		}));
	}
	
	public void makeEffect(Body pBody , Vector2 pVector ){
		pBody.setLinearVelocity(pVector);
		writeVectorToLog(pVector ," make effect");
	    };
	
    public void writeVectorToLog( Vector2 pVector , String pStr){
    	Log.i("flag", pStr + "  pVector=" + pVector.x  +" y:" +pVector.y );
    }
    
    public void addControl(){
		float rectWidth = 150;
		float rectHeight = 20;
		
		selfRect = new Rectangle(0, 0 , rectWidth, rectHeight);
		selfRectBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, selfRect, BodyType.KinematicBody, FIXTURE_PLATFORM);
						
		this.mScene.attachChild(selfRect);
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(selfRect, selfRectBody, true, true ));
		selfRectBody.setTransform(rectWidth/2 / 32, (CAMERA_HEIGHT - rectHeight /2 ) /32, 0);
		
		enemyRect = new Rectangle(0 , 0 , rectWidth, rectHeight);
		enemyRectBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, enemyRect, BodyType.KinematicBody, FIXTURE_PLATFORM);
		this.mScene.attachChild(enemyRect);
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(enemyRect, enemyRectBody, true, true));
		enemyRectBody.setTransform(rectWidth /2 /32 , rectHeight /2 /32 , 0);
			
	}
	
	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
				
		if(!pSceneTouchEvent.isActionUp()){
			moveSelfFlag = true;
			if(pSceneTouchEvent.getX() > CAMERA_WIDTH/2){
				isSelfRight = true;
			}
			else{
			isSelfRight = false;
			}
		}
		else{
			moveSelfFlag = false;
		}
		
//		TouchControlMessage messageToSend = (TouchControlMessage)getMessage(FLAG_MESSAGE_TOUCH_CONTROL);
//		messageToSend.set(pSceneTouchEvent.getX() , pSceneTouchEvent.getY() , pSceneTouchEvent.getAction());
//		sendMessage(messageToSend);
		
		return true;
	}
	

	public void moveSelfPlatform( boolean pMoveFlag , boolean pIsRight){
		
		
		if(pMoveFlag){
			if(pIsRight && (selfRect.getX() + selfRect.getWidth()) < CAMERA_WIDTH) {
				selfRectBody.setLinearVelocity(10, 0);
			}
			else if(!pIsRight && (selfRect.getX() > 0))	{
				selfRectBody.setLinearVelocity(-10 , 0);
			} 
			else{
				selfRectBody.setLinearVelocity(0 , 0);	
			}
		}
		else{
		selfRectBody.setLinearVelocity(0 , 0);	
		}
	}

	public void moveEnemyPlatform( boolean pMoveFlag , boolean pIsRight){
		if(pMoveFlag){
			if(pIsRight && (enemyRect.getX() + enemyRect.getWidth()) < CAMERA_WIDTH) {
				enemyRectBody.setLinearVelocity(10, 0);
				Log.i("flag", "vel right " );
			}
			else if(!pIsRight && (enemyRect.getX() > 0))	{
				enemyRectBody.setLinearVelocity(-10 , 0);
			} 
			else{
				enemyRectBody.setLinearVelocity(0 , 0);	
			}
		}
		else{
			enemyRectBody.setLinearVelocity(0 , 0);	
		}
	}
	
	@Override
	public void onHandleMessage(
			Connector<BluetoothSocketConnection> pConnector,
			ICommonMessage pMessage) throws IOException {
		if (pMessage instanceof TouchControlMessage) {
			Log.i("flag", "message handled TouchControlMessage ");
			TouchControlMessage mMessage = (TouchControlMessage)pMessage;
			float mX = ((TouchControlMessage) pMessage).x;
			float mY =((TouchControlMessage) pMessage).y;
			
		
			if(((TouchControlMessage) pMessage).action != TouchEvent.ACTION_UP){
				moveEnemyFlag = true;
				if(mX > CAMERA_WIDTH/2){
					isEnemyRight = false;
				}
				else{
				isEnemyRight = true;
				}
			}
			else{
				moveEnemyFlag = false;
			}
			
			return;
		}
		
		if(pMessage instanceof SynchronizingMessage){
			Log.i("flag", "message handled SynchronizingMessage ");
			synchronizeGame((SynchronizingMessage)pMessage);
			
		}
		super.onHandleMessage(pConnector, pMessage);
	}

		
	private void synchronizeGame(SynchronizingMessage pMessage) {
		if ( enemyRectBody!= null){
			enemyRectBody.setTransform(new Vector2(CAMERA_WIDTH/32 - pMessage.platformPos.x , 0) , 0);
			enemyRectBody.setLinearVelocity(pMessage.platformVelocity.mul(-1));
		}
		
	}

	@Override
	protected Map<Short, Class<? extends ICommonMessage>> getMessageMap() {
		return messageMap;
	}
	
	
	public static class MovePlatformCommonMessage extends Message implements ICommonMessage {
		public int mID;
		public boolean moveFlag;
		public boolean isRight;
		
		public MovePlatformCommonMessage() {	
		}
		
		public MovePlatformCommonMessage(int pID ,boolean isRight , boolean moveFlag){
			this.mID = pID ;
			this.isRight = isRight;
			this.moveFlag = moveFlag;
		}
		
		
		public void set(int pID ,boolean isRight , boolean moveFlag) {
			this.mID = pID;
			this.isRight = isRight;
			this.moveFlag = moveFlag;
		}
		
		@Override
		public short getFlag() {
			return ConstantStorage.FLAG_MESSAGE_COMMON_MOVE_PLATFORM;
		}

		
		@Override
		protected void onReadTransmissionData(DataInputStream pDataInputStream)
				throws IOException {
			this.mID = pDataInputStream.readInt();
			this.isRight = pDataInputStream.readBoolean();
			this.moveFlag = pDataInputStream.readBoolean();
			
		}

		@Override
		protected void onWriteTransmissionData(
				DataOutputStream pDataOutputStream) throws IOException {
			pDataOutputStream.writeInt(this.mID);
			pDataOutputStream.writeBoolean(this.isRight);
			pDataOutputStream.writeBoolean(this.moveFlag);
			
		}
		
	}
	
	public static class SynchronizingMessage extends CommonMessage {

	
	//	public SyncContainer[] container =;
	
		public Vector2 platformPos = new Vector2();
		public Vector2 platformVelocity = new Vector2(); ;
		
		public SynchronizingMessage () {}
		
		public void set(Vector2 platformPos ,Vector2 platformVelocity) {
			this.platformPos = platformPos;
			this.platformVelocity = platformVelocity;
		}
		
		@Override
		public short getFlag() {
			return ConstantStorage.FLAG_MESSAGE_SYNCHRONIZING;
		}

		@Override
		protected void onReadTransmissionData(DataInputStream pDataInputStream)
				throws IOException {
	
			this.platformPos.x = pDataInputStream.readFloat();
			this.platformPos.y = pDataInputStream.readFloat();
			this.platformVelocity.x = pDataInputStream.readFloat();
			this.platformVelocity.y = pDataInputStream.readFloat();
		}

		@Override
		protected void onWriteTransmissionData(
				DataOutputStream pDataOutputStream) throws IOException {
		
			pDataOutputStream.writeFloat(this.platformPos.x);
			pDataOutputStream.writeFloat(this.platformPos.y);
			pDataOutputStream.writeFloat(this.platformVelocity.x);
			pDataOutputStream.writeFloat(this.platformVelocity.y);
		}
	}
	
	private static class SyncContainer{
		public int iD;
		public Vector2 positionI;
		public Vector2 VelocityI;
		
	}
	
	
	
}
