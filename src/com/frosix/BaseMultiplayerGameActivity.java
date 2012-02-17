package com.frosix;

import java.io.IOException;
import java.util.Map;

import org.anddev.andengine.extension.multiplayer.protocol.adt.message.IMessage;
import org.anddev.andengine.extension.multiplayer.protocol.shared.BluetoothSocketConnection;
import org.anddev.andengine.extension.multiplayer.protocol.shared.Connector;
import org.anddev.andengine.extension.multiplayer.protocol.shared.Connector.IConnectorListener;
import org.anddev.andengine.extension.multiplayer.protocol.shared.IMessageHandler;
import org.anddev.andengine.extension.multiplayer.protocol.util.MessagePool;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import org.anddev.andengine.util.Debug;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import com.frosix.protocol.adt.message.ConnectionCloseCommonMessage;
import com.frosix.protocol.adt.message.ICommonMessage;
import com.frosix.util.BluetoothListDevicesActivity;

public abstract class BaseMultiplayerGameActivity extends BaseGameActivity
		implements
		IMessageHandler<BluetoothSocketConnection, Connector<BluetoothSocketConnection>, ICommonMessage>,
		ConstantStorage, IConnectorListener<Connector<?>> {

	private BluetoothAdapter mBluetoothAdapter;
	private final MessagePool<IMessage> mMessagePool = new MessagePool<IMessage>();
	protected BluetoothDelegate bluetoothDelegate;

	@Override
	protected void onCreate(Bundle pSavedInstanceState) {
		super.onCreate(pSavedInstanceState);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available!",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		} else {
			if (mBluetoothAdapter.isEnabled()) {
				showDialog(DIALOG_CHOOSE_SERVER_OR_CLIENT_ID);
			} else {
				final Intent enableIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableIntent,
						REQUESTCODE_BLUETOOTH_ENABLE);
			}
		}
	}

	protected abstract Map<Short, Class<? extends ICommonMessage>> getMessageMap();

	private void initMessagePool(
			Map<Short, Class<? extends ICommonMessage>> messageMap) {
		mMessagePool.registerMessage(FLAG_MESSAGE_COMMON_CONNECTION_CLOSE,
				ConnectionCloseCommonMessage.class);
		if (messageMap != null) {
			for (Map.Entry<Short, Class<? extends ICommonMessage>> messageEntry : messageMap
					.entrySet()) {
				mMessagePool.registerMessage(messageEntry.getKey(),
						messageEntry.getValue());
			}
		}
	}

	@Override
	protected Dialog onCreateDialog(final int pID) {
		switch (pID) {
		case DIALOG_SHOW_SERVER_IP_ID:
			return new AlertDialog.Builder(this)
					.setIcon(android.R.drawable.ic_dialog_info)
					.setTitle("Server-Details")
					.setCancelable(false)
					.setMessage(
							"The Name of your Server is:\n"
									+ mBluetoothAdapter.getName() + "\n"
									+ "The MACAddress of your Server is:\n"
									+ mBluetoothAdapter.getAddress())
					.setPositiveButton(android.R.string.ok, null).create();
		case DIALOG_CHOOSE_SERVER_OR_CLIENT_ID:
			return new AlertDialog.Builder(this)
					.setIcon(android.R.drawable.ic_dialog_info)
					.setTitle("Be Server or Client ...").setCancelable(false)
					.setPositiveButton("Client", new OnClickListener() {
						@Override
						public void onClick(final DialogInterface pDialog,
								final int pWhich) {
							final Intent intent = new Intent(
									BaseMultiplayerGameActivity.this,
									BluetoothListDevicesActivity.class);
							BaseMultiplayerGameActivity.this
									.startActivityForResult(intent,
											REQUESTCODE_BLUETOOTH_CONNECT);
						}
					}).setNegativeButton("Server", new OnClickListener() {
						@Override
						public void onClick(final DialogInterface pDialog,
								final int pWhich) {
							BaseMultiplayerGameActivity activity = BaseMultiplayerGameActivity.this;
							activity.toast("You can add and move sprites, which are only shown on the clients.");
							setBluetoothDelegate(new ServerBluetoothDelegate(
									activity));
							activity.showDialog(DIALOG_SHOW_SERVER_IP_ID);
						}
					}).create();
		default:
			return super.onCreateDialog(pID);
		}
	}

	private void setMessageHandler(
			Map<Short, Class<? extends ICommonMessage>> messageMap) {
		bluetoothDelegate.registerMessage(FLAG_MESSAGE_COMMON_CONNECTION_CLOSE,
				ConnectionCloseCommonMessage.class, this);
		if (messageMap != null) {
			for (Map.Entry<Short, Class<? extends ICommonMessage>> messageEntry : messageMap
					.entrySet()) {
				bluetoothDelegate.registerMessage(messageEntry.getKey(),
						messageEntry.getValue(), this);
			}
		}
	}

	private void setBluetoothDelegate(BluetoothDelegate bluetoothDelegate) {
		this.bluetoothDelegate = bluetoothDelegate;
		Map<Short, Class<? extends ICommonMessage>> messageMap = getMessageMap();
		initMessagePool(messageMap);
		setMessageHandler(messageMap);
		this.bluetoothDelegate.init();
	}

	@Override
	protected void onDestroy() {
		bluetoothDelegate.onDestroy();
		super.onDestroy();
	}

	@Override
	public boolean onKeyUp(final int pKeyCode, final KeyEvent pEvent) {
		switch (pKeyCode) {
		case KeyEvent.KEYCODE_BACK:
			this.finish();
			return true;
		}
		return super.onKeyUp(pKeyCode, pEvent);
	}

	@Override
	protected void onActivityResult(final int pRequestCode,
			final int pResultCode, final Intent pData) {
		switch (pRequestCode) {
		case REQUESTCODE_BLUETOOTH_ENABLE:
			showDialog(DIALOG_CHOOSE_SERVER_OR_CLIENT_ID);
			break;
		case REQUESTCODE_BLUETOOTH_CONNECT:
			String mServerMACAddress = pData.getExtras().getString(
					BluetoothListDevicesActivity.EXTRA_DEVICE_ADDRESS);
			setBluetoothDelegate(new ClientBluetoothDelegate(mServerMACAddress,
					this));
			break;
		default:
			super.onActivityResult(pRequestCode, pResultCode, pData);
		}
	}

	private void log(final String pMessage) {
		Debug.d(pMessage);
	}

	private void toast(final String pMessage) {
		log(pMessage);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(BaseMultiplayerGameActivity.this, pMessage,
						Toast.LENGTH_SHORT).show();
			}
		});
	}

	protected IMessage getMessage(short flag) {
		return mMessagePool.obtainMessage(flag);
	}

	protected void sendMessage(ICommonMessage message) {
		bluetoothDelegate.sendMessage(message);
		mMessagePool.recycleMessage(message);
	}

	@Override
	public void onStarted(Connector<?> pConnector) {
		toast("Connected");
	}

	@Override
	public void onTerminated(Connector<?> pConnector) {
		toast("Disconnected");
		finish();
	}

	@Override
	public void onHandleMessage(
			Connector<BluetoothSocketConnection> pConnector,
			ICommonMessage pMessage) throws IOException {
		if (pMessage instanceof ConnectionCloseCommonMessage) {
			finish();
		}
	}

}