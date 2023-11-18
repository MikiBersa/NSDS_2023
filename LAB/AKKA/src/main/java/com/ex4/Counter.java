package com.ex4;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.ex3.StartConnection;

public class Counter {

    private static final int numThreads = 10;
    private static final int numMessages = 100;

    public static void main(String[] args) {


        // initializes and creates the system and the actor
        final ActorSystem sys = ActorSystem.create("System");
        final ActorRef server = sys.actorOf(ActorServer4.props(), "server");
        final ActorRef client = sys.actorOf(ActorClient4.props(), "client");

        // Tell the client who is the server
        client.tell(new StartConnection(server), ActorRef.noSender());

        // An example execution
        client.tell(new TextMsg("Hello Luca!", ActorRef.noSender()), ActorRef.noSender());
        client.tell(new TextMsg("Hello Alessandro!", ActorRef.noSender()), ActorRef.noSender());

        client.tell(new Sleep(), ActorRef.noSender());

        client.tell(new TextMsg("You should be sleeping now 1!", ActorRef.noSender()), ActorRef.noSender());
        client.tell(new TextMsg("You should be sleeping now 2!", ActorRef.noSender()), ActorRef.noSender());

        client.tell(new Wakeup(), ActorRef.noSender());

        // Wait for messages to eventually arrive
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        sys.terminate();
    }
}
