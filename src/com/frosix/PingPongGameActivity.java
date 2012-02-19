package com.frosix;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
import org.anddev.andengine.extension.multiplayer.protocol.adt.message.EmptyMessage;
import org.anddev.andengine.extension.multiplayer.protocol.adt.message.Message;
import org.anddev.andengine.extension.multiplayer.protocol.client.connector.ServerConnector;
import org.anddev.andengine.extension.multiplayer.protocol.server.connector.ClientConnector;
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

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.frosix.protocol.adt.message.CommonMessage;
import com.frosix.protocol.adt.message.ICommonMessage;
import com.frosix.protocol.adt.message.TouchControlMessage;

public class PingPongGameActivity extends BaseMultiplayerGameActivity implements IOnSceneTouchListener {
	
	private static final float CAMERA_HEIGHT = 800;
	private static final float CAMERA_WIDTH = 480;
	
	private static final float WORLD_HEIGHT = 25;
	private static final float WORLD_WIDTH = 15;
	
	private Camera mCamera;
	private BitmapTextureAtlas mBitmapTextureAtlas;
	private TiledTextureRegion mCircleFaceTextureRegion;
	private Scene mScene;
	private PhysicsWorld mPhysicsWorld;
		
	Rectangle selfRect;
	private Body selfRectBody ;
	
	Rectangle enemyRect;
	private volatile Body enemyRectBody;
	
	private boolean moveSelfFlag =false;
	private boolean isSelfRight =false;
	
	/*private boolean moveEnemyFlag =false;
	private boolean isEnemyRight =false;
	
		
	private boolean isSceneLoaded = false ;
	private boolean isMessageHandled = false ;
	private boolean onStartedflag = false;*/
	
	Body globBbody;
	
	private Body[] selfBodies = new Body[selfBodyCount];
	private Body[] commonBodies = new Body[commonBodyCount];
	private Body[] enemyBodies = new Body[selfBodyCount];
	
	private static final FixtureDef FIXTURE_WALL = PhysicsFactory.createFixtureDef(1, 1f, 0f);
	private static final FixtureDef FIXTURE_PLATFORM = PhysicsFactory.createFixtureDef(0.5f, 0.5f, 0.5f);
	private static final FixtureDef FIXTURE_BALL = PhysicsFactory.createFixtureDef(1, 1f, 1f);
	private static final byte selfBodyCount = 1;
	private static final byte commonBodyCount = 1;
	private ExecutorService pool;
	
	@SuppressWarnings("serial")
	private Map<Short, Class<? extends ICommonMessage>> messageMap = new HashMap<Short, Class<? extends ICommonMessage>>() {{
		put(FLAG_MESSAGE_SYNCHRONIZING , SynchronizingMessage.class);
	}};
	
