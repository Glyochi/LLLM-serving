MODEL_FOLDER="/opt/tritonserver/projects/model-repository"
# python3 /app/scripts/launch_triton_server.py --world_size=1 --model_repo=${MODEL_FOLDER}

tritonserver \
  --model-repository=$MODEL_FOLDER \
  --log-verbose=1 \
  --exit-on-error=false \
  --trace-config mode=opentelemetry \
  --trace-config rate=1 \
  --trace-config opentelemetry,url=http://otel-collector:4318/v1/traces \
  --trace-config level=TIMESTAMPS \
  --trace-config opentelemetry,resource=service.name=triton

