import type { Dispatch } from "react";

interface ChatBoxProps {
  currentMessage: string;
  setMessage: Dispatch<string>;
  handleSubmit: () => void;
}

const ChatBox: React.FC<ChatBoxProps> = ({ currentMessage, setMessage, handleSubmit }) => {
  const handleMessageChange = (event: React.ChangeEvent<HTMLTextAreaElement>) => {
    setMessage(event.target.value);
  };

  const handleKeyDown = (event: React.KeyboardEvent<HTMLTextAreaElement>) => {
    // Check if the Enter key is pressed
    if (event.key === "Enter") {
      // Check if the Shift key is pressed
      if (event.shiftKey) {
        // Keep the same behavior (new line)
      } else {
        // Check if the Shift key is NOT pressed
        // Enter by itself no longer create new line
        event.preventDefault();
        handleSubmit();
      }
    }
  };

  return (
    <div className="w-full flex py-1 px-2 rounded-xl bg-gray-500 shadow-gray-900 shadow-md">
      <textarea
        className="flex-1 p-2 h-[100%] rounded focus:outline-none focus:ring-0 field-sizing-content overflow-y-scroll"
        value={currentMessage}
        onChange={handleMessageChange}
        onKeyDown={handleKeyDown}
        placeholder="SPEAK TO ME!!"
      />
      <button onClick={handleSubmit} className="bg-gray-700 rounded-2xl px-2 py-1 m-auto ml-2 h-fit p-5">
        Submit
      </button>
    </div>
  );
};

export default ChatBox;
