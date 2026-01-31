import { useEffect, useRef, useState } from "react";

interface SpinnerProps {
  isDone: boolean;
}

function formatElapsedTime(milliseconds: number) {
  let seconds = Math.floor(milliseconds / 1000);
  let minutes = Math.floor(seconds / 60);
  let hours = Math.floor(minutes / 60);
  let days = Math.floor(hours / 24);

  seconds = seconds % 60;
  minutes = minutes % 60;
  hours = hours % 24;

  // Pad with leading zeros if necessary
  const pad = (num: number) => num.toString().padStart(2, " ");

  if (days > 0) {
    return `${days}d ${pad(hours)}:${pad(minutes)}:${pad(seconds)}`;
  } else if (hours > 0) {
    return `${pad(hours)}:${pad(minutes)}:${pad(seconds)}`;
  } else if (minutes > 0) {
    return `${pad(minutes)}:${pad(seconds)}`;
  } else {
    return `${pad(seconds)}s`;
  }
}

const Spinner: React.FC<SpinnerProps> = ({ isDone }) => {
  const startTimeRef = useRef<number>(performance.now());
  const [timeElapsed, setTimeElapsed] = useState<string>();

  useEffect(() => {
    const intervalId = setInterval(() => {
      var currentTime = performance.now();
      var timeElapsedString = formatElapsedTime(Math.round(currentTime - startTimeRef.current));
      setTimeElapsed(timeElapsedString);
    }, 1000);

    return () => {
      clearInterval(intervalId);
    };
  });

  return (
    <div className="h-6 m-1 flex w-fit items-center">
      {!isDone && (
        <>
          <div className="w-3 h-3 m-2 border-2 border-gray-100 border-t-transparent rounded-full animate-spin"></div>
          <div className="m-auto">{timeElapsed}</div>
        </>
      )}
    </div>
  );
};

export default Spinner;
