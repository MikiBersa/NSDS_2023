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
        // QUI IDEALE SAREBBE, MEGLIO METTERE I MESSAGGI DI RITORNO -> CREANDO UNA CLASSE DEDICATA
        ReplyMsg replyMsg = new ReplyMsg(emailstorage.get(p.getName()));
        sender().tell(replyMsg,self());
    }

    void onPutmsg(Putmsg p){
        emailstorage.put(p.getName(),p.getEmail());
        System.out.println("Done!");
    }

    public static Props props() {
        return Props.create(ActorServer.class);
    }
}
