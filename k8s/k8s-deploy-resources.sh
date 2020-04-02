#!/bin/bash
set -e

kubectl apply -f tb-namespace.yml
kubectl config set-context $(kubectl config current-context) --namespace=thingsboard
kubectl apply -f tb-node-configmap.yml
kubectl apply -f tb-mqtt-transport-configmap.yml
kubectl apply -f tb-http-transport-configmap.yml
kubectl apply -f tb-coap-transport-configmap.yml
kubectl apply -f thingsboard.yml
