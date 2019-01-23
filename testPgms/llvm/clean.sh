#!/bin/bash
for f in */; do cd $f;make clean;cd ../;done
