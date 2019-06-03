# ORAM SERVER

This repository contains the code used for experiments in connections with my work on my Master's Thesis. This is the server-side software that works together with the [client-side](https://github.com/christofferj1/ORAM).

### Run with gradle

If you have gradle installed, you can build and run it with the commands *gradle build* and *gradle run*.

If you do not have gradle installed, you can use the wrapper scripts, *gradlew* for Unix based systems and *gradlew.bat* for Windows based.

### Using the software

This software sets up the server environment, by creating the dummy blocks needed to run the ORAM algorithms and acts as the server. When running the software, you use a command line interface to

1. choose how many layers of ORAM you want to run.
2. choose which kind of ORAM you would like to run, Lookahead ORAM, Path ORAM or Trivial ORAM. If you have chosen to run multiple layers of recursive ORAM, you must choose an ORAM for each layer, starting with the biggest. If you choose either Trivial or the specialized Lookahead ORAM, no more layers need to be chosen, as the ORAMs don't need further layers for the position map. ([l/lt/p/t] means you should write e.g. 'l' and hit enter.)

It then creates the blocks needed. At last it prints the local IP address, which can be used to connect to the server, if both server and client runs on the same network.