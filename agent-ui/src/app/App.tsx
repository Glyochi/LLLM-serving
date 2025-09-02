import './App.css'
import Header from '../components/layout/Header'

function App() {

  return (
    <>
      <div className="flex h-screen w-screen">
        <div className="flex-1 flex flex-col">
          <Header />
          <main className="flex-1 bg-gray-100 p-4 overflow-y-auto">
            <div>HELLLOOO</div>
          </main>
        </div>
      </div>
    </>
  )
}

export default App
