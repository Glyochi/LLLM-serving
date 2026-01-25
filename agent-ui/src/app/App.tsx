import "./App.css";
import Header from "../components/layout/Header";
import Home from "../pages/Home";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";

function App() {
  const client = new QueryClient();
  return (
    <>
      <QueryClientProvider client={client}>
        <div className="flex h-screen w-screen">
          <div className="flex-1 flex flex-col">
            <Header />
            <main className="flex-1 bg-gray-800 p-4 overflow-y-auto text-white">
              <Home></Home>
            </main>
          </div>
        </div>
      </QueryClientProvider>
    </>
  );
}

export default App;
