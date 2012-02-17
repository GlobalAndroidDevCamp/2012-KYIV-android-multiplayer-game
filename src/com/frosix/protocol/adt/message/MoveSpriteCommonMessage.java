package com.frosix.protocol.adt.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.anddev.andengine.extension.multiplayer.protocol.adt.message.Message;

import com.frosix.ConstantStorage;

public class MoveSpriteCommonMessage extends CommonMessage {
	public int mID;
	public float mX;
	public float mY;

	public MoveSpriteCommonMessage() {
		
	}

	public MoveSpriteCommonMessage(final int pID, final float pX, final float pY) {
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
		return ConstantStorage.FLAG_MESSAGE_COMMON_MOVE_SPRITE;
	}

	@Override
	protected void onReadTransmissionData(final DataInputStream pDataInputStream) throws IOException {
		this.mID = pDataInputStream.readInt();
		this.mX = pDataInputStream.readFloat();
		this.mY = pDataInputStream.readFloat();
	}

	@Override
	protected void onWriteTransmissionData(final DataOutputStream pDataOutputStream) throws IOException {
		pDataOutputStream.writeInt(this.mID);
		pDataOutputStream.writeFloat(this.mX);
		pDataOutputStream.writeFloat(this.mY);
	}
}
