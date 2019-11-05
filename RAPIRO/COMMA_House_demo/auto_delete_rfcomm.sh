#!/bin/bash

chk_num=$(ps -ef | grep "sudo rfcomm listen /dev/rfcomm0 22" | grep -v grep | wc -l)

if [ ${chk_num} = "2" ]; then
  pid=$(ps -ef | grep "sudo rfcomm listen /dev/rfcomm0 22" | grep -v grep | head -n 1)
  pid=$(echo ${pid} | cut -f 2 -d " ")

  echo kill ${pid} >> rfcommkill.log 2>&1
  sudo kill -9 ${pid} >> rfcommkill.log 2>&1

  pid=$(ps -ef | grep "rfcomm listen /dev/rfcomm0 22" | grep -v grep | head -n 1)
  pid=$(echo ${pid} | cut -f 2 -d " ")

  echo kill ${pid} >> rfcommkill.log 2>&1
  sudo kill -9 ${pid} >> rfcommkill.log 2>&1
fi
