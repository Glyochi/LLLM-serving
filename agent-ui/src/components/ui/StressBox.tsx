import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useEffect, useRef, useState } from "react";
import type { Conversation, Prompt } from "../../models/PromptTypes";
import glygatewayAgentService from "../../services/GlygatewayAgentService";
import { getPromptWithGlobalSettings, getRandomConversation } from "../../services/Utils";

interface StressBoxProps {}

const StressBox: React.FC<StressBoxProps> = ({}) => {
  const client = useQueryClient();

  const concurrentWorkersRef = useRef<number>(1);
  const [concurrentWorkers, setConcurrentWorkers] = useState<number>(concurrentWorkersRef.current);

  const stressTestRunningRef = useRef<boolean>(false);
  const [stressTestRunning, setStressTestRunning] = useState<boolean>(stressTestRunningRef.current);

  const [requestsCount, setRequestsCount] = useState<number>(0);

  function sleep(ms: number) {
    return new Promise((resolve) => setTimeout(resolve, ms));
  }

  // Not the prettiest solutions but it works lol
  async function startStressTest() {
    console.log("Started stress testing");

    var currentBusyCount = 0;

    async function makeRequest() {
      const _ = await glygatewayAgentService.streamComplete(
        getPromptWithGlobalSettings(client, getRandomConversation()),
      );
      currentBusyCount -= 1;
      setRequestsCount((prev) => prev + 1);
      console.log("Task done");
    }

    while (stressTestRunningRef.current) {
      var newTaskCount = 0;
      while (currentBusyCount < concurrentWorkersRef.current) {
        currentBusyCount += 1;
        newTaskCount += 1;
        makeRequest();
      }
      if (newTaskCount != 0) {
        console.log("Added " + newTaskCount + " new tasks");
      }
      await sleep(100);
    }
    console.log("Stopped stress testing");
  }

  return (
    <div className="w-[100%] flex-col m-auto bg-gray-700 p-5 rounded mb-30">
      <div className="flex flex-col">
        <div className="flex-1">Workers {concurrentWorkers}</div>
        <input
          className="flex-1"
          type="range"
          min={1}
          max={300}
          step={1}
          value={concurrentWorkers}
          onChange={(e) => {
            concurrentWorkersRef.current = parseInt(e.target.value);
            setConcurrentWorkers(concurrentWorkersRef.current);
          }}
        />
      </div>

      <div className="flex mt-5 items-center">
        <div>Count: {requestsCount}</div>
        <button
          className="m-auto mr-0 rounded-md px-3 py-1 bg-gray-900 hover:bg-red-800"
          onClick={() => {
            stressTestRunningRef.current = !stressTestRunningRef.current;
            setStressTestRunning(stressTestRunningRef.current);
            if (stressTestRunningRef.current) {
              startStressTest();
            }
          }}
        >
          {!stressTestRunning ? <>Stress</> : <>Stop</>}
        </button>
      </div>
    </div>
  );
};

export default StressBox;
