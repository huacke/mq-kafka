#!/bin/bash

username=$1

password=$2

hostname=$3

pwdfile="/tmp/$(cat /proc/sys/kernel/random/uuid)"

touch ${pwdfile}

echo -e ${password} >${pwdfile}

sshpass -f ${pwdfile} ssh-copy-id -o StrictHostKeyChecking=no ${username}@${hostname}