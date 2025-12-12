package cn.bugstack.xfg.dev.tech.test;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.PathResource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

@SpringBootTest
@Slf4j
@RunWith(SpringRunner.class)
public class JGitTest {

    @Resource
    private OllamaChatClient ollamaChatClient;
    @Resource
    private TokenTextSplitter tokenTextSplitter;
    @Resource
    private SimpleVectorStore simpleVectorStore;
    @Resource
    private PgVectorStore pgVectorStore;

    @Test
    public  void test() throws IOException, GitAPIException {
        String repoURL="https://gitee.com/yjzwww1234/ddd-big-marker";
        String username="yjzwww1234";
        String password="c63932bd3a9b73dfac6d0be38384e6a8";

        String localPath="./cloned-repo";

        log.info("克隆路径: "+new File(localPath).getAbsolutePath());

        FileUtils.deleteDirectory(new File(localPath));

        // 完成一次Git操作
        Git git=Git.cloneRepository()
                .setURI(repoURL)
                .setDirectory(new File(localPath))
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username,password))
                .call();

        git.close();


    }

//    @Test
//    public void test_file() throws IOException {
//
//        Files.walkFileTree(Paths.get(""),new SimpleFileVisitor<>(){
//
//            @Override
//            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//                log.info("文件路径:{}",file.toString());
//
//                PathResource resource = new PathResource(file);
//
//                TikaDocumentReader reader =new TikaDocumentReader(resource);
//
//                List<Document> documents = reader.get();
//                List<Document> documentSplitter = tokenTextSplitter.apply(documents);
//
//                documents.forEach(doc->doc.getMetadata().put("knowledge","ddd-big-marker"));
//                documentSplitter.forEach(doc->doc.getMetadata().put("knowledge","ddd-big-marker"));
//
//                return FileVisitResult.CONTINUE;
//            }
//        });
//
//    }

    @Test
    public void test_file() throws IOException {
        // 👇 明确指定要扫描的目录（建议用你克隆的仓库路径）
        Path rootDir = Paths.get("./cloned-repo");

        if (!Files.exists(rootDir)) {
            log.warn("目录不存在: {}", rootDir.toAbsolutePath());
            return;
        }

        Files.walkFileTree(rootDir, new SimpleFileVisitor<>() {

            // 👇 跳过 .git、.idea 等隐藏目录
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                String dirName = dir.getFileName().toString();
                if (dirName.startsWith(".")) { // 跳过所有以 . 开头的隐藏目录
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                // 👇 只处理常见可读文件（按需增删后缀）
                String fileName = file.getFileName().toString().toLowerCase();
                if (!(fileName.endsWith(".txt") || fileName.endsWith(".md") ||
                        fileName.endsWith(".pdf") || fileName.endsWith(".docx") ||
                        fileName.endsWith(".pptx") || fileName.endsWith(".xlsx") ||
                        fileName.endsWith(".java") || fileName.endsWith(".xml") ||
                        fileName.endsWith(".json") || fileName.endsWith(".yml") ||
                        fileName.endsWith(".yaml") || fileName.endsWith(".properties"))) {
                    return FileVisitResult.CONTINUE; // 跳过不支持的文件
                }

                log.info("正在处理文件: {}", file);

                try {
                    PathResource resource = new PathResource(file);
                    TikaDocumentReader reader = new TikaDocumentReader(resource);

                    List<Document> documents = reader.get(); // 可能为空，但不会 null（新版 Spring AI 会处理）

                    // 如果 Tika 未能提取任何内容，跳过
                    if (documents == null || documents.isEmpty()) {
                        log.debug("文件无有效内容，跳过: {}", file);
                        return FileVisitResult.CONTINUE;
                    }

                    // 添加元数据
                    documents.forEach(doc -> doc.getMetadata().put("knowledge", "ddd-big-marker"));

                    // 分块
                    List<Document> documentSplitter = tokenTextSplitter.apply(documents);
                    documentSplitter.forEach(doc -> doc.getMetadata().put("knowledge", "ddd-big-marker"));

                    // 👇 在这里可以保存到向量库、打印、或做其他处理
                    log.info("成功处理文件: {}, 文档数: {}, 分块数: {}",
                            file, documents.size(), documentSplitter.size());

                } catch (Exception e) {
                    log.warn("处理文件失败: {}", file, e);
                    // 不抛出异常，继续处理其他文件
                }

                return FileVisitResult.CONTINUE;
            }
        });
    }


}
