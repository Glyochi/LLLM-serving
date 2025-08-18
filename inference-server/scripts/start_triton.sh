MODEL_FOLDER="/opt/tritonserver/projects/model-repository"
# python3 /app/scripts/launch_triton_server.py --world_size=1 --model_repo=${MODEL_FOLDER}

tritonserver \
  --model-repository=$MODEL_FOLDER \
  --log-verbose=1 \
  --exit-on-error=false

