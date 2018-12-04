Instructions to execute.

'make server' builds the server
'make client' builds the client

to execute server - java Server <port> <filename> <probablity>
to execute client - java Client <server-hostname> <server-port> <filename> <value of N> <value of MSS>

1.
We see a varying graph. For N = 1 time is high but it reduces from 2 to 16 and then again varies till 1024.
For 1 since it just sends 1 packet at a time it is slow. For 2 to 16 it thus is better as more packets are sent. 
But then when a lot of packets are sent, for a single loss multiple packets must be sent thus it increases 
overhead. Thus above 16, it totally depends on number of packet losses. If more, then the time taken is greater.
For 1024 time is more because each packet loss leads to 1024 packets to be retransmitted.
2.
The result is pretty straight forward. More the MSS less the time. This is because for the same rate (even if 
loss is present) more data per packet is transmitted. So definitely it increases efficiency given constant N and 
packet loss probablity.
3.
Here too the result is straight forward. More the packet loss probablity, more is the time taken. Since more packets get lost, more time is taken given other factors are constant.