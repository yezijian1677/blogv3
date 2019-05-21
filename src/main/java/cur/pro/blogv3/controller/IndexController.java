package cur.pro.blogv3.controller;

import com.github.pagehelper.PageInfo;
import com.vdurmont.emoji.EmojiParser;
import cur.pro.blogv3.constant.WebConst;
import cur.pro.blogv3.dto.ErrorCode;
import cur.pro.blogv3.dto.MetaDto;
import cur.pro.blogv3.dto.Types;
import cur.pro.blogv3.exception.TipException;
import cur.pro.blogv3.modal.Bo.ArchiveBo;
import cur.pro.blogv3.modal.Bo.CommentBo;
import cur.pro.blogv3.modal.Bo.RestResponseBo;
import cur.pro.blogv3.modal.Vo.CommentVo;
import cur.pro.blogv3.modal.Vo.ContentVo;
import cur.pro.blogv3.modal.Vo.MetaVo;
import cur.pro.blogv3.service.ICommentService;
import cur.pro.blogv3.service.IContentService;
import cur.pro.blogv3.service.IMetaService;
import cur.pro.blogv3.service.ISiteService;
import cur.pro.blogv3.utils.IPKit;
import cur.pro.blogv3.utils.PatternKit;
import cur.pro.blogv3.utils.TaleUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.util.List;

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

    /**
     * 文章预览
     * @param request
     * @param cid
     * @return
     */
    @GetMapping(value = {"article/{cid}/preview", "article/{cid}.html"})
    public String articlePreview(HttpServletRequest request, @PathVariable String cid) {
        ContentVo contents = contentService.getContents(cid);
        if (null == contents) {
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


    /**
     * 公共方法
     * 组合评论和文章
     * @param request
     * @param contentVo
     */
    private void completeArticle(HttpServletRequest request, ContentVo contentVo) {
        if (contentVo.getAllowComment()) {
            String cp = request.getParameter("cp");
            if (StringUtils.isBlank(cp)) {
                cp = "1";
            }
            request.setAttribute("cp", cp);
            PageInfo<CommentBo> commentBoPageInfo = commentService.getComments(contentVo.getCid(), Integer.parseInt(cp), 6);
            request.setAttribute("comments", commentBoPageInfo);
        }
    }

    /**
     * 注销
     * @param session
     * @param response
     */
    @RequestMapping("logout")
    public void logout(HttpSession session, HttpServletResponse response) {
        TaleUtils.logout(session, response);
    }

    /**
     * 评论操作
     */
    @PostMapping(value = "comment")
    @ResponseBody
    @Transactional(rollbackFor = TipException.class)
    public RestResponseBo comment(HttpServletRequest request, HttpServletResponse response,
                                  @RequestParam Integer cid, @RequestParam Integer coid,
                                  @RequestParam String author, @RequestParam String mail,
                                  @RequestParam String url, @RequestParam String text, @RequestParam String _csrf_token) {

        String ref = request.getHeader("Referer");
        if (StringUtils.isBlank(ref) || StringUtils.isBlank(_csrf_token)) {
            return RestResponseBo.fail(ErrorCode.BAD_REQUEST);
        }

        String token = cache.hget(Types.CSRF_TOKEN.getType(), _csrf_token);
        if (StringUtils.isBlank(token)) {
            return RestResponseBo.fail(ErrorCode.BAD_REQUEST);
        }

        if (null == cid || StringUtils.isBlank(text)) {
            return RestResponseBo.fail("请输入完整后评论");
        }

        if (StringUtils.isNotBlank(author) && author.length() > 50) {
            return RestResponseBo.fail("姓名过长");
        }

        if (StringUtils.isNotBlank(mail) && !TaleUtils.isEmail(mail)) {
            return RestResponseBo.fail("请输入正确的邮箱格式");
        }

        if (StringUtils.isNotBlank(url) && !PatternKit.isURL(url)) {
            return RestResponseBo.fail("请输入正确的URL格式");
        }

        if (text.length() > 200) {
            return RestResponseBo.fail("请输入200个字符以内的评论");
        }

        String val = IPKit.getIpAddrByRequest(request) + ":" + cid;
        Integer count = cache.hget(Types.COMMENTS_FREQUENCY.getType(), val);
        if (null != count && count > 0) {
            return RestResponseBo.fail("您发表评论太快了，请过会再试");
        }

        author = TaleUtils.cleanXSS(author);
        text = TaleUtils.cleanXSS(text);

        author = EmojiParser.parseToAliases(author);
        text = EmojiParser.parseToAliases(text);

        CommentVo comments = new CommentVo();
        comments.setAuthor(author);
        comments.setCid(cid);
        comments.setIp(request.getRemoteAddr());
        comments.setUrl(url);
        comments.setContent(text);
        comments.setMail(mail);
        comments.setParent(coid);
        try {
            commentService.insertComment(comments);
            cookie("tale_remember_author", URLEncoder.encode(author, "UTF-8"), 7 * 24 * 60 * 60, response);
            cookie("tale_remember_mail", URLEncoder.encode(mail, "UTF-8"), 7 * 24 * 60 * 60, response);
            if (StringUtils.isNotBlank(url)) {
                cookie("tale_remember_url", URLEncoder.encode(url, "UTF-8"), 7 * 24 * 60 * 60, response);
            }
            // 设置对每个文章1分钟可以评论一次
            cache.hset(Types.COMMENTS_FREQUENCY.getType(), val, 1, 60);
            return RestResponseBo.ok();
        } catch (Exception e) {
            String msg = "评论发布失败";
            if (e instanceof TipException) {
                msg = e.getMessage();
            } else {
                log.error(msg, e);
            }
            return RestResponseBo.fail(msg);
        }
    }


    /**
     * 分类页
     * @param request
     * @param keyword
     * @param limit
     * @return
     */
    @GetMapping(value = "category/{keyword}")
    public String categories(HttpServletRequest request, @PathVariable String keyword,
                             @RequestParam(value = "limit", defaultValue = "12") int limit) {
        return this.categories(request, keyword, 1, limit);
    }

    @GetMapping(value = "category/{keyword}/{page}")
    public String categories(HttpServletRequest request,
                             @PathVariable String keyword,
                             @PathVariable int page,
                             @RequestParam(value = "limit", defaultValue = "12") int limit
    ) {
        page = page < 0 || page > WebConst.MAX_PAGE ? 1 : page;
        MetaDto metaDto = metaService.getMeta(Types.CATEGORY.getType(), keyword);
        if (null == metaDto) {
            return this.render_404();
        }

        PageInfo<ContentVo> contentVoPageInfo = contentService.getArticles(metaDto.getMid(), page, limit);

        request.setAttribute("articles", contentVoPageInfo);
        request.setAttribute("meta", metaDto);
        request.setAttribute("type", "分类");
        request.setAttribute("keyword", keyword);

        return this.render("page-category");
    }

    /**
     * 归档页
     * @param request
     * @return
     */
    @GetMapping(value = "archives")
    public String archives(HttpServletRequest request) {
        List<ArchiveBo> archiveBos = siteService.getArchives();
        request.setAttribute("archives", archiveBos);
        return this.render("archives");
    }

    /**
     * 友链页
     * @param request
     * @return
     */
    @GetMapping(value = "links")
    public String links(HttpServletRequest request) {
        List<MetaVo> links = metaService.getMetas(Types.LINK.getType());
        request.setAttribute("links", links);
        return this.render("links");
    }

    /**
     * 关于界面
     * @param pagename
     * @param request
     * @return
     */
    @GetMapping(value = "/{pagename}")
    public String page(@PathVariable String pagename, HttpServletRequest request) {
        ContentVo contents = contentService.getContents(pagename);
        if (null == contents) {
            return this.render_404();
        }
        if (contents.getAllowComment()) {
            String cp = request.getParameter("cp");
            if (StringUtils.isBlank(cp)) {
                cp = "1";
            }
            PageInfo<CommentBo> commentBoPageInfo = commentService.getComments(contents.getCid(), Integer.parseInt(cp), 6);
            request.setAttribute("comments", commentBoPageInfo);

        }
        request.setAttribute("article", contents);
        updateArticleHit(contents.getCid(), contents.getHits());

        return this.render("page");

    }

    /**
     * 搜索页
     * @param request
     * @param keyword
     * @param limit
     * @return
     */
    @GetMapping(value = "search/{keyword}")
    public String Search(HttpServletRequest request, @PathVariable String keyword, @RequestParam(value = "limit", defaultValue = "12") int limit) {
        return this.search(request, keyword, 1, limit);
    }


    @GetMapping(value = "search/{keyword}/{page}")
    public String search(HttpServletRequest request, @PathVariable String keyword,@PathVariable int page, @RequestParam(value = "limit", defaultValue = "12") int limit){
        page = page < 0 || page > WebConst.MAX_PAGE ? 1 : page;
        PageInfo<ContentVo> articles = contentService.getArticles(keyword, page ,limit);
        request.setAttribute("articles", articles);
        request.setAttribute("type", "搜索");
        request.setAttribute("keyword", keyword);

        return this.render("page-category");
    }


    /**
     * 文章点击率
     * @param cid
     * @param chits
     */
    @Transactional
    protected void updateArticleHit(Integer cid, Integer chits) {
        Integer hits = cache.hget("article", "hits");
        if (chits == null) {
            chits = 0;
        }
        hits = null == hits ? 1 : hits + 1;
        if (hits >= WebConst.HIT_EXCEED) {
            ContentVo temp = new ContentVo();
            temp.setCid(cid);
            temp.setHits(chits + hits);
            contentService.updateContentByCid(temp);
            cache.hset("article", "hist", 1);
        } else {
            cache.hset("article", "hits", hits);

        }
    }

    /**
     * 标签页
     * @param request
     * @param name
     * @param limit
     * @return
     */
    @GetMapping(value = "tag/{name}")
    public String tags(HttpServletRequest request, @PathVariable String name, @RequestParam(value = "limit", defaultValue = "12") int limit) {
        return this.tags(request, name, 1, limit);
    }

    @GetMapping(value = "tag/{name}/{page}")
    public String tags(HttpServletRequest request, @PathVariable String name, @PathVariable int page,
                       @RequestParam(value = "limit", defaultValue = "12") int limit) {
        page = page < 0 || page > WebConst.MAX_PAGE ? 1 : page;
        //blank
        name = name.replaceAll("\\+", " ");
        MetaDto metaDto = metaService.getMeta(Types.TAG.getType(), name);
        if (null == metaDto) {
            return this.render_404();
        }

        PageInfo<ContentVo> contentVoPageInfo = contentService.getArticles(metaDto.getMid(), page, limit);
        request.setAttribute("articles", contentVoPageInfo);
        request.setAttribute("meta", metaDto);
        request.setAttribute("type", "标签");
        request.setAttribute("keyword", name);

        return this.render("page-category");
    }

    private void cookie(String name, String value, int maxAge, HttpServletResponse response) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(maxAge);
        cookie.setSecure(false);
        response.addCookie(cookie);
    }

}
