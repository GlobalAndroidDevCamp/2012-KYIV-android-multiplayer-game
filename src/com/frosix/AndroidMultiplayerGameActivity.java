package com.frosix;

import java.io.IOException;
import java.util.UUID;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.extension.multiplayer.protocol.exception.BluetoothException;
import org.anddev.andengine.extension.multiplayer.protocol.server.BluetoothSocketServer;
import org.anddev.andengine.extension.multiplayer.protocol.server.connector.BluetoothSocketConnectionClientConnector;
import org.anddev.andengine.extension.multiplayer.protocol.shared.BluetoothSocketConnection;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import org.anddev.andengine.util.Debug;

import android.bluetooth.BluetoothAdapter;
import android.util.Log;
import android.view.Display;

public class AndroidMultiplayerGameActivity extends BaseGameActivity  {

	
	private Camera mCamera ;
	private BitmapTextureAtlas mBitmapTextureAtlas;
	private Scene mScene;
	Rectangle rect;
	private String mServerMACAddress;
	private BluetoothSocketServer<BluetoothSocketConnectionClientConnector> mBluetoothSocketServer;
	
	
	
	@Override
	public Engine onLoadEngine() {
		Display display = getWindowManager().getDefaultDisplay(); 
		  int CAMERA_WIDTH  = display.getWidth();
		  int CAMERA_HEIGHT = display.getHeight();
		  Log.i("info" , " Width " + CAMERA_WIDTH + " HEIGHT =  " + CAMERA_HEIGHT); //new RatioResolutionPolicy(320 , 480),
		  this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		  final EngineOptions engineOptions = new EngineOptions(true,
					ScreenOrientation.PORTRAIT, new RatioResolutionPolicy(
							CAMERA_WIDTH, CAMERA_HEIGHT), mCamera);
			
			
			final Engine mEngine = new Engine(engineOptions);
		
			return mEngine;
	}
	@Override
	public void onLoadResources() {
	
	}

	@Override
	public Scene onLoadScene() {
		mScene = new Scene();
		rect = new Rectangle( 0 ,0 ,80, 80) ;
		rect.setColor( 0 ,255 ,0);
		
		mScene.attachChild(rect);
		
		
		mScene.setOnSceneTouchListener(new IOnSceneTouchListener() {
			@Override
			public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
			    rect.setPosition(pSceneTouchEvent.getX()-40 , pSceneTouchEvent.getY()-40 );
				return true;
			}
		});
		
		return mScene;
	}

	@Override
	public void onLoadComplete() {}
	

	private void initServer() {
		this.mServerMACAddress = BluetoothAdapter.getDefaultAdapter().getAddress();
		try {
			this.mBluetoothSocketServer = new BluetoothSocketServer<BluetoothSocketConnectionClientConnector>(ConstantStorage.MY_UUID, new ClientConnectorListener(), new ServerStateListener()) {
				@Override
				protected BluetoothSocketConnectionClientConnector newClientConnector(final BluetoothSocketConnection pBluetoothSocketConnection) throws IOException {
					try {
						return new BluetoothSocketConnectionClientConnector(pBluetoothSocketConnection);
					} catch (final BluetoothException e) {
						Debug.e(e);
						/* Actually cannot happen. */
						return null;
					}
				}
			};
		} catch (final BluetoothException e) {
			Debug.e(e);
		}

		this.mBluetoothSocketServer.start();
	}
	
   
    
}