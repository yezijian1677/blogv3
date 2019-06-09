package cur.pro.blogv3.service.impl;

import com.github.pagehelper.PageHelper;
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
import java.util.ArrayList;
import java.util.List;

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

        //更新评论所属文章的状态
        ContentVo temp = new ContentVo();
        temp.setCid(contents.getCid());
        temp.setCommentsNum(contents.getCommentsNum() + 1);
        contentService.updateContentByCid(temp);

    }

    @Override
    public PageInfo<CommentBo> getComments(Integer cid, int page, int limit) {

        if (null != cid) {
            PageHelper.startPage(page, limit);

            CommentVoExample commentVoExample = new CommentVoExample();
            commentVoExample.createCriteria().andCidEqualTo(cid).andParentEqualTo(0);
            commentVoExample.setOrderByClause("coid desc");

            List<CommentVo> parents = commentVoMapper.selectByExampleWithBLOBs(commentVoExample);
            PageInfo<CommentVo> commentVoPageInfo = new PageInfo<>(parents);
            PageInfo<CommentBo> returnBo = copyPageInfo(commentVoPageInfo);
            if (parents.size() != 0) {
                List<CommentBo> commentBos = new ArrayList<>(parents.size());
                parents.forEach(parent->{
                    CommentBo commentBo = new CommentBo(parent);
                    commentBos.add(commentBo);
                });
                returnBo.setList(commentBos);
            }
            return returnBo;
        }
        return null;
    }

    @Override
    public PageInfo<CommentVo> getCommentsWithPage(CommentVoExample commentVoExample, int page, int limit) {
        PageHelper.startPage(page, limit);
        List<CommentVo> commentVos = commentVoMapper.selectByExampleWithBLOBs(commentVoExample);
        PageInfo<CommentVo> pageInfo = new PageInfo<>(commentVos);

        return pageInfo;
    }

    @Override
    public CommentVo getCommentById(Integer coid) {
        if (null != coid) {
            return commentVoMapper.selectByPrimaryKey(coid);
        }
        return null;
    }

    @Override
    public void delete(Integer coid, Integer cid) {
        if (null == coid) {
            throw new TipException("主键为空");
        }
        commentVoMapper.deleteByPrimaryKey(coid);

        //减少content下的评论数量
        ContentVo contents = contentService.getContents(cid + "");
        if (null != contents && contents.getCommentsNum() > 0) {
            ContentVo temp = new ContentVo();
            temp.setCid(cid);
            temp.setCommentsNum(contents.getCommentsNum() - 1);
            contentService.updateContentByCid(temp);
        }
    }

    @Override
    public void update(CommentVo commentVo) {
        if (null != commentVo && null != commentVo.getCoid()) {
            commentVoMapper.updateByPrimaryKeyWithBLOBs(commentVo);
        }
    }

    /**
     * copy原有的分页信息，除数据
     *
     * @param ordinal
     * @param <T>
     * @return
     */
    private <T> PageInfo<T> copyPageInfo(PageInfo ordinal) {
        PageInfo<T> returnBo = new PageInfo<T>();
        returnBo.setPageSize(ordinal.getPageSize());
        returnBo.setPageNum(ordinal.getPageNum());
        returnBo.setEndRow(ordinal.getEndRow());
        returnBo.setTotal(ordinal.getTotal());
        returnBo.setHasNextPage(ordinal.isHasNextPage());
        returnBo.setHasPreviousPage(ordinal.isHasPreviousPage());
        returnBo.setIsFirstPage(ordinal.isIsFirstPage());
        returnBo.setIsLastPage(ordinal.isIsLastPage());
        returnBo.setNavigateFirstPage(ordinal.getNavigateFirstPage());
        returnBo.setNavigateLastPage(ordinal.getNavigateLastPage());
        returnBo.setNavigatepageNums(ordinal.getNavigatepageNums());
        returnBo.setSize(ordinal.getSize());
        returnBo.setPrePage(ordinal.getPrePage());
        returnBo.setNextPage(ordinal.getNextPage());
        return returnBo;
    }
}
