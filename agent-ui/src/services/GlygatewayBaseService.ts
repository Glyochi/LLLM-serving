
class GlygatewayBaseService {
  protected url: string = "http://glyml:8090"
  protected headers: Record<string, string> = {
    'Content-Type': 'application/json',
  }

  constructor() {
  }

}

export default GlygatewayBaseService 
