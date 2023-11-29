package com.EVAL;

import akka.actor.ActorRef;

public class ConfigSensor {

    private ActorRef dispatcher;

    public ConfigSensor(ActorRef dispatcher){
        this.dispatcher = dispatcher;
    }


    public ActorRef getDispatcher() {
        return dispatcher;
    }
}
