export default function Header() {
  return (
    <header className="flex items-center justify-between bg-bg text-white px-4 py-3 shadow">
      {/* Left: logo */}
      <div className="flex items-center gap-2">
        <span className="text-xl font-bold">Gly GPT</span>
      </div>

      {/* Right: actions */}
      <div className="flex items-center gap-4">
        <button className="rounded-md px-3 py-1.5 text-sm bg-accent hover:bg-gray-600">
          Settings
        </button>
      </div>
    </header>
  );
}

