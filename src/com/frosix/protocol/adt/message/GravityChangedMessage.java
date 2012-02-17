package com.frosix.protocol.adt.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.badlogic.gdx.math.Vector2;
import com.frosix.ConstantStorage;

public class GravityChangedMessage extends CommonMessage {

	private Vector2 gravityVector;
	
	public void set(Vector2 pVector){
		gravityVector = pVector;
	}
	
	public Vector2 getGravity(){
		return this.gravityVector;
	}
	
	@Override
	public short getFlag() {
		return ConstantStorage.FLAG_MESSAGE_GRAVITY_CHANGED;
	}

	@Override
	protected void onReadTransmissionData(DataInputStream pDataInputStream)
			throws IOException {
		this.gravityVector.x = pDataInputStream.readFloat();
		this.gravityVector.y = pDataInputStream.readFloat();
	}

	@Override
	protected void onWriteTransmissionData(DataOutputStream pDataOutputStream)
			throws IOException {
		pDataOutputStream.writeFloat(gravityVector.x);
		pDataOutputStream.writeFloat(gravityVector.y);
	}

}
