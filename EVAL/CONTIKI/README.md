# Evaluation lab - Contiki-NG

## Group number: 13

## Group members

- Bersani Michele
- Marenghi Manuela
- Dalle Rive Paolo

## Solution description

PROJECT DESCRIPTION: Mote 1 is the server, the other Motes are clients.

CLIENT CODE:

At each SEND_INTERVAL the client checks whether it is connected to the server. 
If it is connected to the server then it sends the reading.
if it is not connected it stores the reading inside the array reading[MAX_READINGS], it parses the array in a cyclic manner when the max position is reached.

If he manages to reconnect, he checks whether he was previously disconnected using the avg flag. (avg=0 if connected, avg=1 if disconnected).

If he goes from disconnected to connected, he calculates the average (using an average() function) of the readings saved locally, rounds from float to unsigned and sends it to the server as an unsigned.


SERVER CODE:

In the server code, a able() function is used to check if a new client can connect or not, based on MAX_RECEIVERS.
If the client is not in the ip array:
 if the array is full, the connection request is ignored;
 if the array is not full, the ip is added to the ip array.
If the client is already in the array, the reading is accepted.

The server always receives unsigned data type from clients.
The average/readings code is left untouched.
