import ReactMarkdown from "react-markdown";

interface MessageBoxUserProps {
  messageContent: string;
}

const MessageBoxUser: React.FC<MessageBoxUserProps> = ({ messageContent }) => {
  return (
    <div className="m-auto mr-0 bg-gray-600 p-2 rounded max-w-[60%] flex-col">
      <div className="break-all whitespace-pre-line leading-snug">
        <ReactMarkdown>{messageContent}</ReactMarkdown>
      </div>
    </div>
  );
};

export default MessageBoxUser;
