package com.ex4;

import akka.actor.ActorRef;

public class TextMsg extends Msg {

    private String text;
    private ActorRef sender;

    public TextMsg (String text, ActorRef sender) {
        this.text = text;
        this.sender = sender;
    }

    public TextMsg (String text) {
        this.text = text;
        this.sender = null;
    }

    public String getText() {
        return text;
    }

    public ActorRef getSender() {
        return sender;
    }

    public void setSender(ActorRef sender) {
        this.sender = sender;
    }
}
