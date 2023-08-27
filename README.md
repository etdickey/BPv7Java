# BPv7Java
This repository consists of the code implementing bundle protocol version 7 as described in the paper (insert link here). 
The protocol is implemented based on RFC 9171, but due to use of JSON over CBOR it is not strictly following the RFC.
The src folder contains DTCP which has the code for Disruption-TCP (DTCP) a de facto convergence layer.
The main BP code is in the BPv7 folder.

The method to run the code is as follows:
First install dependencies which include:
<Insert dependencies>
With the following commands:
<Terminal commands to install dependencies>

To run the code go into the <folder> directory and run:
<command>

EXAMPLE TRANSMISSION:
The example transmission following the figure shown below (An example transmission from Node A to Node B through Node F the ‘Forwarder.’)
(1) Sending the message: send(a).
(2) User API sends the received message a to the BPA: send(a).
(3) BPA storing the received message a with its id as a key
a_id=4 inside the send buffer send_buffer if there is a space
available for it.
(4) BPA’s send_buffer returns the key of a which is 4.
(5) The sender thread SenderThread is spawned by BPA. The
thread calls next_bundle function to retrieve a message that
needs to be sent.
(6) send_buffer returns a to SenderThread
(7) SenderThread makes send_buffer mark a as sent.
(8) SenderThread inquires DTCP via DTCP API whether the
next node – Node F – is currently reachable or not:
canReach(nodeID). If DTCP API returns No, run (8) again;
if Yes, then move on to (9).
(8.5) DTCP API checks whether Node F is reachable or not,
then response back to SenderThread.
(9) Once DTCP API sends Yes in (8), process a into a bundle and
send: Send(Bundle(a)).
(10) ANSF: Process Send(Bundle(a)) into network serializable
format and send it over to TCP.
(11) Transmit it to the next node via TCP.
(12-13) ANSF.
(14) Notify ListenerThread that a new packet/bundle has ar-
rived: YouGotAMessage(Bundle(a)).
(15) Send it to another function in BPA for decoding.
(16) Decode bundle and realize it is destined for another node.
Send delivery confirmation admin record if requested.
(17) Message a gets stored in sending buffer SendBuffer with
some key of Node F’s choice x.
(18) SenderThread asks for the next message to be sent: getNextMsg
(19) SenderThread receives the message a from sendBuffer.
(20-29) Same as (8-17).
(30) The application of Node B requests for a certain number of
bytes n.
(31) Reads data from the receiving buffer receiveBuffer: a[n]
= getPayload(n)
(32) receiveBuffer returns a[n].
(33) Stores a[n] to a local buffer just in case.
(34) Returns a[n] to the application.
![image](https://github.com/etdickey/BPv7Java/assets/61432064/c59483ce-641f-44fa-a7d5-19cd99034999)

Absence of Routing Method. Information about routing and
forwarding is provided in Sections 3.8 and 4.3 of RFC 4838, the first
RFC describing the basic architecture of DTN. However, it provides
only rough, high-level intuition on how routings in DTN can be
modeled mathematically. There have not been any updates or new
versions of the RFC since 4838 appeared. Although RFC 9171 and
5050 are strictly about a specific protocol, given that they assume
the existence of a convergence layer protocol for handling node
ID name resolution, some practical details about routing must at
least be mentioned and described.

Individual graphs:
![image](https://github.com/etdickey/BPv7Java/assets/61432064/a5c2d8ab-eb52-4c41-a7d0-89f493549dc9)
![image](https://github.com/etdickey/BPv7Java/assets/61432064/8c6f4180-e1ba-4cb3-a05f-2284645b6b5b)
![image](https://github.com/etdickey/BPv7Java/assets/61432064/92f412a9-5fcb-402d-adda-78f6d772d454)
![image](https://github.com/etdickey/BPv7Java/assets/61432064/5c7b537a-5c87-4e0f-ae95-48e2012f7334)
![image](https://github.com/etdickey/BPv7Java/assets/61432064/d1ebe00d-37aa-49cd-b776-f99405f7ca04)
![image](https://github.com/etdickey/BPv7Java/assets/61432064/890b9a87-c448-430c-8985-bcffb97227e3)
![image](https://github.com/etdickey/BPv7Java/assets/61432064/010a255e-8150-41b8-81c9-2d5d77bf65ec)
![image](https://github.com/etdickey/BPv7Java/assets/61432064/14ae5f12-c143-46af-a0ef-6fc16e9b93aa)






