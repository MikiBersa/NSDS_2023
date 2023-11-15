package com.ex3;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class ActorClient extends AbstractActor {
    private ActorRef server;
    @Override
    public Receive createReceive() {
        return receiveBuilder().match(String.class,this::onReplyReceived).match(StartConnection.class,this::onStartConnection).build();
    }

    private void onStartConnection(StartConnection start) {
        this.server=start.getServer();
        sendput("giovanni","xxxxx");
        sendput("carlo","yyyy");
        sendget("giovanni");
    }

    void onReplyReceived(String p) {
        System.out.println("The email is:"+p);
    }
    public void sendput(String name, String email){
           server.tell(new Putmsg(email,name),self());
    }
    public void sendget(String name){
        server.tell(new Getmsg(name),self());
    }
    public static Props props() {
        return Props.create(ActorClient.class);
    }
}
