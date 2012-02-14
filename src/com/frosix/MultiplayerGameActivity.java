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
import org.anddev.andengine.extension.multiplayer.protocol.adt.message.client.IClientMessage;
import org.anddev.andengine.extension.multiplayer.protocol.adt.message.server.IServerMessage;
import org.anddev.andengine.extension.multiplayer.protocol.client.IServerMessageHandler;
import org.anddev.andengine.extension.multiplayer.protocol.client.connector.BluetoothSocketConnectionServerConnector;
import org.anddev.andengine.extension.multiplayer.protocol.client.connector.BluetoothSocketConnectionServerConnector.IBluetoothSocketConnectionServerConnectorListener;
import org.anddev.andengine.extension.multiplayer.protocol.client.connector.ServerConnector;
import org.anddev.andengine.extension.multiplayer.protocol.exception.BluetoothException;
import org.anddev.andengine.extension.multiplayer.protocol.server.BluetoothSocketServer;
import org.anddev.andengine.extension.multiplayer.protocol.server.IClientMessageHandler;
import org.anddev.andengine.extension.multiplayer.protocol.server.connector.BluetoothSocketConnectionClientConnector;
import org.anddev.andengine.extension.multiplayer.protocol.server.connector.ClientConnector;
import org.anddev.andengine.extension.multiplayer.protocol.shared.BluetoothSocketConnection;
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

import com.frosix.adt.messages.server.ConnectionCloseServerMessage;
import com.frosix.util.BluetoothListDevicesActivity;

public class MultiplayerGameActivity extends BaseGameActivity implements ConstantStorage {

	
	private Camera mCamera ;
	private Scene mScene;
	private Rectangle selfRect;
	private Rectangle alienRect;
	private String mServerMACAddress;
	private BluetoothSocketServer<BluetoothSocketConnectionClientConnector> mBluetoothSocketServer;
	private BluetoothAdapter mBluetoothAdapter;
	private final MessagePool<IMessage> mMessagePool = new MessagePool<IMessage>();
	private ServerConnector<BluetoothSocketConnection> mServerConnector;
	
	@Override
	protected void onCreate(Bundle pSavedInstanceState) {
		super.onCreate(pSavedInstanceState);
		initMessagePool();
		this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		this.mServerMACAddress = BluetoothAdapter.getDefaultAdapter().getAddress();
		if (this.mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available!", Toast.LENGTH_LONG).show();
			this.finish();
			return;
		} else {
			if (this.mBluetoothAdapter.isEnabled()) {
				this.showDialog(DIALOG_CHOOSE_SERVER_OR_CLIENT_ID);
			} else {
				final Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				this.startActivityForResult(enableIntent, REQUESTCODE_BLUETOOTH_ENABLE);
			}
		}
	}
	
	private void initMessagePool() {
		this.mMessagePool.registerMessage(FLAG_MESSAGE_SERVER_MOVE_SPRITE, MoveSpriteServerMessage.class);
		this.mMessagePool.registerMessage(FLAG_MESSAGE_SERVER_CONNECTION_CLOSE, ConnectionCloseServerMessage.class);
		this.mMessagePool.registerMessage(FLAG_MESSAGE_CLIENT_MOVE_SPRITE, MoveSpriteClientMessage.class);
	}
	
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
			    
			    if (mBluetoothSocketServer == null) {
			    	MultiplayerGameActivity.this.sendClientMessage(x, y);
			    } else {
			    	MultiplayerGameActivity.this.sendServerMessage(x, y);
			    }
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
	
	private void sendClientMessage(float x, float y) {
		final MoveSpriteClientMessage moveSpriteClientMessage = (MoveSpriteClientMessage) mMessagePool.obtainMessage(FLAG_MESSAGE_CLIENT_MOVE_SPRITE);
		moveSpriteClientMessage.set(0, x, y);

		try {
			mServerConnector.sendClientMessage(moveSpriteClientMessage);
		} catch (IOException e) {
			Log.e(DEBUGTAG, "Unable to send client message", e);
		}

		mMessagePool.recycleMessage(moveSpriteClientMessage);
	}
	
