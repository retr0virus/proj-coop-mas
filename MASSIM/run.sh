#!/bin/bash
RUNCP=bin:lib/mason.16.jar

java -cp ${RUNCP} sim.app.snr.SearchAndRescueWithUI ../files/1000x1000_maze2_10.png
