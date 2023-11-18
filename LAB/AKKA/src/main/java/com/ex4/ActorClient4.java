package com.ex4;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.ex3.ActorClient;
import com.ex3.StartConnection;

public class ActorClient4 extends AbstractActor {
    private ActorRef server;
    @Override
    public Receive createReceive() {
        return receiveBuilder().match(TextMsg.class, this::sendText).match(StartConnection.class, this::onStartConnection)
                .match(Sleep.class, this::reroute).match(Wakeup.class, this::reroute).build();
    }

    void onStartConnection(StartConnection p) {
        this.server=p.getServer();
    }

    void reroute(Msg msg) {
        // QUALSIASI SI AIL MESSAGGIO DI TIPO LO INOLTRO AL SERVER
        System.out.println("CLIENT: Rerouting message " +msg);
        server.tell(msg, self());
    }

    void sendText(TextMsg msg) {
        if (msg.getSender() == ActorRef.noSender()) {
            // Message coming from outside the actor system
            // QUI I MESSAGGI DEVONO ESSERE TRASFORMATI  METTENDO IL SENDER PERCHè POI
            // SE IL SERVER LI DEVE MANDARE INDIETRO DEVE SAPERE DA CHI è STATO INIVIATO SE NO DA ERROREs
            // QUINDI OGNI MESSAGGIO CHE HO BISOGNO DI RIMANDARE INDIETRO HO BISOGNO DI SAPERE CHI LO HA MANDATO
            System.out.println("CLIENT: Sending text message: " + msg.getText());
            msg.setSender(self());
            server.tell(msg, self());
        } else {
            // The server is sending this back
            System.out.println("CLIENT: Received reply, text: " + msg.getText());
        }
    }

    void onReplyReceived(String p) {
        System.out.println(p);
    }


    public static Props props() {
        return Props.create(ActorClient4.class);
    }
}
