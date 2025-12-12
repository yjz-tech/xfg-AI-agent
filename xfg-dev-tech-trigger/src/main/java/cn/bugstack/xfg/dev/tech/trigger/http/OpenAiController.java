package cn.bugstack.xfg.dev.tech.trigger.http;


import cn.bugstack.xfg.dev.tech.api.IAiService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/openai/")
public class OpenAiController implements IAiService {

    @Resource
    private OpenAiChatClient chatClient;

    @Resource
    private PgVectorStore pgVectorStore;

    @GetMapping("generate")
    @Override
    public ChatResponse generate(String model, String message) {
        return chatClient.call(new Prompt(message, OpenAiChatOptions.builder()
                                                .withModel(model)
                .build()));
    }

    @Override
    public Flux<ChatResponse> generateStream(String model, String message) {
        return chatClient.stream(new Prompt(
                message,
                OpenAiChatOptions.builder()
                .withModel(model)
                .build()
        ));
    }

    @Override
    public Flux<ChatResponse> generateStreamRag(String model, String ragTag, String query) {
        String SYSTEM_PROMPT = """
                Use the information from the DOCUMENTS section to provide accurate answers but act as if you knew this information innately.
                If unsure, simply state that you don't know.
                Another thing you need to note is that your reply must be in Chinese!
                DOCUMENTS:
                    {documents}
                """;

        SearchRequest request = SearchRequest.query(query).withTopK(5).withFilterExpression("knowledge == '知识库名称'");

        List<Document> documents = pgVectorStore.similaritySearch(request);
        String documentsCollectors = documents.stream().map(Document::getContent).collect(Collectors.joining());


        Message ragMessage = new SystemPromptTemplate(SYSTEM_PROMPT).createMessage(Map.of("documents", documentsCollectors));

        ArrayList<Message> messages = new ArrayList<>();
        messages.add(new UserMessage(query));
        messages.add(ragMessage);

        return chatClient.stream(new Prompt(
                query,
                OpenAiChatOptions.builder()
                        .withModel(model)
                        .build()
        ));
    }
}
