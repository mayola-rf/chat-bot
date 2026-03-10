Experiment with Spring AI using Docker model runner

With Docker Image
1. Run LLM: `docker run -d -p 11434:11434 --name ollama ollama/ollama`
where, `-d` → run in detached mode
`-p 11434:11434` → maps container port to your host
`--name ollama` → gives the container a name. The LLM server will be available at http://localhost:11434
2. Download Model inside container: `docker exec -it ollama ollama pull mistral` (Mistral is a small LLM, runs on CPU uses 6-8 GB RAM)
3. Test Model: Run model -> `docker exec -it ollama ollama run mistral`. Test prompt -> Explain tokenization in layman terms

Connect to Spring AI

spring.ai.ollama.base-url=http://localhost:11434

spring.ai.ollama.chat.options.model=mistral

