FROM nvcr.io/nvidia/tritonserver:25.06-trtllm-python-py3

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        nvtop \
        python3 \
        python3-pip \
        procps \
        ca-certificates && \
    # Install awscli using pip
    pip3 install --no-cache-dir awscli && \
    # Clean up apt lists to keep the image size down
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*
