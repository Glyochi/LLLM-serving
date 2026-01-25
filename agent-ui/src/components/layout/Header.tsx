import { useState } from "react";
import { Modal } from "../ui/Modal";
import { Settings } from "../ui/Settings";

export default function Header() {
  const [showSettings, setShowSettings] = useState<boolean>(true);

  return (
    <header className="flex items-center justify-between bg-bg text-white px-4 py-3 shadow">
      {/* Left: logo */}
      <div className="flex items-center gap-2">
        <span className="text-xl font-bold">Gly LLLM Serving</span>
      </div>

      {/* Right: actions */}
      <div className="flex items-center gap-4">
        <button
          className="rounded-md px-3 py-1.5 text-sm hover:bg-gray-600"
          onClick={() => setShowSettings((prev) => !prev)}
        >
          Settings
        </button>
      </div>

      {showSettings && (
        <Modal open={showSettings} onClose={() => setShowSettings(false)}>
          <Settings />
        </Modal>
      )}
    </header>
  );
}
