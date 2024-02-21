package com.EVAL;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class SensorProcessorActor extends AbstractActor {

	private double currentAverage;
	private int numTemp = 0;
	@Override
	public Receive createReceive() {
		return receiveBuilder().match(TemperatureMsg.class, this::gotData).build();
	}

	private void gotData(TemperatureMsg msg) throws Exception {

		System.out.println("SENSOR PROCESSOR " + self() + ": Got data from " + msg.getSender());

		// SE RICEVO UN VALORE NEGAIVO MANDO ECCEZIONE
		if(msg.getTemperature() < 0){
			throw new Exception("Processor fault for negative temperature!");
		}else{
			// CALCOLARE LA MEDIA
			this.numTemp++;
			this.currentAverage = (this.currentAverage * (this.numTemp - 1) + msg.getTemperature()) / this.numTemp;
			System.out.println("SENSOR PROCESSOR " + self() + ": Current avg is " + currentAverage);
		}
	}

	static Props props() {
		return Props.create(SensorProcessorActor.class);
	}

	public SensorProcessorActor() {
	}
}
