#!/bin/bash
#Parameter: <Host> <User> <Passwort> <Quellverzeichnis> <Zielverzeichnis>
rsync -t -r -v -u --rsh='sshpass -p '$3' ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no' $2@$1:$4 $5
