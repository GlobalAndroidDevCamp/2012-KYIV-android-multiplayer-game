package com.frosix;

import org.anddev.andengine.extension.multiplayer.protocol.shared.BluetoothSocketConnection;
import org.anddev.andengine.extension.multiplayer.protocol.shared.Connector;
import org.anddev.andengine.extension.multiplayer.protocol.shared.IMessageHandler;

import com.frosix.protocol.adt.message.ICommonMessage;

public interface BluetoothDelegate {
	
	void sendMessage(ICommonMessage message);
	
	void init();
	
	void registerMessage(short flag, Class<? extends ICommonMessage> messageClass, IMessageHandler<BluetoothSocketConnection, Connector<BluetoothSocketConnection>, ICommonMessage> messageHandler);
	
	void onDestroy();
	
}
