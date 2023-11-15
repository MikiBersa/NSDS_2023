package com.faultTolerance.counter;
//a message that has a parameter that define the type of action that has to be taken or we can have different message types
public class DataMessage {

	private int code;
	
	public int getCode() {
		return code;
	}

	public DataMessage(int code) {
		this.code = code;
	}
	
}
