package com.EVAL;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.concurrent.ThreadLocalRandom;

public class TemperatureSensorActor extends AbstractActor {

	private ActorRef dispatcher;
	private final static int MIN_TEMP = 0;
	private final static int MAX_TEMP = 50;

	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(GenerateMsg.class, this::onGenerate)
				.match(ConfigSensor.class, this::onConfig)
				.build();
	}

	private void onConfig(ConfigSensor msg) {
		dispatcher = msg.getDispatcher();
	}

	private void onGenerate(GenerateMsg msg) {
		int temp = ThreadLocalRandom.current().nextInt(MIN_TEMP, MAX_TEMP + 1);
		dispatcher.tell(new TemperatureMsg(temp,self()), self());
	}

	static Props props() {
		return Props.create(TemperatureSensorActor.class);
	}

}
