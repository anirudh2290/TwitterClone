Simulation of Twitter clients
==============================

Team members: 
Ankit Sharma (UFID: 24868901) 
Mugdha Khade (UFID: 98907680) 
Bharath Kurumaddali (UFID: 65130561) 
Anirudh Subramanian (UFID: 94453124) 

Environment tested on :: cise Lin servers Number of cores :: 8

Instructions :: How to Run 
===========================

sbt "runMain project4 <numberOfUsers> <isServer> <numberOfLoadBalancers>"

example:: On server :: sbt "runMain project4 1000 true 4" . On client :: sbt "runMain project4 1000 false 4"

Tests
=====

1.	Recorded the number of tweet requested and responses that are processed by the server for a variant of Number of Users. 
2.	For each second we record the number of tweets per second that are coming in (tweets) and going out (tweets requested) of the server (along with the load balancers)
	for variant number of servers
	
	
Project Design Description
==========================

Simulating a virtual twitter environment of 1 million users as close as possible with real 
time statistics available. A user can tweet short message (of junk characters) of 140 
characters length and also receive tweets from other users whom one is following.

Client side::
-------------

We kept a client master and the client workers which tweet are created by the client master. The client workers send and receive tweets at periodic time intervals.
The period that we have kept for sending tweets is 10 seconds and the period that we have kept for receiving for receiving tweets is 1 second. 
Also not every client doesnt send a tweet every second. They only send the tweet when the random number generated between 0 and 1 falls below the stable Probability.
We have peaks in the middle of the time duration and we increase the probability during this time duration which will allow for more clients to tweet.
Thus our statistics model handles nearly stable tweets per seconds with peaks many times more than the normal. 
We obtained the statistics model from here :: https://blog.twitter.com/2013/new-tweets-per-second-record-and-how


We can extend our statistics model to have some users not tweet at all and some users tweet everytime by keeping their probability value as 0 or 1. 
We are generating random string for tweets.

Server side::
-------------

On the server side there are servers which act as load balancers and do the routing to the ServerWorkers.
The following happens in the ServerWorkers.
Each ServerWorker contains a Map of id to User data structure . The User data structure contains the message queue and followers list.
The followers list is initialized when the init is called for the first server. Every time it gets a tweet , the worker adds it to its own
message queue as well as followers message queue.
When it gets a receiveTweet request then it sends back the message queue for that particular user.

Statistics::
------------

We have measured the following statistics on the server side. 
We have measured the total count of tweets received and total count for timeline requests received.
We have also measured the total count of tweets every second received at the server level for tweets received and timeline sent back.

Please refer to the Graph.docx at the root of the project for more information.



Maximum number of users we tested for :: 1 million

