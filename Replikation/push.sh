#!/bin/bash
#Parameter: <Host> <User> <Passwort> <Quellverzeichnis> <Zielverzeichnis>
rsync --delete -t -r -v --rsh='sshpass -p '$3' ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no' $5 $2@$1:$4