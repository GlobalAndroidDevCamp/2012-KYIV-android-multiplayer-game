package com.frosix;

import java.io.IOException;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.extension.multiplayer.protocol.adt.message.IMessage;
import org.anddev.andengine.extension.multiplayer.protocol.shared.BluetoothSocketConnection;
import org.anddev.andengine.extension.multiplayer.protocol.shared.Connector;
import org.anddev.andengine.extension.multiplayer.protocol.shared.Connector.IConnectorListener;
import org.anddev.andengine.extension.multiplayer.protocol.shared.IMessageHandler;
import org.anddev.andengine.extension.multiplayer.protocol.util.MessagePool;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import org.anddev.andengine.util.Debug;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.widget.Toast;

import com.frosix.protocol.adt.message.ICommonMessage;
import com.frosix.protocol.adt.message.MoveSpriteCommonMessage;
import com.frosix.util.BluetoothListDevicesActivity;

public class MultiplayerGameActivity extends BaseGameActivity implements IMessageHandler<BluetoothSocketConnection, Connector<BluetoothSocketConnection>, ICommonMessage>, ConstantStorage,
																			IConnectorListener<Connector<?>> {
	
	private Camera mCamera;
	private Scene mScene;
	private Rectangle selfRect;
	private Rectangle alienRect;
	private BluetoothAdapter mBluetoothAdapter;
	private final MessagePool<IMessage> mMessagePool = new MessagePool<IMessage>();
	private BluetoothDelegate bluetoothDelegate; 
	
	@Override
	protected void onCreate(Bundle pSavedInstanceState) {
		super.onCreate(pSavedInstanceState);
		initMessagePool();
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available!", Toast.LENGTH_LONG).show();
			finish();
			return;
		} else {
			if (mBluetoothAdapter.isEnabled()) {
				showDialog(DIALOG_CHOOSE_SERVER_OR_CLIENT_ID);
			} else {
				final Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableIntent, REQUESTCODE_BLUETOOTH_ENABLE);
			}
		}
	}
	
	private void initMessagePool() {
		this.mMessagePool.registerMessage(FLAG_MESSAGE_COMMON_MOVE_SPRITE, MoveSpriteCommonMessage.class);
		this.mMessagePool.registerMessage(FLAG_MESSAGE_SERVER_CONNECTION_CLOSE, com.frosix.protocol.adt.message.server.ConnectionCloseServerMessage.class);
	}
	
	@Override
	public Engine onLoadEngine() {
		Display display = getWindowManager().getDefaultDisplay(); 
		int CAMERA_WIDTH  = display.getWidth();
		int CAMERA_HEIGHT = display.getHeight();
		Log.i("info" , " Width " + CAMERA_WIDTH + " HEIGHT =  " + CAMERA_HEIGHT); //new RatioResolutionPolicy(320 , 480),
		mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
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
				final MoveSpriteCommonMessage moveSpriteCommonMessage = (MoveSpriteCommonMessage) mMessagePool.obtainMessage(FLAG_MESSAGE_COMMON_MOVE_SPRITE);
				moveSpriteCommonMessage.set(0, x, y);
				bluetoothDelegate.sendMessage(moveSpriteCommonMessage);
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
		MoveSpriteCommonMessage moveSpriteCommonMessage = (MoveSpriteCommonMessage)pMessage;
		moveSprite(moveSpriteCommonMessage.mID, moveSpriteCommonMessage.mX, moveSpriteCommonMessage.mY);
	}

	public void moveSprite(final int pID, final float pX, final float pY) {
		alienRect.setPosition(pX - alienRect.getWidth() * 0.5f, pY - alienRect.getHeight() * 0.5f);
	}
	
	@Override
	public void onStarted(Connector<?> pConnector) {
	}

	@Override
	public void onTerminated(Connector<?> pConnector) {
		finish();
	}

	@Override
	protected Dialog onCreateDialog(final int pID) {
		switch(pID) {
			case DIALOG_SHOW_SERVER_IP_ID:
				return new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setTitle("Server-Details")
				.setCancelable(false)
				.setMessage("The Name of your Server is:\n" + mBluetoothAdapter.getName() + "\n" + "The MACAddress of your Server is:\n" + mBluetoothAdapter.getAddress())
				.setPositiveButton(android.R.string.ok, null)
				.create();
			case DIALOG_CHOOSE_SERVER_OR_CLIENT_ID:
				return new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setTitle("Be Server or Client ...")
				.setCancelable(false)
				.setPositiveButton("Client", new OnClickListener() {
					@Override
					public void onClick(final DialogInterface pDialog, final int pWhich) {
						final Intent intent = new Intent(MultiplayerGameActivity.this, BluetoothListDevicesActivity.class);
						MultiplayerGameActivity.this.startActivityForResult(intent, REQUESTCODE_BLUETOOTH_CONNECT);
					}
				})
				.setNegativeButton("Server", new OnClickListener() {
					@Override
					public void onClick(final DialogInterface pDialog, final int pWhich) {
						MultiplayerGameActivity.this.toast("You can add and move sprites, which are only shown on the clients.");
						MultiplayerGameActivity.this.bluetoothDelegate = new ServerBluetoothDelegate(MultiplayerGameActivity.this);
						MultiplayerGameActivity.this.bluetoothDelegate.setMoveSpriteMessageHandler(MultiplayerGameActivity.this);
						MultiplayerGameActivity.this.bluetoothDelegate.init();
						MultiplayerGameActivity.this.showDialog(DIALOG_SHOW_SERVER_IP_ID);
					}
				})
				.create();
			default:
				return super.onCreateDialog(pID);
		}
	}
	
	private void toast(final String pMessage) {
		log(pMessage);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(MultiplayerGameActivity.this, pMessage, Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	private void log(final String pMessage) {
		Debug.d(pMessage);
	}
	
	@Override
	protected void onActivityResult(final int pRequestCode, final int pResultCode, final Intent pData) {
		switch(pRequestCode) {
			case REQUESTCODE_BLUETOOTH_ENABLE:
				showDialog(DIALOG_CHOOSE_SERVER_OR_CLIENT_ID);
				break;
			case REQUESTCODE_BLUETOOTH_CONNECT:
				String mServerMACAddress = pData.getExtras().getString(BluetoothListDevicesActivity.EXTRA_DEVICE_ADDRESS);
				bluetoothDelegate = new ClientBluetoothDelegate(mBluetoothAdapter, mServerMACAddress, this);
				bluetoothDelegate.setMoveSpriteMessageHandler(this);
				bluetoothDelegate.init();
				break;
			default:
				super.onActivityResult(pRequestCode, pResultCode, pData);
		}
	}
   
    
}