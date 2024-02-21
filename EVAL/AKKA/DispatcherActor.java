package com.EVAL;

import akka.actor.*;
import akka.japi.pf.DeciderBuilder;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;

public class DispatcherActor extends AbstractActorWithStash {

	private final static int NO_PROCESSORS = 4;

	private final ArrayList<ActorRef> processor=new ArrayList<>();

	private int currentProcessor;
	private HashMap<ActorRef,ActorRef> sensortoProcessor=new HashMap<>();
	private HashMap<ActorRef,Integer> numbSensor=new HashMap<>();
	int min=333333;
	private static SupervisorStrategy strategy =
			new OneForOneStrategy(
					1,
					Duration.ofMinutes(1),
					DeciderBuilder.match(Exception.class, e -> SupervisorStrategy.resume()).build()
			);

	@Override
	public SupervisorStrategy supervisorStrategy() {
		return strategy;
	}

	public DispatcherActor() {
		for(int i=0;i<NO_PROCESSORS;i++){
			processor.add(getContext().actorOf(SensorProcessorActor.props()));
			numbSensor.put(processor.get(i),0);
		}
		currentProcessor=0;
	}


	@Override
	public Receive createReceive() {
		return loadbalancing();
	}

	private final Receive loadbalancing(){
		return receiveBuilder()
				.match(DispatchLogicMsg.class,this::changelogic)
				.match(TemperatureMsg.class,this::dispatchDataLoadBalancer)
				.build();
	}

    private void changelogic(DispatchLogicMsg msg){
		if (msg.getLogic()==0){
			System.out.println("DISPACHER: round robin turned on");
			getContext().become(roundrobin());
		} else {
			System.out.println("DISPACHER: load balancing turned on");
			getContext().become(loadbalancing());
		}
	}

	private final Receive roundrobin(){
		return receiveBuilder()
				.match(DispatchLogicMsg.class,this::changelogic)
				.match(TemperatureMsg.class,this::dispatchDataRoundRobin)
				.build();
	}

	private void dispatchDataLoadBalancer(TemperatureMsg msg) {
		if(!sensortoProcessor.containsKey(msg.getSender())){
			reBilancioSensor(msg.getSender());
		}
		sensortoProcessor.get(msg.getSender()).tell(msg, self());
	}

	private void reBilancioSensor(ActorRef actorRef){
        ActorRef p = null;
		int min = -1;
        for(ActorRef i: numbSensor.keySet()){
			if(numbSensor.get(i) < min || min==-1){
				p=i;
				min = numbSensor.get(i);
			}
		}

		sensortoProcessor.put(actorRef,p);
		int num = numbSensor.get(p)+1;
		numbSensor.put(p, num);
	}


	private void dispatchDataRoundRobin(TemperatureMsg msg) {
         processor.get(currentProcessor).tell(msg,self());
		 currentProcessor++;
		 if(currentProcessor==NO_PROCESSORS){
			 currentProcessor=0;
		 }
	}

	static Props props() {
		return Props.create(DispatcherActor.class);
	}
}
