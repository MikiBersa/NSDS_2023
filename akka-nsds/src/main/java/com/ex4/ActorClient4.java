package com.ex4;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.ex3.ActorClient;
import com.ex3.StartConnection;

public class ActorClient4 extends AbstractActor {
    private ActorRef server;
    @Override
    public Receive createReceive() {
        return receiveBuilder().match(String.class,this::onReplyReceived).match(StartConnection.class,this::onStartConnection).build();
    }

    void onStartConnection(StartConnection p) {
        this.server=p.getServer();
        server.tell(new GenericStringMex("ciao"),self());
        server.tell(new Sleep(),self());
        server.tell(new GenericStringMex("come va"),self());
        server.tell(new Wakeup(),self());
        server.tell(new GenericStringMex("bene"),self());
    }

    void onReplyReceived(String p) {
        System.out.println(p);
    }


    public static Props props() {
        return Props.create(ActorClient4.class);
    }
}
