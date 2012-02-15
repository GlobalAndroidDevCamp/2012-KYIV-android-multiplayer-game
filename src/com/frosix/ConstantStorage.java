package com.frosix;



public interface ConstantStorage {
	
	public static final String MY_UUID = "6D2DF50E-06EF-C21C-7DB0-345099A5F64E";
	public static final String DEBUGTAG = "multiplayer-game";
	
	public static final short FLAG_MESSAGE_SERVER_CONNECTION_CLOSE = Short.MIN_VALUE;
	public static final short FLAG_MESSAGE_COMMON_MOVE_SPRITE = FLAG_MESSAGE_SERVER_CONNECTION_CLOSE + 1;
	//public static final short FLAG_MESSAGE_MOVE_SPRITE = FLAG_MESSAGE_SERVER_MOVE_SPRITE + 1;
	
	public static final int DIALOG_CHOOSE_SERVER_OR_CLIENT_ID = 0;
	public static final int DIALOG_SHOW_SERVER_IP_ID = DIALOG_CHOOSE_SERVER_OR_CLIENT_ID + 1;
	
	public static final int REQUESTCODE_BLUETOOTH_ENABLE = 0;
	public static final int REQUESTCODE_BLUETOOTH_CONNECT = REQUESTCODE_BLUETOOTH_ENABLE + 1;
	
}
