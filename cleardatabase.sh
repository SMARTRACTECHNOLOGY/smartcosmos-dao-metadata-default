#!/usr/bin/env bash

mysql -u root -pchangeme <<-EOF
DROP DATABASE devkit;
CREATE DATABASE devkit;
GRANT ALL PRIVILEGES ON devkit.* TO 'cosmos'@'localhost' \
IDENTIFIED BY 'dev';
FLUSH PRIVILEGES;
EOF

if [ $? -eq 0 ]; then
  printf "Successful.\n"
fi
