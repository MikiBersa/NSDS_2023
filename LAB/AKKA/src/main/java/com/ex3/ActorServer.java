package com.ex3;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.counter.CounterActor;

import java.util.HashMap;

public class ActorServer extends AbstractActor {
    HashMap<String,String>  emailstorage=new HashMap<>();
    @Override
    public Receive createReceive() {
        return receiveBuilder().match(Putmsg.class,this::onPutmsg).match(Getmsg.class,this::onGetmsg).build();
    }
    void onGetmsg(Getmsg p){
        // System.out.println("Email of: "+p.getName()+" is :"+emailstorage.get(p.getName()));
         sender().tell(emailstorage.get(p.getName()),self());
    }

    void onPutmsg(Putmsg p){
        emailstorage.put(p.getName(),p.getEmail());
        System.out.println("Done!");
    }

    public static Props props() {
        return Props.create(ActorServer.class);
    }
}