	@Override
	public Engine onLoadEngine() {
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
	
	private boolean isClient() {
		return bluetoothDelegate instanceof ClientBluetoothDelegate;
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
		pool = Executors.newSingleThreadExecutor();
		mScene.registerUpdateHandler(new TimerHandler(0.04f, true, new ITimerCallback() {	
			
			@Override
			public void onTimePassed(TimerHandler pTimerHandler) {
				final SynchronizingMessage syncMessageToSend = (SynchronizingMessage)getMessage(FLAG_MESSAGE_SYNCHRONIZING);
				syncMessageToSend.set(selfBodies);
				syncMessageToSend.bodies = 0;
				pool.execute(new Runnable() {
					@Override
					public void run() {
						sendMessage(syncMessageToSend);
					}
				});
			}
			
		}));
		commonBodies[0] = addFace(1);
		if (!isClient()) {
			mScene.registerUpdateHandler(new TimerHandler(0.1f, true, new ITimerCallback() {	
				
				@Override
				public void onTimePassed(TimerHandler pTimerHandler) {
					final SynchronizingMessage syncMessageToSend = (SynchronizingMessage)getMessage(FLAG_MESSAGE_SYNCHRONIZING);
					syncMessageToSend.set(commonBodies);
					syncMessageToSend.bodies = 1;
					pool.execute(new Runnable() {
						@Override
						public void run() {
							sendMessage(syncMessageToSend);
						}
					});
				}
			}));
		}
	}
	
	public void makeEffect(Body pBody , Vector2 pVector ){
		pBody.setLinearVelocity(pVector);
		writeVectorToLog(pVector ," make effect");
	};
	
    public void writeVectorToLog( Vector2 pVector , String pStr){
    	Log.i("flag", pStr + "  pVector=" + pVector.x  +" y:" +pVector.y );
    }
    
    public void addControl(){
		float rectWidth = 120;
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
		enemyRectBody.setUserData(2);
		
		selfBodies[0] = selfRectBody;
		enemyRectBody.setTransform(rectWidth /2 /32 , rectHeight /2 /32 , 0);
		enemyBodies[0] = enemyRectBody;	
	}
    
    private Body addFace( int count){
		
		final AnimatedSprite face = new AnimatedSprite(0, 0, this.mCircleFaceTextureRegion);
		face.setScale(1.5f);
		final Body body = PhysicsFactory.createCircleBody(this.mPhysicsWorld, face, BodyType.DynamicBody, FIXTURE_BALL);
		

		body.setTransform(240 /32 , 400/ 32, 0);
		face.setUserData(count);
				
		this.mScene.attachChild(face);
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(face, body, true, true));
		return body;
		
		
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
		/*if (pMessage instanceof TouchControlMessage) {
			//Log.i("flag", "message handled TouchControlMessage ");
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
		}*/
		if(pMessage instanceof SynchronizingMessage){
			synchronizeGame((SynchronizingMessage)pMessage, pConnector);
		}
		
		super.onHandleMessage(pConnector, pMessage);
	}

		
	private void synchronizeGame(final SynchronizingMessage pMessage, final Connector<BluetoothSocketConnection> connector) {
		
		this.runOnUpdateThread(new Runnable() {
			
			@Override
			public void run() {
				Body[] source = pMessage.bodies == 0 ? enemyBodies : commonBodies;
				for (int i = 0; i < pMessage.syncContainers.length; i++) {
					Body b = source[i];
					SyncContainer container = pMessage.syncContainers[i];
					Vector2 position = container.positionI;
					
					b.setTransform(WORLD_WIDTH - position.x, WORLD_HEIGHT - position.y, 0);
					b.setLinearVelocity(container.velocityI.mul(-1));
				}
				if (connector instanceof ClientConnector) {
					((ClientConnector<BluetoothSocketConnection>)connector).getClientMessageReader().recycleMessage(pMessage);
				}
				if (connector instanceof ServerConnector) {
					((ServerConnector<BluetoothSocketConnection>)connector).getServerMessageReader().recycleMessage(pMessage);
				}
			}
			
		});
	}

	@Override
	protected Map<Short, Class<? extends ICommonMessage>> getMessageMap() {
		return messageMap;
	}
	
	@Override
	public void onStarted(Connector<?> pConnector) {
		super.onStarted(pConnector);
		
	}
	
	private void startGameIfPossible() {
		makeEffect(commonBodies[0], new Vector2(0, 15));
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

	    public SyncContainer[] syncContainers = new SyncContainer[selfBodyCount];
	    //0-self, 1-common
	    public byte bodies;
		
		public SynchronizingMessage () {
			for (byte i = 0; i < selfBodyCount; i ++) {
				syncContainers[i] = new SyncContainer();
			}
		}
		
		public void set(Body[] bodies) {
			for (byte i = 0; i < bodies.length; i++) {
				SyncContainer container = syncContainers[i];
				Body body = bodies[i];
				Vector2 position = body.getPosition();
				container.positionI.set(position.x, position.y);
				Vector2 velocity = body.getLinearVelocity();
				container.velocityI.set(velocity.x, velocity.y);
			}
		}
		
		@Override
		public short getFlag() {
			return ConstantStorage.FLAG_MESSAGE_SYNCHRONIZING;
		}

		@Override
		protected void onReadTransmissionData(DataInputStream pDataInputStream)
				throws IOException {
			bodies = pDataInputStream.readByte();
			for (int i = 0; i < syncContainers.length; i++) {
				SyncContainer container = syncContainers[i];
				container.positionI.set(pDataInputStream.readFloat(), pDataInputStream.readFloat());
				container.velocityI.set(pDataInputStream.readFloat(), pDataInputStream.readFloat());
			}
		}

		@Override
		protected void onWriteTransmissionData(
				DataOutputStream pDataOutputStream) throws IOException {
			pDataOutputStream.writeByte(bodies);
			for (int i = 0; i < syncContainers.length; i++) {
				SyncContainer container = syncContainers[i];
				Vector2 position = container.positionI;
				Vector2 velocity = container.velocityI;
				pDataOutputStream.writeFloat(position.x);
				pDataOutputStream.writeFloat(position.y);
				pDataOutputStream.writeFloat(velocity.x);
				pDataOutputStream.writeFloat(velocity.y);
			}
		}
	}
	
	private static class SyncContainer{
		public Vector2 positionI = new Vector2();
		public Vector2 velocityI = new Vector2();
	}
	
}
