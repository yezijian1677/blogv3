package cur.pro.blogv3.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import cur.pro.blogv3.modal.Bo.CommentBo;
import cur.pro.blogv3.modal.Vo.CommentVo;
import cur.pro.blogv3.modal.Vo.CommentVoExample;

public interface ICommentService {

    /**
     * 保存评论
     *
     * @param commentVo
     */
    void insertComment(CommentVo commentVo);

    /**
     * 获取文章的评论
     *
     * @param cid
     * @param page
     * @param limit
     * @return
     */
    PageInfo<CommentBo> getComments(Integer cid, int page, int limit);

    /**
     * 获取文章评论
     *
     * @param commentVoExample
     * @param page
     * @param limit
     * @return
     */
    PageInfo<CommentVo> getCommentsWithPage(CommentVoExample commentVoExample, int page, int limit);

    /**
     * 根据主键查询评论
     *
     * @param coid
     * @return
     */
    CommentVo getCommentById(Integer coid);

    /**
     * 删除评论
     *
     * @param coid
     * @param cid
     */
    void delete(Integer coid, Integer cid);

    /**
     * 更新评论状态
     * @param commentVo
     */
    void update(CommentVo commentVo);



}
