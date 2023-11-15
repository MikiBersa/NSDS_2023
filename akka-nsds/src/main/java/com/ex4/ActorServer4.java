package com.ex4;

import akka.actor.AbstractActor;
import akka.actor.AbstractActorWithStash;
import akka.actor.Props;
import com.ex3.ActorServer;

import java.util.ArrayList;

public class ActorServer4 extends AbstractActorWithStash {
    boolean sleep;
    @Override
    /*public Receive createReceive() {
        return receiveBuilder().match(GenericStringMex.class,this::onStringReceived).build();
    }

    void onStringReceived(GenericStringMex s){
        if(sleep){
            stash();
        }
        else{
            unstash();
            sender().tell(s.getS(),self());
        }
    }
*/
    public Receive createReceive() {
        return wakeup();
    }

    private final Receive wakeup() {
        return receiveBuilder().match(GenericStringMex.class,this::onStringReceived).match(Sleep.class,this::onSleep).build();
    }

    void onWakeup(Wakeup p) {
        getContext().become(wakeup());
        unstashAll();
    }

    private final Receive sleep() {
        return receiveBuilder().match(GenericStringMex.class,this::onStringReceivedSleep).match(Wakeup.class,this::onWakeup).build();
    }

    void onSleep(Sleep p) {
        getContext().become(sleep());
    }

    void onStringReceivedSleep(GenericStringMex p){
        stash();
    }

    void onStringReceived(GenericStringMex p) {
        sender().tell(p.getS(),self());
    }


    public static Props props() {
        return Props.create(ActorServer4.class);
    }
}
