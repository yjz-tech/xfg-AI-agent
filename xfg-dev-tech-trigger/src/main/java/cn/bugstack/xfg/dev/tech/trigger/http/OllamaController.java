package cn.bugstack.xfg.dev.tech.trigger.http;

import cn.bugstack.xfg.dev.tech.api.IAiService;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/ollama/")
public class OllamaController implements IAiService {

    @Autowired
    private OllamaChatClient chatClient;



//    @RequestMapping(value = "generate",method = RequestMethod.GET)

    /**
     * http://localhost:8090/api/v1/ollama/generate?model=deepseek-r1:1.5b&message=1+1
     * @param model
     * @param message
     * @return
     */
    @GetMapping("generate")
    @Override
    public ChatResponse generate(@RequestParam String model,@RequestParam String message) {
        return chatClient.call(new Prompt(message, OllamaOptions.create().withModel(model)));
    }

    /**
     * http://localhost:8090/api/v1/ollama/generate_stream?model=deepseek-r1:1.5b&message=1+1
     * @param model
     * @param message
     * @return
     */
    @GetMapping("generate_stream")
    @Override
    public Flux<ChatResponse> generateStream(@RequestParam String model,@RequestParam String message) {
        return chatClient.stream(new Prompt(message,OllamaOptions.create().withModel(model)));
    }
}
