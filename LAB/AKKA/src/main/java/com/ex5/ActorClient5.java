package com.ex5;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.concurrent.TimeoutException;

import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;

public class ActorClient5 extends AbstractActor {
    private ActorRef server;
    private scala.concurrent.duration.Duration timeout = scala.concurrent.duration.Duration.create(5, SECONDS);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class,this::onReplyReceived)
                .match(PutMsg5.class,this::sendput)
                .match(GetMsg5.class,this::sendget)
                .match(ReplyMsg.class, this::processReply)
                .match(StartConnection.class,this::onStartConnection).build();
    }

    private void onStartConnection(StartConnection start) {
        this.server=start.getServer();
    }

    void onReplyReceived(String p) {
        System.out.println("The email is:"+p);
    }

    public void sendput(PutMsg5 msgClient){
           server.tell(msgClient,self());
    }

    public void sendget(GetMsg5 name){
        System.out.println("CLIENT: Issuing query for " + name);

        // UTILIZZO NEL CASO IN CUI SO GIà CHE ASPETTO UNA RISPOSTA DA QUEL MESSAGGIO IN QUESTO CASO LO STO -> IN QUESTO CASO METTE DI DEFUALT CHI LO HA MANDATO
        // FACENDO IN MANIERA SINCRONA
        scala.concurrent.Future<Object> waitingForReply = ask(server, name, 5000);
        try {
            ReplyMsg reply = (ReplyMsg) waitingForReply.result(timeout, null);
            if (reply.getEmail()!=null) {
                System.out.println("CLIENT: Received reply, email is " + reply.getEmail());
            } else {
                System.out.println("CLIENT: Received reply, no email found!");
            }
        } catch (TimeoutException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // server.tell(new Getmsg(name),self());
    }

    void processReply(ReplyMsg msg) {
        // GESTIONE INVIO DI MESSAGGIO DA PARTE DEL SERVER NON GRADITO / IN PIù
        // MA LA RISPOSTA DEL SERVER DOVREBBE ARRIVARE DIRETTAMENTE IN waitingForReply
        System.out.println("CLIENT: Unsolicited reply, this should not happen!");
    }
    public static Props props() {
        return Props.create(ActorClient5.class);
    }
}
