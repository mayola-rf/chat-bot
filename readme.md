Experiment with Spring AI using Docker model runner

Option 1: With Docker Image
1. Run LLM: `docker run -d -p 11434:11434 --name ollama ollama/ollama`
where, `-d` → run in detached mode
`-p 11434:11434` → maps container port to your host
`--name ollama` → gives the container a name. The LLM server will be available at http://localhost:11434
2. Download Model inside container: `docker exec -it ollama ollama pull mistral` (Mistral is a small LLM, runs on CPU uses 6-8 GB RAM)
3. Test Model: Run model -> `docker exec -it ollama ollama run mistral`. Test prompt -> Explain tokenization in layman terms


Option 2: Docker Model Runner
1. Run model-runner `docker desktop enable model-runner --tcp=12434`
2. Use Test Container and set up base-url Ref: `DockerModelRunnerTest`


Connect to Spring AI

spring.ai.ollama.base-url=http://localhost:11434

spring.ai.ollama.chat.options.model=mistral

Endpoints
GET http://localhost:8080/ai?userInput=Explain+embeddings+in+3+statements
GET http://localhost:8080/ai/prompt?country=India&state=Karnataka
GET http://localhost:8080/ai/structured-output?number=2&topic=world+war+II
GET http://localhost:8080/ai/structured-output-native-advisor?number=3&topic=world+war+I
GET http://localhost:8080/ai/using-tools?request=what%27s+today%27s+day+of+week+using+the+findDay+and+getCurrentDateTime+tools
GET http://localhost:8080/ai/using-tools?request=What+is+today%27s+date%3F+Use+the+available+tools
