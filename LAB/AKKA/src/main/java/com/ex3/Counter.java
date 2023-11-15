package com.ex3;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.ex4.ActorClient4;
import com.ex4.ActorServer4;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Counter {

    private static final int numThreads = 10;
    private static final int numMessages = 100;

    public static void main(String[] args) {


        // initializes and creates the system and the actor
        final ActorSystem sys = ActorSystem.create("System");
        final ActorRef server = sys.actorOf(ActorServer.props(), "server");
        final ActorRef client = sys.actorOf(ActorClient.props(), "client");


        client.tell(new StartConnection(server), ActorRef.noSender());
        // Wait for all messages to be sent and received

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        sys.terminate();
    }
}
