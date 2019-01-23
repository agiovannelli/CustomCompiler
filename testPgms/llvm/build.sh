#!/bin/bash
for f in */; do cd $f;make;cd ../;done
