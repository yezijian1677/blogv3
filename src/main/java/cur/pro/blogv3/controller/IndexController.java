package cur.pro.blogv3.controller;

import com.github.pagehelper.PageInfo;
import cur.pro.blogv3.constant.WebConst;
import cur.pro.blogv3.modal.Vo.ContentVo;
import cur.pro.blogv3.service.ICommentService;
import cur.pro.blogv3.service.IContentService;
import cur.pro.blogv3.service.IMetaService;
import cur.pro.blogv3.service.ISiteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Controller
@Slf4j
public class IndexController extends BaseController {

    @Resource
    private IContentService contentService;
    @Resource
    private ICommentService commentService;
    @Resource
    private IMetaService metaService;
    @Resource
    private ISiteService siteService;

    /**
     * 首页
     */
    @GetMapping(value = {"/", "index"})
    public String index(HttpServletRequest request, @RequestParam(value = "limit", defaultValue = "12") int limit) {
        return this.index(request, 1, limit);
    }

    /**
     * 首页的分页
     * @param request
     * @param p 第几页
     * @param limit 每页大小
     * @return
     */
    @GetMapping(value = "page/{p}")
    public String index(HttpServletRequest request, @PathVariable int p, @RequestParam(value = "limit", defaultValue = "12") int limit) {
        p = p < 0 || p > WebConst.MAX_POSTS ? 1 : p;
        PageInfo<ContentVo> articles = contentService.getContents(p, limit);
        request.setAttribute("articles", articles);
        if (p > 1) {
            this.title(request, "第" + p + "页");
        }
        return this.render("index");
    }

    /**
     * 文章页
     *
     * @param request
     * @param cid 文章主键
     * @return
     */
    @GetMapping(value = {"article/{cid}", "article/{cid}.html"})
    public String getArticle(HttpServletRequest request, @PathVariable String cid) {
        ContentVo contents = contentService.getContents(cid);
        if (null == contents || "draft".equals(contents.getStatus())) {
            return this.render_404();
        }

        request.setAttribute("article", contents);
        request.setAttribute("is_post", true);
        //        组合
        completeArticle(request, contents);
        //         更新点击
        updateArticleHit(contents.getCid(), contents.getHits());

        return this.render("post");
    }

    


}
