#!/bin/bash

make clean
make
make run
make compile
cat out.j
java GoProgram
