package com.frosix;



public interface ConstantStorage {
	
	public static final String MY_UUID = "6D2DF50E-06EF-C21C-7DB0-345099A5F64E";
	public static final String DEBUGTAG = "multiplayer-game";
	
	public static final short FLAG_MESSAGE_COMMON_CONNECTION_CLOSE = Short.MIN_VALUE;
	public static final short FLAG_MESSAGE_COMMON_MOVE_SPRITE = FLAG_MESSAGE_COMMON_CONNECTION_CLOSE + 1;
	public static final short FLAG_MESSAGE_COMMON_MOVE_PLATFORM = FLAG_MESSAGE_COMMON_MOVE_SPRITE+1;
	public static final short FLAG_MESSAGE_SYNCHRONIZING = FLAG_MESSAGE_COMMON_MOVE_PLATFORM+1;
	public static final short FLAG_MESSAGE_TOUCH_CONTROL = FLAG_MESSAGE_SYNCHRONIZING+1;
	public static final short FLAG_MESSAGE_GRAVITY_CHANGED = FLAG_MESSAGE_TOUCH_CONTROL+1;
	public static final short FLAG_MESSAGE_START = FLAG_MESSAGE_GRAVITY_CHANGED+1;
	
	public static final int DIALOG_CHOOSE_SERVER_OR_CLIENT_ID = 0;
	public static final int DIALOG_SHOW_SERVER_IP_ID = DIALOG_CHOOSE_SERVER_OR_CLIENT_ID + 1;
	
	public static final int REQUESTCODE_BLUETOOTH_ENABLE = 0;
	public static final int REQUESTCODE_BLUETOOTH_CONNECT = REQUESTCODE_BLUETOOTH_ENABLE + 1;
	
}
