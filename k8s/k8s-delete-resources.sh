#!/bin/bash
set -e

kubectl config set-context $(kubectl config current-context) --namespace=thingsboard
kubectl delete -f thingsboard.yml
