# Tutorial
- [K8s Zero to Hero](https://www.youtube.com/watch?v=MTHGoGUFpvE)
## Dependencies
- Docker
- heml: package manager/template engine
- kubectl: kubernetes client
- kubectx: kubectl add-on to make cluster switching easier
- kluctl: improve kubernetes configuration management
- KindD: deploy kubernetes within docker for local developement
- k9s: TUI for observing kubernetes clusters
- oras: OCI registry client
- yq: parsing and manipulating yaml
- minikube: tools for running single-node Kubernetes cluster on local machine

## Kubernetes
- `Kubectl`: cli interface with Kubernetes
- `Cluster`: 
    - Well a "cluster" is a collection of nodes
    - Can have 1 or multiple (odd number) of `control plane`
    - Can have multiple of `worker nodes`
- `Node`: 
    - A group of pod, usually 1 machine 1 node
    - Could be a `control plane` node
    - Or a `worker` node
- `Pod`: 
    - a group of `docker containers` 
- `Container`:
    - docker container
- `Controll plane node`
    - A special type of node 
    - `kube-apiserver`: interface for the control plane. Every communication walks thru here
    - `etcd`: distributed key-value store that stores the cluster's 
- `Worker node`
    - Has `kubelet` to interface with the `Controller API Server` to report status or manage containers 
    - Contains containers that user want to run
- Resource management
    - `ResourceQuota`
        - defining the maximum amount of resources per `namespace`
        - ```kubectl describe ns {namespace}```
    - `Requests`
        - Defining the minimum amount of resources a `Container` would get
        - Per container basis 
    - `Limits`
        - Defining the maximum amount of resources a `Container` would get
        - Per container basis 
    - cpu: 1 core == 1000 mili core
    - memory: Gi, M, ...
    - All `containter` resources are added up and compared with the `namespace` resource limit. If it goes over new container will not be created 
- `ReplicaSet`
    - Manage `Pods` and make sure they are always running to maintain availability (say always 10 pods)
        - Provide `self-healing`, which auto replaces `Pods` that fail or are deleted
        - Provide `auto scaling`, which spin up or spin down `Pods` as needed/set
    - Keep pods count by label count (if manually removed the label of 1 deployment pod, the deployment will make a new one, thinking the old one is dead)
- `Deployment`
    - Manage `ReplicaSets` and `Pods` through `ReplicaSets`
        - Provide `rolling updates` by slowly spin up `Pods` in new `ReplicaSet` and spin down `Pods` in old `ReplicaSet`.
        It also routes trafic to both `ReplicaSet` as that happen
        - Provide `versioning` by keeping track of multiple pass version of `ReplicaSet`
- `Service`
    - [Comparison](https://stackoverflow.com/questions/45079988/ingress-vs-load-balancer)
    - `ClusterIP`
        - Provide an internal load balancer/round robin that distribute works among all the `Pods` in a `Deployment`
    - `NodePort` (built upon `ClusterIP`)
        - Expose a port on every `Node` in the `Cluster`
    - `Load Balancer` (built upon `NodePort`)
        - Well, a load balancer than sits on top of `Ingress Controller`
        - Externally load ballance based on `Node` metrics
        - SHOUTOUT GOAT MDAY FOR THE LECTURES
    - `Ingress Controller` (built upon `LoadBalancer`)
        - Subscribe to `Ingress` objects. Incoming requests, based on the `host` header, will be routed to the associated `Service` 
        - `Ingress`
            - `Ingress` has a defined hostname and points to a `Service`
            - Has rules on incoming requests
- `Storage Class`
    - Teach the cluster how to create certain type of storage and handle it (AWS, Azure, GCP,...)
- `Persistent Volume`
    - What represents the storage itself
    - Varies vendor to vendor :(
- `Persistent Volume Claim`
    - This is the thing being mounted to the `Pod` itself 
    - Multiple `Pods` can mount the same `Persistent Volume Claim`
    - What represents the access to the `Persistent Volume`
    - One to one with `Persistent Volume`
- Probe
    - `Liveness probe`
        - Probe the container at an interval
        - After a certain number of failed attempts, it restart the container
    - `Readiness probe`
        - Same as `Liveness probe`, probe the container at an interval
        - It doesn't kill and restart the container, but wait patiently for the container to be ready
        - Until then, it will cut off the container from `Load Balancer`
- Mounting Volumes
    - Similar to docker

## minikube
- minikube start (start a single node cluster)
- minikube ssh
- minikube ip
- minikube addons enable ingress
    - Creating service ingress-nginx-controller in namespace ingress-nginx
    - This service is of type `NodePort` which just open a port for each node 
        => Any request to {node_ip}:{node_port} will be picked up by the ingress-nginx-controller
        => Then the ingress-controller would then resolve the ClusterIp/Service via all the subscrbied ingress objects 
        => All requests will be routed to internal ClusterIp/Service
- kubectl -n ingress-nginx patch svc ingress-nginx-controller -p '{"spec":{"type":"LoadBalancer"}}'
    - change ingress-nginx-controller to `LoadBalancer` type
    - This service is now `LoadBalancer`. 
        - This is for load balancing
        - And for Cloud Integration to automatically pick up and route an external IP to this + additional integration?
- minikube tunnel
    - Minikube Node already has an IP address, but making request directly to this would do nothing because there's nothing hosted on there
    - The Service/ClusterIP are hosted and available only inside the cluster in different namespaces
    - This command would then listen to requests to a range of ip addresses at the host level, route them to according internal ip addresses
    - 10.96.0.0/12
        - The "12" is for identifying the network. 
        - Binary form 00001010.01100000.00000000.00000000 
        - The first 12 bits are fixed => network portion
        - The last 20 bits are '0' => host portion 
- sudo ip route del 10.96.0.0/12
    - The reroute rule can stick around even when the tunnel is no longer running
    - This is to clear the reroute rule


==> To conclude, to expose the internal service to host level (Non production)
    - You could just run `minikube addons enable ingress` and hit `{node_ip}:{node_port}` with the correct `Host` header
    - Or you could run `minikube tunnel` which will expose everything internally to host level (Just hit the service Ip directly, No Host name/Ingress Object needed)
    - Both is redundant
    

## Kubectl
- `-w` for wait, update as changes happen
- kubectl api-resources
    - to list all API resources along with associated API group and version
- kubectl apply -f {manifest.yaml}
- kubectl get nodes/pods
- kubectl get namespaces
    - kube-system (etcd, api server, scheduler,...)
- kubectl delete pods/resourcequota {name}
- kubectl describe ns (namespaces)/pods/nodes => Go to for debugging
- kubectl logs {pod_name} => also go to for debuggin
    - -c {container_name}
    - --all-containers
    - -f and --since {time like 10s}
- -L {label key} to add a column of label
- concept of label, can filter by label values
- concept of deployment of pods
    - kubectl get deployment
    - kubectl get replicasets -L {label_key}
    - kubectl rollout history deploy {deployment_name}
    - kubectl rollout undo deploy {deployment_name}
- concept of persistent storage (aws linked)
    - kubectl get pv,pvc
- concept of a service
- concept of network policy
- kubectl get pod {pod_name} -o jsonpath='{}' | jq .spec.containers[].name


## Kubernetes Metrics Server
```
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
```
If use with `minikube`, enable addons
```
minikube addons enable metrics-server
```
This is an extra pod that do autoscaling (horizontal or vertical)

## Helm
- Template engine
- Bundle a bunch of manifest file with NO VALUES but PLACEHOLDERS for values into a `chart` aka tarball file
- Take a `chart`, unzip it and use golang substitution to insert values into the template
- Then apply that to `kubernetes` to create EVERTHING

## Devbox/Nix
- Nix is a package manager for reproducible installations (Tho can be used like docker)
- Devbox is a wrapper around it for newbies like me :(


