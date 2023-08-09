#!/bin/bash

# Usage: ./minio-upload my-bucket my-file.jpg


bucket=$1
file=$2

host=178.140.0.246
port=9000

s3_key=frm_admin
s3_secret=frm12345!

resource="/${bucket}/${file}"
content_type="application/octet-stream"
date=`date -R`
_signature="PUT\n\n${content_type}\n${date}\n${resource}"
signature=`echo -en ${_signature} | openssl sha1 -hmac ${s3_secret} -binary | base64`

curl -X PUT -T "${file}" \
          -H "Host: ${host}" \
          -H "Port: ${port}" \
          -H "Date: ${date}" \
          -H "Content-Type: ${content_type}" \
          -H "Authorization: AWS ${s3_key}:${signature}" \
          http://${host}:${port}${resource}