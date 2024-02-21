# Evaluation lab - Node-RED

## Group number: 13

## Group members

- Manuela Marenghi
- Michele Bersani
- Paolo Dalle Rive

## Description of message flows
When receiving a message the telegram bot decide depending on the query how to answer:

-"What's tomorrow’s forecast in Milano/Rome?" --> It answer checking the weather description after 24 hours from the request

-What’s the weather in two days in Milano/Rome? --> It answer checking the weather description after 48 hours from the request

-"What’s the expected wind speed tomorrow in Milano/Rome?"  -->  It answer with the expected speed after 24 hours from the request

-What’s the expected wind speed tomorrow in Milano/Rome?  -->  It answer with the expected speed after 48 hours from the request

When other queries are sent the reply is always about an error message.
In order to do manage the flow we decide to use same function block for the two cities and using the msg.location information from the openweather payload, that was already set in the first function that takes decision about the different flow to follow.
Flow of nodes:
-Telgram Receiver : receive the message from the chat and send it to the function that acts as dispacher
- ReadMessage  : decide basing on the request wheter to reply with a message error or to send the correct message to the openweather node with information about :timing of the requested forecast(24 or 48 hours), wind speed or weather, city(Milan or Rome)
- Wheater : return a message with the forecast within the 5 next days
- HandleWeather : generate the answer about weather
- WriteMessage : format the answer
- Telegram Sender : sends the answer to the chat

## Extensions

## Bot URL
Telegram API key : 5896944400:AAEKohrc3xU1PYD9vUT_IdCXpOXMdyj1nlM
Link: t.me/nodered_red_bot
Openweather API key : 403001fb4dd31e5ec4743fa058339109
