#!/bin/bash
#Fuehrt "ls" auf ueber SSH auf einem entfernten Rechner aus.
#Parameter: <Host> <User> <Passwort> <Verzeichnis>
sshpass -p $3 ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $2@$1 'cmd /c dir /A:-D /T:W' $4