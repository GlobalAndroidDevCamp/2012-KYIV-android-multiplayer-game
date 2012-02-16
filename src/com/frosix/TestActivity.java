package com.frosix;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.extension.multiplayer.protocol.shared.BluetoothSocketConnection;
import org.anddev.andengine.extension.multiplayer.protocol.shared.Connector;
import org.anddev.andengine.input.touch.TouchEvent;

import android.view.Display;

import com.frosix.protocol.adt.message.ICommonMessage;
import com.frosix.protocol.adt.message.MoveSpriteCommonMessage;

public class TestActivity extends BaseMultiplayerGameActivity {
	
	private Rectangle selfRect;
	private Rectangle alienRect;
	@SuppressWarnings("serial")
	private Map<Short, Class<? extends ICommonMessage>> messageMap = new HashMap<Short, Class<? extends ICommonMessage>>() {{
		put(FLAG_MESSAGE_COMMON_MOVE_SPRITE, MoveSpriteCommonMessage.class);
	}};
	
	@Override
	public Engine onLoadEngine() {
		Display display = getWindowManager().getDefaultDisplay();
		int CAMERA_WIDTH  = display.getWidth();
		int CAMERA_HEIGHT = display.getHeight();
		Camera mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		return new Engine(new EngineOptions(true, ScreenOrientation.PORTRAIT, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), mCamera));
	}
	
	@Override
	public void onLoadResources() {
	
	}

	@Override
	public Scene onLoadScene() {
		Scene mScene = new Scene();
		
		alienRect = new Rectangle( 0 ,0 ,80, 80) ;
		mScene.attachChild(alienRect);
		
		selfRect = new Rectangle(0, 0, 80, 80);
		mScene.attachChild(selfRect);
		mScene.setOnSceneTouchListener(new IOnSceneTouchListener() {
			@Override
			public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
			    float x = pSceneTouchEvent.getX();
			    float y = pSceneTouchEvent.getY();
				selfRect.setPosition(x - selfRect.getWidth() * 0.5f, y - selfRect.getHeight() * 0.5f);
				final MoveSpriteCommonMessage moveSpriteCommonMessage = (MoveSpriteCommonMessage) getMessage(FLAG_MESSAGE_COMMON_MOVE_SPRITE);
				moveSpriteCommonMessage.set(0, x, y);
				sendMessage(moveSpriteCommonMessage);
				return true;
			}
		});
		mScene.registerUpdateHandler(new IUpdateHandler(){
			@Override
			public void onUpdate(float pSecondsElapsed) {
				if(selfRect.collidesWith(alienRect) ){
					selfRect.setColor(255, 0, 0);
					alienRect.setColor(255, 0, 0);
				}
				else {
					selfRect.setColor(0, 0, 255);
					alienRect.setColor( 0 ,255 ,0);
				}
			}
			@Override
			public void reset() {					
			}});
		return mScene;
	}

	@Override
	public void onLoadComplete() {}
	
	@Override
	public void onHandleMessage(
			Connector<BluetoothSocketConnection> pConnector,
			ICommonMessage pMessage) throws IOException {
		if (pMessage instanceof MoveSpriteCommonMessage) {
			MoveSpriteCommonMessage moveSpriteCommonMessage = (MoveSpriteCommonMessage)pMessage;
			moveSprite(moveSpriteCommonMessage.mID, moveSpriteCommonMessage.mX, moveSpriteCommonMessage.mY);
			return;
		}
		super.onHandleMessage(pConnector, pMessage);
	}

	public void moveSprite(final int pID, final float pX, final float pY) {
		alienRect.setPosition(pX - alienRect.getWidth() * 0.5f, pY - alienRect.getHeight() * 0.5f);
	}

	public Map<Short, Class<? extends ICommonMessage>> getMessageMap() {
		return messageMap;
	}

}
