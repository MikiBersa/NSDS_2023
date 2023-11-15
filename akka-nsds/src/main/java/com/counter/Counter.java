package com.counter;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.ex2.CounterActorStas;
import com.ex3.ActorClient;
import com.ex3.ActorServer;
import com.ex3.Putmsg;
import com.ex3.StartConnection;
import com.ex4.ActorClient4;
import com.ex4.ActorServer4;
import com.ex4.GenericStringMex;
import com.faultTolerance.counter.DataMessage;

public class Counter {

	private static final int numThreads = 10;
	private static final int numMessages = 100;

	public static void main(String[] args) {
        // initializes and creates the system and the actor
		final ActorSystem sys = ActorSystem.create("System");
		final ActorRef server = sys.actorOf(ActorServer4.props(), "server");
		final ActorRef client = sys.actorOf(ActorClient4.props(), "client");
		// Send messages from multiple threads in parallel
		final ExecutorService exec = Executors.newFixedThreadPool(numThreads);

		/*for (int i = 0; i < numMessages; i++) {
			//messages come from an anonimous sender
			exec.submit(() -> counter.tell(new DataMessage(1), ActorRef.noSender()));
			exec.submit(() -> counter.tell(new DataMessage(2), ActorRef.noSender()));
			exec.submit(() -> counter.tell(new SimpleMessage(), ActorRef.noSender()));
		}
		counter.tell(new DataMessage(2),ActorRef.noSender());
		counter.tell(new DataMessage(2),ActorRef.noSender());
		counter.tell(new DataMessage(1),ActorRef.noSender());
		counter.tell(new DataMessage(2),ActorRef.noSender());
		counter.tell(new DataMessage(1),ActorRef.noSender());
		counter.tell(new DataMessage(1),ActorRef.noSender());
		*/
        client.tell(new StartConnection(server),ActorRef.noSender());
		// Wait for all messages to be sent and received

		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		exec.shutdown();
		sys.terminate();

	}

}
