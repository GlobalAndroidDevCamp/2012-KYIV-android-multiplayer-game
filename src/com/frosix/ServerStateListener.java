package com.frosix;


import org.anddev.andengine.extension.multiplayer.protocol.server.BluetoothSocketServer;
import org.anddev.andengine.extension.multiplayer.protocol.server.BluetoothSocketServer.IBluetoothSocketServerListener;
import org.anddev.andengine.extension.multiplayer.protocol.server.connector.BluetoothSocketConnectionClientConnector;
import org.anddev.andengine.util.Debug;

import android.util.Log;

public class ServerStateListener implements IBluetoothSocketServerListener<BluetoothSocketConnectionClientConnector> {
	@Override
	public void onStarted(final BluetoothSocketServer<BluetoothSocketConnectionClientConnector> pBluetoothSocketServer) {
		Log.i("listnerLog" ,"SERVER: Started.");
	}

	@Override
	public void onTerminated(final BluetoothSocketServer<BluetoothSocketConnectionClientConnector> pBluetoothSocketServer) {
		Log.i("listnerLog" ,"SERVER: Terminated.");
	}

	@Override
	public void onException(final BluetoothSocketServer<BluetoothSocketConnectionClientConnector> pBluetoothSocketServer, final Throwable pThrowable) {
		Debug.e(pThrowable);
		Log.i("listnerLog" ,"SERVER: Exception: " + pThrowable);
	}
}