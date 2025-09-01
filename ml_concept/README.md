# Question
- Why for determining how similar Q and K is, why the dot product is good enough? What is cosine similarity? Why is it not used instead?

# Transformer Notes 
- Residual connections
    - Easier for training 
        - Params in the attention heads are less constraints. 
        - They are free to 'learn' the relationship between tokens, and not have to presesrve the word embedding/position encoding values
    - Avoid vanishing gradient problem (Deep model, mani layers -> Causing the weights of earlier layers update slow or not have any impact on final result)
- `input` is what the initial prompt for the model to start generating
- `output` only includes what the model generated. Does not include the initial `input` fed to the model

# Encoder/Decoder Transformer
- For inference
    - For ENCODER, it uses `self-attention` (All info about `input prompt`)
    - For DECODER, it uses `self-attention` (All info about `generated output`)
    - Extra step after DECODER, uses `Encoder-Decoder attention`
- For training
    - For ENCODER, it uses `self-attention` (All info about `input prompt`)
    - For DECODER, it uses `masked self-attention` (All info about `generated output`)
        - What it means that you don't want the model to cheat and include future not generated token in the attention layer, which affects the output
        - Another thing is that we can `parallel` this for all output tokens
            - Say we have output tokens A, B, C
            - We don't need to know B, or C to start decoding A. And we dont need to know C to start decoding B
            - Tho we need A, B to finish decoding C (not to start decoding)
            => We could start A, B, C at the same time. And when at the stage where C needs to know A and B, we are already there and ready
    - Extra step after DECODER, uses `Encoder-Decoder attention`

[Input Token] -> `Fully connected layer` or `Word embedding network` -> Embedding input
[ENCODER]: Prompt input embedding -> RoPE -> Position encoded value [PV] -> [Attention Head] Self attention layer [K, V, Q] -> Sum([Softmax(Q . K) to get prob] * V) -> Self attention value [SV] -> Residual connection value [RCV]
                                        │                                                                                                                      │
                                        └──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┘

[DECODER]: Generated input embedding -> RoPE (exclude indexes from Decoder, start from 0) -> Position encoded value [PV] -> [Attention Head] Self attention layer [K, V, Q] -> Self attention value [SV] -> Residual connection value [RCV]
                                                                                 │                                                                                 │
                                                                                 └─────────────────────────────────────────────────────────────────────────────────┘

RCV (Encoder) for each token, has all information of relevant prompt tokens
RCV (Decoder) for each token, has all information of relevant generated tokens 
[RCV (Decoder)] -> [Attention Head] Encoder-Decoder Attention [K, V, Q] -> Sum([Softmax(Q . K) to get prob] * V) -> Encoder-Decoder attention value [EDV] -> Residual connection value [Final RCV]
     │                                                                                                                              │
     └──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┘

[Final RCV] -> `Fully connected layer` or `Word embedding network` -> Logits -> Softmax -> Token probabilities -> Token [Multiple decoding strategy]


# Decoder only Transformer
- Use `Masked Self-Attention` all the time, including both `input` and `output`, for both training and inference

[Input Token] -> `Fully connected layer` or `Word embedding network` -> Embedding input
[DECODER]: Last input embedding (regardless of prompt/generated token) -> RoPE -> Position encoded value [PV] -> [Attention Head] Self attention layer [K, V, Q] -> Sum([Softmax(Q . K) to get prob] * V) -> Self attention value [SV] -> Residual connection value [RCV]
                                                                                            │                                                                                                                      │
                                                                                            └──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┘

RCV (Encoder) for each token, has all information of relevant prompt tokens + generated 
[RCV (Decoder)] -> `Fully connected layer` or `Word embedding network` -> Logits -> Softmax -> Token probabilities -> Token [Multiple decoding strategy]


# Mixture of Expert (MoE)
- Basically replace feed forward network with a
    - Bunch of experts (smaller feed forward network)
    - Gating network (Judge to decide which expert get to be activated. Could either be multiple experts and then result is weighted sum, or singular expert)
- This happens on a `per token basis`, not on `per prompt basis`. This is the important distinction.
- Example would be, given a prompt `on the river bank`
    - After encoding + RoPE, the embedding has "bank" at `50/50 money/nature`
    - After self-attention, "bank" embedding now becomes `20/80 money/nature`
    - The gating network looks at this semantic meanings, decides to activate `nature expert`
    - Embedding is now passed to the `nature expert` which gives the most appropriate output token

# SIDE QUEST: ML/DL
- Good first read on convolutional neural network `https://developersbreach.com/convolution-neural-network-deep-learning/#1-1-convolution`
- [CNN] Convolution layer
    - Pure math, convolution is a complicated function
    - But practically, its called this way because it's function is equivalent to what we are doing with images and kernel/filter. Just look up how kernel/filter works
    - Main purpose is to extract features
    - (Local connectivity) `Its sparely connected`. Each neural is connected only to a subset of input image
    - (Parameter sharing) All neurons in a particular feature map share the same weights/kernel/filter 
- [FC] Fully connected layer/Dense layer 
    - It's a regular neural netwok, `but all inputs from previous layer are connected to every activation unit of the next layer` => `Much more dense`
    - Susceptible to overfitting. Dropout is used to counter that
- [RNN] Recurrent neural network 
    - It's designed for sequential data (text, speech, time series)
    - Unlike feed forward network, RNNs utilize `recurrent connections`, where the output of a neuron at one step is fed back as input to the network at the next step
- [FFN] Feed forward network
    - Opposite of RNN. A feed forward network has no recurrent connections. Just in and out.
