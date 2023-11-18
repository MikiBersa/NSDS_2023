package com.ex5;

import akka.actor.AbstractActor;
import akka.actor.Props;

import java.util.HashMap;

public class ActorServer5 extends AbstractActor {
    HashMap<String,String>  emailstorage=new HashMap<>();
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(PutMsg5.class,this::onPutmsg)
                .match(GetMsg5.class,this::onGetmsg)
                .build();
    }
    void onGetmsg(GetMsg5 p){
            ReplyMsg replyMsg = new ReplyMsg(emailstorage.get(p.getName()));
            // SARA' PRESO DAL CLIENT CON L'ASK
            sender().tell(replyMsg, self());

    }

    void onPutmsg(PutMsg5 p) throws Exception{
        if(p.getName() == "Fail!"){
            // LANCIARE ECCEZIONE
            System.out.println("VADO IN FAIL");
            throw new Exception("Server fault!");
        }else {
            emailstorage.put(p.getName(), p.getEmail());
            System.out.println("Done!");
        }
    }

    public static Props props() {
        return Props.create(ActorServer5.class);
    }
}
