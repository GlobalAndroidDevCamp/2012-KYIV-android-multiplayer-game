package com.frosix;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.anddev.andengine.extension.multiplayer.protocol.adt.message.client.ClientMessage;

public class MoveSpriteClientMessage extends ClientMessage {
	
	private MoveSpriteMessageDelegate delegate;
	
	public MoveSpriteClientMessage() {
		delegate = new MoveSpriteMessageDelegate();
	}

	public MoveSpriteClientMessage(final int pID, final float pX, final float pY) {
		delegate = new MoveSpriteMessageDelegate(pID, pX, pY);
	}

	public void set(final int pID, final float pX, final float pY) {
		delegate.set(pID, pX, pY);
	}
	@Override
	public short getFlag() {
		return delegate.getFlag();
	}

	@Override
	protected void onReadTransmissionData(DataInputStream pDataInputStream)
			throws IOException {
		delegate.onReadTransmissionData(pDataInputStream);
		
	}

	@Override
	protected void onWriteTransmissionData(DataOutputStream pDataOutputStream)
			throws IOException {
		delegate.onWriteTransmissionData(pDataOutputStream);
		
	}
	
	public int getID() {
		return delegate.getID();
	}

	public float getX() {
		return delegate.getX();
	}

	public float getY() {
		return delegate.getY();
	}

}
