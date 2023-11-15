package com.counter;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class CounterActor extends AbstractActor {

	private int counter;

	public CounterActor() {
		this.counter = 0;
	}

	@Override
	public Receive createReceive() {
		// COSTRUZIONE DEL MATCH
		return receiveBuilder().match(SimpleMessage.class, this::onMessage).
				match(OtherMessage.class, this::onOtherMessage).build();
	}

	void onMessage(SimpleMessage msg) {
		++counter;
		System.out.println("Counter increased to " + counter);
	}

	void onOtherMessage(OtherMessage msg){
		System.out.println("Received other type of message!");
	}

	static Props props() {
		return Props.create(CounterActor.class);
	}

}
