package com.frosix;


import org.anddev.andengine.extension.multiplayer.protocol.server.connector.ClientConnector;
import org.anddev.andengine.extension.multiplayer.protocol.server.connector.BluetoothSocketConnectionClientConnector.IBluetoothSocketConnectionClientConnectorListener;
import org.anddev.andengine.extension.multiplayer.protocol.shared.BluetoothSocketConnection;

import android.util.Log;

public class ClientConnectorListener implements IBluetoothSocketConnectionClientConnectorListener {
	@Override
	public void onStarted(final ClientConnector<BluetoothSocketConnection> pConnector) {
		Log.i("listnerLog" ,"SERVER: Client connected: " + pConnector.getConnection().getBluetoothSocket().getRemoteDevice().getAddress());
	}

	@Override
	public void onTerminated(final ClientConnector<BluetoothSocketConnection> pConnector) {
		Log.i("listnerLog" , "SERVER: Client disconnected: " + pConnector.getConnection().getBluetoothSocket().getRemoteDevice().getAddress());
	}
}
