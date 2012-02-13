package protocol.server;

import org.anddev.andengine.extension.multiplayer.protocol.adt.message.client.IClientMessage;
import org.anddev.andengine.extension.multiplayer.protocol.exception.BluetoothException;
import org.anddev.andengine.extension.multiplayer.protocol.server.BluetoothSocketServer;
import org.anddev.andengine.extension.multiplayer.protocol.server.IClientMessageHandler;
import org.anddev.andengine.extension.multiplayer.protocol.server.connector.ClientConnector;
import org.anddev.andengine.extension.multiplayer.protocol.server.connector.ClientConnector.IClientConnectorListener;
import org.anddev.andengine.extension.multiplayer.protocol.shared.BluetoothSocketConnection;
import org.anddev.andengine.util.ParameterCallable;

public abstract class MessageReceivingBluetoothSocketServer<CC extends ClientConnector<BluetoothSocketConnection>> extends BluetoothSocketServer<CC> {

	public MessageReceivingBluetoothSocketServer(
			String pUUID,
			org.anddev.andengine.extension.multiplayer.protocol.server.BluetoothSocketServer.IBluetoothSocketServerListener<CC> pBluetoothSocketServerListener)
			throws BluetoothException {
		super(pUUID, pBluetoothSocketServerListener);
	}

	public MessageReceivingBluetoothSocketServer(
			String pUUID,
			IClientConnectorListener<BluetoothSocketConnection> pClientConnectorListener,
			org.anddev.andengine.extension.multiplayer.protocol.server.BluetoothSocketServer.IBluetoothSocketServerListener<CC> pBluetoothSocketServerListener)
			throws BluetoothException {
		super(pUUID, pClientConnectorListener, pBluetoothSocketServerListener);
	}

	public MessageReceivingBluetoothSocketServer(
			String pUUID,
			IClientConnectorListener<BluetoothSocketConnection> pClientConnectorListener)
			throws BluetoothException {
		super(pUUID, pClientConnectorListener);
	}

	public MessageReceivingBluetoothSocketServer(String pUUID)
			throws BluetoothException {
		super(pUUID);
	}
	
	public void registerClientMessage(final short pFlag, final Class<? extends IClientMessage> pClientMessageClass, final IClientMessageHandler<BluetoothSocketConnection> pClientMessageHandler) {
		mClientConnectors.call(new ParameterCallable<CC>() {
			@Override
			public void call(CC pParameter) {
				pParameter.registerClientMessage(pFlag, pClientMessageClass, pClientMessageHandler);
			}
		});
	}

}
