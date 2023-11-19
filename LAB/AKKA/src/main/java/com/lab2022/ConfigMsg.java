package com.lab2022;

import akka.actor.ActorRef;

public class ConfigMsg {

    private ActorRef broker;

    public ConfigMsg (ActorRef broker) {
        this.broker = broker;
    }

    public ActorRef getBrokerRef() {
        return broker;
    }
}
