package com.ex3;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.concurrent.TimeoutException;

import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;

public class ActorClient extends AbstractActor {
    private ActorRef server;
    private scala.concurrent.duration.Duration timeout = scala.concurrent.duration.Duration.create(5, SECONDS);

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
        System.out.println("CLIENT: Issuing query for " + name);

        // UTILIZZO NEL CASO IN CUI SO GIà CHE ASPETTO UNA RISPOSTA DA QUEL MESSAGGIO IN QUESTO CASO LO STO -> IN QUESTO CASO METTE DI DEFUALT CHI LO HA MANDATO
        // FACENDO IN MANIERA SINCRONA
        scala.concurrent.Future<Object> waitingForReply = ask(server, new Getmsg(name), 5000);
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
    public static Props props() {
        return Props.create(ActorClient.class);
    }
}
