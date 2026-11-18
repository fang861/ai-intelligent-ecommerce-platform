package com.scaffold.controller.admin;

import com.scaffold.agent.RagKnowledgeService;
import com.scaffold.common.AuthContext;
import com.scaffold.common.Result;
import com.scaffold.dto.RagDocView;
import com.scaffold.dto.RagOverview;
import com.scaffold.dto.RagSearchHit;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/rag")
public class AdminRagController {

    @Autowired
    private RagKnowledgeService ragKnowledgeService;

    @GetMapping("/overview")
    public Result<RagOverview> overview(HttpServletRequest request) {
        AuthContext.requireAdmin(request);
        return Result.ok(ragKnowledgeService.overview());
    }

    @GetMapping("/docs")
    public Result<List<RagDocView>> docs(HttpServletRequest request) {
        AuthContext.requireAdmin(request);
        return Result.ok(ragKnowledgeService.listDocs(false));
    }

    @GetMapping("/docs/{filename:.+}")
    public Result<RagDocView> docDetail(HttpServletRequest request, @PathVariable String filename) {
        AuthContext.requireAdmin(request);
        return Result.ok(ragKnowledgeService.getDoc(filename));
    }

    @PostMapping(value = "/docs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<RagOverview> upload(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        AuthContext.requireAdmin(request);
        return Result.ok(ragKnowledgeService.upload(file));
    }

    @DeleteMapping("/docs/{filename:.+}")
    public Result<RagOverview> delete(HttpServletRequest request, @PathVariable String filename) {
        AuthContext.requireAdmin(request);
        return Result.ok(ragKnowledgeService.deleteUploaded(filename));
    }

    @PostMapping("/search")
    public Result<List<RagSearchHit>> search(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        AuthContext.requireAdmin(request);
        String query = body.get("query") == null ? "" : String.valueOf(body.get("query"));
        int topK = 5;
        if (body.get("topK") != null) {
            try {
                topK = Integer.parseInt(String.valueOf(body.get("topK")));
            } catch (NumberFormatException ignored) {
                // keep default
            }
        }
        return Result.ok(ragKnowledgeService.search(query, topK));
    }

    @PostMapping("/reload")
    public Result<RagOverview> reload(HttpServletRequest request) {
        AuthContext.requireAdmin(request);
        return Result.ok(ragKnowledgeService.reload());
    }
}
