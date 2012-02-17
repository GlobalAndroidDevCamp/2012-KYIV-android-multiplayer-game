package com.frosix.protocol.adt.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.anddev.andengine.input.touch.TouchEvent;

import com.frosix.ConstantStorage;

public class TouchControlMessage extends CommonMessage {

	public float x; 
	public float y; 
	public int action;
	
	public void set( float x , float y , int action){
		this.x = x;
		this.y = y;
		this.action = action;
	}
	
	@Override
	public short getFlag() {
		return ConstantStorage.FLAG_MESSAGE_TOUCH_CONTROL;
	}
	
	@Override
	protected void onReadTransmissionData(DataInputStream pDataInputStream)
			throws IOException {
	this.x = pDataInputStream.readFloat();
	this.y = pDataInputStream.readFloat();
	this.action =pDataInputStream.readInt();
	
	}

	@Override
	protected void onWriteTransmissionData(DataOutputStream pDataOutputStream)
			throws IOException {
		pDataOutputStream.writeFloat(x);
		pDataOutputStream.writeFloat(y);
		pDataOutputStream.writeInt(action);
	}

}
