#!/usr/bin/bash

# Just run
# java -cp ".:lib/commons-cli-1.5.0.jar:lib/gson-2.8.9.jar:" MyCity.java -s WA Seattle

# Build
javac -cp ".:lib/commons-cli-1.5.0.jar:lib/gson-2.8.9.jar:" MyCity.java
# Run
java -cp ".:lib/commons-cli-1.5.0.jar:lib/gson-2.8.9.jar:" MyCity -s WA Seattle
