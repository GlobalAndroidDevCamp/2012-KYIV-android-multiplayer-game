package com.frosix;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class MoveSpriteMessageDelegate {
	
	public int mID;
	public float mX;
	public float mY;
	
	public MoveSpriteMessageDelegate() {

	}

	public MoveSpriteMessageDelegate(final int pID, final float pX, final float pY) {
		this.mID = pID;
		this.mX = pX;
		this.mY = pY;
	}

	public void set(final int pID, final float pX, final float pY) {
		this.mID = pID;
		this.mX = pX;
		this.mY = pY;
	}

	protected void onReadTransmissionData(DataInputStream pDataInputStream)
			throws IOException {
		this.mID = pDataInputStream.readInt();
		this.mX = pDataInputStream.readFloat();
		this.mY = pDataInputStream.readFloat();
		
	}

	protected void onWriteTransmissionData(DataOutputStream pDataOutputStream)
			throws IOException {
		pDataOutputStream.writeInt(this.mID);
		pDataOutputStream.writeFloat(this.mX);
		pDataOutputStream.writeFloat(this.mY);
		
	}

	public int getID() {
		return mID;
	}

	public float getX() {
		return mX;
	}

	public float getY() {
		return mY;
	}
	
}
