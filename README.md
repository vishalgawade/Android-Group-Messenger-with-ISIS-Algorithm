<p align="center">
Large-Scale Distributed Systems</br>
Group Messenger with TOTAL and FIFO ordering</br>
modified ISIS algorithm</br>
CSE 586 - Spring 2019
</p>

---------------

![Algo](https://github.com/vishalgawade/CSE586/blob/master/ISIS_Algorithm_Working.gif)
---------------
![Algo2](https://github.com/vishalgawade/CSE586/blob/master/1.png)

Goal
------
Implement advanced concepts that add ordering guarantees to the group messaging. Specifically, we need to develop a **Group Messaging Android application with decentralized [TOTAL and FIFO ordering] guarantees**. 

**NOTE**</br>
[**ISIS system**](http://webcache.googleusercontent.com/search?q=cache:3rOwsftQvYoJ:www.cs.cornell.edu/home/rvr/sys/p79-birman.pdf+&cd=1&hl=en&ct=clnk&gl=us&client=safari) developed at Cornell (Birman, 1993; Birman and Joseph, 1987a, 1987b; and Birman and Van Renesse, 1994) provides totally ordered multicast delivery algorithm. However in this assignment we need to design and **implement a modified version of ISIS algorithm** that guarantees both TOTAL and FIFO ordering and provides a persistent Key-Value storage with **ordering remaining intact even in case of application failures**.


References
---------------
I have taken reference from below sources to design a modified version of ISIS algorithm: -</br>
1. [Lecture slides](http://www.cse.buffalo.edu/~stevko/courses/cse486/spring19/lectures/12-multicast2.pdf)</br>
2. [Distributed Systems: Concepts and Design (5th Edition) ](https://www.pearsonhighered.com/program/Coulouris-Distributed-Systems-Concepts-and-Design-5th-Edition/PGM85317.html)</br>
3. [Cloud Computing Concepts - University of Illinois at Urbana-Champaign](https://www.coursera.org/learn/cloud-computing)


What is TOTAL Ordering
----------------------------------
**Every process delivers all messages in the same order**. Here we don't care about any causal relationship of messages and as long as every process follows a single order we are fine.

For example: -</br>
> –  **P1:** m0, m1, m2</br>
> –  **P2:** m3, m4, m5</br>
> –  **P3:** m6, m7, m8</br>

One of the TOTAL ordering would be: - </br>
> –  **P1:** m8, m1, m2, m4, m3, m5, m6, m0, m7</br>
> –  **P2:** m8, m1, m2, m4, m3, m5, m6, m0, m7</br>
> –  **P3:** m8, m1, m2, m4, m3, m5, m6, m0, m7</br>


What is FIFO Ordering
-------------------------------
The message delivery order at each process should **preserve the message sending order** from every process. But **each process can deliver in a different order**.

For example: -</br>
> –  **P1:** m0, m1, m2</br>
> –  **P2:** m3, m4, m5</br>
> –  **P3:** m6, m7, m8</br>

One of the FIFO ordering would be: - </br>
> –  **P1:** m0, m3, m6, m1, m2, m4, m7, m5, m8</br>
> –  **P2:** m3, m0, m1, m4, m6, m7, m5, m2, m8</br>
> –  **P3:** m6, m7, m8, m0, m1, m2, m3, m4, m5</br>


What is TOTAL and FIFO Ordering
-----------------------------------------------
The message delivery order at each process should **preserve the message sending order** from every process and  **every process delivers all messages in the same order**.

For example: -</br>
> –  **P1:** m0, m1, m2</br>
> –  **P2:** m3, m4, m5</br>
> –  **P3:** m6, m7, m8</br>

One of the TOTAL and FIFO ordering would be: - </br>
> –  **P1:** m0, m3, m6, m1, m2, m4, m7, m5, m8</br>
> –  **P2:** m0, m3, m6, m1, m2, m4, m7, m5, m8</br>
> –  **P3:** m0, m3, m6, m1, m2, m4, m7, m5, m8</br>


Requirements for the design
----------------------------------------
This project implements a modified version of ISIS algorithm based on below design guidelines: -

> 1. Our app should multicast every user-entered message to all app instances (including the one that is sending the message). 
> 2. Our app should use B-multicast. It should not implement R-multicast.
> 3. **We need to come up with an algorithm that provides a TOTAL-FIFO ordering under a failure**.
> 4. There will be **at most one failure of an app instance in the middle of execution**.  When a failure happens, the app instance will never come back during a run.
> 5. Each message should be used to detect a node failure.
> 6. **Do not just rely on socket creation or connect status to determine if a node has failed**. Due to the Android emulator networking setup, it is not safe to just rely on socket creation or connect status to judge node failures.
> 7. We cannot assume which app instance will fail. In fact, the grader will run our group messenger multiple times and each time it will kill a different instance. Thus, we should not rely on chance (e.g., randomly picking a central sequencer) to handle failures. Instead, we should implement a decentralized algorithm (e.g., something based on ISIS).
> 8. When handling a failure, it is important to make sure that our implementation does not stall. After we detect a failure, we need to clean up any state related to it, and move on.
> 9. When there is a node failure, the grader will not check how you are ordering the messages sent by the failed node. **Please refer to the testing section below for details**.
> 10. Every message should be stored in our provider individually by all app instances. Each message should be stored as a < key, value > pair. The key should be the final delivery sequence number for the message (as a string); the value should be the actual message (again, as a string). The delivery sequence number should start from 0 and increase by 1 for each message.
> 11. We have fixed the ports & sockets: -</br>
	a) Our app opens one server socket that listens on **Port 10000**.</br>
	b) The grading will use 5 AVDs. The redirection ports are **11108, 11112, 11116, 11120, and 11124**.


Testing
----------
We should **implement a decentralized algorithm** to handle failures correctly. This means that we should not implement a centralized algorithm. This also means that we should not implement any variation of a centralized algorithm that randomly picks a central node. 

**Phase 1 --- Testing without any failure**</br>
In this phase, all the messages should be delivered in a TOTAL-FIFO order. For each message, all the delivery sequence numbers should be the same across processes.

**Phase 2 --- Testing with a failure**</br>
In this phase, all the messages sent by live nodes should be delivered in a TOTAL-FIFO order. Due to a failure, the delivery sequence numbers might go out of sync if some nodes deliver messages from the failed node, while others do not. This is OK; the TOTAL-FIFO ordering guarantees for the messages sent by live nodes. 

Credits
-------
This project contains scripts and other related material that is developed by [**Networked Systems Research Group**](https://nsr.cse.buffalo.edu) at **[University of Buffalo, The State University of New York](http://www.cse.buffalo.edu)**.

I acknowledge and grateful to [**Professor Steve ko**](https://nsr.cse.buffalo.edu/?page_id=272) for their continuous support throughout the Course ([**CSE 586**](http://www.cse.buffalo.edu/~stevko/courses/cse486/spring19/)) that helped me learn the skills of Large Scale Distributed Systems and develop a **modified version of ISIS algorithm that guarantees both TOTAL and FIFO ordering**.


Developer
---------
Vishal Gawade (vgawade@buffalo.edu)</br>
