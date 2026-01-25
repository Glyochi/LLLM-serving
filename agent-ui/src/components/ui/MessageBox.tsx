import type { Message } from "../../models/PromptTypes";
import MessageBoxBot from "./MessageBoxBot";
import MessageBoxUser from "./MessageBoxUser";

interface MessageBoxProps {
  message: Message;
}

const MessageBox: React.FC<MessageBoxProps> = ({ message }) => {
  if (message.role == "USER") {
    return <MessageBoxUser messageContent={message.content} />;
  } else {
    return <MessageBoxBot messageContent={message.content} />;
  }
};

export default MessageBox;
