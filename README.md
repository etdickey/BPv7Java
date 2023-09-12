# BPv7Java

This repository consists of the code implementing Bundle Protocol version 7 (BPv7) and supplementary information for the paper 
<i><a href="">Bundle Protocol version 7 Implementation with Configurable Faulty Network and Evaluation</a></i> published in [IEEE WiSEE 2023](https://2023.ieee-wisee.org/) at Aveiro, Portugal. 

#### Contributors / Authors

<center>
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
</center>

### Description

This is a lightweight, easy-to-understand, framework specifically designed to simulate and test BPv7. It was implemented solely based on [RFC 9171](https://datatracker.ietf.org/doc/rfc9171/) (see the disclaimer below), the IETF standardization document that defines and specifies BPv7. 

The [src](src) folder is the main folder that contains the implementation of BPv7 architecture. It largely consists of three subfolders: [DTCP](src/DTCP), [BPv7](src/BPv7), [Configs](src/Configs). 
* The [src/DTCP](src/DTCP) folder contains the code for Disruption-TCP (DTCP), the de facto convergence layer that we created for this project. It is our configurable faulty network CLA that is capable of simulating disruptions, both expected and unexpected ones. Please refer to **Section II-A** of our paper for details. 
* The main BPv7 code is in the [src/BPv7](src/BPv7) folder; see [RFC 9171](https://datatracker.ietf.org/doc/rfc9171/) for more details.
* Various parameters (e.g., bundle lifetime, sending delay range between bundles, etc.) can be set in the [src/Configs](src/Configs) folder. Moreover, simulation scenarios can be found and added in [src/Configs/resources](src/Configs/resources) folder.

The [mininet](mininet) folder contains the files for network configurations (e.g., changing the topology of networks). 
* [mininet/Makefile](mininet/Makefile) file allows users to add more hosts.
* [mininet/cfg/netcfg.json](mininet/cfg/netcfg.json) file is for configuring switches/gateways that connect hosts. 


**Disclaimer**: Due to the use of JSON over CBOR, this implementation is not fully [RFC9171](https://datatracker.ietf.org/doc/rfc9171/)-compliant. JSON was chosen over CBOR for understandability and ease of implementation and analysis, as one of our goals was to create an easy-to-understand testbed for BPv7.

## Running the code

### Dependencies
* [Mininet](http://mininet.org/): Software-defined networking (SDN) based network emulator.
* [Open Network Operating System (ONOS)](https://opennetworking.org/onos/): Software providing the control plane for an SDN. This is the operating system for the SDN controller inside our Mininet.

### Instruction
The video tutorial (demo video) is available in this link: https://youtu.be/aika4nRm7wM.
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
5. Start the ONOS command-line interface (CLI) from the third terminal. The password is `rocks`:
   ```
   make cli
   ```
   and activate the routing application `fwd` using this command:
   ```
   app activate fwd
   ```
7. In the fourth terminal, run the ONOS [netcfg](mininet/cfg/netcfg.json) script:
   ```
   make netcfg
   ```
8. You can try to ping hosts from one another to see if they respond correctly. For example, in the default setting (see [Makefile](mininet/Makefile) in `mininet` folder), there are three hosts: `h1`, `h2`, and `h3`. One can test if `h2` is reachable from `h1` using the following script:
	```
	h1 ping h2
	```
9. One can modify the config files ([Makefile](mininet/Makefile) for Mininet and [netcfg](mininet/cfg/netcfg.json) for ONOS) as needed, to simulate more complicated network topology. The default setup is: three hosts (`h1`, `h2`, and `h3`) being connected to switch `s1` acting as a gateway. However, our DTCP forces a topology of `h1 -- h3 -- h2`, basically making `h3` act as a forwarding node.

For more details, please refer to our demo video https://youtu.be/aika4nRm7wM.
	

## Example Transmission

This is a continuation of **Section II-B** (Implementation - Mininet) of our paper. Here is how a transmission from `Node A` to `Node B` through `Node F` the 'Forwarder' would look like.

<p align="center">
<img src="https://github.com/etdickey/BPv7Java/assets/29069044/b9964104-1904-4190-9757-f3402c34ef19" width="50%" height="50%"/>
</p>

Explanations for each step are as follows:

* **(1)** Sending the message: `send(a)`.
* **(2)** User API sends the received message a to the BPA: `send(a)`.
* **(3)** BPA storing the received message a with its id as a key `a_id=4` inside the send buffer `send_buffer` if there is a space available for it.
* **(4)** BPA's `send_buffer` returns the key of a which is `4`.
* **(5)** The sender thread `SenderThread` is spawned by BPA. The thread calls `next_bundle` function to retrieve a message that needs to be sent.
* **(6)** `send_buffer` returns a to `SenderThread`
* **(7)** `SenderThread` makes `send_buffer` mark `a` as sent.
* **(8)** `SenderThread` inquires DTCP via DTCP API whether the next node – `Node F` – is currently reachable or not: `canReach(nodeID)`.
  
	If DTCP API returns `No`, run **(8)** again; if `Yes`, then move on to **(9)**.

* **(8.5)** DTCP API checks whether `Node F` is reachable or not, then response back to `SenderThread`.
* **(9)** Once DTCP API sends `Yes` in **(8)**, process `a` into a bundle and send: `Send(Bundle(a))`.
* **(10)** ANSF: Process `Send(Bundle(a))` into network serializable format and send it over to TCP.
* **(11)** Transmit it to the next node via TCP.
* **(12-13)** ANSF.
* **(14)** Notify `ListenerThread` that a new packet/bundle has arrived: `YouGotAMessage(Bundle(a))`.
* **(15)** Send it to another function in BPA for decoding.
* **(16)** Decode bundle and realize it is destined for another node. Send delivery confirmation admin record if requested.
* **(17)** Message a gets stored in the sending buffer `SendBuffer` with some key of `Node F`’s choice `x`.
* **(18)** `SenderThread` asks for the next message to be sent: `getNextMsg`
* **(19)** `SenderThread` receives the message `a` from `sendBuffer`.
* **(20-29)** Same as **(8-17)**.
* **(30)** The application of `Node B` requests for a certain number of bytes `n`.
* **(31)** Reads data from the receiving buffer `receiveBuffer`: `a[n] = getPayload(n)`
* **(32)** `receiveBuffer` returns `a[n]`.
* **(33)** Stores `a[n]` to a local buffer just in case.
* **(34)** Returns `a[n]` to the application.

## Individual Graphs

This is a continuation of **Section III** (Analysis) of our paper. Here is the list of figures that appears on our paper:
* [**Figure 1.** Scenario 100, 101, 111: Delay from application layer to application layer between sender and receiver, high density tests without the 110 test (which has high density of packets, high packet size, and low density of expected downs). Packets stopped being sent from the application layer at t = 50s.](mininet/50s_sums_logs_and_graphs/EndToEndLONGHighDensity_No110.png) 
	This is also **Figure 1** of our paper.
* [**Figure 2.** Scenario 100, 101, 110, 111: Delay from application layer to application layer between sender and receiver, high density tests. Same as Figure 1 with the 110 test added. Packets stopped being sent from the application layer at t = 50s.](mininet/50s_sums_logs_and_graphs/EndToEndLONGHighDensity.png) 
	This is also **Figure 2** of our paper.
* [**Figure 3.** Scenario 000, 001, 010, 011: Delay between application layers of sender and receiver, low density tests. Packets stopped sending from the application layer at t = 50s.](mininet/50s_sums_logs_and_graphs/EndToEndLONGLowDensity.png) 
	This is also **Figure 3** of our paper.

The following figures are also the graphs of the delay from application layer to application layer between sender and receiver (also packets stopped being sent from the application layer at t=50s), but **for each scenario**.
* [**Figure 4.** Scenario 000: Low density, small packet sizes, low expected disruption density.](mininet/50s_sums_logs_and_graphs/EndToEndLONG000.png)
* [**Figure 5.** Scenario 001: Low density, small packet sizes, high expected disruption density.](mininet/50s_sums_logs_and_graphs/EndToEndLONG001.png)
* [**Figure 6.** Scenario 010: Low density, large packet sizes, low expected disruption density.](mininet/50s_sums_logs_and_graphs/EndToEndLONG010.png)
* [**Figure 7.** Scenario 011: Low density, large packet sizes, high expected disruption density.](mininet/50s_sums_logs_and_graphs/EndToEndLONG011.png)
* [**Figure 8.** Scenario 100: High density, small packet sizes, low expected disruption density.](mininet/50s_sums_logs_and_graphs/EndToEndLONG100.png)
* [**Figure 9.** Scenario 101: High density, small packet sizes, high expected disruption density.](mininet/50s_sums_logs_and_graphs/EndToEndLONG101.png)
* [**Figure 10.** Scenario 110: High density, large packet sizes, low expected disruption density.](mininet/50s_sums_logs_and_graphs/EndToEndLONG110.png)
* [**Figure 11.** Scenario 111: High density, large packet sizes, high expected disruption density.](mininet/50s_sums_logs_and_graphs/EndToEndLONG111.png)

Figure 1-3 are combinations of Figure 4-11. In particular,
* For [**Figure 1**](mininet/50s_sums_logs_and_graphs/EndToEndLONGHighDensity_No110.png), see [**Figure 8**](mininet/50s_sums_logs_and_graphs/EndToEndLONG100.png), [**Figure 9**](mininet/50s_sums_logs_and_graphs/EndToEndLONG101.png), and [**Figure 11**](mininet/50s_sums_logs_and_graphs/EndToEndLONG111.png).
* For [**Figure 2**](mininet/50s_sums_logs_and_graphs/EndToEndLONGHighDensity.png), see [**Figure 8**](mininet/50s_sums_logs_and_graphs/EndToEndLONG100.png), [**Figure 9**](mininet/50s_sums_logs_and_graphs/EndToEndLONG101.png), [**Figure 10**](mininet/50s_sums_logs_and_graphs/EndToEndLONG110.png), and [**Figure 11**](mininet/50s_sums_logs_and_graphs/EndToEndLONG111.png).
* For [**Figure 3**](mininet/50s_sums_logs_and_graphs/EndToEndLONGLowDensity.png), see [**Figure 4**](mininet/50s_sums_logs_and_graphs/EndToEndLONG000.png), [**Figure 5**](mininet/50s_sums_logs_and_graphs/EndToEndLONG001.png), [**Figure 6**](mininet/50s_sums_logs_and_graphs/EndToEndLONG010.png), and [**Figure 7**](mininet/50s_sums_logs_and_graphs/EndToEndLONG011.png).

A more accurate figure for **Figure 10 (Scenario 110)** can be found below:
<p align="center">
<img src="https://github.com/etdickey/BPv7Java/assets/29069044/c4ed266b-0bf8-4071-b880-8d0877578406" width="50%" height="50%">
</p>

This plot represents the case of "**BPA Thrashing**" where the queue in the forwarding host got so big that by the time a bundle queued up, it had already expired, and so they needed their lifetime extended by the sending host and to be resent. Recall that packets were stopped being sent from the application layer at t=50s, and that thrashing was under what we consider normal network loads. This hence suggests that even a small-scaled DTN network may experience a performance issue under network traffic that is considered normal today.

Similarly, here is a more accurate version of **Figure 11 (Scenario 111)**
<p align="center">
<img src="https://github.com/etdickey/BPv7Java/assets/29069044/f4a54738-1f2f-4270-ba3e-6e2edba2026d" width="50%" height="50%">
</p>

and this updates **Figure 1 (Scenario 100, 101, 111)** as follows:
<p align="center">
<img src="https://github.com/etdickey/BPv7Java/assets/29069044/403326bf-ea81-4e75-9a08-e2dcb82df44f" width="50%" height="50%">
</p>

For more simulation results, please see [mininet/5s_sims_logs_and_graphs](mininet/5s_sims_logs_and_graphs). This folder contains the results for the experiments where the packets were stopped being sent from the application layer at t=5s. 


## Missing Critical Features in RFC 9171
This is the continuation of **Section IV-C** (Architectural Improvements - Missing Critical Features) and **Section II-C-1** (Implementation - Configuration - Routing and Name Lookup) of our paper. These were omitted in our paper as these are the deficiencies of the current BPv7 architecture regarding potential deployment issues that are closer to the implementation details than flaws of RFC [4838](https://datatracker.ietf.org/doc/rfc4838/)/[5050](https://datatracker.ietf.org/doc/rfc5050/)/[9171](https://datatracker.ietf.org/doc/rfc9171/). 

#### Undefined Notion of Clock Accuracy and Method of Synchronization
As described in Section 8 of [RFC 9171](https://datatracker.ietf.org/doc/rfc9171/), BPv7 makes use of absolute timestamps in many places, and includes provisions for nodes having inaccurate clocks. However, it states that nodes may be unaware that their clock is inaccurate and exhibit unexpected behavior, but does not say how to synchronize clocks within DTN, or how nodes can learn if their clocks are inaccurate. This is a major potential flaw and needs to be addressed in the future. Assuming that a network --- especially a (potentially) large unstable network with prevailing disconnectivity and asymmetric data rates like DTN --- is always time synchronized is a huge, or maybe unrealistic, assumption.

#### Absence of Routing Method. 
Information about routing and forwarding is provided in Sections 3.8 and 4.3 of [RFC 4838](https://datatracker.ietf.org/doc/rfc4838/), the first RFC describing the basic architecture of DTN. However, it provides only rough, high-level intuition on how routings in DTN can be modeled mathematically. There have not been any updates or new versions of the RFC since [RFC 4838](https://datatracker.ietf.org/doc/rfc4838/) appeared. Although [RFC 9171](https://datatracker.ietf.org/doc/rfc9171/) and [RFC 5050](https://datatracker.ietf.org/doc/rfc5050/) are strictly about a specific protocol, given that they assume the existence of a convergence layer protocol for handling node ID name resolution, some practical details about routing must at least be mentioned and described.



