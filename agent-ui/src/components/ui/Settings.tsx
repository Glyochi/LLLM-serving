import { useQueryClient } from "@tanstack/react-query";
import { useRef, useState } from "react";
import { default_inference_params, global_keys } from "../../configs/Constants";

type SettingsProps = {};

// maxTokens: 100,
// temperature: 1.0,
// seed: 809357414,
// topP: 0.9,
// stream: true,
export function Settings() {
  const client = useQueryClient();

  const maxTokensRef = useRef<number>(
    client.getQueryData<number>([global_keys.maxTokens]) ?? default_inference_params.maxTokens,
  );
  const [maxTokens, setMaxTokens] = useState<number>(maxTokensRef.current);
  client.setQueryData<number>([global_keys.maxTokens], maxTokensRef.current);

  const temperatureRef = useRef<number>(
    client.getQueryData<number>([global_keys.temperature]) ?? default_inference_params.temperature,
  );
  const [temperature, setTemperature] = useState<number>(temperatureRef.current);
  client.setQueryData<number>([global_keys.temperature], temperatureRef.current);

  const seedRef = useRef<number>(client.getQueryData<number>([global_keys.seed]) ?? default_inference_params.seed);
  const [seed, setSeed] = useState<number>(seedRef.current);
  client.setQueryData<number>([global_keys.seed], seedRef.current);

  const topPRef = useRef<number>(client.getQueryData<number>([global_keys.topP]) ?? default_inference_params.topP);
  const [topP, setTopP] = useState<number>(topPRef.current);
  client.setQueryData<number>([global_keys.topP], topPRef.current);

  return (
    <div className="flex flex-col">
      <div className="flex w-full gap-4">
        <div className="flex-1">Max Tokens</div>
        <div className="flex-1">{maxTokens}</div>
        <input
          className="flex-1"
          type="range"
          min={10}
          max={1000}
          value={maxTokens}
          onChange={(e) => {
            maxTokensRef.current = parseInt(e.target.value);
            setMaxTokens(maxTokensRef.current);
          }}
        />
      </div>
      <div className="flex w-full gap-4">
        <div className="flex-1">Temperature</div>
        <div className="flex-1">{temperature}</div>
        <input
          className="flex-1"
          type="range"
          min={0.0}
          max={2.0}
          step={0.1}
          value={temperature}
          onChange={(e) => {
            temperatureRef.current = parseFloat(e.target.value);
            setTemperature(temperatureRef.current);
          }}
        />
      </div>
      <div className="flex w-full gap-4">
        <div className="flex-1">Seed</div>
        <div className="flex-1">{seed}</div>
        <input
          className="flex-1"
          type="range"
          min={0}
          max={1000000000}
          step={1}
          value={seed}
          onChange={(e) => {
            seedRef.current = parseInt(e.target.value);
            setSeed(seedRef.current);
          }}
        />
      </div>
      <div className="flex w-full gap-4">
        <div className="flex-1">TopP</div>
        <div className="flex-1">{topP}</div>
        <input
          className="flex-1"
          type="range"
          min={0.0}
          max={1.0}
          step={0.1}
          value={topP}
          onChange={(e) => {
            topPRef.current = parseFloat(e.target.value);
            setTopP(topPRef.current);
          }}
        />
      </div>
    </div>
  );
}
