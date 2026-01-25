type Role = "SYSTEM" | "USER";

type InferenceParams = {
  modelId: string;
  maxTokens: number;
  temperature: number;
  seed: number;
  topP: number;
  stream: boolean;
};

type Message = {
  role: Role;
  content: string;
};

type Conversation = {
  contents: Array<Message>;
};

type Prompt = { conversation: Conversation; inferenceParams: InferenceParams };

export type { Role, InferenceParams, Message, Conversation, Prompt };
