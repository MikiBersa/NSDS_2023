package com.ex5;

import akka.actor.ActorRef;

public class StartConnection {
    ActorRef server;
    public StartConnection(ActorRef server){
        this.server=server;
    }
    public StartConnection(){

    }
    public ActorRef getServer() {
        return server;
    }
}
