package cur.pro.blogv3.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.vdurmont.emoji.EmojiParser;
import cur.pro.blogv3.constant.WebConst;
import cur.pro.blogv3.dao.ContentVoMapper;
import cur.pro.blogv3.dao.MetaVoMapper;
import cur.pro.blogv3.dto.Types;
import cur.pro.blogv3.exception.TipException;
import cur.pro.blogv3.modal.Vo.ContentVo;
import cur.pro.blogv3.modal.Vo.ContentVoExample;
import cur.pro.blogv3.modal.Vo.MetaVo;
import cur.pro.blogv3.service.IContentService;
import cur.pro.blogv3.service.IMetaService;
import cur.pro.blogv3.service.IRelationshipService;
import cur.pro.blogv3.utils.DateKit;
import cur.pro.blogv3.utils.TaleUtils;
import cur.pro.blogv3.utils.Tools;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class ContentServiceImpl implements IContentService {

    @Resource
    private ContentVoMapper contentVoMapper;

    @Resource
    private MetaVoMapper metaVoMapper;

    @Resource
    private IRelationshipService relationshipService;

    @Resource
    private IMetaService metaService;

    @Override
    public void publish(ContentVo contents) {
        if (null == contents) {
            throw new TipException("文章对象为空");
        }
        if (StringUtils.isBlank(contents.getTitle())) {
            throw new TipException("文章标题不能为空");
        }
        if (StringUtils.isBlank(contents.getContent())) {
            throw new TipException("文章内容不能为空");
        }
        int titleLength = contents.getTitle().length();
        if (titleLength > WebConst.MAX_TITLE_COUNT) {
            throw new TipException("文章标题过长");
        }
        int contentLength = contents.getContent().length();
        if (contentLength > WebConst.MAX_TEXT_COUNT) {
            throw new TipException("文章内容过长");
        }
        if (null == contents.getAuthorId()) {
            throw new TipException("请登录后发布文章");
        }
        if (StringUtils.isNotBlank(contents.getSlug())) {
            if (contents.getSlug().length() < 5) {
                throw new TipException("路径太短了");
            }
            if (!TaleUtils.isPath(contents.getSlug())) throw new TipException("您输入的路径不合法");
            ContentVoExample contentVoExample = new ContentVoExample();
            contentVoExample.createCriteria().andTypeEqualTo(contents.getType()).andStatusEqualTo(contents.getSlug());
            long count = contentVoMapper.countByExample(contentVoExample);
            if (count > 0) throw new TipException("该路径已经存在，请重新输入");
        } else {
            contents.setSlug(null);
        }

        contents.setContent(EmojiParser.parseToAliases(contents.getContent()));

        int time = DateKit.getCurrentUnixTime();
        contents.setCreated(time);
        contents.setModified(time);
        contents.setHits(0);
        contents.setCommentsNum(0);

        String tags = contents.getTags();
        String categories = contents.getCategories();
        contentVoMapper.insert(contents);
        Integer cid = contents.getCid();

        metaService.setMetas(cid, tags, Types.TAG.getType());
        metaService.setMetas(cid, categories, Types.CATEGORY.getType());
    }

    @Override
    public PageInfo<ContentVo> getContents(Integer p, Integer limit) {
        log.debug("Enter getContents method");
        ContentVoExample example = new ContentVoExample();
        example.setOrderByClause("created desc");
        example.createCriteria().andTypeEqualTo(Types.ARTICLE.getType()).andStatusEqualTo(Types.PUBLISH.getType());
        PageHelper.startPage(p, limit);
        List<ContentVo> data = contentVoMapper.selectByExampleWithBLOBs(example);
        PageInfo<ContentVo> pageInfo = new PageInfo<>(data);
        log.debug("Eit getContents method");

        return pageInfo;
    }

    @Override
    public ContentVo getContents(String id) {
        if (StringUtils.isNotBlank(id)) {
            if (Tools.isNumber(id)) {
                ContentVo contentVo = contentVoMapper.selectByPrimaryKey(Integer.valueOf(id));
                if (contentVo != null) {
                    //点击率
                    contentVo.setHits(contentVo.getHits() + 1);
                    contentVoMapper.updateByPrimaryKey(contentVo);
                }
                return contentVo;
            } else {
                ContentVoExample contentVoExample = new ContentVoExample();
                contentVoExample.createCriteria().andSlugEqualTo(id);
                List<ContentVo> contentVos = contentVoMapper.selectByExampleWithBLOBs(contentVoExample);
                if (contentVos.size() != 1) {
                    throw new TipException("query content by id and return is not one");
                }
                return contentVos.get(0);
            }
        }
        return null;
    }

    @Override
    public void updateContentByCid(ContentVo contentVo) {
        if (null != contentVo && null != contentVo.getCid()) {
            contentVoMapper.updateByPrimaryKeySelective(contentVo);
        }
    }

    @Override
    public PageInfo<ContentVo> getArticles(Integer mid, int page, int limit) {
        int total = metaVoMapper.countWithSql(mid);
        PageHelper.startPage(page, limit);
        List<ContentVo> list = contentVoMapper.findByCatalog(mid);
        PageInfo<ContentVo> pageInfo = new PageInfo<>(list);
        pageInfo.setTotal(total);
        return pageInfo;
    }

    @Override
    public PageInfo<ContentVo> getArticles(String keyword, Integer page, Integer limit) {
        PageHelper.startPage(page, limit);
        ContentVoExample contentVoExample = new ContentVoExample();
        ContentVoExample.Criteria criteria = contentVoExample.createCriteria();
        criteria.andTypeEqualTo(Types.ARTICLE.getType());
        criteria.andStatusEqualTo(Types.PUBLISH.getType());
        criteria.andTitleLike("%" + keyword + "%");
        contentVoExample.setOrderByClause("created desc");
        List<ContentVo> contentVos = contentVoMapper.selectByExampleWithBLOBs(contentVoExample);

        return new PageInfo<>(contentVos);
    }

    @Override
    public PageInfo<ContentVo> getArticleWithpage(ContentVoExample contentVoExample, Integer page, Integer limit) {
        PageHelper.startPage(page, limit);
        List<ContentVo> contentVos = contentVoMapper.selectByExampleWithBLOBs(contentVoExample);

        return new PageInfo<>(contentVos);
    }

    @Override
    public void deleteByCid(Integer cid) {
        ContentVo contentVo = this.getContents(cid + "");
        if (null != contentVo) {
            contentVoMapper.deleteByPrimaryKey(cid);
            relationshipService.deleteById(cid, null);

        }

    }

    @Override
    public void updateArticle(ContentVo contentVo) {
        if (null == contentVo || null == contentVo.getCid()) {
            throw new TipException("文章对象不能为空");
        }
        if (StringUtils.isBlank(contentVo.getTitle())) {
            throw new TipException("文章标题不能为空");
        }
        if (StringUtils.isBlank(contentVo.getContent())) {
            throw new TipException("文章内容不能为空");
        }
        if (contentVo.getTitle().length() > 200) {
            throw new TipException("文章标题过长");
        }
        if (contentVo.getContent().length() > 65000) {
            throw new TipException("文章内容过长");
        }
        if (null == contentVo.getAuthorId()) {
            throw new TipException("请登录后发布文章");
        }
        if (StringUtils.isBlank(contentVo.getSlug())) {
            contentVo.setSlug(null);
        }
        int time = DateKit.getCurrentUnixTime();
        contentVo.setModified(time);
        Integer cid = contentVo.getCid();
        contentVo.setContent(EmojiParser.parseToAliases(contentVo.getContent()));

        contentVoMapper.updateByPrimaryKeySelective(contentVo);
        relationshipService.deleteById(cid, null);
        metaService.setMetas(cid, contentVo.getTags(), Types.TAG.getType());
        metaService.setMetas(cid, contentVo.getCategories(), Types.CATEGORY.getType());

    }

    @Override
    public void updateCategory(String ordinal, String newCategory) {
        ContentVo contentVo = new ContentVo();
        contentVo.setCategories(newCategory);
        ContentVoExample example = new ContentVoExample();
        example.createCriteria().andCategoriesEqualTo(ordinal);
        contentVoMapper.updateByExampleSelective(contentVo, example);
    }
}
