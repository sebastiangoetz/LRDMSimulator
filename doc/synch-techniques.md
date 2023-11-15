# Synchronization techniques

## anti-entropy mechanism [^1]
- write logs with version vector
- two nodes exchange logs and vector and unite them

## Named Data Networking [^2] [^3]
- each piece of data is assigned a semantically meaningful name
- client express interests
- modules:
  - FIB: list of name prefixes
  - PIT: track of all Interests the module has forwarded
  - CS: caches passing-by data packets
  - forwarding strategy: makes interest forwarding
- when Interest reaches node with data it gets resent in reverse path to original requester
- each node in bettwenn caches data package in own CS to satisfy future requests

## Comprehensive Synchronization: [^4]
- whole data gets transmitted to find differences

## Status flag synchronization: [^4]
- flags if data was created, modified or deleted
- synch with more clients does not work well

## Timestamp synchronization: (Lamport Clock) [^4]
- timestamp when data was last time synchronized with other client
- when synch again only send changed item since last synch

## Mathematical synchronization [^4] 
- use of mathematical properties of the data

## Synchronization based on Message Digest [^4]
- another form of mathematical synch
- only uses SQL operations

## Log synchronization: [^4]
- every change executed as transaction
- saved in log
- transaction only replayed in other client

## synchronization algorithms [^4]
- The RSYNC Algorithm
- SAMD Synchronization Algorithm

## indirect replication alg. [^5]
- use of decode and encode
- data is split into data blocks

## Clocks [^6]

## Distributed Spanning Tree [^7]

## single-source of trust [^8]

## Polling over the source of truth and syncing data into external systems [^8]

## Synchronous change in other sources for every change in the source of truth [^8]

## Change Data Capture [^8]
- CDC systems monitor the source of truth for data modifications, including inserts, updates, and deletes
- Once the changes are captured, the CDC system propagates them to the target systems
- The target systems receive the captured changes and apply them to their respective datasets
- The changes are typically processed in the same order they occurred, ensuring consistency across the data sources

## Event-Driven Architecture [^8]
- When a change occurs in the source of truth, an event is generated to represent that change
- The generated events are propagated through an event-driven infrastructure, which can include message brokers, event buses, or event streaming platforms
- Upon receiving an event, the target systems process it to update their data accordingly

## Consensus algorithms [^9]
- to ensure that all nodes agree on a certain value or decision

## two-phase commit protocol [^9]


