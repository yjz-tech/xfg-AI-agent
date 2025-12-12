package cn.bugstack.xfg.dev.tech.api;

import cn.bugstack.xfg.dev.tech.api.response.Response;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IRAGService {

    Response<List<String>> queryRgTagList();

    Response<String> uploadFile(String ragTag,List<MultipartFile> files);

    Response<String> analyzeGitRepository(String repoURL,String username,String token) throws IOException,Exception;

}
