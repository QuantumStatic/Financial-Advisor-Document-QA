import os

GROQ_API_KEY = os.environ.get("GROQ_API_KEY", "placeholder-for-testing")
CHROMA_HOST = os.getenv("CHROMA_HOST", "localhost")
CHROMA_PORT = int(os.getenv("CHROMA_PORT", "8001"))
CHROMA_COLLECTION = "rag_docs"
GROQ_MODEL = "llama-3.3-70b-versatile"
MAX_ITERATIONS = 10
CHUNK_SIZE = 1000
CHUNK_OVERLAP = 200
KAFKA_BOOTSTRAP_SERVERS = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092")