	private void sendServerMessage(float x, float y) {
		final MoveSpriteServerMessage moveFaceServerMessage = (MoveSpriteServerMessage) mMessagePool.obtainMessage(FLAG_MESSAGE_SERVER_MOVE_SPRITE);
		moveFaceServerMessage.set(0, x, y);

		try {
			mBluetoothSocketServer.sendBroadcastServerMessage(moveFaceServerMessage);
		} catch (IOException e) {
			Log.e(DEBUGTAG, "Unable to send server message", e);
		}

		mMessagePool.recycleMessage(moveFaceServerMessage);
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
						BluetoothSocketConnectionClientConnector clientConnector = new BluetoothSocketConnectionClientConnector(pBluetoothSocketConnection);
						clientConnector.registerClientMessage(FLAG_MESSAGE_CLIENT_MOVE_SPRITE, MoveSpriteClientMessage.class, new IClientMessageHandler<BluetoothSocketConnection>() {
							@Override
							public void onHandleMessage(
									ClientConnector<BluetoothSocketConnection> pClientConnector,
									IClientMessage pClientMessage) throws IOException {
								final MoveSpriteClientMessage moveSpriteClientMessage = (MoveSpriteClientMessage)pClientMessage;
								MultiplayerGameActivity.this.moveSprite(moveSpriteClientMessage.getID(), moveSpriteClientMessage.getX(), moveSpriteClientMessage.getY());
							}
						});
						return clientConnector;
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
	
	private void initClient() {
		try {
			this.mServerConnector = new BluetoothSocketConnectionServerConnector(new BluetoothSocketConnection(this.mBluetoothAdapter, this.mServerMACAddress, MY_UUID), new ExampleServerConnectorListener());

			this.mServerConnector.registerServerMessage(FLAG_MESSAGE_SERVER_CONNECTION_CLOSE, ConnectionCloseServerMessage.class, new IServerMessageHandler<BluetoothSocketConnection>() {
				@Override
				public void onHandleMessage(final ServerConnector<BluetoothSocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
					MultiplayerGameActivity.this.finish();
				}
			});

			this.mServerConnector.registerServerMessage(FLAG_MESSAGE_SERVER_MOVE_SPRITE, MoveSpriteServerMessage.class, new IServerMessageHandler<BluetoothSocketConnection>() {
				@Override
				public void onHandleMessage(final ServerConnector<BluetoothSocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
					final MoveSpriteServerMessage moveSpriteServerMessage = (MoveSpriteServerMessage)pServerMessage;
					MultiplayerGameActivity.this.moveSprite(moveSpriteServerMessage.getID(), moveSpriteServerMessage.getX(), moveSpriteServerMessage.getY());
				}
			});

			this.mServerConnector.getConnection().start();
		} catch (final Throwable t) {
			Debug.e(t);
		}
	}
	
	public void moveSprite(final int pID, final float pX, final float pY) {
		/* Move the sprite. */
		alienRect.setPosition(pX - alienRect.getWidth() * 0.5f, pY - alienRect.getHeight() * 0.5f);
	}
	
	private class ExampleServerConnectorListener implements IBluetoothSocketConnectionServerConnectorListener {
		@Override
		public void onStarted(final ServerConnector<BluetoothSocketConnection> pConnector) {
			Log.i("listnerLog" ,"CLIENT: Connected to server.");
		}

		@Override
		public void onTerminated(final ServerConnector<BluetoothSocketConnection> pConnector) {
			Log.i("listnerLog" ,"CLIENT: Disconnected from Server...");
			MultiplayerGameActivity.this.finish();
		}
	}
	
	@Override
	protected Dialog onCreateDialog(final int pID) {
		switch(pID) {
			case DIALOG_SHOW_SERVER_IP_ID:
				return new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setTitle("Server-Details")
				.setCancelable(false)
				.setMessage("The Name of your Server is:\n" + BluetoothAdapter.getDefaultAdapter().getName() + "\n" + "The MACAddress of your Server is:\n" + this.mServerMACAddress)
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
						MultiplayerGameActivity.this.initServer();
						MultiplayerGameActivity.this.showDialog(DIALOG_SHOW_SERVER_IP_ID);
					}
				})
				.create();
			default:
				return super.onCreateDialog(pID);
		}
	}
	
	private void toast(final String pMessage) {
		this.log(pMessage);
		this.runOnUiThread(new Runnable() {
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
				this.showDialog(DIALOG_CHOOSE_SERVER_OR_CLIENT_ID);
				break;
			case REQUESTCODE_BLUETOOTH_CONNECT:
				this.mServerMACAddress = pData.getExtras().getString(BluetoothListDevicesActivity.EXTRA_DEVICE_ADDRESS);
				this.initClient();
				break;
			default:
				super.onActivityResult(pRequestCode, pResultCode, pData);
		}
	}
   
    
}