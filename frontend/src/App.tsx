import { useState } from "react"

interface DocumentForm {
  docuId: string;
  documentText: string;
}

interface ChunkData {
  sectionName: string;
  chunk: string;
  startLine: number;
}

interface JDAnalysisResult {
  documentId: string;
  totalChunks: number;
  chunkData: ChunkData[];
}


function App() {
  const [formData, setFormData] = useState<DocumentForm>({
    docuId: '',
    documentText: ''
  });

  const [status, setStatus] = useState<string>('');
  const [ready, setReady] = useState<boolean>(false);
  const [analysis, setAnalysis] = useState<JDAnalysisResult | null>(null);


  //submit handler
  const handleSubmit = async (e: React.SyntheticEvent<HTMLFormElement>) => {
    e.preventDefault();
    setStatus("Processing...");
    setReady(false);

    try {
      const response = await fetch("http://localhost:8080/api/document", {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(formData),
      });
      const result = await response.text();
      setStatus(result);
      setReady(true);
    } catch (error) {
      setStatus('Error: ' + error);
    }
  }


  //change handler
  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    setReady(false);
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  }

  //fetch analysis
  const fetchAnalysis = async () => {
    try {
      const res = await fetch(`http://localhost:8080/api/document/${formData.docuId}`);

      if (!res.ok) throw new Error("Failed to fetch analysis");

      const result: JDAnalysisResult = await res.json();
      setAnalysis(result);
      console.log(result);
    } catch (error) {
      setStatus('Error: ' + error);
    }

  }


  return (
    <div className='app-container'>
      <h1>Hire Me Please - Document Ingestion</h1>

      <form onSubmit={handleSubmit}>
        <div>
          <label>Document ID: </label>
          <input
            name="docuId"
            type="text"
            value={formData.docuId}
            onChange={handleChange}
            required
          />
        </div>
        <br />
        <div>
          <label>Document Content (JD or Resume):</label>
          <br />
          <textarea
            name="documentText"
            rows={15}
            cols={50}
            value={formData.documentText}
            onChange={handleChange}
            required
          />
        </div>

        <button type="submit">Process</button>
      </form>
      <hr />
      {status && (
        <div>
          <strong>Server Response:</strong>
          <pre>{status}</pre>
          <br /><br />
          {ready && (
            <button onClick={fetchAnalysis}>2. Get Analysis Result</button>
          )}
        </div>
      )}




      {analysis && (
        <div>
          <hr />
          <h2>Analysis Results</h2>
          <p>
            <strong>ID:</strong> {analysis.documentId} |
            <strong> Total Chunks:</strong> {analysis.totalChunks}
          </p>

          <div>
            {analysis.chunkData.map((item, index) => (
              <div key={index} style={{ marginBottom: '10px' }}>
                <span>
                  [{item.sectionName}, Line {item.startLine}]:
                </span>
                <p style={{ margin: '4px 0 0 20px', fontStyle: 'italic' }}>
                  "{item.chunk}"
                </p>
              </div>
            ))}
          </div>
        </div>
      )}

    </div>
  )
}

export default App
