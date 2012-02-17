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

import com.frosix.protocol.adt.message.ConnectionCloseCommonMessage;
import com.frosix.protocol.adt.message.ICommonMessage;

public class ClientBluetoothDelegate extends AbstractBluetoothDelegate<ServerConnector<BluetoothSocketConnection>> {
	
	public ClientBluetoothDelegate(String serverMacAddress, final IConnectorListener<Connector<?>> connectorListener) {
		try {
			IBluetoothSocketConnectionServerConnectorListener listener = new IBluetoothSocketConnectionServerConnectorListener() {
				@Override
				public void onStarted(final ServerConnector<BluetoothSocketConnection> pConnector) {
					Log.i("listnerLog" ,"CLIENT: Connected to server.");
					connectorListener.onStarted(pConnector);
				}
				@Override
				public void onTerminated(final ServerConnector<BluetoothSocketConnection> pConnector) {
					Log.i("listnerLog" ,"CLIENT: Disconnected from Server...");
					connectorListener.onTerminated(pConnector);
				}
			};
			bluetoothEndpoint = new BluetoothSocketConnectionServerConnector(new BluetoothSocketConnection(BluetoothAdapter.getDefaultAdapter(), serverMacAddress, ConstantStorage.MY_UUID), listener);
		} catch (final Throwable t) {
			Debug.e(t);
		}
	}
	
	@Override
	public void registerMessage(short flag, Class<? extends ICommonMessage> messageClass,
			final IMessageHandler<BluetoothSocketConnection, Connector<BluetoothSocketConnection>, ICommonMessage> messageHandler) {
		bluetoothEndpoint.registerServerMessage(flag, messageClass, new IServerMessageHandler<BluetoothSocketConnection>() {
			@Override
			public void onHandleMessage(final ServerConnector<BluetoothSocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
				messageHandler.onHandleMessage(pServerConnector, (ICommonMessage)pServerMessage);
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
			Debug.e(e);
		}
	}

	@Override
	public void onDestroy() {
		try {
			bluetoothEndpoint.sendClientMessage(new ConnectionCloseCommonMessage());
		} catch (final IOException e) {
			Debug.e(e);
		}
		bluetoothEndpoint.terminate();
	}
	
}
