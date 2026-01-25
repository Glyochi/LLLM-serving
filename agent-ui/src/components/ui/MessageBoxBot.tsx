import ReactMarkdown from "react-markdown";

interface MessageBoxBotProps {
  messageContent: string;
}

const MessageBoxBot: React.FC<MessageBoxBotProps> = ({ messageContent }) => {
  return (
    <div className="m-auto ml-0 p-2 rounded w-[100%]">
      <div className="break-all whitespace-pre-line leading-snug">
        <ReactMarkdown>{messageContent}</ReactMarkdown>
      </div>
    </div>
  );
};

export default MessageBoxBot;
