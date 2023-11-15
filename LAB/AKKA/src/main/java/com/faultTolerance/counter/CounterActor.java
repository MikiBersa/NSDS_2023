package com.faultTolerance.counter;

import java.util.Optional;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class CounterActor extends AbstractActor {

	private int counter;

	public CounterActor() {
		this.counter = 0;
	}

	@Override
	public Receive createReceive() {
		// IN QUESTO CASO SOLO UN TIPO PERCHè ABBIAMO SOLO DATAMESSAGE COME CLASSE DI TIPO MESSAGGIO
		return receiveBuilder().match(DataMessage.class, this::onMessage).build();
	}

	void onMessage(DataMessage msg) throws Exception {
		if (msg.getCode() == CounterSupervisor.NORMAL_OP) {
			System.out.println("I am executing a NORMAL operation...counter is now " + (++counter));
		} else if (msg.getCode() == CounterSupervisor.FAULT_OP) {
			System.out.println("I am emulating a FAULT!");		
			throw new Exception("Actor fault!");
		}
	}

	// SONO METODI CHE GESTISCONO PRIMA E DOPO IL RESTART DA PARTE DEL SUPERVISOR CHE VEDENDO CHE GLI HO INVIATO UN ERRORE (FAULT)
	// MI HA FATTO IL RESTART -> IN QUESTO CASO FULAT ACTOR1 CREA UN NUOVO ACTOR2 CHE PARTE DA ZERO
	// POSSO FARE IL RESUME
	@Override
	public void preRestart(Throwable reason, Optional<Object> message) {
		System.out.print("Preparing to restart...");		
	}
	
	@Override
	public void postRestart(Throwable reason) {
		System.out.println("...now restarted!");	
	}
	
	static Props props() {
		return Props.create(CounterActor.class);
	}

}
