# Evaluation lab - Apache Kafka

## Group number: 13

## Group members

- Manuela Marenghi
- Michele Bersani
- Paolo Dalle Rive

## Exercise 1
The numbers of partitions allowed for inputTopic is min=1 for the basic case where you have no partitions other
than partition Zero, and max=N.
The number of consumers allowed in order for every consumer to be able to get records from the topic
is NumberOfConsumers <= NumberOfPartitions for consumers of the same group.
Min consumers = 1

- Number of partitions allowed for inputTopic (min, max)
- Number of consumers allowed (min, max)
  - Consumer 1: <groupA>
  - Consumer 2: <groupA>
  - ...
  - Consumer n: <groupB>
    The name of the group can be the same for multiple consumers,
    provided the constraints on the number of partitions and consumers
    are guaranteed.

group name can be repeated for different Consumer but must be different from PopularTopicConsumer groups,
otherwise for example some records would be read by PopularTopicConsumer and not by AtMostOncePrinter.

## Exercise 2
In order to have a global maximum we can have N partitions, with 1 consumer per group
We can then have N consumers, provided each and every one of them belongs to 
a different group.

The min number of partitions is still ONE. The min number of consumers is 1.

- Number of partitions allowed for inputTopic (min, max)
- Number of consumers allowed (min, max)
  - Consumer 1: <GroupC>
  - Consumer 2: <GroupC1>
  - ...
  - Consumer n: <GroupD>
  -
