import { useMutation, useQueryClient } from "@tanstack/react-query";
import ChatBox from "../components/ui/ChatBox";
import glygatewayAgentService from "../services/GlygatewayAgentService";
import { use, useRef, useState } from "react";
import { consumeNdjsonResponse, getPromptWithGlobalSettings } from "../services/Utils";
import type { StreamTokenEvent } from "../models/NdjsonTypes";
import type { Message, Prompt } from "../models/PromptTypes";
import ConversationBox from "../components/ui/ConversationBox";
import StressBox from "../components/ui/StressBox";

function Home() {
  const client = useQueryClient();

  const conversationRef = useRef<Array<Message>>([]);
  const [conversation, setConversation] = useState<Array<Message>>(conversationRef.current);

  const chatMessageRef = useRef<string>("");
  const [chatMessage, setChatMessage] = useState<string>(chatMessageRef.current);

  const responseStringRef = useRef<string>("");
  const [responseString, setResponseString] = useState<string>(responseStringRef.current);
  const [responseLoading, setResponseLoading] = useState<boolean>(false);

  const mutation = useMutation({
    mutationFn: async (prompt: Prompt) => {
      setResponseLoading(true);
      return consumeNdjsonResponse(
        await glygatewayAgentService.streamComplete(prompt),
        (v: StreamTokenEvent) => {
          // Stream token response
          responseStringRef.current += v.content;
          setResponseString(responseStringRef.current);
          return;
        },
        null,
      );
    },
    onSuccess: () => {
      // Replace placeholder streaming message with final message box (by putting it in conversationRef)
      conversationRef.current = [...conversationRef.current, { role: "SYSTEM", content: responseStringRef.current }];
      setConversation(conversationRef.current);

      responseStringRef.current = "";
      setResponseString(responseStringRef.current);
      setResponseLoading(false);
    },
    onError: () => {
      responseStringRef.current = "";
      setResponseString(responseStringRef.current);
      setResponseLoading(false);
    },
  });

  const handleSubmit = () => {
    if (chatMessageRef.current.trim() == "") {
      return;
    }
    // Construct prompt
    var allConversation = conversationRef.current;
    allConversation.push({ role: "USER", content: chatMessageRef.current });
    var prompt = getPromptWithGlobalSettings(client, {
      contents: allConversation,
    });

    // Send inference request
    mutation.mutate(prompt);

    // Update UI
    setConversation(allConversation);
    chatMessageRef.current = "";
    setChatMessage("");
  };

  const handleReset = () => {
    conversationRef.current = [];
    setConversation(conversationRef.current);
  };

  return (
    <div className="w-full m-auto flex">
      <div className="flex-1">
        <button className="bg-gray-600 px-2 py-1 rounded-xl" onClick={handleReset}>
          Reset
        </button>
      </div>

      <div className="flex-3 h-1/2 m-auto flex flex-col">
        <div className="w-full flex">
          <ConversationBox
            conversationHistory={conversation}
            streamingMessage={responseString}
            streamingMessageLoading={responseLoading}
          />
        </div>

        <div className="fixed bottom-5 left-0 right-0 w-full h-30 flex">
          <div className="flex m-auto w-[40%] max-h-[80%]">
            <ChatBox
              currentMessage={chatMessage}
              setMessage={(message) => {
                setChatMessage(message);
                chatMessageRef.current = message;
                return;
              }}
              handleSubmit={handleSubmit}
            />
          </div>
        </div>
      </div>

      <div className="flex-1">
        <StressBox />
      </div>
    </div>
  );
}

export default Home;
