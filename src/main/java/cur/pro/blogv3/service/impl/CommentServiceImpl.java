package cur.pro.blogv3.service.impl;

import com.github.pagehelper.PageInfo;
import cur.pro.blogv3.dao.CommentVoMapper;
import cur.pro.blogv3.exception.TipException;
import cur.pro.blogv3.modal.Bo.CommentBo;
import cur.pro.blogv3.modal.Vo.CommentVo;
import cur.pro.blogv3.modal.Vo.CommentVoExample;
import cur.pro.blogv3.modal.Vo.ContentVo;
import cur.pro.blogv3.service.ICommentService;
import cur.pro.blogv3.service.IContentService;
import cur.pro.blogv3.utils.DateKit;
import cur.pro.blogv3.utils.TaleUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class CommentServiceImpl implements ICommentService {

    @Resource
    private CommentVoMapper commentVoMapper;
    @Resource
    private IContentService contentService;

    @Override
    public void insertComment(CommentVo commentVo) {
        if (null == commentVo) {
            throw new TipException("评论对象为空");
        }
        if (StringUtils.isBlank(commentVo.getAuthor())) {
            commentVo.setAuthor("热心网友");
        }
        if (StringUtils.isNotBlank(commentVo.getMail()) && !TaleUtils.isEmail(commentVo.getMail())) {
            throw new TipException("请输入正确的邮箱格式");
        }
        if (StringUtils.isBlank(commentVo.getContent())) {
            throw new TipException("评论内容不能为空");
        }
        if (commentVo.getContent().length() < 5 || commentVo.getContent().length() > 2000) {
            throw new TipException("评论字数在5-2000个字符");
        }
        if (null == commentVo.getCid()) {
            throw new TipException("评论文章不能为空");
        }
        ContentVo contents = contentService.getContents(String.valueOf(commentVo.getCid()));
        if (null == contents) {
            throw new TipException("不存在的文章");
        }

        commentVo.setOwnerId(contents.getAuthorId());
        commentVo.setCreated(DateKit.getCurrentUnixTime());
        commentVoMapper.insertSelective(commentVo);

        ContentVo temp = new ContentVo();
        temp.setCid(contents.getCid());
        temp.setCommentsNum(contents.getCommentsNum() + 1);
        contentService.updateContentByCid(temp);

    }

    @Override
    public PageInfo<CommentBo> getComments(Integer cid, int page, int limit) {
        return null;
    }

    @Override
    public PageInfo<CommentVo> getCommentsWithPage(CommentVoExample commentVoExample, int page, int limit) {
        return null;
    }

    @Override
    public CommentVo getCommentById(Integer coid) {
        return null;
    }

    @Override
    public void delete(Integer coid, Integer cid) {

    }

    @Override
    public void update(CommentVo commentVo) {

    }
}
