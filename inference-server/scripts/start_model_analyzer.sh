IS_FRESH_RUN=false

if [[ "${1:-}" == "--fresh" ]]; then
  IS_FRESH_RUN=true
fi 

if [[ "$IS_FRESH_RUN" == "true" ]]; then
  echo
  echo "FRESH RUN: DELETING PREVIOUS CHECKPOINTS..."
  echo
  sleep 2
  rm /opt/triton-model-analyzer/checkpoints/*
  rm -r /opt/triton-model-analyzer/artifacts/*
  rm perf_analyzer_error.log
fi

echo
echo "STARTING MODEL ANALYZER..."
echo
model-analyzer profile -f ./scripts/model_analyzer_config.yaml

echo
echo "reports/ and results/ are ready"

