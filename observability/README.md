# Docker
- Service A can access service B without B exposing port to host machine, just by service_name:port (if under same network, thats default if no network defined)
- Exposed posts
    - Gateway: 8090
    - Triton metrics: 8002
    - OTEL collector: 
        - 4317 (receive data port grpc)
        - 9464 (received/collected data, ready to serve prometheus)
        - 8888 (otel collecter's own services, for prometheus)
    - Tempo:
        - 3200 (export for UI)
    - Prometheus
        - 9090 (export for UI)
    - Grafana
        - 3000 (hosting UI)

# OpenTelemetry Collector
## Role
- To collect data from Spring app, and then distribute to other backend database
## [Config](https://opentelemetry.io/docs/collector/configuration/) 
- `Receivers`: Configure how and where to collect data
- `Processors`: Configure how to process the data
- `Exporters`: Configure how to send data (pull/push based)
- `Connectors`: Join pipelines together, more complex stuff
- `Service`: Enabling/disabling components above

# Prometheus
## [Config](https://prometheus.io/docs/prometheus/latest/configuration/configuration/) 

# Tempo
## [Config](https://grafana.com/docs/tempo/latest/configuration/) 
## Local storage
- Tempo runs as user 10001, and docker compose creates volume as root => need to chown the volumes
- local storage should be linked to `var/tempo`
- [Help with setting up](https://github.com/monitoringartist/grafana-opentelemetry)


# Loki
## [Config](https://grafana.com/docs/loki/latest/configure/)

# Grafana
## [Config](https://grafana.com/docs/grafana/latest/setup-grafana/configure-grafana/)

