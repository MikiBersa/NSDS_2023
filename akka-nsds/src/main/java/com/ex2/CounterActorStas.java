package com.ex2;

import akka.actor.AbstractActor;
import akka.actor.AbstractActorWithStash;
import akka.actor.Props;
import com.counter.CounterActor;
import com.counter.OtherMessage;
import com.counter.SimpleMessage;
import com.faultTolerance.counter.DataMessage;

public class CounterActorStas extends AbstractActorWithStash {
    private int counter;

    public CounterActorStas() {
        this.counter = 0;
    }
    //specify a single match between SimpleMessage and the method onMessage
    @Override

    public Receive createReceive() {
        return	 receiveBuilder().match(DataMessage.class, this::onMessage).match(OtherMessage.class, this::onOtherMessage).match(SimpleMessage.class,this::onSimpleMessage).build();
    }

    void onOtherMessage(OtherMessage p){
        if(counter>0){
            --counter;
            System.out.println("Counter decreased to " + counter);
        }
        else{
            System.out.println("Cannot decrease");
            stash();
        }
    }
    void onSimpleMessage(SimpleMessage msg){
        ++counter;
        System.out.println("Counter increased to " + counter);
        unstash();
    }

    void onMessage(DataMessage msg) {
        if(msg.getCode()==1){
            ++counter;
            System.out.println("Counter increased to " + counter);
            unstash();
        }
        else{
            if(counter>0){
                --counter;
                System.out.println("Counter decreased to " + counter);
            }
            else{
                System.out.println("Cannot decrease");
                stash();
            }
        }
    }

    public static Props props() {
        return Props.create(CounterActorStas.class);
    }
}
