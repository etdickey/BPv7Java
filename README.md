# BPv7Java

This repository consists of the code implementing Bundle Protocol version 7 (BPv7) and supplementary information for the paper 
<i><a href="">Bundle Protocol version 7 Implementation with Configurable Faulty Network and Evaluation</a></i> published in [IEEE WiSEE 2023](https://2023.ieee-wisee.org/) at Aveiro, Portugal. 

#### Contributors / Authors
<table>
  <tbody>
    <tr>
      <td align="center" valign="top" width="16.66%"><a href="https://github.com/aarcc530"><img src="https://avatars.githubusercontent.com/u/24422787?v=4" width="100px;" alt="Aidan Casey"/><br /><sub><b>Aidan Casey</b></sub></a><br /></td>
      <td align="center" valign="top" width="16.66%"><a href="https://github.com/etdickey"><img src="https://avatars.githubusercontent.com/u/16783711?v=4" width="100px;" alt="Ethan Dickey"/><br /><sub><b>Ethan Dickey</b></sub></a><br /></td>
			<td align="center" valign="top" width="16.66%"><a href="https://github.com/jihunhwang"><img src="https://avatars.githubusercontent.com/u/29069044?v=4" width="100px;" alt="Jimmy Hwang"/><br /><sub><b>Jihun Hwang</b></sub></a><br /></td>
			<td align="center" valign="top" width="16.66%"><a href="https://github.com/Sachitkothari"><img src="https://avatars.githubusercontent.com/u/61432064?v=4" width="100px;" alt="Sachit Kothari"/><br /><sub><b>Sachit Kothari</b></sub></a><br /></td>
			<td align="center" valign="top" width="16.66%"><a href="https://github.com/007rau"><img src="https://avatars.githubusercontent.com/u/20044189?v=4" width="100px;" alt="Raushan Pandey"/><br /><sub><b>Raushan Pandey</b></sub></a><br /></td>
			<td align="center" valign="top" width="16.66%"><a href="https://github.com/wenboxie-umass"><img src="https://avatars.githubusercontent.com/u/29896922?v=4" width="100px;" alt="Wenbo Xie"/><br /><sub><b>Wenbo Xie</b></sub></a><br /></td>
    </tr>
  </tbody>
</table>

#### Description

This is a lightweight, easy-to-understand, framework specifically designed to simulate and test BPv7. It was implemented solely based on [RFC 9171](https://datatracker.ietf.org/doc/rfc9171/), the IETF standardization document that defines and specifies BPv7. 

**Disclaimer**: Due to the use of JSON over CBOR, this implementation is not fully RFC9171-compliant. JSON was chosen over CBOR for the ease of analysis and debugging process, as one of our goals was to create an easy-to-understand testbed for BPv7.

The src folder contains DTCP which has the code for Disruption-TCP (DTCP) a de facto convergence layer that we created for this research. Please refer to the paper (Section II-A) for details.
The main BP code is in the BPv7 folder.



## Running the code

### Dependencies
* [Mininet](http://mininet.org/): Software-defined networking (SDN) based network emulator.
* [Open Network Operating System (ONOS)](https://opennetworking.org/onos/): Software providing the control plane for an SDN. This is the operating system for the SDN controller inside our Mininet.

### Instruction
The video tutorial (demo video) is available in this link: https://youtu.be/aika4nRm7wM
1. Clone this repository
2. Open four separate terminals
3. In the first shell, start ONOS:
   ```
   make controller
   ```
4. In the second terminal, start Mininet:
   ```
   make mininet
   ```
5. Start the ONOS command-line interface (CLI) from the third terminal. The password is "rocks":
   ```
   make cli
   ```
6. In the fourth terminal, run the ONOS [netcfg](mininet/cfg/netcfg.json) script:
   ```
   make netcfg
   ```
7. You can try to ping hosts from one another to see if they respond correctly. For example, in the default setting (see [Makefile](mininet/Makefile) in `mininet` folder), there are three hosts: `h1`, `h2`, and `h3`. One can test if `h2` is reachable from `h1` using the following script:
	```
	h1 ping h2
	```
8. One can modify the config files ([Makefile](mininet/Makefile) for Mininet and [netcfg](mininet/cfg/netcfg.json) for ONOS)
	

## Example Transmission

The example transmission following the figure shown below (An example transmission from Node A to Node B through Node F the ‘Forwarder.’)

![image](https://github.com/etdickey/BPv7Java/assets/61432064/c59483ce-641f-44fa-a7d5-19cd99034999)
```markdown
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
```
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

### Individual graphs
See

![image](https://github.com/etdickey/BPv7Java/assets/61432064/a5c2d8ab-eb52-4c41-a7d0-89f493549dc9)
![image](https://github.com/etdickey/BPv7Java/assets/61432064/8c6f4180-e1ba-4cb3-a05f-2284645b6b5b)
![image](https://github.com/etdickey/BPv7Java/assets/61432064/92f412a9-5fcb-402d-adda-78f6d772d454)
![image](https://github.com/etdickey/BPv7Java/assets/61432064/5c7b537a-5c87-4e0f-ae95-48e2012f7334)
![image](https://github.com/etdickey/BPv7Java/assets/61432064/d1ebe00d-37aa-49cd-b776-f99405f7ca04)
![image](https://github.com/etdickey/BPv7Java/assets/61432064/890b9a87-c448-430c-8985-bcffb97227e3)
![image](https://github.com/etdickey/BPv7Java/assets/61432064/010a255e-8150-41b8-81c9-2d5d77bf65ec)
![image](https://github.com/etdickey/BPv7Java/assets/61432064/14ae5f12-c143-46af-a0ef-6fc16e9b93aa)





[contributors-shield]: https://img.shields.io/github/contributors/othneildrew/Best-README-Template.svg?style=for-the-badge
[contributors-url]: https://github.com/etdickey/BPv7Java/graphs/contributors
