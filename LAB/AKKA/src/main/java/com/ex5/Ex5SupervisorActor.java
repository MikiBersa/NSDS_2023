package com.ex5;

import akka.actor.AbstractActor;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.japi.pf.DeciderBuilder;

import java.time.Duration;

public class Ex5SupervisorActor extends AbstractActor {

    // #strategy
    private static SupervisorStrategy strategy =
            new OneForOneStrategy(
                    1, // Max no of retries
                    Duration.ofMinutes(1), // Within what time period
                    // SupervisorStrategy.resume() -> PROCESSO IN FUALT VIENE RIATTIVATO LO STESSO COSì PARTE DAL SUO STESSO STATO
                    // SupervisorStrategy.stop() -> IN QUESTO CASO NON CREARE NESSUN ALTRO ACTOR E QUINDI I MESSAGGI DOPO NON SONO GESTITI
                    DeciderBuilder.match(Exception.class, e -> SupervisorStrategy.resume())
                            .build());

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }

    public Ex5SupervisorActor() {
    }

    @Override
    public Receive createReceive() {
        // Creates the child actor within the supervisor actor context
        // COSì UNISCO IL FIGLIO ACTOR DENTRO A Ex5SupervisorActor COSì  LO GESTISCE
        return receiveBuilder()
                .match(
                        Props.class,
                        props -> {
                            // MANDA A CHI FA FATTO RICHIESTA IN QUESTO CASO IL MAIN IL RIFERIMENTO ALL'ACTOR CREATO
                            // POI QUESTO RIFERIMENTO LO PASSA AL CLIENT O AL SERVER
                            // COL IL getContext() è il contesto del supervisor nel quale viene creto il server ed il client
                            getSender().tell(getContext().actorOf(props), getSelf());
                        })
                .build();
    }

    static Props props() {
        return Props.create(Ex5SupervisorActor.class);
    }

}
