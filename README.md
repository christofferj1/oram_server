# ORAM SERVER

ORAM is short for Oblivious RAM, which is a cryptographic primitive used to simulate RAM and hide the access pattern. It can be used when data is stored on an online server, and you want to make sure the server can't tell what block of data you are accessing or if you are reading or writing. It is an additional layer of security put on top when conventional encryption is not enough.

This repository contains the code used for experiments in connections with my work on my Master's Thesis. This is the server-side software that works together with the [client-side](https://github.com/christofferj1/ORAM).

### Run with gradle

If you have gradle installed, you can build and run it with the commands *gradle build* and *gradle run*.

If you do not have gradle installed, you can use the wrapper scripts, *gradlew* for Unix based systems and *gradlew.bat* for Windows based. These scripts should also be used with the "build" and "run" commands. They are build with gradle version 5.3.

### Using the software

This software sets up the server environment, by creating the dummy blocks needed to run the ORAM algorithms and acts as the server. When running the software, you use a command line interface to

1. choose how many layers of ORAM you want to run. If you choose one, you decide the size, if you choose more, the sizes are predefined.
2. choose which kind of ORAM you would like to run, Lookahead ORAM, Path ORAM or Trivial ORAM. If you have chosen to run multiple layers of recursive ORAM, you must choose an ORAM for each layer, starting with the biggest. If you choose either Trivial or the specialized Lookahead ORAM, no more layers need to be chosen, as the ORAMs don't need further layers for the position map. ([l/lt/p/t] means you should write e.g. 'l' and hit enter.)

It then creates the blocks needed, and are ready to connect to a client. The client needs the IP-address of the server, the port is hard coded to 59595.