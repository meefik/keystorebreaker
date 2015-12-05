KeystoreBreaker
===============

Copyright (C) Anton Skshidlevsky, [GPLv3](http://opensource.org/licenses/gpl-3.0.html)

Password guessing to java keystore.

Usage:

    $ java -jar KeystoreBreaker.jar <keystore file> <sequence> <first passwd> <last passwd> <number of threads>

Example:

    $ java -jar KeystoreBreaker.jar test.jks 0123456789abcdefghijklmnopqrstuvwxyz 000000 zzzzzz 4

    Keystore: test.jks
    Threads: 4
    Sequence: 01256789abcdefghijklmnopqrstuvwxyz
    First password: 000000
    Last password: zzzzzz
    Combinations: 1544804415
    Distribution by threads: 
    #0: 000000 - 2000ja
    #1: 2000ja - 10000j
    #2: 10000j - 0000jr
    #3: 0000jr - zzzzzz
    Processing: 
    #0: wxj000 / 2000ja  0% [ 20735 pwd/s ]
    #1: zhk0ja / 10000j  0% [ 21327 pwd/s ]
    #2: fij00j / 0000jr  0% [ 20187 pwd/s ]
    #3: uxj0jr / zzzzzz  0% [ 20713 pwd/s ]
    Total: 0% [ 82943 pwd/s ]  Time left: 0.05:10:23

Password for test.jks: 123456
