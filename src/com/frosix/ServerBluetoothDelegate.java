package com.frosix;

import java.io.IOException;

import org.anddev.andengine.extension.multiplayer.protocol.adt.message.client.IClientMessage;
import org.anddev.andengine.extension.multiplayer.protocol.client.connector.BluetoothSocketConnectionServerConnector.IBluetoothSocketConnectionServerConnectorListener;
import org.anddev.andengine.extension.multiplayer.protocol.exception.BluetoothException;
import org.anddev.andengine.extension.multiplayer.protocol.server.BluetoothSocketServer;
import org.anddev.andengine.extension.multiplayer.protocol.server.IClientMessageHandler;
import org.anddev.andengine.extension.multiplayer.protocol.server.connector.BluetoothSocketConnectionClientConnector;
import org.anddev.andengine.extension.multiplayer.protocol.server.connector.BluetoothSocketConnectionClientConnector.IBluetoothSocketConnectionClientConnectorListener;
import org.anddev.andengine.extension.multiplayer.protocol.server.connector.ClientConnector;
import org.anddev.andengine.extension.multiplayer.protocol.shared.BluetoothSocketConnection;
import org.anddev.andengine.extension.multiplayer.protocol.shared.Connector;
import org.anddev.andengine.extension.multiplayer.protocol.shared.IMessageHandler;
import org.anddev.andengine.extension.multiplayer.protocol.shared.Connector.IConnectorListener;
import org.anddev.andengine.util.Debug;

import android.util.Log;

import com.frosix.protocol.adt.message.ICommonMessage;
import com.frosix.protocol.adt.message.MoveSpriteCommonMessage;

public class ServerBluetoothDelegate extends AbstractBluetoothDelegate<BluetoothSocketServer<BluetoothSocketConnectionClientConnector>> {
	
	private IMessageHandler<BluetoothSocketConnection, Connector<BluetoothSocketConnection>, ICommonMessage> moveSpriteMessageHandler;
	
	public ServerBluetoothDelegate(final IConnectorListener<Connector<?>> connectorTerminateListener) {
		try {
			IBluetoothSocketConnectionClientConnectorListener connectorListener = new IBluetoothSocketConnectionClientConnectorListener() {
				@Override
				public void onStarted(
						ClientConnector<BluetoothSocketConnection> pClientConnector) {
					Log.i("listnerLog" ,"SERVER: Client connected: " + pClientConnector.getConnection().getBluetoothSocket().getRemoteDevice().getAddress());
				}
				@Override
				public void onTerminated(
						ClientConnector<BluetoothSocketConnection> pClientConnector) {
					Log.i("listnerLog" , "SERVER: Client disconnected: " + pClientConnector.getConnection().getBluetoothSocket().getRemoteDevice().getAddress());
					connectorTerminateListener.onTerminated(pClientConnector);
				}
			};
			bluetoothEndpoint = new BluetoothSocketServer<BluetoothSocketConnectionClientConnector>(ConstantStorage.MY_UUID, connectorListener, new ServerStateListener()) {
				@Override
				protected BluetoothSocketConnectionClientConnector newClientConnector(final BluetoothSocketConnection pBluetoothSocketConnection) throws IOException {
					try {
						BluetoothSocketConnectionClientConnector clientConnector = new BluetoothSocketConnectionClientConnector(pBluetoothSocketConnection);
						clientConnector.registerClientMessage(ConstantStorage.FLAG_MESSAGE_COMMON_MOVE_SPRITE, MoveSpriteCommonMessage.class, new IClientMessageHandler<BluetoothSocketConnection>() {
							@Override
							public void onHandleMessage(
									ClientConnector<BluetoothSocketConnection> pClientConnector,
									IClientMessage pClientMessage) throws IOException {
								final MoveSpriteCommonMessage moveSpriteCommonMessage = (MoveSpriteCommonMessage)pClientMessage;
								moveSpriteMessageHandler.onHandleMessage(pClientConnector, moveSpriteCommonMessage);
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
	}
	
	@Override
	public void setMoveSpriteMessageHandler(
			IMessageHandler<BluetoothSocketConnection, Connector<BluetoothSocketConnection>, ICommonMessage> moveSpriteMessageHandler) {
		this.moveSpriteMessageHandler = moveSpriteMessageHandler;
	}
	
	@Override
	public void init() {
		bluetoothEndpoint.start();
	}

	@Override
	public void sendMessage(ICommonMessage message) {
		try {
			bluetoothEndpoint.sendBroadcastServerMessage(message);
		} catch (IOException e) {
			Log.e(ConstantStorage.DEBUGTAG, "Unable to send server message", e);
		}
	}

}
