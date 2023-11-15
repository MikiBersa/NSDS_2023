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

        client.tell(new StartConnection(server), ActorRef.noSender());

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        sys.terminate();
    }
}
