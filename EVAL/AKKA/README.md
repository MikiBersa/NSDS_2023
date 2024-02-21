# Evaluation lab - Akka

## Group number: XX

## Group members

- Manuela Marenghi
- Michele Bersani
- Paolo Dalle Rive

## Description of message flows

The main starts by creating a set number of sensors.
The dispatcher is created and with it, the processors are created.
We send a ConfigSensor message to all the sensors which contains a reference to the dispatcher.
When the Sensors receive a GenerateMsg, they sent a TemperatureMsg to the Dispatcher.
Based on the dispatching setting, the Dispatcher proceeds to send the TemperatureMsg to the Processors.
The Processors compute the average without replying. If an exception were to be thrown,the average
would not be computed for the current message and the Dispatcher would handle the exception.
The exception is handled by Resuming the Processor.
We start in LOAD BALANCING, after the first batch we go to ROUND ROBIN and add the faulty sensor,
we end in LOAD BALANCING.
We then create faultySensors, send them the ConfigSensor message and the dispatcher acts as above.
And finally we send a new batch of TemperatureMsg, also containing the faulty ones.
The exception is created only when the temperature is < 0.


