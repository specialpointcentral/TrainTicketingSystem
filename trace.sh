#!/bin/sh
cd src/main/java
javac -encoding UTF-8 -cp . ticketingsystem/Trace.java
java -cp . ticketingsystem/Trace