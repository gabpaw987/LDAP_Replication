#!/bin/bash
#Fuehrt "ls" auf ueber SSH auf einem entfernten Rechner aus.
#Parameter: <Host> <User> <Passwort> <Verzeichnis>
sshpass -p $3 ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $2@$1 'ls --time-style=long-iso -l ' $4 | grep -v ^d
