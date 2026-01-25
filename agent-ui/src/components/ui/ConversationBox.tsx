import type { Message } from "../../models/PromptTypes";
import MessageBox from "./MessageBox";
import MessageBoxBot from "./MessageBoxBot";

interface ConversationBoxProps {
  conversationHistory: Array<Message>;
  streamingMessage: string;
}

const ConversationBox: React.FC<ConversationBoxProps> = ({ conversationHistory, streamingMessage }) => {
  return (
    <div className="w-[90%] min-h-screen flex-col m-auto bg-gray-700 p-5 rounded mb-30">
      {conversationHistory.map((message, id) => {
        return (
          <div key={id} className="w-full flex mb-2">
            <MessageBox message={message} />
          </div>
        );
      })}
      {streamingMessage != "" && (
        <div className="w-full flex mb-2">
          <MessageBoxBot messageContent={streamingMessage} />{" "}
        </div>
      )}
    </div>
  );
};

export default ConversationBox;
