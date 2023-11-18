package com.ex4;

import akka.actor.AbstractActor;
import akka.actor.AbstractActorWithStash;
import akka.actor.Props;
import com.ex3.ActorServer;

import java.util.ArrayList;

public class ActorServer4 extends AbstractActorWithStash {
    @Override
    public Receive createReceive() {
        return awake();
    }

    private final Receive awake() {
        return receiveBuilder().match(TextMsg.class, this::echo).match(Sleep.class, this::goToSleep).build();
    }

    private final Receive sleepy() {
        return receiveBuilder().match(TextMsg.class, this::putAside).match(Wakeup.class, this::wakeup).build();
    }

    // Processing messages
    void echo(TextMsg msg) {
        System.out.println("SERVER: Echo-ing back msg to "+msg.getSender()+" with text: "+msg.getText());
        msg.getSender().tell(msg, self());
        // FUNZIONA ANCHE CON sender().tell(msg, self());
    }

    void putAside(TextMsg msg) {
        System.out.println("SERVER: Setting aside msg...");
        // ONGI VOLTA CHE RICEVO UN MESSAGGIO LO METTO IN STASH
        stash();
    }

    // Changes of behavior
    void goToSleep (Sleep msg) {
        System.out.println("SERVER: Going to sleep... ");
        getContext().become(sleepy());
    }

    void wakeup (Wakeup msg) {
        System.out.println("SERVER: Waking up!");
        getContext().become(awake());
        unstashAll();
    }

    static Props props() {
        return Props.create(ActorServer4.class);
    }
}
