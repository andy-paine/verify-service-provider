#!/usr/bin/env bash

if [[ -z "${KEY_NAME}" ]]; then
    echo "Key name: "
    read KEY_NAME
fi

if [[ -z "${KEY_TYPE}" ]]; then
    echo "Key type description (Signing/Encryption): " 
    read KEY_TYPE
fi

openssl genrsa -out "$KEY_NAME".key 2048

params="/C=GB/ST=London/L=Aldgate/O=Cabinet Office/OU=GDS/CN=SAML $KEY_TYPE"

openssl req -new -key "$KEY_NAME".key -out "$KEY_NAME".csr \
    -subj "$params"