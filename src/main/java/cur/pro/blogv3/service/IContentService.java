package cur.pro.blogv3.service;

import com.github.pagehelper.PageInfo;
import cur.pro.blogv3.modal.Vo.CommentVo;
import cur.pro.blogv3.modal.Vo.ContentVo;
import cur.pro.blogv3.modal.Vo.ContentVoExample;

public interface IContentService {

    /**
     * 发表文章
     *
     * @param contentVo
     */
    void publish(ContentVo contentVo);

    /**
     * 查询文章
     *
     * @param p
     * @param limit
     * @return
     */
    PageInfo<ContentVo> getContents(Integer p, Integer limit);

    /**
     * 根据id获取文章
     *
     * @param id
     * @return
     */
    ContentVo getContents(String id);

    /**
     * 更新文章
     *
     * @param contentVo
     */
    void updateContentByCid(ContentVo contentVo);

    /**
     * 查询分类下的文章列表
     *
     * @param mid
     * @param page
     * @param limit
     * @return
     */
    PageInfo<ContentVo> getArticles(Integer mid, int page, int limit);

    /**
     * 搜索分页
     *
     * @param keyword
     * @param page
     * @param limit
     * @return
     */
    PageInfo<ContentVo> getArticles(String keyword, Integer page, Integer limit);

    /**
     *
     * @param contentVoExample
     * @param page
     * @param limit
     * @return
     */
    PageInfo<ContentVo> getArticleWithpage(ContentVoExample contentVoExample, Integer page, Integer limit);

    /**
     * 根据id删除文章
     *
     * @param cid
     */
    void deleteByCid(Integer cid);

    /**
     * 修改文章
     *
     * @param contentVo
     */
    void updateArticle(ContentVo contentVo);

    /**
     * 更改文章的所在分类
     *
     * @param ordinal
     * @param newCategory
     */
    void updateCategory(String ordinal, String newCategory);
}
