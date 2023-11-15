package com.ex2;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.ex3.StartConnection;
import com.ex4.ActorClient4;
import com.ex4.ActorServer4;
import com.faultTolerance.counter.DataMessage;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Counter {

    private static final int numThreads = 10;
    private static final int numMessages = 100;

    public static void main(String[] args) {


        // initializes and creates the system and the actor
        final ActorSystem sys = ActorSystem.create("System");
        final ActorRef counter = sys.actorOf(CounterActorStas.props(), "counter");

		/*for (int i = 0; i < numMessages; i++) {
			//messages come from an anonimous sender
			exec.submit(() -> counter.tell(new DataMessage(1), ActorRef.noSender()));
			exec.submit(() -> counter.tell(new DataMessage(2), ActorRef.noSender()));
			exec.submit(() -> counter.tell(new SimpleMessage(), ActorRef.noSender()));
		}*/

		counter.tell(new DataMessage(2),ActorRef.noSender());
		counter.tell(new DataMessage(2),ActorRef.noSender());
		counter.tell(new DataMessage(1),ActorRef.noSender());
		counter.tell(new DataMessage(2),ActorRef.noSender());
		counter.tell(new DataMessage(1),ActorRef.noSender());
		counter.tell(new DataMessage(1),ActorRef.noSender());

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        sys.terminate();
    }
}