[^1] https://citeseerx.ist.psu.edu/document?repid=rep1&type=pdf&doi=97e26cc618e912ecc1f1f17fcced78ea68b1dff0
[^2] https://ieeexplore.ieee.org/stamp/stamp.jsp?tp=&arnumber=9077503
[^3] https://users.cs.fiu.edu/~afanasyev/assets/papers/tr-shang2017sync-survey.pdf
[^4] https://ieeexplore.ieee.org/abstract/document/7569323
[^5] https://pdf.sciencedirectassets.com/271503/1-s2.0-S0898122106X08726/1-s2.0-S0898122106001313/main.pdf?X-Amz-Security-Token=IQoJb3JpZ2luX2VjEG8aCXVzLWVhc3QtMSJGMEQCIHw8%2BnY%2FngraBPyZxlZxvtZeBQ1MBVNeirmNCQki5mFsAiAu05tyuKonbDY6WVNDbBhD9VpEvEQcNtVQy4Uh2ZVD%2Byq8BQin%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F8BEAUaDDA1OTAwMzU0Njg2NSIMH99kqfz4ggqEDgdLKpAFx3i8S7NU8CADX4dk3ZqTUX8GrzySdH9njRmX0kY9jJYi63bSWTDgotYfbBfL5Zoy4dYDOPJUA7IRmpAdILE6fvuuA6Npg0U2BlgMFvXASVensWkktsOQ7XZsMmh9QbHBSSXbG88Dj6S1NWzrFL9%2FDcwsBtaZ4Id5k0xCaJNHb9SL2CD%2F9INNseriOM5K7oz2BdNY2Umvou%2Bu98g8%2Fm2Nzbk04AYJcn6ZvtmdVgqm5%2F%2FsZt%2BCnoN5BaKuHstLp%2Biz9wFX2LCw29AtdNCh6t7bsk9CpPStXPoN6t9RsHBY89Rzqeh6ICL29p2EZumifUsidU0EkbtuHWPfuoT5eK%2F8%2F6D9GRJFAU5PISAw5wLI5CoI0alGoDRouRXs13v4xb6noSN%2BPkQkCEJ%2Bvg9NGrQEvluHDgMeiOCbKGRfBwGPwLra%2BUM1JR99vQE1fh9UCbC%2BxyfPu4oOXdKVVG%2FdXoSjLJZ2x8NCUhfMadlyn5RnMrTX5%2FfCUYqkqxAjpDS6MBAruCnyLw0y54WU%2BV1tVJSHWxdXXXx92OxAv0%2FVMRaGz6h5g45x9%2BJ3b7Gwnk9TfAdKy6o74loExR5%2FdBtbrQfyfDpn%2F6yuJPGsJN%2Brd81T%2Bow6dnOk1NARApYDw66y8FWFyx36O48ldWeYX9HaS8FVYcl5dPGSGctm8eB0CDy7QAqv0grUc3Uzl%2BwZDh8Xdjm%2FNMOpxMt0lPLCOd%2BIJb4nL8hYGVCWPC6JWnOFXqLdhRhoktm3XA1dZWDmm6%2FYM8d3HmXCOVH8ZKq79vNytiGW%2BnxlXGJdYf0WQ8%2BnABLE9B5SHeiUFtR2IkikU56NpPsMRSkNSLef%2F8JGYoAlcnH6oZEOmufnjDCIjIXAfwVzt6QwuYmpqgY6sgGPYRjJFRFEoFlW8BqpPHNnqcqk06muBDqV%2BBzkVnMyk4tzPKkDMd6MIPzYAeklo%2Bt0YrSuS4j7gDfqEee4btXAddTif%2BLH0Yd8fBXxtAYUz9QdCnq4Ol3Yg0KEAogtoG4V6eZd1yw9jjr0lgtxT1bzJ5ujEdJhf7qX5mCxUzzqC3NlydAzcMC6sTUEQlf2qhBe8JYRKyyDNrZfHADt%2BiGlfCbnZKlquQgraw23%2Flj%2F7%2F56&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20231107T144231Z&X-Amz-SignedHeaders=host&X-Amz-Expires=300&X-Amz-Credential=ASIAQ3PHCVTY4YPLQWOD%2F20231107%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Signature=7cb8fc775416393b1e59b2e734f23e263ed7a01221110ffec7ac572df8c331e5&hash=9b1eafa4b353633ec6825af7c7b6e29997f714c63da6e05d18bc36181b6e9d75&host=68042c943591013ac2b2430a89b270f6af2c76d8dfd086a07176afe7c76c2c61&pii=S0898122106001313&tid=spdf-32abb9b6-1d94-4b05-881c-a4eeb61dc71b&sid=4e8f65b45e906043128a6ff6a5d9a2a52da8gxrqb&type=client&tsoh=d3d3LnNjaWVuY2VkaXJlY3QuY29t&ua=1e035e555c595756065251&rr=822657e30de230cc&cc=de
[^6] https://ieeexplore.ieee.org/stamp/stamp.jsp?tp=&arnumber=4607217
[^7] https://onlinelibrary.wiley.com/doi/epdf/10.1002/dac.3996?saml_referrer
[^8] https://medium.com/@ketansomvanshi007/synchronizing-multiple-data-sources-in-a-distributed-system-ensuring-consistency-and-accuracy-8e087b3b5ed6
[^9] https://blog.sofwancoder.com/distributed-systems-synchronisation-in-complex-systems