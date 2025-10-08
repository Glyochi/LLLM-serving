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
### General Learning
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
- `Operator`
    - [Resource](https://www.youtube.com/watch?v=QoDqxm7ybLc)
    - Used for automating deployment of statefull app (Not a simple restart container anymore)
    - Maintained by community, make sures all instances are synced + deconstruct/construct happens in the correct order
    - `StatefulSet`
        - The core instance, managed by the `Operator`
        - `AlertManager` is part of prometheus operator stack
- `DaemonSet`
    - Runs on every `Worker Node`
    - Prometheus's node-exporter translates `Worker Node` metrics to Prometheus metrics
- `CRD`/`Custom Resource Definition`
- `Exporter`
    - Format metrics data for prometheus, separated from the main metrics source
    - Opensource somewhere out there

### [IMPORTANT] Quirky K8s
- How K8s passes args
    - Each list item in a yaml array is `one argv token`, which means `--trace-config mode=opentelemetry` is one token with a space in the middle
    - This will cause confusion for passing arguments to a command, which wants to see two tokens `--trace-config` and `mode=opentelemetry`
    - To bypass this, pass it in as `--trace-config=mode=opentelemetry`
- How K8s resolve domain name/[concept of `ndots`](https://www.reddit.com/r/kubernetes/comments/duj86x/help_understanding_how_dns_works_and_what_ndots/)
    - in each pod's `/etc/resolv.conf`, it has something like
        ```
        nameserver 10.231.10.10
        search rando-namespace.svc.cluster.local svc.cluster.local cluster.local
        options ndots:5
        ```
    - This has `ndots` value of 5
    - Say i have a service call `app-server` running in namespace `other-ns`, and the full service name is `app-server.other-ns.svc.cluster.local` 
    - The reason i do `nslookup app-server.other-ns` and get an `ip address` back is because i'm doing a DNS lookup for a string with `1 dot in it`, which is less than `5 dots from the above settings` 
        - It will attempt the following lookups 
        - `app-server.other-ns.rando-namespace.svc.cluster.local`
        - `app-server.other-ns.svc.cluster.local` (will resolve here)
        - `app-server.other-ns.cluster.local`
        - `app-server.other-ns.` (absolute domain name)
    - [Quirky] with this knowledge in mind, there's a weird case where `triton.observability.svc.cluster.local` doesnt get resolved but `triton.observability.svc.clutser.local.` with the trailing `.` does 
        - This is because the second one has 5 dots, which means it wont try all the possible combinations like above => absolute reference to the service
        - But it also means that some combinations of the tried out domain is causing issue? Why?
    


        

## minikube
- minikube start (start a single node cluster)
- minikube start --driver=docker --gpus=all(Run in a docker container instead of VM)
- minikube addons enable nvidia-gpu-device-plugin (nvidia stuffs)
    - kubectl describe node {node_name} | grep -A2 nvidia.com/gpu
- minikube status
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
- kubectl get all
- kubectl get nodes/pods
- kubectl get namespaces
    - kube-system (etcd, api server, scheduler,...)
- kubectl delete pods/resourcequota {name}
- kubectl describe ns (namespaces)/pods/nodes => Go to for debugging
- kubectl logs {pod_name} => also go to for debuggin
    - -c {container_name}
    - --all-containers
    - -f and --since {time like 10s}
- kubectl exec -it {pod_name} -- /bin/bash
- -L {label_key} to add a column of label
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
- An image with curl for debug: kubectl run test -it --rm --image=curlimages/curl --restart=Never -- sh
- kubectl port-forward {service-name} 9090 --address='0.0.0.0'

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
### Prometheus installation
```
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
```

### Grafana installation
```
helm repo add grafana https://grafana.github.io/helm-charts
```

### Otel Collector installation
```
helm repo add open-telemetry https://open-telemetry.github.io/opentelemetry-helm-charts
```

### Grafana installation
```
```

### Triton installation
- Triton Helm Chart can be found [here](https://catalog.ngc.nvidia.com/orgs/nvidia/helm-charts/tritoninferenceserver?version=1.0.0)
- However, it seems to be outdated. The current helm chart for triton is based on this with modification made to get it to work with triton 25.06
    - The binary name is now `tritonserver` and not `trtserver` => changing flags. Refer to `README.md` in `inference folder` in this repo
    - [Resources on liveness/readiness probe](https://github.com/triton-inference-server/server/issues/6469)


### General learning
- helm show values {chart_name} > {some_yaml_file}
    - Would show all the overidable params
- helm chart quirks 
    - Automatically expose `receiver port`
    - But doesn't automatically expose `exporter ports`
    - Have to create a service myself that points to the exposed ports for `exporter`
- helm simple chart
    - `Chart.yaml`: metadata about the chart (helm create)
    - `values.yaml`: default configuration values of the chart, these can be overriden during installation
    - `templates folder`: 
        - Folder that contains all templates that uses values from `values.yaml` to create the final Kube manifest files
        - This is where Golang substitution syntax comes in
- `Grafana` is reading from `Prometheus`
- `Prometheus` is scraping `Otel Collector` and `Triton`
- `Tempo` is writing to `Prometheus` because its [better than scraping?](https://github.com/grafana/tempo/discussions/2621#discussioncomment-6377004)
- `APP` is writing to `Otel Collector`
- `Triton` is [chilling](https://catalog.ngc.nvidia.com/orgs/nvidia/helm-charts/tritoninferenceserver?version=1.0.0) 



- Helpful commands
    - helm search repo {repo_name} (etc. nvidia/triton)

## Devbox/Nix
- Nix is a package manager for reproducible installations (Tho can be used like docker)
- Devbox is a wrapper around it for newbies like me :(


## ECK/ECS/K8s with EC2/Fargate
- [Resource](https://www.youtube.com/watch?v=AYAh6YDXuho)
- `ECS`/`Elastic Container Service`
    - Elastic Container Service
    - AWS native Control plane
    - Manages the Containers for us
    - Containers run on VM machines/`EC2` instances that the Containers run on
    - `EC2` instances are still managed by user
        - Has Docker Agent
        - ECS Agent (for similar purpose to Kubelet)
        - Has to be created manually and linked to `ECS` ???
    - Less complex than K8s Cluster and `ECS control plane` is free
- `EC2`
    - On-Demand Instance
        - Fixed price for the type of hardware (g4dn.xlarge vs g5.xlarge)
        - Guaranteed availability => No interuptions as long as we keep paying the bills (or no outages)
    - Spot Instance
        - Up to 90% off On-Demands prices based on supply/demand
        - Using spare AWS capacity, can be terminated by AWS with a two-minute warning when the capacitiy is claimed by someone else
        - Has to implement workaround interruption
    - Has full control over what is on the `EC2` instance
        - OS
        - Packages/Dependencies
        - Configurations
        - ...
- `Fargate`
    - Alternative to creating `EC2` instances manually
    - Serveless way to launch containers, by spinning up VM on demand
    - Where that containers end up, is not determined by us, just whatever server 
    provisioned by `Fargate` that is free and enough resources to run the container
    - Cheap because only pay for what the containers is using
        - how long is it running
        - how much capacity is being consumed
    - Has less control than `EC2` instances because VM is being managed by `Fargate`, you can only control the containers
- `EKS`/`Elastic Kubernetes Service`
    - Manage K8s Cluster on AWS
    - Continue to use K8s API + all the community tools (Helm,...)
    - "Easily" migrate to other platforms, or at least easier than `ECS` (If not dependent on a lot of AWS services)
    - How it works
        - Control Plane is AWS Native
            - `EKS` deploys and manages `Kubernetes Master Nodes`
            - K8s Master Services already on them Nodes (Controller stuffs for managing pods/containers)
            - Master Nodes (including etcd data) automatic replicated accross Availability Zones => HIGH AVAILABILITY
            => Don't worry about it
        - Worker Nodes
            - Has to create them `EC2` instances (Da Compute Fleet) manually 
            - Or use `Nodegroup` (semi-managed)
                - Creates, deletes `EC2` instances for you, but you need to configure it (Huh kinda like what `Replicaset` is to `Pod`)
                - Worker nodes managed by `Nodegroup` will get all the processes necessary installed on them (Container runtime, Worker processes,...)
                - Still have to configure other stuffs like auto scaling
            - Or let them be managed by `Fargate` (fully-managed)
            - Then connect them to `EKS`
            - Has Docker Agent
            - Has K8s Processes (Kubelet) to communicate with the control plane
        - Once the cluster is configured, we can connect to the cluster using `kubectl` and start deploying containers
- [NOTE] `ECS` and `EKS` can also have a mix of `EC2` and `Fargate` as well, doesn't have to be just one
- `ECR`/`Elastic Container Registry`
    - App images registry
    - Easily integrated with `ECS`/`EKS`
    - Some CI/CD integration as well which is noice


