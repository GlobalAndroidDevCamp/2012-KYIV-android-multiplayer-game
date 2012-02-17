package com.frosix;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.anddev.andengine.extension.multiplayer.protocol.adt.message.client.IClientMessage;
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

import com.frosix.protocol.adt.message.ConnectionCloseCommonMessage;
import com.frosix.protocol.adt.message.ICommonMessage;

public class ServerBluetoothDelegate extends AbstractBluetoothDelegate<BluetoothSocketServer<BluetoothSocketConnectionClientConnector>> {

	private List<MessageDescriptor> messageDescriptors = new ArrayList<MessageDescriptor>();
	
	public ServerBluetoothDelegate(final IConnectorListener<Connector<?>> connectorListener) {
		try {
			IBluetoothSocketConnectionClientConnectorListener listener = new IBluetoothSocketConnectionClientConnectorListener() {
				@Override
				public void onStarted(
						ClientConnector<BluetoothSocketConnection> pClientConnector) {
					Log.i("listnerLog" , "SERVER: Client connected: " + pClientConnector.getConnection().getBluetoothSocket().getRemoteDevice().getAddress());
					connectorListener.onStarted(pClientConnector);
				}
				@Override
				public void onTerminated(
						ClientConnector<BluetoothSocketConnection> pClientConnector) {
					Log.i("listnerLog" , "SERVER: Client disconnected: " + pClientConnector.getConnection().getBluetoothSocket().getRemoteDevice().getAddress());
					connectorListener.onTerminated(pClientConnector);
				}
			};
			bluetoothEndpoint = new BluetoothSocketServer<BluetoothSocketConnectionClientConnector>(ConstantStorage.MY_UUID, listener, new ServerStateListener()) {
				@Override
				protected BluetoothSocketConnectionClientConnector newClientConnector(final BluetoothSocketConnection pBluetoothSocketConnection) throws IOException {
					try {
						BluetoothSocketConnectionClientConnector clientConnector = new BluetoothSocketConnectionClientConnector(pBluetoothSocketConnection);
						for (final MessageDescriptor messageDescriptor : messageDescriptors) {
							clientConnector.registerClientMessage(messageDescriptor.getFlag(), messageDescriptor.getMessageClass(), new IClientMessageHandler<BluetoothSocketConnection>() {
								@Override
								public void onHandleMessage(
										ClientConnector<BluetoothSocketConnection> pClientConnector,
										IClientMessage pClientMessage) throws IOException {
									messageDescriptor.getMessageHandler().onHandleMessage(pClientConnector, (ICommonMessage)pClientMessage);
								}
							});
						}
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
	public void registerMessage(short flag, Class<? extends ICommonMessage> messageClass,
			IMessageHandler<BluetoothSocketConnection, Connector<BluetoothSocketConnection>, ICommonMessage> messageHandler) {
		messageDescriptors.add(new MessageDescriptor(flag, messageClass, messageHandler));
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
			Debug.e(e);
		}
	}
	
	@Override
	public void onDestroy() {
		try {
			bluetoothEndpoint.sendBroadcastServerMessage(new ConnectionCloseCommonMessage());
		} catch (final IOException e) {
			Debug.e(e);
		}
		bluetoothEndpoint.terminate();
	}
	
	private static class MessageDescriptor {
		
		private short flag;
		private Class<? extends ICommonMessage> messageClass;
		private IMessageHandler<BluetoothSocketConnection, Connector<BluetoothSocketConnection>, ICommonMessage> messageHandler;
		
		public MessageDescriptor(
				short flag,
				Class<? extends ICommonMessage> messageClass,
				IMessageHandler<BluetoothSocketConnection, Connector<BluetoothSocketConnection>, ICommonMessage> messageHandler) {
			super();
			this.flag = flag;
			this.messageClass = messageClass;
			this.messageHandler = messageHandler;
		}
		
		public short getFlag() {
			return flag;
		}
		
		public Class<? extends ICommonMessage> getMessageClass() {
			return messageClass;
		}
		
		public IMessageHandler<BluetoothSocketConnection, Connector<BluetoothSocketConnection>, ICommonMessage> getMessageHandler() {
			return messageHandler;
		}
		
	}

}
