package com.frosix.protocol.adt.message;

import org.anddev.andengine.extension.multiplayer.protocol.adt.message.Message;

public abstract class AbstractCommonMessage extends Message implements ICommonMessage {
	
	private short flag;

	public AbstractCommonMessage(short flag) {
		this.flag = flag;
	}

	@Override
	public short getFlag() {
		return flag;
	}

}
