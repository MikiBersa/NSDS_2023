package com.counter;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.faultTolerance.counter.DataMessage;

public class CounterActor extends AbstractActor {

	private int counter;

	public CounterActor() {
		this.counter = 0;
	}
    //specify a single match between SimpleMessage and the method onMessage
	@Override
	public Receive createReceive() {
		return	 receiveBuilder().match(DataMessage.class, this::onMessage).match(OtherMessage.class, this::onOtherMessage).match(SimpleMessage.class,this::onSimpleMessage).build();
	}

	void onOtherMessage(OtherMessage p){
		--counter;
		System.out.println("Counter decreased to " + counter);
	}
	void onSimpleMessage(SimpleMessage msg){
		++counter;
		System.out.println("Counter increased to " + counter);
	}

	void onMessage(DataMessage msg) {
		if(msg.getCode()==1){
			++counter;
			System.out.println("Counter increased to " + counter);
		}
		else{
			--counter;
			System.out.println("Counter decreased to " + counter);
		}
	}

	static Props props() {
		return Props.create(CounterActor.class);
	}

}
