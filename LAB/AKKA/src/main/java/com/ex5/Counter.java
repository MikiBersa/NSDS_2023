package com.ex5;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Counter {

    private static final int numThreads = 10;
    private static final int numMessages = 100;

    public static void main(String[] args) {
        scala.concurrent.duration.Duration timeout = scala.concurrent.duration.Duration.create(5, SECONDS);

        // initializes and creates the system and the actor
        final ActorSystem sys = ActorSystem.create("System");
        // QUI CREO IL SUPERVISORE
        final ActorRef supervisor = sys.actorOf(Ex5SupervisorActor.props(), "supervisor");

        ActorRef server, client;

        // MANDA LA RICHEISTA AL SUPERVISORE DI CREARE SIA IL SERVER CHE IL CLIENT
        scala.concurrent.Future<Object> waitingForServer = ask(supervisor, Props.create(ActorServer5.class), 5000);
        scala.concurrent.Future<Object> waitingForClient = ask(supervisor, Props.create(ActorClient5.class), 5000);

        try {
            server = (ActorRef) waitingForServer.result(timeout, null);
            client = (ActorRef) waitingForClient.result(timeout, null);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Tell the client who is the server
        client.tell(new StartConnection(server), ActorRef.noSender());

        // An example execution
        client.tell(new PutMsg5("Luca","luca.mottola@polimi.it"), ActorRef.noSender());
        client.tell(new PutMsg5("Alessandro","alessandro.margara@polimi.it"), ActorRef.noSender());

        client.tell(new GetMsg5("Alessandro"), ActorRef.noSender());
        client.tell(new GetMsg5("Gianpaolo"), ActorRef.noSender());

        // Now make the server fail
        client.tell(new PutMsg5("Fail!","none"), ActorRef.noSender());

        // If resuming, this should return luca.mottola@polimi.it
        client.tell(new GetMsg5("Luca"), ActorRef.noSender());


        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        sys.terminate();
    }
}
