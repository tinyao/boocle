#! /bin/sh

cp ./BookCircle-v1.3.1.apk bookcircle.apk

echo "\n1. 上传文件到阿里云..."

scp ./BookCircle-v1.3.1.apk ./bookcircle.apk ./version-info.json root@ali.czzz.org:/alidata/www/book/package

echo "\n2. 复制文件到 bookcircle.github.com/package ..."

cp ./BookCircle-v1.3.1.apk ./bookcircle.apk ~/Coding/github/bookcircle.github.com/package/

echo "\ncomplete !\n"

