package com.frosix;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.anddev.andengine.extension.multiplayer.protocol.adt.message.server.ServerMessage;

public class MoveSpriteMessage extends ServerMessage{

	private int mID;
	private float mX;
	private float mY;
	
	public MoveSpriteMessage() {

	}

	public MoveSpriteMessage(final int pID, final float pX, final float pY) {
		this.mID = pID;
		this.mX = pX;
		this.mY = pY;
	}

	public void set(final int pID, final float pX, final float pY) {
		this.mID = pID;
		this.mX = pX;
		this.mY = pY;
	}
	@Override
	public short getFlag() {
		return ConstantStorage.FLAG_MESSAGE_SERVER_MOVE_SPRITE;
	}

	@Override
	protected void onReadTransmissionData(DataInputStream pDataInputStream)
			throws IOException {
		this.mID = pDataInputStream.readInt();
		this.mX = pDataInputStream.readFloat();
		this.mY = pDataInputStream.readFloat();
		
	}

	@Override
	protected void onWriteTransmissionData(DataOutputStream pDataOutputStream)
			throws IOException {
		pDataOutputStream.writeInt(this.mID);
		pDataOutputStream.writeFloat(this.mX);
		pDataOutputStream.writeFloat(this.mY);
		
	}

}
