package com.frosix;

import java.io.IOException;

import org.anddev.andengine.extension.multiplayer.protocol.adt.message.server.IServerMessage;
import org.anddev.andengine.extension.multiplayer.protocol.client.IServerMessageHandler;
import org.anddev.andengine.extension.multiplayer.protocol.client.connector.BluetoothSocketConnectionServerConnector;
import org.anddev.andengine.extension.multiplayer.protocol.client.connector.ServerConnector;
import org.anddev.andengine.extension.multiplayer.protocol.client.connector.BluetoothSocketConnectionServerConnector.IBluetoothSocketConnectionServerConnectorListener;
import org.anddev.andengine.extension.multiplayer.protocol.shared.BluetoothSocketConnection;
import org.anddev.andengine.extension.multiplayer.protocol.shared.Connector;
import org.anddev.andengine.extension.multiplayer.protocol.shared.IMessageHandler;
import org.anddev.andengine.extension.multiplayer.protocol.shared.Connector.IConnectorListener;
import org.anddev.andengine.util.Debug;

import android.bluetooth.BluetoothAdapter;
import android.util.Log;

import com.frosix.protocol.adt.message.ICommonMessage;
import com.frosix.protocol.adt.message.MoveSpriteCommonMessage;

public class ClientBluetoothDelegate extends AbstractBluetoothDelegate<ServerConnector<BluetoothSocketConnection>> {
	
	public ClientBluetoothDelegate(BluetoothAdapter bluetoothAdapter, String serverMacAddress, final IConnectorListener<Connector<?>> connectorTerminateListener) {
		try {
			IBluetoothSocketConnectionServerConnectorListener connectorListener = new IBluetoothSocketConnectionServerConnectorListener() {
				@Override
				public void onStarted(final ServerConnector<BluetoothSocketConnection> pConnector) {
					Log.i("listnerLog" ,"CLIENT: Connected to server.");
				}
				@Override
				public void onTerminated(final ServerConnector<BluetoothSocketConnection> pConnector) {
					Log.i("listnerLog" ,"CLIENT: Disconnected from Server...");
					connectorTerminateListener.onTerminated(pConnector);
				}
			};
			bluetoothEndpoint = new BluetoothSocketConnectionServerConnector(new BluetoothSocketConnection(bluetoothAdapter, serverMacAddress, ConstantStorage.MY_UUID), connectorListener);
		} catch (final Throwable t) {
			Debug.e(t);
		}
	}
	
	@Override
	public void setMoveSpriteMessageHandler(
			final IMessageHandler<BluetoothSocketConnection, Connector<BluetoothSocketConnection>, ICommonMessage> moveSpriteMessageHandler) {
		bluetoothEndpoint.registerServerMessage(ConstantStorage.FLAG_MESSAGE_COMMON_MOVE_SPRITE, MoveSpriteCommonMessage.class, new IServerMessageHandler<BluetoothSocketConnection>() {
			@Override
			public void onHandleMessage(final ServerConnector<BluetoothSocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
				final MoveSpriteCommonMessage moveSpriteCommonMessage = (MoveSpriteCommonMessage)pServerMessage;
				moveSpriteMessageHandler.onHandleMessage(pServerConnector, moveSpriteCommonMessage);
			}
		});
	}
	

	@Override
	public void init() {
		bluetoothEndpoint.start();
	}

	@Override
	public void sendMessage(ICommonMessage message) {
		try {
			bluetoothEndpoint.sendClientMessage(message);
		} catch (IOException e) {
			Log.e(ConstantStorage.DEBUGTAG, "Unable to send server message", e);
		}
	}
	
}
