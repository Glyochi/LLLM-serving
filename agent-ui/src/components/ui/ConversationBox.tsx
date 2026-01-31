import type { Message } from "../../models/PromptTypes";
import MessageBox from "./MessageBox";
import MessageBoxBot from "./MessageBoxBot";
import Spinner from "./Spinner";

interface ConversationBoxProps {
  conversationHistory: Array<Message>;
  streamingMessage: string;
  streamingMessageLoading: boolean;
}

const ConversationBox: React.FC<ConversationBoxProps> = ({
  conversationHistory,
  streamingMessage,
  streamingMessageLoading,
}) => {
  return (
    <div className="w-[90%] min-h-screen flex-col m-auto bg-gray-700 p-5 rounded mb-30">
      {conversationHistory.map((message, id) => {
        return (
          <div key={id} className="w-full flex mb-2">
            <MessageBox message={message} />
          </div>
        );
      })}
      {streamingMessageLoading && (
        <div className="flex flex-col">
          <Spinner isDone={!streamingMessageLoading} />
          <div className="w-full flex mb-2">
            <MessageBoxBot messageContent={streamingMessage} />
          </div>
        </div>
      )}
    </div>
  );
};

export default ConversationBox;
